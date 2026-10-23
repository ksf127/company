import { StatusTag } from '@company/ui';
import type { AdminApi } from '../api';
import { CrudPage, STATUS_OPTIONS, statusText, type Field } from '../components/CrudPage';
import type { SysRole } from '../types';

const DATA_SCOPE_OPTIONS = [
  { label: '全部数据', value: '1' },
  { label: '自定义', value: '2' },
  { label: '本部门', value: '3' },
  { label: '本部门及以下', value: '4' },
  { label: '仅本人', value: '5' }
];

const fields: Field[] = [
  { name: 'roleName', label: '角色名称' },
  { name: 'roleKey', label: '权限字符' },
  { name: 'roleSort', label: '显示顺序', type: 'number' },
  { name: 'dataScope', label: '数据范围', type: 'select', options: DATA_SCOPE_OPTIONS },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS },
  { name: 'remark', label: '备注', type: 'textarea' }
];

export function RolePage({ api }: { api: AdminApi }) {
  return (
    <CrudPage<SysRole>
      title="角色管理"
      description="admin 角色在后端被识别为超级管理员，权限集合固定为 *:*:*"
      idField="roleId"
      keywordPlaceholder="按角色名称搜索"
      columns={[
        { title: '角色名称', render: (row) => row.roleName },
        { title: '权限字符', render: (row) => row.roleKey },
        { title: '顺序', render: (row) => row.roleSort ?? '-' },
        {
          title: '状态',
          render: (row) => <StatusTag text={statusText(row.status)} tone={row.status === '0' ? 'success' : 'danger'} />
        },
        { title: '备注', render: (row) => row.remark ?? '-' }
      ]}
      fields={fields}
      rowKey={(row) => String(row.roleId)}
      toForm={(row) => ({
        roleId: String(row.roleId),
        roleName: row.roleName,
        roleKey: row.roleKey,
        roleSort: row.roleSort ? String(row.roleSort) : '',
        dataScope: row.dataScope ?? '1',
        status: row.status,
        remark: row.remark ?? ''
      })}
      load={(pageNum, pageSize, keyword) => api.role.page(pageNum, pageSize, keyword)}
      create={(form) => api.role.create({
        roleName: form.roleName,
        roleKey: form.roleKey,
        roleSort: form.roleSort ? Number(form.roleSort) : 0,
        dataScope: form.dataScope || '1',
        status: form.status || '0',
        remark: form.remark || undefined
      })}
      update={(form) => api.role.update({
        roleId: Number(form.roleId),
        roleName: form.roleName,
        roleKey: form.roleKey,
        roleSort: form.roleSort ? Number(form.roleSort) : 0,
        dataScope: form.dataScope || '1',
        status: form.status || '0',
        remark: form.remark || undefined
      })}
      remove={(id) => api.role.remove(Number(id))}
    />
  );
}
