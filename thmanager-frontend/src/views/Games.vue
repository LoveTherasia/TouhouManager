<template>
  <AppShell title="游戏库" subtitle="已收录的东方 Project 作品">
    <template #headerActions>
      <AppButton variant="ghost" size="sm" :loading="gamesStore.loading" @click="gamesStore.fetchGames()">
        刷新
      </AppButton>
    </template>

    <AppLoading v-if="gamesStore.loading && !gamesStore.games.length" />
    <AppEmpty v-else-if="!gamesStore.games.length" text="暂无游戏数据" />

    <div v-else class="games-grid">
      <div
        v-for="game in gamesStore.games"
        :key="game.id"
        class="game-card"
        @contextmenu.prevent="showContextMenu($event, game)"
      >
        <div class="game-card__cover">
          <CoverImage
            :src="getGameCoverUrl(game)"
            :alt="getGameDisplayName(game)"
            variant="banner"
            aspect-ratio="1 / 1"
            interactive
          />
          <AppBadge :variant="game.installed ? 'success' : 'default'" class="game-card__badge">
            {{ game.installed ? '已安装' : '未安装' }}
          </AppBadge>
        </div>
        <div class="game-card__info">
          <div class="game-card__name">{{ getGameDisplayName(game) }}</div>
          <div class="game-card__meta">
            <span>{{ getGameShortName(game).toUpperCase() }}</span>
            <span>{{ formatPlayTime(getGamePlayTimeMinutes(game)) }}</span>
          </div>
          <div v-if="game.installPath" class="game-card__path">{{ game.installPath }}</div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <div class="context-menu__item" @click="openPathModal">
          <span class="context-menu__icon">📁</span>
          <span>设置安装路径</span>
        </div>
        <div v-if="contextMenu.game?.installPath" class="context-menu__item context-menu__item--danger" @click="handleClearPath">
          <span class="context-menu__icon">🗑️</span>
          <span>清除安装路径</span>
        </div>
      </div>
    </Teleport>

    <!-- 设置路径弹窗 -->
    <AppModal v-model="pathModalVisible" :title="`设置路径 - ${pathModalGame?.displayName || pathModalGame?.titleCn || ''}`">
      <div class="path-modal-content">
        <p class="path-modal-desc">请输入游戏的安装目录完整路径</p>
        <input
          ref="pathInput"
          v-model="pathInputValue"
          type="text"
          class="path-modal-input"
          placeholder="例如: D:\Games\Touhou\th06"
          @keyup.enter="confirmSetPath"
        />
        <div class="path-modal-actions">
          <AppButton variant="ghost" @click="pathModalVisible = false">取消</AppButton>
          <AppButton :loading="settingPath" @click="confirmSetPath">确认</AppButton>
        </div>
      </div>
    </AppModal>
  </AppShell>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useGamesStore } from '@/stores/games'
import { getGameDisplayName, getGameShortName, getGamePlayTimeMinutes, getGameCoverUrl, formatPlayTime } from '@/utils/format'
import AppShell from '@/components/layout/AppShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppLoading from '@/components/ui/AppLoading.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import AppModal from '@/components/ui/AppModal.vue'
import CoverImage from '@/components/ui/CoverImage.vue'

const gamesStore = useGamesStore()

const contextMenu = ref({ visible: false, x: 0, y: 0, game: null })
const pathModalVisible = ref(false)
const pathModalGame = ref(null)
const pathInputValue = ref('')
const pathInput = ref(null)
const settingPath = ref(false)

const showContextMenu = (event, game) => {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    game
  }
}

const hideContextMenu = () => {
  contextMenu.value.visible = false
}

const openPathModal = () => {
  hideContextMenu()
  pathModalGame.value = contextMenu.value.game
  pathInputValue.value = contextMenu.value.game?.installPath || ''
  pathModalVisible.value = true
  nextTick(() => {
    pathInput.value?.focus()
  })
}

const confirmSetPath = async () => {
  if (!pathModalGame.value) return
  
  settingPath.value = true
  try {
    await gamesStore.updateGamePath(pathModalGame.value.id, pathInputValue.value.trim())
    await gamesStore.fetchGames()
    pathModalVisible.value = false
  } catch (error) {
    console.error('Failed to update game path:', error)
  } finally {
    settingPath.value = false
  }
}

const handleClearPath = async () => {
  hideContextMenu()
  if (!contextMenu.value.game) return

  try {
    await gamesStore.updateGamePath(contextMenu.value.game.id, '')
    await gamesStore.fetchGames()
  } catch (error) {
    console.error('Failed to clear game path:', error)
  }
}

onMounted(() => {
  gamesStore.fetchGames()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped>
.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-5);
}

.game-card {
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
}
.game-card:hover {
  border-color: var(--color-border-active);
  transform: translateY(-2px);
  box-shadow: var(--shadow-panel);
}

.game-card__cover {
  position: relative;
}
.game-card__cover :deep(.cover-image) {
  border-radius: 0;
}

.game-card__badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  z-index: 4;
}

.game-card__info { padding: var(--space-4); }

.game-card__name {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  margin-bottom: var(--space-2);
}

.game-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
  margin-bottom: var(--space-2);
}

.game-card__path {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Context Menu */
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 180px;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-panel);
  padding: var(--space-2);
  animation: menuFadeIn 0.15s ease;
}

@keyframes menuFadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.context-menu__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  transition: background var(--transition-fast);
}

.context-menu__item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.context-menu__item--danger {
  color: var(--color-accent-crimson);
}

.context-menu__item--danger:hover {
  background: rgba(196, 30, 58, 0.12);
}

.context-menu__icon {
  font-size: 16px;
  line-height: 1;
}

/* Path Modal */
.path-modal-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.path-modal-desc {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.path-modal-input {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--text-base);
  font-family: var(--font-mono);
  outline: none;
  transition: border-color var(--transition-fast);
}

.path-modal-input:focus {
  border-color: var(--color-border-active);
}

.path-modal-input::placeholder {
  color: var(--color-text-muted);
}

.path-modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-2);
}
</style>
