<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="app-modal-overlay" @click.self="close">
        <div class="app-modal" :style="{ maxWidth: width }">
          <button v-if="closable" class="app-modal__close" @click="close">&times;</button>
          <div v-if="title" class="app-modal__header">
            <h2 class="app-modal__title">{{ title }}</h2>
          </div>
          <div class="app-modal__body">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '440px' },
  closable: { type: Boolean, default: true }
})
const emit = defineEmits(['update:modelValue', 'close'])

const close = () => {
  emit('update:modelValue', false)
  emit('close')
}
</script>

<style scoped>
.app-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.72);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: var(--space-5);
}

.app-modal {
  position: relative;
  width: 100%;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-panel);
  padding: var(--space-8);
}

.app-modal__close {
  position: absolute;
  top: var(--space-4);
  right: var(--space-5);
  font-size: 24px;
  color: var(--color-text-muted);
  line-height: 1;
  transition: color var(--transition-fast);
}
.app-modal__close:hover { color: var(--color-text-primary); }

.app-modal__header {
  margin-bottom: var(--space-6);
}

.app-modal__title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 600;
  text-align: center;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--transition-base);
}
.modal-enter-active .app-modal,
.modal-leave-active .app-modal {
  transition: transform var(--transition-base), opacity var(--transition-base);
}
.modal-enter-from,
.modal-leave-to { opacity: 0; }
.modal-enter-from .app-modal,
.modal-leave-to .app-modal {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}
</style>
