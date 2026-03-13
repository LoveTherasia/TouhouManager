import { localRequest } from './request'

//游戏信息都是从本地应用后端接口获取
export const getGames = () => {
  return localRequest.get('/api/games')
}

export const getGameById = (id) => {
  return localRequest.get(`/api/games/${id}`)
}

export const updateGame = (id, data) => {
  return localRequest.put(`/api/games/${id}/path`, data)
}

export const launchGame = (id, countdown = 3) => {
  return localRequest.post(`/api/games/${id}/launch`, { countdown })
}

export const forceStopGame = () => {
  return localRequest.post('/api/games/force-stop')
}

export const getGameStatus = () => {
  return localRequest.get('/api/games/status')
}

export const clearAllGamePaths = () => {
  return localRequest.post('/api/games/clear-paths')
}
