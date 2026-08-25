<template>
  <AppShell title="上报详情" show-back :show-tab="false" :show-logout="false">
    <div v-if="loading" class="loading-tip">加载中...</div>
    <template v-else-if="report">
      <section class="section-card">
        <div class="detail-status">
          <span class="status-badge" :class="`status-badge--${report.status}`">{{ issueStatusLabel(report.status) }}</span>
        </div>
        <h2 class="detail-title">{{ report.title }}</h2>
      </section>

      <section class="section-card">
        <div class="section-card__title" style="margin-bottom:12px">处理进度</div>
        <div class="timeline">
          <div
            v-for="step in timeline"
            :key="step.key"
            class="timeline__item"
            :class="{ 'timeline__item--done': step.done, 'timeline__item--current': step.current }"
          >
            <div class="timeline__dot" />
            <div class="timeline__content">
              <div class="timeline__label">{{ step.label }}</div>
              <div v-if="step.time" class="timeline__time">{{ step.time }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="section-card">
        <div class="detail-row"><span class="detail-row__label">水库名称</span><span>{{ report.reservoirName || '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">上报人</span><span>{{ report.reporterName || '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">上报时间</span><span>{{ formatTime(report.createTime) }}</span></div>
        <div v-if="report.lat" class="detail-row"><span class="detail-row__label">坐标位置</span><span>{{ report.lat }}, {{ report.lng }}</span></div>
        <div v-if="report.assigneeName" class="detail-row"><span class="detail-row__label">处理人</span><span>{{ report.assigneeName }}</span></div>
      </section>

      <section class="section-card">
        <div class="section-card__title" style="margin-bottom:10px">问题描述</div>
        <p class="detail-desc">{{ report.description }}</p>
      </section>

      <section v-if="photos.length" class="section-card">
        <div class="section-card__title" style="margin-bottom:10px">现场照片</div>
        <div class="photo-grid">
          <img v-for="(p, i) in photos" :key="i" :src="photoUrl(p)" alt="" />
        </div>
      </section>

      <section v-if="report.adminRemark" class="section-card">
        <div class="section-card__title" style="margin-bottom:10px">处理备注</div>
        <p class="detail-desc">{{ report.adminRemark }}</p>
      </section>
    </template>
    <EmptyState v-else text="记录不存在" icon="报" action-text="返回" @action="$router.back()" />
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { issueAPI } from '@/api'
import { issueStatusLabel, formatTime, parsePhotos, photoUrl } from '@/utils/labels'
import AppShell from '@/components/layout/AppShell.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const report = ref(null)
const loading = ref(true)

const photos = computed(() => parsePhotos(report.value?.photos))

const timeline = computed(() => {
  const s = report.value?.status || 'pending'
  const order = ['pending', 'reviewing', 'assigned', 'resolved']
  const labels = { pending: '已提交', reviewing: '审核中', assigned: '已指派', resolved: '已解决' }
  let idx = order.indexOf(s)
  if (s === 'closed') idx = 3
  return order.map((key, i) => ({
    key,
    label: labels[key],
    done: i <= idx,
    current: i === idx,
    time: i === 0 ? formatTime(report.value?.createTime) : (i === idx ? formatTime(report.value?.updateTime) : '')
  }))
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await issueAPI.getById(route.params.id)
    report.value = res.data
  } catch {
    report.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading-tip { text-align: center; padding: 40px; color: var(--color-text-muted); }
.detail-status { margin-bottom: 12px; }
.detail-title { font-size: 18px; font-weight: 700; line-height: 1.4; }
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
.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.photo-grid img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  background: var(--color-bg-muted);
}
.timeline { display: flex; flex-direction: column; gap: 0; }
.timeline__item {
  display: flex;
  gap: 12px;
  position: relative;
  padding-bottom: 18px;
}
.timeline__item:last-child { padding-bottom: 0; }
.timeline__item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: var(--color-border);
}
.timeline__item--done:not(:last-child)::before { background: var(--color-primary); }
.timeline__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d9d9d9;
  margin-top: 3px;
  flex-shrink: 0;
  z-index: 1;
}
.timeline__item--done .timeline__dot { background: var(--color-primary); }
.timeline__item--current .timeline__dot {
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.18);
}
.timeline__label { font-size: 14px; font-weight: 600; color: var(--color-text-muted); }
.timeline__item--done .timeline__label { color: var(--color-text); }
.timeline__time { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }
</style>
