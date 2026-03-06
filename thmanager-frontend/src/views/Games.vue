<template>
  <div class="games-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>游戏库</span>
          <el-button type="primary" @click="refreshGames">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="gamesStore.games" v-loading="gamesStore.loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="displayName" label="游戏名称" min-width="200">
          <template #default="{ row }">
            <div class="game-name">
              <el-tag v-if="row.installed" type="success" size="small">已安装</el-tag>
              <el-tag v-else type="info" size="small">未安装</el-tag>
              <span class="name-text">{{ row.displayName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="installPath" label="安装路径" min-width="250" show-overflow-tooltip />
        <el-table-column prop="totalPlayTime" label="游戏时长" width="120" />
        <el-table-column prop="replayCount" label="Replay数" width="100" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.installed"
              type="primary" 
              size="small"
              @click="handleLaunch(row)"
              :disabled="gamesStore.isRunning"
            >
              启动
            </el-button>
            <el-button type="info" size="small" @click="viewDetails(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 游戏详情对话框 -->
    <el-dialog v-model="detailVisible" title="游戏详情" width="600px">
      <el-descriptions :column="2" border v-if="selectedGame">
        <el-descriptions-item label="游戏名称">{{ selectedGame.displayName }}</el-descriptions-item>
        <el-descriptions-item label="安装状态">
          <el-tag :type="selectedGame.installed ? 'success' : 'info'">
            {{ selectedGame.installed ? '已安装' : '未安装' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="安装路径" :span="2">{{ selectedGame.installPath || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="游戏时长">{{ selectedGame.totalPlayTime }}</el-descriptions-item>
        <el-descriptions-item label="Replay数量">{{ selectedGame.replayCount }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 倒计时对话框 -->
    <el-dialog
      v-model="countdownVisible"
      title="准备启动"
      width="300px"
      :close-on-click-modal="false"
      :show-close="false"
      center
    >
      <div class="countdown-content">
        <div class="countdown-number">{{ countdown }}</div>
        <div class="countdown-text">{{ selectedGame?.displayName }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useGamesStore } from '../stores/games'
import { ElMessage } from 'element-plus'

const gamesStore = useGamesStore()

const detailVisible = ref(false)
const countdownVisible = ref(false)
const selectedGame = ref(null)
const countdown = ref(3)

const refreshGames = () => {
  gamesStore.fetchGames()
  ElMessage.success('刷新成功')
}

const handleLaunch = (game) => {
  selectedGame.value = game
  countdownVisible.value = true
  countdown.value = 3
  
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      countdownVisible.value = false
      gamesStore.launchGame(game.id, 0)
      ElMessage.success(`正在启动 ${game.displayName}`)
    }
  }, 1000)
}

const viewDetails = (game) => {
  selectedGame.value = game
  detailVisible.value = true
}
</script>

<style scoped>
.games-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.game-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
}

.countdown-content {
  text-align: center;
  padding: 20px;
}

.countdown-number {
  font-size: 72px;
  font-weight: bold;
  color: #409EFF;
}

.countdown-text {
  font-size: 16px;
  color: #606266;
  margin-top: 10px;
}
</style>
