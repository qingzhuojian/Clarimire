<template>
  <AppShell title="任务详情" show-back :show-tab="false" :show-logout="false">
    <div v-if="loading" class="loading-tip">加载中...</div>
    <template v-else-if="task">
      <section class="section-card">
        <div class="detail-status">
          <span class="status-badge" :class="`status-badge--${task.status}`">{{ statusLabel(task.status) }}</span>
          <span
            v-if="task.taskType"
            class="type-chip"
            :class="`type-chip--${task.taskType}`"
          >{{ typeLabel(task.taskType) }}</span>
          <span v-if="overdue" class="bang">!</span>
        </div>
        <h2 class="detail-title">{{ task.title }}</h2>
      </section>

      <section class="section-card">
        <div class="detail-row"><span class="detail-row__label">水库名称</span><span>{{ task.reservoirName || '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">截止时间</span><span>{{ formatTime(task.dueTime) }}</span></div>
        <div class="detail-row"><span class="detail-row__label">指派人</span><span>{{ task.assigneeName || '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">创建时间</span><span>{{ formatTime(task.createTime) }}</span></div>
      </section>

      <section v-if="task.description" class="section-card">
        <div class="section-card__title" style="margin-bottom:10px">任务说明 / 管理员备注</div>
        <p class="detail-desc">{{ task.description }}</p>
      </section>

      <div class="detail-actions">
        <button
          v-if="canStart"
          class="btn btn-primary"
          :disabled="acting"
          @click="goCheckin"
        >开始并打卡</button>
        <button
          v-else-if="task.status === 'in_progress'"
          class="btn btn-primary"
          :disabled="acting"
          @click="goCheckin"
        >去打卡</button>
        <button
          v-if="task.status === 'in_progress' && isAssignedLike"
          class="btn btn-secondary"
          @click="$router.push(`/inspector/tasks/${task.id}/followup`)"
        >问题跟进</button>
        <button
          v-if="task.status === 'in_progress' && isDaily"
          class="btn btn-secondary"
          @click="$router.push(`/inspector/tasks/${task.id}/result`)"
        >巡查结果</button>
      </div>
    </template>
    <EmptyState v-else text="任务不存在" icon="任" action-text="返回" @action="$router.replace('/inspector')" />
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patrolAPI } from '@/api'
import { taskStatusLabel as statusLabel, taskTypeLabel as typeLabel, formatTime } from '@/utils/labels'
import { useToast } from '@/composables/useToast'
import AppShell from '@/components/layout/AppShell.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const { show: showToast } = useToast()
const task = ref(null)
const loading = ref(true)
const acting = ref(false)

const isDaily = computed(() => task.value?.taskType === 'daily')
const isAssignedLike = computed(() => {
  const t = task.value?.taskType
  return t === 'assigned' || t === 'emergency'
})
const canStart = computed(() => {
  const s = task.value?.status
  return s === 'pending' || s === 'assigned'
})
const overdue = computed(() => {
  if (!task.value || task.value.status === 'completed') return false
  if (!task.value.dueTime) return isDaily.value
  return new Date(task.value.dueTime).getTime() <= Date.now()
})

const load = async () => {
  loading.value = true
  try {
    const res = await patrolAPI.getTask(route.params.id)
    task.value = res.data
  } catch {
    task.value = null
  } finally {
    loading.value = false
  }
}

const goCheckin = async () => {
  if (!task.value) return
  acting.value = true
  try {
    if (canStart.value) {
      await patrolAPI.updateTask({ ...task.value, status: 'in_progress' })
    }
    const q = new URLSearchParams({
      taskId: String(task.value.id),
      reservoir: task.value.reservoirName || ''
    })
    router.push(`/inspector/checkin?${q.toString()}`)
  } catch (e) {
    showToast(e.message || '操作失败', 'error')
  } finally {
    acting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.loading-tip { text-align: center; padding: 40px; color: var(--color-text-muted); }
.detail-actions { display: flex; flex-direction: column; gap: 10px; margin-top: 8px; }
.detail-status { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.detail-title { font-size: 18px; font-weight: 700; color: var(--color-text); }
.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
}
.detail-row:last-child { border-bottom: none; }
.detail-row__label { color: var(--color-text-secondary); }
.detail-desc { font-size: 14px; line-height: 1.7; white-space: pre-wrap; }
.bang {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-danger);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}
</style>
