import { readFileSync } from 'node:fs';
import { normalize } from 'node:path';
import commonjs from '@rollup/plugin-commonjs';
import { nodeResolve } from '@rollup/plugin-node-resolve';
import clear from 'rollup-plugin-clear';
import replace from '@rollup/plugin-replace';
import esbuild from 'rollup-plugin-esbuild';

const pkg = JSON.parse(readFileSync(normalize('./package.json'), { encoding:'utf8', flag:'r' }).toString());
const deps = pkg.dependencies;
const depKeys = Object.keys(deps);

const baseConfig = {
    plugins: [
        replace({
            preventAssignment: false,
            values: {
                'process.env.NODE_ENV': JSON.stringify('production'),
            },
        }),
        commonjs(),
        nodeResolve(),
        esbuild({
            minify: true,
        }),
    ],
};

const mappings = [
    ['judo-data-api-common', '@judo/data-api-common', deps['@judo/data-api-common'], []],
    ['react-jsx-runtime', 'react/jsx-runtime', deps.react, ['react']],
    ['react-is', 'react-is', deps['react-is'], ['react']],
    ['react-dom-client', 'react-dom/client', deps['react-dom'], ['react', 'react-dom']],
    ['react-oidc-context', 'react-oidc-context', deps['react-oidc-context'], ['react', 'react-dom', 'react/jsx-runtime', 'oidc-client-ts']],
    ['oidc-client-ts', 'oidc-client-ts', deps['oidc-client-ts'], [/*'crypto-js', 'jwt-decode'*/]],
    ['mui-system', '@mui/system', deps['@mui/material'], ['@emotion/react', '@emotion/styled', 'react', 'react-dom', 'react/jsx-runtime']],
    ['mui-material-locale', '@mui/material/locale', deps['@mui/material'], []],
    ['mui-material', '@mui/material', deps['@mui/material'], ['@mui/system', '@mui/material/locale', '@emotion/react', '@emotion/styled', 'react', 'react-dom', 'react-is', 'react/jsx-runtime']],
    ['mui-x-data-grid', '@mui/x-data-grid', deps['@mui/x-data-grid'], ['@mui/material', '@mui/system', '@mui/material/locale', 'react', 'react-dom', 'react-is', 'react/jsx-runtime']],
    ['mui-x-date-pickers', '@mui/x-date-pickers', deps['@mui/x-date-pickers'], ['@emotion/react', '@emotion/styled', '@mui/material', '@mui/system', '@mui/material/locale', 'date-fns', 'dayjs', 'luxon', 'moment', 'react', 'react-dom', 'react-is']],
    ['mui-x-date-pickers-adapter-day-js', '@mui/x-date-pickers', deps['@mui/x-date-pickers'], ['@emotion/react', '@emotion/styled', '@mui/material', '@mui/system', '@mui/material/locale', 'date-fns', 'dayjs', 'luxon', 'moment', 'react', 'react-dom', 'react-is']],
    ['notistack', 'notistack', deps.notistack, ['react', 'react-dom', 'react-is']],
];

function generateConfigs(mappings) {
    return mappings.map(([entrypoint, depName, depVersion, externals]) => ({
        input: `external/${entrypoint}.tsx`,
        ...baseConfig,
        output: {
            format: 'system',
            file: `./dist/${depName}@${depVersion.replace(/[~^]/g, '')}/umd/${entrypoint}.min.js`,
        },
        external: [...externals],
    }));
}

export default [
    {
        input: 'src/index.ts',
        plugins: [
            clear({
                targets: ['dist'],
            }),
        ],
    },
    ...generateConfigs(mappings),
];
