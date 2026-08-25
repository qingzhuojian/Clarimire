import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'
import { getGeometryCentroid } from '@/geoscene/utils/getGeometryCentroid'
import { resolveNameField } from '@/geoscene/utils/resolveNameField'
import { formatFieldValue } from '@/geoscene/utils/featureFieldLabels'

/**
 * @param {object} [style]
 */
export function buildTextSymbol(style = {}, text = '') {
  return {
    type: 'text',
    text: String(text ?? ''),
    color: style.color || '#1a1a1a',
    haloColor: style.haloColor || '#ffffff',
    haloSize: style.haloSize ?? 1.5,
    font: {
      size: style.fontSize ?? 12,
      weight: 'bold'
    }
  }
}

/**
 * 为图层字段生成标注
 * @param {import('@geoscene/core/views/MapView').default} view
 * @param {import('@geoscene/core/layers/FeatureLayer').default} layer
 * @param {string} fieldName
 * @param {object} [style]
 */
export async function createFieldAnnotations(view, layer, fieldName, style = {}) {
  const annotationLayer = new GraphicsLayer({
    title: '地图标注',
    listMode: 'hide',
    visible: style.visible !== false
  })
  view.map.add(annotationLayer)

  const result = await layer.queryFeatures({
    where: '1=1',
    outFields: ['*'],
    returnGeometry: true
  })

  for (const feature of result.features) {
    const raw = feature.attributes?.[fieldName]
    if (raw == null || raw === '') continue
    const label = formatFieldValue(fieldName, raw)
    if (!label || label === '—') continue
    const { value: nameValue } = resolveNameField(feature.attributes || {})
    const center = getGeometryCentroid(feature.geometry)
    if (!center) continue

    annotationLayer.add(
      new Graphic({
        geometry: center,
        symbol: buildTextSymbol(style, label),
        attributes: { label, name: nameValue, field: fieldName }
      })
    )
  }

  return annotationLayer
}

/**
 * 即时更新已有标注的文字样式（不重新查询）
 * @param {import('@geoscene/core/layers/GraphicsLayer').default|null} annotationLayer
 * @param {object} style
 */
export function applyAnnotationStyle(annotationLayer, style = {}) {
  if (!annotationLayer) return
  annotationLayer.graphics.forEach((graphic) => {
    const text = graphic.attributes?.label ?? graphic.symbol?.text ?? ''
    graphic.symbol = buildTextSymbol(style, text)
  })
}

export function removeAnnotationLayer(view, layer) {
  if (layer && view?.map) view.map.remove(layer)
}
