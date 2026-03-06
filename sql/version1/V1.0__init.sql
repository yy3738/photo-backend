-- ============================================================
-- 摄影作品平台 数据库初始化脚本
-- 版本：V1.0（final-state merged）
-- 数据库：MySQL 8.0+
-- 说明：最终态初始化脚本（不包含历史迁移 ALTER）
-- ============================================================

-- -----------------------------------------------------------
-- 1. t_sys_user 用户表（最终态：无 role 列）
-- -----------------------------------------------------------
CREATE TABLE t_sys_user (
    id          BIGINT       NOT NULL                                COMMENT '主键ID',
    openid      VARCHAR(64)  NOT NULL                                COMMENT '微信openid',
    nickname    VARCHAR(64)  NOT NULL DEFAULT ''                     COMMENT '微信昵称',
    avatar      VARCHAR(512) NOT NULL DEFAULT ''                     COMMENT '头像URL',
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
-- 4. t_biz_photo 作品表（最终态：preview_key）
-- -----------------------------------------------------------
CREATE TABLE t_biz_photo (
    id              BIGINT       NOT NULL                            COMMENT '主键ID',
    title           VARCHAR(50)  NOT NULL                            COMMENT '作品标题',
    description     VARCHAR(500) NOT NULL DEFAULT ''                 COMMENT '作品描述',
    preview_key     VARCHAR(255) NOT NULL                            COMMENT '预览图OSS Key',
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
-- 6. t_biz_order 订单表（最终态：photo_preview_key）
-- -----------------------------------------------------------
CREATE TABLE t_biz_order (
    id                  BIGINT       NOT NULL                        COMMENT '主键ID',
    user_id             BIGINT       NOT NULL                        COMMENT '买家用户ID',
    photo_id            BIGINT       NOT NULL                        COMMENT '作品ID',
    photographer_id     BIGINT       NOT NULL                        COMMENT '摄影师用户ID',
    photo_title         VARCHAR(50)  NOT NULL                        COMMENT '作品标题（快照）',
    photo_preview_key   VARCHAR(255) NOT NULL                        COMMENT '预览图OSS Key（快照）',
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
    KEY idx_biz_order_photographer_id (photographer_id),
    KEY idx_biz_order_photo_id (photo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- -----------------------------------------------------------
-- 7. t_biz_license 授权申请表（最终态：photo_preview_key）
-- -----------------------------------------------------------
CREATE TABLE t_biz_license (
    id                BIGINT       NOT NULL                          COMMENT '主键ID',
    photo_id          BIGINT       NOT NULL                          COMMENT '作品ID',
    photo_title       VARCHAR(50)  NOT NULL                          COMMENT '作品标题（冗余）',
    photo_preview_key VARCHAR(255) NOT NULL                          COMMENT '预览图OSS Key（冗余）',
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
    type                VARCHAR(10)  NOT NULL                        COMMENT '类型：recharge/purchase/earn/refund',
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

-- ============================================================
-- 种子数据（保留全部：RBAC + 测试用户 + 用户角色）
-- ============================================================

-- -----------------------------------------------------------
-- 1. 初始化角色
-- -----------------------------------------------------------
INSERT INTO t_sys_role (id, name, code, sort, status, remark) VALUES
(1, '管理员',  'admin',        1, 1, '系统管理员，拥有全部权限'),
(2, '摄影师',  'photographer', 2, 1, '摄影师，可管理自己的作品和审批授权'),
(3, '买家',    'buyer',        3, 1, '普通买家，可浏览、购买作品和申请授权');

-- -----------------------------------------------------------
-- 2. 初始化菜单（目录 + 菜单 + 按钮）
-- -----------------------------------------------------------

-- ---- 管理后台 ----
INSERT INTO t_sys_menu (id, name, parent_id, type, path, permission, icon, sort, status) VALUES
-- 顶级目录
(100, '系统管理',   0,   'dir',    '/admin',           '',                    'Setting',   1, 1),
(200, '业务管理',   0,   'dir',    '/admin/biz',       '',                    'Document',  2, 1),

-- 系统管理子菜单
(101, '用户管理',   100, 'menu',   '/admin/users',     'admin:user:list',     'User',      1, 1),
(102, '角色管理',   100, 'menu',   '/admin/roles',     'admin:role:list',     'Key',       2, 1),
(103, '菜单管理',   100, 'menu',   '/admin/menus',     'admin:menu:list',     'Menu',      3, 1),
(104, '组织管理',   100, 'menu',   '/admin/orgs',      'admin:org:list',      'Office',    4, 1),

-- 用户管理按钮
(1011, '封禁用户',   101, 'button', '', 'admin:user:ban',      '', 1, 1),
(1012, '解封用户',   101, 'button', '', 'admin:user:unban',    '', 2, 1),
(1013, '积分充值',   101, 'button', '', 'admin:user:recharge', '', 3, 1),
(1014, '分配角色',   101, 'button', '', 'admin:user:assign',   '', 4, 1),

-- 角色管理按钮
(1021, '新建角色',   102, 'button', '', 'admin:role:create',   '', 1, 1),
(1022, '编辑角色',   102, 'button', '', 'admin:role:update',   '', 2, 1),
(1023, '删除角色',   102, 'button', '', 'admin:role:delete',   '', 3, 1),

-- 菜单管理按钮
(1031, '新建菜单',   103, 'button', '', 'admin:menu:create',   '', 1, 1),
(1032, '编辑菜单',   103, 'button', '', 'admin:menu:update',   '', 2, 1),
(1033, '删除菜单',   103, 'button', '', 'admin:menu:delete',   '', 3, 1),

-- 组织管理按钮
(1041, '新建组织',   104, 'button', '', 'admin:org:create',    '', 1, 1),
(1042, '编辑组织',   104, 'button', '', 'admin:org:update',    '', 2, 1),
(1043, '删除组织',   104, 'button', '', 'admin:org:delete',    '', 3, 1),

-- 业务管理子菜单
(201, '仪表盘',     200, 'menu',   '/admin/dashboard',   'admin:dashboard',       'DataLine',  1, 1),
(202, '作品审核',   200, 'menu',   '/admin/photos',      'admin:photo:list',      'Picture',   2, 1),
(203, '授权审核',   200, 'menu',   '/admin/licenses',    'admin:license:list',    'Document',  3, 1),
(204, '分类管理',   200, 'menu',   '/admin/categories',  'admin:category:list',   'Folder',    4, 1),
(205, '标签管理',   200, 'menu',   '/admin/tags',        'admin:tag:list',        'PriceTag',  5, 1),

-- 作品审核按钮
(2021, '审核作品',   202, 'button', '', 'admin:photo:review',    '', 1, 1),

-- 授权审核按钮
(2031, '审核授权',   203, 'button', '', 'admin:license:review',  '', 1, 1),

-- 分类管理按钮
(2041, '新建分类',   204, 'button', '', 'admin:category:create', '', 1, 1),
(2042, '编辑分类',   204, 'button', '', 'admin:category:update', '', 2, 1),
(2043, '删除分类',   204, 'button', '', 'admin:category:delete', '', 3, 1),

-- 标签管理按钮
(2051, '新建标签',   205, 'button', '', 'admin:tag:create',      '', 1, 1),
(2052, '编辑标签',   205, 'button', '', 'admin:tag:update',      '', 2, 1),
(2053, '删除标签',   205, 'button', '', 'admin:tag:delete',      '', 3, 1),

-- ---- 摄影师工作台 ----
(300, '摄影师工作台', 0,   'dir',    '/studio',            '',                      'Camera',    3, 1),
(301, '我的作品',     300, 'menu',   '/studio/photos',     'studio:photo:list',     'Picture',   1, 1),
(302, '销售统计',     300, 'menu',   '/studio/stats',      'studio:stats',          'DataLine',  2, 1),
(303, '收入明细',     300, 'menu',   '/studio/earnings',   'studio:earnings',       'Money',     3, 1),
(304, '授权审批',     300, 'menu',   '/studio/licenses',   'studio:license:list',   'Document',  4, 1),

-- 摄影师按钮
(3011, '上传作品',   301, 'button', '', 'studio:photo:create',   '', 1, 1),
(3012, '编辑作品',   301, 'button', '', 'studio:photo:update',   '', 2, 1),
(3013, '删除作品',   301, 'button', '', 'studio:photo:delete',   '', 3, 1),
(3014, '上下架',     301, 'button', '', 'studio:photo:status',   '', 4, 1),
(3041, '审批授权',   304, 'button', '', 'studio:license:review', '', 1, 1);

-- -----------------------------------------------------------
-- 3. 角色-菜单关联
-- -----------------------------------------------------------

-- admin 角色 → 全部菜单
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
(1,  1, 100), (2,  1, 101), (3,  1, 102), (4,  1, 103), (5,  1, 104),
(6,  1, 1011),(7,  1, 1012),(8,  1, 1013),(9,  1, 1014),
(10, 1, 1021),(11, 1, 1022),(12, 1, 1023),
(13, 1, 1031),(14, 1, 1032),(15, 1, 1033),
(16, 1, 1041),(17, 1, 1042),(18, 1, 1043),
(19, 1, 200), (20, 1, 201), (21, 1, 202), (22, 1, 203), (23, 1, 204), (24, 1, 205),
(25, 1, 2021),(26, 1, 2031),
(27, 1, 2041),(28, 1, 2042),(29, 1, 2043),
(30, 1, 2051),(31, 1, 2052),(32, 1, 2053),
(33, 1, 300), (34, 1, 301), (35, 1, 302), (36, 1, 303), (37, 1, 304),
(38, 1, 3011),(39, 1, 3012),(40, 1, 3013),(41, 1, 3014),(42, 1, 3041);

-- photographer 角色 → 工作台菜单
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
(50, 2, 300), (51, 2, 301), (52, 2, 302), (53, 2, 303), (54, 2, 304),
(55, 2, 3011),(56, 2, 3012),(57, 2, 3013),(58, 2, 3014),(59, 2, 3041);

-- -----------------------------------------------------------
-- 4. 初始化测试用户（最终态：无 role 字段）
-- -----------------------------------------------------------
INSERT INTO t_sys_user (id, openid, nickname, avatar, points, is_banned, org_id) VALUES
(1, 'oTest_admin_001',        '管理员小张', '', 0,    0, NULL),
(2, 'oTest_photographer_001', '摄影师老李', '', 500,  0, NULL),
(3, 'oTest_buyer_001',        '买家小王',   '', 1000, 0, NULL);

-- -----------------------------------------------------------
-- 5. 用户-角色关联
-- -----------------------------------------------------------
INSERT INTO t_sys_user_role (id, user_id, role_id) VALUES
(1, 1, 1),   -- 管理员小张 → admin
(2, 2, 2),   -- 摄影师老李 → photographer
(3, 3, 3);   -- 买家小王   → buyer

-- ============================================================
-- 示例业务数据（原 example.sql 合并）
-- ============================================================

-- -----------------------------------------------------------
-- 1. 组织
-- -----------------------------------------------------------
INSERT INTO t_sys_org (id, name, parent_id, sort, status) VALUES
(1001, '平台运营部', 0, 1, 1),
(1002, '内容审核组', 1001, 1, 1),
(1003, '摄影师联盟', 0, 2, 1);

-- -----------------------------------------------------------
-- 7. 分类
-- -----------------------------------------------------------
INSERT INTO t_sys_category (id, name, sort, photo_count) VALUES
(6001, '风光',   1, 4),
(6002, '人像',   2, 3),
(6003, '纪实',   3, 2),
(6004, '建筑',   4, 1),
(6005, '美食',   5, 0);

-- -----------------------------------------------------------
-- 8. 标签
-- -----------------------------------------------------------
INSERT INTO t_sys_tag (id, name, category_id, sort) VALUES
-- 风光标签
(7001, '日出日落', 6001, 1),
(7002, '山川',     6001, 2),
(7003, '海洋',     6001, 3),
(7004, '星空',     6001, 4),
-- 人像标签
(7005, '写真',     6002, 1),
(7006, '婚纱',     6002, 2),
(7007, '街拍',     6002, 3),
-- 纪实标签
(7008, '城市',     6003, 1),
(7009, '乡村',     6003, 2),
(7010, '人文',     6003, 3),
-- 建筑标签
(7011, '现代建筑', 6004, 1),
(7012, '古建筑',   6004, 2),
-- 美食标签
(7013, '中餐',     6005, 1),
(7014, '西餐',     6005, 2);

-- -----------------------------------------------------------
-- 9. 作品（10 张，覆盖各状态）
-- -----------------------------------------------------------
INSERT INTO t_biz_photo (id, title, description, preview_key, original_key, category_id, category_name, user_id, price, allow_license, status, reject_reason, purchase_count) VALUES
-- 张伟的风光作品
(8001, '金色日出',       '清晨五点攀登黄山拍摄的日出全景',                'https://oss.example.com/preview/8001.jpg', 'original/8001.jpg', 6001, '风光', 10002, 200,  1, 'approved', NULL, 3),
(8002, '星河璀璨',       '西藏阿里暗夜保护区银河拱桥',                    'https://oss.example.com/preview/8002.jpg', 'original/8002.jpg', 6001, '风光', 10002, 500,  1, 'approved', NULL, 1),
(8003, '雪山倒影',       '四姑娘山双桥沟海子倒影',                        'https://oss.example.com/preview/8003.jpg', 'original/8003.jpg', 6001, '风光', 10002, 300,  1, 'approved', NULL, 0),
(8004, '海上日落',       '三亚湾落日余晖',                                'https://oss.example.com/preview/8004.jpg', 'original/8004.jpg', 6001, '风光', 10002, 150,  0, 'pending',  NULL, 0),
-- 李娜的人像作品
(8005, '古镇写真',       '乌镇水乡旗袍人像',                              'https://oss.example.com/preview/8005.jpg', 'original/8005.jpg', 6002, '人像', 10003, 350,  1, 'approved', NULL, 2),
(8006, '城市街拍',       '上海外滩夜景街拍',                              'https://oss.example.com/preview/8006.jpg', 'original/8006.jpg', 6002, '人像', 10003, 250,  1, 'approved', NULL, 1),
(8007, '花海婚纱',       '云南罗平油菜花田婚纱照',                        'https://oss.example.com/preview/8007.jpg', 'original/8007.jpg', 6002, '人像', 10003, 800,  1, 'rejected', '图片水印未去除，请重新上传', 0),
-- 王磊的纪实/建筑作品
(8008, '老街时光',       '重庆十八梯老街改造前的最后影像',                  'https://oss.example.com/preview/8008.jpg', 'original/8008.jpg', 6003, '纪实', 10004, 180,  1, 'approved', NULL, 1),
(8009, '菜市场的清晨',   '成都菜市场凌晨四点的忙碌景象',                    'https://oss.example.com/preview/8009.jpg', 'original/8009.jpg', 6003, '纪实', 10004, 120,  1, 'approved', NULL, 0),
(8010, '未来之城',       '深圳前海自贸区现代建筑群',                        'https://oss.example.com/preview/8010.jpg', 'original/8010.jpg', 6004, '建筑', 10004, 280,  1, 'offline',  NULL, 2);

-- -----------------------------------------------------------
-- 10. 作品-标签关联
-- -----------------------------------------------------------
INSERT INTO t_biz_photo_tag (id, photo_id, tag_id) VALUES
(9001, 8001, 7001), -- 金色日出 → 日出日落
(9002, 8001, 7002), -- 金色日出 → 山川
(9003, 8002, 7004), -- 星河璀璨 → 星空
(9004, 8003, 7002), -- 雪山倒影 → 山川
(9005, 8004, 7001), -- 海上日落 → 日出日落
(9006, 8004, 7003), -- 海上日落 → 海洋
(9007, 8005, 7005), -- 古镇写真 → 写真
(9008, 8006, 7007), -- 城市街拍 → 街拍
(9009, 8007, 7006), -- 花海婚纱 → 婚纱
(9010, 8008, 7008), -- 老街时光 → 城市
(9011, 8008, 7010), -- 老街时光 → 人文
(9012, 8009, 7010), -- 菜市场的清晨 → 人文
(9013, 8009, 7009), -- 菜市场的清晨 → 乡村
(9014, 8010, 7011); -- 未来之城 → 现代建筑

-- -----------------------------------------------------------
-- 11. 订单（8 笔，覆盖不同买家购买不同摄影师作品）
-- 积分规则：平台抽佣 10%，摄影师得 90%
-- -----------------------------------------------------------
INSERT INTO t_biz_order (id, user_id, photo_id, photographer_id, photo_title, photo_preview_key, price, platform_fee, photographer_earned) VALUES
-- 小陈买了 3 张
(11001, 10005, 8001, 10002, '金色日出',   'https://oss.example.com/preview/8001.jpg', 200, 20,  180),
(11002, 10005, 8005, 10003, '古镇写真',   'https://oss.example.com/preview/8005.jpg', 350, 35,  315),
(11003, 10005, 8010, 10004, '未来之城',   'https://oss.example.com/preview/8010.jpg', 280, 28,  252),
-- 小刘买了 3 张
(11004, 10006, 8001, 10002, '金色日出',   'https://oss.example.com/preview/8001.jpg', 200, 20,  180),
(11005, 10006, 8006, 10003, '城市街拍',   'https://oss.example.com/preview/8006.jpg', 250, 25,  225),
(11006, 10006, 8008, 10004, '老街时光',   'https://oss.example.com/preview/8008.jpg', 180, 18,  162),
-- 老赵买了 2 张
(11007, 10007, 8001, 10002, '金色日出',   'https://oss.example.com/preview/8001.jpg', 200, 20,  180),
(11008, 10007, 8005, 10003, '古镇写真',   'https://oss.example.com/preview/8005.jpg', 350, 35,  315),
-- 小林买了 1 张（被封禁前购买的）
(11009, 10008, 8002, 10002, '星河璀璨',   'https://oss.example.com/preview/8002.jpg', 500, 50,  450),
-- 小陈又买了 1 张
(11010, 10005, 8010, 10004, '未来之城',   'https://oss.example.com/preview/8010.jpg', 280, 28,  252);

-- -----------------------------------------------------------
-- 12. 授权申请（覆盖四种状态）
-- -----------------------------------------------------------
INSERT INTO t_biz_license (id, photo_id, photo_title, photo_preview_key, applicant_id, photographer_id, purpose, scene, duration, contact, status, reject_reason, certificate_url) VALUES
-- 已授权：小陈申请金色日出商业授权
(12001, 8001, '金色日出', 'https://oss.example.com/preview/8001.jpg', 10005, 10002,
 'commercial', '用于旅游宣传海报设计', '2025年全年', '13800001111',
 'approved', NULL, 'https://oss.example.com/cert/12001.pdf'),
-- 待摄影师确认：小刘申请城市街拍新闻授权
(12002, 8006, '城市街拍', 'https://oss.example.com/preview/8006.jpg', 10006, 10003,
 'news', '新闻杂志配图使用', '2025年6月-12月', '13800002222',
 'pending_photographer', NULL, NULL),
-- 待管理员审核：老赵申请古镇写真个人授权
(12003, 8005, '古镇写真', 'https://oss.example.com/preview/8005.jpg', 10007, 10003,
 'personal', '个人公众号文章配图', '长期', '13800003333',
 'pending_admin', NULL, NULL),
-- 已拒绝：小陈申请老街时光教育授权（摄影师拒绝）
(12004, 8008, '老街时光', 'https://oss.example.com/preview/8008.jpg', 10005, 10004,
 'education', '大学摄影教材案例', '5年', '13800001111',
 'rejected', '该作品涉及居民隐私，不适合公开出版使用', NULL);

-- -----------------------------------------------------------
-- 13. 积分流水（与订单对应 + 管理员充值记录）
-- -----------------------------------------------------------
INSERT INTO t_biz_points_record (id, user_id, type, amount, balance, remark, related_order_id, related_photo_id, related_photo_title) VALUES
-- 管理员给买家充值
(13001, 10005, 'earn',  10000, 10000, '管理员充值',                    NULL,  NULL, NULL),
(13002, 10006, 'earn',  5000,  5000,  '管理员充值',                    NULL,  NULL, NULL),
(13003, 10007, 'earn',  3000,  3000,  '管理员充值',                    NULL,  NULL, NULL),
(13004, 10008, 'earn',  1000,  1000,  '管理员充值',                    NULL,  NULL, NULL),

-- 小陈购买支出
(13005, 10005, 'spend', 200,   9800,  '购买作品《金色日出》',           11001, 8001, '金色日出'),
(13006, 10005, 'spend', 350,   9450,  '购买作品《古镇写真》',           11002, 8005, '古镇写真'),
(13007, 10005, 'spend', 280,   9170,  '购买作品《未来之城》',           11003, 8010, '未来之城'),
-- 小刘购买支出
(13008, 10006, 'spend', 200,   4800,  '购买作品《金色日出》',           11004, 8001, '金色日出'),
(13009, 10006, 'spend', 250,   4550,  '购买作品《城市街拍》',           11005, 8006, '城市街拍'),
(13010, 10006, 'spend', 180,   4370,  '购买作品《老街时光》',           11006, 8008, '老街时光'),
-- 老赵购买支出
(13011, 10007, 'spend', 200,   2800,  '购买作品《金色日出》',           11007, 8001, '金色日出'),
(13012, 10007, 'spend', 350,   2450,  '购买作品《古镇写真》',           11008, 8005, '古镇写真'),
-- 小林购买支出
(13013, 10008, 'spend', 500,   500,   '购买作品《星河璀璨》',           11009, 8002, '星河璀璨'),

-- 摄影师收入（90%）
(13014, 10002, 'earn',  180,   180,   '作品《金色日出》被购买',         11001, 8001, '金色日出'),
(13015, 10003, 'earn',  315,   315,   '作品《古镇写真》被购买',         11002, 8005, '古镇写真'),
(13016, 10004, 'earn',  252,   252,   '作品《未来之城》被购买',         11003, 8010, '未来之城'),
(13017, 10002, 'earn',  180,   360,   '作品《金色日出》被购买',         11004, 8001, '金色日出'),
(13018, 10003, 'earn',  225,   540,   '作品《城市街拍》被购买',         11005, 8006, '城市街拍'),
(13019, 10004, 'earn',  162,   414,   '作品《老街时光》被购买',         11006, 8008, '老街时光'),
(13020, 10002, 'earn',  180,   540,   '作品《金色日出》被购买',         11007, 8001, '金色日出'),
(13021, 10003, 'earn',  315,   855,   '作品《古镇写真》被购买',         11008, 8005, '古镇写真'),
(13022, 10002, 'earn',  450,   990,   '作品《星河璀璨》被购买',         11009, 8002, '星河璀璨');
