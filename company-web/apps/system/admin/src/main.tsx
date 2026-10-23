import { createRoot } from 'react-dom/client';
import { EventBus, FrontendSdk } from '@company/sdk';
import AdminApp from './App';

/**
 * 独立运行入口：与 shell 注入的 sdk / eventBus 语义一致，避免"独立跑正常、集成后异常"。
 */
const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token'),
  onUnauthorized: () => {
    localStorage.removeItem('token');
    window.location.assign('/login');
  }
});

createRoot(document.getElementById('root')!).render(
  <AdminApp appId="admin" basename="" sdk={sdk} eventBus={new EventBus()} />
);
