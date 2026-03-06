// placeholder service worker to avoid 404
self.addEventListener('install', (e) => {
  self.skipWaiting()
})
self.addEventListener('activate', (e) => {
  clients.claim()
})
