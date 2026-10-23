import { createRoot } from 'react-dom/client';
import { EventBus, FrontendSdk } from '@company/sdk';
import App from './App';

/**
 * 独立运行入口：不经过 shell 也能调试单个微前端。
 */
const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token')
});

createRoot(document.getElementById('root')!).render(
  <App appId="demo05" basename="" sdk={sdk} eventBus={new EventBus()} />
);
