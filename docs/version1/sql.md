# 摄影作品平台 - 数据库设计文档

版本：v0.1 | 数据库：MySQL 8.0+

---

## 一、设计原则

- 基本符合第三范式（3NF），适当冗余以优化查询（已注释说明）
- 不使用外键约束和触发器，关联关系由应用层维护
- 业务表以 `t_biz_` 开头，系统表以 `t_sys_` 开头
- 主键统一 `BIGINT`，MyBatis-Plus 雪花算法生成
- 逻辑删除，不做物理删除
- 所有表包含公共字段：`id`、`create_time`、`update_time`、`create_by`、`update_by`、`version`、`is_deleted`

---

## 二、公共字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，雪花算法生成 |
| create_time | DATETIME | 创建时间，默认 CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间，自动更新 |
| create_by | BIGINT | 创建人 ID |
| update_by | BIGINT | 修改人 ID |
| version | INT | 乐观锁版本号，默认 1 |
| is_deleted | TINYINT(1) | 逻辑删除，0-未删除 1-已删除 |

---

## 三、表清单

| 序号 | 表名 | 类型 | 说明 |
|------|------|------|------|
| 1 | t_sys_user | 系统表 | 用户表 |
| 2 | t_sys_category | 系统表 | 分类表 |
| 3 | t_sys_tag | 系统表 | 标签表 |
| 4 | t_biz_photo | 业务表 | 作品表 |
| 5 | t_biz_photo_tag | 业务表 | 作品-标签关联表 |
| 6 | t_biz_order | 业务表 | 订单表 |
| 7 | t_biz_license | 业务表 | 授权申请表 |
| 8 | t_biz_points_record | 业务表 | 积分流水表 |
| 9 | t_sys_org | 系统表 | 组织/部门表（树形结构） |
| 10 | t_sys_role | 系统表 | 角色表 |
| 11 | t_sys_menu | 系统表 | 菜单表（目录/菜单/按钮三级） |
| 12 | t_sys_user_role | 系统表 | 用户-角色关联表 |
| 13 | t_sys_role_menu | 系统表 | 角色-菜单关联表 |

---

## 四、表结构详细设计

### 4.1 t_sys_user（用户表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| openid | VARCHAR(64) | Y | - | 微信 openid，唯一 |
| nickname | VARCHAR(64) | N | '' | 微信昵称 |
| avatar | VARCHAR(512) | N | '' | 头像 URL |
| role | VARCHAR(20) | Y | 'buyer' | 角色：buyer/photographer/admin |
| points | INT | Y | 0 | 积分余额 |
| is_banned | TINYINT(1) | Y | 0 | 是否封禁 0-正常 1-封禁 |
| org_id | BIGINT | N | NULL | 所属组织ID |

索引：
- `uk_sys_user_openid (openid)` — 唯一索引

### 4.2 t_sys_category（分类表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| name | VARCHAR(50) | Y | - | 分类名称 |
| sort | INT | Y | 0 | 排序值，升序 |
| photo_count | INT | Y | 0 | 作品数量（冗余，定期同步） |

### 4.3 t_sys_tag（标签表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| name | VARCHAR(50) | Y | - | 标签名称 |
| category_id | BIGINT | Y | - | 所属分类 ID |
| sort | INT | Y | 0 | 排序值，升序 |

索引：
- `idx_sys_tag_category_id (category_id)`

### 4.4 t_biz_photo（作品表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| title | VARCHAR(50) | Y | - | 作品标题 |
| description | VARCHAR(500) | N | '' | 作品描述 |
| preview_key | VARCHAR(255) | Y | - | 预览图 OSS Key |
| original_key | VARCHAR(255) | Y | - | 原图 OSS Key |
| category_id | BIGINT | Y | - | 分类 ID |
| category_name | VARCHAR(50) | Y | - | 分类名称（冗余，避免列表查询 JOIN） |
| photographer_id | BIGINT | Y | - | 摄影师用户 ID |
| price | INT | Y | - | 积分价格，范围 10~9999 |
| allow_license | TINYINT(1) | Y | 1 | 是否允许授权申请 |
| status | VARCHAR(20) | Y | 'pending' | 状态：pending/approved/rejected/offline |
| reject_reason | VARCHAR(200) | N | NULL | 审核拒绝原因 |
| purchase_count | INT | Y | 0 | 购买次数（冗余，下单时 +1） |

索引：
- `idx_biz_photo_photographer_id (photographer_id)`
- `idx_biz_photo_category_id (category_id)`
- `idx_biz_photo_status_create (status, create_time)` — 公开列表查询

### 4.5 t_biz_photo_tag（作品-标签关联表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| photo_id | BIGINT | Y | - | 作品 ID |
| tag_id | BIGINT | Y | - | 标签 ID |

索引：
- `uk_biz_photo_tag (photo_id, tag_id)` — 唯一索引，防重复关联
- `idx_biz_photo_tag_tag_id (tag_id)` — 按标签查作品

### 4.6 t_biz_order（订单表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| buyer_id | BIGINT | Y | - | 买家用户 ID |
| photo_id | BIGINT | Y | - | 作品 ID |
| photographer_id | BIGINT | Y | - | 摄影师用户 ID |
| photo_title | VARCHAR(50) | Y | - | 作品标题（快照冗余） |
| photo_preview_key | VARCHAR(255) | Y | - | 预览图 Key（快照冗余） |
| price | INT | Y | - | 成交积分价格（快照） |
| platform_fee | INT | Y | - | 平台抽佣积分（10%） |
| photographer_earned | INT | Y | - | 摄影师实得积分（90%） |

索引：
- `uk_biz_order_buyer_photo (buyer_id, photo_id)` — 唯一索引，防重复购买
- `idx_biz_order_photographer_id (photographer_id)`
- `idx_biz_order_photo_id (photo_id)`

### 4.7 t_biz_license（授权申请表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| photo_id | BIGINT | Y | - | 作品 ID |
| photo_title | VARCHAR(50) | Y | - | 作品标题（冗余） |
| photo_preview_key | VARCHAR(255) | Y | - | 预览图 Key（冗余） |
| applicant_id | BIGINT | Y | - | 申请人用户 ID |
| photographer_id | BIGINT | Y | - | 摄影师用户 ID |
| purpose | VARCHAR(20) | Y | - | 用途：commercial/news/personal/education/other |
| scene | VARCHAR(200) | Y | - | 使用场景描述 |
| duration | VARCHAR(100) | Y | - | 使用期限（自由填写） |
| contact | VARCHAR(50) | Y | - | 联系方式 |
| status | VARCHAR(30) | Y | 'pending_admin' | 状态：pending_admin/pending_photographer/approved/rejected |
| reject_reason | VARCHAR(200) | N | NULL | 拒绝原因 |
| certificate_key | VARCHAR(255) | N | NULL | 授权证书 OSS Key |

索引：
- `idx_biz_license_applicant_id (applicant_id)`
- `idx_biz_license_photographer_id (photographer_id)`
- `idx_biz_license_photo_id (photo_id)`
- `idx_biz_license_status (status)`

### 4.8 t_biz_points_record（积分流水表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| user_id | BIGINT | Y | - | 用户 ID |
| type | VARCHAR(10) | Y | - | 类型：earn/spend |
| amount | INT | Y | - | 变动积分数（正数） |
| balance | INT | Y | - | 变动后余额 |
| remark | VARCHAR(200) | N | '' | 备注说明 |
| related_order_id | BIGINT | N | NULL | 关联订单 ID |
| related_photo_id | BIGINT | N | NULL | 关联作品 ID |
| related_photo_title | VARCHAR(50) | N | NULL | 关联作品标题（冗余） |

索引：
- `idx_biz_points_record_user_id (user_id)`
- `idx_biz_points_record_user_type (user_id, type)` — 按类型查流水

### 4.9 t_sys_org（组织/部门表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| name | VARCHAR(100) | Y | - | 组织名称 |
| parent_id | BIGINT | N | 0 | 父组织ID，0表示顶级 |
| sort | INT | Y | 0 | 排序值，升序 |
| status | TINYINT(1) | Y | 1 | 状态 0-禁用 1-启用 |

索引：
- `idx_sys_org_parent_id (parent_id)`

### 4.10 t_sys_role（角色表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| name | VARCHAR(50) | Y | - | 角色名称 |
| code | VARCHAR(50) | Y | - | 角色编码，唯一标识 |
| sort | INT | Y | 0 | 排序值，升序 |
| status | TINYINT(1) | Y | 1 | 状态 0-禁用 1-启用 |
| remark | VARCHAR(200) | N | '' | 备注 |

索引：
- `uk_sys_role_code (code)` — 唯一索引

### 4.11 t_sys_menu（菜单表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| name | VARCHAR(50) | Y | - | 菜单名称 |
| parent_id | BIGINT | N | 0 | 父菜单ID，0表示顶级 |
| type | VARCHAR(10) | Y | - | 类型：dir-目录 menu-菜单 button-按钮 |
| path | VARCHAR(200) | N | '' | 路由路径 |
| permission | VARCHAR(100) | N | '' | 权限标识，如 photo:review |
| icon | VARCHAR(100) | N | '' | 图标 |
| sort | INT | Y | 0 | 排序值，升序 |
| status | TINYINT(1) | Y | 1 | 状态 0-隐藏 1-显示 |

索引：
- `idx_sys_menu_parent_id (parent_id)`

### 4.12 t_sys_user_role（用户-角色关联表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| user_id | BIGINT | Y | - | 用户ID |
| role_id | BIGINT | Y | - | 角色ID |

索引：
- `uk_sys_user_role (user_id, role_id)` — 唯一索引，防重复分配
- `idx_sys_user_role_role_id (role_id)` — 按角色查用户

### 4.13 t_sys_role_menu（角色-菜单关联表）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | Y | - | 主键 |
| role_id | BIGINT | Y | - | 角色ID |
| menu_id | BIGINT | Y | - | 菜单ID |

索引：
- `uk_sys_role_menu (role_id, menu_id)` — 唯一索引，防重复分配
- `idx_sys_role_menu_menu_id (menu_id)` — 按菜单查角色

---

## 五、ER 关系说明（应用层维护，无外键）

```
t_sys_user 1 ──── N t_biz_photo          (photographer_id → user.id)
t_sys_user 1 ──── N t_biz_order           (buyer_id → user.id)
t_sys_user 1 ──── N t_biz_license         (applicant_id → user.id)
t_sys_user 1 ──── N t_biz_points_record   (user_id → user.id)
t_sys_user N ──── 1 t_sys_org             (org_id → org.id)
t_sys_user N ──── N t_sys_role            (通过 t_sys_user_role 关联)

t_sys_org  1 ──── N t_sys_org             (parent_id → org.id，树形自关联)

t_sys_role N ──── N t_sys_menu            (通过 t_sys_role_menu 关联)

t_sys_menu 1 ──── N t_sys_menu            (parent_id → menu.id，树形自关联)

t_sys_category 1 ── N t_sys_tag            (category_id → category.id)
t_sys_category 1 ── N t_biz_photo          (category_id → category.id)

t_biz_photo N ──── N t_sys_tag             (通过 t_biz_photo_tag 关联)
t_biz_photo 1 ──── N t_biz_order           (photo_id → photo.id)
t_biz_photo 1 ──── N t_biz_license         (photo_id → photo.id)
```

---

## 六、冗余字段说明

| 表 | 冗余字段 | 来源 | 原因 |
|----|----------|------|------|
| t_sys_category | photo_count | COUNT(t_biz_photo) | 分类列表高频查询，避免实时 COUNT |
| t_biz_photo | category_name | t_sys_category.name | 作品列表避免 JOIN 分类表 |
| t_biz_photo | purchase_count | COUNT(t_biz_order) | 详情页展示，避免实时 COUNT |
| t_biz_order | photo_title, photo_preview_key | t_biz_photo | 订单快照，作品修改不影响历史订单 |
| t_biz_license | photo_title, photo_preview_key | t_biz_photo | 同上 |
| t_biz_points_record | related_photo_title | t_biz_photo | 流水记录快照 |
