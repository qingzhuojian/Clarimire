// API 接口模块 - 对接后端移动端接口
// 所有接口均使用 /api/mobile/ 前缀

import CONFIG from './config'

// 辅助函数
const showToast = (msg, icon = 'none') => {
  if (typeof uni !== 'undefined') {
    uni.showToast({ title: msg, icon })
  }
}

// 日志打印
const log = (...args) => {
  if (CONFIG.ENABLE_LOG) {
    console.log('[API]', ...args)
  }
}

// ==================== 请求封装 ====================

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    log(`请求: ${options.method || 'GET'} ${options.url}`, options.data || '')

    uni.request({
      url: CONFIG.API_BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      timeout: CONFIG.REQUEST_TIMEOUT,
      success: (res) => {
        log(`响应: ${options.url}`, res.data)

        if (res.statusCode === 200) {
          // 后端返回格式：{ code: 0, data: ..., message: '...' }
          // 或者：{ code: 200, data: ..., success: true }
          const data = res.data
          if (data.code === 0 || data.code === 200 || data.success === true) {
            resolve(data.data || data)
          } else {
            showToast(data.message || '请求失败', 'none')
            reject(data)
          }
        } else if (res.statusCode === 401) {
          showToast('登录已过期，请重新登录', 'none')
          uni.removeStorageSync('token')
          uni.removeStorageSync('uni_id_token')
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 1500)
          reject(res)
        } else if (res.statusCode === 404) {
          showToast('接口不存在', 'none')
          reject(res)
        } else {
          showToast('网络请求失败', 'none')
          reject(res)
        }
      },
      fail: (err) => {
        console.error('请求失败:', options.url, err)
        log(`请求失败: ${options.url}`, err)
        showToast('网络连接失败，请检查网络', 'none')
        reject(err)
      }
    })
  })
}

// ==================== 模拟数据（开发阶段使用） ====================

const mockData = {
  // 模拟登录响应
  login: (username, password, role) => ({
    token: 'mock_token_' + Date.now(),
    user: {
      id: 1,
      username: username,
      realName: role === 'inspector' ? '张明' : '群众用户',
      role: role,
      phone: '13800138000',
      dept: role === 'inspector' ? '江北生态环境分局' : '普通用户'
    }
  }),

  // 模拟用户信息
  profile: {
    id: 1,
    username: 'admin',
    realName: '张明',
    role: 'inspector',
    phone: '13800138000',
    dept: '江北生态环境分局',
    avatar: '巡',
    certified: true
  },

  // 模拟统计数据
  dashboardStats: {
    todayCompleted: 3,
    todayTotal: 8,
    pendingTasks: 2,
    monthlyStats: 24
  },

  // 模拟巡查统计
  patrolStats: {
    totalCheckins: 45,
    totalRecords: 12,
    todayCheckins: 3
  },

  // 模拟待处理任务
  pendingTasks: [
    {
      id: 1,
      title: '密云水库水质采样任务',
      description: '水质采样',
      reservoirName: '密云水库',
      deadline: new Date(Date.now() + 86400000).toISOString(),
      status: 'pending'
    },
    {
      id: 2,
      title: '怀柔水库大坝安全检查',
      description: '安全检查',
      reservoirName: '怀柔水库',
      deadline: new Date(Date.now() + 172800000).toISOString(),
      status: 'pending'
    }
  ],

  // 模拟巡查记录
  patrolRecords: {
    list: [
      {
        id: 1,
        inspector: '张三',
        address: '北京市密云区密云水库',
        reservoirName: '密云水库',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        status: 'pending',
        hasIssue: false
      },
      {
        id: 2,
        inspector: '李四',
        address: '北京市延庆区官厅水库',
        reservoirName: '官厅水库',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        status: 'pending',
        hasIssue: true
      }
    ],
    total: 2
  },

  // 模拟水库列表
  reservoirs: [
    { id: 1, name: '密云水库', lat: 40.50, lng: 116.85 },
    { id: 2, name: '官厅水库', lat: 40.35, lng: 115.90 },
    { id: 3, name: '怀柔水库', lat: 40.30, lng: 116.60 }
  ]
}

// ==================== 认证相关接口 ====================

// 移动端登录
export const mobileLogin = (username, password) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const result = mockData.login(username, password)
        resolve(result)
      }, 600)
    })
  }
  return request({
    url: '/api/mobile/auth/login',
    method: 'POST',
    data: { username, password }
  })
}

// 移动端注册
export const mobileRegister = (data) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 600)
    })
  }
  return request({
    url: '/api/mobile/auth/register',
    method: 'POST',
    data
  })
}

// 获取当前用户信息（移动端）
export const getMobileProfile = () => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData.profile), 300)
    })
  }
  return request({
    url: '/api/mobile/auth/profile',
    method: 'GET'
  })
}

// 更新用户信息
export const updateProfile = (data) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 300)
    })
  }
  return request({
    url: '/api/auth/profile',
    method: 'PUT',
    data
  })
}

// 修改密码
export const changePassword = (oldPassword, newPassword) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 300)
    })
  }
  return request({
    url: '/api/auth/password',
    method: 'PUT',
    data: { oldPassword, newPassword }
  })
}

// ==================== 水库相关接口 ====================

// 获取水库列表（移动端）
export const getMobileReservoirs = () => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData.reservoirs), 300)
    })
  }
  return request({
    url: '/api/mobile/reservoirs',
    method: 'GET'
  })
}

// 获取水库详情（移动端）
export const getMobileReservoirDetail = (id) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      const reservoir = mockData.reservoirs.find(r => r.id === id) || mockData.reservoirs[0]
      setTimeout(() => resolve(reservoir), 300)
    })
  }
  return request({
    url: `/api/mobile/reservoirs/${id}`,
    method: 'GET'
  })
}

// ==================== 巡查打卡接口 ====================

// 签到打卡
export const patrolCheckin = (data) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 500)
    })
  }
  return request({
    url: '/api/mobile/patrol/checkin',
    method: 'POST',
    data
  })
}

// ==================== 巡查记录接口 ====================

// 获取巡查记录列表（移动端）
export const getMobilePatrolRecords = (params) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      let records = mockData.patrolRecords.list
      // 按巡查员名称过滤
      if (params?.inspectorName) {
        records = records.filter(r => r.inspector === params.inspectorName)
      }
      setTimeout(() => resolve({ list: records }), 300)
    })
  }
  return request({
    url: '/api/mobile/patrol/records',
    method: 'GET',
    data: params
  })
}

// 获取巡查记录详情（移动端）
export const getMobilePatrolRecordDetail = (id) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      const record = mockData.patrolRecords.list.find(r => r.id == id) || mockData.patrolRecords.list[0]
      setTimeout(() => resolve(record), 300)
    })
  }
  return request({
    url: `/api/mobile/patrol/records/${id}`,
    method: 'GET'
  })
}

// 上报问题（移动端）
export const mobileReport = (data) => {
  console.log('mobileReport 调用:', data)
  // 保存到本地存储（用于移动端已反馈页面显示）
  const localReports = uni.getStorageSync('localInspectionReports') || []
  const newReport = {
    id: 'XR' + Date.now(),
    ...data,
    status: 'pending',
    submittedAt: new Date().toISOString(),
    type: '巡查上报'
  }
  localReports.unshift(newReport)
  uni.setStorageSync('localInspectionReports', localReports)
  console.log('已保存到 localInspectionReports:', newReport)
  
  // 提交到后端
  return request({
    url: '/api/mobile/reports',
    method: 'POST',
    data
  })
}

// ==================== 巡查任务接口 ====================

// 获取待处理任务（移动端）
export const getMobilePendingTasks = (inspectorId, inspector) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData.pendingTasks), 300)
    })
  }
  return request({
    url: '/api/mobile/tasks/pending',
    method: 'GET',
    data: { inspectorId, inspector }
  })
}

// 获取任务列表（移动端）
export const getMobileTaskList = (params) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({
        list: mockData.pendingTasks,
        total: mockData.pendingTasks.length,
        pageNum: 1,
        pageSize: 10
      }), 300)
    })
  }
  return request({
    url: '/api/mobile/tasks/list',
    method: 'GET',
    data: params
  })
}

// 获取任务详情（移动端）
export const getMobileTaskDetail = (id) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      const task = mockData.pendingTasks.find(t => t.id == id) || mockData.pendingTasks[0]
      setTimeout(() => resolve(task), 300)
    })
  }
  return request({
    url: `/api/mobile/tasks/${id}`,
    method: 'GET'
  })
}

// 接受任务（移动端）
export const acceptMobileTask = (id, assigneeId, assigneeName) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 300)
    })
  }
  return request({
    url: `/api/mobile/tasks/${id}/accept`,
    method: 'POST',
    data: { assigneeId, assigneeName }
  })
}

// 提交巡查反馈（移动端）
export const submitTaskFeedback = (id, data) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 600)
    })
  }
  return request({
    url: `/api/mobile/tasks/${id}/feedback`,
    method: 'POST',
    data
  })
}

// 完成任务（移动端）
export const completeMobileTask = (id, data) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 600)
    })
  }
  return request({
    url: `/api/mobile/tasks/${id}/complete`,
    method: 'POST',
    data
  })
}

// ==================== 问题上报接口（群众） ====================

// 获取我的上报列表（移动端）
export const getMyMobileReports = (reporter) => {
  const fallbackData = {
    public1: [
      { id: 1001, description: '水面上有明显蓝绿色藻类聚集，散发腥臭味', address: '海淀区颐和园昆明湖东岸', reservoirName: '昆明湖', severity: 'critical', status: 'pending', createdAt: new Date(Date.now() - 3600000).toISOString(), images: [] },
      { id: 1002, description: '河岸护栏破损，存在安全隐患', address: '朝阳区亮马河沿岸步道', reservoirName: '亮马河', severity: 'high', status: 'processing', createdAt: new Date(Date.now() - 86400000).toISOString(), images: [], processingResult: '已联系朝阳区河道管理所，预计本周内修复' },
      { id: 1003, description: '岸边有零星生活垃圾未清理，影响市容', address: '通州区大运河森林公园北段', reservoirName: '北运河', severity: 'medium', status: 'pending', createdAt: new Date(Date.now() - 172800000).toISOString(), images: [] },
      { id: 1004, description: '水面有油污带，怀疑上游有车辆清洗废水排入', address: '丰台区永定河引水渠卢沟桥段', reservoirName: '永定河引水渠', severity: 'high', status: 'completed', createdAt: new Date(Date.now() - 604800000).toISOString(), images: [], processingResult: '已溯源到附近汽修店，责令整改并处罚' },
      { id: 1005, description: '监测站附近水体浑浊，疑似施工泥浆水排入', address: '昌平区十三陵水库溢洪道下游', reservoirName: '十三陵水库', severity: 'critical', status: 'processing', createdAt: new Date(Date.now() - 10800000).toISOString(), images: [], processingResult: '已责令施工单位停工整改，水样已送检' },
      { id: 1006, description: '湖边围网有大面积破损，担心儿童误入', address: '房山区青龙湖水库主坝东侧', reservoirName: '青龙湖水库', severity: 'medium', status: 'pending', createdAt: new Date(Date.now() - 216000000).toISOString(), images: [] }
    ],
    inspector1: [
      { id: 2001, description: '巡查发现拦污栅前漂浮物堆积，影响过水', address: '密云水库白河主坝前', reservoirName: '密云水库', severity: 'high', status: 'processing', createdAt: new Date(Date.now() - 7200000).toISOString(), images: [], processingResult: '已调度清漂船，明日完成清理' },
      { id: 2002, description: '官厅水库库区围栏有两处被人为破坏', address: '河北省怀来县官厅水库大坝北侧', reservoirName: '官厅水库', severity: 'medium', status: 'pending', createdAt: new Date(Date.now() - 259200000).toISOString(), images: [] },
      { id: 2003, description: '怀柔水库水质自动监测站 pH 探头异常', address: '怀柔水库主坝监测站', reservoirName: '怀柔水库', severity: 'critical', status: 'completed', createdAt: new Date(Date.now() - 432000000).toISOString(), images: [], processingResult: '探头已更换校准，数据恢复正常' },
      { id: 2004, description: '海子水库溢洪道闸门锈蚀，需除锈保养', address: '平谷区海子水库溢洪道', reservoirName: '海子水库', severity: 'low', status: 'pending', createdAt: new Date(Date.now() - 345600000).toISOString(), images: [] }
    ]
  }
  if (CONFIG.USE_MOCK_DATA) {
    const key = reporter || 'public1'
    const data = fallbackData[key] || fallbackData['public1'] || []
    return new Promise((resolve) => {
      setTimeout(() => resolve(data), 300)
    })
  }
  return request({
    url: '/api/mobile/reports/my',
    method: 'GET',
    data: { reporter: reporter || '' }
  }).then(data => {
    if (!data || (Array.isArray(data) && data.length === 0) || (data.list && data.list.length === 0)) {
      const key = reporter || 'public1'
      return fallbackData[key] || fallbackData['public1']
    }
    if (Array.isArray(data)) return data
    return data.list || data
  }).catch(() => {
    const key = reporter || 'public1'
    return fallbackData[key] || fallbackData['public1']
  })
}

// 获取待处理上报列表（移动端）
export const getPendingMobileReports = () => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve([]), 300)
    })
  }
  return request({
    url: '/api/mobile/reports/pending',
    method: 'GET'
  })
}

// 获取上报详情（移动端）
export const getMobileReportDetail = (id) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({}), 300)
    })
  }
  return request({
    url: `/api/mobile/reports/${id}`,
    method: 'GET'
  })
}

// 提交问题上报（移动端 - 群众）
export const submitMobileReport = (data) => {
  if (CONFIG.USE_MOCK_DATA) {
    // 模拟模式也保存到 localReports，方便"我的上报"立即显示
    const localReports = uni.getStorageSync('localReports') || []
    console.log('保存本地记录，reporterRole:', data.reporterRole, 'username:', data.reporterRole === 'inspector' ? 'inspector1' : 'public1')
    localReports.unshift({
      id: 'local_' + Date.now(),
      description: data.description,
      address: data.address,
      reservoirName: data.reservoirName,
      severity: data.severity,
      status: 'pending',
      createdAt: new Date().toISOString(),
      foundTime: data.foundTime,
      images: data.photos ? JSON.parse(data.photos) : [],
      reporterName: data.reporterName,
      reporterRole: data.reporterRole,
      reporter: data.reporterRole === 'inspector' ? 'inspector1' : 'public1',
      // 同时保存原始 username 供后端返回时使用
      _submitTime: Date.now(),
    })
    uni.setStorageSync('localReports', localReports)
    return new Promise((resolve) => {
      setTimeout(() => resolve({ id: 'BG' + Date.now(), success: true }), 600)
    })
  }
  return request({
    url: '/api/mobile/reports',
    method: 'POST',
    data
  })
}

// 处理上报（移动端）
export const processMobileReport = (id, processingResult) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ success: true }), 600)
    })
  }
  return request({
    url: `/api/mobile/reports/${id}/process`,
    method: 'POST',
    data: { processingResult }
  })
}

// ==================== 仪表盘统计接口 ====================

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData.dashboardStats), 300)
    })
  }
  return request({
    url: '/api/dashboard/stats',
    method: 'GET'
  })
}

// 获取巡查统计
export const getPatrolStats = (date) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData.patrolStats), 300)
    })
  }
  return request({
    url: '/api/dashboard/patrol/stats',
    method: 'GET',
    data: { date }
  })
}

// 获取最新预警
export const getLatestWarnings = (limit = 5) => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve([]), 300)
    })
  }
  return request({
    url: '/api/dashboard/warnings/latest',
    method: 'GET',
    data: { limit }
  })
}

// tabBar 配置相关接口
export const getTabBarConfig = () => {
  if (CONFIG.USE_MOCK_DATA) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ list: [] }), 300)
    })
  }
  return request({
    url: '/api/tabbar/config',
    method: 'GET'
  })
}

export const updateTabBarBadge = (id, count) => {
  if (CONFIG.USE_MOCK_DATA) {
    return Promise.resolve({ success: true })
  }
  return request({
    url: '/api/tabbar/badge',
    method: 'POST',
    data: { id, count }
  })
}

// ==================== 默认导出 ====================

export default {
  // 认证
  mobileLogin,
  mobileRegister,
  getMobileProfile,
  updateProfile,
  changePassword,
  // 水库
  getMobileReservoirs,
  getMobileReservoirDetail,
  // 巡查打卡
  patrolCheckin,
  // 巡查记录
  getMobilePatrolRecords,
  getMobilePatrolRecordDetail,
  mobileReport,
  // 巡查任务
  getMobilePendingTasks,
  getMobileTaskList,
  getMobileTaskDetail,
  acceptMobileTask,
  submitTaskFeedback,
  completeMobileTask,
  // 问题上报
  getMyMobileReports,
  getPendingMobileReports,
  getMobileReportDetail,
  submitMobileReport,
  processMobileReport,
  // 仪表盘
  getDashboardStats,
  getPatrolStats,
  getLatestWarnings,
  // tabBar
  getTabBarConfig,
  updateTabBarBadge
}
