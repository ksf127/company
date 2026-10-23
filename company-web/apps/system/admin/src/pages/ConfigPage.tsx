import type { AdminApi } from '../api';
import { CrudPage, type Field } from '../components/CrudPage';
import type { SysConfig } from '../types';

const fields: Field[] = [
  { name: 'configName', label: '参数名称' },
  { name: 'configKey', label: '参数键名' },
  { name: 'configValue', label: '参数键值' },
  { name: 'configType', label: '系统内置', type: 'select', options: [{ label: '是', value: 'Y' }, { label: '否', value: 'N' }] },
  { name: 'remark', label: '备注', type: 'textarea' }
];

export function ConfigPage({ api }: { api: AdminApi }) {
  return (
    <CrudPage<SysConfig>
      title="参数设置"
      description="若依 sys_config：业务侧通过 GET /system/config/value/{configKey} 读取"
      idField="configId"
      keywordPlaceholder="按参数名称搜索"
      columns={[
        { title: '参数名称', render: (row) => row.configName },
        { title: '参数键名', render: (row) => row.configKey },
        { title: '参数键值', render: (row) => row.configValue },
        { title: '内置', render: (row) => (row.configType === 'Y' ? '是' : '否') }
      ]}
      fields={fields}
      rowKey={(row) => String(row.configId)}
      toForm={(row) => ({
        configId: String(row.configId),
        configName: row.configName,
        configKey: row.configKey,
        configValue: row.configValue,
        configType: row.configType ?? 'N',
        remark: row.remark ?? ''
      })}
      load={(pageNum, pageSize, keyword) => api.config.page(pageNum, pageSize, keyword)}
      create={(form) => api.config.create(payload(form))}
      update={(form) => api.config.update(payload(form))}
      remove={(id) => api.config.remove(Number(id))}
    />
  );
}

function payload(form: Record<string, string>): Record<string, unknown> {
  return {
    configId: form.configId ? Number(form.configId) : undefined,
    configName: form.configName,
    configKey: form.configKey,
    configValue: form.configValue,
    configType: form.configType || 'N',
    remark: form.remark || undefined
  };
}
