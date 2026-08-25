/**
 * 简化的二维湖泊扩散模型
 */
export class LakeDiffusionModel {
  /**
   * @param {object} params - 模型参数
   */
  constructor(params = {}) {
    this.Ex = params.Ex || 10
    this.Ey = params.Ey || 10
    this.vx = params.vx || 0.1
    this.vy = params.vy || 0.05
    this.m = params.m || 1000
    this.K = params.K || 0.00001
  }

  /**
   * 计算指定时刻、位置的浓度
   * @param {number} t - 时间(秒)
   * @param {number} x - x坐标(米)
   * @param {number} y - y坐标(米)
   * @returns {number}
   */
  calculate(t, x, y) {
    if (t <= 0) return 0
    const denom = 4 * Math.PI * t * Math.sqrt(this.Ex * this.Ey)
    const expTerm = Math.exp(
      -((x - this.vx * t) ** 2) / (4 * this.Ex * t) -
      ((y - this.vy * t) ** 2) / (4 * this.Ey * t)
    )
    const decay = Math.exp(-this.K * t)
    return (this.m / denom) * expTerm * decay
  }
}

/**
 * 污染物浓度色：绿 → 黄 → 橙 → 红（与图例一致），偏不透明避免发白
 * @param {number} value - 浓度
 * @param {number} max - 最大浓度（建议用全局峰值，便于看出时间变化）
 * @returns {number[]} rgba
 */
export function concentrationColor(value, max) {
  if (max <= 0) return [0, 200, 0, 0.6]
  const t = Math.min(1, Math.max(0, value / max))
  // 略抬高中高段对比，低浓度也不至于发白透明
  const ratio = Math.pow(t, 0.7)
  let r
  let g
  let b
  if (ratio < 0.25) {
    const u = ratio / 0.25
    r = Math.round(0 + 255 * u)
    g = 220
    b = 0
  } else if (ratio < 0.5) {
    const u = (ratio - 0.25) / 0.25
    r = 255
    g = Math.round(220 + 35 * u)
    b = 0
  } else if (ratio < 0.75) {
    const u = (ratio - 0.5) / 0.25
    r = 255
    g = Math.round(255 - 127 * u)
    b = 0
  } else {
    const u = (ratio - 0.75) / 0.25
    r = 255
    g = Math.round(128 * (1 - u))
    b = 0
  }
  const a = 0.62 + ratio * 0.33
  return [r, g, b, a]
}

/** 经纬度位移到米（近似） */
export function metersOffset(lng, lat, dxM, dyM) {
  return {
    lng: lng + dxM / (111320 * Math.cos((lat * Math.PI) / 180)),
    lat: lat + dyM / 110540
  }
}

/**
 * 在包围盒内按间距生成候选网格点（米），再由调用方做面内裁剪
 * @param {{ longitude: number, latitude: number }} center
 * @param {number} halfExtentM
 * @param {number} spacingM
 */
export function buildLocalGrid(center, halfExtentM, spacingM = 400) {
  const points = []
  for (let dx = -halfExtentM; dx <= halfExtentM; dx += spacingM) {
    for (let dy = -halfExtentM; dy <= halfExtentM; dy += spacingM) {
      const p = metersOffset(center.longitude, center.latitude, dx, dy)
      points.push({ lng: p.lng, lat: p.lat, xM: dx, yM: dy })
    }
  }
  return points
}
