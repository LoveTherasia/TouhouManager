import { defineStore } from 'pinia'
import { ref, computed, callWithErrorHandling } from 'vue'
import * as gamesApi from '../api/games'

export const useGamesStore = defineStore('games', () => {
  // State
  const games = ref([])
  const currentGame = ref(null)
  const isRunning = ref(false)
  const countdown = ref(0)
  const loading = ref(false)

  // Getters
  const installedGames = computed(() => 
    games.value.filter(g => g.installed)
  )

  const totalPlayTime = computed(() => {
    return games.value.reduce((total, game) => {
      const hours = parseInt(game.totalPlayTime?.split('小时')[0] || 0)
      return total + hours
    }, 0)
  })

  // Actions
  const fetchGames = async () => {
    loading.value = true
    try {
      const data = await gamesApi.getGames()
      games.value = data
    } catch (error) {
      console.error('Failed to fetch games:', error)
    } finally {
      loading.value = false
    }
  }

  const launchGame = async (id, seconds = 3) => {
    try {
      await gamesApi.launchGame(id, seconds)
      return true
    } catch (error) {
      console.error('Failed to launch game:', error)
      return false
    }
  }

  const stopGame = async () => {
    try {
      await gamesApi.forceStopGame()
      isRunning.value = false
      return true
    } catch (error) {
      console.error('Failed to stop game:', error)
      return false
    }
  }

  const checkStatus = async () => {
    try {
      const status = await gamesApi.getGameStatus()
      isRunning.value = status.running
      return status
    } catch (error) {
      console.error('Failed to check status:', error)
      return { running: false }
    }
  }

  const updateGamePath = async(gameId,path) => {
    try{
      await gamesApi.updateGame(gameId,{path})
      return true;
    }catch(error){
      console.error('Failed to update game path:',error);
      throw error;
    }
  }

  return {
    games,
    currentGame,
    isRunning,
    countdown,
    loading,
    installedGames,
    totalPlayTime,
    fetchGames,
    launchGame,
    stopGame,
    checkStatus,
    updateGamePath
  }
})
