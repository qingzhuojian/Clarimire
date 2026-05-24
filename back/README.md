# Clarimire 水务管理系统

## 项目简介

这是一个水务综合管理平台，包含后端 Spring Boot 服务和前端 Vue 3 应用。

## 环境要求

- **后端**: JDK 17+, Maven 3.8+
- **前端**: Node.js 18+, npm 9+
- **数据库**: MySQL 8.0+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd back
```

### 2. 初始化后端配置

后端敏感配置（如数据库密码、JWT密钥）存储在 `application-local.yml` 中，该文件不会被提交到仓库。

```bash
cd backend/src/main/resources
copy application-local.yml.example application-local.yml
```

然后编辑 `application-local.yml`，填写你的配置：

```yaml
spring:
  datasource:
    username: your_db_username    # 替换为你的数据库用户名
    password: your_db_password    # 替换为你的数据库密码

jwt:
  secret: your-secure-jwt-secret   # 替换为你的JWT密钥（建议32位以上随机字符串）
```

### 3. 创建数据库

```sql
CREATE DATABASE water_data DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后执行 SQL 初始化脚本（位于 `src/main/resources/db/` 目录）：

```bash
mysql -u root -p water_data < src/main/resources/db/complete_schema.sql
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

或使用 IDE 直接运行 `ClarimireApplication.java`。

后端默认运行在 `http://localhost:8080`。

### 5. 初始化前端配置

编辑 `src/config/api.config.js`，修改后端地址：

```javascript
const API_CONFIG = {
  baseURL: 'http://localhost:8080',  // 如果后端在其他服务器，修改为实际地址
}
```

### 6. 安装前端依赖并启动

```bash
cd frontend
npm install
npm run serve
```

前端默认运行在 `http://localhost:8081`。

## API 文档（Swagger）

启动后端后，可以访问 Swagger API 文档页面：

- **地址**: http://localhost:8080/doc.html
- **原生Swagger UI**: http://localhost:8080/swagger-ui/index.html

这个页面展示了所有 API 接口，可以：
- 查看接口列表、路径、参数、返回值
- 在线测试接口
- 查看数据结构

> **注意**: Swagger 仅在开发环境（dev）启用，生产环境自动关闭。

## 默认账号

初始化数据库后，可使用以下账号登录：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

> **注意**: 生产环境请务必修改默认密码！

## 目录结构

```
back/
├── backend/                    # Spring Boot 后端
│   └── src/main/
│       ├── java/com/clarimire/  # Java 源码
│       └── resources/
│           ├── application.yml     # 主配置文件
│           ├── application-local.yml  # 本地配置（不提交）
│           ├── application-local.yml.example  # 本地配置模板
│           ├── db/               # 数据库脚本
│           └── mapper/           # MyBatis XML 映射文件
└── frontend/                   # Vue 3 前端
    └── src/
        ├── api/               # API 接口
        ├── config/           # 配置文件
        ├── views/            # 页面组件
        └── ...
```

## 配置说明

### 后端配置文件优先级

Spring Boot 配置加载优先级（高到低）：

1. `application-local.yml` - 本地开发配置（不会被提交）
2. `application.yml` - 主配置文件（会被提交，不含敏感信息）

### 环境变量

可在启动时通过环境变量覆盖配置：

```bash
# Linux/Mac
export JWT_SECRET=your-production-secret
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password

# Windows
set JWT_SECRET=your-production-secret
set SPRING_DATASOURCE_USERNAME=prod_user
set SPRING_DATASOURCE_PASSWORD=prod_password

mvn spring-boot:run
```

## 常见问题

### 1. 前端无法连接后端

检查：
- 后端是否已启动（端口 8080）
- 前端 `api.config.js` 中的 `baseURL` 是否正确
- 跨域配置是否正确（后端已配置 CORS）

### 2. 数据库连接失败

检查：
- MySQL 服务是否启动
- `application-local.yml` 中的数据库账号密码是否正确
- 数据库 `water_data` 是否已创建

### 3. 端口被占用

修改 `application.yml` 中的 `server.port` 更改后端端口，并同步修改前端 `api.config.js` 中的 `baseURL`。
