import { execute as centroidExecute } from '@geoscene/core/geometry/operators/centroidOperator'

/**
 * 获取几何中心（GeoScene 无 geometryEngine.centroid，使用 centroidOperator 或 extent.center）
 */
export function getGeometryCentroid(geometry) {
  if (!geometry) return null

  if (geometry.type === 'point') {
    return geometry
  }

  try {
    const centroid = centroidExecute(geometry)
    if (centroid) return centroid
  } catch {
    // fallback below
  }

  return geometry.extent?.center || null
}
