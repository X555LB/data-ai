# AGENTS.md

本文件为所有 AI 编码助手（Claude Code、Cursor、Qoder、Codex、Cline 等）在本仓库中协作时提供统一的项目指引与行为约束。

## 1. 项目概述

**data-ai** 是一个基于 **Spring Boot 3 + Spring AI** 的企业级 AI 应用后端服务。

核心能力：

- **多模型聊天对话**：统一接入通义千问、文心一言、DeepSeek、智谱、讯飞星火、豆包、混元、硅基流动、MiniMax、Moonshot、百川、OpenAI、Azure OpenAI、Anthropic、Gemini、Ollama、Grok 等国内外主流大模型
- **知识库（RAG）**：文档上传解析、段落切分、向量化、向量检索（Qdrant / Redis / Milvus 三选一）
- **工具调用**：Spring AI Function Calling + MCP Server / Client
- **多媒体生成**：图片（StableDiffusion、Midjourney）、音乐（Suno）

## 2. 技术栈

| 类别 | 组件 | 版本 |
| --- | --- | --- |
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.5.9 |
| AI | Spring AI | 1.1.2 |
| AI | Spring AI Alibaba | 1.1.0.0-RC2 |
| 持久层 | MyBatis Plus | 3.5.8 |
| 持久层 | MyBatis Plus Join | 1.5.3 |
| 持久层 | dynamic-datasource | 4.3.1 |
| 持久层 | p6spy | 3.9.1 |
| 数据库 | MySQL / Redis | - |
| 向量库 | Qdrant / Redis / Milvus | - |
| 响应式 | Spring WebFlux（Flux 流式） | - |
| 工具 | Lombok 1.18.42 / MapStruct 1.6.3 / Hutool 5.8.32 / Guava | - |
| 文档 | springdoc-openapi + swagger-annotations 2.2.25 | - |

完整依赖见 [pom.xml](pom.xml)。

## 3. 常用命令

本项目使用本地 Maven（未附带 mvnw）：

```bash
# 编译
mvn clean compile

# 打包（生成 fat jar）
mvn clean package -DskipTests

# 开发运行
mvn spring-boot:run

# 启动打包产物
java -jar target/data-ai-2026.04-SNAPSHOT.jar
```

默认端口：`48090`；Swagger UI：`http://localhost:48090/swagger-ui.html`。

Maven 仓库优先顺序（见 [pom.xml](pom.xml)）：华为云 → 阿里云 → Spring Milestones → Spring Snapshots。

## 4. 运行前准备

1. **MySQL** `127.0.0.1:3306`，库名 `data-ai`，账号 `root/123456`
2. **Redis** `127.0.0.1:6379`
3. 可选向量库：Qdrant `127.0.0.1:6334`、Milvus `127.0.0.1:19530`
   （[application.yml](src/main/resources/application.yml) 中默认通过 `spring.autoconfigure.exclude` 禁用 Qdrant/Milvus 自动装配，按需启用）
4. 各 AI 平台 API Key 在 [application.yml](src/main/resources/application.yml) 的 `spring.ai.*` 与 `boss.ai.*` 段内按需替换

## 5. 包结构

启动类：[BootstrapApplication.java](src/main/java/cn/boss/data/ai/BootstrapApplication.java)，根包 `cn.boss.data.ai`。

```
cn.boss.data.ai
├── controller/        HTTP 接入层，按 chat / knowledge / model 分域；子目录 vo/ 存放请求响应 VO
├── service/           业务逻辑层（接口 + Impl 分离），子目录 bo/ 存放业务对象
├── dal/
│   ├── dataobject/    数据库实体 DO
│   └── mysql/         MyBatis Plus Mapper（@MapperScan 扫描此包）
├── enums/             枚举与 ErrorCodeConstants（含 model/ 子目录）
├── framework/
│   ├── ai/            AI 核心：AiAutoConfiguration / AiProperties / 模型工厂 / websearch
│   ├── common/        基础设施：core / enums / exception / pojo / util / validation
│   └── mybatis/core/  MyBatis 扩展：BaseDO / BaseMapperX / 类型处理器 / 查询包装器
├── tool/
│   ├── function/      Spring AI Function Bean 风格工具
│   └── method/        @Tool 方法风格工具
└── util/              业务工具（AiUtils、FileTypeUtils）
```

## 6. 架构与编码约定

### 6.1 分层约定

- **Controller**：只做参数校验与权限校验，调用 Service，返回 `CommonResult<T>`（见 `framework.common.pojo`）。不要写业务逻辑。
- **Service**：接口 + 实现分离（`XxxService` / `XxxServiceImpl`），事务注解写在实现类上。
- **DAL**：DO 继承 `BaseDO`；Mapper 继承 `BaseMapperX`。
- **VO**：按业务子域放 `controller/<domain>/vo/<sub>/` 下，使用 **MapStruct** 做 DO ↔ VO 转换。
- **枚举**：实现 `ArrayValuable<T>`；业务错误码集中到 `enums/ErrorCodeConstants.java`。
- **异常**：使用 `framework/common/exception` 下的异常类配合 `ErrorCodeConstants`，禁止直接抛 `RuntimeException`。

### 6.2 AI 模型设计要点

- 新增平台需在 [AiPlatformEnum.java](src/main/java/cn/boss/data/ai/enums/model/AiPlatformEnum.java) 登记。
- 模型类型枚举：`AiModelTypeEnum` = CHAT / IMAGE / VOICE / VIDEO / EMBEDDING / RERANK。
- 统一装配入口：`framework/ai/config/AiAutoConfiguration.java`；`boss.ai.*` 自研配置由 `AiProperties` 承接。
- 模型工厂（`framework/ai/core/model/`）依据 `AiPlatformEnum` 动态选取 Client，支持按 API Key 缓存隔离。
- **流式响应**：聊天接口返回 `Flux<...>`，**禁止**强行阻塞为 List。
- **MCP**：Server/Client 默认关闭（`spring.ai.mcp.*.enabled=false`），按需开启。

### 6.3 持久化约定

- 逻辑删除：`deleted=1` 已删除、`0` 未删除（详见 [application.yml](src/main/resources/application.yml)）。
- 多数据源：默认主库 `master`，通过 `@DS("xxx")` 切换；SQL 代理由 **p6spy** 输出，配置见 [spy.properties](src/main/resources/spy.properties)。
- MyBatis Plus Join 的 `table-alias = t`，`logic-del-type = on`。

### 6.4 命名 & 代码风格

- 业务前缀统一为 `Ai`（如 `AiChatMessageService`、`AiKnowledgeDocumentDO`），与表名 `ai_xxx` 对齐。
- Lombok：优先 `@Data`、`@Builder`、`@AllArgsConstructor` + `@NoArgsConstructor`；与 MapStruct 协同需保留 `lombok-mapstruct-binding`（[pom.xml](pom.xml)）。
- 编译参数 `-parameters` 已开启，**不要移除**。
- 注释使用中文 JavaDoc；只在意图不明显处补充行内注释。

## 7. Agent 行为约束

请所有 AI 助手严格遵守：

1. **最小变更原则**：只改用户明确要求的内容，不做顺带重构、不补充"看起来更好"的抽象、不大面积格式化。
2. **先读后改**：修改任何文件前先读取其现有内容，绝不基于猜测编辑。
3. **不新增依赖**：优先复用 Hutool / Guava / Spring AI 已提供的能力；确需新增需在回复中明确说明理由。
4. **不生成文档**：除非用户显式要求，不要主动创建 `*.md`、`README`、变更记录等文档文件。
5. **不运行危险命令**：禁止 `rm -rf`、`git push --force`、`git reset --hard`、`--no-verify` 等破坏性操作；禁止直接提交代码（除非用户明确要求）。
6. **流程定义表只读**：涉及 Activiti/Flowable 时，`act_re_procdef`、`act_re_model`、`act_re_deployment` **禁止执行清理/删除脚本**，仅允许通过业务流程或官方 API 修改。
7. **配置安全**：不要把真实 API Key、密码提交到 git；示例配置中的密钥已是占位值时不要替换为真实值。
8. **中文交流**：默认以中文响应用户。

## 8. 项目知识库

详细设计文档位于 [.qoder/repowiki/zh/content](.qoder/repowiki/zh/content)：

- 架构设计：整体架构概览 / 设计模式应用 / 组件交互关系 / 技术栈选型
- AI 模型管理：模型工厂设计 / AI 平台集成 / 模型配置管理 / API 密钥管理 / 模型类型与平台配置
- 聊天对话系统：对话管理 / 消息处理
- 知识库管理系统：文档管理 / 段落处理 / 向量检索
- 其他：工具函数系统 / 数据访问层设计 / 框架组件设计 / API 接口文档 / 配置指南 / 部署指南 / 故障排除

**深入改动某个模块前，优先阅读对应的 wiki 文档。**
