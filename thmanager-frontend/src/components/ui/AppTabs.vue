<template>
  <div class="app-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      :class="['app-tabs__item', { 'app-tabs__item--active': modelValue === tab.key }]"
      @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  tabs: { type: Array, required: true },
  modelValue: { type: String, required: true }
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.app-tabs {
  display: inline-flex;
  gap: 2px;
  padding: 3px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.app-tabs__item {
  padding: 8px 20px;
  border-radius: calc(var(--radius-md) - 2px);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
}

.app-tabs__item:hover {
  color: var(--color-text-secondary);
}

.app-tabs__item--active {
  background: var(--color-accent-crimson);
  color: #fff;
  box-shadow: 0 2px 12px rgba(196, 30, 58, 0.35);
}
</style>
