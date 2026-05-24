import request from '@/utils/request'

// ==================== Web端巡查记录API ====================

// 获取巡查记录列表 (Web端)
export function getPatrolRecords(params) {
  return request({
    url: '/api/web/patrol/records',
    method: 'get',
    params
  })
}

// 获取巡查记录详情 (Web端)
export function getPatrolRecordById(id) {
  return request({
    url: `/api/web/patrol/records/${id}`,
    method: 'get'
  })
}

// 派发任务 (Web端)
export function assignPatrolTask(data) {
  return request({
    url: '/api/web/patrol/assign',
    method: 'post',
    data
  })
}

// 更新巡查记录 (Web端)
export function updatePatrolRecord(data) {
  return request({
    url: `/api/web/patrol/records/${data.id}`,
    method: 'put',
    data
  })
}

// 更新巡查记录状态 (Web端)
export function updatePatrolStatus(id, data) {
  return request({
    url: `/api/web/patrol/records/${id}/status`,
    method: 'put',
    params: data
  })
}

// 删除巡查记录 (Web端)
export function deletePatrolRecord(id) {
  return request({
    url: `/api/web/patrol/records/${id}`,
    method: 'delete'
  })
}

// ==================== 移动端巡查API ====================

// 定位签到 (移动端)
export function mobileCheckin(data) {
  return request({
    url: '/api/mobile/patrol/checkin',
    method: 'post',
    data
  })
}

// 获取移动端巡查记录列表
export function getMobilePatrolRecords(params) {
  return request({
    url: '/api/mobile/patrol/records',
    method: 'get',
    params
  })
}

// 获取移动端巡查记录详情
export function getMobilePatrolRecordById(id) {
  return request({
    url: `/api/mobile/patrol/records/${id}`,
    method: 'get'
  })
}

// 提交问题上报 (移动端)
export function mobileReport(data) {
  return request({
    url: '/api/mobile/patrol/report',
    method: 'post',
    data
  })
}
