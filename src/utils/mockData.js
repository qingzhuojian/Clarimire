// 模拟数据模块 - 本地存储与模拟数据实现

const STORAGE_KEYS = {
  TASKS: 'envInspectionTasks',
  REPORTS_PUBLIC: 'envInspectionReportsPublic',
  REPORTS_INSPECTOR: 'envInspectionReportsInspector',
  NOTIFICATIONS: 'envInspectionNotifications',
  CLOCK_RECORDS: 'envInspectionClockRecords',
  SETTINGS: 'envInspectionSettings',
  VIRTUAL_LOCATION: 'envInspectionVirtualLocation',
  TAB_BAR_CONFIG: 'envInspectionTabBarConfig'
}

// 工具函数
const formatDate = (date) => {
  const d = new Date(date)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

const getDateLabel = (date) => {
  const d = new Date(date)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const itemDate = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  
  if (itemDate.getTime() === today.getTime()) return '今天'
  if (itemDate.getTime() === yesterday.getTime()) return '昨天'
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}月${day}日`
}

// 模拟 tabBar 配置数据
export const getMockTabBarConfig = () => [
  {
    id: 'home',
    pagePath: 'pages/index/index',
    text: '首页',
    iconPath: '/static/tabbar/home.png',
    selectedIconPath: '/static/tabbar/home-active.png',
    badge: 0,
    isShow: true,
    sort: 1
  },
  {
    id: 'tasks',
    pagePath: 'pages/tasks/tasks',
    text: '巡查任务',
    iconPath: '/static/tabbar/tasks.png',
    selectedIconPath: '/static/tabbar/tasks-active.png',
    badge: 0,
    isShow: true,
    sort: 2
  },
  {
    id: 'report',
    pagePath: 'pages/my-reports/my-reports',
    text: '我的上报',
    iconPath: '/static/tabbar/report.png',
    selectedIconPath: '/static/tabbar/report-active.png',
    badge: 0,
    isShow: true,
    sort: 3
  },
  {
    id: 'notifications',
    pagePath: 'pages/notifications/notifications',
    text: '消息',
    iconPath: '/static/tabbar/notification.png',
    selectedIconPath: '/static/tabbar/notification-active.png',
    badge: 3,
    isShow: true,
    sort: 4
  },
  {
    id: 'mine',
    pagePath: 'pages/mine/mine',
    text: '我的',
    iconPath: '/static/tabbar/mine.png',
    selectedIconPath: '/static/tabbar/mine-active.png',
    badge: 0,
    isShow: true,
    sort: 5
  }
]

// tabBar 配置操作
export const tabBarStore = {
  // 获取 tabBar 配置
  getConfig() {
    let config = storage.get(STORAGE_KEYS.TAB_BAR_CONFIG)
    if (!config) {
      config = getMockTabBarConfig()
      storage.set(STORAGE_KEYS.TAB_BAR_CONFIG, config)
    }
    return config
  },

  // 更新单个 tab 项的 badge
  updateBadge(id, count) {
    const config = this.getConfig()
    const item = config.find(t => t.id === id)
    if (item) {
      item.badge = count
      storage.set(STORAGE_KEYS.TAB_BAR_CONFIG, config)
      this.applyToTabBar()
    }
  },

  // 更新整个配置
  setConfig(config) {
    storage.set(STORAGE_KEYS.TAB_BAR_CONFIG, config)
    this.applyToTabBar()
  },

  // 应用配置到原生 tabBar
  applyToTabBar() {
    const config = this.getConfig()
    const list = config.filter(t => t.isShow !== false).map(t => ({
      pagePath: t.pagePath,
      text: t.text,
      iconPath: t.iconPath,
      selectedIconPath: t.selectedIconPath
    }))
    
    uni.setTabBarBadge({
      index: 0,
      text: ''
    })
    config.forEach((item, index) => {
      if (item.badge > 0) {
        uni.setTabBarBadge({
          index,
          text: String(item.badge)
        })
      } else {
        uni.removeTabBarBadge({ index })
      }
    })
  },

  // 重置为默认配置
  reset() {
    const defaultConfig = getMockTabBarConfig()
    storage.set(STORAGE_KEYS.TAB_BAR_CONFIG, defaultConfig)
    this.applyToTabBar()
  }
}

// 模拟任务数据
export const getMockTasks = () => [
  {
    id: '1',
    title: '朝阳区排水口日常巡查',
    location: '北京市朝阳区望京街道',
    type: '日常巡查',
    status: 'pending',
    priority: 'normal',
    deadline: '2024-05-15 18:00',
    description: '对辖区内重点排污口进行例行检查，重点关注污水处理设施运行状态。',
    coords: { lat: 39.995, lng: 116.480 },
    tags: ['日常', '排污口', '重点'],
    photos: []
  },
  {
    id: '2',
    title: '凉水河水质采样监测',
    location: '北京市丰台区凉水河',
    type: '专项检查',
    status: 'in_progress',
    priority: 'urgent',
    deadline: '2024-05-15 12:00',
    description: '对凉水河重点断面进行水质采样，检测COD、氨氮、总磷等指标。',
    coords: { lat: 39.858, lng: 116.285 },
    tags: ['专项', '水质', '紧急'],
    photos: []
  },
  {
    id: '3',
    title: '信访件现场核查 - 异味扰民',
    location: '海淀区中关村街道',
    type: '信访核查',
    status: 'completed',
    priority: 'normal',
    deadline: '2024-05-14 18:00',
    description: '核实居民反映的工业企业异味扰民问题。',
    coords: { lat: 39.982, lng: 116.310 },
    tags: ['信访', '异味'],
    photos: [],
    result: '已现场核查，涉事企业已停产整改',
    processTime: '2024-05-14 16:30'
  }
]

// 模拟巡查记录
export const getMockInspectionRecords = () => [
  {
    id: 'XR1001',
    name: '工业园区A区日常巡查',
    status: '已完成',
    time: '2024-05-14 09:30',
    location: '苏州工业园区星湖街328号',
    desc: '共检查12个点位，发现1处轻微渗漏，已现场整改。',
    result: '问题已整改完成',
    tags: ['日常', '排污口'],
    photos: ['photo1', 'photo2', 'photo3']
  },
  {
    id: 'XR1002',
    name: '河道水质采样监测',
    status: '已完成',
    time: '2024-05-13 14:20',
    location: '吴江区松陵镇东太湖大道',
    desc: '采集水样5份，送检中。',
    result: '水样已送检，等待检测报告',
    tags: ['专项', '水质'],
    photos: ['photo1', 'photo2']
  },
  {
    id: 'XR1003',
    name: '信访件现场核查 - 噪声扰民',
    status: '已完成',
    time: '2024-05-12 10:15',
    location: '姑苏区观前街88号',
    desc: '现场核查建筑工地夜间施工情况。',
    result: '涉事工地已停工整改',
    tags: ['信访', '噪声'],
    photos: ['photo1']
  }
]

// 模拟消息通知
export const getMockNotifications = () => [
  {
    id: '1',
    type: 'urgent',
    title: '紧急任务下达',
    content: '接上级通知，需立即对吴江工业区进行突击检查。请于今日14:00前完成报到。',
    time: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
    isRead: false,
    actionRequired: true
  },
  {
    id: '2',
    type: 'task',
    title: '新任务：企业排污许可核查',
    content: '您有一个新的排污许可核查任务，请及时查看并安排巡查时间。',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
    isRead: false,
    actionRequired: true
  },
  {
    id: '3',
    type: 'system',
    title: '水质检测报告已生成',
    content: '您于5月10日提交的水质样本检测报告已生成，请登录系统查看详细数据。',
    time: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
    isRead: true,
    actionRequired: false
  }
]

// 模拟群众上报数据
export const getMockReports = () => [
  {
    id: 'BG20240514001',
    title: '河岸垃圾堆积',
    category: '固废污染',
    urgency: 'warning',
    status: '处理中',
    time: new Date(Date.now() - 1000 * 60 * 60 * 3).toISOString(),
    location: '吴中区越溪街道小石湖公园旁',
    description: '河岸边有大量生活垃圾堆积，气味难闻，影响周边居民生活。',
    photos: [],
    processResult: '等待巡查员现场核查中...\n预计1-3个工作日内完成'
  },
  {
    id: 'BG20240513002',
    title: '工厂排放黑烟',
    category: '大气污染',
    urgency: 'urgent',
    status: '已处理',
    time: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
    location: '相城区元和街道工业园',
    description: '工业园内有工厂烟囱持续排放黑色烟雾，疑似未达标排放。',
    photos: [],
    processResult: '1. 涉事单位已接受处理\n2. 问题已整改完成\n3. 如有其他问题，欢迎继续上报'
  }
]

// 存储操作
export const storage = {
  get(key) {
    try {
      const data = uni.getStorageSync(key)
      return data ? JSON.parse(data) : null
    } catch (e) {
      return null
    }
  },
  set(key, data) {
    try {
      uni.setStorageSync(key, JSON.stringify(data))
      return true
    } catch (e) {
      return false
    }
  },
  remove(key) {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (e) {
      return false
    }
  }
}

// 虚拟定位存储
export const virtualLocationStore = {
  // 获取虚拟定位配置
  get() {
    return storage.get(STORAGE_KEYS.VIRTUAL_LOCATION) || null
  },
  
  // 设置虚拟定位
  set(data) {
    return storage.set(STORAGE_KEYS.VIRTUAL_LOCATION, data)
  },
  
  // 清除虚拟定位
  clear() {
    return storage.remove(STORAGE_KEYS.VIRTUAL_LOCATION)
  },
  
  // 是否启用虚拟定位
  isEnabled() {
    const config = this.get()
    return config && config.enabled === true
  },
  
  // 获取虚拟坐标
  getCoords() {
    const config = this.get()
    if (config && config.enabled) {
      return { lat: config.lat, lng: config.lng }
    }
    return null
  }
}

// 预设城市坐标
export const CITY_COORDS = {
  '北京市': { lat: 39.9042, lng: 116.4074, address: '北京市东城区' },
  '上海市': { lat: 31.2304, lng: 121.4737, address: '上海市黄浦区' },
  '广州市': { lat: 23.1291, lng: 113.2644, address: '广州市越秀区' },
  '深圳市': { lat: 22.5431, lng: 114.0579, address: '深圳市福田区' },
  '苏州市': { lat: 31.2989, lng: 120.5853, address: '江苏省苏州市姑苏区' },
  '南京市': { lat: 32.0603, lng: 118.7969, address: '江苏省南京市玄武区' },
  '杭州市': { lat: 30.2741, lng: 120.1551, address: '浙江省杭州市西湖区' },
  '成都市': { lat: 30.5728, lng: 104.0668, address: '四川省成都市锦江区' },
  '武汉市': { lat: 30.5928, lng: 114.3055, address: '湖北省武汉市江汉区' },
  '西安市': { lat: 34.3416, lng: 108.9398, address: '陕西省西安市新城区' }
}

// 任务操作
export const taskStore = {
  _selectedTaskId: null,

  getAll() {
    let tasks = storage.get(STORAGE_KEYS.TASKS)
    const validTasks = getMockTasks()
    const validTitles = new Set(validTasks.map(t => t.title))
    
    if (!tasks || !Array.isArray(tasks)) {
      tasks = validTasks
      storage.set(STORAGE_KEYS.TASKS, tasks)
    } else {
      const cleanedTasks = tasks.filter(t => validTitles.has(t.title))
      if (cleanedTasks.length !== tasks.length) {
        tasks = cleanedTasks.length ? cleanedTasks : validTasks
        storage.set(STORAGE_KEYS.TASKS, tasks)
      }
    }
    return tasks
  },

  getByStatus(status) {
    const tasks = this.getAll()
    if (status === 'all') return tasks
    return tasks.filter(t => t.status === status)
  },

  updateStatus(id, status) {
    const tasks = this.getAll()
    const index = tasks.findIndex(t => t.id === id)
    if (index !== -1) {
      tasks[index].status = status
      if (status === 'completed') {
        tasks[index].completedAt = new Date().toISOString()
      }
      storage.set(STORAGE_KEYS.TASKS, tasks)
      return true
    }
    return false
  },

  setSelectedTask(id) {
    this._selectedTaskId = id
  },

  getSelectedTask() {
    if (!this._selectedTaskId) return null
    const tasks = this.getAll()
    return tasks.find(t => t.id === this._selectedTaskId) || null
  },

  clearSelectedTask() {
    this._selectedTaskId = null
  },

  addReport(report, role = 'public') {
    const key = role === 'inspector' ? STORAGE_KEYS.REPORTS_INSPECTOR : STORAGE_KEYS.REPORTS_PUBLIC
    const reports = storage.get(key) || []
    const newReport = {
      ...report,
      id: 'BG' + Date.now(),
      time: new Date().toISOString(),
      status: '待处理',
      role
    }
    reports.unshift(newReport)
    storage.set(key, reports)
    return newReport
  }
}

// 上报记录操作
export const reportStore = {
  getAll(role = 'public') {
    const key = role === 'inspector' ? STORAGE_KEYS.REPORTS_INSPECTOR : STORAGE_KEYS.REPORTS_PUBLIC
    const userReports = storage.get(key) || []
    const mockReports = role === 'public' ? getMockReports() : []
    return [...userReports, ...mockReports]
  },

  getById(id) {
    const publicReports = storage.get(STORAGE_KEYS.REPORTS_PUBLIC) || []
    const inspectorReports = storage.get(STORAGE_KEYS.REPORTS_INSPECTOR) || []
    return [...publicReports, ...inspectorReports].find(r => r.id === id)
  },

  updateStatus(id, status) {
    const publicReports = storage.get(STORAGE_KEYS.REPORTS_PUBLIC) || []
    const inspectorReports = storage.get(STORAGE_KEYS.REPORTS_INSPECTOR) || []
    let reports = publicReports
    let key = STORAGE_KEYS.REPORTS_PUBLIC
    let index = reports.findIndex(r => r.id === id)
    if (index === -1) {
      reports = inspectorReports
      key = STORAGE_KEYS.REPORTS_INSPECTOR
      index = reports.findIndex(r => r.id === id)
    }
    if (index !== -1) {
      reports[index].status = status
      storage.set(key, reports)
      return true
    }
    return false
  },

  deleteReport(id) {
    const publicReports = storage.get(STORAGE_KEYS.REPORTS_PUBLIC) || []
    const inspectorReports = storage.get(STORAGE_KEYS.REPORTS_INSPECTOR) || []
    let index = publicReports.findIndex(r => r.id === id)
    if (index !== -1) {
      publicReports.splice(index, 1)
      storage.set(STORAGE_KEYS.REPORTS_PUBLIC, publicReports)
      return true
    }
    index = inspectorReports.findIndex(r => r.id === id)
    if (index !== -1) {
      inspectorReports.splice(index, 1)
      storage.set(STORAGE_KEYS.REPORTS_INSPECTOR, inspectorReports)
      return true
    }
    return false
  },

  getCount() {
    return this.getAll().length
  }
}

// 通知操作
export const notificationStore = {
  getAll() {
    let notifications = storage.get(STORAGE_KEYS.NOTIFICATIONS)
    if (!notifications) {
      notifications = getMockNotifications()
      storage.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
    }
    return notifications
  },
  
  getUnreadCount() {
    const notifications = this.getAll()
    return notifications.filter(n => !n.isRead).length
  },
  
  markAsRead(id) {
    const notifications = this.getAll()
    const index = notifications.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications[index].isRead = true
      storage.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
      return true
    }
    return false
  },
  
  markAllAsRead() {
    const notifications = this.getAll()
    notifications.forEach(n => n.isRead = true)
    storage.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
  },
  
  updateActionRequired(id, actionRequired) {
    const notifications = this.getAll()
    const index = notifications.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications[index].actionRequired = actionRequired
      storage.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
      return true
    }
    return false
  }
}

// 打卡记录
export const clockStore = {
  getRecords() {
    return storage.get(STORAGE_KEYS.CLOCK_RECORDS) || []
  },
  
  addRecord(record) {
    const records = this.getRecords()
    records.unshift({
      ...record,
      id: 'CK' + Date.now(),
      time: new Date().toISOString()
    })
    storage.set(STORAGE_KEYS.CLOCK_RECORDS, records)
    return records[0]
  },
  
  getTodayRecord() {
    const records = this.getRecords()
    const today = new Date().toDateString()
    return records.find(r => new Date(r.time).toDateString() === today)
  }
}

// 工具函数导出
export const utils = {
  formatDate,
  getDateLabel
}

export default {
  storage,
  taskStore,
  reportStore,
  notificationStore,
  clockStore,
  tabBarStore,
  utils,
  getMockTasks,
  getMockInspectionRecords,
  getMockReports,
  getMockNotifications,
  getMockTabBarConfig
}
