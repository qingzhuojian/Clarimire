import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

/**
 * 天地图 XYZ 同源代理
 * - 路径：/tianditu-xyz/t0|t1|...|t7/DataServer?...
 * - 转发到 https://t{n}.tianditu.gov.cn/DataServer?...
 * - Referer 固定 127.0.0.1（localhost 会被天地图 301007 拒绝）
 * - 响应加 Cache-Control，便于浏览器缓存瓦片
 */
function tiandituXyzProxy() {
  return {
    target: 'https://t0.tianditu.gov.cn',
    changeOrigin: true,
    secure: true,
    // /tianditu-xyz/t3/DataServer?... → /DataServer?...
    rewrite: (p) => p.replace(/^\/tianditu-xyz\/t[0-7]/, ''),
    router: (req) => {
      const m = String(req.url || '').match(/\/tianditu-xyz\/(t[0-7])/)
      return m ? `https://${m[1]}.tianditu.gov.cn` : 'https://t0.tianditu.gov.cn'
    },
    configure: (proxy) => {
      proxy.on('proxyReq', (proxyReq, req) => {
        proxyReq.setHeader('Referer', 'http://127.0.0.1:5173/')
        proxyReq.setHeader('Origin', 'http://127.0.0.1:5173')
        proxyReq.setHeader(
          'User-Agent',
          req.headers['user-agent'] ||
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'
        )
      })
      proxy.on('proxyRes', (proxyRes) => {
        // 瓦片可长期缓存（同一 z/x/y/tk 内容不变）
        if (proxyRes.statusCode === 200) {
          proxyRes.headers['cache-control'] = 'public, max-age=86400, stale-while-revalidate=604800'
          // 去掉可能禁用缓存的上游头
          delete proxyRes.headers['pragma']
          delete proxyRes.headers['expires']
        }
      })
    }
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    envPrefix: ['VITE_'],
    server: {
      port: 5173,
      host: '0.0.0.0',
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/api/, '')
        },
        '/geoscene': {
          target: env.VITE_GEOSCENE_URL?.replace(/\/geoscene\/?$/, '') || 'https://gis.dev.local:6443',
          changeOrigin: true,
          secure: false
        },
        '/tianditu-xyz': tiandituXyzProxy(),
        // 兼容旧路径
        '/tianditu-tms': tiandituXyzProxy()
      }
    }
  }
})
