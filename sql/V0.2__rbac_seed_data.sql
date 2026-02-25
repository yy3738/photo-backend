-- ============================================================
-- RBAC 初始化种子数据
-- 版本：V0.2
-- 说明：初始化角色、菜单、角色-菜单关联；
--       为已有用户按 role 字段补写 user_role 关联
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
-- 3. 角色-菜单关联（admin 拥有全部，photographer 拥有工作台）
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

-- buyer 角色 → 无后台菜单（仅前台浏览/购买/申请授权，不需要菜单权限）