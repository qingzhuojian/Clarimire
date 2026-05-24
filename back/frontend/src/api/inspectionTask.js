import request from '@/utils/request'

// ==================== Web端任务API ====================

// 获取任务列表 (Web端)
export function getTaskList(params) {
  return request({
    url: '/api/web/tasks',
    method: 'get',
    params
  })
}

// 获取任务详情 (Web端)
export function getTaskById(id) {
  return request({
    url: `/api/web/tasks/${id}`,
    method: 'get'
  })
}

// 创建任务 (Web端)
export function createTask(data) {
  return request({
    url: '/api/web/tasks/create',
    method: 'post',
    data
  })
}

// 更新任务 (Web端)
export function updateTask(id, data) {
  return request({
    url: `/api/web/tasks/${id}`,
    method: 'put',
    data
  })
}

// 删除任务 (Web端)
export function deleteTask(id) {
  return request({
    url: `/api/web/tasks/${id}`,
    method: 'delete'
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
