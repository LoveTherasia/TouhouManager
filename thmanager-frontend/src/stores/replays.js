import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as replaysApi from '../api/replays'

export const useReplaysStore = defineStore('replays', () => {
  // State
  const replays = ref([])
  const currentReplay = ref(null)
  const loading = ref(false)
  const filterGameId = ref(null)

  // Getters
  const filteredReplays = computed(() => {
    if (!filterGameId.value) return replays.value
    return replays.value.filter(r => r.gameId === filterGameId.value)
  })

  const totalReplays = computed(() => replays.value.length)

  const clearedCount = computed(() => 
    replays.value.filter(r => r.cleared).length
  )

  // Actions
  const fetchReplays = async (params = {}) => {
    loading.value = true
    try {
      const data = await replaysApi.getReplays(params)
      replays.value = data
    } catch (error) {
      console.error('Failed to fetch replays:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchReplaysByGame = async (gameId) => {
    loading.value = true
    try {
      const data = await replaysApi.getReplaysByGame(gameId)
      replays.value = data
    } catch (error) {
      console.error('Failed to fetch replays:', error)
    } finally {
      loading.value = false
    }
  }

  const deleteReplay = async (id) => {
    try {
      await replaysApi.deleteReplay(id)
      replays.value = replays.value.filter(r => r.id !== id)
      return true
    } catch (error) {
      console.error('Failed to delete replay:', error)
      return false
    }
  }

  const scanNewReplays = async () => {
    try {
      const result = await replaysApi.scanReplays()
      await fetchReplays()
      return result
    } catch (error) {
      console.error('Failed to scan replays:', error)
      return { imported: 0 }
    }
  }

  const setFilter = (gameId) => {
    filterGameId.value = gameId
  }

  return {
    replays,
    currentReplay,
    loading,
    filterGameId,
    filteredReplays,
    totalReplays,
    clearedCount,
    fetchReplays,
    fetchReplaysByGame,
    deleteReplay,
    scanNewReplays,
    setFilter
  }
})
