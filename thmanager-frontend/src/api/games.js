import request from './request'

export const getGames = () => {
  return request.get('/api/games')
}

export const getGameById = (id) => {
  return request.get(`/api/games/${id}`)
}

export const updateGame = (id, data) => {
  return request.put(`/api/games/${id}`, data)
}

export const launchGame = (id, countdown = 3) => {
  return request.post(`/api/games/${id}/launch`, { countdown })
}

export const forceStopGame = () => {
  return request.post('/api/games/force-stop')
}

export const getGameStatus = () => {
  return request.get('/api/games/status')
}
