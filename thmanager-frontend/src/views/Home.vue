<template>
  <div class="home-container">
    <!-- 背景图片（模糊放大版） -->
    <div 
      v-if="selectedGame" 
      class="background-image"
      :style="{ backgroundImage: `url('/image/main/${selectedGame.shortName}.jpg')` }"
    ></div>
    
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>东方Project</h2>
      </div>
      <div class="game-list">
        <div 
          v-for="game in gamesStore.games" 
          :key="game.id"
          class="game-item"
          :class="{ active: selectedGame?.id === game.id }"
          @click="selectGame(game)"
        >
          <div class="game-avatar">
            <img 
              :src="`/image/avatar/${game.shortName}.jpg`" 
              :alt="game.displayName"
              @error="handleImageError"
            />
          </div>
          <div class="game-name">{{ game.displayName }}</div>
        </div>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 右侧图片（正常大小） -->
      <div v-if="selectedGame" class="main-image-container">
        <img 
          :src="`/image/main/${selectedGame.shortName}.jpg`" 
          :alt="selectedGame.displayName"
          class="main-image"
          @error="handleImageError"
        />
      </div>
      
      <!-- 右下角按钮组 -->
      <div v-if="selectedGame" class="bottom-buttons">
        <div class="status-text" v-if="!selectedGame.installed">
          已安装?点击这里配置安装路径
        </div>
        <div class="button-group">
          <button 
            class="primary-button"
            :disabled="gamesStore.isRunning"
            @click="handleLaunch"
          >
            {{ selectedGame.installed ? '启动游戏' : '安装游戏' }}
          </button>
          <button 
            class="secondary-button"
            @click="navigateToSettings"
          >
            ⚙️ 设置
          </button>
        </div>
      </div>
      
      <!-- 右上角统计按钮 -->
      <div class="top-right-buttons">
        <button 
          class="info-button" 
          @click="navigateToStatistics"
        >
          📊 统计
        </button>
      </div>
    </div>
    
    <!-- 音乐播放器按钮（左上角圆形） -->
    <div v-if="selectedGame" class="music-button-container">
      <div 
        :class="musicButtonClass" 
        @click="toggleMusic"
        title="点击播放/暂停音乐"
      >
        🎵
      </div>
      <!-- 隐藏的实际音频元素 -->
      <audio 
        ref="audioPlayer" 
        :src="`/music/${selectedGame.shortName}.mp3`" 
        @play="handlePlay"
        @pause="handlePause"
        @ended="handlePause"
        @error="handleAudioError"
      >
        您的浏览器不支持音频播放
       </audio>
    </div>
    
    <!-- 倒计时对话框 -->
    <div v-if="countdownVisible" class="countdown-dialog">
      <div class="countdown-content">
        <div class="countdown-number">{{ countdown }}</div>
        <div class="countdown-text">秒后启动游戏</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGamesStore } from '../stores/games'

const router = useRouter()
const gamesStore = useGamesStore()

const selectedGame = ref(null)
const countdownVisible = ref(false)
const countdown = ref(3)
const audioPlayer = ref(null)
const isPlaying = ref(false)

// 计算音乐按钮的旋转动画类
const musicButtonClass = computed(() => ({
  'music-button': true,
  'playing': isPlaying.value
}))

const selectGame = (game) => {
  selectedGame.value = game
  
  // 播放对应游戏的音乐
  playGameMusic(game)
}

// 播放游戏音乐的独立函数
const playGameMusic = (game) => {
  if (!audioPlayer.value || !game) return
  
  // 先暂停当前播放的音乐
  audioPlayer.value.pause()
  audioPlayer.value.currentTime = 0
  
  // 设置新的音乐源
  const musicUrl = `/music/${game.shortName}.mp3`
  
  // 移除之前可能存在的事件监听器
  const audio = audioPlayer.value
  
  // 清除之前的事件监听器
  const originalCanplay = audio._canplayListener
  if (originalCanplay) {
    audio.removeEventListener('canplay', originalCanplay)
  }
  
  // 设置新的src
  audio.src = musicUrl
  
  // 监听canplay事件，确保音频加载完成后再播放
  const canplayListener = () => {
    audio.play().then(() => {
      isPlaying.value = true
      console.log(`正在播放: ${game.displayName} 的背景音乐`)
    }).catch(e => {
      console.log('音乐播放失败:', e)
      isPlaying.value = false
    })
    // 播放后移除监听器
    audio.removeEventListener('canplay', canplayListener)
  }
  
  // 保存监听器引用，以便后续移除
  audio._canplayListener = canplayListener
  audio.addEventListener('canplay', canplayListener)
  
  // 触发加载
  audio.load()
}

// 切换音乐播放/暂停
const toggleMusic = () => {
  if (!audioPlayer.value || !selectedGame.value) return
  
  if (isPlaying.value) {
    audioPlayer.value.pause()
    isPlaying.value = false
  } else {
    audioPlayer.value.play().then(() => {
      isPlaying.value = true
    }).catch(e => {
      console.log('音乐播放失败:', e)
    })
  }
}

// 处理音频播放事件
const handlePlay = () => {
  isPlaying.value = true
}

// 处理音频暂停事件
const handlePause = () => {
  isPlaying.value = false
}

const handleLaunch = async () => {
  if (!selectedGame.value) return
  
  if (selectedGame.value.installed) {
    // 启动游戏
    countdownVisible.value = true
    countdown.value = 3
    
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        countdownVisible.value = false
        gamesStore.launchGame(selectedGame.value.id, 0)
      }
    }, 1000)
  } else {
    // 安装游戏（暂时跳转设置页面）
    navigateToSettings()
  }
}

const navigateToSettings = () => {
  if (selectedGame.value) {
    router.push(`/settings?gameId=${selectedGame.value.id}`)
  } else {
    router.push('/settings')
  }
}

const navigateToStatistics = () => {
  router.push('/statistics')
}

const handleImageError = (event) => {
  // 图片加载失败时使用默认图片
  event.target.src = '/image/avatar/default.jpg'
}

const handleAudioError = (event) => {
  console.log('音频加载失败:', event)
}

onMounted(async () => {
  await gamesStore.fetchGames()
  // 默认选择第一个游戏并播放音乐
  if (gamesStore.games.length > 0) {
    const firstGame = gamesStore.games[0]
    selectedGame.value = firstGame
    // 延迟一点播放，确保音频元素已经准备好
    setTimeout(() => {
      playGameMusic(firstGame)
    }, 100)
  }
})
</script>

<style scoped>
.home-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

/* 背景图片 */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  filter: blur(20px) brightness(0.4);
  z-index: 0;
}

/* 侧边栏 */
.sidebar {
  position: absolute;
  left: 0;
  top: 0;
  width: 200px;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  z-index: 10;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  text-align: center;
}

.game-list {
  padding: 10px;
  height: calc(100% - 70px);
  overflow-y: auto;
}

/* 自定义滚动条 - 仅在使用时显示 */
.game-list {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.game-list::-webkit-scrollbar {
  width: 4px;
  display: none; /* Chrome, Safari and Opera */
}

.game-list:hover::-webkit-scrollbar {
  display: block;
}

.game-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.game-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.game-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 游戏项 */
.game-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
}

.game-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.game-item.active {
  background: rgba(64, 158, 255, 0.3);
  border: 1px solid rgba(64, 158, 255, 0.5);
}

.game-avatar {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.game-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.game-item:hover .game-avatar img {
  transform: scale(1.05);
}

.game-name {
  color: #fff;
  font-size: 14px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

/* 主内容区 */
.main-content {
  position: relative;
  margin-left: 200px;
  height: 100%;
  z-index: 5;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
  padding: 40px;
}

/* 主图片 */
.main-image-container {
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.main-image {
  width: 400px;
  height: 500px;
  object-fit: cover;
}

/* 右下角按钮 */
.bottom-buttons {
  position: relative;
  z-index: 2;
  text-align: right;
  margin-top: auto;
}

.status-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-bottom: 10px;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 按钮样式 */
.primary-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.primary-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.primary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.secondary-button {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.secondary-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.info-button {
  background: rgba(103, 194, 58, 0.3);
  color: white;
  border: 1px solid rgba(103, 194, 58, 0.5);
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.info-button:hover {
  background: rgba(103, 194, 58, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* 右上角按钮 */
.top-right-buttons {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 15;
}

/* 音乐播放器按钮（左上角圆形） */
.music-button-container {
  position: absolute;
  top: 20px;
  left: 220px;
  z-index: 20;
}

.music-button {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);
  font-size: 20px;
}

.music-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

/* 播放时的旋转动画 */
.music-button.playing {
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 倒计时 */
.countdown-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.countdown-content {
  text-align: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 300px;
}

.countdown-number {
  font-size: 72px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 20px;
}

.countdown-text {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-image {
    width: 300px;
    height: 400px;
  }
  
  .music-button-container {
    left: 210px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 150px;
  }
  
  .main-content {
    margin-left: 150px;
    padding: 20px;
  }
  
  .main-image {
    width: 250px;
    height: 350px;
  }
  
  .game-avatar {
    width: 100px;
    height: 100px;
  }
}
</style>