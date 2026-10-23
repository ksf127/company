# 后端架构

## 1. 模块职责

| 模块 | 职责 | 约束 |
| --- | --- | --- |
| `company-bom` | `dependencyManagement` 版本清单 | 不写构建插件、不写业务依赖 |
| `company-parent` | 编译器、Surefire、Enforcer、Lombok 注解处理器 | 所有服务 POM 的 parent，不写业务依赖 |
| `company-common/common-core` | `ApiResult`、`ErrorCode`、`BizException` 等内核 | 零框架依赖，任何模块可引用 |
| `company-common/common-web` | 全局异常处理、Web 公共配置 | 只被 `company-services` 继承 |
| `company-starters/*` | 中间件接入的唯一入口 | 业务服务禁止直接引入 Redis / MySQL / MyBatis-Plus / RabbitMQ 客户端 |
| `company-api/*` | 服务间契约：DTO + Feign 接口 | 只放契约，不放实现 |
| `company-services/*` | 可运行服务 | 新增服务只能加在这里 |

Starter 清单：

| Starter | 提供的能力 | 自动配置类 |
| --- | --- | --- |
| `company-redis-starter` | `RedisTemplate`、序列化与连接池默认值 | `CompanyRedisAutoConfiguration` |
| `company-mybatis-starter` | MyBatis-Plus、分页插件、数据源、Flyway（`db/migration`） | `CompanyMybatisAutoConfiguration` |
| `company-mq-starter` | `RabbitTemplate`（JSON 序列化、publisher confirms / returns）、监听容器（AUTO ack、3 次退避重试后丢弃，不重回队列） | `CompanyMqAutoConfiguration` |
| `company-job-starter` | `ThreadPoolTaskScheduler` + `@EnableScheduling`，线程数 `company.job.pool-size`（默认 4） | `CompanyJobAutoConfiguration` |

Starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册，业务服务只加依赖即可，不需要 `@Import`。

根 POM 只做聚合：不写 `dependencyManagement`、不写 `pluginManagement`。版本问题去 BOM，构建问题去 parent。

## 2. 服务内部分层

```text
controller  →  service  →  repository  →  mapper
                                  ↘  entity
```

- `controller`：只做参数接收、校验、调用 service、返回 `ApiResult`。
- `service`：业务编排、事务边界；**不得出现** MyBatis-Plus、Redis 客户端类型。
- `repository`：唯一允许使用 `MyBatis-Plus` / `Mapper` 的层，向 service 暴露领域方法。
- `mapper`：MyBatis 接口。
- `entity`：数据库映射对象，只在 repository / mapper / service 内部流转。

这套分层由两个"物理屏障"保证：

**ArchUnit（`src/test/java/**/ArchitectureTest.java`）**

- controller 不得依赖 mapper / repository；
- service 不得依赖 mapper；
- service 不得依赖 `org.springframework.data.redis..`、`com.baomidou.mybatisplus..`、`redis.clients..`；
- 包名必须使用全限定形式（例如 `com.company.service.system.service..`）。
  不要写 `..service..`：基础包 `com.company.service.system` 本身含 `service` 段，会被 ArchUnit 子串匹配到所有包。

**Maven Enforcer（`company-services/pom.xml`）**

所有服务默认禁止直接依赖：

```text
org.springframework.boot:spring-boot-starter-data-redis
redis.clients:jedis
com.mysql:mysql-connector-j
com.baomidou:mybatis-plus-spring-boot3-starter
```

需要 Redis / 数据库时，只能在服务 POM 引入 `company-redis-starter` / `company-mybatis-starter`。

## 3. Nacos 配置分层

配置文件放在 `deploy/nacos-config/`，由 `publish.mjs` 发布，禁止在控制台手改生产配置。

| 层级 | dataId | 内容 | 变更影响 |
| --- | --- | --- | --- |
| L0 | `application.yaml` | Jackson、优雅停机、Tomcat 线程、Actuator、日志格式、Feign 默认超时 | 全部服务 |
| L1 | `mysql.yaml`、`redis.yaml`、`rabbitmq.yaml` | 连接池参数、MyBatis-Plus 通用行为、Flyway、MQ 确认与重试、集群地址 | 使用该中间件的全部服务 |
| L2 | `<service-name>.yaml` | 端口、数据源 URL/账号、服务专属业务参数、Feign 单服务超时 | 仅该服务 |
| L3 | 环境变量 / K8s Secret | 数据库密码、JWT 密钥、各环境地址 | 仅该环境 |

服务通过 `spring.config.import` 按顺序导入：

```yaml
spring:
  config:
    import:
      - optional:nacos:application.yaml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:mysql.yaml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:${spring.application.name}.yaml?group=${NACOS_GROUP:DEFAULT_GROUP}&refreshEnabled=true
```

约定：

- 配置里永远不出现明文密码，统一 `${ENV_VAR:默认值}`。
- `spring.application.name` 必须与 L2 dataId 同名，否则服务读不到自己的配置。
- 本地联调使用 `--with-local` 发布 `local/` 覆盖层，只影响本地 namespace。

发布：

```bash
node deploy/nacos-config/publish.mjs
NACOS_NS=test node deploy/nacos-config/publish.mjs
```

## 4. 数据库版本（Flyway）

Flyway 随 `company-mybatis-starter` 下发，无需在业务服务单独声明依赖：

```text
company-services/<name>-service/src/main/resources/
└── db/migration/
    ├── V1__init.sql          # 建表 + 种子数据
    └── V2__add_xxx.sql       # 后续增量，只增不改
```

- `deploy/nacos-config/base/mysql.yaml` 统一配置：`locations=classpath:db/migration`、`baseline-on-migrate=true`、`baseline-version=0`、`validate-on-migrate=true`。
- `baseline-on-migrate` 让已有库（由 `deploy/db/init/*.sql` 初始化）平滑接管，不会重复执行历史脚本。
- 脚本必须幂等写法（`CREATE TABLE IF NOT EXISTS`），不写 `CREATE DATABASE` / `USE`，库由服务自己的数据源 URL 决定。
- 变更只新增版本文件，禁止修改已发布的 `V*.sql`，否则 `validate-on-migrate` 会拒绝启动。
- 单测不使用真实数据库：服务测试以 Mockito 替代 repository，`mvn test` 不需要 MySQL。

## 5. 统一网关

`gateway-service`（8080）是基于 Spring Cloud Gateway 的 WebFlux 应用，是前端与外部系统访问后端的唯一入口。

- **鉴权**：`JwtAuthGlobalFilter` 校验 `Authorization: Bearer <token>`，密钥读取 `company.auth.jwt.secret`，必须与 auth-service 一致。
- **白名单**：`/api/auth/login`、`/actuator/**` 与 `OPTIONS` 预检请求直接放行。
- **失败响应**：缺失或非法 token 返回 401，body 为 `{"code":401,"message":"未认证或令牌已失效","data":null}`。
- **身份透传**：校验通过后向下游注入 `X-User-Id`、`X-Username`，下游服务无需重复解析 JWT。
- **限流**：`IpRateLimitGlobalFilter` 按 IP 做内存固定窗口限流，默认 200 req/s（`company.gateway.rate-limit-per-second`），超出返回 429。

路由写在 L2 配置 `deploy/nacos-config/services/gateway-service.yaml`，路径规则与各 Controller 的实际路径对齐：

| 外部路径 | 下游 | 转发方式 |
| --- | --- | --- |
| `/api/auth/**` | `lb://auth-service` | `StripPrefix=1` |
| `/api/system/**` | `lb://system-service` | `StripPrefix=1` |
| `/api/user/**`、`/api/users/**` | `lb://user-service` | `StripPrefix=1` / `RewritePath` |
| `/api/order/**`、`/api/orders/**` | `lb://order-service` | `StripPrefix=1` / `RewritePath` |
| `/api/demo-01/**` … `/api/demo-10/**` | `lb://demo01-service` … | `StripPrefix=1` |

修改网关路由后必须重新发布 Nacos 配置，并逐个核对下游 Controller 的 `@RequestMapping` 前缀。开发期前端仍由 shell 的 Vite 代理直连各服务端口，不经过网关。

## 6. 统一响应与异常

```java
public record ApiResult<T>(int code, String message, T data) {
    public static <T> ApiResult<T> ok(T data) { ... }        // code = 0
    public static <T> ApiResult<T> fail(ErrorCode c, String m) { ... }
}
```

- 只有 `code == 0` 表示成功，前端 SDK 依此解包。
- 业务异常抛 `BizException(ErrorCode, message)`，由 `common-web` 的全局异常处理转换为 `ApiResult`。
- 分页统一返回 `PageResult<T>(long total, List<T> rows)`。

## 7. 新增一个服务

1. 在 `company-api/` 下新建 `<name>-api`（需要被别的服务调用时），并在 `company-api/pom.xml` 注册模块、在 `company-bom` 声明版本。
2. 在 `company-services/` 下新建 `<name>-service`，parent 指向 `company-services/pom.xml`。
3. 只允许通过 Starter 引入中间件依赖。
4. 创建 `src/main/resources/application.yml`，按 L0 → L1 → L2 顺序 import Nacos 配置。
5. 在 `deploy/nacos-config/services/<name>-service.yaml` 写端口与专属配置；如对外暴露，追加一条网关路由。
6. 需要数据库时在 `src/main/resources/db/migration/` 放 `V1__init.sql`，由 Flyway 建表。
7. 在 `deploy/k8s/values/<name>-service.yaml` 写镜像与副本数。
8. 添加 `ArchitectureTest`（可复制现有服务的测试类）。
9. 运行 `mvn -B test`，确认 Enforcer 与 ArchUnit 都通过。

## 8. 现有服务

| 服务 | 端口 | 数据库 | 说明 |
| --- | --- | --- | --- |
| `gateway-service` | 8080 | 无 | 统一入口：JWT 校验、身份透传、限流、路由（WebFlux） |
| `auth-service` | 8081 | 无（无状态） | JWT 登录、用户信息、登录日志写入；通过 Feign 调 system-service |
| `system-service` | 8082 | `system_db`（Flyway 管理） | 若依基础模块；含示例定时任务 `SystemSummaryJob` |
| `order-service` | 8083 | 示例库 | 交易域示例 |
| `user-service` | 8084 | `user_db`（Flyway 管理） | 身份域示例 |
| `demo01-service` … `demo10-service` | 8091 – 8100 | 无（内存） | 最小样板：单表 CRUD，用来对照扩展 100+ 服务时的目录与配置 |

`auth-service` 不直接访问 `system_db`，只通过 `system-api` 的 `SystemClient` 调用 system-service 的 `/internal/system/**` 接口，保证数据库归属单一。

### 示例服务（demo01 – demo10）

这 10 个服务是刻意压缩到最小的模板，只保留扩展时需要改动的部分：

```text
company-services/demoXX-service/
├── pom.xml                                  # parent 指向 company-services，只声明 boot 插件
└── src/main/
    ├── java/com/company/service/demoXX/
    │   ├── DemoXXServiceApplication.java
    │   ├── controller/DemoRecordController.java   # GET/POST/PUT/DELETE /records
    │   ├── service/DemoRecordService.java         # 分页、校验、BizException
    │   ├── repository/DemoRecordRepository.java   # 内存 Map，模拟持久层
    │   ├── entity/DemoRecord.java
    │   └── dto/{PageResult,SaveRecordRequest}.java
    └── resources/application.yml               # 端口、context-path、import L0 + L2
```

约定：端口 `8090 + 序号`，`context-path: /demo-XX`，L2 dataId 为 `demoXX-service.yaml`。仓库层用 `ConcurrentHashMap` + `AtomicLong`，因此不引入 MyBatis / 数据源，重启即复位。新增同类服务时复制目录、替换包名与端口、在 `company-services/pom.xml` 加一行 `<module>` 即可。
