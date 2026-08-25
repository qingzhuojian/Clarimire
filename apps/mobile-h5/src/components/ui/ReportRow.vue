<template>
  <div class="report-row" @click="$emit('click', report)">
    <div class="report-row__main">
      <span class="report-row__title">{{ report.title }}</span>
      <span class="status-badge" :class="`status-badge--${report.status}`">{{ issueStatusLabel(report.status) }}</span>
    </div>
    <div class="report-row__meta">
      <span v-if="report.reservoirName">{{ report.reservoirName }}</span>
      <span v-if="report.reservoirName" class="report-row__dot">·</span>
      <span>{{ timeText }}</span>
    </div>
    <span class="report-row__arrow">›</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { issueStatusLabel, formatTime, isToday } from '@/utils/labels'

const props = defineProps({
  report: { type: Object, required: true }
})
defineEmits(['click'])

const timeText = computed(() => {
  const value = props.report.createTime
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return formatTime(value)
  const pad = (n) => String(n).padStart(2, '0')
  const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (isToday(value)) return `今天 ${hm}`
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${hm}`
})
</script>

<style scoped>
.report-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
}
.report-row:last-child { border-bottom: none; }
.report-row__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.report-row__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.report-row__meta {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}
.report-row__dot { opacity: 0.5; }
.report-row__arrow {
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 16px;
  line-height: 1;
}
</style>
