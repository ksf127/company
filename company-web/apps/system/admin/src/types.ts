/** 字段名与若依 sys_* 表保持一致，便于直接对照后端实体与 SQL */

export interface PageResult<T> {
  total: number;
  rows: T[];
}

export interface SysUser {
  userId: number;
  deptId?: number;
  userName: string;
  nickName: string;
  email?: string;
  phonenumber?: string;
  sex?: string;
  status: string;
  createTime?: string;
}

export interface SysRole {
  roleId: number;
  roleName: string;
  roleKey: string;
  roleSort?: number;
  dataScope?: string;
  status: string;
  remark?: string;
}

export interface SysMenu {
  menuId: number;
  menuName: string;
  parentId?: number;
  orderNum?: number;
  path?: string;
  component?: string;
  menuType: string;
  visible?: string;
  status?: string;
  perms?: string;
  icon?: string;
}

export interface SysDept {
  deptId: number;
  parentId?: number;
  deptName: string;
  orderNum?: number;
  leader?: string;
  phone?: string;
  email?: string;
  status: string;
}

export interface SysDictType {
  dictId: number;
  dictName: string;
  dictType: string;
  status: string;
  remark?: string;
}

export interface SysDictData {
  dictCode: number;
  dictSort?: number;
  dictLabel: string;
  dictValue: string;
  dictType: string;
  isDefault?: string;
  status: string;
}

export interface SysConfig {
  configId: number;
  configName: string;
  configKey: string;
  configValue: string;
  configType?: string;
  remark?: string;
}

export interface SysLoginLog {
  infoId: number;
  userName: string;
  ipaddr?: string;
  browser?: string;
  os?: string;
  status: string;
  msg?: string;
  loginTime?: string;
}
