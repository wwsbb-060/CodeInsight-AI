# 🔍 CodeInsight AI

> 面向开发者的智能代码评审平台 — 用 AI 降低大型项目的阅读成本。

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://react.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

---

## 📖 目录

- [项目背景](#-项目背景)
- [V0.1 版本目标](#-v01-版本目标)
- [系统架构](#-系统架构)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [API 设计](#-api-设计)
- [数据库设计](#-数据库设计)
- [开发计划](#-开发计划)

---

## 🎯 项目背景

传统代码评审依赖人工阅读，效率低、门槛高。新成员理解一个大型仓库通常需要 **数天甚至数周**。

CodeInsight AI 通过 **AI + 软件工程** 的方式，实现：

| 能力 | 说明 |
|------|------|
| 📥 **自动分析** | 输入 GitHub 仓库地址，自动 Clone 并解析 |
| 🤖 **智能评审** | AI 自动生成项目总结、模块分析、代码质量报告 |
| 💬 **仓库问答** | 基于 RAG 技术，像聊天一样理解代码（规划中） |
| 📊 **可视分析** | 代码统计、质量趋势、Bug 分布一目了然（规划中） |
| 📄 **报告导出** | 支持 Markdown / PDF 报告，方便团队共享 |

---

## 🎯 V0.1 版本目标

> **核心理念：先跑通全链路，再迭代功能。**

V0.1 聚焦 **最核心的用户流程**：注册登录 → 导入仓库 → AI 分析 → 生成报告。

### 功能清单

```
✅ 用户注册 / 登录            JWT 认证
✅ 用户注册 / 登录            JWT + Redis 黑名单
✅ GitHub 仓库导入            填写 URL，系统 git 命令浅克隆
✅ 自动 Clone                 异步后台执行，列表实时轮询状态
✅ AI 项目总结                9 维度结构化评审 + JSON 摘要
✅ Markdown 报告生成          前端渲染、代码高亮、下载导出
✅ 评审历史管理              持久化存储、备注编辑、一键删除
```

### V0.1 用户流程

```
用户注册 → 登录 → 输入 GitHub 仓库 URL
                          ↓
                  后端 Clone 仓库（异步）
                          ↓
              ProjectScanner 解析文件树 + 关键代码
                          ↓
              PromptBuilder 拼装 Prompt → 调用 AI API
                          ↓
            生成 Markdown 报告 + JSON 摘要 → 存储数据库
                          ↓
            前端轮询状态 → 完成后展示报告 / 下载
```

### 不在 V0.1 范围内

- ❌ RAG 仓库问答（V0.2）
- ❌ PDF 报告导出（V0.2）
- ❌ 数据看板 / ECharts（V0.2）
- ❌ 多版本管理（V0.3）
- ❌ 安全漏洞分析（V0.3）
- ❌ 本地项目上传（V0.3）

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────┐
│                   Browser                     │
│            React + TypeScript + Vite          │
│         Ant Design + TanStack Query           │
└──────────────────┬──────────────────────────┘
                   │  HTTP REST API
                   ▼
┌──────────────────────────────────────────────┐
│              Spring Boot 后端                  │
│                                                │
│   ┌──────────┬──────────┬──────────┐          │
│   │ 用户模块  │ 仓库模块  │ 评审模块  │          │
│   └──────────┴──────────┴──────────┘          │
│                   │                            │
│          GitCloneService（系统 git）            │
│                   │                            │
│   ┌───────────────┼───────────────┐            │
│   │ ProjectScanner │ PromptBuilder │            │
│   └───────────────┼───────────────┘            │
│                   │                            │
│            AI Review Engine                    │
│         (LangChain4j + LLM)                   │
│                   │                            │
│         ┌─────────┴─────────┐                 │
│         │  MySQL     Redis   │                 │
│         └───────────────────┘                 │
└──────────────────────────────────────────────┘
```

---

## 🛠 技术栈

### 前端

| 技术 | 用途 |
|------|------|
| React 18 | UI 框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Ant Design 5 | 组件库 |
| React Router 6 | 路由管理 |
| TanStack Query | 服务端状态管理 |
| Axios | HTTP 客户端 |
| React Markdown | Markdown 渲染 |

### 后端

| 技术 | 用途 |
|------|------|
| Spring Boot 3 | 应用框架 |
| MyBatis Plus | ORM |
| MySQL 8 | 关系数据库 |
| Redis 7 | 缓存 + Token 管理 |
| JWT | 用户认证 |
| 系统 Git | 仓库克隆（浅克隆） |
| LangChain4j | AI 编排框架 |
| DeepSeek API | 大语言模型 |

### DevOps

| 技术 | 用途 |
|------|------|
| Docker + Docker Compose | 容器化部署 |
| Nginx | 反向代理 |
| GitHub Actions | CI/CD |

---

## 📁 项目结构

```
CodeInsight-AI/
├── frontend/                    # React 前端项目
│   ├── src/
│   │   ├── api/                 # API 请求封装
│   │   ├── components/          # 公共组件
│   │   ├── pages/               # 页面组件
│   │   │   ├── LoginPage.tsx      # 登录页
│   │   │   ├── RegisterPage.tsx   # 注册页
│   │   │   ├── DashboardPage.tsx  # 工作台（导入 + 列表）
│   │   │   ├── ReportsPage.tsx    # 分析报告（列表 + 摘要 + 备注）
│   │   │   └── ReportPage.tsx     # 报告详情（Markdown + 下载）
│   │   ├── hooks/               # 自定义 Hooks
│   │   ├── router/              # 路由配置
│   │   ├── store/               # 全局状态
│   │   ├── types/               # TypeScript 类型
│   │   └── utils/               # 工具函数
│   ├── package.json
│   └── vite.config.ts
│
├── backend/                     # Spring Boot 后端项目
│   └── src/main/java/com/codeinsight/
│       ├── CodeInsightApplication.java
│       ├── config/              # 配置类（Security、CORS、AI）
│       ├── controller/          # 控制器
│       │   ├── AuthController.java
│       │   ├── RepositoryController.java
│       │   └── ReviewController.java
│       ├── service/             # 业务逻辑
│       │   ├── AuthService.java
│       │   ├── RepositoryService.java
│       │   ├── GitCloneService.java    # 异步 git clone
│       │   ├── ReviewService.java
│       │   ├── AsyncReviewExecutor.java # 异步 AI 评审
│       │   └── ai/              # AI 核心
│       │       ├── AiReviewEngine.java  # 协调者
│       │       ├── ProjectScanner.java  # 文件扫描
│       │       └── PromptBuilder.java   # Prompt 构建
│       ├── mapper/              # MyBatis Mapper
│       ├── entity/              # 数据库实体
│       ├── dto/                 # 数据传输对象
│       ├── common/              # 公共类（Result、Exception）
│       │   └── Result.java      # 统一响应格式
│       └── security/            # JWT 安全相关
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/        # 数据库迁移脚本
│   └── pom.xml
│
├── docker/                      # 部署相关
│   ├── docker-compose.yml
│   ├── nginx.conf
│   └── Dockerfile
│
├── docs/                        # 项目文档
│   ├── architecture.md
│   ├── api.md
│   └── database.md
│
├── .github/workflows/          # CI/CD
│   └── ci.yml
│
├── README.md
└── LICENSE
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| Maven | 3.8+ |
| Docker（可选） | 24+ |

### 方式一：Docker Compose（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/CodeInsight-AI.git
cd CodeInsight-AI

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入 DeepSeek API Key 等配置

# 3. 一键启动
docker-compose -f docker/docker-compose.yml up -d

# 4. 访问
# 前端：http://localhost:3000
# 后端：http://localhost:8080
```

### 方式二：本地开发

#### 后端

```bash
cd backend

# 创建数据库
mysql -u root -p -e "CREATE DATABASE codeinsight DEFAULT CHARACTER SET utf8mb4;"

# 修改 src/main/resources/application.yml 中的数据库连接和 API Key

# 启动
mvn spring-boot:run
```

#### 前端

```bash
cd frontend

npm install
npm run dev

# 访问 http://localhost:5173
```

---

## 📡 API 设计（V0.1）

### 通用规范

- 基础路径：`/api`
- 认证方式：`Authorization: Bearer <token>`
- 统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/logout` | 用户登出 | 是 |

**POST /api/auth/register**

```json
// Request
{
  "username": "john",
  "password": "123456",
  "email": "john@example.com"
}

// Response
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "john"
  }
}
```

**POST /api/auth/login**

```json
// Request
{
  "username": "john",
  "password": "123456"
}

// Response
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "username": "john"
  }
}
```

### 仓库接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/repositories` | 导入 GitHub 仓库 | 是 |
| GET | `/api/repositories` | 获取仓库列表 | 是 |
| GET | `/api/repositories/{id}` | 获取仓库详情 | 是 |
| DELETE | `/api/repositories/{id}` | 删除仓库 | 是 |

**POST /api/repositories**

```json
// Request
{
  "url": "https://github.com/user/repo.git",
  "branch": "main"
}

// Response
{
  "code": 200,
  "message": "仓库导入成功",
  "data": {
    "id": 1,
    "name": "repo",
    "url": "https://github.com/user/repo.git",
    "status": "CLONING"
  }
}
```

### 评审接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/reviews` | 创建评审任务 | 是 |
| GET | `/api/reviews` | 获取评审列表 | 是 |
| GET | `/api/reviews/{id}` | 获取评审详情 | 是 |
| DELETE | `/api/reviews/{id}` | 删除评审记录 | 是 |
| PUT | `/api/reviews/{id}/memo` | 编辑备注 | 是 |
| GET | `/api/reviews/{id}/report` | 下载 Markdown 报告 | 是 |

**POST /api/reviews**

```json
// Request
{
  "repositoryId": 1
}

// Response
{
  "code": 200,
  "message": "评审任务已创建",
  "data": {
    "id": 1,
    "repositoryId": 1,
    "status": "ANALYZING"
  }
}
```

**GET /api/reviews/{id}**

```json
// Response
{
  "code": 200,
  "data": {
    "id": 1,
    "repositoryId": 1,
    "status": "COMPLETED",
    "summary": "该项目是一个基于 Spring Boot 的电商系统...",
    "reportMarkdown": "# 项目分析报告\n\n## 整体架构\n...",
    "aiModel": "deepseek-chat",
    "tokenUsed": 4520,
    "createdAt": "2025-01-15T10:30:00",
    "completedAt": "2025-01-15T10:32:15"
  }
}
```

---

## 🗄 数据库设计（V0.1）

### ER 图

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│   user   │──1:N──│  repository  │──1:N──│  review  │
└──────────┘       └──────────────┘       └──────────┘
```

### 表结构

```sql
-- 用户表
CREATE TABLE `user` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL UNIQUE,
    `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密',
    `email`       VARCHAR(100),
    `avatar_url`  VARCHAR(255),
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 仓库表
CREATE TABLE `repository` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `url`         VARCHAR(255) NOT NULL,
    `branch`      VARCHAR(50)  DEFAULT 'main',
    `local_path`  VARCHAR(255) COMMENT '本地存储路径',
    `status`      VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING / CLONING / READY / ERROR',
    `error_msg`   VARCHAR(500),
    `file_count`  INT          DEFAULT 0,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评审记录表
CREATE TABLE `review` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `repository_id`   BIGINT       NOT NULL,
    `user_id`         BIGINT       NOT NULL,
    `status`          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING / ANALYZING / COMPLETED / ERROR',
    `summary`         TEXT         COMMENT '项目摘要',
    `report_markdown` MEDIUMTEXT   COMMENT '完整 Markdown 报告',
    `memo`            VARCHAR(500) COMMENT '用户备注',
    `digest`          TEXT         COMMENT 'AI 生成的结构化摘要 JSON',
    `ai_model`        VARCHAR(100) COMMENT '使用的 AI 模型',
    `token_used`      INT          DEFAULT 0 COMMENT '消耗的 Token 数',
    `error_msg`       VARCHAR(500),
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `completed_at`    DATETIME,
    FOREIGN KEY (`repository_id`) REFERENCES `repository`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📅 开发计划

```
V0.1 ──────── V0.2 ──────── V0.3 ──────── V1.0
基础版本      智能问答      深度分析      正式发布
 (当前)       (计划中)      (计划中)      (未来)
```

### V0.1 — 基础版本 🚩 当前

| 阶段 | 任务 | 预估 |
|------|------|------|
| 后端初始化 | Spring Boot 项目搭建、数据库建表、基础配置 | 1 天 |
| 用户模块 | 注册 / 登录 / JWT 认证 / 登出 | 2 天 |
| 仓库模块 | 系统 git Clone + 文件树解析 + 异步状态管理 | 2 天 |
| AI 评审引擎 | LangChain4j 集成 + Prompt 设计 + AI 调用 | 3 天 |
| 前端初始化 | Vite + React + Ant Design 项目搭建、路由 | 1 天 |
| 登录页面 | 登录 / 注册表单 + Token 存储 + 路由守卫 | 2 天 |
| 仓库导入页 | 仓库 URL 输入 → 提交 → Clone 状态轮询 | 2 天 |
| 报告展示页 | Markdown 渲染 + 代码高亮 + 报告下载 | 1 天 |
| 联调测试 | 前后端联调、异常处理、边界测试 | 2 天 |
| Docker 部署 | Docker Compose + Nginx + 一键启动脚本 | 1 天 |
| **合计** | | **约 3 周** |

### V0.2 — 智能问答 🎯 规划中

> **核心定位：** 从"单向报告"变成"双向对话"——用户可以对仓库里的代码提问。

#### 功能清单

```
🚧 RAG 仓库问答             基于向量检索 + LLM 的智能问答
🚧 仪表盘                   个人数据统计 + Token 消耗可视化
🚧 PDF 报告导出             报告详情页一键导出 PDF
```

#### 1. 💬 RAG 仓库问答（P0 核心）

用户在报告详情页底部直接提问，后端通过向量检索找到相关源码片段，拼装上下文后由 LLM 生成带引用的回答。

| 阶段 | 做什么 | 技术 |
|------|--------|------|
| 构建知识库 | Clone 完成、评审生成后，代码文件按函数/类切块做 Embedding | LangChain4j + DeepSeek Embedding |
| 存储向量 | 嵌入向量存入向量数据库 | Milvus Lite（嵌入式，无需额外部署） |
| 语义检索 | 用户提问 → 向量检索 → 返回最相关的 5-10 个代码片段 | 余弦相似度 |
| 生成回答 | 代码片段 + 问题 → LLM → 带源码引用的回答 | DeepSeek API |

**交互设计：**

```
┌──────────────────────────────────────────┐
│  💬 智能问答                               │
│                                          │
│  👤 这个项目的登录逻辑在哪里？               │
│  🤖 登录逻辑在 AuthController.java L27-45 │
│     行，使用 Spring Security 的             │
│     AuthenticationManager 进行认证。        │
│     JWT 配置在 JwtUtils.java，Token 有效期 │
│     24 小时。                              │
│     📎 AuthController.java L27-45          │
│     📎 JwtUtils.java L18-32               │
│                                          │
│  [___________________________] [发送]     │
└──────────────────────────────────────────┘
```

#### 2. 📊 仪表盘（P1）

| 卡片 | 内容 |
|------|------|
| 分析概览 | 总仓库数、总报告数、本月分析次数 |
| Token 消耗 | 累计 Token、日均消耗（ECharts 折线图） |
| 最近活动 | 最近 5 条评审记录时间线 |

数据从已有表实时计算，不新增采集逻辑。

#### 3. 📄 PDF 报告导出（P2）

后端将 Markdown 报告转换为 PDF，前端提供下载按钮。

#### 架构决策：为什么不换数据库

V0.2 引入向量检索，需要向量数据库。方案选择：

| 方案 | 描述 | 决定 |
|------|------|------|
| PostgreSQL + pgvector | 换掉 MySQL，一套数据库搞定 | ❌ 推倒重来，V0.1 代码全要回归 |
| **MySQL + Milvus Lite** | MySQL 不动，旁边挂嵌入式向量库 | ✅ 搭积木，不影响已有功能 |

#### 新增技术栈

| 技术 | 用途 |
|------|------|
| DeepSeek Embedding API | 代码文本转向量 |
| Milvus Lite | 嵌入式向量数据库，存储和检索向量 |
| ECharts | 仪表盘图表（前端已有依赖） |
| iText / Flying Saucer | Markdown → PDF 转换 |

#### 改动预估

| 模块 | 新文件 | 影响已有文件 |
|------|--------|-------------|
| RAG 问答 | ~8 个 | 0 个（完全独立模块） |
| 仪表盘 | ~4 个 | AppLayout.tsx（侧边栏加一项） |
| PDF 导出 | ~2 个 | ReportPage.tsx（加一个按钮） |

### V0.3 — 深度分析（规划中）

- 🐛 AI Bug 检测
- 🔒 安全漏洞扫描
- 📈 代码质量趋势分析
- 🗂 多版本管理 & 历史分析记录

---

## 📄 License

MIT

---

<p align="center">
  <b>CodeInsight AI</b> — 让每一行代码都被理解。
</p>
