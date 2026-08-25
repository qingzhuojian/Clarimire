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
      <WaterSituationList v-if="activeTab === 'waterSituation'" />
      <SectionMonitorList v-else />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import WaterSituationList from '@/views/waterSituation/Index.vue'
import SectionMonitorList from '@/views/sectionMonitor/Index.vue'

const tabs = [
  { key: 'waterSituation', label: '水情管理' },
  { key: 'sectionMonitor', label: '监测断面管理' }
]

const route = useRoute()
const router = useRouter()

const normalizeTab = (tab) => (tabs.some((t) => t.key === tab) ? tab : 'waterSituation')
const activeTab = ref(normalizeTab(route.query.tab))

watch(activeTab, (tab) => {
  if (route.query.tab !== tab) {
    router.replace({ path: '/data-management', query: { tab } })
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
  min-height: 0;
}
</style>
