import request from './request'

export const getReplays = (params) => {
  return request.get('/api/replays', { params })
}

export const getReplaysByGame = (gameId) => {
  return request.get(`/api/replays/game/${gameId}`)
}

export const getReplayById = (id) => {
  return request.get(`/api/replays/${id}`)
}

export const deleteReplay = (id) => {
  return request.delete(`/api/replays/${id}`)
}

export const exportReplay = (id) => {
  return request.get(`/api/replays/${id}/export`)
}

export const scanReplays = () => {
  return request.post('/api/replays/scan')
}
