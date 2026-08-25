import Basemap from '@geoscene/core/Basemap'
import WebTileLayer from '@geoscene/core/layers/WebTileLayer'

/**
 * 高德矢量底图（国内加载比 OSM 快）
 * 注意：正式商用需申请高德 Key；开发演示可用公开瓦片地址
 */
export function createGaodeBasemap(style = 'vector') {
  const templates = {
    vector: 'https://webrd0{subDomain}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={col}&y={row}&z={level}',
    satellite: 'https://webst0{subDomain}.is.autonavi.com/appmaptile?style=6&x={col}&y={row}&z={level}'
  }
  const layer = new WebTileLayer({
    urlTemplate: templates[style] || templates.vector,
    subDomains: ['1', '2', '3', '4'],
    copyright: '© 高德地图'
  })
  return new Basemap({
    baseLayers: [layer],
    title: style === 'satellite' ? 'Gaode Satellite' : 'Gaode',
    id: style === 'satellite' ? 'gaode-satellite' : 'gaode'
  })
}
