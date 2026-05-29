<template>
  <button
    :class="['app-btn', `app-btn--${variant}`, `app-btn--${size}`, { 'app-btn--block': block, 'app-btn--loading': loading }]"
    :disabled="disabled || loading"
    :type="type"
  >
    <span v-if="loading" class="app-btn__spinner" />
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  block: { type: Boolean, default: false },
  type: { type: String, default: 'button' }
})
</script>

<style scoped>
.app-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  white-space: nowrap;
  position: relative;
}

.app-btn--sm { padding: 6px 14px; font-size: var(--text-sm); }
.app-btn--md { padding: 10px 20px; font-size: var(--text-sm); }
.app-btn--lg { padding: 14px 28px; font-size: var(--text-base); }
.app-btn--block { width: 100%; }

.app-btn--primary {
  background: linear-gradient(135deg, var(--color-accent-crimson) 0%, #8b1538 100%);
  color: #fff;
  box-shadow: var(--shadow-glow-crimson);
}
.app-btn--primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 0 56px rgba(196, 30, 58, 0.35);
}

.app-btn--secondary {
  background: var(--color-bg-elevated);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-strong);
}
.app-btn--secondary:hover:not(:disabled) {
  background: var(--color-bg-hover);
  border-color: var(--color-border-active);
}

.app-btn--ghost {
  background: transparent;
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
}
.app-btn--ghost:hover:not(:disabled) {
  color: var(--color-text-primary);
  border-color: var(--color-border-strong);
  background: var(--color-bg-hover);
}

.app-btn--gold {
  background: linear-gradient(135deg, var(--color-accent-gold) 0%, #a07830 100%);
  color: #1a1208;
  box-shadow: var(--shadow-glow-gold);
}

.app-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none !important;
}

.app-btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
