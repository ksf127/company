import { useEffect, useState } from 'react';
import { StatusTag } from '@company/ui';
import type { AdminApi } from '../api';
import { CrudPage, STATUS_OPTIONS, statusText, type Field } from '../components/CrudPage';
import type { SysUser } from '../types';

const SEX_OPTIONS = [
  { label: '男', value: '0' },
  { label: '女', value: '1' },
  { label: '未知', value: '2' }
];

export function UserPage({ api }: { api: AdminApi }) {
  const [roleOptions, setRoleOptions] = useState<{ label: string; value: string }[]>([]);

  useEffect(() => {
    api.role.list()
      .then((roles) => setRoleOptions(roles.map((role) => ({ label: role.roleName, value: String(role.roleId) }))))
      .catch(() => setRoleOptions([]));
  }, [api]);

  const fields: Field[] = [
    { name: 'userName', label: '登录账号' },
    { name: 'nickName', label: '用户昵称' },
    { name: 'password', label: '密码（编辑时留空表示不修改）', type: 'password' },
    { name: 'deptId', label: '部门 ID', type: 'number' },
    { name: 'phonenumber', label: '手机号码' },
    { name: 'email', label: '邮箱' },
    { name: 'sex', label: '性别', type: 'select', options: SEX_OPTIONS },
    { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS },
    { name: 'roleIds', label: '角色', type: 'checks', options: roleOptions }
  ];

  return (
    <CrudPage<SysUser>
      title="用户管理"
      description="若依 sys_user：密码只在写入时提交，接口永不返回散列"
      idField="userId"
      keywordPlaceholder="按登录账号搜索"
      columns={[
        { title: '账号', render: (row) => row.userName },
        { title: '昵称', render: (row) => row.nickName },
        { title: '部门', render: (row) => row.deptId ?? '-' },
        { title: '手机', render: (row) => row.phonenumber ?? '-' },
        {
          title: '状态',
          render: (row) => <StatusTag text={statusText(row.status)} tone={row.status === '0' ? 'success' : 'danger'} />
        },
        { title: '创建时间', render: (row) => row.createTime ?? '-' }
      ]}
      fields={fields}
      rowKey={(row) => String(row.userId)}
      toForm={(row) => ({
        userId: String(row.userId),
        userName: row.userName,
        nickName: row.nickName,
        password: '',
        deptId: row.deptId ? String(row.deptId) : '',
        phonenumber: row.phonenumber ?? '',
        email: row.email ?? '',
        sex: row.sex ?? '',
        status: row.status,
        roleIds: ''
      })}
      prepareEdit={async (row) => {
        const roleIds = await api.user.roleIds(row.userId);
        return {
          userId: String(row.userId),
          userName: row.userName,
          nickName: row.nickName,
          password: '',
          deptId: row.deptId ? String(row.deptId) : '',
          phonenumber: row.phonenumber ?? '',
          email: row.email ?? '',
          sex: row.sex ?? '',
          status: row.status,
          roleIds: roleIds.join(',')
        };
      }}
      load={(pageNum, pageSize, keyword) => api.user.page(pageNum, pageSize, keyword)}
      create={(form) => api.user.create(toPayload(form))}
      update={(form) => api.user.update(toPayload(form))}
      remove={(id) => api.user.remove(Number(id))}
    />
  );
}

function toPayload(form: Record<string, string>): Record<string, unknown> {
  return {
    userId: form.userId ? Number(form.userId) : undefined,
    userName: form.userName,
    nickName: form.nickName,
    password: form.password || undefined,
    deptId: form.deptId ? Number(form.deptId) : undefined,
    phonenumber: form.phonenumber || undefined,
    email: form.email || undefined,
    sex: form.sex || undefined,
    status: form.status || '0',
    roleIds: form.roleIds ? form.roleIds.split(',').filter(Boolean).map(Number) : []
  };
}
