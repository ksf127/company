#!/usr/bin/env node
/**
 * 生产构建：按清单构建 shell 与全部远端，并生成生产清单。
 * 产物结构：
 *   dist/shell/            主应用（含 remotes.json）
 *   dist/apps/<id>/        每个微前端的 remoteEntry.js 与静态资源
 * registry 中的 entry 在构建期被改写为 /apps/<id>/remoteEntry.js，
 * 上 CDN 时只需整体上传 dist 并设置 PUBLIC_BASE。
 */
import { spawn } from 'node:child_process';
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const dist = path.join(root, 'dist');
const publicBase = process.env.PUBLIC_BASE ?? '';
const concurrency = Number(process.env.BUILD_CONCURRENCY ?? 4);
const only = process.argv.slice(2).filter((arg) => !arg.startsWith('-'));

const registry = JSON.parse(await readFile(path.join(root, 'registry/remotes.json'), 'utf8'));
const apps = registry.apps.filter((app) => app.enabled !== false);
const selected = only.length === 0 ? apps : apps.filter((app) => only.includes(app.id));

await rm(dist, { recursive: true, force: true });
await mkdir(dist, { recursive: true });

const startedAt = Date.now();
await runPool(selected, concurrency, (app) =>
  build(`@company/app-${app.id}`, {
    PUBLIC_PATH: `${publicBase}/apps/${app.id}/`,
    VITE_APP_ID: app.id
  }, path.join(dist, 'apps', app.id))
);

// 生产清单：entry 指向 CDN 路径，shell 构建完成后随主应用一起发布
const productionRegistry = {
  ...registry,
  apps: registry.apps.map((app) =>
    app.enabled === false
      ? app
      : { ...app, entry: `${publicBase}/apps/${app.id}/remoteEntry.js` }
  )
};

await build('@company/shell', {}, path.join(dist, 'shell'));
await writeFile(path.join(dist, 'shell', 'remotes.json'), JSON.stringify(productionRegistry, null, 2));

console.log(`\n构建完成，用时 ${((Date.now() - startedAt) / 1000).toFixed(1)}s`);
console.log(`  主应用：dist/shell`);
console.log(`  微前端：${selected.length} 个 → dist/apps/*`);

function build(workspace, env, outDir) {
  return new Promise((resolve, reject) => {
    console.log(`[build] ${workspace}`);
    const child = spawn(
      'npm',
      ['run', 'build', '--workspace', workspace, '--', '--outDir', outDir, '--emptyOutDir'],
      { cwd: root, stdio: 'inherit', shell: true, env: { ...process.env, ...env } }
    );
    child.on('exit', (code) =>
      code === 0 ? resolve() : reject(new Error(`${workspace} 构建失败，code=${code}`))
    );
  });
}

async function runPool(items, size, worker) {
  const queue = [...items];
  const runners = Array.from({ length: Math.min(size, queue.length) }, async () => {
    while (queue.length > 0) {
      const item = queue.shift();
      await worker(item);
    }
  });
  await Promise.all(runners);
}
