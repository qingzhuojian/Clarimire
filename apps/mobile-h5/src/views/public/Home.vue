<template>
  <AppShell subtitle="发现问题，一键上报" :show-tab="false">
    <div class="hero-card">
      <div class="hero-card__title">群众服务</div>
      <p class="hero-card__desc">发现水库周边环境问题？一键上报，管理员审核后派单处理。</p>
    </div>

    <div class="quick-grid">
      <QuickAction title="我要上报" desc="分段填写 · 可附照片" primary @click="$router.push('/public/report')" />
      <QuickAction title="我的上报" desc="查看处理进度" @click="$router.push('/public/reports')" />
    </div>

    <section v-if="recentReports.length" class="section-card">
      <div class="section-card__head">
        <h3 class="section-card__title">最近上报</h3>
        <button type="button" class="section-card__link" @click="$router.push('/public/reports')">查看全部</button>
      </div>
      <ReportRow v-for="r in recentReports" :key="r.id" :report="r" @click="goDetail" />
    </section>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { issueAPI } from '@/api'
import AppShell from '@/components/layout/AppShell.vue'
import QuickAction from '@/components/ui/QuickAction.vue'
import ReportRow from '@/components/ui/ReportRow.vue'

const router = useRouter()
const recentReports = ref([])
const reporterId = Number(localStorage.getItem('userId'))

const goDetail = (r) => router.push(`/public/reports/${r.id}`)

onMounted(async () => {
  const res = await issueAPI.list({ reporterId })
  recentReports.value = (res.data || []).slice(0, 3)
})
</script>
