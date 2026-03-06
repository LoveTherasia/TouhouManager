<template>
  <div class="settings-page">
    <div class="card">
      <div class="card-header">
        <span>游戏路径设置</span>
        <button class="primary-button" @click="saveSettings">
          ✅ 保存设置
        </button>
      </div>
      <div class="card-body">
        <div class="alert info">
          <div class="alert-icon">ℹ️</div>
          <div class="alert-content">
            <div class="alert-title">提示</div>
            <div class="alert-description">请设置各游戏的安装路径，设置后系统会自动检测游戏是否已安装并扫描 Replay 文件。</div>
          </div>
        </div>
        
        <div v-if="loading" class="loading">
          加载中...
        </div>
        <div v-else class="settings-form">
          <div v-for="game in gamesStore.games" :key="game.id" class="form-group">
            <label class="form-label">
              {{ game.displayName }}
              <span :class="['status-badge', game.installed ? 'installed' : 'not-installed']">
                {{ game.installed ? '已安装' : '未安装' }}
              </span>
            </label>
            <div class="path-input-group">
              <input 
                type="text" 
                v-model="gamePaths[game.id]" 
                class="path-input"
                placeholder="请输入游戏安装路径"
              />
              <button class="secondary-button" @click="browsePath(game.id)">
                浏览
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useGamesStore } from '../stores/games'

const route = useRoute()
const gamesStore = useGamesStore()

const loading = ref(false)
const gamePaths = ref({})

const loadGamePaths = () => {
  gamesStore.games.forEach(game => {
    gamePaths.value[game.id] = game.path || ''
  })
}

const saveSettings = async () => {
  loading.value = true
  try {
    for (const [gameId, path] of Object.entries(gamePaths.value)) {
      await gamesStore.updateGamePath(parseInt(gameId), path)
    }
    await gamesStore.fetchGames()
    alert('设置保存成功！')
  } catch (error) {
    console.error('保存设置失败:', error)
    alert('保存设置失败，请重试')
  } finally {
    loading.value = false
  }
}

const browsePath = (gameId) => {
  // 这里可以实现文件选择对话框
  // 由于是模拟环境，暂时使用 prompt
  const path = prompt('请输入游戏安装路径:')
  if (path) {
    gamePaths.value[gameId] = path
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await gamesStore.fetchGames()
    loadGamePaths()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.settings-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
}

.card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.primary-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.4);
}

.primary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.6);
}

.card-body {
  padding: 20px;
}

.alert {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
  background: rgba(144, 147, 153, 0.1);
  border: 1px solid rgba(144, 147, 153, 0.2);
}

.alert.info {
  background: rgba(144, 147, 153, 0.1);
  border-color: rgba(144, 147, 153, 0.2);
}

.alert-icon {
  font-size: 20px;
  margin-top: 2px;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #fff;
}

.alert-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.loading {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.6);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.installed {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.status-badge.not-installed {
  background: rgba(144, 147, 153, 0.2);
  color: #909399;
  border: 1px solid rgba(144, 147, 153, 0.3);
}

.path-input-group {
  display: flex;
  gap: 8px;
}

.path-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.path-input:hover {
  border-color: rgba(64, 158, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.secondary-button {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.secondary-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .path-input-group {
    flex-direction: column;
  }
  
  .secondary-button {
    align-self: flex-start;
  }
}
</style>