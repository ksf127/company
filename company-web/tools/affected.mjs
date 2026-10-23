#!/usr/bin/env node
/**
 * 受影响构建：100 个应用不可能每次全量构建。
 * 根据 git diff 计算需要重新构建的微前端，供 CI 只跑受影响的流水线。
 * 用法：npm run affected -- --base=origin/main [--build]
 */
import { execFileSync } from 'node:child_process';
import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const args = process.argv.slice(2);
const base = args.find((arg) => arg.startsWith('--base='))?.split('=')[1] ?? 'origin/main';
const shouldBuild = args.includes('--build');

const registry = JSON.parse(await readFile(path.join(root, 'registry/remotes.json'), 'utf8'));
const appIds = registry.apps.map((app) => app.id);

const changed = git(['diff', '--name-only', base]);
if (changed === null) {
  console.error(`无法解析基准分支 ${base}，请先 git fetch 或显式指定 --base`);
  process.exit(1);
}

const affected = new Set();
let shellChanged = false;
let sharedChanged = false;

for (const file of changed) {
  if (!file.startsWith('company-web/')) {
    continue;
  }
  const relative = file.slice('company-web/'.length);

  if (relative.startsWith('apps/')) {
    const id = relative.split('/')[2];
    if (appIds.includes(id)) {
      affected.add(id);
    }
    continue;
  }
  if (relative.startsWith('packages/')) {
    // contracts / sdk / ui / build-config 是所有应用的共同上游
    sharedChanged = true;
    continue;
  }
  if (relative.startsWith('shell/') || relative.startsWith('registry/')) {
    shellChanged = true;
  }
}

if (sharedChanged) {
  for (const id of appIds) {
    affected.add(id);
  }
  shellChanged = true;
}

const ids = [...affected].sort();
console.log(`基准：${base}`);
console.log(`共享依赖变更：${sharedChanged ? '是（全部应用受影响）' : '否'}`);
console.log(`需要构建的微前端（${ids.length}）：${ids.length > 0 ? ids.join(', ') : '无'}`);
console.log(`需要构建 shell：${shellChanged ? '是' : '否'}`);

if (shouldBuild && (ids.length > 0 || shellChanged)) {
  const { spawnSync } = await import('node:child_process');
  const result = spawnSync('node', ['tools/build.mjs', ...ids], { cwd: root, stdio: 'inherit' });
  process.exit(result.status ?? 1);
}

function git(gitArgs) {
  try {
    return execFileSync('git', gitArgs, { cwd: path.resolve(root, '..'), encoding: 'utf8' })
      .split('\n')
      .filter(Boolean);
  } catch {
    return null;
  }
}
