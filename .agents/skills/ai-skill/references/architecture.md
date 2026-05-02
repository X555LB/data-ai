# 项目架构与模块设计

## Maven 多模块结构

```
data-ai/
├── yudao-dependencies          # BOM 版本管理，统一依赖版本
├── yudao-framework             # 核心组件封装
│   ├── yudao-common            # 通用工具类
│   ├── yudao-spring-boot-starter-web
│   ├── yudao-spring-boot-starter-mybatis
│   ├── yudao-spring-boot-starter-security
│   ├── yudao-spring-boot-starter-redis
│   ├── yudao-spring-boot-starter-job
│   └── yudao-spring-boot-starter-mq
├── yudao-server                # 启动入口/聚合层
└── yudao-module-xxx            # 业务模块
    ├── yudao-module-system     # 系统核心: 用户/权限/租户
    ├── yudao-module-infra      # 基建: 代码生成/文件/配置
    ├── yudao-module-bpm        # 工作流
    └── yudao-module-xxx-biz    # 其他业务模块
```

---

## 新建模块规范

### 模块物理结构

1. **位置**: 项目根目录
2. **命名**: `yudao-module-{biz_name}`
3. **POM 配置**:
```xml
<parent>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao</artifactId>
</parent>
<artifactId>yudao-module-{biz_name}</artifactId>
<packaging>jar</packaging>

<dependencies>
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-mybatis</artifactId>
    </dependency>
</dependencies>
```

### 包结构规范

**Base Package**: `cn.iocoder.yudao.module.{biz_name}`

```
cn.iocoder.yudao.module.{biz}/
├── controller/
│   ├── admin/          # 管理端 @RequestMapping("/admin-api/{biz}/{feature}")
│   └── app/            # 用户端 @RequestMapping("/app-api/{biz}/{feature}")
├── service/
│   ├── XxxService.java
│   └── XxxServiceImpl.java
├── dal/
│   ├── dataobject/     # DO 实体类
│   ├── mysql/          # Mapper 接口
│   └── redis/          # Redis DAO
├── api/                # 模块间通信接口
├── convert/            # MapStruct 转换器
├── enums/              # 枚举和错误码
└── job/                # 定时任务
```

**Controller 命名规避冲突**：
- 管理端: `{Feature}Controller`
- 用户端: `App{Feature}Controller`

### 注册与启动

1. **Maven 聚合**: 在 `yudao-server/pom.xml` 中引入新模块依赖
2. **包扫描**: 确保 `@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao")` 覆盖新模块

---

## 代码分层详解

### Controller 层

```java
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/admin-api/system/user")
@Validated
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO reqVO) {
        return success(userService.createUser(reqVO));
    }
}
```

**规范**:
- 必须使用 Swagger 注解 (`@Tag`, `@Operation`)
- 必须使用 `@Validated` 校验入参
- 必须使用 `CommonResult<T>` 包装返回值
- 必须使用 `@PreAuthorize` 权限注解

### Service 层

```java
public interface UserService {
    Long createUser(UserCreateReqVO reqVO);
}

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public Long createUser(UserCreateReqVO reqVO) {
        // 1. 校验逻辑
        validateUserExists(reqVO.getUsername());
        // 2. VO 转 DO
        UserDO user = UserConvert.INSTANCE.convert(reqVO);
        // 3. 插入数据
        userMapper.insert(user);
        return user.getId();
    }
}
```

### DAL 层

```java
// DO 实体类
@TableName("system_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO {
    @TableId
    private Long id;
    private String username;
    private String password;
    private Integer status;
}

// Mapper 接口
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
            .likeIfPresent(UserDO::getUsername, reqVO.getUsername())
            .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
            .orderByDesc(UserDO::getId));
    }
}
```

---

## 模块移除流程

当需要移除不需要的模块（如 CRM、ERP）时：

1. **删除 Maven 依赖**: 
   - 根目录 `pom.xml` 移除模块声明
   - `yudao-server/pom.xml` 移除依赖

2. **删除数据库表**: 清理对应前缀的表（如 `crm_` 开头）

3. **清理系统数据**:
   - `system_menu`: 删除 `component` 路径指向该模块的记录
   - `system_dict_type/data`: 删除对应模块的字典
   - `system_error_code`: 删除对应模块的错误码

4. **前端清理**: 删除 `src/views/{module}` 和 `src/api/{module}`

---

## 常见问题排查

| 问题 | 解决方案 |
|------|----------|
| 404 Not Found | 检查 `YudaoServerApplication` 是否扫描到新模块的 package |
| Swagger 分组不显示 | 在配置类中注册 `GroupedOpenApi` Bean |
| SQL 日志不打印 | 设置 `logging.level.{package}: DEBUG` |
| 前端 Lint 报错 | 执行 `npm run lint -- --fix` |
| 日期格式问题 | 统一使用 `LocalDateTime`，不支持 `LocalDate` |
