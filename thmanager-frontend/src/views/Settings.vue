<template>
  <AppShell title="游戏设置" subtitle="配置各作品的安装路径">
    <template #headerActions>
      <AppButton variant="ghost" size="sm" @click="clearAllPaths">清空所有路径</AppButton>
      <AppButton size="sm" :loading="saving" @click="saveSettings">保存设置</AppButton>
    </template>

    <div class="settings-notice">
      <span class="settings-notice__icon">ℹ</span>
      <p>设置安装路径后，系统将自动检测游戏是否已安装并扫描 Replay 文件。</p>
    </div>

    <AppLoading v-if="loading" />
    <div v-else class="settings-list">
      <div v-for="game in gamesStore.games" :key="game.id" class="settings-item">
        <div class="settings-item__header">
          <div>
            <div class="settings-item__name">{{ getGameDisplayName(game) }}</div>
            <div class="settings-item__version">{{ getGameShortName(game).toUpperCase() }}</div>
          </div>
          <AppBadge :variant="game.installed ? 'success' : 'default'">
            {{ game.installed ? '已安装' : '未安装' }}
          </AppBadge>
        </div>
        <div class="settings-item__path">
          <input
            v-model="gamePaths[game.id]"
            class="path-input"
            placeholder="请输入游戏安装路径，例如 D:\Games\Touhou\th06"
          />
          <AppButton variant="secondary" size="sm" @click="browsePath(game.id)">浏览</AppButton>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useGamesStore } from '@/stores/games'
import { getGameDisplayName, getGameShortName } from '@/utils/format'
import AppShell from '@/components/layout/AppShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppLoading from '@/components/ui/AppLoading.vue'

const gamesStore = useGamesStore()
const loading = ref(false)
const saving = ref(false)
const gamePaths = ref({})

const loadGamePaths = () => {
  gamesStore.games.forEach(game => {
    gamePaths.value[game.id] = game.installPath || ''
  })
}

const saveSettings = async () => {
  saving.value = true
  try {
    for (const [gameId, path] of Object.entries(gamePaths.value)) {
      await gamesStore.updateGamePath(parseInt(gameId), typeof path === 'string' ? path.trim() : '')
    }
    await gamesStore.fetchGames()
    loadGamePaths()
    alert('设置保存成功')
  } catch {
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const browsePath = (gameId) => {
  const path = prompt('请输入游戏安装路径:')
  if (path) gamePaths.value[gameId] = path
}

const clearAllPaths = async () => {
  if (!confirm('确定要清空所有游戏的路径吗？')) return
  loading.value = true
  try {
    await gamesStore.clearAllGamePaths()
    loadGamePaths()
  } catch {
    alert('清空失败，请重试')
  } finally {
    loading.value = false
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
.settings-notice {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: var(--color-accent-gold-soft);
  border: 1px solid rgba(212, 168, 83, 0.25);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-6);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.settings-notice__icon {
  color: var(--color-accent-gold);
  font-weight: 700;
  flex-shrink: 0;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.settings-item {
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  transition: border-color var(--transition-base);
}
.settings-item:hover { border-color: var(--color-border-strong); }

.settings-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.settings-item__name {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
}

.settings-item__version {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin-top: 2px;
}

.settings-item__path {
  display: flex;
  gap: var(--space-3);
}

.path-input {
  flex: 1;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--text-sm);
  font-family: var(--font-mono);
  outline: none;
  transition: border-color var(--transition-fast);
}
.path-input:focus { border-color: var(--color-border-active); }
</style>
