<template>
  <AppShell subtitle="个人中心" :tabs="PUBLIC_TABS" active-tab="mine">
    <section class="section-card profile-card">
      <div class="avatar">{{ avatarText }}</div>
      <div>
        <div class="profile-name">{{ realName }}</div>
        <div class="profile-meta">群众 · {{ username }}</div>
      </div>
    </section>

    <section class="section-card menu-card">
      <button type="button" class="menu-item" @click="$router.push('/public/report')">
        <span>我要上报</span><span class="menu-item__arrow">›</span>
      </button>
      <button type="button" class="menu-item" @click="$router.push('/public/reports')">
        <span>我的上报</span><span class="menu-item__arrow">›</span>
      </button>
    </section>

    <button class="btn btn-secondary" @click="logout">退出登录</button>
  </AppShell>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '@/components/layout/AppShell.vue'
import { PUBLIC_TABS } from '@/config/tabs'

const router = useRouter()
const realName = computed(() => localStorage.getItem('realName') || '群众')
const username = computed(() => localStorage.getItem('username') || '-')
const avatarText = computed(() => (realName.value || '群').slice(0, 1))

const logout = () => {
  localStorage.clear()
  router.replace('/login')
}
</script>

<style scoped>
.profile-card { display: flex; align-items: center; gap: 14px; }
.avatar {
  width: 52px; height: 52px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-soft), var(--color-primary));
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
}
.profile-name { font-size: 17px; font-weight: 700; }
.profile-meta { margin-top: 4px; font-size: 13px; color: var(--color-text-secondary); }
.menu-card { padding: 0; overflow: hidden; }
.menu-item {
  width: 100%; display: flex; align-items: center; justify-content: space-between;
  padding: 16px; border: none; border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-card); font-size: 15px; color: var(--color-text); cursor: pointer; text-align: left;
}
.menu-item:last-child { border-bottom: none; }
.menu-item__arrow { color: var(--color-text-muted); font-size: 18px; }
</style>
