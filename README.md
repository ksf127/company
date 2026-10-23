# Company

面向 **100+ 微服务 / 100+ 微前端** 的企业级单体仓库（Monorepo）。架构约束不靠文档，靠代码结构与构建期检查强制：

- 后端：Maven 多模块 + Spring Boot 3 + Spring Cloud Alibaba，分层依赖由 Enforcer 与 ArchUnit 守住。
- 前端：npm workspaces + Module Federation 2.0，应用注册由运行时清单驱动，架构体检脚本卡住越界依赖。
- 基础模块：内置若依（RuoYi）风格的用户 / 角色 / 菜单 / 部门 / 字典 / 参数 / 登录日志，可直接作为业务系统的底座。

---

## 架构分层（结构即规范）

```text
company/                                # 顶层 Git 仓库
├── pom.xml                             # [根 POM] 仅做模块聚合，不做依赖管理
│
├── company-bom/                        # 【POM L1】公司级 BOM（架构组维护）
│   └── pom.xml                         # 锁定 Spring Boot/Cloud 及所有第三方版本
│
├── company-parent/                     # 【POM 构建父】只负责构建插件与编译规则
│
├── company-starters/                   # 【POM L2】中间件 Starter 集合（基础组维护）
│   ├── company-redis-starter/          # Redis 封装
│   │   └── AutoConfiguration + 默认配置
│   └── company-mybatis-starter/        # DB 封装（唯一允许引入 MyBatis-Plus 的入口）
│
├── company-api/                        # 【契约层】Feign 接口 + DTO（业务线维护）
│   ├── system-api/                     # 系统服务契约
│   │   └── 只有 Interface 和 DTO，无实现
│   └── user-api/                       # 用户服务契约
│
├── company-common/                     # 【公共层】纯工具类（架构组维护）
│   ├── common-core/                    # 零外部依赖，严禁引入 Spring Web/DB
│   └── common-web/                     # 全局异常、请求上下文等 Web 公共能力
│
├── company-services/                   # 【业务服务层】一线开发只碰这个目录
│   ├── system-service/
│   │   ├── pom.xml                     # 继承 parent、引入 Starter，不写 version
│   │   ├── src/main/java/...           # 业务代码
│   │   └── src/main/resources/
│   │       └── application.yml         # 【L3】极简，只保留服务自身差异
│   └── user-service/
│       └── ...
│
├── company-web/                        # 【前端层】npm workspaces + Module Federation
│   ├── shell/                          # 主应用：布局、路由、登录、远程加载
│   └── apps/                           # 独立微前端（新增应用只改 registry/remotes.json）
│
└── deploy/                             # 【运维层】部署与配置模板（SRE 维护）
    ├── nacos-config/                   # Nacos 配置导出/导入（Config as Code）
    │   ├── base/application.yaml       # 【L0】全局共享配置
    │   ├── base/redis.yaml             # 【L1】中间件共享配置
    │   ├── base/mysql.yaml             # 【L1】中间件共享配置
    │   └── services/*.yaml             # 【L2】服务专属配置
    ├── docker/                         # 统一 Dockerfile 模板
    └── k8s/                            # Helm Chart / K8s YAML
```

💡 **结构即规范**：目录位置决定依赖方向与维护责任，越界会在构建期被 Maven Enforcer / ArchUnit / 前端架构体检脚本拦下。

---

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 语言与运行时 | JDK 17、Node.js >= 20 |
| 后端框架 | Spring Boot 3.2.5、Spring Cloud 2023.0.1、Spring Cloud Alibaba 2023.0.1.0 |
| 注册 / 配置中心 | Nacos 2.3.2（L0–L3 分层配置） |
| 持久层 | MyBatis-Plus 3.5.6（仅允许出现在 repository 层）、MySQL 8.4 |
| 缓存 | Redis 7.4 |
| 认证 | JWT（jjwt 0.12.5）+ BCrypt |
| 前端 | React 19、TypeScript 5.9、Vite 7、Module Federation 2.0 |
| 部署 | Docker Compose（本地）、Helm Chart（K8s） |

---

## 目录结构

```text
company/
├── company-bom/            # 只负责版本（dependencyManagement）
├── company-parent/         # 只负责构建（compiler / surefire / enforcer / 注解处理器）
├── company-common/
│   ├── common-core/        # ApiResult、ErrorCode、BizException 等零依赖内核
│   └── common-web/         # 全局异常处理、请求上下文等 Web 公共能力
├── company-starters/
│   ├── company-mybatis-starter/    # 唯一允许引入 MyBatis-Plus 与驱动的入口
│   └── company-redis-starter/      # 唯一允许引入 Redis 客户端的入口
├── company-api/            # 服务间契约（DTO + Feign 接口），按服务拆模块
│   ├── system-api/
│   └── user-api/
├── company-services/       # 所有可运行服务，新增服务只能加在这里
│   ├── auth-service/
│   ├── system-service/     # 若依基础模块（用户/角色/菜单/部门/字典/参数/日志）
│   ├── order-service/
│   ├── user-service/
│   └── demo01-service/ … demo10-service/   # 10 个最小示例服务（内存存储）
├── deploy/
│   ├── db/init/            # MySQL 初始化脚本（user_db / system_db）
│   ├── nacos-config/       # Config as Code：L0/L1/L2 配置 + 发布脚本
│   ├── docker/             # 通用 Dockerfile
│   ├── docker-compose.yml  # Nacos + MySQL + Redis
│   └── k8s/                # Helm Chart 与各服务 values
└── company-web/
    ├── shell/              # 主应用：布局、路由、登录、远程加载
    ├── apps/
    │   ├── system/admin/   # 系统管理（若依基础模块前端）
    │   ├── identity/user/  # 用户中心
    │   ├── trade/order/    # 订单中心
    │   └── lab/demo01/ … demo10/   # 10 个最小示例微前端
    ├── packages/
    │   ├── contracts/      # 微前端契约（RemoteAppProps / AppSdk / AppEventBus）
    │   ├── sdk/            # FrontendSdk（鉴权头、ApiResult 解包）+ EventBus
    │   ├── kernel/         # 清单加载、远程应用装载、错误边界（仅 shell 使用）
    │   ├── ui/             # 共享 UI 组件
    │   └── build-config/   # 唯一的 Vite + Module Federation 配置
    ├── registry/remotes.json  # 运行时应用清单（新增微前端只改这里）
    └── tools/              # dev / build / check / typecheck / create-app
```

---

## 快速开始

### 1. 启动基础设施

```bash
cd deploy
docker compose up -d
```

包含：Nacos `8848`、MySQL `3306`（自动执行 `db/init/*.sql`）、Redis `6379`。

### 2. 发布 Nacos 配置

```bash
node deploy/nacos-config/publish.mjs              # 发布 L0 base + L2 services
node deploy/nacos-config/publish.mjs --with-local # 本地联调时额外覆盖 local/
```

### 3. 启动后端

```bash
mvn -B clean install -DskipTests

# 分别在不同终端启动
mvn -B -pl company-services/system-service spring-boot:run
mvn -B -pl company-services/auth-service spring-boot:run
```

单测与架构守卫：

```bash
mvn -B test
```

### 4. 启动前端

```bash
cd company-web
npm install
npm run dev            # shell + 清单里全部 enabled 的微前端
npm run dev -- admin   # 只启动 shell 与系统管理
```

访问 <http://localhost:5000>。

**默认账号**：`admin / admin123`

---

## 端口约定

| 类型 | 服务 / 应用 | 端口 |
| --- | --- | --- |
| 后端 | auth-service | 8081 |
| 后端 | system-service | 8082 |
| 后端 | order-service | 8083 |
| 后端 | user-service | 8084 |
| 后端 | demo01-service … demo10-service | 8091 – 8100 |
| 前端 | shell | 5000 |
| 前端 | trade/order | 5001 |
| 前端 | identity/user | 5002 |
| 前端 | system/admin | 5003 |
| 前端 | lab/demo01 … demo10 | 5011 – 5020 |
| 基础设施 | Nacos / MySQL / Redis | 8848 / 3306 / 6379 |

开发期 shell 将 `/api/auth`、`/api/system`、`/api/orders`、`/api/users` 分别代理到 8081–8084，`/api/demo-01` … `/api/demo-10` 代理到 8091–8100；生产由网关承担同样的路由，前端代码不感知服务端口。

### 10 个示例服务（lab）

`demo01` – `demo10` 是一组刻意做小的样板，用来回答“再加 100 个服务长什么样”：

- 后端：`company-services/demoXX-service`，共 9 个文件（Application、Controller、Service、Repository、entity、2 个 DTO、application.yml、pom.xml），**纯内存存储**，不连 MySQL / Redis，启动即可用。
- 接口：`GET|POST|PUT|DELETE /demo-XX/records`，返回统一 `ApiResult`，带关键字搜索与分页。
- 配置：`application.yml` 只 import L0 + 自己的 L2；端口与专属参数写在 `deploy/nacos-config/services/demoXX-service.yaml`。
- 前端：`company-web/apps/lab/demoXX`，6 个文件，页面是一个最小 CRUD 表格，通过 `registry/remotes.json` 自动出现在“实验室”菜单。
- 启动：`mvn -B -pl company-services/demo01-service spring-boot:run` + `npm run dev -- demo01`；Nacos 未启动时默认配置会失败，但可加 `--spring.cloud.nacos.discovery.enabled=false --spring.cloud.nacos.config.enabled=false` 单机跑通。

新增第 11 个服务只需：复制 `demo01-service` → 改包名 / artifactId / 端口 / context-path → 在 `company-services/pom.xml` 加一行 module → 写一份 L2 配置；前端同理复制 `apps/lab/demo01` → 改 `name` / `port` / API 前缀 → 在 `remotes.json` 加一条。

---

## 常用命令

### 后端

| 命令 | 说明 |
| --- | --- |
| `mvn -B clean install` | 全量构建并安装到本地仓库 |
| `mvn -B test` | 运行单测与 ArchUnit 架构测试 |
| `mvn -B -pl company-services/<name> spring-boot:run` | 启动单个服务 |
| `mvn -B dependency:tree -pl company-services/<name>` | 检查依赖是否绕过 Starter |

### 前端

| 命令 | 说明 |
| --- | --- |
| `npm run dev [-- <id>...]` | 启动 shell 与指定微前端 |
| `npm run build [-- <id>...]` | 生产构建，产物在 `dist/` |
| `npm run check` | 架构体检（目录/清单一致性、依赖方向、暴露面） |
| `npm run typecheck` | 所有工作区 TypeScript 检查 |
| `npm run create -- --id=xx --name=xx --route=/xx --port=50xx` | 脚手架生成微前端并自动注册 |
| `npm run affected` | 查看受改动影响的工作区 |

---

## 文档

| 文档 | 内容 |
| --- | --- |
| [docs/backend.md](docs/backend.md) | 后端分层、模块职责、Nacos L0–L3、新增服务、架构守卫 |
| [docs/frontend.md](docs/frontend.md) | 微前端模型、注册中心、SDK 与事件总线、新增应用、登录流程 |
| [docs/system-module.md](docs/system-module.md) | 若依基础模块：数据模型、接口清单、默认数据与集成方式 |
| [docs/operations.md](docs/operations.md) | 本地环境、配置发布、镜像构建、Helm 部署与发布流程 |
