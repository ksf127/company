#!/usr/bin/env node
/**
 * 架构体检：CI 入口。规则不通过直接退出码 1。
 * 这些规则是"物理屏障"的前端版本——不依赖人记文档，靠脚本卡住合并。
 */
import { readFile, readdir, stat } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const errors = [];
const warnings = [];

const registry = await readJson(path.join(root, 'registry/remotes.json'));
const registryErrors = validateRegistry(registry);
for (const message of registryErrors) {
  errors.push(message);
}

const apps = registry.apps ?? [];
const appDirs = await listAppDirs();
const registeredIds = new Set(apps.map((app) => app.id));

// 1. 目录与清单必须双向一致：不能有"藏在仓库里但没注册"的应用，也不能有"注册了但没代码"的应用
for (const app of apps) {
  if (!appDirs.some((dir) => dir.id === app.id)) {
    errors.push(`registry 中的 ${app.id} 在 apps/ 下找不到目录`);
  }
}
for (const dir of appDirs) {
  if (!registeredIds.has(dir.id)) {
    errors.push(`apps/${dir.domain}/${dir.id} 未在 registry/remotes.json 注册`);
  }
}

// 2. 逐个应用检查依赖方向与暴露面
for (const dir of appDirs) {
  const appRoot = path.join(dir.path);
  await checkPackageJson(dir, appRoot);

  const viteConfig = await readText(path.join(appRoot, 'vite.config.ts'));
  if (!viteConfig.includes('defineRemote')) {
    errors.push(`${dir.id}: vite.config.ts 必须使用 @company/build-config 的 defineRemote`);
  }
  const exposeKeys = [...viteConfig.matchAll(/'(\.\/[^']+)'\s*:/g)].map((match) => match[1]);
  const unexpected = exposeKeys.filter((key) => key !== './App');
  if (unexpected.length > 0) {
    errors.push(`${dir.id}: 只允许暴露 './App'，发现 ${unexpected.join(', ')}`);
  }

  for (const file of await listSourceFiles(path.join(appRoot, 'src'))) {
    const source = await readFile(file, 'utf8');
    const relative = path.relative(root, file).replace(/\\/g, '/');
    for (const imported of extractImports(source)) {
      if (imported === '@company/kernel') {
        errors.push(`${relative}: 微前端禁止依赖 @company/kernel（加载与容错由 shell 负责）`);
      }
      if (imported === '@company/shell') {
        errors.push(`${relative}: 微前端禁止依赖 shell`);
      }
      if (/^@company\/app-/.test(imported) && imported !== `@company/app-${dir.id}`) {
        errors.push(`${relative}: 禁止跨应用直接 import（${imported}），请走事件总线或后端接口`);
      }
      if (imported.startsWith('.') && imported.split('/').includes('apps')) {
        errors.push(`${relative}: 禁止通过相对路径引用其它应用（${imported}）`);
      }
    }
  }
}

// 3. shell 不得反向依赖任何业务应用
for (const file of await listSourceFiles(path.join(root, 'shell/src'))) {
  const source = await readFile(file, 'utf8');
  const relative = path.relative(root, file).replace(/\\/g, '/');
  for (const imported of extractImports(source)) {
    if (/^@company\/app-/.test(imported)) {
      errors.push(`${relative}: shell 禁止直接 import 业务应用（${imported}），必须通过 registry + RemoteHost`);
    }
  }
}

// 4. 共享依赖只允许在 build-config 中定义，防止各应用各写一份 shared 规则
for (const dir of appDirs) {
  const config = await readText(path.join(dir.path, 'vite.config.ts'));
  if (/shared\s*:/.test(config)) {
    errors.push(`${dir.id}: 不允许自定义 shared 配置，统一由 @company/build-config 提供`);
  }
}

report();

function validateRegistry(file) {
  const problems = [];
  if (!file || typeof file !== 'object') {
    return ['registry/remotes.json 不是合法 JSON 对象'];
  }
  if (!Array.isArray(file.apps) || file.apps.length === 0) {
    return ['registry/remotes.json 缺少 apps 数组'];
  }
  const seen = { id: new Set(), remoteName: new Set(), route: new Set() };
  for (const app of file.apps) {
    if (!app.id || !app.name || !app.remoteName || !app.entry) {
      problems.push(`应用 ${JSON.stringify(app)} 缺少 id/name/remoteName/entry`);
      continue;
    }
    if (!app.route?.startsWith('/')) {
      problems.push(`${app.id}: route 必须以 / 开头`);
    }
    for (const key of ['id', 'remoteName', 'route']) {
      if (seen[key].has(app[key])) {
        problems.push(`${app.id}: ${key} 重复（${app[key]}）`);
      }
      seen[key].add(app[key]);
    }
    if (app.enabled !== false && !/^https?:\/\/|^\//.test(app.entry)) {
      problems.push(`${app.id}: entry 必须是绝对地址或以 / 开头的路径`);
    }
  }
  return problems;
}

async function listAppDirs() {
  const appsRoot = path.join(root, 'apps');
  const result = [];
  for (const domain of await safeReaddir(appsRoot)) {
    const domainPath = path.join(appsRoot, domain);
    if (!(await stat(domainPath)).isDirectory()) {
      continue;
    }
    for (const id of await safeReaddir(domainPath)) {
      const appPath = path.join(domainPath, id);
      if ((await stat(appPath)).isDirectory()) {
        result.push({ domain, id, path: appPath });
      }
    }
  }
  return result;
}

async function checkPackageJson(dir, appRoot) {
  const pkg = await readJson(path.join(appRoot, 'package.json'));
  if (!pkg) {
    errors.push(`${dir.id}: 缺少 package.json`);
    return;
  }
  if (pkg.name !== `@company/app-${dir.id}`) {
    errors.push(`${dir.id}: package.json 的 name 必须是 @company/app-${dir.id}，当前为 ${pkg.name}`);
  }
  for (const dependency of Object.keys({ ...pkg.dependencies, ...pkg.devDependencies })) {
    if (dependency === '@company/kernel' || dependency === '@company/shell') {
      errors.push(`${dir.id}: 禁止依赖 ${dependency}`);
    }
    if (/^@company\/app-/.test(dependency) && dependency !== pkg.name) {
      errors.push(`${dir.id}: 禁止依赖其它应用 ${dependency}`);
    }
  }
  if (!pkg.dependencies?.['@company/contracts']) {
    errors.push(`${dir.id}: 必须依赖 @company/contracts（远端契约）`);
  }
}

async function listSourceFiles(dir) {
  const result = [];
  for (const entry of await safeReaddir(dir)) {
    const full = path.join(dir, entry);
    const info = await stat(full);
    if (info.isDirectory()) {
      result.push(...(await listSourceFiles(full)));
    } else if (/\.(ts|tsx)$/.test(entry)) {
      result.push(full);
    }
  }
  return result;
}

function extractImports(source) {
  const imports = [];
  for (const match of source.matchAll(/(?:from\s+|import\s*\(\s*)['"]([^'"]+)['"]/g)) {
    imports.push(match[1]);
  }
  return imports;
}

async function safeReaddir(dir) {
  try {
    return await readdir(dir);
  } catch {
    return [];
  }
}

async function readJson(file) {
  const text = await readText(file);
  if (text === null) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch (error) {
    errors.push(`${path.relative(root, file)}: JSON 解析失败 - ${error.message}`);
    return null;
  }
}

async function readText(file) {
  try {
    return await readFile(file, 'utf8');
  } catch {
    return null;
  }
}

function report() {
  for (const warning of warnings) {
    console.warn(`[warn] ${warning}`);
  }
  if (errors.length === 0) {
    console.log(`架构体检通过：${apps.length} 个微前端，${appDirs.length} 个应用目录`);
    return;
  }
  for (const error of errors) {
    console.error(`[error] ${error}`);
  }
  console.error(`\n共 ${errors.length} 项不通过，请修复后重试。`);
  process.exit(1);
}
