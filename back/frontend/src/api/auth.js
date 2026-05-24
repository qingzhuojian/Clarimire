import request from '@/utils/request'

// ==================== Auth API ====================

// 登录
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getProfile(token) {
  return request({
    url: '/api/auth/profile',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 修改密码
export function changePassword(token, data) {
  return request({
    url: '/api/auth/password',
    method: 'put',
    data,
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 更新个人信息
export function updateProfile(token, data) {
  return request({
    url: '/api/auth/profile',
    method: 'put',
    data,
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}
