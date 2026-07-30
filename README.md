# 水环境风险管控系统 - 移动端

基于 uni-app + Vue 3 的跨端移动应用，支持 H5/微信小程序/App。

## 技术栈

- **前端框架**：uni-app + Vue 3 (Composition API)
- **状态管理**：Pinia
- **样式**：SCSS
- **本地存储**：uni-app Storage API

## 功能模块

### 巡查员端
- **首页**：统计卡片、快捷入口、当前任务、最近巡查记录
- **任务管理**：任务列表、任务详情、任务状态筛选、任务接单与提交
- **巡查地图**：地图展示、定位打卡、虚拟定位（开发调试用）
- **巡查记录**：查看个人巡查历史

### 群众端
- **首页**：群众上报入口、最近上报记录
- **问题上报**：问题分类、紧急程度、图片上传、位置定位
- **我的上报**：查看个人上报记录、详细状态

### 通用功能
- **消息中心**：通知列表、消息分类、已读未读状态
- **个人中心**：用户信息、账号切换
- **设置**：密码修改、通知设置、免打扰模式、主题切换（深色/浅色）

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发运行

**H5**
```bash
npm run dev:h5
```

**微信小程序**
```bash
npm run dev:mp-weixin
```

### 构建发布

**H5**
```bash
npm run build:h5
```

**微信小程序**
```bash
npm run build:mp-weixin
```

## 项目结构

```
├── src/
│   ├── pages/              # 页面
│   │   ├── index/          # 首页
│   │   ├── tasks/          # 任务列表
│   │   ├── notifications/  # 消息中心
│   │   ├── mine/           # 个人中心
│   │   ├── report/         # 问题上报
│   │   ├── map/            # 巡查地图
│   │   ├── settings/       # 设置
│   │   ├── notification-settings/  # 通知设置
│   │   ├── inspection-detail/      # 巡查详情
│   │   ├── report-detail/         # 上报详情
│   │   └── my-reports/           # 我的上报
│   ├── store/              # 状态管理
│   │   ├── index.js
│   │   ├── theme.js        # 主题状态
│   │   └── user.js         # 用户状态
│   ├── styles/             # 全局样式
│   │   └── common.scss
│   ├── utils/              # 工具函数
│   │   ├── helper.js       # 辅助函数
│   │   ├── mockData.js     # 模拟数据
│   │   └── api.js          # API 接口
│   ├── App.vue
│   └── main.js
├── static/                 # 静态资源
│   └── tabbar/             # TabBar 图标
├── index.html
├── pages.json              # 页面配置
├── manifest.json           # 应用配置
├── vite.config.ts
└── package.json
```

## 默认测试账号

| 用户名       | 密码    | 角色    |
| --------- | ----- | ----- |
| inspector | 123456 | 巡查员   |
| public    | 123456 | 公众    |

## 配套项目

- **后端 API**：[Clarimire Backend](https://github.com/qingzhuojian/Clarimire/tree/main/back/backend)
- **管理后台**：[Clarimire Frontend](https://github.com/qingzhuojian/Clarimire/tree/main/back/frontend)

## 数据存储

当前使用本地存储模拟后端接口数据：

- **任务数据**：`envInspectionTasks`
- **上报数据**：`envInspectionReports`
- **通知数据**：`envInspectionNotifications`
- **打卡记录**：`envInspectionClockRecords`
- **主题设置**：`envInspectionTheme`
- **用户信息**：`envInspectionUser`

## 注意事项

1. 地图功能需要配置正确的 mapId 才能在真机上使用
2. 定位功能需要用户在设备设置中开启定位权限
3. 拍照功能需要相机和相册权限
4. 对接真实后端时，修改 `src/utils/api.js` 中的 `CONFIG.USE_MOCK_DATA` 为 `false`
