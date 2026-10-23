import type { UserConfig } from 'vite';

/** 远端应用（业务微前端）配置入参 */
export interface RemoteOptions {
  name: string;
  port: number;
  exposes: Record<string, string>;
}

/** 主应用（shell）配置入参 */
export interface HostOptions {
  name: string;
  port: number;
  publicDir: string;
  /** 开发代理：key 为路径前缀，value 为 Vite proxy 配置 */
  proxy?: Record<string, { target: string; changeOrigin?: boolean; rewrite?: (path: string) => string }>;
}

export declare function defineRemote(options: RemoteOptions): UserConfig;
export declare function defineHost(options: HostOptions): UserConfig;
