<template>
  <AppShell :title="pageTitle" show-back :show-tab="false" :show-logout="false">
    <FilterChips v-model="filterStatus" :items="filterItems" />

    <section class="section-card">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <template v-else>
        <div class="task-list">
          <TaskCard
            v-for="t in filteredTasks"
            :key="t.id"
            :task="t"
            show-actions
            @click="goDetail"
            @start="startTask"
          />
        </div>
        <EmptyState v-if="!filteredTasks.length" text="暂无任务" icon="任" />
      </template>
    </section>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { patrolAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import AppShell from '@/components/layout/AppShell.vue'
import FilterChips from '@/components/ui/FilterChips.vue'
import TaskCard from '@/components/ui/TaskCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const { show: showToast } = useToast()
const tasks = ref([])
const filterStatus = ref('')
const loading = ref(false)
const userId = Number(localStorage.getItem('userId'))

const taskType = computed(() => {
  const t = route.query.type
  if (t === 'daily' || t === 'assigned') return t
  return 'daily'
})

const pageTitle = computed(() => (taskType.value === 'assigned' ? '指派任务' : '日常任务'))

const counts = computed(() => {
  const all = tasks.value
  return {
    '': all.length,
    pending: all.filter((t) => t.status === 'pending' || t.status === 'assigned').length,
    in_progress: all.filter((t) => t.status === 'in_progress').length,
    completed: all.filter((t) => t.status === 'completed').length
  }
})

const filterItems = computed(() => [
  { label: `全部(${counts.value['']})`, value: '' },
  { label: `待开始(${counts.value.pending})`, value: 'pending' },
  { label: `进行中(${counts.value.in_progress})`, value: 'in_progress' },
  { label: `已完成(${counts.value.completed})`, value: 'completed' }
])

const filteredTasks = computed(() => {
  if (!filterStatus.value) return tasks.value
  if (filterStatus.value === 'pending') {
    return tasks.value.filter((t) => t.status === 'pending' || t.status === 'assigned')
  }
  return tasks.value.filter((t) => t.status === filterStatus.value)
})

const load = async () => {
  loading.value = true
  try {
    if (taskType.value === 'daily') {
      await patrolAPI.ensureDaily(userId).catch(() => null)
      const res = await patrolAPI.getTasks({ assigneeId: userId, taskType: 'daily' })
      tasks.value = res.data || []
    } else {
      const [a, e] = await Promise.all([
        patrolAPI.getTasks({ assigneeId: userId, taskType: 'assigned' }),
        patrolAPI.getTasks({ assigneeId: userId, taskType: 'emergency' })
      ])
      tasks.value = [...(a.data || []), ...(e.data || [])]
    }
  } finally {
    loading.value = false
  }
}

const goDetail = (t) => router.push(`/inspector/tasks/${t.id}`)

const startTask = async (t) => {
  try {
    if (t.status === 'pending' || t.status === 'assigned') {
      await patrolAPI.updateTask({ ...t, status: 'in_progress' })
    }
    const q = new URLSearchParams({
      taskId: String(t.id),
      reservoir: t.reservoirName || ''
    })
    router.push(`/inspector/checkin?${q.toString()}`)
  } catch (e) {
    showToast(e.message || '操作失败', 'error')
  }
}

watch(taskType, load)
onMounted(load)
</script>

<style scoped>
.loading-tip {
  text-align: center;
  color: var(--color-text-muted);
  padding: 24px;
  font-size: 14px;
}
</style>
