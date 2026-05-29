<template>
  <div :class="['app-card', { 'app-card--hover': hover }]">
    <div v-if="$slots.header || title" class="app-card__header">
      <slot name="header">
        <span class="app-card__title">{{ title }}</span>
      </slot>
      <div v-if="$slots.actions" class="app-card__actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="app-card__body" :class="{ 'app-card__body--flush': flush }">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  hover: { type: Boolean, default: false },
  flush: { type: Boolean, default: false }
})
</script>

<style scoped>
.app-card {
  background: var(--color-bg-glass);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(16px);
  overflow: hidden;
}

.app-card--hover {
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
}
.app-card--hover:hover {
  border-color: var(--color-border-active);
  box-shadow: var(--shadow-panel);
}

.app-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.02);
}

.app-card__title {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-primary);
}

.app-card__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.app-card__body {
  padding: var(--space-5);
}

.app-card__body--flush {
  padding: 0;
}
</style>
