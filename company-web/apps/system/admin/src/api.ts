import type { AppSdk } from '@company/contracts';
import type {
  PageResult,
  SysConfig,
  SysDept,
  SysDictData,
  SysDictType,
  SysLoginLog,
  SysMenu,
  SysRole,
  SysUser
} from './types';

/** 统一拼接查询串，空值不落到 URL 上，避免后端把空串当成过滤条件 */
function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  }
  const text = search.toString();
  return text ? `?${text}` : '';
}

export function createApi(sdk: AppSdk) {
  return {
    user: {
      page: (pageNum: number, pageSize: number, userName: string) =>
        sdk.get<PageResult<SysUser>>(`/system/user/page${query({ pageNum, pageSize, userName })}`),
      roleIds: (userId: number) => sdk.get<number[]>(`/system/user/${userId}/roleIds`),
      create: (body: unknown) => sdk.post<SysUser>('/system/user', body),
      update: (body: unknown) => sdk.put<void>('/system/user', body),
      remove: (userId: number) => sdk.del<void>(`/system/user/${userId}`)
    },
    role: {
      page: (pageNum: number, pageSize: number, roleName: string) =>
        sdk.get<PageResult<SysRole>>(`/system/role/page${query({ pageNum, pageSize, roleName })}`),
      list: () => sdk.get<SysRole[]>('/system/role/list'),
      create: (body: unknown) => sdk.post<SysRole>('/system/role', body),
      update: (body: unknown) => sdk.put<void>('/system/role', body),
      remove: (roleId: number) => sdk.del<void>(`/system/role/${roleId}`)
    },
    menu: {
      list: () => sdk.get<SysMenu[]>('/system/menu/list'),
      create: (body: unknown) => sdk.post<SysMenu>('/system/menu', body),
      update: (body: unknown) => sdk.put<void>('/system/menu', body),
      remove: (menuId: number) => sdk.del<void>(`/system/menu/${menuId}`)
    },
    dept: {
      list: () => sdk.get<SysDept[]>('/system/dept/list'),
      create: (body: unknown) => sdk.post<SysDept>('/system/dept', body),
      update: (body: unknown) => sdk.put<void>('/system/dept', body),
      remove: (deptId: number) => sdk.del<void>(`/system/dept/${deptId}`)
    },
    dict: {
      typePage: (pageNum: number, pageSize: number, dictName: string) =>
        sdk.get<PageResult<SysDictType>>(`/system/dict/type/page${query({ pageNum, pageSize, dictName })}`),
      typeList: () => sdk.get<SysDictType[]>('/system/dict/type/list'),
      createType: (body: unknown) => sdk.post<SysDictType>('/system/dict/type', body),
      updateType: (body: unknown) => sdk.put<void>('/system/dict/type', body),
      removeTypes: (dictId: number) => sdk.del<void>(`/system/dict/type/${dictId}`),
      dataPage: (pageNum: number, pageSize: number, dictType: string) =>
        sdk.get<PageResult<SysDictData>>(`/system/dict/data/page${query({ pageNum, pageSize, dictType })}`),
      createData: (body: unknown) => sdk.post<SysDictData>('/system/dict/data', body),
      updateData: (body: unknown) => sdk.put<void>('/system/dict/data', body),
      removeData: (dictCode: number) => sdk.del<void>(`/system/dict/data/${dictCode}`)
    },
    config: {
      page: (pageNum: number, pageSize: number, configName: string) =>
        sdk.get<PageResult<SysConfig>>(`/system/config/page${query({ pageNum, pageSize, configName })}`),
      create: (body: unknown) => sdk.post<SysConfig>('/system/config', body),
      update: (body: unknown) => sdk.put<void>('/system/config', body),
      remove: (configId: number) => sdk.del<void>(`/system/config/${configId}`)
    },
    loginLog: {
      page: (pageNum: number, pageSize: number, userName: string) =>
        sdk.get<PageResult<SysLoginLog>>(`/system/log/login/page${query({ pageNum, pageSize, userName })}`)
    }
  };
}

export type AdminApi = ReturnType<typeof createApi>;
