import { useEffect, useMemo, useState } from 'react';
import type { AppEventBus, AppManifest, AppSdk } from '@company/contracts';
import { RemoteHost } from '@company/kernel';
import { LoginPage } from './LoginPage';

interface Props {
  apps: AppManifest[];
  sdk: AppSdk;
  eventBus: AppEventBus;
}

export function App({ apps, sdk, eventBus }: Props) {
  const pathname = usePathname();
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  const groups = useMemo(() => {
    const grouped = new Map<string, AppManifest[]>();
    for (const app of apps) {
      const group = app.nav?.group ?? '其他';
      grouped.set(group, [...(grouped.get(group) ?? []), app]);
    }
    for (const list of grouped.values()) {
      list.sort((left, right) => (left.nav?.order ?? 0) - (right.nav?.order ?? 0));
    }
    return [...grouped.entries()];
  }, [apps]);

  const active = matchApp(apps, pathname);

  useEffect(() => {
    if (!active && pathname === '/' && apps.length > 0) {
      navigate(apps[0].route);
    }
  }, [active, apps, pathname]);

  // 远端通过事件总线请求跳转，不需要依赖 shell 的路由实现
  useEffect(() => eventBus.on('shell:navigate', (payload) => {
    if (typeof payload === 'string') {
      navigate(payload);
    }
  }), [eventBus]);

  // 未登录统一落到登录页，登录成功后回到原来的地址
  if (!token) {
    const returnTo = pathname === '/login' ? (apps[0]?.route ?? '/') : pathname;
    return (
      <LoginPage
        onSuccess={() => {
          setToken(localStorage.getItem('token'));
          navigate(returnTo);
        }}
      />
    );
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">Company Console</div>
        <nav>
          {groups.map(([group, list]) => (
            <div key={group} className="nav-group">
              <div className="nav-group-title">{group}</div>
              {list.map((app) => (
                <button
                  key={app.id}
                  type="button"
                  className={app.id === active?.id ? 'nav-item active' : 'nav-item'}
                  onClick={() => navigate(app.route)}
                >
                  {app.name}
                </button>
              ))}
            </div>
          ))}
        </nav>
        <footer className="sidebar-footer">
          <div>{apps.length} 个微前端已注册</div>
          <button
            type="button"
            className="logout"
            onClick={() => {
              localStorage.removeItem('token');
              setToken(null);
              navigate('/login');
            }}
          >
            退出登录
          </button>
        </footer>
      </aside>

      <main className="content">
        {active ? (
          <RemoteHost app={active} sdk={sdk} eventBus={eventBus} />
        ) : (
          <div className="remote-state">
            <p>未找到路由 {pathname} 对应的应用</p>
            <p className="hint">请检查 registry/remotes.json 中的 route 配置</p>
          </div>
        )}
      </main>
    </div>
  );
}

/** 最长前缀匹配：/trade/order/detail 仍然归属 /trade/order */
function matchApp(apps: AppManifest[], pathname: string): AppManifest | undefined {
  return apps
    .filter((app) => pathname === app.route || pathname.startsWith(`${app.route}/`))
    .sort((left, right) => right.route.length - left.route.length)[0];
}

function usePathname(): string {
  const [pathname, setPathname] = useState(() => window.location.pathname);
  useEffect(() => {
    const onPopState = () => setPathname(window.location.pathname);
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);
  return pathname;
}

function navigate(to: string): void {
  if (window.location.pathname === to) {
    return;
  }
  window.history.pushState(null, '', to);
  window.dispatchEvent(new PopStateEvent('popstate'));
}
