// API配置 - 相对路径，便于迁移
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default {
  baseURL: API_BASE_URL,
  
  // 认证
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    userinfo: '/auth/userinfo'
  },
  
  // 水情数据
  waterSituation: {
    list: '/waterSituation/list',
    detail: '/waterSituation',
    create: '/waterSituation/create',
    update: '/waterSituation/update',
    delete: '/waterSituation',
    import: '/waterSituation/import',
    export: '/waterSituation/export',
    reservoirs: '/waterSituation/reservoirs'
  },
  
  // 监测断面
  sectionMonitor: {
    list: '/sectionMonitor/list',
    detail: '/sectionMonitor',
    create: '/sectionMonitor/create',
    update: '/sectionMonitor/update',
    delete: '/sectionMonitor',
    import: '/sectionMonitor/import',
    export: '/sectionMonitor/export',
    reservoirs: '/sectionMonitor/reservoirs',
    years: '/sectionMonitor/years'
  },

  warning: {
    list: '/warning/list',
    reservoirs: '/warning/reservoirs',
    evaluate: '/warning/evaluate',
    evaluateEnv: '/warning/evaluateEnv'
  },

  patrol: {
    tasks: '/patrol/tasks',
    taskDetail: '/patrol/tasks',
    records: '/patrol/records',
    checkin: '/patrol/checkin',
    dashboard: '/patrol/dashboard'
  },

  issues: {
    list: '/issues/list',
    detail: '/issues',
    create: '/issues/create',
    update: '/issues/update',
    review: '/issues/review'
  },

  system: {
    users: '/system/users',
    checkinPolicy: '/system/checkin-policy',
    reservoirLocations: '/system/reservoir-locations'
  },

  upload: {
    photo: '/upload/photo'
  }
}
