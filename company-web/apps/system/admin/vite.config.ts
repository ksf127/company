import { defineRemote } from '@company/build-config';

// 若依基础模块管理台：用户 / 角色 / 菜单 / 部门 / 字典 / 参数 / 登录日志
export default defineRemote({
  name: 'systemApp',
  port: 5003,
  exposes: {
    './App': './src/App.tsx'
  }
});
