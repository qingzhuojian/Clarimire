<template>
  <div class="shell-page">
    <div class="shell-tabs">
      <div
        v-for="item in tabs"
        :key="item.key"
        class="shell-tab"
        :class="{ active: activeTab === item.key }"
        @click="activeTab = item.key"
      >
        {{ item.label }}
      </div>
    </div>
    <div class="shell-content">
      <Users v-if="activeTab === 'users'" />
      <CheckinPolicy v-else />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Users from './Users.vue'
import CheckinPolicy from './CheckinPolicy.vue'

const tabs = [
  { key: 'users', label: '用户权限' },
  { key: 'checkin', label: '打卡策略' }
]

const route = useRoute()
const router = useRouter()

const normalizeTab = (tab) => {
  if (tab === 'checkin-policy') return 'checkin'
  return tabs.some((t) => t.key === tab) ? tab : 'users'
}
const activeTab = ref(normalizeTab(route.query.tab))

watch(activeTab, (tab) => {
  if (route.query.tab !== tab) {
    router.replace({ path: '/system', query: { tab } })
  }
})

watch(
  () => route.query.tab,
  (tab) => {
    activeTab.value = normalizeTab(tab)
  }
)
</script>

<style scoped>
.shell-page {
  width: 100%;
  height: calc(100vh - 56px);
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.shell-tabs {
  display: flex;
  gap: 4px;
  padding: 10px 16px 0;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.shell-tab {
  padding: 10px 18px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
}

.shell-tab:hover {
  color: #1677ff;
}

.shell-tab.active {
  color: #1677ff;
  font-weight: 600;
  border-bottom-color: #1677ff;
}

.shell-content {
  flex: 1;
  overflow: auto;
}
</style>
