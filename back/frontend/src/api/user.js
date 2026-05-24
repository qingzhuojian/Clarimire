import request from '@/utils/request'

// ==================== Web端用户API ====================

// 获取用户列表 (Web端)
export function getUserList(params) {
  return request({
    url: '/api/web/users',
    method: 'get',
    params
  })
}

// 获取用户详情 (Web端)
export function getUserById(id) {
  return request({
    url: `/api/web/users/${id}`,
    method: 'get'
  })
}

// 创建用户 (Web端)
export function createUser(data) {
  return request({
    url: '/api/web/users/create',
    method: 'post',
    data
  })
}

// 更新用户 (Web端)
export function updateUser(id, data) {
  return request({
    url: `/api/web/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户 (Web端)
export function deleteUser(id) {
  return request({
    url: `/api/web/users/${id}`,
    method: 'delete'
  })
}

// 重置密码 (Web端)
export function resetUserPassword(id) {
  return request({
    url: `/api/web/users/${id}/reset-password`,
    method: 'post'
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
