<template>
  <div class="app-shell">
    <aside class="app-shell__sidebar">
      <div class="app-shell__brand">
        <span class="app-shell__brand-icon">⛩</span>
        <div>
          <div class="app-shell__brand-name">ThManager</div>
          <div class="app-shell__brand-sub">东方 Project 管理器</div>
        </div>
      </div>

      <nav class="app-shell__nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="app-shell__nav-item"
          :class="{ 'app-shell__nav-item--active': isActive(item.path) }"
        >
          <span class="app-shell__nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="app-shell__footer">
        <div v-if="gamesStore.isRunning" class="app-shell__status app-shell__status--running">
          <span class="app-shell__status-dot" />
          游戏运行中
        </div>
        <div v-else class="app-shell__status">系统就绪</div>
      </div>
    </aside>

    <main class="app-shell__main">
      <header v-if="title" class="app-shell__header">
        <div>
          <h1 class="app-shell__title">{{ title }}</h1>
          <p v-if="subtitle" class="app-shell__subtitle">{{ subtitle }}</p>
        </div>
        <div v-if="$slots.headerActions" class="app-shell__header-actions">
          <slot name="headerActions" />
        </div>
      </header>
      <div class="app-shell__content">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useGamesStore } from '@/stores/games'

defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' }
})

const route = useRoute()
const gamesStore = useGamesStore()

const navItems = [
  { path: '/', icon: '◈', label: '启动台' },
  { path: '/statistics', icon: '◉', label: '数据统计' },
  { path: '/replays', icon: '◎', label: 'Replay' },
  { path: '/games', icon: '◫', label: '游戏库' },
  { path: '/settings', icon: '◌', label: '设置' }
]

const isActive = (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  background: var(--color-bg-deep);
}

.app-shell__sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-surface);
  border-right: 1px solid var(--color-border);
  padding: var(--space-5) var(--space-4);
}

.app-shell__brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-2);
  margin-bottom: var(--space-8);
}

.app-shell__brand-icon {
  font-size: 28px;
  filter: drop-shadow(0 0 8px rgba(212, 168, 83, 0.4));
}

.app-shell__brand-name {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-accent-gold);
  letter-spacing: 0.04em;
}

.app-shell__brand-sub {
  font-size: 10px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.app-shell__nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
}

.app-shell__nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
}

.app-shell__nav-item:hover {
  color: var(--color-text-secondary);
  background: var(--color-bg-hover);
}

.app-shell__nav-item--active {
  color: var(--color-text-primary);
  background: var(--color-accent-crimson-soft);
  border: 1px solid rgba(196, 30, 58, 0.25);
}

.app-shell__nav-icon {
  font-size: 14px;
  width: 18px;
  text-align: center;
  color: var(--color-accent-gold);
}

.app-shell__footer {
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.app-shell__status {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  padding: var(--space-2) var(--space-3);
}

.app-shell__status--running {
  color: var(--color-success);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.app-shell__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success);
  animation: pulse 1.5s ease infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.app-shell__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--space-6) var(--space-8) var(--space-4);
  flex-shrink: 0;
}

.app-shell__title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: 0.02em;
}

.app-shell__subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: var(--space-1);
}

.app-shell__header-actions {
  display: flex;
  gap: var(--space-2);
}

.app-shell__content {
  flex: 1;
  overflow-y: auto;
  padding: 0 var(--space-8) var(--space-8);
}
</style>
