import type { AppManifest, RegistryFile } from '@company/contracts';

/**
 * 清单是唯一的应用注册来源。
 * 重复的 id / route / remoteName 会让"到底加载了哪个应用"变得不可推理，直接拒绝启动。
 */
export async function loadRegistry(url = '/remotes.json'): Promise<AppManifest[]> {
  const response = await fetch(url, { cache: 'no-cache' });
  if (!response.ok) {
    throw new Error(`应用清单加载失败: ${response.status} ${url}`);
  }
  const file = (await response.json()) as RegistryFile;
  if (!file || !Array.isArray(file.apps)) {
    throw new Error(`应用清单格式错误: ${url}`);
  }
  return validate(file.apps.filter((app) => app.enabled !== false));
}

export function validate(apps: AppManifest[]): AppManifest[] {
  const ids = new Set<string>();
  const routes = new Set<string>();
  const remotes = new Set<string>();

  for (const app of apps) {
    if (!app.id || !app.name || !app.remoteName || !app.entry || !app.route) {
      throw new Error(`应用清单缺少必填字段: ${JSON.stringify(app)}`);
    }
    if (!app.route.startsWith('/')) {
      throw new Error(`应用 ${app.id} 的 route 必须以 / 开头`);
    }
    if (ids.has(app.id)) {
      throw new Error(`重复的应用 id: ${app.id}`);
    }
    if (routes.has(app.route)) {
      throw new Error(`重复的路由前缀: ${app.route}`);
    }
    if (remotes.has(app.remoteName)) {
      throw new Error(`重复的 remoteName: ${app.remoteName}`);
    }
    ids.add(app.id);
    routes.add(app.route);
    remotes.add(app.remoteName);
  }
  return apps;
}
