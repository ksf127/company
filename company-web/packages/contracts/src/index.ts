import type { ComponentType } from 'react';

/** 后端统一响应体，与 company-common-core 的 ApiResult 保持一致 */
export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface AppManifest {
  /** 全局唯一，约定与包名 @company/app-<id> 一致 */
  id: string;
  name: string;
  /** Module Federation 容器名，全局唯一 */
  remoteName: string;
  /** remoteEntry.js 的完整地址（生产环境由 CDN 提供） */
  entry: string;
  /** shell 负责匹配的顶层路由前缀，必须以 / 开头且全局唯一 */
  route: string;
  nav?: {
    group: string;
    order?: number;
  };
  /** 由 shell 做可见性过滤，远端不自行判断 */
  permissions?: string[];
  enabled?: boolean;
}

export interface RegistryFile {
  version: number;
  apps: AppManifest[];
}

/** shell 注入给远端的唯一上下文，远端不直接依赖 shell 或 kernel */
export interface RemoteAppProps {
  appId: string;
  basename: string;
  sdk: AppSdk;
  eventBus: AppEventBus;
}

export interface AppSdk {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
  put<T>(path: string, body?: unknown): Promise<T>;
  del<T>(path: string): Promise<T>;
}

export interface AppEventBus {
  emit(type: string, payload?: unknown): void;
  on(type: string, handler: (payload: unknown) => void): () => void;
}

export type RemoteAppModule = {
  default: ComponentType<RemoteAppProps>;
};
