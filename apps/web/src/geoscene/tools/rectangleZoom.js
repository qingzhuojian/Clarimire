import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import SketchViewModel from '@geoscene/core/widgets/Sketch/SketchViewModel'

/**
 * 启用矩形框选缩放
 * @param {import('@geoscene/core/views/MapView').default} view
 * @param {{ mode?: 'in' | 'out' }} [options]
 * @returns {{ disable: () => void }}
 */
export function enableRectangleZoom(view, options = {}) {
  const mode = options.mode || 'in'
  const layer = new GraphicsLayer({ listMode: 'hide' })
  view.map.add(layer)

  const sketch = new SketchViewModel({
    view,
    layer,
    polygonSymbol: {
      type: 'simple-fill',
      color: mode === 'in' ? [0, 122, 255, 0.12] : [255, 152, 0, 0.12],
      outline: { color: mode === 'in' ? [0, 122, 255, 1] : [255, 152, 0, 1], width: 2 }
    }
  })

  sketch.create('rectangle')

  const handle = sketch.on('create', async (event) => {
    if (event.state !== 'complete' || !event.graphic?.geometry) return
    try {
      if (mode === 'in') {
        await view.goTo(event.graphic.geometry.extent.expand(1.15))
      } else {
        const center = event.graphic.geometry.extent.center
        const nextZoom = Math.max(view.constraints?.minZoom ?? 0, view.zoom - 1.5)
        await view.goTo({ target: center, zoom: nextZoom })
      }
    } finally {
      layer.removeAll()
    }
  })

  return {
    disable: () => {
      handle.remove()
      sketch.cancel()
      view.map.remove(layer)
    }
  }
}
