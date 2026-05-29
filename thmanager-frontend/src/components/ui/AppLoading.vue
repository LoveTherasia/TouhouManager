<template>
  <div class="app-loading">
    <div class="app-loading__ring" />
    <span v-if="text" class="app-loading__text">{{ text }}</span>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '加载中...' }
})
</script>

<style scoped>
.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-10);
  color: var(--color-text-muted);
}

.app-loading__ring {
  width: 32px;
  height: 32px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-accent-gold);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.app-loading__text {
  font-size: var(--text-sm);
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
