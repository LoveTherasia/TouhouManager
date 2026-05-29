<template>
  <div
    :class="['cover-image', `cover-image--${variant}`, { 'cover-image--interactive': interactive }]"
    :style="frameStyle"
  >
    <!-- 背景层：裁切放大 + 模糊，填满固定比例框 -->
    <img
      v-if="resolvedSrc"
      class="cover-image__bg"
      :src="resolvedSrc"
      alt=""
      aria-hidden="true"
      draggable="false"
      @error="onError"
    />

    <!-- 前景层：完整显示原图，不裁切 -->
    <img
      v-if="resolvedSrc"
      class="cover-image__fg"
      :src="resolvedSrc"
      :alt="alt"
      draggable="false"
      @error="onError"
    />

    <!-- 边缘渐隐，避免前景与背景接缝感 -->
    <div class="cover-image__vignette" aria-hidden="true" />

    <!-- 缺省占位 -->
    <div v-if="!resolvedSrc || errored" class="cover-image__fallback">
      <span>{{ fallbackText }}</span>
    </div>

    <slot />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  /** 固定外框比例，原图任意比例都在框内完整展示 */
  aspectRatio: { type: String, default: '3 / 4' },
  /** card=游戏库缩略图(1:1) hero=详情大图 banner=宽图 */
  variant: { type: String, default: 'card' },
  /** 允许 hover 时前景轻微放大 */
  interactive: { type: Boolean, default: false },
  fallback: { type: String, default: '/image/avatar/default.svg' },
  fallbackText: { type: String, default: 'No Cover' },
  width: { type: String, default: '' },
  height: { type: String, default: '' }
})

const errored = ref(false)
const resolvedSrc = computed(() => (errored.value ? props.fallback : props.src) || props.fallback)

const frameStyle = computed(() => {
  const ratioByVariant = {
    card: '1 / 1',
    hero: '3 / 4',
    banner: '16 / 9',
    fill: props.aspectRatio
  }
  const style = { aspectRatio: ratioByVariant[props.variant] || props.aspectRatio }
  if (props.width) style.width = props.width
  if (props.height) style.height = props.height
  return style
})

watch(() => props.src, () => { errored.value = false })

const onError = () => { errored.value = true }
</script>

<style scoped>
.cover-image {
  position: relative;
  overflow: hidden;
  border-radius: inherit;
  background: var(--color-bg-elevated, #161824);
  isolation: isolate;
}

/* ── 背景层：cover + blur，负责「填框」 ── */
.cover-image__bg {
  position: absolute;
  inset: -12%;
  width: 124%;
  height: 124%;
  object-fit: cover;
  object-position: center;
  filter: blur(20px) saturate(1.15) brightness(0.55);
  transform: scale(1.08);
  pointer-events: none;
  user-select: none;
}

/* ── 前景层：contain，负责「完整展示」 ── */
.cover-image__fg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  z-index: 1;
  pointer-events: none;
  user-select: none;
  transition: transform var(--transition-slow, 450ms cubic-bezier(0.16, 1, 0.3, 1));
}

.cover-image--interactive:hover .cover-image__fg {
  transform: scale(1.03);
}

/* 边缘暗角，让 letterbox 区域更自然 */
.cover-image__vignette {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.18) 0%, transparent 22%),
    linear-gradient(to top, rgba(0, 0, 0, 0.22) 0%, transparent 28%);
}

.cover-image__fallback {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.03);
  color: var(--color-text-muted, rgba(240, 236, 228, 0.38));
  font-size: var(--text-xs, 0.75rem);
}

/* ── 尺寸变体 ── */
.cover-image--card {
  width: var(--library-thumb-size, 132px);
  border-radius: var(--radius-md, 10px);
}

.cover-image--hero {
  width: clamp(220px, 26vw, 360px);
  aspect-ratio: 1 / 1;
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-panel, 0 8px 32px rgba(0, 0, 0, 0.45));
}

.cover-image--banner {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-md, 10px);
}

.cover-image--fill {
  width: 100%;
  height: 100%;
}
</style>
