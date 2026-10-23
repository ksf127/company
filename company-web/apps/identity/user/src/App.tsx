import { useEffect, useState } from 'react';
import type { RemoteAppProps } from '@company/contracts';
import { Button, Card, PageHeader } from '@company/ui';

interface UserView {
  id: number;
  username: string;
  displayName: string;
}

/** 用户微前端：订阅订单事件演示跨应用协作，但全程不 import 订单应用代码 */
export default function UserApp({ appId, basename, sdk, eventBus }: RemoteAppProps) {
  const [userId, setUserId] = useState('1');
  const [user, setUser] = useState<UserView | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [lastEvent, setLastEvent] = useState<string>('（暂无）');
  const [loading, setLoading] = useState(false);

  useEffect(
    () => eventBus.on('order:created', (payload) => {
      const order = payload as { id?: number; userId?: number } | undefined;
      setLastEvent(order?.id ? `收到新订单 ${order.id}` : '收到新订单事件');
    }),
    [eventBus]
  );

  async function handleQuery(): Promise<void> {
    setLoading(true);
    setError(null);
    try {
      setUser(await sdk.get<UserView>(`/users/${userId.trim()}`));
    } catch (e) {
      setUser(null);
      setError(e instanceof Error ? e.message : '查询失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader title="用户中心" description={`应用 ${appId} · 路由 ${basename}`} />

      <Card title="查询用户">
        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <input
            value={userId}
            onChange={(event) => setUserId(event.target.value)}
            placeholder="用户 ID"
            style={{ flex: 1, padding: '8px 12px', border: '1px solid #d5d9e2', borderRadius: 8 }}
          />
          <Button onClick={handleQuery} disabled={loading || !userId.trim()}>查询</Button>
        </div>

        {error ? <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p> : null}
        {user ? (
          <p style={{ margin: 0 }}>
            {user.id} · {user.username} · {user.displayName}
          </p>
        ) : null}
      </Card>

      <div style={{ height: 16 }} />

      <Card title="跨应用事件">
        <p style={{ margin: 0, color: '#6b7280' }}>{lastEvent}</p>
      </Card>
    </div>
  );
}
