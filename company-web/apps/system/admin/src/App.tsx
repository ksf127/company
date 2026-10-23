import { useMemo, useState } from 'react';
import { Button } from '@company/ui';
import type { RemoteAppProps } from '@company/contracts';
import { createApi } from './api';
import { ConfigPage } from './pages/ConfigPage';
import { DeptPage } from './pages/DeptPage';
import { DictPage } from './pages/DictPage';
import { LoginLogPage } from './pages/LoginLogPage';
import { MenuPage } from './pages/MenuPage';
import { RolePage } from './pages/RolePage';
import { UserPage } from './pages/UserPage';

type TabKey = 'user' | 'role' | 'menu' | 'dept' | 'dict' | 'config' | 'loginLog';

const TABS: { key: TabKey; label: string }[] = [
  { key: 'user', label: '用户管理' },
  { key: 'role', label: '角色管理' },
  { key: 'menu', label: '菜单管理' },
  { key: 'dept', label: '部门管理' },
  { key: 'dict', label: '字典管理' },
  { key: 'config', label: '参数设置' },
  { key: 'loginLog', label: '登录日志' }
];

/**
 * 若依基础模块管理台：只依赖 contracts / sdk / ui，不 import shell、kernel 或其他 app。
 */
export default function AdminApp({ sdk }: RemoteAppProps) {
  const api = useMemo(() => createApi(sdk), [sdk]);
  const [tab, setTab] = useState<TabKey>('user');

  return (
    <div>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 }}>
        {TABS.map((item) => (
          <Button key={item.key} onClick={() => setTab(item.key)} disabled={tab === item.key}>
            {item.label}
          </Button>
        ))}
      </div>

      {tab === 'user' ? <UserPage api={api} /> : null}
      {tab === 'role' ? <RolePage api={api} /> : null}
      {tab === 'menu' ? <MenuPage api={api} /> : null}
      {tab === 'dept' ? <DeptPage api={api} /> : null}
      {tab === 'dict' ? <DictPage api={api} /> : null}
      {tab === 'config' ? <ConfigPage api={api} /> : null}
      {tab === 'loginLog' ? <LoginLogPage api={api} /> : null}
    </div>
  );
}
