import FeatureLayer from '@geoscene/core/layers/FeatureLayer'
import Graphic from '@geoscene/core/Graphic'
import geosceneConfig from '@/config/geoscene'
import { shouldApplyGcj02Offset, transformGeometryForGaode } from '../utils/coordTransform'

export function shouldAlignLayersToGcj02() {
  return shouldApplyGcj02Offset()
}

function getLayerSourceCrs(layerKey) {
  return geosceneConfig.layerMeta?.[layerKey]?.sourceCrs || 'wgs84'
}

export async function createGcj02AlignedLayer(sourceLayer, layerKey) {
  if (!shouldAlignLayersToGcj02()) return sourceLayer

  await sourceLayer.load()
  const sourceCrs = getLayerSourceCrs(layerKey)
  const result = await sourceLayer.queryFeatures({
    where: '1=1',
    outFields: ['*'],
    returnGeometry: true,
    outSpatialReference: { wkid: 4326 }
  })

  const graphics = await Promise.all(
    result.features
      .filter((f) => f.geometry)
      .map(async (feature) => {
        const geometry = await transformGeometryForGaode(feature.geometry, sourceCrs)
        return new Graphic({
          geometry,
          attributes: { ...feature.attributes }
        })
      })
  )

  const alignedLayer = new FeatureLayer({
    source: graphics,
    fields: sourceLayer.fields,
    objectIdField: sourceLayer.objectIdField,
    geometryType: sourceLayer.geometryType,
    spatialReference: { wkid: 4326 },
    title: sourceLayer.title,
    visible: sourceLayer.visible,
    popupEnabled: sourceLayer.popupEnabled,
    outFields: ['*']
  })

  await alignedLayer.load()
  return alignedLayer
}

export async function replaceLayerWithGcj02Aligned(map, layers, key) {
  const sourceLayer = layers[key]
  if (!sourceLayer || sourceLayer.destroyed || !shouldAlignLayersToGcj02()) return sourceLayer

  const alignedLayer = await createGcj02AlignedLayer(sourceLayer, key)
  if (alignedLayer === sourceLayer) return sourceLayer
  if (sourceLayer.destroyed || !map) return sourceLayer

  if (map.layers.includes(sourceLayer)) {
    map.add(alignedLayer)
    map.remove(sourceLayer)
  }
  layers[key] = alignedLayer
  return alignedLayer
}
