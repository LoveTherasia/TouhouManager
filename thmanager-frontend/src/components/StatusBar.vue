<template>
  <div class="status-bar">
    <el-tag v-if="gamesStore.isRunning" type="success" effect="dark">
      <el-icon class="is-loading"><Loading /></el-icon>
      游戏运行中
    </el-tag>
    <el-tag v-else type="info">就绪</el-tag>
    <span class="status-text">{{ statusMessage }}</span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useGamesStore } from '../stores/games'

const gamesStore = useGamesStore()
const statusMessage = ref('系统就绪')
let statusCheckInterval

onMounted(() => {
  // 定期检查游戏状态
  statusCheckInterval = setInterval(() => {
    gamesStore.checkStatus()
  }, 5000)
})

onUnmounted(() => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval)
  }
})
</script>

<style scoped>
.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-text {
  font-size: 14px;
  color: #606266;
}
</style>
