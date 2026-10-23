-- Flyway V1：user-service 独占 user_db（Database per Service）。
-- 迁移在应用启动时以服务账号执行，因此不包含 CREATE DATABASE / USE / GRANT；
-- 库与授权由 deploy/db/init 或 DBA 在实例初始化阶段完成。

CREATE TABLE IF NOT EXISTS t_user (
    id           BIGINT       NOT NULL COMMENT '雪花 ID',
    username     VARCHAR(64)  NOT NULL COMMENT '登录名',
    display_name VARCHAR(64)  NOT NULL COMMENT '昵称',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT = '用户表';

INSERT INTO t_user (id, username, display_name)
VALUES (1, 'demo', '演示用户')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);
