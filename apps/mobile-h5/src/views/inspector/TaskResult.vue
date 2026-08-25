<template>
  <AppShell title="巡查结果" show-back :show-tab="false" :show-logout="false">
    <section class="section-card">
      <h2 class="detail-title">{{ task?.title || '日常任务' }}</h2>
      <p class="hint">已打卡后请确认现场情况。无问题可直接完成；有问题请上报。</p>
    </section>
    <div class="detail-actions">
      <button class="btn btn-primary" :disabled="acting" @click="finishOk">一切正常，完成任务</button>
      <button class="btn btn-secondary" @click="goReport">发现问题，去上报</button>
    </div>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patrolAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import AppShell from '@/components/layout/AppShell.vue'

const route = useRoute()
const router = useRouter()
const { show: showToast } = useToast()
const task = ref(null)
const acting = ref(false)

onMounted(async () => {
  try {
    const res = await patrolAPI.getTask(route.params.id)
    task.value = res.data
  } catch {
    task.value = null
  }
})

const finishOk = async () => {
  acting.value = true
  try {
    await patrolAPI.completeTask(route.params.id)
    showToast('任务已完成', 'success')
    setTimeout(() => router.replace('/inspector/tasks?type=daily'), 800)
  } catch (e) {
    showToast(e.message || '请先完成打卡', 'error')
  } finally {
    acting.value = false
  }
}

const goReport = () => {
  router.push(`/inspector/tasks/${route.params.id}/report`)
}
</script>

<style scoped>
.detail-title { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
.hint { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.detail-actions { display: flex; flex-direction: column; gap: 10px; margin-top: 8px; }
</style>
