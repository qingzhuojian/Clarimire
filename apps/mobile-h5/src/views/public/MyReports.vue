<template>
  <AppShell title="我的上报" show-back :show-tab="false" :show-logout="false">
    <FilterChips v-model="statusFilter" :items="statusItems" />
    <section class="section-card">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <template v-else>
        <ReportRow v-for="r in filtered" :key="r.id" :report="r" @click="goDetail" />
        <EmptyState
          v-if="!filtered.length"
          text="暂无上报记录"
          icon="报"
          action-text="去上报"
          @action="$router.push('/public/report')"
        />
      </template>
    </section>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { issueAPI } from '@/api'
import AppShell from '@/components/layout/AppShell.vue'
import FilterChips from '@/components/ui/FilterChips.vue'
import ReportRow from '@/components/ui/ReportRow.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const router = useRouter()
const list = ref([])
const statusFilter = ref('')
const loading = ref(false)
const reporterId = Number(localStorage.getItem('userId'))

const counts = computed(() => {
  const all = list.value
  return {
    '': all.length,
    pending: all.filter((r) => r.status === 'pending').length,
    reviewing: all.filter((r) => r.status === 'reviewing').length,
    assigned: all.filter((r) => r.status === 'assigned').length,
    resolved: all.filter((r) => r.status === 'resolved' || r.status === 'closed').length
  }
})

const statusItems = computed(() => [
  { label: `全部(${counts.value['']})`, value: '' },
  { label: `待审核(${counts.value.pending})`, value: 'pending' },
  { label: `审核中(${counts.value.reviewing})`, value: 'reviewing' },
  { label: `已指派(${counts.value.assigned})`, value: 'assigned' },
  { label: `已解决(${counts.value.resolved})`, value: 'resolved' }
])

const filtered = computed(() => {
  if (!statusFilter.value) return list.value
  if (statusFilter.value === 'resolved') {
    return list.value.filter((r) => r.status === 'resolved' || r.status === 'closed')
  }
  return list.value.filter((r) => r.status === statusFilter.value)
})

const goDetail = (r) => router.push(`/public/reports/${r.id}`)

onMounted(async () => {
  loading.value = true
  try {
    const res = await issueAPI.list({ reporterId })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading-tip { text-align: center; padding: 24px; color: var(--color-text-muted); }
</style>
