import request from '@/utils/request'

// ==================== Web端上报API ====================

// 获取上报列表 (Web端)
export function getReportList(params) {
  return request({
    url: '/api/web/reports',
    method: 'get',
    params
  })
}

// 获取上报详情 (Web端)
export function getReportById(id) {
  return request({
    url: `/api/web/reports/${id}`,
    method: 'get'
  })
}

// 创建上报 (Web端)
export function createReport(data) {
  return request({
    url: '/api/web/reports',
    method: 'post',
    data
  })
}

// 更新上报 (Web端)
export function updateReport(id, data) {
  return request({
    url: `/api/web/reports/${id}`,
    method: 'put',
    data
  })
}

// 将上报转为任务 (Web端)
export function convertReportToTask(id, data) {
  return request({
    url: `/api/web/reports/${id}/to-task`,
    method: 'post',
    data
  })
}

// 标记已解决 (Web端)
export function resolveReport(id, data) {
  return request({
    url: `/api/web/reports/${id}/resolve`,
    method: 'post',
    data
  })
}

// 指派上报 (Web端)
export function assignReport(id, data) {
  return request({
    url: `/api/web/reports/${id}/assign`,
    method: 'post',
    data
  })
}

// 删除上报 (Web端)
export function deleteReport(id) {
  return request({
    url: `/api/web/reports/${id}`,
    method: 'delete'
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
