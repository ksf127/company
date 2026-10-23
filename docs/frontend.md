# 前端微前端架构

## 1. 角色划分

| 角色 | 目录 | 职责 |
| --- | --- | --- |
| 主应用 shell | `company-web/shell` | 布局、侧边导航、路由匹配、登录、装载远端、注入 sdk/eventBus |
| 业务远端 | `company-web/apps/<domain>/<id>` | 自身业务页面，只暴露 `./App` |
| 契约 | `packages/contracts` | `RemoteAppProps`、`AppSdk`、`AppEventBus`、`AppManifest` |
| SDK | `packages/sdk` | `FrontendSdk`（鉴权头、`X-Request-Id`、`ApiResult` 解包）+ `EventBus` |
| 内核 | `packages/kernel` | 清单加载与校验、`RemoteHost` 装载、错误边界；**只允许 shell 使用** |
| 构建配置 | `packages/build-config` | 唯一的 `defineHost` / `defineRemote`，统一 Shared 规则 |
| 清单 | `registry/remotes.json` | 运行时应用注册表，shell 启动时加载 |

依赖方向是单向的：

```text
shell ──依赖──▶ kernel / sdk / contracts / ui
远端  ──依赖──▶        sdk / contracts / ui
```

远端**禁止** import shell、kernel 或其它 `@company/app-*`。跨应用协作只能走 `eventBus` 或后端接口。

## 2. 运行时注册中心

`registry/remotes.json` 是应用接入的唯一入口，新增应用不需要改 shell 代码，也不需要重新构建 shell。

```json
{
  "version": 1,
  "apps": [
    {
      "id": "admin",
      "name": "系统管理",
      "remoteName": "systemApp",
      "entry": "http://localhost:5003/remoteEntry.js",
      "route": "/system",
      "nav": { "group": "系统", "order": 5 },
      "permissions": ["system:user:list"],
      "enabled": true
    }
  ]
}
```

| 字段 | 约束 |
| --- | --- |
| `id` | 全局唯一，与包名 `@company/app-<id>`、目录名一致 |
| `remoteName` | 全局唯一，必须与 `vite.config.ts` 中 Module Federation 的 `name` 一致 |
| `entry` | `remoteEntry.js` 的完整地址；构建期会改写为 `${PUBLIC_BASE}/apps/<id>/remoteEntry.js` |
| `route` | 以 `/` 开头且全局唯一；采用最长前缀匹配，`/system/user` 归属 `/system` |
| `nav` | 侧边栏分组与排序 |
| `permissions` | 为后续按权限过滤导航预留；当前 shell 不据此过滤 |
| `enabled` | `false` 时既不构建也不加载 |

启动时 `loadRegistry` 会拒绝重复的 `id` / `route` / `remoteName`，避免"到底加载了哪个应用"无法推理。

开发期 `publicDir` 指向 `registry/`，因此 `/remotes.json` 直接可访问；生产由 `build.mjs` 把改写后的清单写进 `dist/shell/`。

## 3. 契约与 SDK

远端组件签名：

```tsx
import type { RemoteAppProps } from '@company/contracts';

export default function OrderApp({ appId, basename, sdk, eventBus }: RemoteAppProps) {
  // sdk.get/post/put/del 由 shell 注入
  // eventBus.emit / eventBus.on 用于跨应用通信
}
```

`FrontendSdk` 统一处理：

- 自动附加 `Authorization: Bearer <token>`；
- 每个请求生成 `X-Request-Id`；
- 解包后端 `ApiResult<T>`，`code !== 0` 抛 `ApiError`；
- 401 时清除 token 并跳转 `/login`。

除登录页外，远端**不得**自行创建 `fetch` / `axios` 实例，否则鉴权与错误处理会出现多套实现。

跨应用通信示例：

```ts
eventBus.emit('shell:navigate', '/trade/order/123');
```

## 4. 登录流程

1. 用户访问任意路由，shell 检查 `localStorage.token`。
2. 无 token → 渲染 `LoginPage`，并记录 `returnTo`。
3. 登录页直接 `fetch /api/auth/login`（`{username, password}`）。
   **刻意不使用 sdk**：sdk 的 401 会自动跳转登录页，登录失败会变成死循环。
4. 成功后写入 `localStorage.token`，回到 `returnTo`。
5. 侧边栏"退出登录"清除 token 并跳转 `/login`。

默认账号：`admin / admin123`。

## 5. 开发期 API 代理

开发环境由 shell 的 Vite dev server 统一代理，前端只认 `/api/*`：

| 前端路径 | 目标服务 |
| --- | --- |
| `/api/auth/*` | `http://localhost:8081` |
| `/api/system/*` | `http://localhost:8082` |
| `/api/orders/*` | `http://localhost:8083` |
| `/api/users/*` | `http://localhost:8084` |
| `/api/demo-01/*` … `/api/demo-10/*` | `http://localhost:8091` … `8100` |

代理会去掉 `/api` 前缀，因此 `/api/system/user/page` 实际请求 system-service 的 `/system/user/page`。生产环境由网关承担同样规则。

## 6. 新增一个微前端

推荐使用脚手架：

```bash
cd company-web
npm run create -- --id=payment --name=支付中心 --domain=trade --group=交易 --route=/trade/payment --port=5004
npm install
npm run dev -- payment
```

脚手架会：

1. 创建 `apps/<domain>/<id>`（package.json、vite.config.ts、tsconfig.json、index.html、src/App.tsx、src/main.tsx）；
2. 校验 id 格式、端口占用、路由冲突；
3. 自动追加到 `registry/remotes.json`。

手工接入时检查清单：

- [ ] 包名 `@company/app-<id>`，目录名与 `id` 一致；
- [ ] `vite.config.ts` 使用 `defineRemote`，只暴露 `./App`，不自定义 `shared`；
- [ ] 不依赖 `@company/kernel`、`@company/shell`、其它业务应用；
- [ ] 已在 `registry/remotes.json` 注册；
- [ ] `npm run check && npm run typecheck` 通过。

### 示例应用（lab/demo01 – demo10）

`apps/lab/demoXX` 是按最小规模生成的样板，共 6 个文件：

```text
apps/lab/demoXX/
├── package.json        # @company/app-demoXX
├── tsconfig.json
├── vite.config.ts      # defineRemote({ name: 'demoXXApp', port: 501X, exposes: { './App': ... } })
├── index.html
└── src/{main.tsx,App.tsx}   # main 为独立调试入口，App 为被 shell 加载的远端
```

页面只做「列表 + 搜索 + 分页 + 新增 + 删除」，请求走 `sdk.get/post/del('/demo-XX/records')`，不直接使用 `fetch`。10 条注册项统一在 `registry/remotes.json`，`nav.group` 为“实验室”，`route` 为 `/demo-01` … `/demo-10`。

复制扩展时只需：复制目录 → 改包名、`name`、`port`、`API` 常量 → 在 `remotes.json` 追加一条 → `npm install && npm run check`。

## 7. 构建与发布

```bash
npm run build               # 全部
npm run build -- admin      # 只构建指定应用
PUBLIC_BASE=https://cdn.company.com/console npm run build
```

产物：

```text
dist/
├── shell/                  # 主应用 + 生产版 remotes.json
└── apps/<id>/              # 各远端 remoteEntry.js 与静态资源
```

发布时整体上传 `dist/`，主应用通过 `/remotes.json` 找到各远端的 CDN 地址。

## 8. 架构体检

`npm run check` 会强制以下规则（CI 入口，失败即退出码 1）：

1. `apps/` 目录与 `registry/remotes.json` **双向一致**；
2. 每个应用只暴露 `./App`，且必须使用 `defineRemote`；
3. 禁止远端依赖 `@company/kernel` / `@company/shell` / 其它 `@company/app-*`；
4. 禁止通过相对路径跨应用引用；
5. shell 禁止直接 import 业务应用，只能走 registry + `RemoteHost`；
6. 禁止各应用自定义 `shared`，统一由 `build-config` 提供。

`npm run typecheck` 会用本地 TypeScript 对 shell 与所有应用逐一执行 `tsc --noEmit`。
