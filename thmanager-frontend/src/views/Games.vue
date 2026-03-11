<template>
  <div class="games-page">
    <div class="card">
      <div class="card-header">
        <span>游戏库</span>
        <button class="primary-button" @click="refreshGames">
          🔄 刷新
        </button>
      </div>
      <div class="card-body">
        <div v-if="gamesStore.loading" class="loading">
          加载中...
        </div>
        <div v-else-if="gamesStore.games.length === 0" class="empty">
          暂无游戏数据
        </div>
        <div v-else class="games-table">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>游戏名称</th>
                <th>版本</th>
                <th>状态</th>
                <th>路径</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(game, index) in gamesStore.games" :key="game.id">
                <td>{{ index + 1 }}</td>
                <td class="game-name">
                  <span :class="['status-tag', game.installed ? 'installed' : 'not-installed']">
                    {{ game.installed ? '已安装' : '未安装' }}
                  </span>
                  {{ game.displayName }}
                </td>
                <td>{{ game.version }}</td>
                <td>{{ game.installed ? '已安装' : '未安装' }}</td>
                <td class="game-path">{{ game.installPath || '未设置' }}</td>
                <td class="actions">
                  <button class="action-button" @click="navigateToSettings(game)">
                    设置
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGamesStore } from '../stores/games'

const router = useRouter()
const gamesStore = useGamesStore()

const refreshGames = async () => {
  await gamesStore.fetchGames()
}

const navigateToSettings = (game) => {
  router.push(`/settings?gameId=${game.id}`)
}

onMounted(async () => {
  await gamesStore.fetchGames()
})
</script>

<style scoped>
.games-page {
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

.loading, .empty {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.6);
}

.games-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

th {
  background: rgba(255, 255, 255, 0.05);
  font-weight: 600;
  color: #fff;
}

tr:hover {
  background: rgba(255, 255, 255, 0.03);
}

.game-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-tag.installed {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.status-tag.not-installed {
  background: rgba(144, 147, 153, 0.2);
  color: #909399;
  border: 1px solid rgba(144, 147, 153, 0.3);
}

.game-path {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(255, 255, 255, 0.7);
}

.actions {
  display: flex;
  gap: 8px;
}

.action-button {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  th, td {
    padding: 8px 10px;
    font-size: 14px;
  }
  
  .game-path {
    max-width: 200px;
  }
}
</style>