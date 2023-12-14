import { dirname, join } from 'node:path';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';

const __dirname = dirname(fileURLToPath(import.meta.url));
const PORT = 3000;
const API_URL = 'https://tatami-tests.judo.technology';
const subPaths = [
    'application_selection_test__actor_anon1',
    'application_selection_test__actor_anon2',
    'application_selection_test__actor_with_other_realm',
    'application_selection_test__actor_with_realm',
    'application_selection_test__actor_with_same_realm',
];

const app = express();

const proxy = createProxyMiddleware({
    target: API_URL,
    changeOrigin: true,
    pathRewrite: {
        [`^/api/`]: '/api/',
    },
});

app.use('/api', proxy);

for (const subPath of subPaths) {
    app.use(modifyBaseHref(subPath));
    app.use(`/${subPath}`, express.static(actorPath(subPath)));
}

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});

function actorPath(subPath) {
    return join(__dirname, `${subPath}/target/frontend-react/dist`);
}

function modifyBaseHref(subPath) {
    return (req, res, next) => {
        if (req.originalUrl === `/${subPath}/index.html` || req.originalUrl === `/${subPath}` || req.originalUrl === `/${subPath}/` || req.originalUrl.startsWith(`/${subPath}/?`)) {
            const indexPath = join(actorPath(subPath), 'index.html');
            const indexContent = readFileSync(indexPath, 'utf-8');
            const modifiedContent = indexContent.replace('<base href="/">', `<base href="/${subPath}/">`);
            res.send(modifiedContent);
        } else {
            next();
        }
    };
}
