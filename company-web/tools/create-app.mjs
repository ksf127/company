#!/usr/bin/env node
/**
 * 脚手架：生成一个符合架构约束的微前端，并自动登记到 registry/remotes.json。
 * 用法：
 *   npm run create -- --id=payment --name=支付中心 --domain=trade --group=交易 --route=/trade/payment --port=5003
 */
import { mkdir, readFile, stat, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const options = parseArgs(process.argv.slice(2));

for (const key of ['id', 'name', 'route', 'port']) {
  if (!options[key]) {
    fail(`缺少参数 --${key}`);
  }
}

const id = options.id;
const domain = options.domain ?? 'common';
const group = options.group ?? '其他';
const port = Number(options.port);

if (!/^[a-z][a-z0-9-]*$/.test(id)) {
  fail('--id 必须是小写字母开头的 kebab-case，例如 payment');
}
if (!options.route.startsWith('/')) {
  fail('--route 必须以 / 开头');
}
if (!Number.isInteger(port) || port < 1024 || port > 65535) {
  fail('--port 必须是 1024-65535 的整数');
}

const registryPath = path.join(root, 'registry/remotes.json');
const registry = JSON.parse(await readFile(registryPath, 'utf8'));
if (registry.apps.some((app) => app.id === id)) {
  fail(`registry 中已存在应用 ${id}`);
}
if (registry.apps.some((app) => app.route === options.route)) {
  fail(`route ${options.route} 已被占用`);
}
const usedPorts = await collectPorts();
if (usedPorts.includes(port)) {
  fail(`端口 ${port} 已被占用（见 registry 与各应用 vite.config.ts）`);
}

const appRoot = path.join(root, 'apps', domain, id);
if (await exists(appRoot)) {
  fail(`目录 apps/${domain}/${id} 已存在`);
}

await mkdir(path.join(appRoot, 'src'), { recursive: true });
await writeFile(path.join(appRoot, 'package.json'), packageJson());
await writeFile(path.join(appRoot, 'vite.config.ts'), viteConfig());
await writeFile(path.join(appRoot, 'tsconfig.json'), tsconfig());
await writeFile(path.join(appRoot, 'index.html'), html());
await writeFile(path.join(appRoot, 'src/App.tsx'), appComponent());
await writeFile(path.join(appRoot, 'src/main.tsx'), mainEntry());

registry.apps.push({
  id,
  name: options.name,
  remoteName: `${id}App`,
  entry: `http://localhost:${port}/remoteEntry.js`,
  route: options.route,
  nav: { group, order: registry.apps.length * 10 + 10 },
  enabled: true
});
await writeFile(registryPath, `${JSON.stringify(registry, null, 2)}\n`);

console.log(`已创建微前端 apps/${domain}/${id}`);
console.log(`  package: @company/app-${id}`);
console.log(`  端口:    ${port}`);
console.log(`  路由:    ${options.route}`);
console.log(`\n下一步：npm install && npm run dev -- ${id}`);

function parseArgs(args) {
  return Object.fromEntries(
    args
      .filter((arg) => arg.startsWith('--'))
      .map((arg) => {
        const [key, value] = arg.replace(/^--/, '').split('=');
        return [key, value];
      })
  );
}

async function collectPorts() {
  const ports = [];
  for (const app of registry.apps) {
    const match = /:(\d+)\//.exec(app.entry);
    if (match) {
      ports.push(Number(match[1]));
    }
  }
  return ports;
}

async function exists(target) {
  try {
    await stat(target);
    return true;
  } catch {
    return false;
  }
}

function fail(message) {
  console.error(`[create-app] ${message}`);
  process.exit(1);
}

function packageJson() {
  return `${JSON.stringify(
    {
      name: `@company/app-${id}`,
      version: '1.0.0',
      private: true,
      type: 'module',
      scripts: {
        dev: `vite --port ${port} --strictPort`,
        build: 'vite build',
        preview: `vite preview --port ${port} --strictPort`
      },
      dependencies: {
        '@company/contracts': '*',
        '@company/sdk': '*',
        '@company/ui': '*',
        react: '19.3.1',
        'react-dom': '19.3.1'
      },
      devDependencies: {
        '@company/build-config': '*'
      }
    },
    null,
    2
  )}\n`;
}

function viteConfig() {
  return `import { defineRemote } from '@company/build-config';

export default defineRemote({
  name: '${id}App',
  port: ${port},
  exposes: {
    './App': './src/App.tsx'
  }
});
`;
}

function tsconfig() {
  return `${JSON.stringify(
    { extends: '../../../tsconfig.base.json', include: ['src', 'vite.config.ts'] },
    null,
    2
  )}\n`;
}

function html() {
  return `<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${options.name}（独立调试）</title>
  </head>
  <body style="margin: 0; background: #f5f6fa">
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
`;
}

function appComponent() {
  return `import type { RemoteAppProps } from '@company/contracts';
import { Card, PageHeader } from '@company/ui';

/** ${options.name}：只依赖 contracts / sdk / ui，跨应用协作走 eventBus */
export default function ${pascal(id)}App({ appId, basename, sdk, eventBus }: RemoteAppProps) {
  return (
    <div>
      <PageHeader title="${options.name}" description={\`应用 \${appId} · 路由 \${basename}\`} />
      <Card title="开始开发">
        <p style={{ margin: 0, color: '#6b7280' }}>
          使用注入的 sdk 调用后端（例如 sdk.get('/xxx')），使用 eventBus 与其它应用通信。
        </p>
      </Card>
    </div>
  );
}
`;
}

function mainEntry() {
  return `import { createRoot } from 'react-dom/client';
import { EventBus, FrontendSdk } from '@company/sdk';
import App from './App';

const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token')
});

createRoot(document.getElementById('root')!).render(
  <App appId="${id}" basename="" sdk={sdk} eventBus={new EventBus()} />
);
`;
}

function pascal(value) {
  return value
    .split('-')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}
