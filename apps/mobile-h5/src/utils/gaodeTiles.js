import L from 'leaflet'

/**
 * Leaflet + 高德矢量瓦片（国内可访问，无需天地图白名单）
 * 演示可用公开瓦片；正式商用请换成带 Key 的官方方案
 */
export function addGaodeBasemap(map) {
  L.tileLayer(
    'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
    {
      subdomains: ['1', '2', '3', '4'],
      maxZoom: 18,
      attribution: '© 高德地图'
    }
  ).addTo(map)
}
