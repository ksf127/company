import { type CSSProperties, type ReactNode, useEffect, useRef, useState } from 'react';
import { Button, Card, PageHeader } from '@company/ui';
import type { PageResult } from '../types';

const PAGE_SIZE = 10;

export interface Column<T> {
  title: string;
  render: (row: T) => ReactNode;
}

export interface Field {
  name: string;
  label: string;
  type?: 'text' | 'password' | 'number' | 'select' | 'textarea' | 'checks';
  options?: { label: string; value: string }[];
  placeholder?: string;
}

export interface CrudPageProps<T> {
  title: string;
  description?: string;
  columns: Column<T>[];
  fields: Field[];
  rowKey: (row: T) => string;
  load: (pageNum: number, pageSize: number, keyword: string) => Promise<PageResult<T>>;
  create: (form: Record<string, string>) => Promise<unknown>;
  update: (form: Record<string, string>) => Promise<unknown>;
  remove: (id: string) => Promise<unknown>;
  /** 编辑时把行数据转成表单草稿，必填字段用空串表示 */
  toForm: (row: T) => Record<string, string>;
  /** 编辑前补全表单（例如用户角色需要异步查询） */
  prepareEdit?: (row: T) => Promise<Record<string, string>>;
  idField: string;
  keywordPlaceholder?: string;
  /** 只读页面（例如登录日志）隐藏新增/编辑/删除 */
  editable?: boolean;
  /** 不传则使用页面内部搜索框的关键字 */
  toolbar?: ReactNode;
}

const tableStyle: CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: 14
};

const cellStyle: CSSProperties = {
  padding: '10px 8px',
  borderBottom: '1px solid #eef1f6',
  textAlign: 'left',
  whiteSpace: 'nowrap'
};

const inputStyle: CSSProperties = {
  width: '100%',
  padding: '8px 12px',
  border: '1px solid #d5d9e2',
  borderRadius: 8,
  fontSize: 14
};

/**
 * 通用 CRUD 页面：搜索、分页、表单、删除。
 * 若依各基础模块的差异只在字段与接口上，避免 7 个页面各写一遍同样的逻辑。
 */
export function CrudPage<T>(props: CrudPageProps<T>) {
  const { title, description, columns, fields, rowKey, load, create, update, remove, toForm, prepareEdit, idField, editable = true, keywordPlaceholder = '搜索', toolbar } = props;

  const [rows, setRows] = useState<T[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [applied, setApplied] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<Record<string, string> | null>(null);
  const [saving, setSaving] = useState(false);
  const [reloadTick, setReloadTick] = useState(0);

  // 页面传入的 load 每次渲染都是新函数，放到 ref 里避免触发无限请求
  const loadRef = useRef(load);
  loadRef.current = load;

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    loadRef.current(pageNum, PAGE_SIZE, applied)
      .then((result) => {
        if (!cancelled) {
          setRows(result.rows);
          setTotal(result.total);
          setError(null);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(messageOf(e));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [pageNum, applied, reloadTick]);

  const pages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  function reload(): void {
    setReloadTick((tick) => tick + 1);
  }

  function search(): void {
    setPageNum(1);
    setApplied(keyword.trim());
  }

  async function handleEdit(row: T): Promise<void> {
    setError(null);
    try {
      setForm(prepareEdit ? await prepareEdit(row) : toForm(row));
    } catch (e: unknown) {
      setError(messageOf(e));
    }
  }

  async function handleSave(): Promise<void> {
    if (!form) {
      return;
    }
    setSaving(true);
    setError(null);
    try {
      if (form[idField]) {
        await update(form);
      } else {
        await create(form);
      }
      setForm(null);
      reload();
    } catch (e: unknown) {
      setError(messageOf(e));
    } finally {
      setSaving(false);
    }
  }

  async function handleRemove(row: T): Promise<void> {
    if (!window.confirm('确认删除这条记录？')) {
      return;
    }
    setError(null);
    try {
      await remove(rowKey(row));
      reload();
    } catch (e: unknown) {
      setError(messageOf(e));
    }
  }

  function patch(name: string, value: string): void {
    setForm((current) => (current ? { ...current, [name]: value } : current));
  }

  return (
    <div>
      <PageHeader
        title={title}
        description={description}
        actions={editable ? <Button onClick={() => setForm(blankForm(fields))}>新增</Button> : undefined}
      />

      {form ? (
        <Card title={form[idField] ? '编辑' : '新增'}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: 12 }}>
            {fields.map((field) => (
              <label key={field.name} style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 13 }}>
                <span style={{ color: '#4b5563' }}>{field.label}</span>
                {renderField(field, form[field.name] ?? '', (value) => patch(field.name, value))}
              </label>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
            <Button onClick={handleSave} disabled={saving}>{saving ? '保存中…' : '保存'}</Button>
            <Button onClick={() => setForm(null)}>取消</Button>
          </div>
        </Card>
      ) : null}

      <div style={{ height: form ? 16 : 0 }} />

      <Card>
        <div style={{ display: 'flex', gap: 8, marginBottom: 16, alignItems: 'center' }}>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                search();
              }
            }}
            placeholder={keywordPlaceholder}
            style={{ ...inputStyle, maxWidth: 280 }}
          />
          <Button onClick={search}>查询</Button>
          {toolbar}
          <span style={{ marginLeft: 'auto', color: '#6b7280', fontSize: 13 }}>
            {loading ? '加载中…' : `共 ${total} 条`}
          </span>
        </div>

        {error ? <p style={{ color: '#b91c1c', marginTop: 0 }}>{error}</p> : null}

        <div style={{ overflowX: 'auto' }}>
          <table style={tableStyle}>
            <thead>
              <tr style={{ color: '#6b7280', fontSize: 13 }}>
                {columns.map((column) => (
                  <th key={column.title} style={{ ...cellStyle, fontWeight: 600 }}>{column.title}</th>
                ))}
                {editable ? <th style={{ ...cellStyle, fontWeight: 600 }}>操作</th> : null}
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={rowKey(row)}>
                  {columns.map((column) => (
                    <td key={column.title} style={cellStyle}>{column.render(row)}</td>
                  ))}
                  {editable ? (
                    <td style={cellStyle}>
                      <div style={{ display: 'flex', gap: 12 }}>
                        <LinkButton onClick={() => void handleEdit(row)}>编辑</LinkButton>
                        <LinkButton onClick={() => void handleRemove(row)}>删除</LinkButton>
                      </div>
                    </td>
                  ) : null}
                </tr>
              ))}
              {rows.length === 0 && !loading ? (
                <tr>
                  <td colSpan={columns.length + (editable ? 1 : 0)} style={{ ...cellStyle, color: '#9ca3af' }}>
                    暂无数据
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>

        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 16 }}>
          <Button onClick={() => setPageNum((page) => Math.max(1, page - 1))} disabled={pageNum <= 1 || loading}>
            上一页
          </Button>
          <span style={{ fontSize: 13, color: '#4b5563' }}>第 {pageNum} / {pages} 页</span>
          <Button onClick={() => setPageNum((page) => Math.min(pages, page + 1))} disabled={pageNum >= pages || loading}>
            下一页
          </Button>
        </div>
      </Card>
    </div>
  );
}

function renderField(field: Field, value: string, onChange: (value: string) => void): ReactNode {
  if (field.type === 'select') {
    return (
      <select value={value} onChange={(event) => onChange(event.target.value)} style={inputStyle}>
        <option value="">请选择</option>
        {(field.options ?? []).map((option) => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </select>
    );
  }

  if (field.type === 'textarea') {
    return <textarea value={value} onChange={(event) => onChange(event.target.value)} rows={2} style={inputStyle} />;
  }

  if (field.type === 'checks') {
    const selected = new Set(value.split(',').filter(Boolean));
    return (
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, padding: '6px 0' }}>
        {(field.options ?? []).map((option) => (
          <label key={option.value} style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
            <input
              type="checkbox"
              checked={selected.has(option.value)}
              onChange={(event) => {
                if (event.target.checked) {
                  selected.add(option.value);
                } else {
                  selected.delete(option.value);
                }
                onChange([...selected].join(','));
              }}
            />
            {option.label}
          </label>
        ))}
      </div>
    );
  }

  return (
    <input
      type={field.type === 'password' ? 'password' : field.type === 'number' ? 'number' : 'text'}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      placeholder={field.placeholder}
      style={inputStyle}
    />
  );
}

function LinkButton({ children, onClick }: { children: ReactNode; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{ border: 'none', background: 'transparent', color: '#2563eb', cursor: 'pointer', padding: 0, fontSize: 14 }}
    >
      {children}
    </button>
  );
}

function blankForm(fields: Field[]): Record<string, string> {
  const form: Record<string, string> = {};
  for (const field of fields) {
    form[field.name] = '';
  }
  return form;
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}

export const STATUS_OPTIONS = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' }
];

export function statusText(value: string | undefined): string {
  return value === '0' ? '正常' : value === '1' ? '停用' : (value ?? '-');
}
