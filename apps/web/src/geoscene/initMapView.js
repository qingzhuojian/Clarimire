import Map from '@geoscene/core/Map'
import MapView from '@geoscene/core/views/MapView'
import geosceneConfig from '@/config/geoscene'
import { resolveBasemap, getTiandituToken } from '@/geoscene/basemaps/basemapModes'
import { probeTiandituToken } from '@/geoscene/basemaps/createTiandituBasemap'
import { shouldApplyGcj02Offset, transformCenter } from '@/geoscene/utils/coordTransform'

/**
 * 创建 MapView（默认天地图 XYZ）
 */
export async function createMapView(container, options = {}) {
  const { basemapMode, center: optCenter, zoom: optZoom, ...viewOptions } = options
  const basemapType = basemapMode || geosceneConfig.map.basemap || 'tianditu'

  if (basemapType === 'tianditu' || basemapType === 'tianditu-satellite') {
    if (!getTiandituToken()) {
      console.error('[地图] 缺少 VITE_TIANDITU_TOKEN')
    } else {
      probeTiandituToken().then((r) => {
        if (r.ok) console.info('[天地图XYZ] 探测成功', r.sub, r.url)
        else console.error('[天地图XYZ] 探测失败（检查 Key/白名单/代理）', r)
      })
    }
  }

  let basemap
  try {
    basemap = basemapType === 'osm' ? 'osm' : resolveBasemap(basemapType)
  } catch (e) {
    console.error('[地图] 底图创建失败，回退无底图', e)
    basemap = resolveBasemap('none')
  }

  let center = optCenter || geosceneConfig.map.center
  if (shouldApplyGcj02Offset() && (basemapType === 'gaode' || basemapType === 'gaode-satellite')) {
    center = transformCenter(center)
  }

  const map = new Map({ basemap })
  const view = new MapView({
    container,
    map,
    center,
    zoom: optZoom ?? geosceneConfig.map.zoom,
    ui: { components: [] },
    popupEnabled: false,
    background: basemapType === 'none' ? { color: [245, 245, 245, 1] } : undefined,
    ...viewOptions
  })

  await view.when()
  return { map, view }
}
