import request from '@/utils/request'

// ==================== 角色API ====================

// 获取角色列表
export function getRoleList() {
  return request({
    url: '/api/web/roles',
    method: 'get'
  })
}

// 获取角色详情
export function getRoleById(id) {
  return request({
    url: `/api/web/roles/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/api/web/roles/create',
    method: 'post',
    data
  })
}

// 更新角色权限
export function updateRolePermissions(id, data) {
  return request({
    url: `/api/web/roles/${id}/permissions`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/api/web/roles/${id}`,
    method: 'delete'
  })
}
