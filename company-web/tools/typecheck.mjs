import { execFileSync } from 'node:child_process';
import { readdirSync, existsSync } from 'node:fs';
import { join } from 'node:path';

/** 动态发现所有工作区（shell + apps/<domain>/<name>），新增应用无需改脚本 */
const root = process.cwd();
const projects = ['shell'];

for (const domain of readdirSync(join(root, 'apps'), { withFileTypes: true })) {
  if (!domain.isDirectory()) continue;
  for (const app of readdirSync(join(root, 'apps', domain.name), { withFileTypes: true })) {
    if (!app.isDirectory()) continue;
    const dir = join('apps', domain.name, app.name);
    if (existsSync(join(root, dir, 'tsconfig.json'))) projects.push(dir);
  }
}

let failed = 0;
// 直接调用本地 typescript 的 tsc 入口，避免 Windows 下 spawn .cmd 失败
const tsc = join(root, 'node_modules', 'typescript', 'bin', 'tsc');
for (const project of projects) {
  try {
    execFileSync(process.execPath, [tsc, '-p', `${project}/tsconfig.json`, '--noEmit'], {
      cwd: root,
      stdio: 'inherit'
    });
    console.log(`类型检查通过：${project}`);
  } catch {
    failed += 1;
    console.error(`类型检查失败：${project}`);
  }
}

if (failed > 0) {
  console.error(`共 ${failed} 个工作区类型检查失败`);
  process.exit(1);
}
