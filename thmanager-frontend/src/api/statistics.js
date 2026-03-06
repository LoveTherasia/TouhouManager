import request from './request'

export const getStatistics = () => {
  return request.get('/api/statistics')
}

export const getGameStatistics = (gameId) => {
  return request.get(`/api/statistics/game/${gameId}`)
}

export const getPlayTimeStats = () => {
  return request.get('/api/statistics/playtime')
}

export const getScoreStats = () => {
  return request.get('/api/statistics/scores')
}
