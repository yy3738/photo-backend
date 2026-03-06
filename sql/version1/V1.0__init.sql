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
