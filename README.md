# 水环境风险管控系统 (Clarimire)

一个基于 Spring Boot + Vue 3 的水环境风险管控系统，用于水库巡查、预警管理、水情监测等。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.5
- **Java**: JDK 1.8+
- **ORM**: MyBatis
- **数据库**: MySQL 5.7+ / 8.0+
- **构建工具**: Maven

### 前端
- **框架**: Vue 3
- **UI组件**: Element Plus
- **构建工具**: Vite
- **图表**: ECharts

## 项目结构

```
f:\AAA\
├── back\
│   ├── backend\           # 后端 Spring Boot 项目
│   │   ├── src\
│   │   │   ├── main\java\com\clarimire\   # Java 源码
│   │   │   └── main\resources\            # 配置文件和SQL
│   │   ├── pom.xml        # Maven 配置
│   │   └── mvnw          # Maven Wrapper
│   │
│   └── frontend\          # 前端 Vue 项目
│       ├── src\           # Vue 源码
│       ├── public\        # 静态资源
│       └── package.json   # NPM 配置
│
├── .vscode\               # VSCode 配置
└── 后端运行手册.md         # 后端详细文档
```

## 快速启动

### 1. 后端启动

```bash
cd back/backend

# 初始化数据库（确保MySQL运行中）
mysql -u root -p < src/main/resources/db/complete_schema.sql

# 启动后端
mvn spring-boot:run
# 或使用 JAR 启动
mvn clean package -DskipTests
java -jar target/clarimire-backend-1.0.0.jar
```

后端地址: http://localhost:8080

### 2. 前端启动

```bash
cd back/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端地址: http://localhost:8081

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| inspector1 | 123456 | 巡查员 |
| inspector2 | 123456 | 巡查员 |
| public1 | 123456 | 公众 |

## 端口配置

| 服务 | 端口 |
|------|------|
| 后端 API | 8080 |
| 前端 | 8081 |

## 相关文档

- [后端运行手册.md](后端运行手册.md) - 后端详细文档
- [back/backend/错误诊断.md](back/backend/错误诊断.md) - 问题排查指南
