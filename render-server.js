import { createServer } from 'http'
import { readFileSync, existsSync, statSync } from 'fs'
import { extname, join } from 'path'

const PORT = process.env.PORT || 10000
const DIST_DIR = join(process.cwd(), 'dist', 'build', 'h5')

const MIME_TYPES = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.eot': 'application/vnd.ms-fontobject'
}

const server = createServer((req, res) => {
  try {
    let filePath = join(DIST_DIR, decodeURIComponent(req.url === '/' ? 'index.html' : req.url))
    
    // Security: prevent directory traversal
    if (!filePath.startsWith(DIST_DIR)) {
      res.writeHead(403)
      res.end('Forbidden')
      return
    }

    if (existsSync(filePath) && statSync(filePath).isFile()) {
      const ext = extname(filePath)
      const contentType = MIME_TYPES[ext] || 'application/octet-stream'
      
      try {
        const content = readFileSync(filePath)
        res.writeHead(200, { 'Content-Type': contentType })
        res.end(content)
      } catch (err) {
        console.error('Error reading file:', err)
        res.writeHead(500)
        res.end('Internal Server Error')
      }
    } else {
      // SPA fallback: serve index.html for non-file routes
      const indexPath = join(DIST_DIR, 'index.html')
      if (existsSync(indexPath)) {
        const content = readFileSync(indexPath)
        res.writeHead(200, { 'Content-Type': 'text/html' })
        res.end(content)
      } else {
        res.writeHead(404)
        res.end('Not Found')
      }
    }
  } catch (err) {
    console.error('Request error:', err)
    res.writeHead(500)
    res.end('Internal Server Error')
  }
})

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Render server running on port ${PORT}`)
  console.log(`Serving files from: ${DIST_DIR}`)
})
