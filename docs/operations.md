# 环境与部署

## 1. 本地环境

### 基础设施

```bash
cd deploy
docker compose up -d
docker compose ps
```

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| Nacos | 8848 / 9848 | standalone 模式，已开启鉴权（本地固定密钥） |
| MySQL | 3306 | root/root；自动执行 `db/init/01-user_db.sql`、`02-system_db.sql` |
| Redis | 6379 | AOF 持久化 |
| RabbitMQ | 5672 / 15672 | 管理台 guest/guest，`company-mq-starter` 的连接目标 |
| Jaeger | 16686 / 4317 / 4318 | UI 16686，OTLP gRPC 4317、HTTP 4318 |
| Prometheus | 9090 | 抓取各服务 `/actuator/prometheus` |
| Grafana | 3000 | admin/admin，数据源与看板已自动置备 |
| Loki / Promtail | 3100 | Promtail 采集 docker 容器日志并推送至 Loki |

只启动基础设施的最小集（不跑可观测性）：

```bash
cd deploy
docker compose up -d nacos mysql redis rabbitmq
```

初始化脚本会创建：

- `user_db`（服务账号 `app`，仅本库 DML 权限）
- `system_db`（服务账号 `app`，仅本库 DML 权限）

两个库的表结构与种子数据由服务启动时的 Flyway 接管（`src/main/resources/db/migration/V1__init.sql`），已存在的库通过 `baseline-on-migrate` 跳过历史脚本。

### 发布配置

```bash
node deploy/nacos-config/publish.mjs
```

环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `NACOS_ADDR` | `http://127.0.0.1:8848` | Nacos 地址 |
| `NACOS_NS` | 空（public） | 命名空间 ID |
| `NACOS_USERNAME` / `NACOS_PASSWORD` | `nacos` / `nacos` | Nacos 账号 |
| `--with-local` | 不开 | 额外发布 `local/` 覆盖层 |

脚本按 `base → services → local` 顺序发布，dataId 直接使用文件名。

### 启动服务

```bash
mvn -B clean install -DskipTests

mvn -B -pl company-services/gateway-service spring-boot:run
mvn -B -pl company-services/auth-service spring-boot:run
mvn -B -pl company-services/system-service spring-boot:run
```

对外统一走网关 `http://localhost:8080/api/**`；业务服务端口（8081–8100）只用于本地直连调试。

服务启动时按 `application.yaml → mysql.yaml → <service>.yaml` 从 Nacos 拉取配置；Nacos 不可用时使用 `application.yml` 中的本地默认值，但数据库地址仍需可达。

常用环境变量：

| 变量 | 适用服务 | 说明 |
| --- | --- | --- |
| `NACOS_ADDR` / `NACOS_NS` / `NACOS_GROUP` | 全部 | 注册与配置中心 |
| `SYSTEM_DB_URL` / `SYSTEM_DB_USER` / `SYSTEM_DB_PASSWORD` | system-service | system_db 连接 |
| `USER_DB_URL` / `USER_DB_USER` / `USER_DB_PASSWORD` | user-service | user_db 连接 |
| `JWT_SECRET` / `JWT_EXPIRE_MINUTES` | auth-service、gateway-service | 签名密钥（≥ 32 字节），两端必须一致 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 使用 Redis 的服务 | 本地覆盖 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` / `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | 使用 MQ 的服务 | 连接参数 |
| `OTLP_ENDPOINT` | 全部 | OTLP HTTP 地址，默认 `http://localhost:4318/v1/traces` |
| `TRACE_SAMPLE_RATE` | 全部 | 采样率，默认 0.1 |
| `GATEWAY_RATE_LIMIT` | gateway-service | 每 IP 每秒请求上限，默认 200 |

### 前端

```bash
cd company-web
npm install
npm run dev
```

- shell：<http://localhost:5000>
- 单独调试某个微前端：`npm run dev -- admin`
- 单独启动并直连后端：在应用目录执行 `npm run dev`，通过 `VITE_API_BASE` 指向网关或服务地址。

## 2. 镜像构建

镜像只负责组装，构建产物由 CI 产出：

```bash
mvn -B clean package

docker build \
  -f deploy/docker/Dockerfile \
  --build-arg JAR_FILE=company-services/system-service/target/system-service-1.0.0-SNAPSHOT.jar \
  -t registry.company.com/system-service:1.0.0 .
```

运行镜像统一约定：

- 基础镜像 `eclipse-temurin:17-jre-alpine`，非 root 用户 `app`；
- `JAVA_OPTS` 默认 `MaxRAMPercentage=75 + ExitOnOutOfMemoryError + G1GC`；
- 时区 `Asia/Shanghai`；
- 对外端口 `8080`（容器内），实际监听端口由 L2 Nacos 配置决定。

## 3. Helm 部署

Chart 位于 `deploy/k8s/chart`，每个服务一份 values 文件：

```bash
helm upgrade --install system-service deploy/k8s/chart \
  -f deploy/k8s/values/system-service.yaml \
  --namespace company --create-namespace
```

values 关键项：

| 字段 | 说明 |
| --- | --- |
| `replicaCount` | 副本数（默认 2） |
| `image.repository` / `image.tag` | 镜像地址与版本 |
| `containerPort` | 容器端口，需与 L2 配置的 `server.port` 一致 |
| `env` | 非敏感环境变量（Nacos 地址、命名空间、数据库 URL 等） |
| `secretName` | Secret 名称，密码类变量通过 `envFrom` 注入 |
| `resources` | requests/limits，默认 500m/512Mi ~ 1/1Gi |

模板内置：

- `livenessProbe` → `/actuator/health/liveness`
- `readinessProbe` → `/actuator/health/readiness`
- Prometheus 抓取注解 → `/actuator/prometheus`

新增服务时复制一份 `values/<name>-service.yaml`，确保 `containerPort` 与 Nacos L2 中的端口一致。

## 4. 前端发布

```bash
cd company-web
PUBLIC_BASE=https://cdn.company.com/console npm run build
```

- `dist/shell`：主应用，含生产版 `remotes.json`；
- `dist/apps/<id>`：各远端，`entry` 已改写为 `${PUBLIC_BASE}/apps/<id>/remoteEntry.js`；
- 整体上传 `dist/` 即可，新增微前端不需要重新构建 shell，只需更新 `remotes.json`。

## 6. 可观测性

启动整套观测栈：

```bash
cd deploy
docker compose up -d jaeger prometheus grafana loki promtail
```

| 入口 | 地址 | 用途 |
| --- | --- | --- |
| Jaeger UI | <http://localhost:16686> | 按服务名查询链路，traceId 与日志中的 `traceId` 对应 |
| Prometheus | <http://localhost:9090> | 查询指标、确认 target 是否 UP |
| Grafana | <http://localhost:3000> | admin/admin，含 `Company Overview` 看板 |
| Loki | <http://localhost:3100> | 日志查询后端，由 Promtail 推送 |

采集方式：

- **指标**：`deploy/observability/prometheus.yml` 静态抓取宿主机 `host.docker.internal:8081/8082/8083/8084/8091` 的 `/actuator/prometheus`；服务同时暴露 `health`、`info`、`metrics`。新增服务需在 `prometheus.yml` 追加 target 后 `docker compose restart prometheus`。
- **链路**：所有服务通过 `micrometer-tracing-bridge-otel` 上报 OTLP，地址 `management.otlp.tracing.endpoint`（默认 `http://localhost:4318/v1/traces`），传播格式 W3C `traceparent`；网关透传该 header，因此跨服务链路自动串联。采样率由 `TRACE_SAMPLE_RATE` 控制（默认 0.1）。
- **日志**：`deploy/observability/promtail-config.yaml` 通过 docker socket 发现容器，抓取 `/var/lib/docker/containers/*/*-json.log` 并推送到 Loki，标签为容器名与 compose service。
- **看板**：`deploy/observability/grafana/` 由 compose 挂载，datasource（Prometheus uid `prometheus`、Loki uid `loki`）与 dashboard provider 自动置备；新看板 JSON 放进 `grafana/dashboards/` 即生效。

`health` 详情仅对已授权用户展示（`show-details: when-authorized`），避免泄露数据库地址。

## 7. 网关与路由

`gateway-service`（8080）是生产与本地统一的南北向入口，路由来自 Nacos L2 配置 `deploy/nacos-config/services/gateway-service.yaml`。

| 外部路径 | 下游 | 说明 |
| --- | --- | --- |
| `/api/auth/**` | `lb://auth-service` | 登录与令牌，`/api/auth/login` 免鉴权 |
| `/api/system/**` | `lb://system-service` | 若依基础模块 |
| `/api/user/**`、`/api/users/**` | `lb://user-service` | `/api/users/**` 重写为 `/internal/users` |
| `/api/order/**`、`/api/orders/**` | `lb://order-service` | 兼容两种历史路径 |
| `/api/demo-01/**` … `/api/demo-10/**` | `lb://demoXX-service` | 去掉 `/api` 后转发 |

鉴权约定：

- 除 `/api/auth/login`、`/actuator/**` 与 `OPTIONS` 外，全部要求 `Authorization: Bearer <token>`；
- 校验失败返回 `401`，body 为 `{"code":401,"message":"未认证或令牌已失效","data":null}`；
- 通过后注入 `X-User-Id`、`X-Username` 转发给下游，下游不再重复解析 JWT；
- 超过每 IP 限流阈值（默认 200 req/s）返回 `429`。

修改路由后：先 `node deploy/nacos-config/publish.mjs` 发布，再重启或等待网关刷新；新增路由必须对照下游 Controller 的实际 `@RequestMapping` 前缀确认 `StripPrefix` 数量。

## 8. 发布检查清单

**后端**

- [ ] `mvn -B clean verify` 通过（单测 + Enforcer + ArchUnit）
- [ ] Nacos L2 配置已发布到目标命名空间
- [ ] 数据库变更以 `db/migration/V*.sql` 新增，未修改已发布脚本
- [ ] 服务账号权限仅限本库 DML
- [ ] 敏感变量通过 K8s Secret 注入，未写入配置中心或镜像
- [ ] `JWT_SECRET` 至少 32 字节，auth-service 与 gateway-service 取值一致
- [ ] 新增对外接口已在网关配置路由，并在 Prometheus 抓取列表登记

**前端**

- [ ] `npm run check` 通过
- [ ] `npm run lint` 通过
- [ ] `npm run typecheck` 通过
- [ ] `npm run build` 通过，`dist/` 完整
- [ ] `remotes.json` 中的 `entry` 指向正确的 CDN 地址
- [ ] 网关已配置 `/api/auth`、`/api/system`、`/api/orders`、`/api/users` 路由
