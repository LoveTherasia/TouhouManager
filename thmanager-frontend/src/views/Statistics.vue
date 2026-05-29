<template>
  <AppShell title="数据统计" subtitle="Replay 记录与游玩数据概览">
    <AppTabs v-model="activeTab" :tabs="tabs" class="stats-tabs" />

    <!-- 统计概览 -->
    <div v-if="activeTab === 'statistics'" class="stats-grid">
      <div class="stats-overview">
        <div v-for="item in overviewItems" :key="item.label" class="stat-card">
          <div class="stat-card__value">{{ item.value }}</div>
          <div class="stat-card__label">{{ item.label }}</div>
        </div>
      </div>

      <AppCard title="游戏时长排行" hover>
        <AppLoading v-if="loading" />
        <AppEmpty v-else-if="!statisticsStore.playTimeStats.length" />
        <div v-else class="rank-list">
          <div v-for="(item, i) in statisticsStore.playTimeStats" :key="item.gameId" class="rank-item">
            <span class="rank-item__num">{{ i + 1 }}</span>
            <div class="rank-item__info">
              <div class="rank-item__name">{{ item.gameName }}</div>
              <div class="rank-item__sub">{{ item.replayCount }} 条 Replay</div>
            </div>
            <span class="rank-item__value">{{ formatPlayTime(item.playTime) }}</span>
          </div>
        </div>
      </AppCard>

      <AppCard title="最高分 TOP 20" class="stats-full" :flush="true">
        <AppLoading v-if="loading" />
        <AppEmpty v-else-if="!statisticsStore.scoreStats.length" />
        <div v-else class="table-wrap">
          <table class="data-table">
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
              <tr v-for="(score, i) in statisticsStore.scoreStats" :key="score.id">
                <td>{{ i + 1 }}</td>
                <td>{{ score.gameTitle || getGameName(score.gameId) }}</td>
                <td>{{ score.playerName || score.PlayerName }}</td>
                <td><AppBadge variant="gold">{{ score.difficulty }}</AppBadge></td>
                <td class="score-cell">{{ formatScore(score.totalScore) }}</td>
                <td>{{ formatDate(score.gameDate || score.date) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </AppCard>
    </div>

    <!-- Replay 列表 -->
    <div v-else class="replays-section">
      <AppCard :flush="true">
        <template #header>
          <span>Replay 列表</span>
        </template>
        <template #actions>
          <input v-model="replayFilter" class="search-input" placeholder="搜索 Replay..." />
        </template>

        <AppLoading v-if="replaysLoading" />
        <AppEmpty v-else-if="!filteredReplays.length" text="暂无 Replay 数据" />
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>游戏</th>
                  <th>玩家</th>
                  <th @click="toggleSort('difficulty')" class="sortable">难度 {{ sortIcon('difficulty') }}</th>
                  <th @click="toggleSort('totalScore')" class="sortable">分数 {{ sortIcon('totalScore') }}</th>
                  <th @click="toggleSort('stage')" class="sortable">关卡 {{ sortIcon('stage') }}</th>
                  <th @click="toggleSort('date')" class="sortable">日期 {{ sortIcon('date') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(replay, i) in paginatedReplays" :key="replay.id">
                  <td>{{ (currentPage - 1) * pageSize + i + 1 }}</td>
                  <td>{{ getGameName(replay.gameId) }}</td>
                  <td>{{ replay.playerName }}</td>
                  <td>{{ replay.difficultyDisplay || replay.difficulty }}</td>
                  <td class="score-cell">{{ formatScore(replay.totalScore) }}</td>
                  <td>{{ replay.reachedStageNumber || '-' }}</td>
                  <td>{{ formatDate(replay.gameDate) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination">
            <span class="pagination__info">共 {{ filteredReplays.length }} 条 · 第 {{ currentPage }}/{{ totalPages }} 页</span>
            <div class="pagination__btns">
              <AppButton variant="ghost" size="sm" :disabled="currentPage === 1" @click="currentPage--">上一页</AppButton>
              <AppButton variant="ghost" size="sm" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</AppButton>
            </div>
          </div>
        </div>
      </AppCard>
    </div>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStatisticsStore } from '@/stores/statistics'
import { useGamesStore } from '@/stores/games'
import { useReplaysStore } from '@/stores/replays'
import { formatScore, formatPlayTime } from '@/utils/format'
import AppShell from '@/components/layout/AppShell.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppTabs from '@/components/ui/AppTabs.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppLoading from '@/components/ui/AppLoading.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'

const statisticsStore = useStatisticsStore()
const gamesStore = useGamesStore()
const replaysStore = useReplaysStore()

const tabs = [
  { key: 'statistics', label: '统计分析' },
  { key: 'replays', label: 'Replay 列表' }
]

const activeTab = ref('statistics')
const loading = ref(false)
const replaysLoading = ref(false)
const replayFilter = ref('')
const currentPage = ref(1)
const pageSize = 10
const sortBy = ref('totalScore')
const sortOrder = ref('desc')

const overviewItems = computed(() => [
  { label: '游戏数量', value: statisticsStore.stats.totalGames },
  { label: 'Replay 数量', value: statisticsStore.stats.totalReplays },
  { label: '总游玩时长', value: formatPlayTime(statisticsStore.stats.totalPlayTime) },
  { label: '通关次数', value: statisticsStore.stats.clearedCount }
])

const gameNameMap = computed(() => {
  const map = {}
  gamesStore.games.forEach(g => { map[g.id] = g.displayName || g.titleCn })
  return map
})

const getGameName = (id) => gameNameMap.value[id] || '未知游戏'

const formatDate = (d) => d ? String(d).split('T')[0] : '-'

const toggleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortOrder.value = 'desc'
  }
  currentPage.value = 1
}

const sortIcon = (field) => sortBy.value === field ? (sortOrder.value === 'asc' ? '↑' : '↓') : ''

const sortReplays = (list) => {
  const order = { Easy: 0, E: 0, Normal: 1, N: 1, Hard: 2, H: 2, Lunatic: 3, L: 3, Extra: 4, Ex: 4, Phantasm: 5, Ph: 5 }
  return [...list].sort((a, b) => {
    let av, bv
    switch (sortBy.value) {
      case 'difficulty':
        av = order[a.difficulty] ?? 99; bv = order[b.difficulty] ?? 99; break
      case 'stage':
        av = a.reachedStageNumber ?? 0; bv = b.reachedStageNumber ?? 0; break
      case 'date':
        av = a.gameDate || ''; bv = b.gameDate || ''; break
      default:
        av = a.totalScore ?? 0; bv = b.totalScore ?? 0
    }
    if (typeof av === 'number') return sortOrder.value === 'asc' ? av - bv : bv - av
    return sortOrder.value === 'asc' ? String(av).localeCompare(String(bv)) : String(bv).localeCompare(String(av))
  })
}

const filteredReplays = computed(() => {
  let list = replaysStore.replays
  if (replayFilter.value) {
    const q = replayFilter.value.toLowerCase()
    list = list.filter(r =>
      getGameName(r.gameId).toLowerCase().includes(q) ||
      r.playerName?.toLowerCase().includes(q) ||
      r.difficulty?.toLowerCase().includes(q)
    )
  }
  return sortReplays(list)
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredReplays.value.length / pageSize)))
const paginatedReplays = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredReplays.value.slice(start, start + pageSize)
})

onMounted(async () => {
  loading.value = true
  replaysLoading.value = true
  try {
    await Promise.all([
      gamesStore.fetchGames(),
      replaysStore.fetchReplays(),
      statisticsStore.fetchStatistics(),
      statisticsStore.fetchPlayTimeStats(),
      statisticsStore.fetchScoreStats()
    ])
  } finally {
    loading.value = false
    replaysLoading.value = false
  }
})
</script>

<style scoped>
.stats-tabs { margin-bottom: var(--space-6); }

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  text-align: center;
}

.stat-card__value {
  font-family: var(--font-mono);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-accent-gold);
  margin-bottom: var(--space-2);
}

.stat-card__label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.stats-full { margin-top: var(--space-2); }

.rank-list { display: flex; flex-direction: column; gap: var(--space-2); }

.rank-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--color-border);
  transition: border-color var(--transition-fast);
}
.rank-item:hover { border-color: var(--color-border-active); }

.rank-item__num {
  font-family: var(--font-mono);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-accent-crimson);
  width: 28px;
}

.rank-item__info { flex: 1; }
.rank-item__name { font-size: var(--text-sm); font-weight: 600; }
.rank-item__sub { font-size: var(--text-xs); color: var(--color-text-muted); margin-top: 2px; }
.rank-item__value { font-family: var(--font-mono); font-size: var(--text-sm); color: var(--color-accent-gold); }

.table-wrap { overflow-x: auto; }

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.data-table th {
  padding: var(--space-3) var(--space-4);
  text-align: left;
  font-weight: 600;
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
}

.data-table td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}

.data-table tr:hover td { background: rgba(255, 255, 255, 0.02); }

.sortable { cursor: pointer; user-select: none; }
.sortable:hover { color: var(--color-text-primary); }

.score-cell {
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--color-accent-gold) !important;
}

.search-input {
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--text-sm);
  outline: none;
  width: 200px;
}
.search-input:focus { border-color: var(--color-border-active); }

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-border);
}

.pagination__info { font-size: var(--text-sm); color: var(--color-text-muted); }
.pagination__btns { display: flex; gap: var(--space-2); }

@media (max-width: 900px) {
  .stats-overview { grid-template-columns: repeat(2, 1fr); }
}
</style>
