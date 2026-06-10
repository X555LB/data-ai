# AGENTS.md

本文件为所有 AI 编码助手（Claude Code、Cursor、Qoder、Codex、Cline 等）在本仓库中协作时提供统一的项目指引与行为约束。

## 1. 项目概述

**data-ai** 是一个基于 **Spring Boot 3 + Spring AI** 的企业级 AI 应用后端服务。核心能力：多模型聊天对话（通义千问、DeepSeek、OpenAI、Ollama）、知识库 RAG（Qdrant / Redis / Milvus 三选一）、工具调用（Function Calling + MCP）。

## 2. 技术栈

Java 17 / Spring Boot 3.5.9 / Spring AI 1.1.2 / Spring AI Alibaba 1.1.0.0-RC2 / MyBatis Plus 3.5.8 + Join 1.5.3 + dynamic-datasource 4.3.1 + p6spy 3.9.1 / MySQL + Redis / Qdrant + Redis + Milvus / Spring WebFlux（Flux 流式）/ Lombok 1.18.42 + Hutool 5.8.32 + Guava。完整依赖见 [pom.xml](pom.xml)。

## 3. 常用命令

```bash
mvn clean compile          # 编译
mvn clean package -DskipTests  # 打包
mvn spring-boot:run        # 运行
java -jar target/data-ai-2026.04-SNAPSHOT.jar  # 启动产物
```

默认端口 `48090`。Maven 仓库：华为云 → 阿里云 → Spring Milestones → Spring Snapshots。

## 4. 运行前准备

1. **MySQL** `127.0.0.1:3306`，库名 `data-ai`，账号 `root/123456`
2. **Redis** `127.0.0.1:6379`
3. 可选向量库：Qdrant `127.0.0.1:6334`、Milvus `127.0.0.1:19530`（[application.yml](src/main/resources/application.yml) 默认禁用自动装配，按需启用）
4. AI 平台 API Key 在 [application.yml](src/main/resources/application.yml) 的 `spring.ai.*` 与 `boss.ai.*` 段内按需替换

## 5. 包结构

启动类 [BootstrapApplication.java](src/main/java/cn/boss/data/ai/BootstrapApplication.java)，根包 `cn.boss.data.ai`。模块域：chat（AI 对话）、knowledge（知识库）、model（模型管理）。

```
cn.boss.data.ai
├── controller/        HTTP 接入层，按 chat / knowledge / model 分域；vo/ 存放请求响应 VO
├── service/           业务逻辑层（接口 + Impl 分离），bo/ 存放业务对象
├── dal/               dataobject/（DO）+ mysql/（Mapper，@MapperScan）
├── enums/             枚举与 ErrorCodeConstants
├── framework/         ai/（模型工厂 + 配置）| common/（异常 + 工具）| mybatis/core/（BaseDO + BaseMapperX）
├── tool/              function/（Bean 风格）+ method/（@Tool 风格）
└── util/              业务工具
```

## 6. 架构与编码约定

### 6.1 分层约定

- **Controller**：只做参数校验，调用 Service，返回 `CommonResult<T>`（静态导入 `import static ...CommonResult.success;`）。不写业务逻辑。
- **DI**：使用 `@Resource`（非 `@Autowired`）。
- **Service**：接口 + 实现分离，事务注解写在实现类上。方法命名：create / update / delete / get / getList / getPage / validate。
- **DAL**：DO 继承 `BaseDO`（自带 createTime, updateTime, creator, updater, deleted），用 `@TableName` + `@TableId` + `@Data @Builder`；Mapper 继承 `BaseMapperX<T>`，查询以 `select` 开头，分页用 `selectPage` + `LambdaQueryWrapperX`。
- **VO**：放 `controller/<domain>/vo/<sub>/`，用 `BeanUtils.toBean()` / `BeanUtils.toBeanList()` 做 DO ↔ VO 转换。
- **枚举**：实现 `ArrayValuable<T>`；错误码集中到 `ErrorCodeConstants.java`。
- **异常**：使用 `framework/common/exception` + `ErrorCodeConstants`，禁止直接抛 `RuntimeException`。

### 6.2 AI 模型设计要点

- 新增平台在 `AiPlatformEnum` 登记。模型类型：CHAT / IMAGE / VOICE / VIDEO / EMBEDDING / RERANK。
- 装配入口 `AiAutoConfiguration`；`boss.ai.*` 由 `AiProperties` 承接。模型工厂 `AiModelFactory` 按平台动态创建 + 缓存。
- **流式响应**返回 `Flux<...>`，**禁止**阻塞为 List。MCP 默认关闭，按需开启。

### 6.3 持久化约定

逻辑删除 `deleted=1`；多数据源用 `@DS("xxx")` 切换；SQL 代理 p6spy（[spy.properties](src/main/resources/spy.properties)）；MyBatis Plus Join `table-alias=t`，`logic-del-type=on`。

### 6.4 命名 & 代码风格

- 业务前缀 `Ai`（如 `AiChatMessageService`），表名 `ai_xxx`。Lombok 优先 `@Data @Builder @AllArgsConstructor @NoArgsConstructor`。编译参数 `-parameters` 不要移除。中文 JavaDoc 注释。
- 命名规则：DO `Ai<Entity>DO` / Mapper `Ai<Entity>Mapper` / Service `Ai<Entity>Service` / Impl `Ai<Entity>ServiceImpl` / Controller `Ai<Entity>Controller` / 请求 VO `Ai<Entity><Action>ReqVO` / 响应 VO `Ai<Entity>RespVO` / 分页 VO `Ai<Entity>PageReqVO`。

## 7. Agent 行为约束

所有 AI 助手严格遵守：

1. **最小变更**：只改用户要求的内容，不顺带重构、不补充抽象、不大面积格式化。
2. **先读后改**：修改前必须读取现有内容，不基于猜测编辑。
3. **不新增依赖**：优先复用 Hutool / Guava / Spring AI；确需新增需说明理由。
4. **不生成文档**：不主动创建 `*.md`、README 等（用户显式要求除外）。
5. **不运行危险命令**：禁止 `rm -rf`、`push --force`、`reset --hard`、`--no-verify`；不直接提交代码。
6. **流程定义表只读**：Activiti/Flowable 的 `act_re_procdef`、`act_re_model`、`act_re_deployment` 禁止清理/删除。
7. **配置安全**：不把真实密钥提交到 git；占位值不替换为真实值。
8. **中文交流**。

## 8. 项目知识库

设计文档位于 [.qoder/repowiki/zh/content](.qoder/repowiki/zh/content)，涵盖架构设计、AI 模型管理、聊天对话、知识库、工具函数、数据访问层等。**深入改动某模块前，优先阅读对应 wiki。** 项目当前无测试代码（`src/test/` 为空）。
