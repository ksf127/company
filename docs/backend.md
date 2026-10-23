# 后端架构

## 1. 模块职责

| 模块 | 职责 | 约束 |
| --- | --- | --- |
| `company-bom` | `dependencyManagement` 版本清单 | 不写构建插件、不写业务依赖 |
| `company-parent` | 编译器、Surefire、Enforcer、Lombok 注解处理器 | 所有服务 POM 的 parent，不写业务依赖 |
| `company-common/common-core` | `ApiResult`、`ErrorCode`、`BizException` 等内核 | 零框架依赖，任何模块可引用 |
| `company-common/common-web` | 全局异常处理、Web 公共配置 | 只被 `company-services` 继承 |
| `company-starters/*` | 中间件接入的唯一入口 | 业务服务禁止直接引入 Redis / MySQL / MyBatis-Plus 客户端 |
| `company-api/*` | 服务间契约：DTO + Feign 接口 | 只放契约，不放实现 |
| `company-services/*` | 可运行服务 | 新增服务只能加在这里 |

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
| L1 | `mysql.yaml`、`redis.yaml` | 连接池参数、MyBatis-Plus 通用行为、集群地址 | 使用该中间件的全部服务 |
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

## 4. 统一响应与异常

```java
public record ApiResult<T>(int code, String message, T data) {
    public static <T> ApiResult<T> ok(T data) { ... }        // code = 0
    public static <T> ApiResult<T> fail(ErrorCode c, String m) { ... }
}
```

- 只有 `code == 0` 表示成功，前端 SDK 依此解包。
- 业务异常抛 `BizException(ErrorCode, message)`，由 `common-web` 的全局异常处理转换为 `ApiResult`。
- 分页统一返回 `PageResult<T>(long total, List<T> rows)`。

## 5. 新增一个服务

1. 在 `company-api/` 下新建 `<name>-api`（需要被别的服务调用时），并在 `company-api/pom.xml` 注册模块、在 `company-bom` 声明版本。
2. 在 `company-services/` 下新建 `<name>-service`，parent 指向 `company-services/pom.xml`。
3. 只允许通过 Starter 引入中间件依赖。
4. 创建 `src/main/resources/application.yml`，按 L0 → L1 → L2 顺序 import Nacos 配置。
5. 在 `deploy/nacos-config/services/<name>-service.yaml` 写端口与专属配置。
6. 在 `deploy/k8s/values/<name>-service.yaml` 写镜像与副本数。
7. 添加 `ArchitectureTest`（可复制现有服务的测试类）。
8. 运行 `mvn -B test`，确认 Enforcer 与 ArchUnit 都通过。

## 6. 现有服务

| 服务 | 端口 | 数据库 | 说明 |
| --- | --- | --- | --- |
| `auth-service` | 8081 | 无（无状态） | JWT 登录、用户信息、登录日志写入；通过 Feign 调 system-service |
| `system-service` | 8082 | `system_db` | 若依基础模块 |
| `order-service` | 8083 | 示例库 | 交易域示例 |
| `user-service` | 8084 | `user_db` | 身份域示例 |
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
