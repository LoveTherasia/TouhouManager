import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as statsApi from '../api/statistics'

export const useStatisticsStore = defineStore('statistics', () => {
  // State
  const stats = ref({
    totalGames: 0,
    totalReplays: 0,
    totalPlayTime: 0,
    clearedCount: 0
  })
  const gameStats = ref([])
  const playTimeStats = ref([])
  const scoreStats = ref([])
  const loading = ref(false)

  // Actions
  const fetchStatistics = async () => {
    loading.value = true
    try {
      const data = await statsApi.getStatistics()
      stats.value = data
    } catch (error) {
      console.error('Failed to fetch statistics:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchGameStatistics = async (gameId) => {
    try {
      const data = await statsApi.getGameStatistics(gameId)
      return data
    } catch (error) {
      console.error('Failed to fetch game statistics:', error)
      return null
    }
  }

  const fetchPlayTimeStats = async () => {
    try {
      const data = await statsApi.getPlayTimeStats()
      playTimeStats.value = data
    } catch (error) {
      console.error('Failed to fetch playtime stats:', error)
    }
  }

  const fetchScoreStats = async () => {
    try {
      const data = await statsApi.getScoreStats()
      scoreStats.value = data
    } catch (error) {
      console.error('Failed to fetch score stats:', error)
    }
  }

  return {
    stats,
    gameStats,
    playTimeStats,
    scoreStats,
    loading,
    fetchStatistics,
    fetchGameStatistics,
    fetchPlayTimeStats,
    fetchScoreStats
  }
})
