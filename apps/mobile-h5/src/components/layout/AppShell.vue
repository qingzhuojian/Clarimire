<template>
  <div class="app-shell">
    <PageHeader
      :title="title"
      :subtitle="subtitle"
      :show-back="showBack"
      :show-logout="showLogout"
    />
    <main class="app-shell__body" :class="{ 'app-shell__body--no-tab': !showTab }">
      <slot />
    </main>
    <BottomTabBar v-if="showTab && tabs.length" :tabs="tabs" :active="activeTab" />
  </div>
</template>

<script setup>
import PageHeader from './PageHeader.vue'
import BottomTabBar from './BottomTabBar.vue'

defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
  showLogout: { type: Boolean, default: true },
  showTab: { type: Boolean, default: true },
  tabs: { type: Array, default: () => [] },
  activeTab: { type: String, default: '' }
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--color-bg-page);
}
.app-shell__body {
  padding: var(--space-md);
  padding-bottom: calc(var(--tabbar-height) + var(--space-md) + env(safe-area-inset-bottom));
}
.app-shell__body--no-tab {
  padding-bottom: var(--space-md);
}
</style>
