<template>
  <div class="app-input-wrap">
    <label v-if="label" class="app-input__label">{{ label }}</label>
    <input
      :class="['app-input', { 'app-input--error': error }]"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <span v-if="error" class="app-input__error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  error: { type: String, default: '' }
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.app-input-wrap {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.app-input__label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  font-weight: 500;
}

.app-input {
  width: 100%;
  padding: 11px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--text-sm);
  outline: none;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.app-input::placeholder {
  color: var(--color-text-muted);
}

.app-input:focus {
  border-color: var(--color-border-active);
  background: rgba(255, 255, 255, 0.06);
}

.app-input--error {
  border-color: var(--color-accent-crimson);
}

.app-input__error {
  font-size: var(--text-xs);
  color: var(--color-accent-crimson);
}
</style>
