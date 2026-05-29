<template>
  <router-view v-slot="{ Component, route }">
    <Transition :name="route.meta.transition || 'page'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </router-view>
</template>

<script setup>
</script>
