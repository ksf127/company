import type { AdminApi } from '../api';
import { CrudPage, STATUS_OPTIONS, type Field } from '../components/CrudPage';
import type { SysMenu } from '../types';

const MENU_TYPE_OPTIONS = [
  { label: '目录', value: 'M' },
  { label: '菜单', value: 'C' },
  { label: '按钮', value: 'F' }
];

const VISIBLE_OPTIONS = [
  { label: '显示', value: '0' },
  { label: '隐藏', value: '1' }
];

const fields: Field[] = [
  { name: 'menuName', label: '菜单名称' },
  { name: 'parentId', label: '上级菜单 ID（顶级为 0）', type: 'number' },
  { name: 'orderNum', label: '显示顺序', type: 'number' },
  { name: 'menuType', label: '菜单类型', type: 'select', options: MENU_TYPE_OPTIONS },
  { name: 'path', label: '路由地址' },
  { name: 'component', label: '组件路径' },
  { name: 'perms', label: '权限标识（如 system:user:list）' },
  { name: 'icon', label: '图标' },
  { name: 'visible', label: '显示状态', type: 'select', options: VISIBLE_OPTIONS },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS }
];

export function MenuPage({ api }: { api: AdminApi }) {
  // 菜单是典型的小表全量数据：一次取回，前端做过滤与分页即可
  async function loadAll(pageNum: number, pageSize: number, keyword: string) {
    const all = await api.menu.list();
    const filtered = keyword ? all.filter((menu) => menu.menuName.includes(keyword)) : all;
    return {
      total: filtered.length,
      rows: filtered.slice((pageNum - 1) * pageSize, pageNum * pageSize)
    };
  }

  return (
    <CrudPage<SysMenu>
      title="菜单管理"
      description="菜单类型 M 目录 / C 菜单 / F 按钮；权限标识用于前端可见性过滤"
      idField="menuId"
      keywordPlaceholder="按菜单名称搜索"
      columns={[
        { title: '菜单名称', render: (row) => row.menuName },
        { title: '上级', render: (row) => row.parentId ?? 0 },
        { title: '类型', render: (row) => row.menuType },
        { title: '路由', render: (row) => row.path ?? '-' },
        { title: '权限标识', render: (row) => row.perms ?? '-' },
        { title: '顺序', render: (row) => row.orderNum ?? '-' }
      ]}
      fields={fields}
      rowKey={(row) => String(row.menuId)}
      toForm={(row) => ({
        menuId: String(row.menuId),
        menuName: row.menuName,
        parentId: row.parentId ? String(row.parentId) : '0',
        orderNum: row.orderNum ? String(row.orderNum) : '',
        menuType: row.menuType,
        path: row.path ?? '',
        component: row.component ?? '',
        perms: row.perms ?? '',
        icon: row.icon ?? '',
        visible: row.visible ?? '0',
        status: row.status ?? '0'
      })}
      load={loadAll}
      create={(form) => api.menu.create(payload(form))}
      update={(form) => api.menu.update(payload(form))}
      remove={(id) => api.menu.remove(Number(id))}
    />
  );
}

function payload(form: Record<string, string>): Record<string, unknown> {
  return {
    menuId: form.menuId ? Number(form.menuId) : undefined,
    menuName: form.menuName,
    parentId: form.parentId ? Number(form.parentId) : 0,
    orderNum: form.orderNum ? Number(form.orderNum) : 0,
    menuType: form.menuType || 'C',
    path: form.path || undefined,
    component: form.component || undefined,
    perms: form.perms || undefined,
    icon: form.icon || undefined,
    visible: form.visible || '0',
    status: form.status || '0'
  };
}
