import UniqueValueRenderer from '@geoscene/core/renderers/UniqueValueRenderer'
import { applyLayerRenderer, hexToRgba } from '@/geoscene/layers/layerStyle'

function createSymbol(layerKey, color) {
  if (layerKey === 'waterLines') {
    return {
      type: 'simple-line',
      color: hexToRgba(color, 1),
      width: 2.5
    }
  }
  if (layerKey === 'settlements') {
    return {
      type: 'simple-marker',
      color: hexToRgba(color, 0.95),
      size: 10,
      outline: { color: '#ffffff', width: 1.5 }
    }
  }
  return {
    type: 'simple-fill',
    color: hexToRgba(color, 0.75),
    outline: { color: hexToRgba(color, 1), width: 1.5 }
  }
}

/**
 * 按名称映射颜色（用于水库等业务图层分级渲染）
 */
export function applyNameColorRenderer(layer, layerKey, nameField, nameColorMap) {
  if (!layer || !nameField) return
  const uniqueValueInfos = Object.entries(nameColorMap).map(([name, color]) => ({
    value: name,
    symbol: createSymbol(layerKey, color)
  }))

  layer.renderer = new UniqueValueRenderer({
    field: nameField,
    uniqueValueInfos,
    defaultSymbol: createSymbol(layerKey, '#bdbdbd')
  })
}

/**
 * 监测点 GraphicsLayer 按值着色
 */
export function applyMonitoringClassBreaks(layer, nameValueMap, grades) {
  if (!layer) return
  layer.graphics.forEach((graphic) => {
    const name = graphic.attributes?.reservoirName
    const value = nameValueMap[name]
    if (value == null) return
    const grade = grades.find((g) => value >= g.min && value <= g.max)
    if (!grade) return
    graphic.symbol = {
      type: 'simple-marker',
      color: grade.color,
      size: 11,
      outline: { color: '#ffffff', width: 1.5 }
    }
  })
}

export function resetLayerStyle(layer, layerKey, color) {
  if (!layer) return
  applyLayerRenderer(layer, layerKey, color)
}
