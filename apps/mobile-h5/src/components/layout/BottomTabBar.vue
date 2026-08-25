<template>
  <nav class="bottom-tabbar">
    <router-link
      v-for="item in tabs"
      :key="item.key"
      :to="item.to"
      class="bottom-tabbar__item"
      :class="{ 'bottom-tabbar__item--active': active === item.key }"
    >
      <span class="bottom-tabbar__icon" v-html="item.icon" />
      <span class="bottom-tabbar__label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
defineProps({
  tabs: { type: Array, required: true },
  active: { type: String, required: true }
})
</script>

<style scoped>
.bottom-tabbar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  display: flex;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  padding: 6px 0 calc(6px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 12px rgba(37, 99, 235, 0.06);
  z-index: 200;
}
.bottom-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-decoration: none;
  color: var(--color-text-muted);
  padding: 4px 0;
  transition: color 0.2s;
}
.bottom-tabbar__item--active {
  color: var(--color-primary);
}
.bottom-tabbar__item--active .bottom-tabbar__icon {
  background: var(--color-primary-light);
}
.bottom-tabbar__icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
}
.bottom-tabbar__label {
  font-size: 11px;
  font-weight: 500;
}
.bottom-tabbar__item--active .bottom-tabbar__label {
  font-weight: 600;
}
</style>
