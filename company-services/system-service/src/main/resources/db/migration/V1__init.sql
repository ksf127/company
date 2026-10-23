-- Flyway V1：system-service 独占 system_db（Database per Service）。
-- 迁移在应用启动时以服务账号执行，因此不包含 CREATE DATABASE / USE / GRANT；
-- 库与授权由 deploy/db/init 或 DBA 在实例初始化阶段完成。
-- 与若依 sys_* 表结构保持一致；逻辑删除统一使用平台约定的 deleted（1 已删除 / 0 正常）。

CREATE TABLE IF NOT EXISTS sys_user (
    user_id     BIGINT       NOT NULL COMMENT '用户 ID',
    dept_id     BIGINT       NULL COMMENT '部门 ID',
    user_name   VARCHAR(30)  NOT NULL COMMENT '登录账号',
    nick_name   VARCHAR(30)  NOT NULL COMMENT '用户昵称',
    user_type   VARCHAR(10)  NULL DEFAULT '00' COMMENT '用户类型（00 系统用户）',
    email       VARCHAR(50)  NULL DEFAULT '' COMMENT '邮箱',
    phonenumber VARCHAR(11)  NULL DEFAULT '' COMMENT '手机号',
    sex         CHAR(1)      NULL DEFAULT '0' COMMENT '性别（0 男 1 女 2 未知）',
    avatar      VARCHAR(100) NULL DEFAULT '' COMMENT '头像地址',
    password    VARCHAR(100) NULL DEFAULT '' COMMENT 'BCrypt 密码散列',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '账号状态（0 正常 1 停用）',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    login_ip    VARCHAR(128) NULL DEFAULT '' COMMENT '最后登录 IP',
    login_date  DATETIME     NULL COMMENT '最后登录时间',
    create_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_name (user_name),
    KEY idx_dept_id (dept_id)
) ENGINE = InnoDB COMMENT = '用户信息表';

CREATE TABLE IF NOT EXISTS sys_role (
    role_id     BIGINT       NOT NULL COMMENT '角色 ID',
    role_name   VARCHAR(30)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    role_sort   INT          NULL DEFAULT 0 COMMENT '显示顺序',
    data_scope  CHAR(1)      NULL DEFAULT '1' COMMENT '数据范围',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '角色状态（0 正常 1 停用）',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE = InnoDB COMMENT = '角色信息表';

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id     BIGINT       NOT NULL COMMENT '菜单 ID',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    parent_id   BIGINT       NULL DEFAULT 0 COMMENT '父菜单 ID',
    order_num   INT          NULL DEFAULT 0 COMMENT '显示顺序',
    path        VARCHAR(200) NULL DEFAULT '' COMMENT '路由地址',
    component   VARCHAR(255) NULL COMMENT '组件路径',
    query       VARCHAR(255) NULL COMMENT '路由参数',
    is_frame    CHAR(1)      NULL DEFAULT '1' COMMENT '是否外链（0 是 1 否）',
    is_cache    CHAR(1)      NULL DEFAULT '0' COMMENT '是否缓存（0 缓存 1 不缓存）',
    menu_type   CHAR(1)      NULL DEFAULT '' COMMENT '类型（M 目录 C 菜单 F 按钮）',
    visible     CHAR(1)      NULL DEFAULT '0' COMMENT '显示状态（0 显示 1 隐藏）',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '菜单状态（0 正常 1 停用）',
    perms       VARCHAR(100) NULL COMMENT '权限标识',
    icon        VARCHAR(100) NULL DEFAULT '#' COMMENT '菜单图标',
    create_time DATETIME     NULL COMMENT '创建时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (menu_id),
    KEY idx_parent_id (parent_id)
) ENGINE = InnoDB COMMENT = '菜单权限表';

CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id     BIGINT       NOT NULL COMMENT '部门 ID',
    parent_id   BIGINT       NULL DEFAULT 0 COMMENT '父部门 ID',
    ancestors   VARCHAR(50)  NULL DEFAULT '' COMMENT '祖级列表',
    dept_name   VARCHAR(30)  NULL DEFAULT '' COMMENT '部门名称',
    order_num   INT          NULL DEFAULT 0 COMMENT '显示顺序',
    leader      VARCHAR(20)  NULL COMMENT '负责人',
    phone       VARCHAR(11)  NULL COMMENT '联系电话',
    email       VARCHAR(50)  NULL COMMENT '邮箱',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '部门状态（0 正常 1 停用）',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    PRIMARY KEY (dept_id)
) ENGINE = InnoDB COMMENT = '部门表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id     BIGINT       NOT NULL COMMENT '字典主键',
    dict_name   VARCHAR(100) NULL DEFAULT '' COMMENT '字典名称',
    dict_type   VARCHAR(100) NULL DEFAULT '' COMMENT '字典类型',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '状态（0 正常 1 停用）',
    create_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (dict_id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE = InnoDB COMMENT = '字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code   BIGINT       NOT NULL COMMENT '字典编码',
    dict_sort   INT          NULL DEFAULT 0 COMMENT '字典排序',
    dict_label  VARCHAR(100) NULL DEFAULT '' COMMENT '字典标签',
    dict_value  VARCHAR(100) NULL DEFAULT '' COMMENT '字典键值',
    dict_type   VARCHAR(100) NULL DEFAULT '' COMMENT '字典类型',
    css_class   VARCHAR(100) NULL COMMENT '样式属性',
    list_class  VARCHAR(100) NULL COMMENT '表格回显样式',
    is_default  CHAR(1)      NULL DEFAULT 'N' COMMENT '是否默认（Y 是 N 否）',
    status      CHAR(1)      NULL DEFAULT '0' COMMENT '状态（0 正常 1 停用）',
    create_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (dict_code),
    KEY idx_dict_type (dict_type)
) ENGINE = InnoDB COMMENT = '字典数据表';

CREATE TABLE IF NOT EXISTS sys_config (
    config_id    BIGINT       NOT NULL COMMENT '参数主键',
    config_name  VARCHAR(100) NULL DEFAULT '' COMMENT '参数名称',
    config_key   VARCHAR(100) NULL DEFAULT '' COMMENT '参数键名',
    config_value VARCHAR(500) NULL DEFAULT '' COMMENT '参数键值',
    config_type  CHAR(1)      NULL DEFAULT 'N' COMMENT '系统内置（Y 是 N 否）',
    create_by    VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    create_time  DATETIME     NULL COMMENT '创建时间',
    update_by    VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    update_time  DATETIME     NULL COMMENT '更新时间',
    remark       VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE = InnoDB COMMENT = '参数配置表';

CREATE TABLE IF NOT EXISTS sys_logininfor (
    info_id    BIGINT       NOT NULL COMMENT '访问 ID',
    user_name  VARCHAR(50)  NULL DEFAULT '' COMMENT '登录账号',
    ipaddr     VARCHAR(128) NULL DEFAULT '' COMMENT '登录 IP',
    browser    VARCHAR(50)  NULL DEFAULT '' COMMENT '浏览器',
    os         VARCHAR(50)  NULL DEFAULT '' COMMENT '操作系统',
    status     CHAR(1)      NULL DEFAULT '0' COMMENT '登录状态（0 成功 1 失败）',
    msg        VARCHAR(255) NULL DEFAULT '' COMMENT '提示消息',
    login_time DATETIME     NULL COMMENT '访问时间',
    PRIMARY KEY (info_id),
    KEY idx_login_time (login_time)
) ENGINE = InnoDB COMMENT = '系统访问记录';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE = InnoDB COMMENT = '用户与角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    menu_id BIGINT NOT NULL COMMENT '菜单 ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE = InnoDB COMMENT = '角色与菜单关联表';

-- ---------------------------------------------------------------------------
-- 种子数据：默认管理员 admin / admin123（首次登录后请立即修改）
-- ---------------------------------------------------------------------------
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, deleted)
VALUES (100, 0, '0', '总公司', 0, '管理员', '15888888888', 'admin@company.com', '0', 0),
       (101, 100, '0,100', '研发部', 1, '管理员', '15888888888', 'dev@company.com', '0', 0)
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, deleted, create_by, create_time, remark)
VALUES (1, '超级管理员', 'admin', 1, '1', '0', 0, 'system', NOW(), '拥有全部权限'),
       (2, '普通用户', 'common', 2, '1', '0', 0, 'system', NOW(), '只读权限')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- BCrypt(admin123)
INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar,
                      password, status, deleted, create_by, create_time, remark)
VALUES (1, 100, 'admin', '超级管理员', '00', 'admin@company.com', '15888888888', '0', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 0, 'system', NOW(), '系统内置管理员')
ON DUPLICATE KEY UPDATE nick_name = VALUES(nick_name);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_time, remark)
VALUES (1000, '系统管理', 0, 1, 'system', NULL, '1', '0', 'M', '0', '0', NULL, 'setting', NOW(), '系统管理目录'),
       (1001, '用户管理', 1000, 1, 'user', 'system/user/index', '1', '0', 'C', '0', '0', 'system:user:list', 'user', NOW(), ''),
       (1002, '角色管理', 1000, 2, 'role', 'system/role/index', '1', '0', 'C', '0', '0', 'system:role:list', 'peoples', NOW(), ''),
       (1003, '菜单管理', 1000, 3, 'menu', 'system/menu/index', '1', '0', 'C', '0', '0', 'system:menu:list', 'tree-table', NOW(), ''),
       (1004, '部门管理', 1000, 4, 'dept', 'system/dept/index', '1', '0', 'C', '0', '0', 'system:dept:list', 'tree', NOW(), ''),
       (1005, '字典管理', 1000, 5, 'dict', 'system/dict/index', '1', '0', 'C', '0', '0', 'system:dict:list', 'dict', NOW(), ''),
       (1006, '参数设置', 1000, 6, 'config', 'system/config/index', '1', '0', 'C', '0', '0', 'system:config:list', 'edit', NOW(), ''),
       (1007, '登录日志', 1000, 7, 'logininfor', 'system/logininfor/index', '1', '0', 'C', '0', '0', 'system:logininfor:list', 'logininfor', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES (1, '系统开关', 'sys_normal_disable', '0', 'system', NOW(), '系统开关列表'),
       (2, '用户性别', 'sys_user_sex', '0', 'system', NOW(), '用户性别列表')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (1, 1, '正常', '0', 'sys_normal_disable', 'primary', 'Y', '0', 'system', NOW()),
       (2, 2, '停用', '1', 'sys_normal_disable', 'danger', 'N', '0', 'system', NOW()),
       (3, 1, '男', '0', 'sys_user_sex', '', 'Y', '0', 'system', NOW()),
       (4, 2, '女', '1', 'sys_user_sex', '', 'N', '0', 'system', NOW()),
       (5, 3, '未知', '2', 'sys_user_sex', '', 'N', '0', 'system', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES (1, '系统名称', 'sys.app.name', 'Company', 'Y', 'system', NOW(), '系统名称'),
       (2, '用户初始密码', 'sys.user.initPassword', '123456', 'Y', 'system', NOW(), '新建用户默认密码')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
