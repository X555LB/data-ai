---
name: data-ai
description: data-ai 快速开发平台开发规范与代码生成指南。基于 Spring Boot 3 + MyBatis Plus + Vue 3 + Element Plus 技术栈。当用户需要：(1) 使用 data-ai 框架开发后端模块、(2) 生成 Controller/Service/Mapper 代码、(3) 实现 RBAC 权限控制、(4) 接入 Flowable 工作流/审批流、(5) 使用代码生成器、(6) 开发前端 CRUD 页面、(7) 处理多租户、数据权限、(8) 配置定时任务或消息队列时触发此技能。
---

# data-ai 开发规范

## 技术栈版本 (严格遵守)

**后端**:
- JDK 17/21 (`master-jdk17` 分支)
- Spring Boot 3.3.1
- MyBatis Plus 3.5.7
- MySQL >= 5.7
- Redis 5.0+ (Redisson 3.32.0)
- Spring Security 6.3.1
- Flowable 7.0.0

**前端**:
- Vue 3.x (Composition API)
- Vite 4.x
- Element Plus / Vben Admin
- Pinia + TypeScript 4.9+

---

## 核心架构规范

### Maven 模块结构
```
data-ai/
├── yudao-dependencies      # BOM 版本管理
├── yudao-framework         # 核心组件封装
│   ├── yudao-common
│   ├── yudao-spring-boot-starter-web
│   ├── yudao-spring-boot-starter-mybatis
│   └── yudao-spring-boot-starter-security
├── yudao-server            # 启动入口
└── yudao-module-xxx        # 业务模块
    └── cn.iocoder.yudao.module.{biz}
        ├── controller.admin/app
        ├── service
        ├── dal.dataobject/mysql/redis
        └── api (模块间通信)
```

### 代码分层规范 (禁止跨层调用)

| 层级 | 职责 | 命名规范 |
|------|------|----------|
| **Controller** | 接收请求，使用 VO | `XxxController`, URL: `/admin-api/` 或 `/app-api/` |
| **Service** | 业务逻辑，POJO 转换 | `XxxService` + `XxxServiceImpl` |
| **DAL** | 数据访问，继承 `BaseDO` | `XxxMapper extends BaseMapperX` |
| **API** | 模块间通信接口 | 通过 `api` 包暴露 |

### 关键约定

1. **入参/出参**：必须使用 VO (`XxxCreateReqVO`, `XxxRespVO`)，禁止暴露 DO
2. **对象转换**：必须使用 MapStruct (`XxxConvert`)，禁止手动 set/get
3. **异常处理**：使用 `throw exception(ERROR_CODE)`，返回 `CommonResult<T>`
4. **分页查询**：ReqVO 继承 `PageParam`，Mapper 使用 `LambdaQueryWrapperX`
5. **校验注解**：VO 必须使用 `@NotNull`, `@Size` 等，Controller 加 `@Validated`

---

## 多租户与权限

### 多租户
- 所有业务表必须包含 `tenant_id` 字段
- SQL 自动拼接 `WHERE tenant_id = ?`
- 忽略租户：`@TenantIgnore` 或 `TenantUtils.executeIgnore()`

### RBAC 权限
```java
@PreAuthorize("@ss.hasPermission('system:user:query')")
public CommonResult<PageResult<UserRespVO>> getUserPage(...) { }
```
- 管理后台 `/admin-api/` 默认拦截
- 放行接口：`@PermitAll`

### 数据权限
- 表需包含 `dept_id` 或 `user_id` 字段
- 关闭：`@DataPermission(enable = false)`

---

## 建表必备字段 (代码生成器要求)

```sql
`creator` varchar(64) DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
PRIMARY KEY (`id`)
```

---

## 参考文档

根据开发任务，按需阅读以下参考文档：

| 场景 | 参考文档 |
|------|----------|
| 创建新模块、理解项目结构 | [architecture.md](references/architecture.md) |
| 后端 CRUD、权限、缓存、测试 | [backend.md](references/backend.md) |
| 前端组件、Hooks、CRUD 模式 | [frontend.md](references/frontend.md) |
| Flowable 工作流、审批流 | [workflow.md](references/workflow.md) |
| 定时任务、消息队列 | [middleware.md](references/middleware.md) |

---

## 代码生成器使用流程

1. **建表**：遵循上述必备字段规范
2. **导入**：基础设施 → 代码生成 → 导入表
3. **配置**：设置字段查询/列表/必填属性
4. **生成**：下载代码包，解压到对应模块
5. **执行 SQL**：运行 `sql/sql.sql` 添加菜单

---

## API 生成规范

生成 Controller 时必须包含：
```java
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/admin-api/system/user")
@Validated
public class UserController {
    @Operation(summary = "获得用户分页")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO reqVO) { }
}
```
