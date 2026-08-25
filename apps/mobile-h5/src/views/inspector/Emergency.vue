<template>
  <AppShell title="突发上报" show-back :show-tab="false" :show-logout="false">
    <section class="section-card">
      <p class="tip">发现任务外的问题也请上报：可当场处理，或交由管理员闭环派单。</p>
      <ReportForm
        :model="form"
        :submitting="submitting"
        dual-actions
        title-label="事件标题"
        title-placeholder="简要描述突发情况"
        desc-placeholder="请详细说明时间、地点和情况"
        @onsite="submit('onsite')"
        @escalate="submit('escalate')"
      />
    </section>
  </AppShell>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { issueAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import { issueCategoryLabel } from '@/utils/labels'
import AppShell from '@/components/layout/AppShell.vue'
import ReportForm from '@/components/ui/ReportForm.vue'

const router = useRouter()
const { show: showToast } = useToast()
const submitting = ref(false)
const form = reactive({
  title: '',
  reservoirName: '',
  description: '',
  issueType: 'emergency',
  category: 'other',
  lat: null,
  lng: null,
  photos: ''
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
      issueType: 'emergency',
      status: onsite ? 'resolved' : 'pending',
      lat: form.lat,
      lng: form.lng,
      photos: form.photos
    })
    showToast(onsite ? '已当场处理并闭环' : '已上报管理员', 'success')
    setTimeout(() => router.replace('/inspector'), 1000)
  } catch (e) {
    showToast(e.message || '提交失败', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.tip {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
