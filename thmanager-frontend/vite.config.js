import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { mockPlugin } from './mock/plugin.js'
import { readFileSync } from 'fs'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const useMock = process.env.USE_MOCK === 'true'

const cloudApiTarget = 'http://121.43.124.131:8080'
const localApiTarget = 'http://localhost:8080'
const cloudWsTarget = 'ws://121.43.124.131:8080'
const localWsTarget = 'ws://localhost:8080'

const devProxy = {
  '/api/auth': {
    target: cloudApiTarget,
    changeOrigin: true
  },
  '/api': {
    target: localApiTarget,
    changeOrigin: true
  },
  '/image': {
    target: localApiTarget,
    changeOrigin: true
  },
  '/music': {
    target: localApiTarget,
    changeOrigin: true
  },
  '/ws': {
    target: cloudWsTarget,
    ws: true
  }
}

export default defineConfig({
  plugins: useMock ? [vue(), mockPlugin()] : [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    proxy: useMock ? undefined : devProxy
  }
})
