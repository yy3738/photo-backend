# Java 后端开发工程师 Skill

你是一名专业的 Java 后端开发工程师，精通 Spring Boot 生态和企业级应用开发。

---

## 技术栈

| 类别 | 技术选型 | 版本要求 |
|------|----------|----------|
| JDK | OpenJDK / GraalVM | >= 17 |
| 框架 | Spring Boot | >= 3.x |
| ORM | MyBatis-Plus | >= 3.5.x |
| 鉴权 | Sa-Token | >= 1.37.x |
| 数据库 | MySQL | >= 8.0 |
| 构建工具 | Maven | >= 3.9 |
| API 文档 | Knife4j (Swagger) | >= 4.x |
| 参数校验 | Jakarta Validation (Hibernate Validator) | |
| JSON | Jackson | Spring Boot 内置 |
| 工具库 | Hutool、Lombok | |
| 日志 | SLF4J + Logback | Spring Boot 内置 |

---

## 项目结构规范

采用经典分层架构，包名以 `com.photo` 为根包：

```
src/main/java/com/photo/
├── PhotoApplication.java              # 启动类
├── common/                            # 通用模块
│   ├── config/                        # 配置类（Sa-Token、MyBatis-Plus、CORS、OSS 等）
│   ├── constant/                      # 常量定义
│   ├── enums/                         # 枚举类
│   ├── exception/                     # 自定义异常 + 全局异常处理器
│   ├── result/                        # 统一响应封装（R<T>）
│   └── utils/                         # 工具类
├── module/                            # 业务模块（按领域划分）
│   ├── auth/                          # 认证模块
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   ├── user/                          # 用户模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── dto/
│   ├── photo/                         # 作品模块
│   ├── order/                         # 订单模块
│   ├── license/                       # 授权模块
│   ├── category/                      # 分类标签模块
│   ├── upload/                        # 上传模块
│   └── admin/                         # 管理员模块
│       ├── controller/
│       ├── service/
│       └── dto/
src/main/resources/
├── application.yml                    # 主配置
├── application-dev.yml                # 开发环境配置
├── application-prod.yml               # 生产环境配置
└── mapper/                            # MyBatis XML 映射文件（如需要）
```

每个业务模块内部结构：
- `controller/` — 控制器，只做参数接收、校验和响应返回
- `service/` — 业务接口 + 实现类（`XxxService` 接口 + `XxxServiceImpl` 实现）
- `mapper/` — MyBatis-Plus Mapper 接口（继承 `BaseMapper<T>`）
- `entity/` — 数据库实体类（与表一一对应）
- `dto/` — 请求/响应 DTO（`XxxReqDTO`、`XxxRespDTO`）

---

## 数据库设计规范

### 基本原则
- 基本符合第三范式（3NF），适当允许冗余以优化查询性能（需注释说明）
- 表名使用 `snake_case`，统一前缀 `t_`（如 `t_user`、`t_photo`）
- 字段名使用 `snake_case`
- 主键统一使用 `id`，类型 `BIGINT`，MyBatis-Plus 雪花算法生成
- 所有表必须包含以下公共字段：

```sql
id          BIGINT       NOT NULL PRIMARY KEY COMMENT '主键ID',
create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除'
```

### 命名约定
- 布尔字段以 `is_` 开头：`is_deleted`、`is_banned`
- 外键字段以关联表名 + `_id` 命名：`user_id`、`photo_id`
- 状态字段命名为 `status`，使用 `VARCHAR` 存储枚举值
- 金额/积分字段使用 `INT` 或 `BIGINT`（以最小单位存储）

### 索引规范
- 外键字段必须建索引
- 高频查询条件字段建索引
- 联合索引遵循最左前缀原则
- 索引命名：`idx_表名_字段名`

---

## 编码规范

### 统一响应格式

```java
@Data
public class R<T> {
    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
```

- `code = 0` 表示成功，非 0 为业务错误
- Controller 方法统一返回 `R<T>`

### Sa-Token 鉴权架构

```java
// 登录
StpUtil.login(userId);
String token = StpUtil.getTokenValue();

// 角色校验
StpUtil.checkRole("admin");

// 权限拦截器配置
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 公开接口放行
            SaRouter.match("/auth/**").stop();
            SaRouter.match("/categories/**").stop();
            SaRouter.match("/photos").stop();
            SaRouter.match("/photos/search").stop();
            SaRouter.match("/photos/{id}").stop();

            // 需登录接口
            SaRouter.match("/**").check(r -> StpUtil.checkLogin());

            // 管理员接口
            SaRouter.match("/admin/**").check(r -> StpUtil.checkRole("admin"));

            // 摄影师接口
            SaRouter.match("/studio/**").check(r -> {
                StpUtil.checkRoleOr("photographer", "admin");
            });
        })).addPathPatterns("/**");
    }
}
```

- 用户角色存储在 Sa-Token Session 中
- Token 通过 `Authorization: Bearer {token}` 传递
- Sa-Token 配置 `token-prefix: Bearer`

### Controller 规范

```java
@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public R<PageResult<PhotoRespDTO>> list(PhotoQueryDTO query) {
        return R.ok(photoService.listPhotos(query));
    }

    @PostMapping("/{id}/buy")
    public R<BuyRespDTO> buy(@PathVariable Long id) {
        return R.ok(photoService.buyPhoto(id));
    }
}
```

- 使用构造器注入（`@RequiredArgsConstructor`），不用 `@Autowired`
- 路径参数用 `@PathVariable`，查询参数用 DTO 对象接收，请求体用 `@RequestBody` + `@Validated`
- Controller 不写业务逻辑，只做参数传递

### Service 规范

```java
public interface PhotoService extends IService<Photo> {
    PageResult<PhotoRespDTO> listPhotos(PhotoQueryDTO query);
    BuyRespDTO buyPhoto(Long photoId);
}

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl extends ServiceImpl<PhotoMapper, Photo> implements PhotoService {

    private final OrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuyRespDTO buyPhoto(Long photoId) {
        // 业务逻辑
    }
}
```

- Service 接口继承 `IService<T>`，实现类继承 `ServiceImpl<M, T>`
- 涉及多表写操作必须加 `@Transactional(rollbackFor = Exception.class)`
- 复杂查询使用 MyBatis-Plus 的 `LambdaQueryWrapper`

### Entity 规范

```java
@Data
@TableName("t_photo")
public class Photo {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;
    private String description;
    private Long photographerId;
    private String status;
    private Integer price;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
```

- 使用 `@TableId(type = IdType.ASSIGN_ID)` 雪花算法主键
- 使用 `@TableLogic` 逻辑删除
- 时间字段使用 `LocalDateTime`
- 不在 Entity 上加业务逻辑

### DTO 规范

```java
// 请求 DTO — 带校验注解
@Data
public class PhotoCreateReqDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题不超过50字")
    private String title;

    @Size(max = 500, message = "描述不超过500字")
    private String description;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Size(max = 5, message = "标签最多5个")
    private List<Long> tagIds;

    @NotNull @Range(min = 10, max = 9999, message = "定价范围10~9999")
    private Integer price;
}

// 响应 DTO — 纯数据载体
@Data
public class PhotoRespDTO {
    private Long id;
    private String title;
    private String previewUrl;
    private String categoryName;
    private Integer price;
    private LocalDateTime createTime;
}
```

- 请求和响应 DTO 分开定义，不复用 Entity
- 请求 DTO 使用 Jakarta Validation 注解校验
- Entity 与 DTO 之间使用 BeanUtil 或手动转换

### 分页封装

```java
@Data
public class PageQuery {
    @Min(1)
    private Integer page = 1;

    @Range(min = 1, max = 50)
    private Integer pageSize = 20;

    public <T> Page<T> toMpPage() {
        return new Page<>(page, pageSize);
    }
}

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;

    public static <T> PageResult<T> of(IPage<?> page, List<T> list) {
        return new PageResult<>(list, page.getTotal(),
            (int) page.getCurrent(), (int) page.getSize());
    }
}
```

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLogin(NotLoginException e) {
        return R.fail(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRole(NotRoleException e) {
        return R.fail(403, "权限不足");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return R.fail(400, msg);
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(e.getHttpStatus())
            .body(R.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "服务器内部错误");
    }
}
```

- 自定义 `BizException` 携带业务错误码和 HTTP 状态码
- Sa-Token 异常统一捕获转换

---

## 注释规范

- 类注释：说明类的职责，使用 `/** */` 格式
- 方法注释：公开方法写清楚功能说明，复杂参数加 `@param`、`@return`
- 行内注释：只在逻辑不直观的地方加，解释"为什么"而非"做了什么"
- 不写废话注释（如 `// 获取用户` 在 `getUser()` 上面）

```java
/**
 * 作品服务
 */
public interface PhotoService extends IService<Photo> {

    /**
     * 积分购买作品
     * 扣减买家积分 + 增加摄影师积分（扣除10%平台抽佣），原子操作
     */
    BuyRespDTO buyPhoto(Long photoId);
}
```

---

## MyBatis-Plus 配置

```java
@Configuration
@MapperScan("com.photo.module.*.mapper")
public class MybatisPlusConfig {

    /**
     * 分页 + 乐观锁 + 防全表更新插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }
}
```

自动填充：

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
}
```

---

## 关键约束

1. 所有接口遵循 RESTful 风格，路径使用 `kebab-case`
2. 时间格式统一 ISO 8601：`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
3. 敏感配置（数据库密码、OSS 密钥等）不硬编码，通过 `application-*.yml` 或环境变量注入
4. 积分操作必须保证原子性，使用数据库事务 + 乐观锁/悲观锁防并发
5. OSS 原图路径不暴露给前端，通过后端签名 URL 访问
6. 逻辑删除全局启用，`is_deleted = 1` 表示已删除
7. 接口限流使用 Sa-Token 的 `@SaCheckSafe` 或自定义注解 + AOP 实现
8. 日志打印关键业务操作（登录、购买、审核），不打印敏感信息
9. 所有列表接口支持分页，默认 page=1, pageSize=20, 最大 50
10. 枚举值在代码中定义为 Enum 类，数据库存储字符串值
