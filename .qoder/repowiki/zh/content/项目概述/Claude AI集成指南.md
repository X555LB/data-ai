# Claude AI集成指南

<cite>
**本文档引用的文件**
- [AGENTS.md](file://AGENTS.md)
- [AiAutoConfiguration.java](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiAutoConfiguration.java)
- [AiProperties.java](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiProperties.java)
- [AiModelFactory.java](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactory.java)
- [AiModelFactoryImpl.java](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java)
- [AiPlatformEnum.java](file://src/main/java/cn/boss/data/ai/enums/model/AiPlatformEnum.java)
- [application.yml](file://src/main/resources/application.yml)
- [AiModelServiceImpl.java](file://src/main/java/cn/boss/data/ai/service/model/AiModelServiceImpl.java)
- [AiUtils.java](file://src/main/java/cn/boss/data/ai/util/AiUtils.java)
- [AiWebSearchClient.java](file://src/main/java/cn/boss/data/ai/framework/ai/core/websearch/AiWebSearchClient.java)
- [AiBoChaWebSearchClient.java](file://src/main/java/cn/boss/data/ai/framework/ai/core/websearch/bocha/AiBoChaWebSearchClient.java)
</cite>

## 更新摘要
**所做更改**
- 删除了与 Claude 平台集成相关的所有内容和引用
- 移除了 CLAUDE.md 文档文件的引用
- 更新了项目架构说明，反映 Claude 平台的完全移除
- 调整了平台支持列表，移除 Claude 相关配置
- 更新了故障排除指南中的平台相关问题

## 目录
1. [简介](#简介)
2. [项目架构](#项目架构)
3. [核心组件](#核心组件)
4. [架构概览](#架构概览)
5. [详细组件分析](#详细组件分析)
6. [依赖关系分析](#依赖关系分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)

## 简介

这是一个基于Spring Boot 3.5 + Spring AI 1.1构建的AI平台后端系统，支持多模型厂商集成，包括DeepSeek、OpenAI、Ollama等。该项目采用模块化设计，提供完整的AI对话、知识库管理和模型管理功能。

**章节来源**
- [AGENTS.md:5-7](file://AGENTS.md#L5-L7)

## 项目架构

### 技术栈

项目采用现代化的技术栈构建：

- **运行环境**: Java 17, Spring Boot 3.5.9, Maven
- **ORM框架**: MyBatis Plus 3.5.8 + dynamic-datasource
- **AI框架**: Spring AI 1.1.2 + 自定义多平台适配层
- **向量存储**: Qdrant / Milvus（手动创建，禁用自动配置）
- **缓存系统**: Redis + spring-boot-starter-cache
- **工具库**: Lombok, MapStruct 1.6, Hutool 5.8, Swagger/OpenAPI 3

### 模块化架构

```mermaid
graph TB
subgraph "应用层"
Controller[控制器层]
Service[服务层]
DAL[数据访问层]
end
subgraph "AI适配层"
Factory[模型工厂]
Config[配置管理]
WebSearch[网络搜索]
end
subgraph "基础设施"
Database[(MySQL数据库)]
Redis[(Redis缓存)]
VectorStore[(向量存储)]
end
Controller --> Service
Service --> Factory
Factory --> Database
Factory --> Redis
Factory --> VectorStore
WebSearch --> Controller
```

**图表来源**
- [AGENTS.md:9-11](file://AGENTS.md#L9-L11)

### 核心模块

| 模块 | 路径 | 功能描述 |
|------|------|----------|
| chat | controller/chat, service/chat, dal/.../chat | AI对话（会话 + 消息） |
| knowledge | controller/knowledge, service/knowledge, dal/.../knowledge | 知识库（库 + 文档 + 分段 + 向量检索） |
| model | controller/model, service/model, dal/.../model | 模型管理（API Key、模型、角色、工具） |

**章节来源**
- [AGENTS.md:7-7](file://AGENTS.md#L7-L7)

## 核心组件

### AI模型工厂

AI模型工厂是整个系统的核心组件，负责统一管理不同AI平台的模型实例。

```mermaid
classDiagram
class AiModelFactory {
<<interface>>
+getOrCreateChatModel(platform, apiKey, url) ChatModel
+getDefaultChatModel(platform) ChatModel
+getOrCreateEmbeddingModel(platform, apiKey, url, model) EmbeddingModel
+getOrCreateVectorStore(type, embeddingModel, metadataFields) VectorStore
}
class AiModelFactoryImpl {
-buildClientCacheKey(clazz, params) String
-buildTongYiChatModel(key) DashScopeChatModel
-buildDeepSeekChatModel(apiKey) DeepSeekChatModel
-buildOpenAiChatModel(token, url) OpenAiChatModel
-buildOllamaChatModel(url) OllamaChatModel
-buildSimpleVectorStore(embeddingModel) SimpleVectorStore
-buildQdrantVectorStore(embeddingModel) QdrantVectorStore
-buildRedisVectorStore(embeddingModel, metadataFields) RedisVectorStore
}
AiModelFactory <|.. AiModelFactoryImpl
AiModelFactoryImpl --> AiPlatformEnum : "使用"
```

**图表来源**
- [AiModelFactory.java:1-63](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactory.java#L1-L63)
- [AiModelFactoryImpl.java:1-324](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java#L1-L324)

### AI平台枚举

系统支持多个AI平台，通过枚举统一管理：

| 平台 | 平台标识 | 名称 |
|------|----------|------|
| TongYi | "TongYi" | 通义千问 |
| DeepSeek | "DeepSeek" | DeepSeek |
| OpenAI | "OpenAI" | OpenAI |
| Ollama | "Ollama" | Ollama |

**章节来源**
- [AiPlatformEnum.java:1-54](file://src/main/java/cn/boss/data/ai/enums/model/AiPlatformEnum.java#L1-L54)

## 架构概览

### 配置管理架构

```mermaid
graph LR
subgraph "配置层"
YML[application.yml]
Properties[AiProperties]
AutoConfig[AiAutoConfiguration]
end
subgraph "AI服务层"
Factory[AiModelFactoryImpl]
ChatModel[ChatModel实例]
EmbeddingModel[EmbeddingModel实例]
VectorStore[VectorStore实例]
end
subgraph "外部服务"
OpenAI[OpenAI API]
Ollama[Ollama API]
Qdrant[Qdrant向量库]
Redis[Redis缓存]
end
YML --> Properties
Properties --> AutoConfig
AutoConfig --> Factory
Factory --> ChatModel
Factory --> EmbeddingModel
Factory --> VectorStore
ChatModel --> OpenAI
ChatModel --> Ollama
VectorStore --> Qdrant
VectorStore --> Redis
```

**图表来源**
- [AiAutoConfiguration.java:1-70](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiAutoConfiguration.java#L1-L70)
- [AiProperties.java:1-25](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiProperties.java#L1-L25)
- [application.yml:79-139](file://src/main/resources/application.yml#L79-L139)

### 模型创建流程

```mermaid
sequenceDiagram
participant Client as 客户端
participant Service as AiModelServiceImpl
participant Factory as AiModelFactoryImpl
participant Platform as AI平台
participant Cache as 缓存
Client->>Service : 获取ChatModel
Service->>Service : 验证模型和API Key
Service->>Factory : getOrCreateChatModel(platform, apiKey, url)
Factory->>Cache : 检查缓存
alt 缓存存在
Cache-->>Factory : 返回现有实例
else 缓存不存在
Factory->>Platform : 创建新实例
Platform-->>Factory : 返回实例
Factory->>Cache : 存储到缓存
end
Factory-->>Service : 返回ChatModel
Service-->>Client : 返回ChatModel
```

**图表来源**
- [AiModelServiceImpl.java:109-116](file://src/main/java/cn/boss/data/ai/service/model/AiModelServiceImpl.java#L109-L116)
- [AiModelFactoryImpl.java:74-91](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java#L74-L91)

**章节来源**
- [AiAutoConfiguration.java:34-37](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiAutoConfiguration.java#L34-L37)
- [AiModelFactoryImpl.java:71-145](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java#L71-L145)

## 详细组件分析

### AI配置管理

AI配置通过Spring Boot的配置属性机制实现，支持动态配置和运行时修改。

```mermaid
classDiagram
class AiProperties {
-WebSearch webSearch
+getWebSearch() WebSearch
+setWebSearch(webSearch) void
}
class WebSearch {
-boolean enable
-String apiKey
+isEnabled() boolean
+getApiKey() String
+isEnable() boolean
+setEnable(enable) void
+setApiKey(apiKey) void
}
class AiAutoConfiguration {
+aiModelFactory() AiModelFactory
+observationRegistry() ObservationRegistry
+tokenCountEstimator() TokenCountEstimator
+batchingStrategy() BatchingStrategy
+webSearchClient(aiProperties) AiWebSearchClient
}
AiProperties --> WebSearch : "包含"
AiAutoConfiguration --> AiProperties : "使用"
```

**图表来源**
- [AiProperties.java:1-25](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiProperties.java#L1-L25)
- [AiAutoConfiguration.java:1-70](file://src/main/java/cn/boss/data/ai/framework/ai/config/AiAutoConfiguration.java#L1-L70)

### 网络搜索功能

系统集成了博查网络搜索功能，支持AI助手的在线信息检索能力。

```mermaid
sequenceDiagram
participant Client as 客户端
participant WebSearch as AiWebSearchClient
participant BoCha as AiBoChaWebSearchClient
participant API as 博查API
Client->>WebSearch : search(request)
WebSearch->>BoCha : 处理搜索请求
BoCha->>API : POST /v1/web-search
API-->>BoCha : 返回搜索结果
BoCha->>BoCha : 转换响应格式
BoCha-->>WebSearch : 返回AiWebSearchResponse
WebSearch-->>Client : 返回搜索结果
```

**图表来源**
- [AiWebSearchClient.java:1-17](file://src/main/java/cn/boss/data/ai/framework/ai/core/websearch/AiWebSearchClient.java#L1-L17)
- [AiBoChaWebSearchClient.java:54-86](file://src/main/java/cn/boss/data/ai/framework/ai/core/websearch/bocha/AiBoChaWebSearchClient.java#L54-L86)

### AI工具函数

AiUtils提供了跨平台的AI工具函数，统一处理不同AI平台的消息格式和选项配置。

```mermaid
flowchart TD
Start([开始]) --> GetPlatform[获取AI平台]
GetPlatform --> CheckPlatform{检查平台类型}
CheckPlatform --> |TongYi| BuildTongYi[构建通义千问选项]
CheckPlatform --> |DeepSeek| BuildDeepSeek[构建DeepSeek选项]
CheckPlatform --> |OpenAI| BuildOpenAI[构建OpenAI选项]
CheckPlatform --> |Ollama| BuildOllama[构建Ollama选项]
BuildTongYi --> SetThinking[设置思维模式]
BuildDeepSeek --> SetModel[设置模型]
BuildOpenAI --> SetModel
BuildOllama --> SetNumPredict[设置预测数量]
SetThinking --> AddTools[添加工具回调]
SetModel --> AddTools
SetNumPredict --> AddTools
AddTools --> AddContext[添加工具上下文]
AddContext --> ReturnOptions[返回ChatOptions]
ReturnOptions --> End([结束])
```

**图表来源**
- [AiUtils.java:23-49](file://src/main/java/cn/boss/data/ai/util/AiUtils.java#L23-L49)

**章节来源**
- [AiUtils.java:1-52](file://src/main/java/cn/boss/data/ai/util/AiUtils.java#L1-L52)

## 依赖关系分析

### 外部依赖关系

```mermaid
graph TB
subgraph "Spring AI生态系统"
SpringAI[Spring AI Core]
OpenAI[OpenAI AutoConfiguration]
DeepSeek[DeepSeek AutoConfiguration]
DashScope[DashScope AutoConfiguration]
Ollama[Ollama AutoConfiguration]
end
subgraph "向量存储"
Qdrant[Qdrant VectorStore]
RedisVS[Redis VectorStore]
SimpleVS[Simple VectorStore]
end
subgraph "监控和观测"
Micrometer[Micrometer Observation]
TokenCounter[JTokkit Token计数]
end
SpringAI --> OpenAI
SpringAI --> DeepSeek
SpringAI --> DashScope
SpringAI --> Ollama
SpringAI --> Qdrant
SpringAI --> RedisVS
SpringAI --> SimpleVS
SpringAI --> Micrometer
SpringAI --> TokenCounter
```

**图表来源**
- [AiModelFactoryImpl.java:11-53](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java#L11-L53)

### 内部组件依赖

系统内部组件遵循清晰的依赖层次结构：

```mermaid
graph TD
subgraph "表现层"
Controller[控制器]
VO[值对象]
end
subgraph "业务层"
Service[服务实现]
BO[业务对象]
end
subgraph "数据访问层"
Mapper[映射器]
DO[数据对象]
end
subgraph "基础设施层"
Factory[模型工厂]
Utils[工具类]
Enum[枚举]
end
Controller --> Service
Service --> Mapper
Mapper --> DO
Service --> Factory
Factory --> Utils
VO --> Enum
DO --> Enum
```

**图表来源**
- [AGENTS.md:7-7](file://AGENTS.md#L7-L7)

**章节来源**
- [AGENTS.md:7-7](file://AGENTS.md#L7-L7)

## 性能考虑

### 缓存策略

系统实现了多层次的缓存机制来提升性能：

1. **模型实例缓存**: 使用Hutool的Singleton实现，避免重复创建AI模型实例
2. **向量存储持久化**: SimpleVectorStore每分钟自动保存到文件系统
3. **连接池优化**: Redis使用JedisPooled连接池管理

### 性能优化建议

1. **合理配置向量存储**: 根据数据量选择合适的向量存储方案
2. **监控指标收集**: 利用Micrometer收集AI模型调用性能指标
3. **批量处理**: 使用BatchingStrategy优化嵌入模型的批量处理

## 故障排除指南

### 常见问题及解决方案

| 问题类型 | 症状 | 可能原因 | 解决方案 |
|----------|------|----------|----------|
| 模型加载失败 | IllegalArgumentException: 未知平台 | 平台配置错误 | 检查AiPlatformEnum配置 |
| API密钥无效 | 认证失败 | 密钥过期或格式错误 | 更新API密钥配置 |
| 向量存储连接失败 | 连接超时 | 服务不可达 | 检查向量存储服务状态 |
| 缓存异常 | 序列化失败 | 缓存数据损坏 | 清理缓存文件 |

### 调试建议

1. **启用详细日志**: 在application.yml中调整日志级别
2. **监控AI调用**: 使用ObservationRegistry收集调用指标
3. **检查配置**: 确认所有必需的配置项都已正确设置

**章节来源**
- [AiModelFactoryImpl.java:87-89](file://src/main/java/cn/boss/data/ai/framework/ai/core/model/AiModelFactoryImpl.java#L87-L89)
- [application.yml:58-62](file://src/main/resources/application.yml#L58-L62)

## 结论

本项目提供了一个完整且可扩展的AI平台后端解决方案，具有以下特点：

1. **模块化设计**: 清晰的分层架构和模块划分
2. **多平台支持**: 统一的适配层支持多个AI平台
3. **高性能**: 多层次缓存和优化的资源管理
4. **易扩展**: 插件化的架构便于添加新的AI平台
5. **生产就绪**: 完善的配置管理和监控机制

该系统为构建企业级AI应用提供了坚实的基础，开发者可以根据具体需求进行定制和扩展。

**更新说明**: 本指南已更新以反映 Claude 平台集成的完全移除。项目现已专注于 DeepSeek、OpenAI 和 Ollama 等平台的支持，并移除了所有与 Claude 相关的配置和代码引用。