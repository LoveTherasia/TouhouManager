import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

const useCloudServer = true

const cloudApiTarget = 'http://121.43.124.131:8080'
const localApiTarget = 'http://localhost:8080'
const cloudWsTarget = 'ws://121.43.124.131:8080'
const localWsTarget = 'ws://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    proxy: {
      '/api/auth': {
        target: useCloudServer ? cloudApiTarget : localApiTarget,
        changeOrigin: true
      },
      '/api': {
        target: localApiTarget,
        changeOrigin: true
      },
      '/ws': {
        target: useCloudServer ? cloudWsTarget : localWsTarget,
        ws: true
      }
    }
  }
})
