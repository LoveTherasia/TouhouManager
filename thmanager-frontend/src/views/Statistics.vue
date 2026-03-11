<template>
  <div class="statistics-page">
    <div class="back-button-container">
      <button class="back-button" @click="goHome">
        ←
      </button>
    </div>
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
                    <th class="sortable-header">
                      <span class="header-text">难度</span>
                      <span class="sort-icons">
                        <span 
                          :class="['sort-icon', 'up', { active: sortBy === 'difficulty' && sortOrder === 'asc' }]"
                          @click="handleSort('difficulty', 'asc')"
                        >▲</span>
                        <span 
                          :class="['sort-icon', 'down', { active: sortBy === 'difficulty' && sortOrder === 'desc' }]"
                          @click="handleSort('difficulty', 'desc')"
                        >▼</span>
                      </span>
                    </th>
                    <th class="sortable-header">
                      <span class="header-text">分数</span>
                      <span class="sort-icons">
                        <span 
                          :class="['sort-icon', 'up', { active: sortBy === 'totalScore' && sortOrder === 'asc' }]"
                          @click="handleSort('totalScore', 'asc')"
                        >▲</span>
                        <span 
                          :class="['sort-icon', 'down', { active: sortBy === 'totalScore' && sortOrder === 'desc' }]"
                          @click="handleSort('totalScore', 'desc')"
                        >▼</span>
                      </span>
                    </th>
                    <th class="sortable-header">
                      <span class="header-text">关卡</span>
                      <span class="sort-icons">
                        <span 
                          :class="['sort-icon', 'up', { active: sortBy === 'stage' && sortOrder === 'asc' }]"
                          @click="handleSort('stage', 'asc')"
                        >▲</span>
                        <span 
                          :class="['sort-icon', 'down', { active: sortBy === 'stage' && sortOrder === 'desc' }]"
                          @click="handleSort('stage', 'desc')"
                        >▼</span>
                      </span>
                    </th>
                    <th class="sortable-header">
                      <span class="header-text">日期</span>
                      <span class="sort-icons">
                        <span 
                          :class="['sort-icon', 'up', { active: sortBy === 'date' && sortOrder === 'asc' }]"
                          @click="handleSort('date', 'asc')"
                        >▲</span>
                        <span 
                          :class="['sort-icon', 'down', { active: sortBy === 'date' && sortOrder === 'desc' }]"
                          @click="handleSort('date', 'desc')"
                        >▼</span>
                      </span>
                    </th>
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
                共 {{ filteredReplays.length }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
              </div>
              <div class="pagination-buttons">
                <button 
                  class="pagination-button" 
                  :disabled="currentPage === 1"
                  @click="currentPage--"
                >
                  上一页
                </button>
                <button 
                  v-for="page in visiblePages" 
                  :key="page"
                  :class="['pagination-button', { active: currentPage === page }]"
                  @click="currentPage = page"
                >
                  {{ page }}
                </button>
                <button 
                  class="pagination-button" 
                  :disabled="currentPage === totalPages"
                  @click="currentPage++"
                >
                  下一页
                </button>
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
import { useRouter } from 'vue-router'
import { useStatisticsStore } from '../stores/statistics'
import { useGamesStore } from '../stores/games'
import { useReplaysStore } from '../stores/replays'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const statisticsStore = useStatisticsStore()
const gamesStore = useGamesStore()
const replaysStore = useReplaysStore()

const activeTab = ref('statistics')
const loading = ref(false)
const replaysLoading = ref(false)
const replayFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const sortBy = ref('totalScore')
const sortOrder = ref('desc')

const handleSort = (field, order) => {
  if (sortBy.value === field && sortOrder.value === order) {
    sortBy.value = 'totalScore'
    sortOrder.value = 'desc'
  } else {
    sortBy.value = field
    sortOrder.value = order
  }
  currentPage.value = 1
}

const sortReplays = (replays) => {
  const sorted = [...replays]
  
  const difficultyOrder = {
    'Easy': 0,
    'E': 0,
    'Normal': 1,
    'N': 1,
    'Hard': 2,
    'H': 2,
    'Lunatic': 3,
    'L': 3,
    'Extra': 4,
    'Ex': 4,
    'Phantasm': 5,
    'Ph': 5
  }
  
  sorted.sort((a, b) => {
    let aVal, bVal
    switch (sortBy.value) {
      case 'difficulty':
        aVal = difficultyOrder[a.difficulty] !== undefined ? difficultyOrder[a.difficulty] : 99
        bVal = difficultyOrder[b.difficulty] !== undefined ? difficultyOrder[b.difficulty] : 99
        break
      case 'totalScore':
        aVal = a.totalScore
        bVal = b.totalScore
        break
      case 'stage':
        aVal = a.stage
        bVal = b.stage
        break
      case 'date':
        aVal = a.date
        bVal = b.date
        break
      default:
        aVal = a.totalScore
        bVal = b.totalScore
    }
    
    if (typeof aVal === 'number' && typeof bVal === 'number') {
      return sortOrder.value === 'asc' ? aVal - bVal : bVal - aVal
    } else {
      aVal = String(aVal || '')
      bVal = String(bVal || '')
      return sortOrder.value === 'asc' 
        ? aVal.localeCompare(bVal)
        : bVal.localeCompare(aVal)
    }
  })
  return sorted
}

const totalPages = computed(() => {
  const count = filteredReplays.value.length;
  const size = pageSize.value || 10;
  console.log(filteredReplays);
  console.log(filteredReplays.value.length);
  return count == 0 ? 1 : Math.ceil(count / size);
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 5; i++) pages.push(i)
    } else if (current >= total - 2) {
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      for (let i = current - 2; i <= current + 2; i++) pages.push(i)
    }
  }
  return pages
})

const filteredReplays = computed(() => {
  let result
  if (!replayFilter.value) {
    result = replaysStore.replays
  } else {
    const filter = replayFilter.value.toLowerCase()
    result = replaysStore.replays.filter(replay => 
      replay.gameTitle.toLowerCase().includes(filter) ||
      replay.playerName.toLowerCase().includes(filter) ||
      replay.difficulty.toLowerCase().includes(filter)
    )
  }
  return sortReplays(result)
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
  height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  overflow-y: auto;
  box-sizing: border-box;
}

.back-button-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  z-index: 1000;
}

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateX(-30px);
  transition: all 0.3s ease;
  pointer-events: none;
}

.back-button-container:hover .back-button {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
}

.back-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.back-button:active {
  transform: scale(0.95);
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

.pagination-buttons {
  display: flex;
  gap: 4px;
  margin-left: 20px;
}

.pagination-button {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.pagination-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 0%);
  border-color: transparent;
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sortable-header {
  position: relative;
  cursor: pointer;
  user-select: none;
}

.header-text {
  margin-right: 8px;
}

.sort-icons {
  display: inline-flex;
  flex-direction: column;
  opacity: 0;
  transition: opacity 0.2s ease;
  vertical-align: middle;
}

.sortable-header:hover .sort-icons {
  opacity: 1;
}

.sort-icon {
  font-size: 10px;
  line-height: 1;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 1px 0;
}

.sort-icon:hover {
  color: rgba(255, 255, 255, 0.7);
}

.sort-icon.active {
  color: #667eea;
  opacity: 1;
}
</style>