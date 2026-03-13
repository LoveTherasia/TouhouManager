import {localRequest} from './request'

export const getAllGames = () => {
  return localRequest.get('/api/settings/games')
}

export const updateGameSettings = (id, data) => {
  return localRequest.put(`/api/settings/games/${id}`, data)
}

export const batchUpdatePaths = (data) => {
  return localRequest.post('/api/settings/batch-update', data)
}
