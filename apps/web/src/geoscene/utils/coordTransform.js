/**
 * WGS84 / CGCS2000 ↔ GCJ-02
 * 仅高德底图需要偏移；天地图/OSM/无底图禁止偏移
 */
import geosceneConfig from '@/config/geoscene'

const PI = Math.PI
const A = 6378245.0
const EE = 0.00669342162296594323

export function shouldApplyGcj02Offset() {
  if (geosceneConfig.map?.useGcj02Offset === false) return false
  const basemap = geosceneConfig.map?.basemap || ''
  return basemap === 'gaode' || basemap === 'gaode-satellite'
}

function outOfChina(lng, lat) {
  return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}

export function wgs84ToGcj02(lng, lat) {
  if (outOfChina(lng, lat)) return [lng, lat]
  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = (lat / 180.0) * PI
  let magic = Math.sin(radLat)
  magic = 1 - EE * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / (((A * (1 - EE)) / (magic * sqrtMagic)) * PI)
  dLng = (dLng * 180.0) / ((A / sqrtMagic) * Math.cos(radLat) * PI)
  return [lng + dLng, lat + dLat]
}

function transformLat(x, y) {
  let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0) / 3.0
  ret += ((20.0 * Math.sin(y * PI) + 40.0 * Math.sin((y / 3.0) * PI)) * 2.0) / 3.0
  ret += ((160.0 * Math.sin((y / 12.0) * PI) + 320 * Math.sin((y * PI) / 30.0)) * 2.0) / 3.0
  return ret
}

function transformLng(x, y) {
  let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0) / 3.0
  ret += ((20.0 * Math.sin(x * PI) + 40.0 * Math.sin((x / 3.0) * PI)) * 2.0) / 3.0
  ret += ((150.0 * Math.sin((x / 12.0) * PI) + 300.0 * Math.sin((x / 30.0) * PI)) * 2.0) / 3.0
  return ret
}

export function transformCenter(center) {
  return wgs84ToGcj02(center[0], center[1])
}

function transformCoordPair(coord) {
  const [lng, lat] = wgs84ToGcj02(coord[0], coord[1])
  coord[0] = lng
  coord[1] = lat
}

export function transformGeometryToGcj02(geometry) {
  if (!geometry) return geometry
  const g = geometry.clone()
  switch (g.type) {
    case 'point': {
      const lng = g.longitude ?? g.x
      const lat = g.latitude ?? g.y
      const [gcjLng, gcjLat] = wgs84ToGcj02(lng, lat)
      if (g.longitude != null) g.longitude = gcjLng
      if (g.latitude != null) g.latitude = gcjLat
      if (g.x != null) g.x = gcjLng
      if (g.y != null) g.y = gcjLat
      break
    }
    case 'multipoint':
      g.points.forEach(transformCoordPair)
      break
    case 'polyline':
      g.paths.forEach((path) => path.forEach(transformCoordPair))
      break
    case 'polygon':
      g.rings.forEach((ring) => ring.forEach(transformCoordPair))
      break
    default:
      break
  }
  return g
}

export async function transformGeometryForGaode(geometry, sourceCrs = 'wgs84') {
  if (!geometry || !shouldApplyGcj02Offset() || sourceCrs === 'gcj02') {
    return geometry?.clone?.() || geometry
  }
  return transformGeometryToGcj02(geometry)
}
