import { loadRemote, registerRemotes } from '@module-federation/enhanced/runtime';
import type { AppManifest, RemoteAppModule, RemoteAppProps } from '@company/contracts';
import type { ComponentType } from 'react';

const REMOTE_TIMEOUT_MS = 8000;
const REMOTE_RETRY = 2;

const loaded = new Map<string, Promise<ComponentType<RemoteAppProps>>>();

/** 只注册清单里的远端，运行时按需加载，100 个应用也不会预加载 */
export function registerAppRemotes(apps: AppManifest[]): void {
  registerRemotes(
    apps.map((app) => ({
      name: app.remoteName,
      entry: app.entry,
      // MF2 产物 remoteEntry.js 是 ESM（export { init, get }），必须按 module 加载
      type: 'module' as const
    })),
    { force: true }
  );
}

export function loadRemoteApp(app: AppManifest): Promise<ComponentType<RemoteAppProps>> {
  const cached = loaded.get(app.id);
  if (cached) {
    return cached;
  }
  const task = retry(
    () => withTimeout(loadRemote<RemoteAppModule>(`${app.remoteName}/App`), REMOTE_TIMEOUT_MS, `加载 ${app.name} 超时`),
    REMOTE_RETRY
  ).then((module) => {
    const component = module?.default;
    if (typeof component !== 'function') {
      throw new Error(`远端 ${app.remoteName} 未正确导出 default 组件`);
    }
    return component;
  });

  // 失败要移除缓存，否则用户点击"重试"永远拿到同一个 rejected Promise
  task.catch(() => loaded.delete(app.id));
  loaded.set(app.id, task);
  return task;
}

function withTimeout<T>(task: Promise<T>, timeoutMs: number, message: string): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error(message)), timeoutMs);
    task.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error) => {
        clearTimeout(timer);
        reject(error);
      }
    );
  });
}

async function retry<T>(task: () => Promise<T>, times: number): Promise<T> {
  let lastError: unknown;
  for (let attempt = 0; attempt <= times; attempt += 1) {
    try {
      return await task();
    } catch (error) {
      lastError = error;
      await new Promise((resolve) => setTimeout(resolve, 300 * 2 ** attempt));
    }
  }
  throw lastError;
}
