import { fileURLToPath } from 'node:url';
import { defineHost } from '@company/build-config';

const rewrite = (path: string): string => path.replace(/^\/api/, '');

export default defineHost({
  name: 'shell',
  port: 5000,
  // 清单来自 registry/，开发与构建都直接使用同一份文件
  publicDir: fileURLToPath(new URL('../registry', import.meta.url)),
  // 开发期前端只认 /api 前缀，具体端口由这里决定；生产由网关承担同样的路由
  proxy: {
    '/api/auth': { target: 'http://localhost:8081', changeOrigin: true, rewrite },
    '/api/system': { target: 'http://localhost:8082', changeOrigin: true, rewrite },
    '/api/orders': { target: 'http://localhost:8083', changeOrigin: true, rewrite },
    '/api/users': { target: 'http://localhost:8084', changeOrigin: true, rewrite },
    '/api/demo-01': { target: 'http://localhost:8091', changeOrigin: true, rewrite },
    '/api/demo-02': { target: 'http://localhost:8092', changeOrigin: true, rewrite },
    '/api/demo-03': { target: 'http://localhost:8093', changeOrigin: true, rewrite },
    '/api/demo-04': { target: 'http://localhost:8094', changeOrigin: true, rewrite },
    '/api/demo-05': { target: 'http://localhost:8095', changeOrigin: true, rewrite },
    '/api/demo-06': { target: 'http://localhost:8096', changeOrigin: true, rewrite },
    '/api/demo-07': { target: 'http://localhost:8097', changeOrigin: true, rewrite },
    '/api/demo-08': { target: 'http://localhost:8098', changeOrigin: true, rewrite },
    '/api/demo-09': { target: 'http://localhost:8099', changeOrigin: true, rewrite },
    '/api/demo-10': { target: 'http://localhost:8100', changeOrigin: true, rewrite }
  }
});
