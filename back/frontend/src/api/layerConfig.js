import request from '@/utils/request'

// ==================== 图层配置API ====================

// 获取图层配置列表
export function getLayerConfigList() {
  return request({
    url: '/api/web/layer-configs',
    method: 'get'
  })
}

// 更新图层配置
export function updateLayerConfig(data) {
  return request({
    url: '/api/web/layer-configs',
    method: 'put',
    data
  })
}
