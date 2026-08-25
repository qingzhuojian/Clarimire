import * as webMercatorUtils from '@geoscene/core/geometry/support/webMercatorUtils'

/**
 * 判断是否已是经纬度
 * @param {number} x
 * @param {number} y
 */
export function looksLikeLonLat(x, y) {
  return Number.isFinite(x) && Number.isFinite(y) && Math.abs(x) <= 180 && Math.abs(y) <= 90
}

/**
 * 将任意平面/地理坐标规范为 WGS84 经纬度
 * @param {number} x
 * @param {number} y
 * @param {{ wkid?: number, latestWkid?: number }|null} [spatialReference]
 * @returns {{ lng: number, lat: number }|null}
 */
export function xyToLonLat(x, y, spatialReference = null) {
  if (!Number.isFinite(x) || !Number.isFinite(y)) return null
  if (looksLikeLonLat(x, y)) return { lng: x, lat: y }

  const wkid = spatialReference?.wkid || spatialReference?.latestWkid
  if (wkid === 4326) return { lng: x, lat: y }

  // Web Mercator 或明显超出经纬度范围的值
  if (wkid === 3857 || wkid === 102100 || wkid === 102113 || Math.abs(x) > 180 || Math.abs(y) > 90) {
    try {
      const pair = webMercatorUtils.xyToLngLat(x, y)
      if (Array.isArray(pair) && pair.length >= 2) {
        return { lng: pair[0], lat: pair[1] }
      }
    } catch {
      // fall through
    }
  }
  return { lng: x, lat: y }
}

/**
 * 从 MapView 的 mapPoint 取经纬度
 * @param {import('@geoscene/core/geometry/Point').default} mapPoint
 */
export function lonLatFromMapPoint(mapPoint) {
  if (!mapPoint) return null
  if (
    Number.isFinite(mapPoint.longitude) &&
    Number.isFinite(mapPoint.latitude) &&
    looksLikeLonLat(mapPoint.longitude, mapPoint.latitude)
  ) {
    return { lng: mapPoint.longitude, lat: mapPoint.latitude }
  }
  return xyToLonLat(mapPoint.x, mapPoint.y, mapPoint.spatialReference)
}

/**
 * 从 Point / extent.center 取经纬度
 * @param {{ longitude?: number, latitude?: number, x?: number, y?: number, spatialReference?: object }} pt
 * @param {object} [fallbackSr]
 */
export function lonLatFromPointLike(pt, fallbackSr = null) {
  if (!pt) return null
  if (Number.isFinite(pt.longitude) && Number.isFinite(pt.latitude) && looksLikeLonLat(pt.longitude, pt.latitude)) {
    return { lng: pt.longitude, lat: pt.latitude }
  }
  return xyToLonLat(pt.x ?? pt.longitude, pt.y ?? pt.latitude, pt.spatialReference || fallbackSr)
}

export function isValidLonLat(lng, lat) {
  return looksLikeLonLat(lng, lat)
}
