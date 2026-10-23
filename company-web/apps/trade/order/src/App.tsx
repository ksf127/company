import { useState } from 'react';
import type { RemoteAppProps } from '@company/contracts';
import { Button, Card, PageHeader, StatusTag } from '@company/ui';

interface OrderView {
  id: number;
  userId: number;
  userName: string;
  amount: number;
  status: string;
  createdAt: string;
}

/**
 * 订单微前端：只依赖契约包、sdk、ui。
 * 不 import shell、不 import kernel、不 import 其他 app。
 */
export default function OrderApp({ appId, basename, sdk, eventBus }: RemoteAppProps) {
  const [orderId, setOrderId] = useState('');
  const [order, setOrder] = useState<OrderView | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleQuery(): Promise<void> {
    if (!orderId.trim()) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setOrder(await sdk.get<OrderView>(`/orders/${orderId.trim()}`));
    } catch (e) {
      setOrder(null);
      setError(e instanceof Error ? e.message : '查询失败');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(): Promise<void> {
    setLoading(true);
    setError(null);
    try {
      const created = await sdk.post<OrderView>('/orders', { userId: 1, amount: 99.9 });
      setOrder(created);
      setOrderId(String(created.id));
      // 跨应用联动只走事件总线，例如通知用户中心刷新最近订单
      eventBus.emit('order:created', created);
    } catch (e) {
      setError(e instanceof Error ? e.message : '创建失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="订单中心"
        description={`应用 ${appId} · 路由 ${basename}`}
        actions={<Button onClick={handleCreate} disabled={loading}>创建测试订单</Button>}
      />

      <Card title="按订单号查询">
        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <input
            value={orderId}
            onChange={(event) => setOrderId(event.target.value)}
            placeholder="订单 ID"
            style={{ flex: 1, padding: '8px 12px', border: '1px solid #d5d9e2', borderRadius: 8 }}
          />
          <Button onClick={handleQuery} disabled={loading || !orderId.trim()}>查询</Button>
        </div>

        {error ? <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p> : null}
        {order ? (
          <dl style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: 8, margin: 0 }}>
            <dt>订单号</dt>
            <dd style={{ margin: 0 }}>{order.id}</dd>
            <dt>用户</dt>
            <dd style={{ margin: 0 }}>{order.userName}（{order.userId}）</dd>
            <dt>金额</dt>
            <dd style={{ margin: 0 }}>{order.amount}</dd>
            <dt>状态</dt>
            <dd style={{ margin: 0 }}>
              <StatusTag text={order.status} tone={order.status === 'CREATED' ? 'success' : 'neutral'} />
            </dd>
            <dt>创建时间</dt>
            <dd style={{ margin: 0 }}>{order.createdAt}</dd>
          </dl>
        ) : null}
      </Card>
    </div>
  );
}
