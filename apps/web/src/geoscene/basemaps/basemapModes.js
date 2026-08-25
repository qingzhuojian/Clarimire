import Basemap from '@geoscene/core/Basemap'
import { createTiandituBasemap, getTiandituToken } from './createTiandituBasemap'

export const BASEMAP_MODES = [
  { id: 'tianditu', label: '标准地图' },
  { id: 'tianditu-satellite', label: '卫星影像' },
  { id: 'none', label: '无底图' }
]

export function resolveBasemap(mode = 'tianditu') {
  if (mode === 'none') {
    return new Basemap({
      baseLayers: [],
      title: '无底图',
      id: 'none'
    })
  }
  if (mode === 'tianditu-satellite' || mode === 'gaode-satellite') {
    return createTiandituBasemap('satellite')
  }
  return createTiandituBasemap('vector')
}

export function applyMapBasemap(map, mode = 'tianditu', view = null) {
  if (!map) return
  try {
    map.basemap = resolveBasemap(mode)
  } catch (e) {
    console.error('[底图]', e)
    map.basemap = resolveBasemap('none')
  }
  if (view) {
    view.background = mode === 'none' ? { color: [245, 245, 245, 1] } : null
  }
}

export { getTiandituToken }
