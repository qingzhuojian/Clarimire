# 清浊鉴——水环境风险监测与智能预警系统

工程代号：Clarimire

Web 管理端 + 移动端 H5 + Spring Boot 后端，覆盖水库巡查打卡、任务闭环、问题上报、水情/断面数据、预警与污染模拟、GeoScene 地图等。

## 目录结构

```
Clarimire/
├── apps/
│   ├── web/          # Web 管理端 (Vue3 + Element Plus + GeoScene)  → :5173
│   └── mobile-h5/    # 移动端 H5 (Vue3 + Leaflet)                 → :5174
├── server/           # 后端 Spring Boot                            → :8080
├── sql/              # 建库与扩展脚本
└── README.md
```

## 快速启动

### 1. 数据库

```bash
mysql -u root -p --default-character-set=utf8mb4 < sql/init.sql
mysql -u root -p --default-character-set=utf8mb4 clarimire < sql/V2_warning_tables.sql
mysql -u root -p --default-character-set=utf8mb4 clarimire < sql/V3_patrol_tables.sql
```

### 2. 后端

```bash
cd server
mvn spring-boot:run
```

### 3. Web 管理端

```bash
cd apps/web
cp .env.example .env   # 按需填写 GeoScene / 天地图等
npm install
npm run dev
```

访问：http://localhost:5173

### 4. 移动端 H5（无需 HBuilderX）

```bash
cd apps/mobile-h5
cp .env.example .env
npm install
npm run dev
```

访问：http://localhost:5174  

纯 H5，浏览器打开即可；正式环境 `npm run build` 后部署 `dist`。

## 功能概览

| 端 | 能力 |
|----|------|
| Web | 首页地图、地图编辑、数据管理、统计、预警、污染模拟、运维调度、系统管理 |
| 移动巡查员 | 日常/指派任务、打卡、突发上报、巡查记录 |
| 移动群众 | 问题上报、进度查询 |

## 注意事项

- `apps/*/node_modules`、`server/target`、`*.env` 已在 `.gitignore`，勿提交密钥与依赖包。
- 请勿把含真实密码的本地备忘（如 `账号密码.txt`）提交到公开仓库。
- Web 地图依赖 GeoScene 服务；运维打卡/上报不依赖地图服务。
- 移动端底图为高德瓦片（Leaflet），不强制天地图白名单。
