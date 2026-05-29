<template>
  <div class="app-empty">
    <div class="app-empty__icon">{{ icon }}</div>
    <p class="app-empty__text">{{ text }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  icon: { type: String, default: '—' }
})
</script>

<style scoped>
.app-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-5);
  color: var(--color-text-muted);
}

.app-empty__icon {
  font-size: var(--text-2xl);
  margin-bottom: var(--space-3);
  opacity: 0.4;
}

.app-empty__text {
  font-size: var(--text-sm);
}
</style>
