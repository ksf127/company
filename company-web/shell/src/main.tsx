import { createRoot } from 'react-dom/client';
import { EventBus, loadRegistry, registerAppRemotes } from '@company/kernel';
import { FrontendSdk } from '@company/sdk';
import { App } from './App';
import './styles.css';

const sdk = new FrontendSdk({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api',
  getToken: () => localStorage.getItem('token'),
  onUnauthorized: () => {
    localStorage.removeItem('token');
    window.location.assign('/login');
  }
});

const eventBus = new EventBus();

async function bootstrap(): Promise<void> {
  const apps = await loadRegistry(import.meta.env.VITE_REGISTRY_URL ?? '/remotes.json');
  registerAppRemotes(apps);
  createRoot(document.getElementById('root')!).render(<App apps={apps} sdk={sdk} eventBus={eventBus} />);
}

bootstrap().catch((error: Error) => {
  const root = document.getElementById('root')!;
  root.innerHTML = '';
  const pre = document.createElement('pre');
  pre.style.cssText = 'padding:24px;color:#b91c1c;white-space:pre-wrap';
  pre.textContent = `主应用启动失败：${error.message}`;
  root.appendChild(pre);
});
