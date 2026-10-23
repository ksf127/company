import { StatusTag } from '@company/ui';
import type { AdminApi } from '../api';
import { CrudPage } from '../components/CrudPage';
import type { SysLoginLog } from '../types';

/** 登录日志只读：写入方是 auth-service，管理台不提供修改入口 */
export function LoginLogPage({ api }: { api: AdminApi }) {
  return (
    <CrudPage<SysLoginLog>
      title="登录日志"
      description="auth-service 在登录成功/失败时通过内部接口写入"
      idField="infoId"
      editable={false}
      keywordPlaceholder="按登录账号搜索"
      columns={[
        { title: '账号', render: (row) => row.userName },
        { title: 'IP', render: (row) => row.ipaddr ?? '-' },
        { title: '浏览器', render: (row) => row.browser ?? '-' },
        { title: '系统', render: (row) => row.os ?? '-' },
        {
          title: '结果',
          render: (row) => <StatusTag text={row.status === '0' ? '成功' : '失败'} tone={row.status === '0' ? 'success' : 'danger'} />
        },
        { title: '信息', render: (row) => row.msg ?? '-' },
        { title: '时间', render: (row) => row.loginTime ?? '-' }
      ]}
      fields={[]}
      rowKey={(row) => String(row.infoId)}
      toForm={() => ({})}
      load={(pageNum, pageSize, keyword) => api.loginLog.page(pageNum, pageSize, keyword)}
      create={() => Promise.reject(new Error('登录日志不支持新增'))}
      update={() => Promise.reject(new Error('登录日志不支持修改'))}
      remove={() => Promise.reject(new Error('登录日志不支持删除'))}
    />
  );
}
