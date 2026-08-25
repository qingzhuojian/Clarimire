<template>
  <AppShell title="问题上报" show-back :show-tab="false" :show-logout="false">
    <section class="section-card">
      <p v-if="task" class="bound-tip">关联任务：{{ task.title }} · {{ task.reservoirName || '' }}</p>
      <ReportForm
        :model="form"
        :submitting="submitting"
        dual-actions
        title-label="问题标题"
        title-placeholder="简要描述发现的问题"
        desc-placeholder="请详细说明现场情况"
        @onsite="submit('onsite')"
        @escalate="submit('escalate')"
      />
    </section>
  </AppShell>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patrolAPI, issueAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import { issueCategoryLabel } from '@/utils/labels'
import AppShell from '@/components/layout/AppShell.vue'
import ReportForm from '@/components/ui/ReportForm.vue'

const route = useRoute()
const router = useRouter()
const { show: showToast } = useToast()
const task = ref(null)
const submitting = ref(false)
const form = reactive({
  title: '',
  reservoirName: '',
  description: '',
  issueType: 'patrol',
  category: 'other',
  lat: null,
  lng: null,
  photos: ''
})

onMounted(async () => {
  const res = await patrolAPI.getTask(route.params.id)
  task.value = res.data
  if (task.value?.reservoirName) form.reservoirName = task.value.reservoirName
  if (task.value?.title) form.title = `巡查发现问题：${task.value.title}`
})

const submit = async (mode) => {
  if (!form.title || !form.description) {
    showToast('请填写标题和描述', 'error')
    return
  }
  submitting.value = true
  try {
    const cat = issueCategoryLabel(form.category)
    const onsite = mode === 'onsite'
    await issueAPI.create({
      title: form.title,
      reservoirName: form.reservoirName,
      description: `【${cat}】${form.description}${onsite ? '\n【处置】当场处理' : '\n【处置】上报管理员'}`,
      issueType: 'patrol',
      status: onsite ? 'resolved' : 'pending',
      lat: form.lat,
      lng: form.lng,
      photos: form.photos,
      patrolTaskId: Number(route.params.id)
    })
    if (onsite) {
      await patrolAPI.completeTask(route.params.id)
      showToast('已当场处理并完成任务', 'success')
    } else {
      showToast('已上报管理员，等待闭环', 'success')
      // 日常任务上报后仍可完成（问题单另走闭环）；管理员可在任务说明写备注
      await patrolAPI.completeTask(route.params.id)
    }
    setTimeout(() => router.replace('/inspector'), 1000)
  } catch (e) {
    showToast(e.message || '提交失败', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.bound-tip {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
