import { StatusTag } from '@company/ui';
import type { AdminApi } from '../api';
import { CrudPage, STATUS_OPTIONS, statusText, type Field } from '../components/CrudPage';
import type { SysDept } from '../types';

const fields: Field[] = [
  { name: 'deptName', label: '部门名称' },
  { name: 'parentId', label: '上级部门 ID（顶级为 0）', type: 'number' },
  { name: 'orderNum', label: '显示顺序', type: 'number' },
  { name: 'leader', label: '负责人' },
  { name: 'phone', label: '联系电话' },
  { name: 'email', label: '邮箱' },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS }
];

export function DeptPage({ api }: { api: AdminApi }) {
  async function loadAll(pageNum: number, pageSize: number, keyword: string) {
    const all = await api.dept.list();
    const filtered = keyword ? all.filter((dept) => dept.deptName.includes(keyword)) : all;
    return {
      total: filtered.length,
      rows: filtered.slice((pageNum - 1) * pageSize, pageNum * pageSize)
    };
  }

  return (
    <CrudPage<SysDept>
      title="部门管理"
      description="若依 sys_dept：ancestors 由后端维护，前端只提交父子关系"
      idField="deptId"
      keywordPlaceholder="按部门名称搜索"
      columns={[
        { title: '部门名称', render: (row) => row.deptName },
        { title: '上级', render: (row) => row.parentId ?? 0 },
        { title: '顺序', render: (row) => row.orderNum ?? '-' },
        { title: '负责人', render: (row) => row.leader ?? '-' },
        {
          title: '状态',
          render: (row) => <StatusTag text={statusText(row.status)} tone={row.status === '0' ? 'success' : 'danger'} />
        }
      ]}
      fields={fields}
      rowKey={(row) => String(row.deptId)}
      toForm={(row) => ({
        deptId: String(row.deptId),
        deptName: row.deptName,
        parentId: row.parentId ? String(row.parentId) : '0',
        orderNum: row.orderNum ? String(row.orderNum) : '',
        leader: row.leader ?? '',
        phone: row.phone ?? '',
        email: row.email ?? '',
        status: row.status
      })}
      load={loadAll}
      create={(form) => api.dept.create(payload(form))}
      update={(form) => api.dept.update(payload(form))}
      remove={(id) => api.dept.remove(Number(id))}
    />
  );
}

function payload(form: Record<string, string>): Record<string, unknown> {
  return {
    deptId: form.deptId ? Number(form.deptId) : undefined,
    deptName: form.deptName,
    parentId: form.parentId ? Number(form.parentId) : 0,
    orderNum: form.orderNum ? Number(form.orderNum) : 0,
    leader: form.leader || undefined,
    phone: form.phone || undefined,
    email: form.email || undefined,
    status: form.status || '0'
  };
}
