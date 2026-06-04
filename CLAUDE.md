# Project: data-ai

Spring Boot 3.5 + Spring AI 1.1 的 AI 平台后端，支持多模型厂商（DeepSeek、OpenAI、Ollama）。

## Tech Stack

- **Runtime**: Java 17, Spring Boot 3.5.9, Maven
- **ORM**: MyBatis Plus 3.5.8 + dynamic-datasource
- **AI**: Spring AI 1.1.2 + 自定义多平台适配层 (`framework/ai/`)
- **向量存储**: Qdrant / Milvus（手动创建，禁用自动配置）
- **缓存**: Redis + spring-boot-starter-cache
- **工具库**: Lombok, MapStruct 1.6, Hutool 5.8, Swagger/OpenAPI 3

## Architecture

```
cn.boss.data.ai
├── controller/<module>/          # REST 控制器 + VO
│   └── vo/<entity>/              # 请求/响应 VO
├── service/<module>/             # Service 接口 + Impl（同目录）
│   ├── bo/                       # 业务对象（内部传递）
│   └── splitter/                 # 文档切分等业务组件
├── dal/
│   ├── dataobject/<module>/      # DO（继承 BaseDO）
│   └── mysql/<module>/           # Mapper（继承 BaseMapperX）
├── enums/<module>/               # 枚举
├── framework/
│   ├── ai/                       # Spring AI 适配层
│   │   ├── core/model/           # AiModelFactory + 各平台实现
│   │   └── config/               # AI 配置
│   ├── common/                   # CommonResult, 异常, 工具类
│   └── mybatis/                  # BaseMapperX, BaseDO, 查询封装
├── tool/
│   ├── function/                 # AI Tool Calling（Function 方式）
│   └── method/                   # AI Tool Calling（Method 方式）
└── util/                         # 通用工具
```

## Module Domains

| 模块 | 路径 | 说明 |
|------|------|------|
| chat | controller/chat, service/chat, dal/.../chat | AI 对话（会话 + 消息） |
| knowledge | controller/knowledge, service/knowledge, dal/.../knowledge | 知识库（库 + 文档 + 分段 + 向量检索） |
| model | controller/model, service/model, dal/.../model | 模型管理（API Key、模型、角色、工具） |

## Conventions

### Naming

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| DO | `Ai<Entity>DO` | `AiChatConversationDO` |
| Mapper | `Ai<Entity>Mapper` extends `BaseMapperX<XxxDO>` | `AiChatConversationMapper` |
| Service 接口 | `Ai<Entity>Service` | `AiChatConversationService` |
| Service 实现 | `Ai<Entity>ServiceImpl` | `AiChatConversationServiceImpl` |
| Controller | `Ai<Entity>Controller` | `AiChatConversationController` |
| 请求 VO | `Ai<Entity><Action>ReqVO` | `AiChatConversationCreateMyReqVO` |
| 响应 VO | `Ai<Entity>RespVO` | `AiChatConversationRespVO` |
| 分页 VO | `Ai<Entity>PageReqVO` | `AiChatConversationPageReqVO` |

### Patterns

- **DI**: 使用 `@Resource`（非 `@Autowired`）
- **返回值**: Controller 统一返回 `CommonResult<T>`，使用静态导入 `import static ...CommonResult.success;`
- **VO ↔ DO**: 使用 `BeanUtils.toBean()` / `BeanUtils.toBeanList()`，不使用 MapStruct
- **Swagger**: 所有 Controller 使用 `@Tag(name = "...")`，方法使用 `@Operation(summary = "...")`，VO 字段使用 `@Schema(description = "...")`
- **DO**: 继承 `BaseDO`（自带 createTime, updateTime, creator, updater, deleted 逻辑删除），使用 `@TableName`, `@TableId`, Lombok `@Data @Builder`
- **Mapper**: 继承 `BaseMapperX<T>`，查询方法以 `select` 开头，分页使用 `selectPage(pageReqVO, wrapper)` + `LambdaQueryWrapperX`
- **Service 方法**: 创建用 `create`，更新用 `update`，删除用 `delete`，查询单个用 `get`，查询列表用 `getList`，分页用 `getPage`，校验用 `validate`

### AI Model 集成

- 新增平台：在 `framework/ai/core/model/<platform>/` 下创建适配实现
- 平台枚举：`AiPlatformEnum`
- 工厂入口：`AiModelFactory` — 管理 `ChatModel`、`EmbeddingModel`、`VectorStore` 的创建和缓存
- Tool Calling：`tool/function/` 用 `Function<>` 接口，`tool/method/` 用 `@Tool` 注解

### Config

- 端口：48090
- 数据库：MySQL（`data-ai`），dynamic-datasource 支持多数据源
- 缓存：Redis
- 禁用 Qdrant/Milvus 自动配置，通过 `AiModelFactory.getOrCreateVectorStore()` 手动创建

## Build & Run

```bash
mvn compile                    # 编译（含 Lombok + MapStruct annotation processor）
mvn spring-boot:run            # 启动
mvn package                    # 打包
```

**注意**: 项目当前没有测试代码（`src/test/` 为空）。

## CodeGraph

本项目配置了 CodeGraph MCP 服务器，查询符号、调用关系、代码结构时优先使用 `codegraph_*` 工具，不要用 grep/read 重复验证 codegraph 的结果。
