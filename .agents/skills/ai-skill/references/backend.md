# 后端开发规范

## 目录

1. [权限控制 (RBAC)](#1-权限控制-rbac)
2. [数据权限](#2-数据权限)
3. [用户体系](#3-用户体系)
4. [分页查询](#4-分页查询)
5. [对象转换](#5-对象转换)
6. [数据翻译](#6-数据翻译)
7. [异常处理](#7-异常处理)
8. [参数校验](#8-参数校验)
9. [文件存储](#9-文件存储)
10. [Excel 导入导出](#10-excel-导入导出)
11. [日志与审计](#11-日志与审计)
12. [实体规范 (DO)](#12-实体规范-do)
13. [Mapper 增强](#13-mapper-增强)
14. [多数据源](#14-多数据源)
15. [Redis 缓存](#15-redis-缓存)
16. [异步任务](#16-异步任务)
17. [分布式锁](#17-分布式锁)
18. [接口防护](#18-接口防护)
19. [单元测试](#19-单元测试)
20. [WebSocket](#20-websocket)

---

## 1. 权限控制 (RBAC)

### 鉴权模型
- 用户 (User) → 角色 (Role) → 菜单/权限 (Menu/Permission)
- Token 存储: Redis (`oauth2_access_token:%s`) + DB (`system_oauth2_access_token`)
- Header: `Authorization: Bearer {token}`
- 调试模式: `Authorization: Bearer test1` 模拟用户 ID=1

### 权限注解
```java
// 权限标识控制（推荐）
@PreAuthorize("@ss.hasPermission('system:user:query')")
public CommonResult<PageResult<UserRespVO>> getUserPage(...) { }

// 放行接口
@PermitAll
public CommonResult<String> getPublicData() { }
```

---

## 2. 数据权限

基于 MyBatis Plus 拦截器自动过滤，支持部门/个人隔离。

### 必备字段
- `dept_id` - 部门隔离
- `user_id` - 个人隔离

### 开关控制
```java
// 关闭数据权限
@DataPermission(enable = false)
public void queryAll() { }

// 手动忽略
DataPermissionUtils.executeIgnore(() -> { /* 全局查询 */ });
```

---

## 3. 用户体系

| 特性 | 管理员 (Admin) | 会员 (Member) |
|------|----------------|---------------|
| 数据库表 | `system_users` | `member_user` |
| API 前缀 | `/admin-api/**` | `/app-api/**` |
| 获取用户 | `SecurityFrameworkUtils.getLoginUserId()` | 同左 |

---

## 4. 分页查询

### ReqVO 定义
```java
@Data
public class UserPageReqVO extends PageParam {
    private String username;
    private Integer status;
}
```

### Mapper 实现
```java
default PageResult<UserDO> selectPage(UserPageReqVO reqVO) {
    return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
        .likeIfPresent(UserDO::getUsername, reqVO.getUsername())
        .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
        .betweenIfPresent(UserDO::getCreateTime, reqVO.getCreateTime())
        .orderByDesc(UserDO::getId));
}
```

### Controller 返回
```java
return success(UserConvert.INSTANCE.convertPage(pageResult));
```

---

## 5. 对象转换

**禁止手动 set/get 转换，必须使用 MapStruct**

```java
@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserDO convert(UserCreateReqVO reqVO);
    UserRespVO convert(UserDO user);
    PageResult<UserRespVO> convertPage(PageResult<UserDO> page);
}
```

**备选**: `BeanUtils.toBean(source, TargetClass.class)`

---

## 6. 数据翻译

当 VO 需展示关联字段（如 `dept_id` → `dept_name`）时，使用 Easy-Trans：

```java
// VO 实现接口
public class UserRespVO implements com.fhs.core.trans.vo.VO {
    @Trans(type = TransType.SIMPLE, target = DeptDO.class, fields = "name")
    private Long deptId;
    private String deptName; // 自动填充
}

// Controller 添加注解
@TransMethodResult
public CommonResult<UserRespVO> getUser(Long id) { }
```

---

## 7. 异常处理

```java
// 抛出业务异常
throw exception(USER_NOT_EXISTS);

// 错误码定义
public interface ErrorCodeConstants {
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1001001, "用户不存在");
}
```

---

## 8. 参数校验

```java
@Data
public class UserCreateReqVO {
    @NotEmpty(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度 2-20")
    private String username;

    @Mobile  // 自定义手机号校验
    private String mobile;

    @InEnum(CommonStatusEnum.class)  // 枚举校验
    private Integer status;
}
```

**时间规范**: 统一使用 `LocalDateTime`，禁止使用 `Date`

---

## 9. 文件存储

### 后端上传
```java
@Resource
private FileApi fileApi;

public String uploadFile(byte[] content) {
    return fileApi.createFile("path/file.jpg", content);
}
```

### 前端直传 S3
- 配置 `VITE_UPLOAD_TYPE=client`
- 后端只保存 URL 到数据库

### 私有桶
```java
String url = fileApi.presignGetUrl(path, expires);
```

---

## 10. Excel 导入导出

### 导出
```java
@Data
@ExcelIgnoreUnannotated
public class UserRespVO {
    @ExcelProperty("用户名称")
    private String username;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status")
    private Integer status;
}

@GetMapping("/export")
public void export(HttpServletResponse response, @Validated UserPageReqVO reqVO) {
    reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    List<UserDO> list = userService.getUserList(reqVO);
    ExcelUtils.write(response, "用户数据.xls", "用户列表", 
        UserRespVO.class, BeanUtils.toBean(list, UserRespVO.class));
}
```

### 导入
```java
@PostMapping("/import")
public CommonResult<Boolean> importExcel(@RequestParam("file") MultipartFile file) {
    List<UserImportExcelVO> list = ExcelUtils.read(file, UserImportExcelVO.class);
    userService.importUsers(list);
    return success(true);
}
```

---

## 11. 日志与审计

### 操作日志
```java
@LogRecord(
    type = "USER",
    subType = "UPDATE_PASSWORD",
    bizNo = "{{#reqVO.id}}",
    success = "修改密码成功，用户ID：{{#reqVO.id}}"
)
public void updatePassword(UserUpdatePasswordReqVO reqVO) { }
```

### API 访问日志
```java
@ApiAccessLog(enable = false)           // 不记录
@ApiAccessLog(sanitizeKeys = "password") // 脱敏
```

---

## 12. 实体规范 (DO)

```java
@TableName("system_user")
@KeySequence("system_user_seq")  // Oracle/PostgreSQL
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends BaseDO {
    @TableId
    private Long id;
    private String username;
    
    // 字段加密
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String password;
}
```

- 主键: `Long` 自增，或配置 `id-type: ASSIGN_ID` 使用雪花算法
- 逻辑删除: `deleted=0` 正常，`deleted=1` 删除

---

## 13. Mapper 增强

必须继承 `BaseMapperX<T>`：

```java
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
    // 分页查询
    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) { ... }
    
    // 批量插入
    default void batchInsert(List<UserDO> list) {
        insertBatch(list);
    }
}
```

### 联表查询 (MPJ)
```java
return selectJoinList(UserDetailDO.class, new MPJLambdaWrapper<UserDO>()
    .selectAll(UserDO.class)
    .selectAs(DeptDO::getName, UserDetailDO::getDeptName)
    .leftJoin(DeptDO.class, DeptDO::getId, UserDO::getDeptId));
```

---

## 14. 多数据源

```java
@Slave              // 读从库
@Master             // 强制主库
@DS("sharding")     // 切换数据源

@DSTransactional    // 跨库事务
```

---

## 15. Redis 缓存

### Key 管理
```java
public interface RedisKeyConstants {
    String USER_ROLE_LIST = "system:user_role_list:%s";
}
```

### 编程式缓存
```java
// 创建 XxxRedisDAO，注入 StringRedisTemplate
// 禁止 Service 直接操作 RedisTemplate
```

### 声明式缓存
```java
@Cacheable(cacheNames = "users#3600", key = "#id")  // 缓存 3600 秒
public UserDO getUser(Long id) { }
```

---

## 16. 异步任务

```java
@Async
public void sendNotification(Long userId) { }
```
- 禁止同类内部调用
- 自动传递 `LoginUser` 和 `TenantId`

---

## 17. 分布式锁

### 声明式锁
```java
@Lock4j(keys = {"#orderId"}, acquireTimeout = 1000, expire = 60000)
public void processOrder(Long orderId) { }
```

### 编程式锁
```java
RLock lock = redissonClient.getLock("my-lock");
if (lock.tryLock()) {
    try { /* 业务逻辑 */ } 
    finally { lock.unlock(); }
}
```

---

## 18. 接口防护

### 幂等性
```java
@Idempotent(timeout = 10, message = "请勿重复提交")
public void createOrder(@RequestBody OrderReqVO req) { }
```

### 限流
```java
@RateLimiter(count = 10, time = 60)  // 60秒内最多10次
public List<UserDO> list() { }
```

---

## 19. 单元测试

| 基类 | 用途 |
|------|------|
| `BaseMockitoUnitTest` | 纯 Mock 测试（推荐） |
| `BaseDbUnitTest` | H2 数据库测试 |
| `BaseRedisUnitTest` | Redis 测试 |

```java
public class UserServiceTest extends BaseMockitoUnitTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserMapper userMapper;

    @Test
    public void testCreateUser_success() {
        when(userMapper.selectByUsername("test")).thenReturn(null);
        userService.createUser(new UserCreateReqVO().setUsername("test"));
        verify(userMapper).insert(argThat(u -> "test".equals(u.getUsername())));
    }
}
```

---

## 20. WebSocket

### 发送消息
```java
@Resource
private WebSocketMessageSender webSocketMessageSender;

public void notify(Long userId) {
    webSocketMessageSender.sendObject(2, userId, "type", content);
}
```

### 连接地址
`ws://{host}/infra/ws?token={token}`

### 获取用户
```java
WebSocketFrameworkUtils.getLoginUser(session)
```
