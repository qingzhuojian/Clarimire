import request from '@/utils/request'

// ==================== 移动端水库API ====================

// 获取水库列表 (移动端)
export function getMobileReservoirList() {
  return request({
    url: '/api/mobile/reservoirs',
    method: 'get'
  })
}

// 获取水库详情 (移动端)
export function getMobileReservoirById(id) {
  return request({
    url: `/api/mobile/reservoirs/${id}`,
    method: 'get'
  })
}

// ==================== 移动端认证API ====================

// 移动端登录
export function mobileLogin(data) {
  return request({
    url: '/api/mobile/auth/login',
    method: 'post',
    data
  })
}

// 移动端注册
export function mobileRegister(data) {
  return request({
    url: '/api/mobile/auth/register',
    method: 'post',
    data
  })
}

// 获取移动端用户信息
export function getMobileProfile(token) {
  return request({
    url: '/api/mobile/auth/profile',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// ==================== 移动端任务API ====================

// 获取待接受任务 (移动端)
export function getMobilePendingTasks(params) {
  return request({
    url: '/api/mobile/tasks/pending',
    method: 'get',
    params
  })
}

// 获取移动端任务列表
export function getMobileTaskList(params) {
  return request({
    url: '/api/mobile/tasks/list',
    method: 'get',
    params
  })
}

// 获取移动端任务详情
export function getMobileTaskById(id) {
  return request({
    url: `/api/mobile/tasks/${id}`,
    method: 'get'
  })
}

// 接受任务 (移动端)
export function acceptMobileTask(id, data) {
  return request({
    url: `/api/mobile/tasks/${id}/accept`,
    method: 'post',
    params: data
  })
}

// 提交任务反馈 (移动端)
export function submitMobileTaskFeedback(id, data) {
  return request({
    url: `/api/mobile/tasks/${id}/feedback`,
    method: 'post',
    data
  })
}

// 完成任务 (移动端)
export function completeMobileTask(id, data) {
  return request({
    url: `/api/mobile/tasks/${id}/complete`,
    method: 'post',
    data
  })
}

// ==================== 移动端上报API ====================

// 获取我的上报 (移动端)
export function getMyReports(reporter) {
  return request({
    url: '/api/mobile/reports/my',
    method: 'get',
    params: { reporter }
  })
}

// 获取待处理上报 (移动端)
export function getPendingReports() {
  return request({
    url: '/api/mobile/reports/pending',
    method: 'get'
  })
}

// 获取移动端上报详情
export function getMobileReportById(id) {
  return request({
    url: `/api/mobile/reports/${id}`,
    method: 'get'
  })
}

// 创建移动端上报
export function createMobileReport(data) {
  return request({
    url: '/api/mobile/reports',
    method: 'post',
    data
  })
}

// 处理上报 (移动端)
export function processMobileReport(id, data) {
  return request({
    url: `/api/mobile/reports/${id}/process`,
    method: 'post',
    data
  })
}
