import { useEffect, useState } from 'react';
import { Button, Card, PageHeader } from '@company/ui';
import type { AdminApi } from '../api';
import { CrudPage, STATUS_OPTIONS, type Field } from '../components/CrudPage';
import type { SysDictData, SysDictType } from '../types';

const typeFields: Field[] = [
  { name: 'dictName', label: '字典名称' },
  { name: 'dictType', label: '字典类型（如 sys_user_sex）' },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS },
  { name: 'remark', label: '备注', type: 'textarea' }
];

const dataFields: Field[] = [
  { name: 'dictLabel', label: '字典标签' },
  { name: 'dictValue', label: '字典键值' },
  { name: 'dictSort', label: '排序', type: 'number' },
  { name: 'isDefault', label: '是否默认', type: 'select', options: [{ label: '是', value: 'Y' }, { label: '否', value: 'N' }] },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS }
];

export function DictPage({ api }: { api: AdminApi }) {
  const [tab, setTab] = useState<'type' | 'data'>('type');
  const [types, setTypes] = useState<SysDictType[]>([]);
  const [currentType, setCurrentType] = useState('');

  useEffect(() => {
    api.dict.typeList()
      .then((list) => {
        setTypes(list);
        setCurrentType((current) => current || list[0]?.dictType || '');
      })
      .catch(() => setTypes([]));
  }, [api]);

  return (
    <div>
      <PageHeader title="字典管理" description="字典类型 + 字典数据；前端下拉框统一走 /system/dict/data/type/{dictType}" />

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <Button onClick={() => setTab('type')} disabled={tab === 'type'}>字典类型</Button>
        <Button onClick={() => setTab('data')} disabled={tab === 'data'}>字典数据</Button>
      </div>

      {tab === 'type' ? (
        <CrudPage<SysDictType>
          title="字典类型"
          idField="dictId"
          keywordPlaceholder="按字典名称搜索"
          columns={[
            { title: '字典名称', render: (row) => row.dictName },
            { title: '字典类型', render: (row) => row.dictType },
            { title: '状态', render: (row) => (row.status === '0' ? '正常' : '停用') },
            { title: '备注', render: (row) => row.remark ?? '-' }
          ]}
          fields={typeFields}
          rowKey={(row) => String(row.dictId)}
          toForm={(row) => ({
            dictId: String(row.dictId),
            dictName: row.dictName,
            dictType: row.dictType,
            status: row.status,
            remark: row.remark ?? ''
          })}
          load={(pageNum, pageSize, keyword) => api.dict.typePage(pageNum, pageSize, keyword)}
          create={(form) => api.dict.createType({
            dictName: form.dictName,
            dictType: form.dictType,
            status: form.status || '0',
            remark: form.remark || undefined
          })}
          update={(form) => api.dict.updateType({
            dictId: Number(form.dictId),
            dictName: form.dictName,
            dictType: form.dictType,
            status: form.status || '0',
            remark: form.remark || undefined
          })}
          remove={(id) => api.dict.removeTypes(Number(id))}
        />
      ) : types.length === 0 ? (
        <Card>
          <p style={{ margin: 0, color: '#6b7280' }}>还没有字典类型，请先在「字典类型」中新增。</p>
        </Card>
      ) : (
        <CrudPage<SysDictData>
          key={currentType}
          title="字典数据"
          description={`当前字典类型：${currentType}`}
          idField="dictCode"
          keywordPlaceholder="字典数据暂不支持关键字搜索"
          toolbar={
            <select
              value={currentType}
              onChange={(event) => setCurrentType(event.target.value)}
              style={{ padding: '8px 12px', border: '1px solid #d5d9e2', borderRadius: 8 }}
            >
              {types.map((type) => (
                <option key={type.dictType} value={type.dictType}>{type.dictName}（{type.dictType}）</option>
              ))}
            </select>
          }
          columns={[
            { title: '标签', render: (row) => row.dictLabel },
            { title: '键值', render: (row) => row.dictValue },
            { title: '排序', render: (row) => row.dictSort ?? '-' },
            { title: '默认', render: (row) => (row.isDefault === 'Y' ? '是' : '否') },
            { title: '状态', render: (row) => (row.status === '0' ? '正常' : '停用') }
          ]}
          fields={dataFields}
          rowKey={(row) => String(row.dictCode)}
          toForm={(row) => ({
            dictCode: String(row.dictCode),
            dictLabel: row.dictLabel,
            dictValue: row.dictValue,
            dictSort: row.dictSort ? String(row.dictSort) : '',
            isDefault: row.isDefault ?? 'N',
            status: row.status
          })}
          load={(pageNum, pageSize) => api.dict.dataPage(pageNum, pageSize, currentType)}
          create={(form) => api.dict.createData({
            dictType: currentType,
            dictLabel: form.dictLabel,
            dictValue: form.dictValue,
            dictSort: form.dictSort ? Number(form.dictSort) : 0,
            isDefault: form.isDefault || 'N',
            status: form.status || '0'
          })}
          update={(form) => api.dict.updateData({
            dictCode: Number(form.dictCode),
            dictType: currentType,
            dictLabel: form.dictLabel,
            dictValue: form.dictValue,
            dictSort: form.dictSort ? Number(form.dictSort) : 0,
            isDefault: form.isDefault || 'N',
            status: form.status || '0'
          })}
          remove={(id) => api.dict.removeData(Number(id))}
        />
      )}
    </div>
  );
}
