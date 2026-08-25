<template>
  <AppShell title="我要上报" show-back :show-tab="false" :show-logout="false">
    <section class="section-card">
      <ReportForm :model="form" :submitting="submitting" @submit="submit" />
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
  issueType: 'public',
  category: 'other',
  lat: null,
  lng: null,
  photos: ''
})

const submit = async () => {
  if (!form.title || !form.description) {
    showToast('请填写标题和描述', 'error')
    return
  }
  submitting.value = true
  try {
    const cat = issueCategoryLabel(form.category)
    await issueAPI.create({
      title: form.title,
      reservoirName: form.reservoirName,
      description: `【${cat}】${form.description}`,
      issueType: 'public',
      lat: form.lat,
      lng: form.lng,
      photos: form.photos
    })
    showToast('上报成功，等待管理员处理', 'success')
    form.title = ''
    form.reservoirName = ''
    form.description = ''
    form.category = 'other'
    form.lat = null
    form.lng = null
    form.photos = ''
    setTimeout(() => router.push('/public/reports'), 800)
  } catch (e) {
    showToast(e.message || '上报失败', 'error')
  } finally {
    submitting.value = false
  }
}
</script>
