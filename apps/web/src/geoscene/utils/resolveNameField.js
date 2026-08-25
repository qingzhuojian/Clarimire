import { normalizeReservoirName } from '../reservoirNames'

/**
 * 从要素属性中解析名称字段
 * @param {Record<string, *>} attrs
 * @returns {{ field: string, value: string }}
 */
export function resolveNameField(attrs = {}) {
  const candidates = ['name', 'NAME', '库名', '名称']
  const field = candidates.find((k) => attrs[k] != null && attrs[k] !== '')
  return {
    field: field || 'name',
    value: field ? String(attrs[field]) : ''
  }
}

/**
 * 将标准化库名状态映射到图层原始字段值
 * @param {import('@geoscene/core/layers/FeatureLayer').default} layer
 * @param {Record<string, string>} statusByNormalized
 * @returns {Promise<{ statusMap: Record<string, string>, nameField: string }>}
 */
export async function mapStatusToLayerValues(layer, statusByNormalized) {
  const result = await layer.queryFeatures({
    where: '1=1',
    outFields: ['*'],
    returnGeometry: false
  })

  const statusMap = {}
  let nameField = 'name'

  for (const feature of result.features) {
    const { field, value } = resolveNameField(feature.attributes || {})
    if (value) nameField = field
    const normalized = normalizeReservoirName(value)
    if (statusByNormalized[normalized]) {
      statusMap[value] = statusByNormalized[normalized]
    }
  }

  return { statusMap, nameField }
}
