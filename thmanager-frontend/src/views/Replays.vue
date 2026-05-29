<template>
  <AppShell title="Replay 管理" subtitle="浏览与管理录像文件">
    <template #headerActions>
      <select v-model="filterGame" class="filter-select">
        <option value="">全部游戏</option>
        <option v-for="game in gamesStore.games" :key="game.id" :value="game.id">
          {{ getGameDisplayName(game) }}
        </option>
      </select>
      <AppButton size="sm" :loading="scanning" @click="handleScan">扫描新 Replay</AppButton>
    </template>

    <AppLoading v-if="loading && !replaysStore.replays.length" />
    <AppEmpty v-else-if="!filteredReplays.length" text="暂无 Replay 数据" />

    <AppCard v-else :flush="true">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>游戏</th>
              <th>难度</th>
              <th>角色</th>
              <th>分数</th>
              <th>关卡</th>
              <th>日期</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="replay in filteredReplays" :key="replay.id">
              <td>{{ replay.id }}</td>
              <td>{{ getGameName(replay.gameId) }}</td>
              <td>{{ replay.difficultyDisplay || replay.difficulty }}</td>
              <td>{{ replay.character || '-' }}</td>
              <td class="score-cell">{{ formatScore(replay.totalScore) }}</td>
              <td>{{ replay.reachedStageNumber || '-' }}</td>
              <td>{{ formatDate(replay.gameDate) }}</td>
              <td>
                <AppButton variant="ghost" size="sm" @click="handleDelete(replay)">删除</AppButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AppCard>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useReplaysStore } from '@/stores/replays'
import { useGamesStore } from '@/stores/games'
import { formatScore, getGameDisplayName } from '@/utils/format'
import AppShell from '@/components/layout/AppShell.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppLoading from '@/components/ui/AppLoading.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'

const replaysStore = useReplaysStore()
const gamesStore = useGamesStore()
const loading = ref(false)
const scanning = ref(false)
const filterGame = ref('')

const gameNameMap = computed(() => {
  const map = {}
  gamesStore.games.forEach(g => { map[g.id] = getGameDisplayName(g) })
  return map
})

const getGameName = (id) => gameNameMap.value[id] || '未知游戏'
const formatDate = (d) => d ? String(d).split('T')[0] : '-'

const filteredReplays = computed(() => {
  let list = replaysStore.replays
  if (filterGame.value) list = list.filter(r => r.gameId === Number(filterGame.value))
  return list
})

const handleScan = async () => {
  scanning.value = true
  try { await replaysStore.scanNewReplays() }
  finally { scanning.value = false }
}

const handleDelete = async (replay) => {
  if (confirm('确定要删除这个 Replay 吗？')) {
    await replaysStore.deleteReplay(replay.id)
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([gamesStore.fetchGames(), replaysStore.fetchReplays()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.filter-select {
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--text-sm);
  outline: none;
}
.filter-select option { background: var(--color-bg-elevated); }

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
}
.data-table td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}
.data-table tr:hover td { background: rgba(255, 255, 255, 0.02); }
.score-cell { font-family: var(--font-mono); font-weight: 600; color: var(--color-accent-gold) !important; }
</style>
