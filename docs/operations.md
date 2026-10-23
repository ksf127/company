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

初始化脚本会创建：

- `user_db`（服务账号 `app`，仅本库 DML 权限）
- `system_db`（服务账号 `app`，仅本库 DML 权限）

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

mvn -B -pl company-services/auth-service spring-boot:run
mvn -B -pl company-services/system-service spring-boot:run
```

服务启动时按 `application.yaml → mysql.yaml → <service>.yaml` 从 Nacos 拉取配置；Nacos 不可用时使用 `application.yml` 中的本地默认值，但数据库地址仍需可达。

常用环境变量：

| 变量 | 适用服务 | 说明 |
| --- | --- | --- |
| `NACOS_ADDR` / `NACOS_NS` / `NACOS_GROUP` | 全部 | 注册与配置中心 |
| `SYSTEM_DB_URL` / `SYSTEM_DB_USER` / `SYSTEM_DB_PASSWORD` | system-service | system_db 连接 |
| `USER_DB_URL` / `USER_DB_USER` / `USER_DB_PASSWORD` | user-service | user_db 连接 |
| `JWT_SECRET` / `JWT_EXPIRE_MINUTES` | auth-service | 签名密钥（≥ 32 字节）与有效期 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 使用 Redis 的服务 | 本地覆盖 |

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

## 5. 发布检查清单

**后端**

- [ ] `mvn -B clean verify` 通过（单测 + Enforcer + ArchUnit）
- [ ] Nacos L2 配置已发布到目标命名空间
- [ ] 数据库变更脚本已执行，服务账号权限仅限本库
- [ ] 敏感变量通过 K8s Secret 注入，未写入配置中心或镜像
- [ ] `JWT_SECRET` 至少 32 字节且与其它环境不同

**前端**

- [ ] `npm run check` 通过
- [ ] `npm run typecheck` 通过
- [ ] `npm run build` 通过，`dist/` 完整
- [ ] `remotes.json` 中的 `entry` 指向正确的 CDN 地址
- [ ] 网关已配置 `/api/auth`、`/api/system`、`/api/orders`、`/api/users` 路由

## 6. 可观测性

- 所有服务暴露 `health`、`info`、`prometheus`；
- `health` 详情仅对已授权用户展示（`show-details: when-authorized`），避免泄露数据库地址；
- 日志格式包含 `traceId`，采样率由 `TRACE_SAMPLE_RATE` 控制（默认 0.1）；
- K8s 部署自动被 Prometheus 抓取，指标带 `application=<service-name>` 标签。
