import SimpleRenderer from '@geoscene/core/renderers/SimpleRenderer'
import SimpleFillSymbol from '@geoscene/core/symbols/SimpleFillSymbol'
import SimpleLineSymbol from '@geoscene/core/symbols/SimpleLineSymbol'
import SimpleMarkerSymbol from '@geoscene/core/symbols/SimpleMarkerSymbol'
import geosceneConfig from '@/config/geoscene'
import { replaceLayerWithGcj02Aligned } from './alignFeatureLayerToGcj02'

function colorToHex(color) {
  if (!color) return '#1677ff'
  if (typeof color === 'string') return color
  if (Array.isArray(color)) {
    const [r, g, b] = color
    return `#${[r, g, b].map((v) => Number(v).toString(16).padStart(2, '0')).join('')}`
  }
  return '#1677ff'
}

/** 与 geoscene.js 发布任务底图样式一致 */
export const DEFAULT_LAYER_COLORS = {
  districts: colorToHex(geosceneConfig.symbols.district.color),
  waterAreas: colorToHex(geosceneConfig.symbols.waterArea.color),
  reservoirs: colorToHex(geosceneConfig.symbols.reservoir.color),
  waterLines: colorToHex(geosceneConfig.symbols.waterLine.color),
  settlements: colorToHex(geosceneConfig.symbols.settlement.color)
}

export const DEFAULT_MONITORING_COLOR = colorToHex(geosceneConfig.symbols.monitoring?.color || '#FF5722')

export const LAYER_PANEL_ORDER = geosceneConfig.layerPanelOrder || Object.keys(geosceneConfig.layerMeta)

export function hexToRgba(hex, alpha = 0.6) {
  const h = hex.replace('#', '')
  return [
    parseInt(h.slice(0, 2), 16),
    parseInt(h.slice(2, 4), 16),
    parseInt(h.slice(4, 6), 16),
    alpha
  ]
}

function outlineHex(key, fillHex) {
  const symbols = geosceneConfig.symbols
  if (key === 'districts') return symbols.district.outline?.color || fillHex
  if (key === 'waterAreas') return colorToHex(symbols.waterArea.outline?.color) || fillHex
  if (key === 'reservoirs') return colorToHex(symbols.reservoir.outline?.color) || fillHex
  return fillHex
}

/**
 * 为 FeatureLayer 应用颜色渲染器
 */
export function applyLayerRenderer(layer, key, color) {
  if (!layer || !color) return

  if (key === 'districts') {
    layer.renderer = new SimpleRenderer({
      symbol: new SimpleFillSymbol({
        color: hexToRgba(color, 0.12),
        outline: { color: outlineHex(key, color), width: 2 }
      })
    })
    return
  }

  if (key === 'waterLines') {
    layer.renderer = new SimpleRenderer({
      symbol: new SimpleLineSymbol({ color, width: geosceneConfig.symbols.waterLine.width || 2 })
    })
    return
  }

  if (key === 'settlements') {
    const sym = geosceneConfig.symbols.settlement
    layer.renderer = new SimpleRenderer({
      symbol: new SimpleMarkerSymbol({
        color,
        size: sym.size || 7,
        outline: sym.outline || { color: '#ffffff', width: 1 }
      })
    })
    return
  }

  const fillAlpha = key === 'districts' ? 0.3 : 0.55
  layer.renderer = new SimpleRenderer({
    symbol: new SimpleFillSymbol({
      color: hexToRgba(color, fillAlpha),
      outline: { color: outlineHex(key, color), width: 1 }
    })
  })
}

export async function loadBusinessLayers(layers, colorMap = DEFAULT_LAYER_COLORS, map = null) {
  const results = await Promise.all(
    Object.entries(layers).map(async ([key, layer]) => {
      try {
        if (!layer || layer.destroyed) {
          return { key, ok: false, cancelled: true }
        }
        await layer.load()
        if (layer.destroyed) {
          return { key, ok: false, cancelled: true }
        }
        let activeLayer = layer
        if (map) {
          activeLayer = await replaceLayerWithGcj02Aligned(map, layers, key)
        }
        if (!activeLayer || activeLayer.destroyed) {
          return { key, ok: false, cancelled: true }
        }
        applyLayerRenderer(activeLayer, key, colorMap[key] || DEFAULT_LAYER_COLORS[key])
        return { key, ok: activeLayer.loadStatus !== 'failed' }
      } catch (err) {
        const msg = String(err?.message || err?.name || '')
        if (msg.includes('destroyed') || err?.name === 'load:instance-destroyed') {
          return { key, ok: false, cancelled: true }
        }
        console.warn('图层加载失败:', layer?.title || key, err)
        return { key, ok: false, error: err }
      }
    })
  )
  return results
}
