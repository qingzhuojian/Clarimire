import request from '@/utils/request'

// ==================== Dashboard API ====================

// 获取首页统计数据
export function getDashboardStats() {
  return request({
    url: '/api/dashboard/stats',
    method: 'get'
  })
}

// 获取氨氮指数趋势
export function getWaterQualityTrend(params) {
  return request({
    url: '/api/dashboard/water-quality/trend',
    method: 'get',
    params
  })
}

// 获取最新预警
export function getLatestWarnings(limit) {
  return request({
    url: '/api/dashboard/warnings/latest',
    method: 'get',
    params: { limit }
  })
}

// 获取巡查统计
export function getPatrolStats(date) {
  return request({
    url: '/api/dashboard/patrol/stats',
    method: 'get',
    params: { date }
  })
}
