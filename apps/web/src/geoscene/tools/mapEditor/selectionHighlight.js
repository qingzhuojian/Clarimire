import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'

/**
 * 要素选择高亮管理
 */
export function createSelectionManager(view) {
  const layer = new GraphicsLayer({ title: '要素选择', listMode: 'hide' })
  view.map.add(layer)
  const selected = new Map()

  const highlightSymbol = (geometry) => {
    const type = geometry.type
    if (type === 'polyline') {
      return { type: 'simple-line', color: [255, 235, 59, 1], width: 4 }
    }
    if (type === 'point') {
      return {
        type: 'simple-marker',
        color: [255, 235, 59, 0.9],
        size: 14,
        outline: { color: [255, 87, 34, 1], width: 2 }
      }
    }
    return {
      type: 'simple-fill',
      color: [255, 235, 59, 0.35],
      outline: { color: [255, 87, 34, 1], width: 2.5 }
    }
  }

  const getKey = (graphic) => {
    const layerId = graphic.layer?.id || graphic.layer?.title || 'layer'
    const oid = graphic.attributes?.OBJECTID ?? graphic.attributes?.objectId ?? graphic.uid
    return `${layerId}:${oid}`
  }

  return {
    layer,
    selected,
    getKey,
    toggle(graphic) {
      const key = getKey(graphic)
      if (selected.has(key)) {
        selected.delete(key)
        layer.graphics.filter((g) => g.attributes?.__selectKey === key).forEach((g) => layer.remove(g))
        return false
      }
      selected.set(key, graphic)
      layer.add(
        new Graphic({
          geometry: graphic.geometry,
          symbol: highlightSymbol(graphic.geometry),
          attributes: { __selectKey: key }
        })
      )
      return true
    },
    clear() {
      selected.clear()
      layer.removeAll()
    },
    count() {
      return selected.size
    }
  }
}
