import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'

/**
 * 预警页选中水库高亮（亮色描边 + 脉动）
 */
export function createSelectionHighlight(view) {
  const layer = new GraphicsLayer({ title: '选中高亮', listMode: 'hide' })
  view.map.add(layer)

  let pulseTimer = null
  let pulseOn = false
  let currentGeom = null

  const makeSymbol = (strong) => ({
    type: 'simple-fill',
    color: [255, 193, 7, strong ? 0.28 : 0.12],
    outline: {
      color: [255, 87, 34, 1],
      width: strong ? 4.5 : 2.5
    }
  })

  const stopPulse = () => {
    if (pulseTimer) {
      clearInterval(pulseTimer)
      pulseTimer = null
    }
    pulseOn = false
  }

  const startPulse = () => {
    stopPulse()
    pulseTimer = setInterval(() => {
      if (!layer.graphics.length || !currentGeom) return
      pulseOn = !pulseOn
      const g = layer.graphics.getItemAt(0)
      if (g) g.symbol = makeSymbol(pulseOn)
    }, 550)
  }

  return {
    layer,
    clear() {
      stopPulse()
      currentGeom = null
      layer.removeAll()
    },
    destroy() {
      this.clear()
      view.map?.remove(layer)
    },
    /**
     * @param {__esri.Geometry} geometry
     */
    set(geometry) {
      this.clear()
      if (!geometry) return
      currentGeom = geometry
      layer.add(
        new Graphic({
          geometry,
          symbol: makeSymbol(true)
        })
      )
      startPulse()
    }
  }
}
