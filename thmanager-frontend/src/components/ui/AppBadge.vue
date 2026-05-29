<template>
  <span :class="['app-badge', `app-badge--${variant}`]">
    <slot />
  </span>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'default' }
})
</script>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.app-badge--default {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
}

.app-badge--success {
  background: rgba(74, 222, 128, 0.12);
  color: var(--color-success);
  border: 1px solid rgba(74, 222, 128, 0.25);
}

.app-badge--warning {
  background: rgba(251, 191, 36, 0.12);
  color: var(--color-warning);
  border: 1px solid rgba(251, 191, 36, 0.25);
}

.app-badge--gold {
  background: var(--color-accent-gold-soft);
  color: var(--color-accent-gold);
  border: 1px solid rgba(212, 168, 83, 0.3);
}
</style>
