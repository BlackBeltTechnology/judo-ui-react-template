import { createHash } from 'node:crypto';
import process from 'node:process';
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { parse } from '@babel/parser';
import { importSpecifier, identifier } from '@babel/types';
import traverse from '@babel/traverse';
import generate from '@babel/generator';
import { build } from 'esbuild';

function rollupPluginEsmer(opts = {}) {
    const { enabled, root, filesPattern } = {
        enabled: true,
        root: join(process.cwd(), 'src'),
        cachePath: join(process.cwd(), 'node_modules', '.esmer', 'cache'),
        targetPath: join(process.cwd(), 'dist', 'assets'),
        filesPattern: /.*\.(js|ts|jsx|tsx|mjs|mts|cjs|cts)$/,
        ...opts,
    };
    const imports = {};
    let outputDir;

    if (!enabled) {
        return {
            name: 'esmer',
        };
    }

    return {
        name: 'esmer',
        async options(options) {
            processFilesRecursively(root, filesPattern, (entryPath, code) => {
                if (!excludeAssetImport(entryPath)) {
                    const ast = parse(code, {
                        sourceType: 'module',
                        plugins: ['jsx', 'typescript'],
                    });

                    traverse.default(ast, {
                        ImportDeclaration(path) {
                            // We don't care about type imports since they are removed in JS
                            if (path.node.importKind !== 'type') {
                                const importPath = path.node.source.value;
                                const importSpecifiers = path.node.specifiers.map(specifier => ({
                                    specifier: specifier.type === 'ImportDefaultSpecifier' ? specifier.local.name : specifier.imported.name,
                                    isDefaultSpecifier: specifier.type === 'ImportDefaultSpecifier',
                                }));

                                if (importPath && !isLocalImport(importPath) && !excludeAssetImport(importPath)) {
                                    const data = Array.from(new Set([...(imports[importPath] || []), ...importSpecifiers]));
                                    if (!imports[importPath]) {
                                        imports[importPath] = [];
                                    }

                                    // Later on we roll every import token under a single vendor file, therefore we are
                                    // creating an alias for every token which consists of the package + token name to
                                    // ensure uniqueness
                                    imports[importPath] = data.map(d => ({...d, alias: keyToAlias(d.specifier, importPath)}));
                                }
                            }
                        },
                    });
                }
            });

            // The jsx-runtime is a special beast, needs explicit handling, if the project has React
            if (Object.keys(imports).includes('react')) {
                imports['react/jsx-runtime'] = [
                    { specifier: 'Fragment', isDefaultSpecifier: false, alias: keyToAlias('Fragment', 'react/jsx-runtime') },
                    { specifier: 'jsx', isDefaultSpecifier: false, alias: keyToAlias('jsx', 'react/jsx-runtime') },
                    { specifier: 'jsxs', isDefaultSpecifier: false, alias: keyToAlias('jsxs', 'react/jsx-runtime') },
                ];
            }

            // Keep potential existing exports, and add our own
            options.external = [
                ...(options.external || []),
                ...Array.from(Object.keys(imports)),
            ];

            // The minifyInternalExports parameter HAS TO BE FALSE so that external lib tokens are not minified
            options.output = {
                ...options.output,
                minifyInternalExports: false,
            };

            return options;
        },
        outputOptions(options) {
            outputDir = options.dir || dirname(options.file);
        },
        async generateBundle(options, bundle) {
            // Calculate hash based on imports, and generate vendor bundle (if not present)
            const hash = hashText(JSON.stringify(imports, null, 4));
            await generateVendor(imports, hash);

            // Go through every chuck in our bundle
            Object.keys(bundle).forEach(id => {
                // Skip files which do not need processing
                if (!filesPattern.test(id)) {
                    return null;
                }

                const chunkData = bundle[id];

                const ast = parse(chunkData.code, {
                    sourceType: 'module',
                    plugins: ['jsx', 'typescript'],
                });

                // Remove all import declarations which do not have any specifiers in them (to prevent bare import generation)
                const astImportsToRemove = ast.program.body.filter(n => n.type === 'ImportDeclaration' && Array.isArray(n.specifiers) && n.specifiers.length === 0);

                for (const imp of astImportsToRemove) {
                    const idx = ast.program.body.findIndex(n => n === imp);
                    if (idx > -1) {
                        ast.program.body.splice(idx, 1);
                    }
                }

                const paths = Object.keys(imports);

                // Overwrite import declarations in our chunk:
                // - use our specifier names
                // - source should always be the hashed vendor file relative path
                traverse.default(ast, {
                    ImportDeclaration(path) {
                        const sourceValue = path.node.source.value;
                        if (paths.includes(sourceValue)) {
                            const specs = imports[sourceValue];
                            path.node.specifiers.forEach((specifier, specIdx) => {
                                if (specifier.type === 'ImportDefaultSpecifier') {
                                    const ourSpec = specs.find(s => s.isDefaultSpecifier);
                                    if (ourSpec) {
                                        path.node.specifiers[specIdx] = importSpecifier(identifier(specifier.local.name), identifier(ourSpec.alias));
                                        path.node.source.value = `./vendor-${hash}.js`;
                                    }
                                } else {
                                    const ourSpec = specs.find(s => s.specifier === specifier.imported.name);
                                    if (ourSpec) {
                                        specifier.imported.name = ourSpec.alias;
                                        path.node.source.value = `./vendor-${hash}.js`;
                                    }
                                }
                            });
                        }
                    },
                });

                // Generate transformed code
                const transformed = generate.default(ast, {
                    ast: true,
                    code: true,
                    configFile: false,
                });

                // Replace the bundle code with the transformed one
                chunkData.code = transformed.code;

                if (chunkData.map) {
                    chunkData.map = transformed.map;
                }
            });
        },
    };
}

function vendorFileName(hash) {
    return `vendor-${hash}.js`;
}

function vendorTempPath(hash) {
    return join(process.cwd(), 'node_modules', '.esmer', 'temp', vendorFileName(hash));
}

function vendorAssetPath(hash) {
    return join(process.cwd(), 'dist', 'assets', vendorFileName(hash));
}

function writeCode(targetFile, code) {
    const targetPath = dirname(targetFile);
    if (!existsSync(targetPath)) {
        mkdirSync(targetPath, { recursive: true });
    }
    writeFileSync(targetFile, code, { encoding: 'utf-8' });
}

async function generateVendor(imports, hash) {
    const cachedFile = vendorTempPath(hash);
    const targetFile = vendorAssetPath(hash);

    if (existsSync(cachedFile)) {
        const code = readFileSync(cachedFile).toString('utf-8');
        writeCode(targetFile, code);
        return;
    }

    const code = await buildThirdParties(imports, hash);

    writeCode(targetFile, code);
    writeCode(cachedFile, code);
}

function isLocalImport(importPath) {
    return importPath && importPath.startsWith('.') || importPath.startsWith('/') || importPath.startsWith('~');
}

function excludeAssetImport(importPath) {
    return importPath && importPath.match(/.*(css|scss|svg|json|ttf|woff|woff2|eot)$/g);
}

function processFilesRecursively(folderPath, regexPattern, callback) {
    try {
        const entries = readdirSync(folderPath);

        for (const entry of entries) {
            const entryPath = join(folderPath, entry);
            const stat = statSync(entryPath);

            if (stat.isDirectory()) {
                processFilesRecursively(entryPath, regexPattern, callback);
            } else if (stat.isFile() && regexPattern.test(entry)) {
                const fileContent = readFileSync(entryPath, 'utf-8');
                callback(entryPath, fileContent);
            }
        }
    } catch (error) {
        console.error('Error occurred while processing files:', error);
    }
}

async function buildThirdParties(imports, hash) {
    let code = '';
    for (const key in imports) {
        const contents = imports[key].filter(filterUniqueSpecifiers); // TODO: replace Array with Set later
        const defaultExport = contents.find(c => c.isDefaultSpecifier);
        const exportsList = contents.filter(c => !c.isDefaultSpecifier) || [];
        code += `import ${defaultExport ? `${keyToAlias(defaultExport.specifier, key)} ` : ''}${defaultExport && exportsList.length ? ', ' : ''}${exportsList.length ? '{' : ''} ${exportsList.map(e => `${e.specifier} as ${keyToAlias(e.specifier, key)}`).join(', ')}${exportsList.length ? ' }' : ''} from '${key}';`;
    }
    code += 'export {';
    for (const key in imports) {
        const contents = imports[key].filter(filterUniqueSpecifiers);
        const defaultExport = contents.find(c => c.isDefaultSpecifier);
        const exportsList = contents.filter(c => !c.isDefaultSpecifier) || [];
        code += `${defaultExport ? `${keyToAlias(defaultExport.specifier, key)} ` : ''}${defaultExport && exportsList.length ? ', ' : ''}${exportsList.map(e => keyToAlias(e.specifier, key)).join(', ')},`;
    }
    code += '};';
    return buildBundle(code, hash);
}

function filterUniqueSpecifiers({ specifier, isDefaultSpecifier }, index, arr) {
    return arr.findIndex(e => specifier === e.specifier && isDefaultSpecifier === e.isDefaultSpecifier) === index;
}

function keyToAlias(key, path) {
    return path.replaceAll(/[@\/-]/g, '__') + '__' + key;
}

async function buildBundle(code, hash) {
    const result = await build({
        format: 'esm',
        bundle: true,
        write: false,
        platform: 'browser',
        minify: true,
        define: {
            'process.env.NODE_ENV': '"production"',
        },
        stdin: {
            contents: code,
            resolveDir: process.cwd(),
            sourcefile: 'virtual-vendors.js', // Only in-memory file, not persisted
        },
    });

    return result.outputFiles[0].text;
}

function hashText(content, algo = 'md5') {
    const hashFunc = createHash(algo);
    hashFunc.update(content);
    return hashFunc.digest('hex');
}

export default rollupPluginEsmer;
