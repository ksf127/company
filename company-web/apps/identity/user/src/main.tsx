import { createRoot } from 'react-dom/client';
import { EventBus, FrontendSdk } from '@company/sdk';
import UserApp from './App';

const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token')
});

createRoot(document.getElementById('root')!).render(
  <UserApp appId="user" basename="" sdk={sdk} eventBus={new EventBus()} />
);
