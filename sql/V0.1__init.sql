-- ============================================================
-- 摄影作品平台 数据库初始化脚本
-- 版本：V0.1
-- 数据库：MySQL 8.0+
-- 说明：不使用外键和触发器，关联关系由应用层维护
-- ============================================================

-- -----------------------------------------------------------
-- 1. t_sys_user 用户表
-- -----------------------------------------------------------
CREATE TABLE t_sys_user (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    openid      VARCHAR(64)  NOT NULL                                COMMENT '微信openid',
    nickname    VARCHAR(64)  NOT NULL DEFAULT ''                     COMMENT '微信昵称',
    avatar      VARCHAR(512) NOT NULL DEFAULT ''                     COMMENT '头像URL',
    role        VARCHAR(20)  NOT NULL DEFAULT 'buyer'                COMMENT '角色：buyer-买家 photographer-摄影师 admin-管理员',
    points      INT          NOT NULL DEFAULT 0                      COMMENT '积分余额',
    is_banned   TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '是否封禁 0-正常 1-封禁',
    org_id      BIGINT                DEFAULT NULL                   COMMENT '所属组织ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. t_sys_category 分类表
-- -----------------------------------------------------------
CREATE TABLE t_sys_category (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL                                COMMENT '分类名称',
    sort        INT          NOT NULL DEFAULT 0                      COMMENT '排序值，升序',
    photo_count INT          NOT NULL DEFAULT 0                      COMMENT '作品数量（冗余，定期同步）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';

-- -----------------------------------------------------------
-- 3. t_sys_tag 标签表
-- -----------------------------------------------------------
CREATE TABLE t_sys_tag (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL                                COMMENT '标签名称',
    category_id BIGINT       NOT NULL                                COMMENT '所属分类ID',
    sort        INT          NOT NULL DEFAULT 0                      COMMENT '排序值，升序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_sys_tag_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- -----------------------------------------------------------
-- 4. t_biz_photo 作品表
-- -----------------------------------------------------------
CREATE TABLE t_biz_photo (
    id              BIGINT       NOT NULL                            COMMENT '主键ID',
    title           VARCHAR(50)  NOT NULL                            COMMENT '作品标题',
    description     VARCHAR(500) NOT NULL DEFAULT ''                 COMMENT '作品描述',
    preview_url     VARCHAR(255) NOT NULL                            COMMENT '预览图URL',
    original_key    VARCHAR(255) NOT NULL                            COMMENT '原图OSS Key',
    category_id     BIGINT       NOT NULL                            COMMENT '分类ID',
    category_name   VARCHAR(50)  NOT NULL                            COMMENT '分类名称（冗余，避免列表JOIN）',
    user_id         BIGINT       NOT NULL                            COMMENT '摄影师用户ID',
    price           INT          NOT NULL                            COMMENT '积分价格 10~9999',
    allow_license   TINYINT(1)   NOT NULL DEFAULT 1                  COMMENT '是否允许授权申请 0-否 1-是',
    status          VARCHAR(20)  NOT NULL DEFAULT 'pending'          COMMENT '状态：pending-待审核 approved-已上架 rejected-已拒绝 offline-已下架',
    reject_reason   VARCHAR(200)          DEFAULT NULL               COMMENT '审核拒绝原因',
    purchase_count  INT          NOT NULL DEFAULT 0                  COMMENT '购买次数（冗余，下单时+1）',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT                DEFAULT NULL               COMMENT '创建人ID',
    update_by       BIGINT                DEFAULT NULL               COMMENT '修改人ID',
    version         INT          NOT NULL DEFAULT 1                  COMMENT '乐观锁版本号',
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0                  COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_biz_photo_user_id (user_id),
    KEY idx_biz_photo_category_id (category_id),
    KEY idx_biz_photo_status_create (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作品表';

-- -----------------------------------------------------------
-- 5. t_biz_photo_tag 作品-标签关联表
-- -----------------------------------------------------------
CREATE TABLE t_biz_photo_tag (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    photo_id    BIGINT       NOT NULL                                COMMENT '作品ID',
    tag_id      BIGINT       NOT NULL                                COMMENT '标签ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_photo_tag (photo_id, tag_id),
    KEY idx_biz_photo_tag_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作品-标签关联表';

-- -----------------------------------------------------------
-- 6. t_biz_order 订单表
-- -----------------------------------------------------------
CREATE TABLE t_biz_order (
    id                  BIGINT       NOT NULL                        COMMENT '主键ID',
    user_id             BIGINT       NOT NULL                        COMMENT '买家用户ID',
    photo_id            BIGINT       NOT NULL                        COMMENT '作品ID',
    photographer_id     BIGINT       NOT NULL                        COMMENT '摄影师用户ID',
    photo_title         VARCHAR(50)  NOT NULL                        COMMENT '作品标题（快照）',
    photo_preview_url   VARCHAR(255) NOT NULL                        COMMENT '预览图URL（快照）',
    price               INT          NOT NULL                        COMMENT '成交积分价格（快照）',
    platform_fee        INT          NOT NULL                        COMMENT '平台抽佣积分（10%）',
    photographer_earned INT          NOT NULL                        COMMENT '摄影师实得积分（90%）',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT                DEFAULT NULL           COMMENT '创建人ID',
    update_by           BIGINT                DEFAULT NULL           COMMENT '修改人ID',
    version             INT          NOT NULL DEFAULT 1              COMMENT '乐观锁版本号',
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0              COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_order_user_photo (user_id, photo_id),
    KEY idx_biz_order_photographer_id (photographer_id),
    KEY idx_biz_order_photo_id (photo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- -----------------------------------------------------------
-- 7. t_biz_license 授权申请表
-- -----------------------------------------------------------
CREATE TABLE t_biz_license (
    id                BIGINT       NOT NULL                          COMMENT '主键ID',
    photo_id          BIGINT       NOT NULL                          COMMENT '作品ID',
    photo_title       VARCHAR(50)  NOT NULL                          COMMENT '作品标题（冗余）',
    photo_preview_url VARCHAR(255) NOT NULL                          COMMENT '预览图URL（冗余）',
    applicant_id      BIGINT       NOT NULL                          COMMENT '申请人用户ID',
    photographer_id   BIGINT       NOT NULL                          COMMENT '摄影师用户ID',
    purpose           VARCHAR(20)  NOT NULL                          COMMENT '用途：commercial/news/personal/education/other',
    scene             VARCHAR(200) NOT NULL                          COMMENT '使用场景描述',
    duration          VARCHAR(100) NOT NULL                          COMMENT '使用期限（自由填写）',
    contact           VARCHAR(50)  NOT NULL                          COMMENT '联系方式',
    status            VARCHAR(30)  NOT NULL DEFAULT 'pending_admin'  COMMENT '状态：pending_admin-待管理员审核 pending_photographer-待摄影师确认 approved-已授权 rejected-已拒绝',
    reject_reason     VARCHAR(200)          DEFAULT NULL             COMMENT '拒绝原因',
    certificate_url   VARCHAR(255)          DEFAULT NULL             COMMENT '授权证书URL',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         BIGINT                DEFAULT NULL             COMMENT '创建人ID',
    update_by         BIGINT                DEFAULT NULL             COMMENT '修改人ID',
    version           INT          NOT NULL DEFAULT 1                COMMENT '乐观锁版本号',
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0                COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_biz_license_applicant_id (applicant_id),
    KEY idx_biz_license_photographer_id (photographer_id),
    KEY idx_biz_license_photo_id (photo_id),
    KEY idx_biz_license_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='授权申请表';

-- -----------------------------------------------------------
-- 8. t_biz_points_record 积分流水表
-- -----------------------------------------------------------
CREATE TABLE t_biz_points_record (
    id                  BIGINT       NOT NULL                        COMMENT '主键ID',
    user_id             BIGINT       NOT NULL                        COMMENT '用户ID',
    type                VARCHAR(10)  NOT NULL                        COMMENT '类型：earn-收入 spend-支出',
    amount              INT          NOT NULL                        COMMENT '变动积分数（正数）',
    balance             INT          NOT NULL                        COMMENT '变动后余额',
    remark              VARCHAR(200) NOT NULL DEFAULT ''             COMMENT '备注说明',
    related_order_id    BIGINT                DEFAULT NULL           COMMENT '关联订单ID',
    related_photo_id    BIGINT                DEFAULT NULL           COMMENT '关联作品ID',
    related_photo_title VARCHAR(50)           DEFAULT NULL           COMMENT '关联作品标题（冗余）',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT                DEFAULT NULL           COMMENT '创建人ID',
    update_by           BIGINT                DEFAULT NULL           COMMENT '修改人ID',
    version             INT          NOT NULL DEFAULT 1              COMMENT '乐观锁版本号',
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0              COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_biz_points_record_user_id (user_id),
    KEY idx_biz_points_record_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分流水表';

-- -----------------------------------------------------------
-- 9. t_sys_org 组织/部门表（树形结构）
-- -----------------------------------------------------------
CREATE TABLE t_sys_org (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL                                COMMENT '组织名称',
    parent_id   BIGINT                DEFAULT 0                      COMMENT '父组织ID，0表示顶级',
    sort        INT          NOT NULL DEFAULT 0                      COMMENT '排序值，升序',
    status      TINYINT(1)   NOT NULL DEFAULT 1                      COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_sys_org_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织/部门表';

-- -----------------------------------------------------------
-- 10. t_sys_role 角色表
-- -----------------------------------------------------------
CREATE TABLE t_sys_role (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL                                COMMENT '角色名称',
    code        VARCHAR(50)  NOT NULL                                COMMENT '角色编码，唯一标识',
    sort        INT          NOT NULL DEFAULT 0                      COMMENT '排序值，升序',
    status      TINYINT(1)   NOT NULL DEFAULT 1                      COMMENT '状态 0-禁用 1-启用',
    remark      VARCHAR(200) NOT NULL DEFAULT ''                     COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- -----------------------------------------------------------
-- 11. t_sys_menu 菜单表（目录/菜单/按钮三级）
-- -----------------------------------------------------------
CREATE TABLE t_sys_menu (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL                                COMMENT '菜单名称',
    parent_id   BIGINT                DEFAULT 0                      COMMENT '父菜单ID，0表示顶级',
    type        VARCHAR(10)  NOT NULL                                COMMENT '类型：dir-目录 menu-菜单 button-按钮',
    path        VARCHAR(200) NOT NULL DEFAULT ''                     COMMENT '路由路径',
    permission  VARCHAR(100) NOT NULL DEFAULT ''                     COMMENT '权限标识，如 photo:review',
    icon        VARCHAR(100) NOT NULL DEFAULT ''                     COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0                      COMMENT '排序值，升序',
    status      TINYINT(1)   NOT NULL DEFAULT 1                      COMMENT '状态 0-隐藏 1-显示',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_sys_menu_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- -----------------------------------------------------------
-- 12. t_sys_user_role 用户-角色关联表
-- -----------------------------------------------------------
CREATE TABLE t_sys_user_role (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    user_id     BIGINT       NOT NULL                                COMMENT '用户ID',
    role_id     BIGINT       NOT NULL                                COMMENT '角色ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户-角色关联表';

-- -----------------------------------------------------------
-- 13. t_sys_role_menu 角色-菜单关联表
-- -----------------------------------------------------------
CREATE TABLE t_sys_role_menu (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    role_id     BIGINT       NOT NULL                                COMMENT '角色ID',
    menu_id     BIGINT       NOT NULL                                COMMENT '菜单ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT                DEFAULT NULL                   COMMENT '创建人ID',
    update_by   BIGINT                DEFAULT NULL                   COMMENT '修改人ID',
    version     INT          NOT NULL DEFAULT 1                      COMMENT '乐观锁版本号',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0                      COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_sys_role_menu_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-菜单关联表';
