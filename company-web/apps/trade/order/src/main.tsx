import { createRoot } from 'react-dom/client';
import { EventBus, FrontendSdk } from '@company/sdk';
import OrderApp from './App';

/**
 * 独立运行入口：不经过 shell，便于单个团队本地开发与调试。
 * sdk / eventBus 的构造方式与 shell 注入的一致，避免"独立跑正常、集成后异常"。
 */
const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token')
});

createRoot(document.getElementById('root')!).render(
  <OrderApp appId="order" basename="" sdk={sdk} eventBus={new EventBus()} />
);
