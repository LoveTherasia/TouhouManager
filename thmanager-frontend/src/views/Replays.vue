<template>
  <div class="replays-page">
    <div class="card">
      <div class="card-header">
        <span>Replay 管理</span>
        <div class="header-actions">
          <select v-model="filterGame" class="custom-select" style="width: 200px; margin-right: 10px;">
            <option value="">筛选游戏</option>
            <option
              v-for="game in gamesStore.installedGames"
              :key="game.id"
              :value="game.id"
            >{{ game.displayName }}</option>
          </select>
          <button class="primary-button" @click="handleScan">
            🔄 扫描新 Replay
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading">
          加载中...
        </div>
        <div v-else-if="replays.length === 0" class="empty">
          暂无 Replay 数据
        </div>
        <div v-else class="replays-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>游戏</th>
                <th>难度</th>
                <th>角色</th>
                <th>分数</th>
                <th>日期</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="replay in filteredReplays" :key="replay.id">
                <td>{{ replay.id }}</td>
                <td>{{ getGameName(replay.gameId) }}</td>
                <td>{{ replay.difficulty }}</td>
                <td>{{ replay.character }}</td>
                <td>{{ replay.score.toLocaleString() }}</td>
                <td>{{ formatDate(replay.date) }}</td>
                <td class="actions">
                  <button class="action-button" @click="handleView(replay)">
                    查看
                  </button>
                  <button class="action-button delete" @click="handleDelete(replay)">
                    删除
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
import { ref, computed, onMounted } from 'vue'
import { useReplaysStore } from '../stores/replays'
import { useGamesStore } from '../stores/games'

const replaysStore = useReplaysStore()
const gamesStore = useGamesStore()

const loading = ref(false)
const filterGame = ref('')

const replays = computed(() => replaysStore.replays)

const filteredReplays = computed(() => {
  if (!filterGame.value) {
    return replays.value
  }
  return replays.value.filter(replay => replay.gameId === filterGame.value)
})

const getGameName = (gameId) => {
  const game = gamesStore.games.find(g => g.id === gameId)
  return game ? game.displayName : '未知游戏'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const handleScan = async () => {
  loading.value = true
  try {
    await replaysStore.scanReplays()
  } finally {
    loading.value = false
  }
}

const handleView = (replay) => {
  console.log('查看 Replay:', replay)
  // 实现查看 Replay 的逻辑
}

const handleDelete = async (replay) => {
  if (confirm('确定要删除这个 Replay 吗？')) {
    await replaysStore.deleteReplay(replay.id)
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await gamesStore.fetchGames()
    await replaysStore.fetchReplays()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.replays-page {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.custom-select {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.custom-select:hover {
  border-color: rgba(64, 158, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.custom-select option {
  background: #1a1a2e;
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

.replays-table {
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

.action-button.delete {
  background: rgba(255, 87, 34, 0.2);
  border-color: rgba(255, 87, 34, 0.4);
}

.action-button.delete:hover {
  background: rgba(255, 87, 34, 0.3);
  border-color: rgba(255, 87, 34, 0.6);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .custom-select {
    width: 150px;
  }
  
  th, td {
    padding: 8px 10px;
    font-size: 14px;
  }
}
</style>