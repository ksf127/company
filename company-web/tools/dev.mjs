#!/usr/bin/env node
/**
 * 本地联调：shell + 清单里所有 enabled 的微前端一起启动。
 * 用法：npm run dev            # 全部
 *       npm run dev -- order   # 只启动 shell 与路由匹配的若干应用
 */
import { spawn } from 'node:child_process';
import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const registry = JSON.parse(await readFile(path.join(root, 'registry/remotes.json'), 'utf8'));
const filters = process.argv.slice(2).filter((arg) => !arg.startsWith('-'));

const apps = registry.apps.filter((app) => app.enabled !== false);
const selected = filters.length === 0 ? apps : apps.filter((app) => filters.includes(app.id));

if (filters.length > 0 && selected.length !== filters.length) {
  const missing = filters.filter((id) => !apps.some((app) => app.id === id));
  console.error(`未在 registry/remotes.json 找到应用：${missing.join(', ')}`);
  process.exit(1);
}

const workspaces = ['@company/shell', ...selected.map((app) => `@company/app-${app.id}`)];
console.log(`启动 ${workspaces.length} 个进程：\n  ${workspaces.join('\n  ')}\n`);

const children = workspaces.map((workspace) => {
  const child = spawn('npm', ['run', 'dev', '--workspace', workspace], {
    cwd: root,
    stdio: 'inherit',
    shell: true
  });
  child.on('exit', (code) => {
    if (code !== 0) {
      console.error(`[${workspace}] 退出，code=${code}`);
    }
  });
  return child;
});

// 任何一个子进程退出（包括 Ctrl+C）都要带走其它进程，避免端口被占
function shutdown(exitCode) {
  for (const child of children) {
    if (!child.killed) {
      child.kill();
    }
  }
  process.exit(exitCode);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));

const firstExit = await Promise.race(
  children.map(
    (child) =>
      new Promise((resolve) => {
        child.on('exit', (code) => resolve(code ?? 1));
      })
  )
);
shutdown(firstExit);
