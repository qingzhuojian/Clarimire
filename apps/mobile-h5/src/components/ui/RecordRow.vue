<template>
  <div class="record-row" @click="$emit('click', record)">
    <div class="record-row__main">
      <span class="record-row__name">{{ record.reservoirName || '未知水库' }}</span>
      <ZoneBadge v-if="zone" :zone="zone" short />
    </div>
    <div class="record-row__meta">
      <span>{{ timeText }}</span>
      <span v-if="distanceText" class="record-row__dot">·</span>
      <span v-if="distanceText">{{ distanceText }}</span>
    </div>
    <span class="record-row__arrow">›</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ZoneBadge from './ZoneBadge.vue'
import { isToday } from '@/utils/labels'

const props = defineProps({
  record: { type: Object, required: true }
})
defineEmits(['click'])

const zone = computed(() => props.record.locationZone || '')

const timeText = computed(() => {
  const value = props.record.createTime
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (isToday(value)) return `今天 ${hm}`
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${hm}`
})

const distanceText = computed(() => {
  if (props.record.distanceM == null || props.record.distanceM === '') return ''
  const n = Number(props.record.distanceM)
  if (!Number.isFinite(n)) return ''
  if (Math.abs(n) >= 1000) return `${(n / 1000).toFixed(2)}公里`
  return `${Math.round(n)}米`
})
</script>

<style scoped>
.record-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
}
.record-row:last-child {
  border-bottom: none;
}
.record-row__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.record-row__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record-row__meta {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}
.record-row__dot {
  opacity: 0.5;
}
.record-row__arrow {
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 16px;
  line-height: 1;
}
</style>
