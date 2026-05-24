import request from '@/utils/request'

// ==================== Web端预警API ====================

// 获取预警列表 (Web端)
export function getWarningList(params) {
  return request({
    url: '/api/web/warnings',
    method: 'get',
    params
  })
}

// 获取预警详情 (Web端)
export function getWarningById(id) {
  return request({
    url: `/api/web/warnings/${id}`,
    method: 'get'
  })
}

// 更新预警 (Web端)
export function updateWarning(id, data) {
  return request({
    url: `/api/web/warnings/${id}`,
    method: 'put',
    data
  })
}

// 更新预警状态 (Web端)
export function updateWarningStatus(id, status) {
  return request({
    url: `/api/web/warnings/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 删除预警 (Web端)
export function deleteWarning(id) {
  return request({
    url: `/api/web/warnings/${id}`,
    method: 'delete'
  })
}

// ==================== 预警阈值API ====================

// 获取预警阈值配置
export function getWarningThresholds() {
  return request({
    url: '/api/web/warning-thresholds',
    method: 'get'
  })
}

// 更新预警阈值配置
export function updateWarningThresholds(data) {
  return request({
    url: '/api/web/warning-thresholds',
    method: 'put',
    data
  })
}
