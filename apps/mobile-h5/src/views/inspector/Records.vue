<template>
  <AppShell title="巡查记录" show-back :show-tab="false" :show-logout="false">
    <FilterChips v-model="zoneFilter" :items="zoneItems" />
    <section class="section-card">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <template v-else>
        <RecordRow v-for="r in filtered" :key="r.id" :record="r" @click="goDetail" />
        <EmptyState
          v-if="!filtered.length"
          text="暂无巡查记录"
          icon="录"
          action-text="回首页"
          @action="$router.replace('/inspector')"
        />
      </template>
    </section>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { patrolAPI } from '@/api'
import AppShell from '@/components/layout/AppShell.vue'
import FilterChips from '@/components/ui/FilterChips.vue'
import RecordRow from '@/components/ui/RecordRow.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const router = useRouter()
const records = ref([])
const zoneFilter = ref('')
const loading = ref(false)
const userId = Number(localStorage.getItem('userId'))

const zoneItems = [
  { label: '全部', value: '' },
  { label: '核心区', value: 'core' },
  { label: '缓冲圈', value: 'buffer' },
  { label: '异地', value: 'remote' }
]

const filtered = computed(() => {
  if (!zoneFilter.value) return records.value
  return records.value.filter((r) => r.locationZone === zoneFilter.value)
})

const goDetail = (r) => {
  sessionStorage.setItem(`patrol_record_${r.id}`, JSON.stringify(r))
  router.push(`/inspector/records/${r.id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await patrolAPI.getRecords({ userId })
    records.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading-tip { text-align: center; padding: 24px; color: var(--color-text-muted); font-size: 14px; }
</style>
