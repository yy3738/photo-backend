# 摄影作品平台 - 后端接口设计文档

> 基于 PRD v0.3 + CLAUDE.md 整理，供前后端对接使用。

---

## 一、通用约定

### 1.1 Base URL

| 环境 | 地址 |
|------|------|
| 开发 | `http://localhost:3000` |
| 生产 | 由 `VITE_API_BASE_URL` 环境变量注入 |

### 1.2 认证方式

所有需要登录的接口在请求头携带：

```
Authorization: Bearer {accessToken}
```

无需登录的接口（登录、刷新 token、公开作品列表/详情）可不携带。

### 1.3 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | number | 0 = 成功，非 0 = 业务错误 |
| message | string | 错误描述，成功时为 "success" |
| data | any | 业务数据，失败时可为 null |

### 1.4 HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 / token 失效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如积分不足、重复购买） |
| 429 | 请求频率超限 |
| 500 | 服务器内部错误 |

### 1.5 分页参数（列表接口通用）

**请求 Query：**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 1 | 页码，从 1 开始 |
| pageSize | number | 20 | 每页条数，最大 50 |

**响应 data 结构：**

```json
{
  "list": [],
  "total": 100,
  "page": 1,
  "pageSize": 20
}
```

### 1.6 时间格式

统一使用 ISO 8601：`2026-02-21T10:00:00.000Z`

---

## 二、数据模型

### User（用户）

```json
{
  "id": "string",
  "openid": "string",
  "nickname": "string",
  "avatar": "string",
  "role": "buyer | photographer | admin",
  "points": 0,
  "isBanned": false,
  "createdAt": "ISO8601"
}
```

### Photo（作品）

```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "previewUrl": "string",
  "categoryId": "string",
  "categoryName": "string",
  "tags": [{ "id": "string", "name": "string" }],
  "price": 100,
  "allowLicense": true,
  "status": "pending | approved | rejected | offline",
  "rejectReason": "string | null",
  "photographer": {
    "id": "string",
    "nickname": "string",
    "avatar": "string"
  },
  "purchaseCount": 0,
  "createdAt": "ISO8601",
  "updatedAt": "ISO8601"
}
```

### Order（订单）

```json
{
  "id": "string",
  "photoId": "string",
  "photoTitle": "string",
  "photoPreviewUrl": "string",
  "price": 100,
  "createdAt": "ISO8601"
}
```

### License（授权申请）

```json
{
  "id": "string",
  "photoId": "string",
  "photoTitle": "string",
  "photoPreviewUrl": "string",
  "applicantId": "string",
  "purpose": "commercial | news | personal | education | other",
  "scene": "string",
  "duration": "string",
  "contact": "string",
  "status": "pending_admin | pending_photographer | approved | rejected",
  "rejectReason": "string | null",
  "certificateUrl": "string | null",
  "createdAt": "ISO8601",
  "updatedAt": "ISO8601"
}
```

### Category（分类）

```json
{
  "id": "string",
  "name": "string",
  "sort": 0,
  "photoCount": 0
}
```

### Tag（标签）

```json
{
  "id": "string",
  "name": "string",
  "categoryId": "string",
  "sort": 0
}
```

### PointsRecord（积分流水）

```json
{
  "id": "string",
  "type": "earn | spend",
  "amount": 90,
  "balance": 990,
  "remark": "string",
  "relatedPhotoId": "string | null",
  "relatedPhotoTitle": "string | null",
  "createdAt": "ISO8601"
}
```

### Org（组织/部门）

```json
{
  "id": "string",
  "name": "string",
  "parentId": "string",
  "sort": 0,
  "status": 1,
  "children": []
}
```

### Role（角色）

```json
{
  "id": "string",
  "name": "string",
  "code": "string",
  "sort": 0,
  "status": 1,
  "remark": "string",
  "menuIds": ["string"]
}
```

### Menu（菜单/权限）

```json
{
  "id": "string",
  "name": "string",
  "parentId": "string",
  "type": "dir | menu | button",
  "path": "string",
  "permission": "string",
  "icon": "string",
  "sort": 0,
  "status": 1,
  "children": []
}
```

---

## 三、接口清单

---

### 3.1 认证模块 `/auth`

#### POST /auth/wx-login

微信登录，用 code 换取 token 和用户信息。

**请求体：**

```json
{
  "code": "wx_login_code"
}
```

**响应 data：**

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": {
    "id": "string",
    "nickname": "string",
    "avatar": "string",
    "role": "buyer",
    "points": 0
  }
}
```

**说明：**
- 首次登录自动注册，角色默认为 `buyer`
- 同步微信昵称和头像

---

#### POST /auth/refresh

刷新 accessToken。

**请求体：**

```json
{
  "refreshToken": "eyJ..."
}
```

**响应 data：**

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

**说明：**
- refreshToken 有效期 7 天，accessToken 有效期 2 小时
- 刷新后同时返回新的 refreshToken（滚动刷新）
- refreshToken 失效返回 401，前端跳转登录页

---

### 3.2 分类与标签 `/categories`

#### GET /categories

获取所有分类列表（公开，无需登录）。

**响应 data：**

```json
[
  { "id": "1", "name": "风景", "sort": 1, "photoCount": 120 }
]
```

---

#### GET /categories/:id/tags

获取指定分类下的标签列表（公开，无需登录）。

**响应 data：**

```json
[
  { "id": "10", "name": "山川", "categoryId": "1", "sort": 1 }
]
```

---

### 3.3 作品公开接口 `/photos`

#### GET /photos

获取作品列表（瀑布流，公开）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | string | 否 | 分类 ID，不传则全部 |
| tagId | string | 否 | 标签 ID |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 Photo 数组（仅返回 `approved` 状态作品，`previewUrl` 为带水印预览图）

---

#### GET /photos/search

搜索作品（公开）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 关键词，匹配标题/标签 |
| categoryId | string | 否 | 分类筛选 |
| tagId | string | 否 | 标签筛选 |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 同 GET /photos

---

#### GET /photos/:id

获取作品详情（公开）。

**响应 data：** Photo 对象，额外包含：

```json
{
  "isPurchased": false
}
```

`isPurchased` 需登录后才有意义，未登录时返回 `false`。

---

#### POST /photos/:id/buy

积分购买作品（需登录）。

**请求体：** 无

**响应 data：**

```json
{
  "orderId": "string",
  "pointsSpent": 100,
  "remainingPoints": 900
}
```

**业务规则：**
- 积分不足返回 409，`message` 说明原因
- 已购买过返回 409
- 积分扣减和摄影师积分增加必须原子操作
- 摄影师实得 90%，平台抽佣 10%

---

#### GET /photos/:id/download

获取原图临时下载链接（需登录且已购买，或为作品摄影师本人，或为管理员）。

**响应 data：**

```json
{
  "url": "https://oss.xxx.com/photos/xxx.jpg?Expires=...&Signature=...",
  "expireAt": "ISO8601"
}
```

**说明：** URL 有效期 5 分钟，前端直接调用微信 API 保存到相册。

---

### 3.4 OSS 上传签名 `/upload`

#### GET /upload/signature

获取 OSS 直传签名（需登录，摄影师/管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 是 | 原始文件名 |
| type | string | 是 | `preview` 或 `original` |

**响应 data：**

```json
{
  "host": "https://bucket.oss-cn-hangzhou.aliyuncs.com",
  "key": "photos/2026/02/uuid_original.jpg",
  "policy": "base64_encoded_policy",
  "accessId": "OSS_ACCESS_KEY_ID",
  "signature": "签名字符串",
  "expireAt": "ISO8601"
}
```

**说明：**
- 前端用此签名直接 POST 到 OSS，`success_action_status` 设为 `200`
- 上传成功后前端将 `key` 传给后端创建作品接口
- 限流：每用户每分钟 10 次

---

### 3.5 用户中心 `/user`

#### GET /user/me

获取当前登录用户信息（需登录）。

**响应 data：** User 对象

---

#### POST /user/enable-photographer

开启摄影师模式（需登录，仅 buyer 可调用）。

**请求体：** 无

**响应 data：**

```json
{
  "role": "photographer"
}
```

---

#### GET /user/points-history

获取积分明细（需登录）。

**Query 参数：** 分页参数

**响应 data：** 分页结构，`list` 为 PointsRecord 数组，按时间倒序

---

#### GET /user/orders

获取我的订单（需登录）。

**Query 参数：** 分页参数

**响应 data：** 分页结构，`list` 为 Order 数组，按时间倒序

---

#### GET /user/licenses

获取我的授权申请（需登录）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 筛选状态 |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 License 数组

---

### 3.6 授权申请 `/licenses`

#### POST /licenses

提交授权申请（需登录）。

**请求体：**

```json
{
  "photoId": "string",
  "purpose": "commercial | news | personal | education | other",
  "scene": "使用场景描述，200字内",
  "duration": "使用期限，自由填写",
  "contact": "13800138000"
}
```

**响应 data：**

```json
{
  "id": "string",
  "status": "pending_admin"
}
```

---

#### GET /licenses/:id

获取授权申请详情（需登录，仅申请人/相关摄影师/管理员可访问）。

**响应 data：** License 对象（含关联作品信息）

---

#### GET /licenses/:id/certificate

获取授权证书下载链接（需登录，仅申请人，状态须为 `approved`）。

**响应 data：**

```json
{
  "url": "https://oss.xxx.com/certificates/xxx.jpg?...",
  "expireAt": "ISO8601"
}
```

**说明：** 证书为图片格式（JPG/PNG），后端生成后存入 OSS，返回 5 分钟有效签名 URL，前端调用微信 API 保存到相册。

**证书建议包含字段：** 授权编号、作品标题、作品预览图、摄影师昵称、被授权方联系方式、使用用途、使用场景、使用期限、授权日期、平台名称/Logo。

---

### 3.7 摄影师工作台 `/studio`

#### POST /studio/photos

提交新作品（需登录，摄影师/管理员）。

**请求体：**

```json
{
  "title": "作品标题，50字内",
  "description": "作品描述，500字内，可选",
  "previewKey": "photos/2026/02/uuid_preview.jpg",
  "originalKey": "photos/2026/02/uuid_original.jpg",
  "categoryId": "string",
  "tagIds": ["string", "string"],
  "price": 100,
  "allowLicense": true
}
```

**响应 data：**

```json
{
  "id": "string",
  "status": "pending"
}
```

**业务规则：**
- `tagIds` 最多 5 个，且必须属于所选 `categoryId`
- `price` 范围 10 ~ 9999

---

#### GET /studio/photos

获取摄影师自己的作品列表（需登录，摄影师/管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | `pending/approved/rejected/offline` |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 Photo 数组（含 `rejectReason`）

---

#### GET /studio/photos/:id

获取摄影师自己的作品详情（需登录，仅本人/管理员）。

**响应 data：** Photo 对象（含原图 key，不含签名 URL）

---

#### PUT /studio/photos/:id

编辑作品（需登录，仅本人/管理员）。

**请求体：**

```json
{
  "title": "string",
  "description": "string",
  "categoryId": "string",
  "tagIds": ["string"],
  "price": 100,
  "allowLicense": true
}
```

**说明：**
- 审核拒绝的作品修改后状态重置为 `pending`
- 已上架作品修改定价仅影响后续购买

---

#### PATCH /studio/photos/:id/status

上架 / 下架作品（需登录，仅本人/管理员）。

**请求体：**

```json
{
  "status": "approved | offline"
}
```

**业务规则：**
- 只允许在 `approved`（上架）和 `offline`（下架）之间切换
- 其他状态返回 400

---

#### DELETE /studio/photos/:id

删除作品（需登录，仅本人/管理员）。

**业务规则：**
- 有购买记录的作品不可删除，返回 409
- 删除前建议前端二次确认

---

#### GET /studio/earnings/overview

获取积分收益概览（需登录，摄影师/管理员）。

**响应 data：**

```json
{
  "totalEarned": 9000,
  "monthEarned": 1200,
  "currentPoints": 8500
}
```

---

#### GET /studio/earnings/history

获取积分收益明细（需登录，摄影师/管理员）。

**Query 参数：** 分页参数

**响应 data：** 分页结构，`list` 为 PointsRecord 数组（仅 `type=earn`），按时间倒序

---

#### GET /studio/licenses

获取待摄影师确认的授权申请列表（需登录，摄影师/管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 默认 `pending_photographer` |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 License 数组（含申请人信息和作品信息）

---

#### PATCH /studio/licenses/:id/review

摄影师确认授权申请（需登录，仅作品摄影师本人/管理员）。

**请求体：**

```json
{
  "action": "approve | reject",
  "rejectReason": "拒绝原因，action=reject 时必填"
}
```

**响应 data：**

```json
{
  "status": "approved | rejected"
}
```

**说明：**
- `approve` 后系统生成授权证书，通知买家
- 仅 `pending_photographer` 状态可操作

---

### 3.8 管理员接口 `/admin`

> 所有 `/admin` 接口需验证 `role === admin`，否则返回 403。

#### GET /admin/dashboard

获取数据概览（管理员）。

**响应 data：**

```json
{
  "today": {
    "newUsers": 12,
    "newPhotos": 5,
    "pointsTraded": 3200,
    "orderCount": 18
  },
  "month": {
    "newUsers": 230,
    "newPhotos": 88,
    "pointsTraded": 56000,
    "orderCount": 320
  },
  "pending": {
    "photoReviewCount": 7,
    "licenseReviewCount": 3
  }
}
```

---

#### GET /admin/photos

获取作品列表（管理员，支持状态筛选）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 默认 `pending` |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 Photo 数组（含摄影师信息）

---

#### PATCH /admin/photos/:id/review

审核作品（管理员）。

**请求体：**

```json
{
  "action": "approve | reject",
  "rejectReason": "画质不达标 | 内容违规 | 信息不完整 | 重复上传 | 其他，action=reject 时必填"
}
```

**响应 data：**

```json
{
  "status": "approved | rejected"
}
```

**说明：**
- `approve` 后作品状态变为 `approved`，通知摄影师
- `reject` 后状态变为 `rejected`，记录原因，通知摄影师

---

#### GET /admin/licenses

获取授权申请列表（管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 默认 `pending_admin` |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 License 数组（含申请人信息和作品信息）

---

#### PATCH /admin/licenses/:id/review

管理员审核授权申请。

**请求体：**

```json
{
  "action": "approve | reject",
  "rejectReason": "拒绝原因，action=reject 时必填"
}
```

**响应 data：**

```json
{
  "status": "pending_photographer | rejected"
}
```

**说明：**
- `approve` 后状态变为 `pending_photographer`，通知摄影师
- `reject` 后状态变为 `rejected`，通知买家

---

#### GET /admin/users

获取用户列表（管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 搜索昵称或手机号 |
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 User 数组

---

#### PATCH /admin/users/:id/ban

封禁用户（管理员）。

**请求体：** 无

**响应 data：** `{ "isBanned": true }`

---

#### PATCH /admin/users/:id/unban

解封用户（管理员）。

**请求体：** 无

**响应 data：** `{ "isBanned": false }`

---

#### GET /admin/categories

获取分类列表（管理员）。

**响应 data：** Category 数组，按 `sort` 升序

---

#### POST /admin/categories

新增分类（管理员）。

**请求体：**

```json
{
  "name": "风景",
  "sort": 1
}
```

**响应 data：** Category 对象

---

#### PUT /admin/categories/:id

编辑分类（管理员）。

**请求体：**

```json
{
  "name": "string",
  "sort": 1
}
```

**响应 data：** Category 对象

---

#### DELETE /admin/categories/:id

删除分类（管理员）。

**业务规则：** 该分类下有作品时返回 409，禁止删除。

---

#### GET /admin/tags

获取标签列表（管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | string | 否 | 按分类筛选 |

**响应 data：** Tag 数组，按 `sort` 升序

---

#### POST /admin/tags

新增标签（管理员）。

**请求体：**

```json
{
  "name": "山川",
  "categoryId": "string",
  "sort": 1
}
```

**响应 data：** Tag 对象

---

#### PUT /admin/tags/:id

编辑标签（管理员）。

**请求体：**

```json
{
  "name": "string",
  "sort": 1
}
```

**响应 data：** Tag 对象

---

#### DELETE /admin/tags/:id

删除标签（管理员）。

---

### 3.9 组织管理 `/admin/orgs`

> 所有 `/admin/orgs` 接口需验证管理员权限。

#### GET /admin/orgs/tree

获取组织树（管理员）。

**响应 data：** Org 数组（树形结构，含 `children`）

---

#### POST /admin/orgs

新增组织（管理员）。

**请求体：**

```json
{
  "name": "组织名称",
  "parentId": "0",
  "sort": 1
}
```

**响应 data：** Org 对象

---

#### PUT /admin/orgs/:id

编辑组织（管理员）。

**请求体：**

```json
{
  "name": "string",
  "parentId": "string",
  "sort": 1
}
```

**响应 data：** Org 对象

---

#### PATCH /admin/orgs/:id/status

启用/禁用组织（管理员）。

**请求体：**

```json
{
  "status": 1
}
```

**说明：** 禁用组织时，该组织下的子组织一并禁用。

---

#### DELETE /admin/orgs/:id

删除组织（管理员）。

**业务规则：** 组织下有用户或子组织时返回 409，禁止删除。

---

### 3.10 角色管理 `/admin/roles`

> 所有 `/admin/roles` 接口需验证管理员权限。

#### GET /admin/roles

获取角色列表（管理员）。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 20 |

**响应 data：** 分页结构，`list` 为 Role 数组

---

#### GET /admin/roles/:id

获取角色详情（管理员）。

**响应 data：** Role 对象（含 `menuIds` — 已分配的菜单ID列表）

---

#### POST /admin/roles

新增角色（管理员）。

**请求体：**

```json
{
  "name": "角色名称",
  "code": "role_code",
  "sort": 1,
  "remark": "备注",
  "menuIds": ["1", "2", "3"]
}
```

**响应 data：** Role 对象

**业务规则：** `code` 不可重复，重复返回 409。

---

#### PUT /admin/roles/:id

编辑角色（管理员）。

**请求体：**

```json
{
  "name": "string",
  "code": "string",
  "sort": 1,
  "remark": "string",
  "menuIds": ["string"]
}
```

**响应 data：** Role 对象

**说明：** 更新 `menuIds` 时全量替换（先删后增）。

---

#### PATCH /admin/roles/:id/status

启用/禁用角色（管理员）。

**请求体：**

```json
{
  "status": 1
}
```

---

#### DELETE /admin/roles/:id

删除角色（管理员）。

**业务规则：** 角色已分配给用户时返回 409，禁止删除。

---

### 3.11 菜单管理 `/admin/menus`

> 所有 `/admin/menus` 接口需验证管理员权限。

#### GET /admin/menus/tree

获取菜单树（管理员）。

**响应 data：** Menu 数组（树形结构，含 `children`）

---

#### POST /admin/menus

新增菜单（管理员）。

**请求体：**

```json
{
  "name": "菜单名称",
  "parentId": "0",
  "type": "dir | menu | button",
  "path": "/admin/users",
  "permission": "user:list",
  "icon": "icon-user",
  "sort": 1
}
```

**响应 data：** Menu 对象

---

#### PUT /admin/menus/:id

编辑菜单（管理员）。

**请求体：**

```json
{
  "name": "string",
  "parentId": "string",
  "type": "dir | menu | button",
  "path": "string",
  "permission": "string",
  "icon": "string",
  "sort": 1
}
```

**响应 data：** Menu 对象

---

#### DELETE /admin/menus/:id

删除菜单（管理员）。

**业务规则：** 菜单下有子菜单时返回 409，禁止删除。

---

### 3.12 用户角色分配 `/admin/users/:id/roles`

> 管理员权限。

#### GET /admin/users/:id/roles

获取用户已分配的角色列表（管理员）。

**响应 data：** Role 数组

---

#### PUT /admin/users/:id/roles

设置用户角色（管理员，全量替换）。

**请求体：**

```json
{
  "roleIds": ["1", "2"]
}
```

**响应 data：**

```json
{
  "roleIds": ["1", "2"]
}
```

**说明：** 全量替换用户的角色关联。

---

#### PUT /admin/users/:id/org

设置用户所属组织（管理员）。

**请求体：**

```json
{
  "orgId": "string"
}
```

**响应 data：**

```json
{
  "orgId": "string"
}
```

---

## 四、待确认事项

| 编号 | 问题 | 影响接口 |
|------|------|----------|
| B-02 | ✅ 已确认：图片格式（JPG/PNG），证书字段见 3.6 节 | `GET /licenses/:id/certificate` |

---

## 五、接口汇总

| 模块 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 认证 | POST | /auth/wx-login | 公开 |
| 认证 | POST | /auth/refresh | 公开 |
| 分类 | GET | /categories | 公开 |
| 分类 | GET | /categories/:id/tags | 公开 |
| 作品 | GET | /photos | 公开 |
| 作品 | GET | /photos/search | 公开 |
| 作品 | GET | /photos/:id | 公开 |
| 作品 | POST | /photos/:id/buy | 登录 |
| 作品 | GET | /photos/:id/download | 登录+已购/本人/管理员 |
| 上传 | GET | /upload/signature | 摄影师/管理员 |
| 用户 | GET | /user/me | 登录 |
| 用户 | POST | /user/enable-photographer | 登录(buyer) |
| 用户 | GET | /user/points-history | 登录 |
| 用户 | GET | /user/orders | 登录 |
| 用户 | GET | /user/licenses | 登录 |
| 授权 | POST | /licenses | 登录 |
| 授权 | GET | /licenses/:id | 登录+相关方 |
| 授权 | GET | /licenses/:id/certificate | 登录+申请人 |
| 工作台 | POST | /studio/photos | 摄影师 |
| 工作台 | GET | /studio/photos | 摄影师 |
| 工作台 | GET | /studio/photos/:id | 摄影师(本人) |
| 工作台 | PUT | /studio/photos/:id | 摄影师(本人) |
| 工作台 | PATCH | /studio/photos/:id/status | 摄影师(本人) |
| 工作台 | DELETE | /studio/photos/:id | 摄影师(本人) |
| 工作台 | GET | /studio/earnings/overview | 摄影师 |
| 工作台 | GET | /studio/earnings/history | 摄影师 |
| 工作台 | GET | /studio/licenses | 摄影师 |
| 工作台 | PATCH | /studio/licenses/:id/review | 摄影师(本人) |
| 管理员 | GET | /admin/dashboard | admin |
| 管理员 | GET | /admin/photos | admin |
| 管理员 | PATCH | /admin/photos/:id/review | admin |
| 管理员 | GET | /admin/licenses | admin |
| 管理员 | PATCH | /admin/licenses/:id/review | admin |
| 管理员 | GET | /admin/users | admin |
| 管理员 | PATCH | /admin/users/:id/ban | admin |
| 管理员 | PATCH | /admin/users/:id/unban | admin |
| 管理员 | GET | /admin/categories | admin |
| 管理员 | POST | /admin/categories | admin |
| 管理员 | PUT | /admin/categories/:id | admin |
| 管理员 | DELETE | /admin/categories/:id | admin |
| 管理员 | GET | /admin/tags | admin |
| 管理员 | POST | /admin/tags | admin |
| 管理员 | PUT | /admin/tags/:id | admin |
| 管理员 | DELETE | /admin/tags/:id | admin |
| 组织管理 | GET | /admin/orgs/tree | admin |
| 组织管理 | POST | /admin/orgs | admin |
| 组织管理 | PUT | /admin/orgs/:id | admin |
| 组织管理 | PATCH | /admin/orgs/:id/status | admin |
| 组织管理 | DELETE | /admin/orgs/:id | admin |
| 角色管理 | GET | /admin/roles | admin |
| 角色管理 | GET | /admin/roles/:id | admin |
| 角色管理 | POST | /admin/roles | admin |
| 角色管理 | PUT | /admin/roles/:id | admin |
| 角色管理 | PATCH | /admin/roles/:id/status | admin |
| 角色管理 | DELETE | /admin/roles/:id | admin |
| 菜单管理 | GET | /admin/menus/tree | admin |
| 菜单管理 | POST | /admin/menus | admin |
| 菜单管理 | PUT | /admin/menus/:id | admin |
| 菜单管理 | DELETE | /admin/menus/:id | admin |
| 用户角色 | GET | /admin/users/:id/roles | admin |
| 用户角色 | PUT | /admin/users/:id/roles | admin |
| 用户组织 | PUT | /admin/users/:id/org | admin |
