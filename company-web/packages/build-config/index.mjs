import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';

/**
 * 全公司只允许这一份 MF 配置。
 * 100 个应用共享同一套 singleton 规则，避免出现两份 React 导致的 hooks 崩溃。
 */
const shared = {
  react: { singleton: true, requiredVersion: '^19.0.0' },
  'react-dom': { singleton: true, requiredVersion: '^19.0.0' },
  'react/jsx-runtime': { singleton: true, requiredVersion: '^19.0.0' }
};

/** 远端应用（业务微前端）：暴露 ./App，由 shell 装载 */
export function defineRemote({ name, port, exposes }) {
  return defineConfig({
    plugins: [
      react(),
      federation({
        name,
        filename: 'remoteEntry.js',
        exposes,
        shared
      })
    ],
    base: process.env.PUBLIC_PATH || '/',
    build: {
      target: 'chrome89',
      sourcemap: true
    },
    server: {
      port,
      strictPort: true,
      cors: true,
      origin: `http://localhost:${port}`
    },
    preview: {
      port,
      strictPort: true
    },
    optimizeDeps: {
      exclude: ['@company/kernel', '@company/contracts', '@company/sdk', '@company/ui']
    }
  });
}

/** 主应用（shell）：只负责布局、路由与加载远端，不包含任何业务代码 */
export function defineHost({ name, port, publicDir, proxy = {} }) {
  return defineConfig({
    plugins: [
      react(),
      federation({
        name,
        remotes: {},
        shared
      })
    ],
    publicDir,
    build: {
      target: 'chrome89',
      sourcemap: true
    },
    server: {
      port,
      strictPort: true,
      // 开发期把 /api/* 转发到各服务；生产由网关统一处理，前端代码不感知端口
      proxy
    },
    preview: {
      port,
      strictPort: true
    },
    optimizeDeps: {
      exclude: ['@company/kernel', '@company/contracts', '@company/sdk', '@company/ui']
    }
  });
}
