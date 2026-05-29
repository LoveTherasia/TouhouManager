import { ref, watch, onUnmounted } from 'vue'
import { getGameShortName } from '@/utils/format'

export function useGameMusic() {
  const audioPlayer = ref(null)
  const isPlaying = ref(false)
  let canplayListener = null

  const cleanup = () => {
    const audio = audioPlayer.value
    if (audio && canplayListener) {
      audio.removeEventListener('canplay', canplayListener)
      canplayListener = null
    }
  }

  const playGameMusic = (game) => {
    if (!audioPlayer.value || !game) return
    cleanup()

    const audio = audioPlayer.value
    audio.pause()
    audio.currentTime = 0
    audio.src = `/music/${getGameShortName(game)}.mp3`

    canplayListener = () => {
      audio.play().then(() => { isPlaying.value = true }).catch(() => { isPlaying.value = false })
      audio.removeEventListener('canplay', canplayListener)
      canplayListener = null
    }
    audio.addEventListener('canplay', canplayListener)
    audio.load()
  }

  const toggleMusic = () => {
    if (!audioPlayer.value) return
    if (isPlaying.value) {
      audioPlayer.value.pause()
      isPlaying.value = false
    } else {
      audioPlayer.value.play().then(() => { isPlaying.value = true }).catch(() => {})
    }
  }

  const pauseMusic = () => {
    if (audioPlayer.value && isPlaying.value) {
      audioPlayer.value.pause()
      isPlaying.value = false
    }
  }

  const resumeMusic = () => {
    if (audioPlayer.value && !isPlaying.value) {
      audioPlayer.value.play().then(() => { isPlaying.value = true }).catch(() => {})
    }
  }

  watch(isPlaying, (playing) => {
    if (!playing && audioPlayer.value) {
      // sync state from audio events
    }
  })

  onUnmounted(cleanup)

  return { audioPlayer, isPlaying, playGameMusic, toggleMusic, pauseMusic, resumeMusic }
}
