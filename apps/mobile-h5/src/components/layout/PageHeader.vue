<template>
  <header class="page-header page-header--back">
    <button v-if="showBack" type="button" class="page-header__back" @click="onBack">返回</button>
    <div v-else class="page-header__greeting">
      <div class="page-header__hello">{{ greetingText() }}，{{ realName }}</div>
      <div v-if="subtitle" class="page-header__sub">{{ subtitle }}</div>
    </div>
    <div class="page-header__actions">
      <span v-if="showBack && title" class="page-header__title">{{ title }}</span>
      <button v-if="showLogout" type="button" class="page-header__logout" @click="logout">退出</button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { greetingText } from '@/utils/labels'

defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
  showLogout: { type: Boolean, default: true }
})

const router = useRouter()
const realName = computed(() => localStorage.getItem('realName') || '用户')

const onBack = () => router.back()

const logout = () => {
  localStorage.clear()
  router.replace('/login')
}
</script>

<style scoped>
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: var(--header-height);
  padding: 10px var(--space-md);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-header);
}
.page-header--back {
  gap: 12px;
}
.page-header__hello {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
}
.page-header__sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.page-header__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}
.page-header__back,
.page-header__logout {
  border: none;
  background: none;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 0;
  flex-shrink: 0;
}
.page-header__back { color: var(--color-primary); }
.page-header__logout { color: var(--color-text-secondary); }
.page-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
</style>
