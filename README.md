# data-ai

> 一个基于 **Spring Boot 3 + Spring AI** 的企业级 AI 应用后端服务，统一接入国内外主流大模型，内建知识库（RAG）、工具调用（Function Calling / MCP）与多媒体生成能力。

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-green.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

---

## 📖 目录

- [特性概览](#-特性概览)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [快速开始](#-快速开始)
- [配置说明](#-配置说明)
- [目录结构](#-目录结构)
- [支持的 AI 平台](#-支持的-ai-平台)
- [开发规范](#-开发规范)
- [相关文档](#-相关文档)
- [License](#-license)

---

## ✨ 特性概览

- 🤖 **多模型聊天**：统一接入 **20+ 种** 国内外主流大模型（通义千问、DeepSeek、文心一言、智谱、讯飞星火、豆包、混元、OpenAI、Claude、Gemini 等）
- 📚 **知识库 (RAG)**：文档上传解析 → 段落切分 → 向量化 → 向量检索，支持 **Qdrant / Redis / Milvus** 三种向量库
- 🛠️ **工具调用**：Spring AI Function Calling，同时支持 **MCP Server** 与 **MCP Client**
- 🎨 **多媒体生成**：图片（StableDiffusion、Midjourney）、音乐（Suno）
- 🌊 **流式响应**：基于 WebFlux 的 `Flux` 实现 SSE 流式输出
- 🗄️ **数据层能力**：MyBatis Plus + Join + 多数据源 + 逻辑删除 + p6spy SQL 代理
- 📐 **分层清晰**：Controller / Service / DAL 严格分层，易于扩展与维护

---

## 🔧 技术栈

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
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 6.x+ |
| 向量库 | Qdrant / Redis / Milvus | - |
| 响应式 | Spring WebFlux | - |
| 工具 | Lombok / MapStruct / Hutool / Guava | - |
| 文档 | springdoc-openapi | - |

完整依赖见 [pom.xml](pom.xml)。

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller 层（REST API）                 │
│      AiChatController / AiKnowledgeController / ...         │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Service 层（业务逻辑）                     │
│   ChatConversation / KnowledgeDocument / Model / Tool        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Framework 层（AI 框架）                    │
│    AiAutoConfiguration │ 模型工厂 │ WebSearch │ MCP          │
└──────┬──────────────────┬──────────────────┬────────────────┘
       │                  │                  │
┌──────▼──────┐   ┌───────▼──────┐   ┌───────▼──────┐
│  Spring AI  │   │   向量存储    │   │  DAL (MP)    │
│  20+ 模型   │   │ Qdrant/Redis │   │  MySQL       │
│             │   │    Milvus    │   │              │
└─────────────┘   └──────────────┘   └──────────────┘
```

架构设计详情见 [.qoder/repowiki/zh/content/架构设计](./.qoder/repowiki/zh/content/架构设计)。

---

## 🚀 快速开始

### 1. 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.x**
- **Redis 6.x+**
- （可选）Qdrant / Milvus 向量库

### 2. 准备数据库

```sql
CREATE DATABASE `data-ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> 建表 SQL 请根据 [.qoder/repowiki/zh/content/数据访问层设计.md](./.qoder/repowiki/zh/content/数据访问层设计.md) 自行准备。

### 3. 配置 API Key

编辑 [src/main/resources/application.yml](src/main/resources/application.yml)，替换 `spring.ai.*` 和 `boss.ai.*` 下的 `api-key` 为你自己的密钥。

### 4. 编译与启动

```bash
# 克隆项目
git clone <repo-url>
cd data-ai

# 编译
mvn clean compile

# 开发运行
mvn spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/data-ai-2026.04-SNAPSHOT.jar
```

### 5. 访问服务

| 入口 | 地址 |
| --- | --- |
| 服务端口 | `http://localhost:48090` |
| API 文档 (Swagger) | `http://localhost:48090/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:48090/v3/api-docs` |

---

## ⚙️ 配置说明

[application.yml](src/main/resources/application.yml) 采用 `---` 分段组织：

### 基础段

```yaml
server:
  port: 48090

spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/data-ai?...
          username: root
          password: 123456
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```

### AI 模型段

```yaml
spring:
  ai:
    deepseek:
      api-key: sk-xxxx
    openai:
      api-key: sk-xxxx
      base-url: https://api.gptsapi.net
    dashscope:   # 通义千问
      api-key: sk-xxxx
    # ... 其他平台

boss:
  ai:
    gemini:
      enable: true
      api-key: xxxx
      model: gemini-2.5-flash
    doubao:
      enable: true
      api-key: xxxx
    # ... 其他自研平台配置
```

### 向量库段

```yaml
spring:
  ai:
    vectorstore:
      redis:
        initialize-schema: true
        index-name: knowledge_index
      qdrant:
        host: 127.0.0.1
        port: 6334
        collection-name: knowledge_segment
      milvus:
        client:
          host: 127.0.0.1
          port: 19530
```

> **注意**：默认禁用了 Qdrant、Milvus 的自动装配（通过 `spring.autoconfigure.exclude`），如需启用请手动调整。

---

## 📁 目录结构

```
data-ai
├── src/main/java/cn/boss/data/ai
│   ├── BootstrapApplication.java         # 启动类
│   ├── controller/                       # HTTP 接入层
│   │   ├── chat/                         #   对话与消息
│   │   ├── knowledge/                    #   知识库管理
│   │   └── model/                        #   模型 / Key / 角色 / 工具
│   ├── service/                          # 业务逻辑层
│   │   ├── chat/
│   │   ├── knowledge/
│   │   └── model/
│   ├── dal/                              # 数据访问层
│   │   ├── dataobject/                   #   数据库实体 DO
│   │   └── mysql/                        #   MyBatis Plus Mapper
│   ├── enums/                            # 枚举 + 错误码
│   ├── framework/                        # 基础框架
│   │   ├── ai/                           #   AI 核心（装配/工厂/WebSearch）
│   │   ├── common/                       #   通用基础（异常/校验/工具）
│   │   └── mybatis/core/                 #   MyBatis 扩展
│   ├── tool/                             # AI 工具（Function / Method）
│   └── util/                             # 业务工具
├── src/main/resources
│   ├── application.yml                   # 主配置
│   ├── logback-spring.xml                # 日志配置
│   └── spy.properties                    # p6spy SQL 代理配置
├── .qoder/repowiki/zh/content            # 项目 wiki 文档
├── AGENTS.md                             # AI Agent 协作指引
├── CLAUDE.md                             # Claude Code 协作指引
├── pom.xml
└── README.md
```

---

## 🌐 支持的 AI 平台

### 国内平台

| 平台 | 厂商 | 模型类型 |
| --- | --- | --- |
| 通义千问 (TongYi) | 阿里云 | 对话 / 向量 / Rerank |
| 文心一言 (YiYan) | 百度 | 对话 |
| DeepSeek | DeepSeek | 对话 |
| 智谱 (ZhiPu) | 智谱 AI | 对话 |
| 星火 (XingHuo) | 讯飞 | 对话 |
| 豆包 (DouBao) | 字节跳动 | 对话 |
| 混元 (HunYuan) | 腾讯 | 对话 |
| 硅基流动 (SiliconFlow) | 硅基流动 | 对话 |
| MiniMax | 稀宇科技 | 对话 |
| Moonshot | 月之暗面 (KIMI) | 对话 |
| 百川 (BaiChuan) | 百川智能 | 对话 |

### 国外平台

| 平台 | 厂商 | 模型类型 |
| --- | --- | --- |
| OpenAI | OpenAI | 对话 / 向量 / 图片 |
| Azure OpenAI | 微软 | 对话 / 向量 |
| Anthropic | Anthropic | 对话 (Claude) |
| Gemini | Google | 对话 |
| Ollama | Ollama | 本地对话 |
| StableDiffusion | Stability AI | 图片 |
| Midjourney | Midjourney | 图片 |
| Suno | Suno AI | 音乐 |
| Grok | xAI | 对话 |

新增平台时请在 [AiPlatformEnum.java](src/main/java/cn/boss/data/ai/enums/model/AiPlatformEnum.java) 注册。

---

## 📏 开发规范

### 分层约定

- **Controller**：仅做参数/权限校验与 Service 调用，返回 `CommonResult<T>`
- **Service**：接口 + 实现分离（`XxxService` / `XxxServiceImpl`），事务注解放在实现类
- **DAL**：DO 继承 `BaseDO`，Mapper 继承 `BaseMapperX`
- **VO**：按业务子域组织，使用 **MapStruct** 进行 DO ↔ VO 转换

### 命名规范

- 业务类统一 `Ai` 前缀（如 `AiChatMessageService`），与表名 `ai_xxx` 对齐
- 枚举实现 `ArrayValuable<T>` 便于校验
- 业务错误码集中在 `enums/ErrorCodeConstants.java`

### 代码风格

- 优先使用 Lombok：`@Data` / `@Builder` / `@AllArgsConstructor` / `@NoArgsConstructor`
- 保留编译参数 `-parameters`（Spring Boot 3.2+ 必需）
- 注释使用中文 JavaDoc，仅在意图不明显处补充
- 流式接口返回 `Flux<...>`，**禁止**强行阻塞为 List

更多约定见 [AGENTS.md](AGENTS.md) 与 [CLAUDE.md](CLAUDE.md)。

---

## 📚 相关文档

| 文档 | 说明 |
| --- | --- |
| [.qoder/repowiki/zh/content/项目概述.md](./.qoder/repowiki/zh/content/项目概述.md) | 项目整体介绍 |
| [.qoder/repowiki/zh/content/快速开始.md](./.qoder/repowiki/zh/content/快速开始.md) | 快速开始指南 |
| [.qoder/repowiki/zh/content/架构设计](./.qoder/repowiki/zh/content/架构设计) | 架构设计与组件交互 |
| [.qoder/repowiki/zh/content/AI模型管理](./.qoder/repowiki/zh/content/AI模型管理) | 模型工厂、API 密钥、平台集成 |
| [.qoder/repowiki/zh/content/聊天对话系统](./.qoder/repowiki/zh/content/聊天对话系统) | 对话管理、消息处理 |
| [.qoder/repowiki/zh/content/知识库管理系统](./.qoder/repowiki/zh/content/知识库管理系统) | 文档管理、段落处理、向量检索 |
| [.qoder/repowiki/zh/content/API接口文档.md](./.qoder/repowiki/zh/content/API接口文档.md) | REST API 说明 |
| [.qoder/repowiki/zh/content/配置指南.md](./.qoder/repowiki/zh/content/配置指南.md) | 配置项详解 |
| [.qoder/repowiki/zh/content/部署指南.md](./.qoder/repowiki/zh/content/部署指南.md) | 部署与上线 |
| [.qoder/repowiki/zh/content/故障排除.md](./.qoder/repowiki/zh/content/故障排除.md) | 常见问题 |

---

## 📄 License

本项目基于 MIT License 开源。
