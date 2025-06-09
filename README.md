# AI-Agent

这是一个基于 Spring Boot 的 AI 代理应用。

## 项目简介

本项目是一个智能 AI 代理，集成了多种 AI 服务和外部工具，能够执行一系列任务，例如与用户聊天、调用外部 API、访问数据库、进行网页自动化等。

## 主要功能

*   **AI 聊天**: 基于大型语言模型（通过阿里云通义千问）提供智能聊天功能。
*   **Web API**: 提供 RESTful API，并使用 Knife4j 生成接口文档。
*   **工具调用**:
    *   **网页搜索**: 集成 Exa 和其他搜索引擎进行网页信息检索。
    *   **地图服务**: 集成高德地图服务。
    *   **航班信息**: 集成飞常准查询航班信息。
    *   **网页自动化**: 使用 Selenium 进行网页自动化操作。
*   **数据存储**:
    *   使用 MySQL 数据库。
    *   使用 MyBatis-Plus 作为 ORM 框架。
    *   集成阿里云 OSS 进行对象存储。
*   **安全认证**: 使用 Sa-Token 进行权限控制。

## 技术栈

*   **后端**: Spring Boot 3, Java 21
*   **AI**: Spring AI, 阿里云通义千问
*   **数据库**: MySQL, MyBatis-Plus
*   **API 文档**: Knife4j
*   **认证**: Sa-Token
*   **工具**: Selenium, Jsoup, Hutool

## 快速开始

1.  **克隆项目**
    ```bash
    git clone <your-repository-url>
    cd AI-Agent
    ```

2.  **配置环境**

    需要配置的凭据包括：
    *   数据库连接信息
    *   阿里云通义千问 API Key
    *   Exa API Key
    *   阿里云 OSS Access Key 和 Secret
    *   高德地图 API Key
    *   飞常准 API Key

3.  **运行项目**
    ```bash
    mvn spring-boot:run
    ```

4.  **访问 API 文档**
    项目启动后，您可以访问以下地址查看 API 文档：
    [http://localhost:8123/api/doc.html](http://localhost:8123/api/doc.html)
