import esriConfig from '@geoscene/core/config.js'
import '@geoscene/core/assets/geoscene/themes/light/main.css'

// 静态资源必须从 HTTP 路径加载（worker / 国际化文件），不能走 blob/file URL
esriConfig.assetsPath = `${window.location.origin}/assets`
esriConfig.locale = 'zh-cn'

// 允许请求本地天地图 TMS 代理与官方域名
const trusted = esriConfig.request.trustedServers
;[
  'https://t0.tianditu.gov.cn',
  'https://t1.tianditu.gov.cn',
  'https://t2.tianditu.gov.cn',
  'https://t3.tianditu.gov.cn',
  'https://t4.tianditu.gov.cn',
  'https://t5.tianditu.gov.cn',
  'https://t6.tianditu.gov.cn',
  'https://t7.tianditu.gov.cn',
  window.location.origin
].forEach((host) => {
  if (!trusted.includes(host)) trusted.push(host)
})

export default esriConfig
