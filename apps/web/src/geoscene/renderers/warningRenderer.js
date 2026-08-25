import UniqueValueRenderer from '@geoscene/core/renderers/UniqueValueRenderer'

const STATUS_COLORS = {
  危险: [244, 67, 54, 0.75],
  警戒: [255, 152, 0, 0.75],
  正常: [33, 150, 243, 0.75]
}

/**
 * 根据水库预警状态更新图层渲染
 * @param {import('@geoscene/core/layers/FeatureLayer').default} layer
 * @param {Record<string, string>} statusMap - 库名 -> 正常/警戒/危险
 * @param {string} [nameField='name']
 */
export function applyWarningRenderer(layer, statusMap, nameField = 'name') {
  const uniqueValueInfos = Object.entries(statusMap).map(([name, status]) => ({
    value: name,
    symbol: {
      type: 'simple-fill',
      color: STATUS_COLORS[status] || STATUS_COLORS['正常'],
      outline: { color: STATUS_COLORS[status] || STATUS_COLORS['正常'], width: 1.5 }
    }
  }))

  layer.renderer = new UniqueValueRenderer({
    field: nameField,
    uniqueValueInfos,
    defaultSymbol: {
      type: 'simple-fill',
      color: STATUS_COLORS['正常'],
      outline: { color: [33, 150, 243, 1], width: 1 }
    }
  })
}
