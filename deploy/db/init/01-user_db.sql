-- 每个服务一个库：user-service 只拥有 user_db
CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE user_db;

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

-- 服务账号只授予自己库的权限
GRANT SELECT, INSERT, UPDATE, DELETE ON user_db.* TO 'app'@'%';
FLUSH PRIVILEGES;
