const ENV_STATUS_COLORS = {
  正常: [33, 150, 243, 0.85],
  黄色: [255, 235, 59, 0.85],
  橙色: [255, 152, 0, 0.85],
  红色: [244, 67, 54, 0.85]
}

/**
 * 更新监测点符号颜色
 * @param {import('@geoscene/core/layers/GraphicsLayer').default} layer
 * @param {Record<string, string>} statusMap - 库名 -> 状态
 */
export function applyMonitoringWarningColors(layer, statusMap) {
  if (!layer) return
  layer.graphics.forEach((graphic) => {
    const name = graphic.attributes?.reservoirName
    const status = statusMap[name] || '正常'
    graphic.symbol = {
      type: 'simple-marker',
      color: ENV_STATUS_COLORS[status] || ENV_STATUS_COLORS['正常'],
      size: 11,
      outline: { color: '#ffffff', width: 1.5 }
    }
  })
}
