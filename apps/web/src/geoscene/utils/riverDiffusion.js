/**
 * 一维河流对流–扩散模型（只向下游）
 * C(x,t) = m/(H·W·√(4πEx·t)) · exp(-(x-vx·t)²/(4Ex·t)) · exp(-K·t)
 */
import { xyToLonLat } from './lonLat'

export class RiverDiffusionModel {
  /**
   * @param {object} params
   * @param {number} [params.Ex] 纵向弥散系数 m²/s
   * @param {number} [params.vx] 流速 m/s
   * @param {number} [params.H] 水深 m
   * @param {number} [params.W] 河宽 m
   * @param {number} [params.K] 一级衰减系数 1/s
   * @param {number} [params.m] 污染物质量 kg
   */
  constructor(params = {}) {
    this.Ex = params.Ex || 10
    this.vx = params.vx || 1.5
    this.H = params.H || 3
    this.W = params.W || 20
    this.K = params.K || 1e-5
    this.m = params.m || 100
    this.constant = this.m / (this.H * this.W * Math.sqrt(4 * Math.PI * this.Ex))
  }

  /**
   * @param {number} t 时间(秒)
   * @param {number} x 沿程距离(米)，污染源为 0，下游为正
   * @returns {number} 浓度 mg/L 量级近似值
   */
  calculate(t, x) {
    if (t <= 0.001 || x < 0 || this.Ex <= 0) return 0
    const expected = this.vx * t
    const plumeHalf = 3 * Math.sqrt(4 * this.Ex * t)
    if (x > expected + plumeHalf) return 0

    try {
      const part1 = this.constant / Math.sqrt(t)
      const part2 = ((x - this.vx * t) ** 2) / (4 * this.Ex * t)
      const c = Math.exp(-this.K * t) * part1 * Math.exp(-part2)
      if (!Number.isFinite(c) || c < 1e-6) return 0
      return Math.round(c * 1e6) / 1e6
    } catch {
      return 0
    }
  }
}

/**
 * 从 Polyline 提取路径坐标（统一为 WGS84 经纬度）
 * @param {import('@geoscene/core/geometry/Polyline').default} geometry
 * @returns {Array<[number, number]>}
 */
export function extractPolylineCoords(geometry) {
  if (!geometry) return []
  const paths = geometry.paths || []
  const sr = geometry.spatialReference
  const coords = []
  for (const path of paths) {
    for (const pt of path) {
      const x = Array.isArray(pt) ? pt[0] : pt.x
      const y = Array.isArray(pt) ? pt[1] : pt.y
      if (x == null || y == null) continue
      const ll = xyToLonLat(x, y, sr)
      if (ll) coords.push([ll.lng, ll.lat])
    }
  }
  return coords
}

/**
 * 两点间大致距离（米）
 */
export function haversineMeters(lng1, lat1, lng2, lat2) {
  const R = 6371000
  const toRad = (d) => (d * Math.PI) / 180
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.min(1, Math.sqrt(a)))
}

/**
 * 沿折线按间距采样，返回带沿程距离的点列
 * @param {Array<[number, number]>} coords
 * @param {number} spacingM
 * @returns {Array<{ lng: number, lat: number, distance: number }>}
 */
export function samplePolyline(coords, spacingM = 200) {
  if (!coords?.length) return []
  const points = [{ lng: coords[0][0], lat: coords[0][1], distance: 0 }]
  let acc = 0
  let last = coords[0]

  for (let i = 1; i < coords.length; i++) {
    const cur = coords[i]
    let segLen = haversineMeters(last[0], last[1], cur[0], cur[1])
    if (segLen < 1e-3) continue

    let remain = spacingM - (acc % spacingM)
    let used = 0
    while (used + remain <= segLen + 1e-6) {
      used += remain
      const ratio = used / segLen
      const lng = last[0] + (cur[0] - last[0]) * ratio
      const lat = last[1] + (cur[1] - last[1]) * ratio
      const dist = points[0].distance + haversineMeters(points[0].lng, points[0].lat, lng, lat)
      // 用累计弧长更稳
      const prev = points[points.length - 1]
      const step = haversineMeters(prev.lng, prev.lat, lng, lat)
      points.push({ lng, lat, distance: prev.distance + step })
      remain = spacingM
    }
    acc += segLen
    last = cur
  }

  const end = coords[coords.length - 1]
  const lastPt = points[points.length - 1]
  if (haversineMeters(lastPt.lng, lastPt.lat, end[0], end[1]) > spacingM * 0.3) {
    const prev = points[points.length - 1]
    points.push({
      lng: end[0],
      lat: end[1],
      distance: prev.distance + haversineMeters(prev.lng, prev.lat, end[0], end[1])
    })
  }

  return points
}

/**
 * 在折线上找最近点，返回沿程距离与坐标
 * @param {Array<[number, number]>} coords
 * @param {number} lng
 * @param {number} lat
 */
export function nearestOnPolyline(coords, lng, lat) {
  if (!coords?.length) return null
  let best = { lng: coords[0][0], lat: coords[0][1], distance: 0, distToClick: Infinity }
  let chain = 0

  for (let i = 1; i < coords.length; i++) {
    const a = coords[i - 1]
    const b = coords[i]
    const seg = haversineMeters(a[0], a[1], b[0], b[1])
    if (seg < 1e-3) continue

    // 投影到线段（平面近似）
    const ax = a[0]
    const ay = a[1]
    const bx = b[0]
    const by = b[1]
    const dx = bx - ax
    const dy = by - ay
    const t = Math.max(0, Math.min(1, ((lng - ax) * dx + (lat - ay) * dy) / (dx * dx + dy * dy || 1)))
    const px = ax + t * dx
    const py = ay + t * dy
    const d = haversineMeters(lng, lat, px, py)
    if (d < best.distToClick) {
      best = {
        lng: px,
        lat: py,
        distance: chain + t * seg,
        distToClick: d
      }
    }
    chain += seg
  }
  return best
}

/**
 * 以污染源沿程位置为 0，只保留下游采样点
 * @param {Array<{ lng: number, lat: number, distance: number }>} samples
 * @param {number} sourceDistance
 */
export function downstreamSamples(samples, sourceDistance) {
  return samples
    .map((p) => ({
      lng: p.lng,
      lat: p.lat,
      x: p.distance - sourceDistance
    }))
    .filter((p) => p.x >= -1)
    .map((p) => ({ ...p, x: Math.max(0, p.x) }))
}
