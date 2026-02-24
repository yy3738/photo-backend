# 摄影作品平台 — 后端项目

## 项目简介

微信小程序摄影作品交易与授权平台后端。摄影师上传作品，买家通过积分购买并申请授权，管理员审核作品与授权申请。

## 技术栈

- Java 17 + Spring Boot 3.4.3
- MyBatis-Plus 3.5.9（ORM + 分页 + 自动填充，需 mybatis-plus-jsqlparser 扩展）
- Sa-Token 1.39.0（认证鉴权，@SaCheckLogin 注解）
- MySQL 8.0
- Maven 构建
- Logback 日志（dev: DEBUG + SQL，prod: WARN + 文件滚动）

## 用户角色

| 角色 | 说明 |
|------|------|
| buyer | 买家，默认角色，可浏览/购买/申请授权 |
| photographer | 摄影师，可上传/管理作品/查看积分收益 |
| admin | 管理员，全部权限 |

## 代码结构

```
src/main/java/com/photo/
├── common/                  # 通用模块
│   ├── config/              # MybatisPlusConfig、SaTokenConfig、CorsConfig
│   ├── enums/               # UserRole、PhotoStatus、LicenseStatus、PointsType、MenuType、LicensePurpose
│   ├── result/              # R<T>、PageQuery、PageResult
│   └── exception/           # BizException、GlobalExceptionHandler
└── mvc/                     # 业务层（按层级分包，类平铺）
    ├── controller/          # 8 个 Controller 平铺
    ├── entity/              # 实体总包
    │   ├── model/           # PO 实体（Sys/Biz 前缀 + PO 后缀，如 SysUserPO、BizPhotoPO）
    │   ├── req/             # 请求对象（XxxReq，如 WxLoginReq、StudioPhotoReq）
    │   └── vo/              # 响应对象（XxxVO，如 PhotoVO、LoginVO）
    ├── mapper/              # Mapper 接口（Sys/Biz 前缀，如 SysUserMapper、BizPhotoMapper）
    └── service/             # Service 类（Sys/Biz 前缀，如 SysUserService、BizPhotoService、AuthService、AdminService）
```

### 命名规则
- entity/model：`Sys` 前缀 = 系统表，`Biz` 前缀 = 业务表，统一 `PO` 后缀
- entity/req：请求 DTO，`XxxReq` 后缀（原 ReqDTO → Req）
- entity/vo：响应 DTO，`XxxVO` 后缀（原 RespDTO → VO）
- mapper：与 PO 对应，`SysXxxMapper` / `BizXxxMapper`
- service：与领域对应，`SysXxxService` / `BizXxxService`（AuthService、AdminService 不加前缀）

## 关键约定

- 统一响应体 `R<T>`，字段：code / message / data
- 分页请求 `PageQuery`（page + pageSize），响应 `PageResult<T>`
- Entity 公共字段：id / createTime / updateTime / createBy / updateBy，MyBatis-Plus MetaObjectHandler 自动填充
- ID 类型 ASSIGN_ID（雪花算法），返回前端时转 String 防精度丢失
- 角色校验在 Service 层手动判断（checkAdmin / checkPhotographer），不依赖注解
- 授权申请流程：pending_admin → pending_photographer → approved / rejected
- 购买作品时摄影师获得 90% 积分收入
- 逻辑删除字段 is_deleted，乐观锁字段 version

## 数据库设计

数据库：MySQL 8.0+，主键 BIGINT 雪花算法，逻辑删除，不使用外键。

### 表清单（13 张）

| 表名 | 类型 | 说明 |
|------|------|------|
| t_sys_user | 系统 | 用户表（含 role、org_id） |
| t_sys_category | 系统 | 分类表 |
| t_sys_tag | 系统 | 标签表 |
| t_sys_org | 系统 | 组织/部门表（树形） |
| t_sys_role | 系统 | 角色表 |
| t_sys_menu | 系统 | 菜单表（dir/menu/button 三级） |
| t_sys_user_role | 系统 | 用户-角色关联表 |
| t_sys_role_menu | 系统 | 角色-菜单关联表 |
| t_biz_photo | 业务 | 作品表 |
| t_biz_photo_tag | 业务 | 作品-标签关联表 |
| t_biz_order | 业务 | 订单表 |
| t_biz_license | 业务 | 授权申请表 |
| t_biz_points_record | 业务 | 积分流水表 |

## API 接口模块

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | /auth | 微信登录 |
| 分类标签 | /categories | 公开分类与标签查询 |
| 作品 | /photos | 公开作品列表/详情/搜索/购买/下载 |
| 上传 | /upload | OSS 直传签名 |
| 用户中心 | /user | 个人信息/积分明细/订单/授权记录 |
| 授权申请 | /licenses | 提交/查看/下载证书 |
| 摄影师工作台 | /studio | 作品CRUD/销售统计/收入明细/授权审批 |
| 管理员 | /admin | 仪表盘/用户管理/作品审核/授权审核/分类标签CRUD/积分充值 |

## 业务规则

- 积分：1元 = 10积分，平台抽佣 10%，摄影师实得 90%
- 作品定价：10 ~ 9999 积分，可改价仅影响后续购买
- Token：Sa-Token 管理会话，timeout 7200s
- 图片：预览图公开访问，原图私有桶签名 URL 5 分钟有效
- 授权流程：买家申请 → 管理员初审 → 摄影师确认 → 生成证书

## 配置说明

application.yml 中需配置：
- 数据库连接（spring.datasource）
- 微信小程序 appId / secret（wx.miniapp）
- OSS 地址（oss.host）
- Sa-Token 超时时间（sa-token.timeout）

## 日志配置

logback-spring.xml 按 profile 区分：
- dev：com.photo DEBUG（含 MyBatis SQL 参数/结果），Spring Web DEBUG，控制台输出
- prod：com.photo INFO，Spring WARN，控制台 + 文件滚动（logs/ 目录，保留 30 天）
- SQL 日志通过 Slf4jImpl 桥接到 logback，不再使用 StdOutImpl

## 文档

```
docs/
├── PRD.md         # 产品需求文档
├── sql.md         # 数据库设计文档
└── api-design.md  # 后端接口设计文档
sql/
└── V0.1__init.sql # 数据库初始化脚本
```
