import { useCallback, useEffect, useState } from 'react';
import type { RemoteAppProps } from '@company/contracts';
import { Button, Card, PageHeader, StatusTag } from '@company/ui';

interface DemoRecord {
  id: number;
  name: string;
  owner: string;
  status: string;
  remark: string;
  createdAt: string;
}

interface PageResult {
  total: number;
  rows: DemoRecord[];
}

const API = '/demo-08/records';
const PAGE_SIZE = 5;

/**
 * 物流中心：最小可运行微前端。
 * 只依赖 contracts / sdk / ui；请求统一走注入的 sdk，跨应用通信走 eventBus。
 */
export default function Demo08App({ appId, basename, sdk }: RemoteAppProps) {
  const [rows, setRows] = useState<DemoRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [applied, setApplied] = useState('');
  const [name, setName] = useState('');
  const [owner, setOwner] = useState('admin');
  const [remark, setRemark] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const query = '?pageNum=' + pageNum + '&pageSize=' + PAGE_SIZE + '&keyword=' + encodeURIComponent(applied);
      const page = await sdk.get<PageResult>(API + query);
      setRows(page.rows);
      setTotal(page.total);
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [sdk, pageNum, applied]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleCreate(): Promise<void> {
    if (!name.trim()) {
      setError('名称不能为空');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await sdk.post(API, { name: name.trim(), owner: owner.trim() || 'admin', status: '0', remark });
      setName('');
      setRemark('');
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : '新增失败');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number): Promise<void> {
    setLoading(true);
    setError(null);
    try {
      await sdk.del(API + '/' + id);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : '删除失败');
    } finally {
      setLoading(false);
    }
  }

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  return (
    <div>
      <PageHeader
        title="物流中心"
        description={'应用 ' + appId + ' · 路由 ' + basename + ' · 后端 :8098/demo-08'}
      />

      <Card title="新增记录">
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="名称" style={inputStyle} />
          <input value={owner} onChange={(e) => setOwner(e.target.value)} placeholder="负责人" style={inputStyle} />
          <input value={remark} onChange={(e) => setRemark(e.target.value)} placeholder="备注" style={{ ...inputStyle, flex: 2 }} />
          <Button onClick={handleCreate} disabled={loading}>新增</Button>
        </div>
      </Card>

      <div style={{ height: 16 }} />

      <Card title={'记录列表（共 ' + total + ' 条）'}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="按名称 / 负责人搜索" style={inputStyle} />
          <Button onClick={() => { setPageNum(1); setApplied(keyword); }} disabled={loading}>查询</Button>
        </div>

        {error ? <p style={{ color: '#b91c1c', margin: '0 0 12px' }}>{error}</p> : null}

        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            <tr style={{ textAlign: 'left', color: '#6b7280' }}>
              <th style={thStyle}>ID</th>
              <th style={thStyle}>名称</th>
              <th style={thStyle}>负责人</th>
              <th style={thStyle}>状态</th>
              <th style={thStyle}>备注</th>
              <th style={thStyle}>创建时间</th>
              <th style={thStyle}>操作</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} style={{ borderTop: '1px solid #eef0f5' }}>
                <td style={tdStyle}>{row.id}</td>
                <td style={tdStyle}>{row.name}</td>
                <td style={tdStyle}>{row.owner}</td>
                <td style={tdStyle}>
                  <StatusTag text={row.status === '0' ? '正常' : '停用'} tone={row.status === '0' ? 'success' : 'danger'} />
                </td>
                <td style={tdStyle}>{row.remark}</td>
                <td style={tdStyle}>{row.createdAt.replace('T', ' ').slice(0, 19)}</td>
                <td style={tdStyle}>
                  <Button onClick={() => void handleDelete(row.id)} disabled={loading}>删除</Button>
                </td>
              </tr>
            ))}
            {rows.length === 0 ? (
              <tr>
                <td colSpan={7} style={{ ...tdStyle, color: '#9ca3af' }}>暂无数据</td>
              </tr>
            ) : null}
          </tbody>
        </table>

        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 12 }}>
          <Button onClick={() => setPageNum((n) => Math.max(1, n - 1))} disabled={loading || pageNum <= 1}>上一页</Button>
          <span style={{ color: '#6b7280', fontSize: 13 }}>第 {pageNum} / {totalPages} 页</span>
          <Button onClick={() => setPageNum((n) => Math.min(totalPages, n + 1))} disabled={loading || pageNum >= totalPages}>下一页</Button>
        </div>
      </Card>
    </div>
  );
}

const inputStyle = { padding: '8px 12px', border: '1px solid #d5d9e2', borderRadius: 8, flex: 1 };
const thStyle = { padding: '8px 6px', fontWeight: 500 };
const tdStyle = { padding: '10px 6px', verticalAlign: 'top' };
