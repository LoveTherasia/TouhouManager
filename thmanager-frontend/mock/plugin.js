import { mockData } from './server.js'
import { readFileSync } from 'fs'
import { join } from 'path'

const IMAGE_DIR = join(__dirname, '..', 'image')
const MUSIC_DIR = join(__dirname, '..', 'public', 'music')

function sendJson(res, data, status = 200) {
  res.statusCode = status
  res.setHeader('Content-Type', 'application/json')
  res.end(JSON.stringify(data))
}

function sendFile(res, filePath) {
  try {
    const data = readFileSync(filePath)
    const ext = filePath.split('.').pop().toLowerCase()
    const types = {
      jpg: 'image/jpeg', jpeg: 'image/jpeg', png: 'image/png',
      webp: 'image/webp', svg: 'image/svg+xml',
      mp3: 'audio/mpeg', wav: 'audio/wav'
    }
    res.statusCode = 200
    res.setHeader('Content-Type', types[ext] || 'application/octet-stream')
    res.end(data)
  } catch {
    res.statusCode = 404
    res.end('Not Found')
  }
}

function readBody(req) {
  return new Promise((resolve) => {
    let body = ''
    req.on('data', chunk => { body += chunk })
    req.on('end', () => {
      try { resolve(body ? JSON.parse(body) : {}) }
      catch { resolve({}) }
    })
  })
}

export function mockPlugin() {
  return {
    name: 'vite-mock-plugin',
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const url = req.url.split('?')[0]
        const method = req.method

        // Games
        if (url === '/api/games' && method === 'GET') {
          return sendJson(res, mockData.games)
        }
        if (url === '/api/games/status' && method === 'GET') {
          return sendJson(res, { running: false })
        }
        if (url === '/api/games/clear-paths' && method === 'POST') {
          mockData.games.forEach(g => { g.installPath = null; g.installed = false })
          return sendJson(res, mockData.games.length)
        }
        if (url.match(/^\/api\/games\/\d+\/launch$/) && method === 'POST') {
          return sendJson(res, { success: true })
        }
        if (url.match(/^\/api\/games\/\d+\/path$/) && method === 'PUT') {
          const id = parseInt(url.split('/')[3])
          const body = await readBody(req)
          const game = mockData.games.find(g => g.id === id)
          if (game) {
            game.installPath = body.path || ''
            game.installed = !!body.path
          }
          return sendJson(res, game)
        }

        // Replays
        if (url === '/api/replays' && method === 'GET') {
          return sendJson(res, { data: mockData.replays, total: mockData.replays.length, page: 1, pageSize: 0 })
        }
        if (url.match(/^\/api\/replays\/game\/\d+$/) && method === 'GET') {
          const gameId = parseInt(url.split('/').pop())
          return sendJson(res, mockData.replays.filter(r => r.gameId === gameId))
        }
        if (url === '/api/replays/scan' && method === 'POST') {
          return sendJson(res, { imported: 0 })
        }

        // Statistics
        if (url === '/api/statistics' && method === 'GET') {
          return sendJson(res, mockData.statistics)
        }
        if (url === '/api/statistics/playtime' && method === 'GET') {
          return sendJson(res, mockData.playTimeStats)
        }
        if (url === '/api/statistics/scores' && method === 'GET') {
          return sendJson(res, mockData.scoreStats)
        }

        // Settings alias
        if (url === '/api/settings/games' && method === 'GET') {
          return sendJson(res, mockData.games)
        }

        // Images
        if (url === '/api/images' && method === 'GET') {
          return sendJson(res, mockData.images)
        }

        // Images (static files)
        if (url.startsWith('/image/') && method === 'GET') {
          const fileName = url.replace('/image/', '')
          return sendFile(res, join(IMAGE_DIR, fileName))
        }

        // Music (static files)
        if (url.startsWith('/music/') && method === 'GET') {
          const fileName = url.replace('/music/', '')
          return sendFile(res, join(MUSIC_DIR, fileName))
        }

        next()
      })
    }
  }
}
