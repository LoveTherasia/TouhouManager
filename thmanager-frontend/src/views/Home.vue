<template>
  <div class="launcher">
    <!-- 背景 -->
    <Transition name="bg-fade" mode="out-in">
      <div
        v-if="selectedGame"
        :key="selectedGame.id"
        class="launcher__bg"
        :style="{ backgroundImage: `url('${selectedGame.coverImage}')` }"
      />
    </Transition>
    <div class="launcher__vignette" />

    <!-- 顶部栏 -->
    <header class="launcher__topbar">
      <div class="launcher__brand">
        <span class="launcher__brand-mark">⛩</span>
        <div>
          <span class="launcher__brand-text">ThManager</span>
          <span class="launcher__brand-sub">Game Library</span>
        </div>
      </div>

      <div class="launcher__topbar-actions">
        <button
          v-if="selectedGame"
          :class="['launcher__music-btn', { 'launcher__music-btn--playing': isPlaying }]"
          title="背景音乐"
          @click="toggleMusic"
        >
          ♪
        </button>
        <router-link to="/statistics" class="launcher__nav-link">数据统计</router-link>
        <router-link to="/settings" class="launcher__nav-link">设置</router-link>
        <button class="launcher__avatar" @click="handleAvatarClick">
          <img v-if="userStore.user?.avatarUrl" :src="userStore.user.avatarUrl" alt="" />
          <span v-else class="launcher__avatar-placeholder" />
        </button>
      </div>
    </header>

    <div class="launcher__body">
      <!-- 游戏库侧栏 -->
      <aside class="launcher__library">
        <div class="launcher__library-header">
          <span class="launcher__library-title">游戏库</span>
          <span class="launcher__library-count">{{ gamesStore.games.length }}</span>
        </div>

        <div class="launcher__library-list">
          <div
            v-for="game in gamesStore.games"
            :key="game.id"
            :class="['library-card', { 'library-card--active': selectedGame?.id === game.id }]"
            @click="selectGame(game)"
          >
            <div class="library-card__cover">
              <CoverImage
                :src="game.coverImage"
                :alt="getGameDisplayName(game)"
                variant="card"
                interactive
              />
              <span v-if="!game.installed" class="library-card__badge">未安装</span>
            </div>
            <div class="library-card__info">
              <span class="library-card__name">{{ getGameDisplayName(game) }}</span>
              <span class="library-card__meta">
                {{ getGameShortName(game).toUpperCase() }}
                ·
                {{ formatPlayTime(getGamePlayTimeMinutes(game)) }}
              </span>
            </div>
          </div>
        </div>
      </aside>

      <!-- 主展示区 -->
      <main v-if="selectedGame" class="launcher__hero">
        <div class="launcher__hero-content">
          <div class="launcher__hero-meta">
            <AppBadge :variant="selectedGame.installed ? 'success' : 'warning'">
              {{ selectedGame.installed ? '已安装' : '未安装' }}
            </AppBadge>
            <span class="launcher__hero-version">{{ getGameShortName(selectedGame).toUpperCase() }}</span>
          </div>

          <h1 class="launcher__hero-title">{{ getGameDisplayName(selectedGame) }}</h1>
          <p class="launcher__hero-desc">{{ selectedGame.description || '东方 Project 系列作品' }}</p>

          <div class="launcher__hero-stats">
            <div class="launcher__stat">
              <span class="launcher__stat-value">{{ formatPlayTime(getGamePlayTimeMinutes(selectedGame)) }}</span>
              <span class="launcher__stat-label">游玩时长</span>
            </div>
          </div>

          <div class="launcher__hero-actions">
            <AppButton size="lg" :disabled="gamesStore.isRunning" @click="handleLaunch">
              {{ selectedGame.installed ? '启动游戏' : '配置安装路径' }}
            </AppButton>
            <AppButton variant="ghost" size="lg" @click="navigateToSettings">游戏设置</AppButton>
          </div>

          <p v-if="!selectedGame.installed" class="launcher__hint">
            尚未检测到安装路径，点击启动按钮前往设置
          </p>
        </div>

        <div class="launcher__hero-visual">
          <CoverImage
            :src="selectedGame.coverImage"
            :alt="getGameDisplayName(selectedGame)"
            variant="hero"
          />
          <div class="launcher__hero-frame" />
        </div>
      </main>

      <div v-else class="launcher__empty">
        <AppLoading text="正在加载游戏列表..." />
      </div>

      <!-- 扩展功能预留区 -->
      <aside class="launcher__features">
        <div class="launcher__features-header">
          <span class="launcher__features-title">扩展</span>
        </div>

        <div class="feature-slots">
          <div
            v-for="slot in featureSlots"
            :key="slot.key"
            class="feature-slot feature-slot--placeholder"
            :title="slot.comingSoon ? '功能开发中' : slot.label"
          >
            <span class="feature-slot__icon">{{ slot.icon }}</span>
            <div class="feature-slot__body">
              <span class="feature-slot__name">{{ slot.label }}</span>
              <span class="feature-slot__desc">{{ slot.desc }}</span>
            </div>
            <span v-if="slot.comingSoon" class="feature-slot__soon">Soon</span>
          </div>
        </div>
      </aside>
    </div>

    <audio
      ref="audioPlayer"
      @play="isPlaying = true"
      @pause="isPlaying = false"
      @ended="isPlaying = false"
    />

    <Teleport to="body">
      <Transition name="countdown">
        <div v-if="countdownVisible" class="launcher__countdown">
          <div class="launcher__countdown-num">{{ countdown }}</div>
          <div class="launcher__countdown-label">秒后启动游戏</div>
        </div>
      </Transition>
    </Teleport>

    <AuthModal v-model="showAuthModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGamesStore } from '@/stores/games'
import { useUserStore } from '@/stores/user'
import { useGameMusic } from '@/composables/useGameMusic'
import { formatPlayTime, getGameDisplayName, getGameShortName, getGamePlayTimeMinutes } from '@/utils/format'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppLoading from '@/components/ui/AppLoading.vue'
import CoverImage from '@/components/ui/CoverImage.vue'
import AuthModal from '@/components/auth/AuthModal.vue'

const router = useRouter()
const gamesStore = useGamesStore()
const userStore = useUserStore()
const { audioPlayer, isPlaying, playGameMusic, toggleMusic, pauseMusic, resumeMusic } = useGameMusic()

const selectedGame = ref(null)
const countdownVisible = ref(false)
const countdown = ref(3)
const showAuthModal = ref(false)
let statusInterval = null
let wasRunning = false

/** 右侧扩展功能预留位，后续可替换为真实路由/组件 */
const featureSlots = [
  { key: 'achievements', icon: '🏆', label: '成就', desc: '通关与挑战记录', comingSoon: true },
  { key: 'collection', icon: '◈', label: '收藏', desc: '喜爱的作品与 Replay', comingSoon: true },
  { key: 'recent', icon: '◎', label: '最近游玩', desc: '快速回到上次进度', comingSoon: true }
]

const selectGame = (game) => {
  selectedGame.value = game
  playGameMusic(game)
}

const handleLaunch = async () => {
  if (!selectedGame.value) return
  if (!selectedGame.value.installed) {
    navigateToSettings()
    return
  }
  pauseMusic()
  countdownVisible.value = true
  countdown.value = 3
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      countdownVisible.value = false
      gamesStore.launchGame(selectedGame.value.id, 0)
    }
  }, 1000)
}

const navigateToSettings = () => {
  const id = selectedGame.value?.id
  router.push(id ? `/settings?gameId=${id}` : '/settings')
}

const handleAvatarClick = () => {
  if (userStore.isLoggedIn()) {
    router.push('/user/edit')
  } else {
    showAuthModal.value = true
  }
}

onMounted(async () => {
  await gamesStore.fetchGames()
  if (gamesStore.games.length > 0) {
    selectGame(gamesStore.games[0])
  }

  statusInterval = setInterval(async () => {
    await gamesStore.checkStatus()
    if (wasRunning && !gamesStore.isRunning && selectedGame.value) {
      resumeMusic()
    }
    wasRunning = gamesStore.isRunning
  }, 2000)
})

onUnmounted(() => {
  if (statusInterval) clearInterval(statusInterval)
})
</script>

<style scoped>
.launcher {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--color-bg-deep);
}

.launcher__bg {
  position: absolute;
  inset: -40px;
  background-size: cover;
  background-position: center;
  filter: blur(32px) brightness(0.35) saturate(1.2);
  z-index: 0;
}

.launcher__vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 35% 50%, transparent 0%, rgba(7, 8, 13, 0.88) 72%);
  z-index: 1;
  pointer-events: none;
}

.bg-fade-enter-active,
.bg-fade-leave-active { transition: opacity 0.6s ease; }
.bg-fade-enter-from,
.bg-fade-leave-to { opacity: 0; }

/* Topbar */
.launcher__topbar {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border);
  background: rgba(0, 0, 0, 0.2);
}

.launcher__brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.launcher__brand-mark {
  font-size: 22px;
  filter: drop-shadow(0 0 6px rgba(212, 168, 83, 0.5));
}

.launcher__brand-text {
  display: block;
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-accent-gold);
  letter-spacing: 0.06em;
  line-height: 1.2;
}

.launcher__brand-sub {
  display: block;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.launcher__topbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.launcher__music-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  color: var(--color-accent-gold);
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}
.launcher__music-btn:hover { border-color: var(--color-border-active); }
.launcher__music-btn--playing { animation: spin 3s linear infinite; }

.launcher__nav-link {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  transition: color var(--transition-fast);
}
.launcher__nav-link:hover { color: var(--color-text-primary); }

.launcher__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--color-border-strong);
  transition: border-color var(--transition-fast);
}
.launcher__avatar:hover { border-color: var(--color-border-active); }
.launcher__avatar img { width: 100%; height: 100%; object-fit: cover; }
.launcher__avatar-placeholder {
  display: block;
  width: 100%;
  height: 100%;
  background: var(--color-bg-elevated);
}

/* Body: library | hero | features */
.launcher__body {
  position: relative;
  z-index: 5;
  display: flex;
  height: calc(100vh - 65px);
}

/* ── Game Library ── */
.launcher__library {
  width: calc(var(--library-width) + var(--library-thumb-width) + var(--space-8));
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-border);
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(12px);
}

.launcher__library-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.launcher__library-title {
  font-family: var(--font-display);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.08em;
}

.launcher__library-count {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.launcher__library-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.library-card {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  cursor: pointer;
  opacity: 0.6;
  transition: all var(--transition-base);
}
.library-card:hover {
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--color-border);
}
.library-card--active {
  opacity: 1;
  background: var(--color-accent-gold-soft);
  border-color: rgba(212, 168, 83, 0.3);
}

.library-card__cover {
  position: relative;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
}
.library-card--active .library-card__cover {
  border-color: var(--color-accent-gold);
  box-shadow: var(--shadow-glow-gold);
}

.library-card__badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 4;
  font-size: 10px;
  text-align: center;
  background: rgba(0, 0, 0, 0.75);
  color: var(--color-warning);
  padding: 3px 0;
}

.library-card__info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-2);
  min-width: 0;
  padding: var(--space-1) 0;
}

.library-card__name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.library-card--active .library-card__name { color: var(--color-accent-gold); }

.library-card__meta {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* ── Hero ── */
.launcher__hero {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-6) var(--space-8);
  gap: var(--space-6);
}

.launcher__hero-content {
  flex: 1;
  max-width: 480px;
}

.launcher__hero-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.launcher__hero-version {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
}

.launcher__hero-title {
  font-family: var(--font-display);
  font-size: var(--text-hero);
  font-weight: 700;
  line-height: 1.15;
  margin-bottom: var(--space-3);
  background: linear-gradient(135deg, var(--color-text-primary) 0%, var(--color-accent-gold) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.launcher__hero-desc {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin-bottom: var(--space-6);
}

.launcher__hero-stats {
  display: flex;
  gap: var(--space-8);
  margin-bottom: var(--space-6);
  padding: var(--space-4) 0;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}

.launcher__stat-value {
  display: block;
  font-family: var(--font-mono);
  font-size: var(--text-xl);
  color: var(--color-accent-gold);
  font-weight: 600;
}

.launcher__stat-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin-top: var(--space-1);
}

.launcher__hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.launcher__hint {
  margin-top: var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.launcher__hero-visual {
  position: relative;
  flex-shrink: 0;
}

.launcher__hero-frame {
  position: absolute;
  inset: -8px;
  border: 1px solid var(--color-border-active);
  border-radius: calc(var(--radius-lg) + 4px);
  z-index: 0;
  pointer-events: none;
}

.launcher__empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Feature panel (achievements etc.) ── */
.launcher__features {
  width: var(--feature-panel-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--color-border);
  background: rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(12px);
}

.launcher__features-header {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.launcher__features-title {
  font-family: var(--font-display);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.08em;
}

.feature-slots {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.feature-slot {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  border: 1px dashed var(--color-border);
  background: rgba(255, 255, 255, 0.02);
  transition: border-color var(--transition-base);
}

.feature-slot--placeholder {
  cursor: default;
  opacity: 0.7;
}
.feature-slot--placeholder:hover {
  border-color: var(--color-border-strong);
  opacity: 0.85;
}

.feature-slot__icon {
  font-size: 20px;
  line-height: 1;
  flex-shrink: 0;
  margin-top: 2px;
}

.feature-slot__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.feature-slot__name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.feature-slot__desc {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  line-height: 1.4;
}

.feature-slot__soon {
  flex-shrink: 0;
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.06);
  padding: 3px 7px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
}

/* Countdown */
.launcher__countdown {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.82);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.launcher__countdown-num {
  font-family: var(--font-display);
  font-size: 120px;
  font-weight: 700;
  color: var(--color-accent-crimson);
  line-height: 1;
  text-shadow: var(--shadow-glow-crimson);
}

.launcher__countdown-label {
  font-size: var(--text-lg);
  color: var(--color-text-secondary);
  margin-top: var(--space-4);
}

.countdown-enter-active,
.countdown-leave-active { transition: opacity 0.3s ease; }
.countdown-enter-from,
.countdown-leave-to { opacity: 0; }

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 1200px) {
  .launcher__features { display: none; }
  .launcher__library {
    width: calc(var(--library-thumb-width) + var(--space-8));
  }
  .library-card__info { display: none; }
}

@media (max-width: 768px) {
  .launcher__body { flex-direction: column; height: auto; overflow-y: auto; }
  .launcher__library {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--color-border);
  }
  .launcher__library-list {
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
  }
  .library-card { flex-direction: column; width: var(--library-thumb-width); flex-shrink: 0; }
  .library-card__info { display: flex; }
  .launcher__hero {
    flex-direction: column-reverse;
    padding: var(--space-5);
  }
}
</style>
