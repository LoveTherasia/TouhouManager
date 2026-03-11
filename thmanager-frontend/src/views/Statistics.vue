<template>
  <div class="statistics-page">
    <div class="tabs">
      <button 
        :class="['tab-button', { active: activeTab === 'statistics' }]"
        @click="activeTab = 'statistics'"
      >
        统计分析
      </button>
      <button 
        :class="['tab-button', { active: activeTab === 'replays' }]"
        @click="activeTab = 'replays'"
      >
        Replays
      </button>
    </div>
    
    <div class="tab-content">
      <!-- 统计分析标签页 -->
      <div v-if="activeTab === 'statistics'" class="statistics-content">
        <div class="grid">
          <div class="card">
            <div class="card-header">总体概览</div>
            <div class="card-body">
              <div v-if="loading" class="loading">加载中...</div>
              <div v-else class="stats-overview">
                <div class="stat-item">
                  <div class="stat-value">{{ statisticsStore.stats.totalGames }}</div>
                  <div class="stat-label">游戏数量</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ statisticsStore.stats.totalReplays }}</div>
                  <div class="stat-label">Replay 数量</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatPlayTime(statisticsStore.stats.totalPlayTime) }}</div>
                  <div class="stat-label">总游玩时长</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ statisticsStore.stats.clearedCount }}</div>
                  <div class="stat-label">通关次数</div>
                </div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="card-header">游戏时长排行</div>
            <div class="card-body">
              <div v-if="loading" class="loading">加载中...</div>
              <div v-else-if="statisticsStore.playTimeStats.length === 0" class="empty">暂无数据</div>
              <div v-else class="playtime-list">
                <div v-for="(item, index) in statisticsStore.playTimeStats" :key="item.gameId" class="playtime-item">
                  <div class="playtime-rank">#{{ index + 1 }}</div>
                  <div class="playtime-info">
                    <div class="playtime-name">{{ item.gameName }}</div>
                    <div class="playtime-detail">{{ item.replayCount }} 条 Replay</div>
                  </div>
                  <div class="playtime-time">{{ formatPlayTime(item.playTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card mt-20">
          <div class="card-header">最高分 TOP 20</div>
          <div class="card-body">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="statisticsStore.scoreStats.length === 0" class="empty">暂无数据</div>
            <div v-else class="scores-table">
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>游戏</th>
                    <th>玩家</th>
                    <th>难度</th>
                    <th>分数</th>
                    <th>日期</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(score, index) in statisticsStore.scoreStats" :key="score.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ score.gameTitle }}</td>
                    <td>{{ score.playerName }}</td>
                    <td>{{ score.difficulty }}</td>
                    <td class="score-highlight">{{ formatScore(score.totalScore) }}</td>
                    <td>{{ score.date ? score.date.split('T')[0] : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Replays 标签页 -->
      <div v-if="activeTab === 'replays'" class="replays-content">
        <div class="card">
          <div class="card-header">
            <span>Replay 列表</span>
            <div class="header-actions">
              <input 
                v-model="replayFilter" 
                placeholder="搜索 Replay" 
                class="search-input"
              />
            </div>
          </div>
          <div class="card-body">
            <div v-if="replaysLoading" class="loading">加载中...</div>
            <div v-else-if="filteredReplays.length === 0" class="empty">
              暂无 Replay 数据
            </div>
            <div v-else class="replays-table">
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>游戏</th>
                    <th>玩家</th>
                    <th>难度</th>
                    <th>分数</th>
                    <th>关卡</th>
                    <th>日期</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(replay, index) in paginatedReplays" :key="replay.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ replay.gameTitle }}</td>
                    <td>{{ replay.playerName }}</td>
                    <td>{{ replay.difficultyDisplay }}</td>
                    <td>{{ formatScore(replay.totalScore) }}</td>
                    <td>{{ replay.stage }}</td>
                    <td>{{ replay.date }}</td>
                    <td class="actions">
                      <button class="action-button" @click="viewReplay(replay)">
                        查看
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="pagination" v-if="filteredReplays.length > 0">
              <div class="pagination-info">
                共 {{ filteredReplays.length }} 条记录
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStatisticsStore } from '../stores/statistics'
import { useGamesStore } from '../stores/games'
import { useReplaysStore } from '../stores/replays'

const statisticsStore = useStatisticsStore()
const gamesStore = useGamesStore()
const replaysStore = useReplaysStore()

const activeTab = ref('statistics')
const loading = ref(false)
const replaysLoading = ref(false)
const replayFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredReplays = computed(() => {
  if (!replayFilter.value) {
    return replaysStore.replays
  }
  const filter = replayFilter.value.toLowerCase()
  return replaysStore.replays.filter(replay => 
    replay.gameTitle.toLowerCase().includes(filter) ||
    replay.playerName.toLowerCase().includes(filter) ||
    replay.difficulty.toLowerCase().includes(filter)
  )
})

const paginatedReplays = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReplays.value.slice(start, end)
})

const formatScore = (score) => {
  return score.toLocaleString()
}

const formatPlayTime = (minutes) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  }
  return `${mins}分钟`
}

const viewReplay = (replay) => {
  console.log('查看 Replay:', replay)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(async () => {
  loading.value = true
  replaysLoading.value = true
  try {
    await gamesStore.fetchGames()
    await replaysStore.fetchReplays()
    await statisticsStore.fetchStatistics()
    await statisticsStore.fetchPlayTimeStats()
    await statisticsStore.fetchScoreStats()
  } finally {
    loading.value = false
    replaysLoading.value = false
  }
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
}

.tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 2px;
}

.tab-button {
  flex: 1;
  padding: 10px 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 600;
}

.tab-button:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.tab-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.4);
}

.tab-content {
  min-height: 400px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
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
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
}

.search-input {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  width: 200px;
}

.search-input:hover {
  border-color: rgba(64, 158, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.card-body {
  padding: 20px;
}

.loading, .empty {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.6);
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.5);
  border: 1px dashed rgba(255, 255, 255, 0.2);
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.mt-20 {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  th, td {
    padding: 8px 10px;
    font-size: 14px;
  }
}
.stats-overview {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.playtime-list {
  max-height: 300px;
  overflow-y: auto;
}

.playtime-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.playtime-item:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(102, 126, 234, 0.3);
}

.playtime-rank {
  width: 40px;
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.playtime-info {
  flex: 1;
}

.playtime-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.playtime-detail {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.playtime-time {
  font-size: 14px;
  font-weight: 600;
  color: #764ba2;
}

.scores-table {
  overflow-x: auto;
}

.score-highlight {
  font-weight: 700;
  color: #667eea;
}
</style>