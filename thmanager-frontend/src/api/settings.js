import request from './request'

export const getAllGames = () => {
  return request.get('/api/settings/games')
}

export const updateGameSettings = (id, data) => {
  return request.put(`/api/settings/games/${id}`, data)
}

export const batchUpdatePaths = (data) => {
  return request.post('/api/settings/batch-update', data)
}
