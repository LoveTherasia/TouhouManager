import { localRequest } from './request'

export const getReplays = (params) => {
  return localRequest.get('/api/replays', { params })
}

export const getReplaysByGame = (gameId) => {
  return localRequest.get(`/api/replays/game/${gameId}`)
}

export const getReplayById = (id) => {
  return localRequest.get(`/api/replays/${id}`)
}

export const deleteReplay = (id) => {
  return localRequest.delete(`/api/replays/${id}`)
}

export const exportReplay = (id) => {
  return localRequest.get(`/api/replays/${id}/export`)
}

export const scanReplays = () => {
  return localRequest.post('/api/replays/scan')
}
