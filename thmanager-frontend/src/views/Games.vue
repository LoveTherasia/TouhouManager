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
        @click="router.push(`/settings?gameId=${game.id}`)"
      >
        <div class="game-card__cover">
          <CoverImage
            :src="game.coverImage"
            :alt="getGameDisplayName(game)"
            variant="banner"
            aspect-ratio="16 / 9"
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
  </AppShell>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGamesStore } from '@/stores/games'
import { getGameDisplayName, getGameShortName, getGamePlayTimeMinutes, formatPlayTime } from '@/utils/format'
import AppShell from '@/components/layout/AppShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppLoading from '@/components/ui/AppLoading.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import CoverImage from '@/components/ui/CoverImage.vue'

const router = useRouter()
const gamesStore = useGamesStore()

onMounted(() => gamesStore.fetchGames())
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
</style>
