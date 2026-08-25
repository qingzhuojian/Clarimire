import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'
import Point from '@geoscene/core/geometry/Point'
import { normalizeReservoirName } from '../reservoirNames'
import geosceneConfig from '@/config/geoscene'
import { shouldApplyGcj02Offset, wgs84ToGcj02 } from '../utils/coordTransform'
import { getGeometryCentroid } from '../utils/getGeometryCentroid'

/**
 * 从水库面要素生成监测点 GraphicsLayer。
 * 点位一律取水库面几何中心（centroid），不使用随意/静态 GeoJSON 坐标。
 * @param {import('@geoscene/core/layers/FeatureLayer').default} reservoirLayer
 * @param {Array} monitorData - 监测断面属性列表
 * @returns {Promise<import('@geoscene/core/layers/GraphicsLayer').default>}
 */
export async function createMonitoringLayer(reservoirLayer, monitorData = [], options = {}) {
  if (!reservoirLayer || reservoirLayer.destroyed) {
    return new GraphicsLayer({
      title: '监测点',
      listMode: 'show',
      visible: options.visible !== false
    })
  }

  await reservoirLayer.load()
  if (reservoirLayer.destroyed) {
    return new GraphicsLayer({
      title: '监测点',
      listMode: 'show',
      visible: options.visible !== false
    })
  }

  const result = await reservoirLayer.queryFeatures({
    where: '1=1',
    outFields: ['*'],
    returnGeometry: true
  })

  const latestByReservoir = buildLatestMonitorMap(monitorData)
  const markerColor = options.color || geosceneConfig.symbols.monitoring?.color || '#FF5722'
  const graphicsLayer = new GraphicsLayer({
    title: '监测点',
    listMode: 'show',
    visible: options.visible !== false
  })

  for (const feature of result.features) {
    const attrs = feature.attributes || {}
    const nameField = ['name', 'NAME', '库名', '名称'].find((k) => attrs[k])
    const rawName = nameField ? attrs[nameField] : ''
    const reservoirName = normalizeReservoirName(rawName)
    const centroid = getGeometryCentroid(feature.geometry)
    if (!centroid) continue

    let lng = centroid.longitude ?? centroid.x
    let lat = centroid.latitude ?? centroid.y
    if (lng == null || lat == null) continue
    // 已对齐的内存图层坐标已是 GCJ-02，服务端 WGS84 图层才需偏移
    if (shouldApplyGcj02Offset() && reservoirLayer.url) {
      ;[lng, lat] = wgs84ToGcj02(lng, lat)
    }

    const monitor = latestByReservoir[reservoirName] || {}
    graphicsLayer.add(new Graphic({
      geometry: new Point({
        longitude: lng,
        latitude: lat,
        spatialReference: centroid.spatialReference
      }),
      symbol: {
        type: 'simple-marker',
        color: markerColor,
        size: 10,
        outline: { color: '#ffffff', width: 1.5 }
      },
      attributes: {
        reservoirName,
        monitorPointName: monitor.monitorPointName || reservoirName,
        ammoniaNitrogen: monitor.ammoniaNitrogen,
        cod: monitor.cod,
        totalPhosphorus: monitor.totalPhosphorus,
        year: monitor.year,
        month: monitor.month
      },
      popupTemplate: {
        title: '{reservoirName} 监测点',
        content: [{
          type: 'fields',
          fieldInfos: [
            { fieldName: 'ammoniaNitrogen', label: '氨氮(mg/L)' },
            { fieldName: 'cod', label: 'COD(mg/L)' },
            { fieldName: 'totalPhosphorus', label: '总磷(mg/L)' },
            { fieldName: 'year', label: '年份' },
            { fieldName: 'month', label: '月份' }
          ]
        }]
      }
    }))
  }

  return graphicsLayer
}

/**
 * @param {Array} monitorData
 * @returns {Record<string, object>}
 */
function buildLatestMonitorMap(monitorData) {
  const map = {}
  for (const item of monitorData) {
    const key = normalizeReservoirName(item.reservoirName)
    const existing = map[key]
    if (!existing || (item.year * 100 + item.month) > (existing.year * 100 + existing.month)) {
      map[key] = item
    }
  }
  return map
}
