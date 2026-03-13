import {localRequest} from './request'

export const getStatistics = () => {
  return localRequest.get('/api/statistics')
}

export const getGameStatistics = (gameId) => {
  return localRequest.get(`/api/statistics/game/${gameId}`)
}

export const getPlayTimeStats = () => {
  return localRequest.get('/api/statistics/playtime')
}

export const getScoreStats = () => {
  return localRequest.get('/api/statistics/scores')
}
