# 校园论坛系统（Campus Forum）

基于 **Spring Boot 3 + Vue 3** 的前后端分离校园论坛系统，实现帖子发布与浏览、板块分类、评论互动、站内私信，以及「普通用户 / 版主 / 管理员」三级角色的权限管理。

## 一、技术栈

| 层次 | 技术选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Security（JWT 无状态认证）、MyBatis-Plus 3.5.10、Redis、JJWT 0.11.5、Hutool、Knife4j（在线接口文档） |
| 数据库 | MySQL 8.0（5 张业务表 / 33 个 RESTful 接口 / 8 个 Controller） |
| 前端 | Vue 3.5、Vite、TypeScript、Element Plus、Pinia、Vue Router 4、Axios、v-md-editor（Markdown 编辑与预览）、highlight.js |
| 构建 | Maven 3.8+、npm / Vite |

## 二、功能概览

### 普通用户（USER）
- 注册、登录（JWT 无状态认证）、个人资料维护
- 板块浏览、帖子分页列表、帖子详情
- 发帖：Markdown 编辑器实时预览、图片上传
- 评论与回复
- 站内私信：消息中心与会话窗口

### 版主（MODERATOR）
- 版主工作台：内容管理与审核

### 管理员（ADMIN）
- 管理后台：数据看板、用户管理、内容管理

## 三、快速开始

### 环境要求

JDK 17+ / Maven 3.8+ / Node 18+ / MySQL 8.0 / **Redis**（用于缓存与登录态相关数据）

### 1. 初始化数据库

```bash
# 进入 MySQL 客户端建库（库名 campus_forum）
mysql -uroot -p
mysql> CREATE DATABASE `campus_forum` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql> exit

# 导入表结构与演示数据
mysql -uroot -p campus_forum < backend/sql/campus_forum.sql
```

> 命令行不便时，也可用 Navicat / MySQL Workbench 等图形工具创建 `campus_forum` 库后导入该 SQL 文件。
>
> `sys_user` 表中保留了演示数据，角色字段分别为 `ADMIN`、`MODERATOR`、`USER`；密码以 BCrypt 密文存储，如需新账号直接使用注册页创建即可。

### 2. 启动 Redis

```bash
redis-server          # 默认监听 6379
```

### 3. 配置并启动后端

数据库与 Redis 的连接信息**不写入版本库**，通过 `application-local.yaml`（已加入 .gitignore）或环境变量注入：

```bash
cd backend/src/main/resources
cp application-local.yaml.example application-local.yaml
# 编辑 application-local.yaml，填入本地 MySQL / Redis 账号密码与文件上传路径
```

```bash
cd backend
mvn spring-boot:run
# 或：mvn -DskipTests package && java -jar target/forum-1.0-SNAPSHOT.jar
```

后端服务端口 `8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev          # Vite 默认 http://localhost:5173
```

前端接口基址见 `frontend/src/utils/request.js`（默认指向 `http://localhost:8080/api`）。

### 5. 在线接口文档

启动后端后访问 **http://localhost:8080/doc.html** （Knife4j）。

## 四、项目结构

```
├── backend/                         后端工程（Maven）
│   ├── sql/campus_forum.sql         数据库结构与演示数据
│   └── src/main/
│       ├── java/com/zwz/forum/
│       │   ├── config/              Spring Security、MyBatis-Plus、Knife4j 等配置
│       │   ├── security/            JWT 认证过滤器与安全策略
│       │   ├── controller/          接口层（8 个 Controller / 33 个接口）
│       │   ├── service/ + impl/     业务层
│       │   ├── mapper/              MyBatis-Plus Mapper
│       │   ├── entity/ dto/ vo/     实体、请求对象、响应对象分层
│       │   ├── aspect/              自定义注解与切面
│       │   └── common/              统一返回、异常处理、自定义注解
│       └── resources/
│           ├── application.yaml     主配置（凭据走环境变量 / 本地配置）
│           └── mapper/*.xml         SQL 映射
└── frontend/                        前端工程（Vue 3 + Vite + TS）
    └── src/
        ├── api/                     按模块封装的接口请求（auth / post / comment / message / user / board / admin）
        ├── store/                   Pinia 状态管理（用户登录态等）
        ├── router/                  路由配置
        ├── layout/                  页面布局
        ├── components/              可复用组件
        └── views/                   页面：auth（登录注册）、forum（帖子）、user（个人中心与私信）、moderator（版主台）、admin（管理后台）
```

## 五、数据库设计

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户（角色 `USER` / `MODERATOR` / `ADMIN`，密码 BCrypt 存储） |
| `forum_board` | 论坛板块 |
| `forum_post` | 帖子（标题、Markdown 内容、所属板块、作者、浏览量等） |
| `forum_comment` | 评论（支持按帖子聚合） |
| `sys_message` | 站内私信 |

## 六、接口分组

| 路径前缀 | 模块 | 访问要求 |
| --- | --- | --- |
| `/api/auth` | 注册、登录 | 公开 |
| `/api/public/board` | 板块查询 | 公开 |
| `/api/post` | 帖子发布、列表、详情 | 登录态 |
| `/api/comment` | 评论 | 登录态 |
| `/api/message` | 站内私信 | 登录态 |
| `/api/user` | 个人资料 | 登录态 |
| `/api/admin` | 管理后台 | 管理员 |
| `/upload` | 图片上传 | 登录态 |

## 七、License

本项目采用[木兰宽松许可证，第 2 版](LICENSE)（Mulan PSL v2）。
