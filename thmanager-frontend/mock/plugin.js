import { mockData } from './server.js'

export function mockPlugin() {
  return {
    name: 'vite-mock-plugin',
    configureServer(server) {
      server.middlewares.use('/api/games', (req, res, next) => {
        if (req.method === 'GET') {
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify(mockData.games))
          return
        }
        next()
      })

      server.middlewares.use('/api/replays', (req, res, next) => {
        if (req.method === 'GET') {
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify(mockData.replays))
          return
        }
        next()
      })

      server.middlewares.use('/api/statistics', (req, res, next) => {
        if (req.method === 'GET') {
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify(mockData.statistics))
          return
        }
        next()
      })

      server.middlewares.use('/api/settings/games', (req, res, next) => {
        if (req.method === 'GET') {
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify(mockData.games))
          return
        }
        next()
      })

      server.middlewares.use('/api/images', (req, res, next) => {
        if (req.method === 'GET') {
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify(mockData.images))
          return
        }
        next()
      })
    }
  }
}
