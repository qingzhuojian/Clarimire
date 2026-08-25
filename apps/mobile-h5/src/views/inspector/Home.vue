<template>
  <AppShell subtitle="巡查工作台" :show-tab="false">
    <div class="home">
      <div class="summary-bar">
        <button type="button" class="summary-bar__cell" @click="goDaily">
          <strong :class="{ 'is-alert': dailyAlert }">{{ openDaily.length }}</strong>
          <span>日常{{ dailyAlert ? ' !' : '' }}</span>
        </button>
        <button type="button" class="summary-bar__cell" @click="$router.push('/inspector/tasks?type=assigned')">
          <strong>{{ assignedCount }}</strong>
          <span>指派</span>
        </button>
        <div class="summary-bar__cell summary-bar__cell--static">
          <strong>{{ weekRecords }}</strong>
          <span>本周打卡</span>
        </div>
      </div>

      <div class="entry-grid">
        <button type="button" class="entry" :class="{ 'entry--alert': dailyAlert }" @click="goDaily">
          <span class="entry__name">日常任务</span>
          <span class="entry__hint">{{ dailyAlert ? '已到期，请尽快完成' : '7 天周期 · 须打卡' }}</span>
        </button>
        <button type="button" class="entry" @click="$router.push('/inspector/tasks?type=assigned')">
          <span v-if="assignedCount" class="entry__badge">{{ assignedCount }}</span>
          <span class="entry__name">指派任务</span>
          <span class="entry__hint">到场打卡 · 问题跟进</span>
        </button>
        <button type="button" class="entry" @click="$router.push('/inspector/emergency')">
          <span class="entry__name">突发上报</span>
          <span class="entry__hint">任务外发现问题</span>
        </button>
        <button type="button" class="entry" @click="$router.push('/inspector/records')">
          <span class="entry__name">巡查记录</span>
          <span class="entry__hint">历史打卡明细</span>
        </button>
      </div>

      <section class="panel">
        <div class="panel__head">
          <h3 class="panel__title">当前任务</h3>
          <button type="button" class="panel__link" @click="goDaily">日常</button>
          <span class="panel__sep">·</span>
          <button type="button" class="panel__link" @click="$router.push('/inspector/tasks?type=assigned')">指派</button>
        </div>
        <TaskCard
          v-for="t in currentTasks"
          :key="(t.taskType || 't') + '-' + t.id"
          :task="t"
          show-actions
          @click="goTask"
          @start="startTask"
        />
        <p v-if="!currentTasks.length" class="panel__empty">暂无待办任务</p>
      </section>

      <section class="panel">
        <div class="panel__head">
          <h3 class="panel__title">最近巡查</h3>
          <button type="button" class="panel__link" @click="$router.push('/inspector/records')">全部</button>
        </div>
        <RecordRow
          v-for="r in records.slice(0, 4)"
          :key="r.id"
          :record="r"
          @click="goRecord"
        />
        <p v-if="!records.length" class="panel__empty">暂无巡查记录</p>
      </section>
    </div>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { patrolAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import AppShell from '@/components/layout/AppShell.vue'
import TaskCard from '@/components/ui/TaskCard.vue'
import RecordRow from '@/components/ui/RecordRow.vue'

const router = useRouter()
const { show: showToast } = useToast()
const dailyTasks = ref([])
const assignedTasks = ref([])
const records = ref([])
const weekRecords = ref(0)
const userId = Number(localStorage.getItem('userId'))

const isOpen = (t) => t.status !== 'completed' && t.status !== 'cancelled'
const openDaily = computed(() => dailyTasks.value.filter(isOpen))
const openAssigned = computed(() => assignedTasks.value.filter(isOpen))
const assignedCount = computed(() => openAssigned.value.length)

const dailyAlert = computed(() =>
  openDaily.value.some((t) => {
    if (!t.dueTime) return true
    return new Date(t.dueTime).getTime() <= Date.now()
  })
)

/** 日常优先，再指派，首页最多 5 条 */
const currentTasks = computed(() => [...openDaily.value, ...openAssigned.value].slice(0, 5))

const goDaily = () => router.push('/inspector/tasks?type=daily')
const goTask = (t) => router.push(`/inspector/tasks/${t.id}`)
const goRecord = (r) => {
  sessionStorage.setItem(`patrol_record_${r.id}`, JSON.stringify(r))
  router.push(`/inspector/records/${r.id}`)
}

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
    showToast(e.message || '无法开始任务', 'error')
  }
}

const load = async () => {
  try {
    await patrolAPI.ensureDaily(userId)
  } catch {
    /* ignore */
  }
  const [dailyRes, assignedRes, emRes, recordRes] = await Promise.all([
    patrolAPI.getTasks({ assigneeId: userId, taskType: 'daily' }),
    patrolAPI.getTasks({ assigneeId: userId, taskType: 'assigned' }),
    patrolAPI.getTasks({ assigneeId: userId, taskType: 'emergency' }).catch(() => ({ data: [] })),
    patrolAPI.getRecords({ userId })
  ])
  dailyTasks.value = dailyRes.data || []
  assignedTasks.value = [...(assignedRes.data || []), ...(emRes.data || [])]
  records.value = recordRes.data || []
  const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
  weekRecords.value = records.value.filter((r) => new Date(r.createTime).getTime() >= weekAgo).length
}

onMounted(load)
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}
.summary-bar__cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 12px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-right: 1px solid var(--color-border);
}
.summary-bar__cell:last-child {
  border-right: none;
}
.summary-bar__cell--static {
  cursor: default;
}
.summary-bar__cell strong {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.2;
}
.summary-bar__cell strong.is-alert {
  color: var(--color-danger);
}
.summary-bar__cell span {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.entry-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.entry {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  min-height: 64px;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: #fff;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  text-align: left;
}
.entry--alert {
  background: linear-gradient(135deg, #60a5fa 0%, #2563eb 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: var(--shadow-primary);
}
.entry__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}
.entry--alert .entry__name {
  color: #fff;
}
.entry__hint {
  font-size: 11px;
  line-height: 1.35;
  color: var(--color-text-secondary);
}
.entry--alert .entry__hint {
  color: rgba(255, 255, 255, 0.9);
}
.entry__badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
}

.panel {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: var(--shadow-card);
}
.panel__head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}
.panel__title {
  flex: 1;
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}
.panel__link {
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
}
.panel__sep {
  color: var(--color-text-muted);
  font-size: 12px;
}
.panel__empty {
  margin: 12px 0 4px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted);
}
</style>
