import Basemap from '@geoscene/core/Basemap'
import WebTileLayer from '@geoscene/core/layers/WebTileLayer'
import SpatialReference from '@geoscene/core/geometry/SpatialReference'
import geosceneConfig from '@/config/geoscene'

/**
 * 天地图 XYZ 底图（vec_w / img_w = Web 墨卡托 EPSG:3857）
 *
 * 注意：GeoScene WebTileLayer 的 {subDomain} 只能出现在「主机名」里。
 * 开发代理路径 /tianditu-xyz/t0/... 必须自己在 getTileUrl 里轮询子域，
 * 否则会变成 /tianditu-xyz//DataServer（双斜杠、无子域）导致代理失败。
 */
const SUB_DOMAINS = ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7']

export function getTiandituToken() {
  return (
    import.meta.env.VITE_TIANDITU_TOKEN ||
    geosceneConfig.map?.tiandituToken ||
    ''
  ).trim()
}

function pickSubDomain(level, row, col) {
  // 按瓦片坐标取模分摊，保证同瓦片 URL 稳定（利于浏览器缓存）
  const idx = Math.abs((level * 31 + row * 17 + col) % SUB_DOMAINS.length)
  return SUB_DOMAINS[idx]
}

function buildTileUrl(layerType, token, level, row, col) {
  const sub = pickSubDomain(level, row, col)
  if (import.meta.env.DEV) {
    return `${window.location.origin}/tianditu-xyz/${sub}/DataServer?T=${layerType}&x=${col}&y=${row}&l=${level}&tk=${token}`
  }
  return `https://${sub}.tianditu.gov.cn/DataServer?T=${layerType}&x=${col}&y=${row}&l=${level}&tk=${token}`
}

/**
 * 占位 urlTemplate：满足 WebTileLayer.load 校验；真实地址由 getTileUrl 生成
 */
function placeholderUrlTemplate(layerType, token) {
  if (import.meta.env.DEV) {
    return `${window.location.origin}/tianditu-xyz/t0/DataServer?T=${layerType}&x={x}&y={y}&l={z}&tk=${token}`
  }
  // 生产：subDomain 在主机名，官方机制可用
  return `https://{subDomain}.tianditu.gov.cn/DataServer?T=${layerType}&x={x}&y={y}&l={z}&tk=${token}`
}

function bindXyzTileAccess(layer, layerType, token) {
  layer.getTileUrl = function getTileUrl(level, row, col) {
    return buildTileUrl(layerType, token, level, row, col)
  }

  layer.fetchTile = function fetchTileByImage(level, row, col, options = {}) {
    const url = this.getTileUrl(level, row, col)
    return new Promise((resolve, reject) => {
      const img = new Image()
      const onAbort = () => {
        cleanup()
        reject(options.signal?.reason || new Error('tile aborted'))
      }
      const cleanup = () => {
        img.onload = null
        img.onerror = null
        options.signal?.removeEventListener('abort', onAbort)
      }
      if (options.signal) {
        if (options.signal.aborted) {
          reject(options.signal.reason || new Error('tile aborted'))
          return
        }
        options.signal.addEventListener('abort', onAbort)
      }
      img.onload = () => {
        cleanup()
        resolve(img)
      }
      img.onerror = () => {
        cleanup()
        reject(new Error(`[天地图XYZ] 瓦片失败: ${url}`))
      }
      img.src = url
    })
  }
}

function createXyzLayer(layerType, title) {
  const token = getTiandituToken()
  const layer = new WebTileLayer({
    urlTemplate: placeholderUrlTemplate(layerType, token),
    // 生产主机名模式需要；开发模式实际不依赖此字段
    subDomains: import.meta.env.DEV ? undefined : SUB_DOMAINS,
    copyright: '© 国家基础地理信息中心 - 天地图',
    title,
    spatialReference: SpatialReference.WebMercator
  })

  bindXyzTileAccess(layer, layerType, token)

  layer.when(
    () => console.info(`[天地图XYZ] 就绪: ${title}（子域 t0–t7）`),
    (err) => console.error(`[天地图XYZ] 失败: ${title}`, err)
  )
  return layer
}

export function probeTiandituToken() {
  const token = getTiandituToken()
  if (!token) return Promise.resolve({ ok: false, reason: 'missing-token' })

  const url = buildTileUrl('vec_w', token, 10, 388, 843)
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => resolve({ ok: true, url })
    img.onerror = () => resolve({ ok: false, reason: 'tile-load-failed', url })
    img.src = url
  })
}

/**
 * @param {'vector' | 'satellite'} style
 */
export function createTiandituBasemap(style = 'vector') {
  const token = getTiandituToken()
  if (!token) {
    throw new Error('未配置 VITE_TIANDITU_TOKEN，请写入 apps/web/.env 后重启 npm run dev')
  }
  console.info(
    `[天地图XYZ] token长度=${token.length}, 子域=${SUB_DOMAINS.join(',')}, DEV代理=${!!import.meta.env.DEV}`
  )

  if (style === 'satellite') {
    return new Basemap({
      baseLayers: [
        createXyzLayer('img_w', '天地图影像'),
        createXyzLayer('cia_w', '天地图影像注记')
      ],
      title: '天地图卫星',
      id: 'tianditu-satellite'
    })
  }

  return new Basemap({
    baseLayers: [
      createXyzLayer('vec_w', '天地图矢量'),
      createXyzLayer('cva_w', '天地图矢量注记')
    ],
    title: '天地图标准',
    id: 'tianditu'
  })
}
