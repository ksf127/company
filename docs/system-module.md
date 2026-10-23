# 若依基础模块（system-service）

以若依（RuoYi）的数据模型与接口语义为基准，独立拆分为 `system-service` + 独立库 `system_db`，作为所有业务系统的底座。

- 不复制 RuoYi-Cloud 整套工程，只保留可复用的基础域；
- 认证独立为 `auth-service`，通过 `system-api` 的 Feign 客户端读取用户数据；
- 前端对应微前端 `company-web/apps/system/admin`，路由 `/system`。

## 1. 数据模型

初始化脚本：[deploy/db/init/02-system_db.sql](../deploy/db/init/02-system_db.sql)

| 表 | 说明 |
| --- | --- |
| `sys_user` | 用户；`password` 为 BCrypt；`status` 0 正常 / 1 停用 |
| `sys_role` | 角色；`role_key` 为权限字符串 |
| `sys_menu` | 菜单与按钮权限；`menu_type` M 目录 / C 菜单 / F 按钮 |
| `sys_dept` | 部门树；`ancestors` 存祖级路径 |
| `sys_dict_type` / `sys_dict_data` | 字典类型与字典数据 |
| `sys_config` | 参数配置 |
| `sys_logininfor` | 登录日志 |
| `sys_user_role` | 用户-角色关联 |
| `sys_role_menu` | 角色-菜单关联 |

约定：

- 逻辑删除统一字段 `deleted`（0 正常 / 1 删除），由 `mysql.yaml` 中的 MyBatis-Plus 全局配置接管；
- 表名自带 `sys_` 前缀，**不设置** `table-prefix`（`t_` 前缀只属于 user-service）；
- 时间字段由服务端写入 `create_time` / `update_time`。

## 2. 默认数据

| 项 | 值 |
| --- | --- |
| 管理员账号 | `admin` / `admin123`（库中存 BCrypt 散列） |
| 管理员角色 | `admin`（超级管理员），权限集合为 `*:*:*` |
| 普通角色 | `common`（只读） |
| 部门 | 总公司（100） / 研发部（101） |
| 新建用户默认密码 | `123456` |
| 默认字典 | `sys_normal_disable`、`sys_user_sex` |
| 默认参数 | `sys.app.name`、`sys.user.initPassword` |

`admin` 的 `*:*:*` 由 `SysPermissionService` 在查询权限时直接返回，不需要在 `sys_role_menu` 中逐条授权。

## 3. 接口清单

所有接口返回 `ApiResult<T>`；分页返回 `PageResult<T> { total, rows }`。

### 用户 `/system/user`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/page?pageNum&pageSize&userName&status` | 分页查询 |
| GET | `/{userId}` | 详情 |
| GET | `/{userId}/roleIds` | 用户已分配角色 |
| POST | `/` | 新增（body: `SysUserSaveRequest`，含 `roleIds`） |
| PUT | `/` | 修改（密码为空表示不修改） |
| DELETE | `/{userIds}` | 批量删除（逗号分隔） |

### 角色 `/system/role`

`GET /page`、`GET /list`、`GET /{roleId}`、`POST /`、`PUT /`、`DELETE /{roleIds}`

### 菜单 `/system/menu`

`GET /list`、`GET /{menuId}`、`POST /`、`PUT /`、`DELETE /{menuIds}`

### 部门 `/system/dept`

`GET /list`、`GET /{deptId}`、`POST /`、`PUT /`、`DELETE /{deptIds}`

### 字典 `/system/dict`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/type/page` | 字典类型分页 |
| GET | `/type/list` | 字典类型全量（下拉框） |
| POST / PUT | `/type` | 新增 / 修改类型 |
| DELETE | `/type/{dictIds}` | 删除类型 |
| GET | `/data/page?dictType` | 字典数据分页 |
| GET | `/data/type/{dictType}` | 按类型取数据 |
| POST / PUT | `/data` | 新增 / 修改数据 |
| DELETE | `/data/{dictCodes}` | 删除数据 |

### 参数 `/system/config`

`GET /page`、`GET /value/{configKey}`、`POST /`、`PUT /`、`DELETE /{configIds}`

### 登录日志 `/system/log/login`

`GET /page?pageNum&pageSize&userName&status`

### 内部接口 `/internal/system`

供 `auth-service` 通过 Feign 调用，不对外暴露：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/users/by-username/{userName}` | 登录校验用（含密码散列） |
| GET | `/users/{userId}` | 用户信息 |
| GET | `/users/{userId}/roles` | 角色列表 |
| GET | `/users/{userId}/permissions` | 权限集合 |
| POST | `/login-logs` | 写入登录日志 |

## 4. 认证流程

```text
前端 POST /api/auth/login
  → auth-service AuthController
  → SystemClient.getAuthByUserName (Feign)
  → system-service /internal/system/users/by-username/{userName}
  → BCrypt 校验 + 状态校验
  → 查询角色与权限
  → 签发 JWT（HS256，默认 120 分钟）
  → 异步写登录日志（失败不影响登录）
```

- `GET /auth/getInfo` 从 `Authorization: Bearer <token>` 解析 `userId`，返回用户、角色、权限。
- `POST /auth/logout` 为保留接口；JWT 无状态，前端清除 token 即可。
- 生产必须通过 `JWT_SECRET` 注入至少 32 字节的密钥，禁止使用默认值。

## 5. 前端管理台

微前端：`company-web/apps/system/admin`（`systemApp`，端口 5003，路由 `/system`）

| Tab | 页面 | 特点 |
| --- | --- | --- |
| 用户管理 | `UserPage` | 角色多选、编辑时回填角色、密码留空不覆盖 |
| 角色管理 | `RolePage` | 角色标识、排序、数据范围 |
| 菜单管理 | `MenuPage` | 全量数据前端过滤 + 分页 |
| 部门管理 | `DeptPage` | 同上 |
| 字典管理 | `DictPage` | 类型 / 数据双 Tab，切换类型自动重置分页 |
| 参数设置 | `ConfigPage` | 参数键值管理 |
| 登录日志 | `LoginLogPage` | 只读，不支持新增/修改/删除 |

七个页面共用 `components/CrudPage.tsx`：搜索、分页、表单弹窗、删除确认只实现一次。

## 6. 与业务服务集成

- 业务服务需要用户/角色/权限时，引入 `company-api/system-api`，使用 `SystemClient` 调用内部接口，**不要直连 `system_db`**。
- 前端需要按权限渲染时，调用 `/api/auth/getInfo` 获取 `permissions`，再通过 `eventBus` 或全局上下文共享。
- 新增基础表时，同步更新：实体 → mapper → repository → service → controller → 初始化 SQL → 前端类型与页面。
