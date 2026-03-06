<template>
  <div class="replays-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Replay 管理</span>
          <div class="header-actions">
            <el-select v-model="filterGame" placeholder="筛选游戏" clearable style="width: 200px; margin-right: 10px;">
              <el-option
                v-for="game in gamesStore.installedGames"
                :key="game.id"
                :label="game.displayName"
                :value="game.id"
              />
            </el-select>
            <el-button type="primary" @click="handleScan">
              <el-icon><Refresh /></el-icon>
              扫描新 Replay
            </el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="filteredReplays" 
        v-loading="replaysStore.loading" 
        stripe
        height="calc(100vh - 250px)"
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="gameTitle" label="游戏" width="150" />
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fullShotType" label="机体" width="120" />
        <el-table-column prop="difficultyDisplay" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficultyDisplay)" size="small">
              {{ row.difficultyDisplay }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="formattedScore" label="分数" width="150" align="right" />
        <el-table-column prop="stage" label="到达面" width="100" />
        <el-table-column label="通关" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.cleared" color="#67C23A"><CircleCheck /></el-icon>
            <el-icon v-else color="#F56C6C"><CircleClose /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="gameDate" label="日期" width="150" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetails(row)">
              详情
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Replay 详情对话框 -->
    <el-dialog v-model="detailVisible" title="Replay 详情" width="700px">
      <el-descriptions :column="2" border v-if="selectedReplay">
        <el-descriptions-item label="文件名" :span="2">{{ selectedReplay.fileName }}</el-descriptions-item>
        <el-descriptions-item label="游戏">{{ selectedReplay.gameTitle }}</el-descriptions-item>
        <el-descriptions-item label="机体">{{ selectedReplay.fullShotType }}</el-descriptions-item>
        <el-descriptions-item label="难度">
          <el-tag :type="getDifficultyType(selectedReplay.difficultyDisplay)">
            {{ selectedReplay.difficultyDisplay }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分数">{{ selectedReplay.formattedScore }}</el-descriptions-item>
        <el-descriptions-item label="到达面">{{ selectedReplay.stage }}</el-descriptions-item>
        <el-descriptions-item label="通关">
          <el-icon v-if="selectedReplay.cleared" color="#67C23A"><CircleCheck /></el-icon>
          <el-icon v-else color="#F56C6C"><CircleClose /></el-icon>
        </el-descriptions-item>
        <el-descriptions-item label="游戏日期">{{ selectedReplay.gameDate }}</el-descriptions-item>
        <el-descriptions-item label="慢速率">{{ selectedReplay.slowRate }}%</el-descriptions-item>
        <el-descriptions-item label="总帧数">{{ selectedReplay.totalFrames }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useReplaysStore } from '../stores/replays'
import { useGamesStore } from '../stores/games'
import { ElMessage, ElMessageBox } from 'element-plus'

const replaysStore = useReplaysStore()
const gamesStore = useGamesStore()

const filterGame = ref(null)
const detailVisible = ref(false)
const selectedReplay = ref(null)

const filteredReplays = computed(() => {
  let list = replaysStore.replays
  if (filterGame.value) {
    list = list.filter(r => r.gameId === filterGame.value)
  }
  return list
})

const getDifficultyType = (diff) => {
  const types = {
    'E': '',
    'N': 'success',
    'H': 'warning',
    'L': 'danger',
    'Ex': 'danger'
  }
  return types[diff] || ''
}

const handleScan = async () => {
  const result = await replaysStore.scanNewReplays()
  ElMessage.success(`扫描完成，导入 ${result.imported || 0} 个 Replay`)
}

const viewDetails = (replay) => {
  selectedReplay.value = replay
  detailVisible.value = true
}

const handleDelete = async (replay) => {
  try {
    await ElMessageBox.confirm('确定要删除这个 Replay 记录吗？', '提示', {
      type: 'warning'
    })
    await replaysStore.deleteReplay(replay.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  replaysStore.fetchReplays()
})
</script>

<style scoped>
.replays-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
