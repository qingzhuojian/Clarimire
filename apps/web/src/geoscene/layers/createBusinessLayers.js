import FeatureLayer from '@geoscene/core/layers/FeatureLayer'
import geosceneConfig from '@/config/geoscene'

/**
 * 创建业务图层实例（渲染器在 load 完成后应用）
 * @param {{ popupEnabled?: boolean }} options
 */
export function createBusinessLayers(options = {}) {
  const layers = {}
  const order = geosceneConfig.layerOrder || ['waterAreas', 'reservoirs', 'waterLines', 'settlements', 'districts']
  const popupEnabled = options.popupEnabled !== false
  const keys = options.keys?.length ? options.keys : order

  for (const key of keys) {
    const meta = geosceneConfig.layerMeta[key]
    if (!meta) continue
    layers[key] = new FeatureLayer({
      url: geosceneConfig.services.layers[key],
      title: meta.name,
      visible: meta.defaultVisible,
      outFields: ['*'],
      popupEnabled
    })
  }
  return layers
}

/**
 * 区县界置于最上层，避免被水系/水库面遮挡
 */
export function reorderBusinessLayers(map, layers) {
  const stack = geosceneConfig.layerOrder || ['waterAreas', 'reservoirs', 'waterLines', 'settlements', 'districts']
  stack.forEach((key) => {
    const layer = layers[key]
    if (layer && map.layers.includes(layer)) {
      map.reorder(layer, map.layers.length - 1)
    }
  })
}

/**
 * 将业务图层按顺序添加到地图
 * @param {import('@geoscene/core/Map').default} map
 * @returns {Record<string, import('@geoscene/core/layers/FeatureLayer').default>}
 */
export function addBusinessLayersToMap(map, options = {}) {
  const layers = createBusinessLayers(options)
  const keys = options.keys?.length ? options.keys : (geosceneConfig.layerOrder || Object.keys(layers))
  keys.forEach((key) => {
    if (layers[key]) map.add(layers[key])
  })
  return layers
}
