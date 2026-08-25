<template>
  <AppShell title="打卡详情" show-back :show-tab="false" :show-logout="false">
    <template v-if="record">
      <section class="section-card">
        <div class="detail-status">
          <ZoneBadge v-if="record.locationZone" :zone="record.locationZone" large />
        </div>
        <h2 class="detail-title">{{ record.reservoirName || '未知水库' }}</h2>
      </section>

      <section class="section-card">
        <div class="detail-row"><span class="detail-row__label">水库名称</span><span>{{ record.reservoirName || '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">打卡区域</span><span>{{ zoneText }}</span></div>
        <div class="detail-row"><span class="detail-row__label">打卡时间</span><span>{{ formatTime(record.createTime) }}</span></div>
        <div class="detail-row"><span class="detail-row__label">距水库</span><span>{{ distanceDetail }}</span></div>
        <div class="detail-row"><span class="detail-row__label">纬度</span><span>{{ record.lat ?? '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">经度</span><span>{{ record.lng ?? '—' }}</span></div>
        <div class="detail-row"><span class="detail-row__label">定位方式</span><span>{{ modeText }}</span></div>
        <div class="detail-row"><span class="detail-row__label">巡查员</span><span>{{ record.realName || record.username || '—' }}</span></div>
      </section>

      <section v-if="remarkText" class="section-card">
        <div class="section-card__title" style="margin-bottom:10px">备注说明</div>
        <p class="detail-desc">{{ remarkText }}</p>
      </section>

      <div v-if="record.lat && record.lng" class="map-box" ref="mapEl"></div>
    </template>
    <EmptyState v-else text="记录不存在" icon="录" action-text="返回" @action="$router.back()" />
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { formatTime, zoneLabel } from '@/utils/labels'
import { addGaodeBasemap } from '@/utils/gaodeTiles'
import AppShell from '@/components/layout/AppShell.vue'
import ZoneBadge from '@/components/ui/ZoneBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const record = ref(null)
const mapEl = ref(null)
let map = null

const zoneText = computed(() =>
  record.value?.locationZone ? zoneLabel(record.value.locationZone) : '—'
)

const distanceDetail = computed(() => {
  const raw = record.value?.distanceM
  if (raw == null || raw === '') return '—'
  const n = Number(raw)
  if (!Number.isFinite(n)) return '—'
  if (Math.abs(n) >= 1000) return `${(n / 1000).toFixed(2)} 公里`
  return `${Math.round(n)} 米`
})

const modeText = computed(() => {
  const m = String(record.value?.checkinMode || 'gps').toLowerCase()
  if (m === 'gps') return 'GPS 定位'
  if (m === 'manual') return '手动选择'
  return record.value?.checkinMode || '—'
})

const remarkText = computed(() => {
  const raw = String(record.value?.remark || '').trim()
  if (!raw) return ''
  if (/^(core|buffer|remote)$/i.test(raw)) return ''
  return raw
})

onMounted(async () => {
  const key = `patrol_record_${route.params.id}`
  const raw = sessionStorage.getItem(key)
  if (raw) {
    try {
      record.value = JSON.parse(raw)
    } catch {
      record.value = null
    }
  }
  await nextTick()
  if (record.value?.lat && record.value?.lng && mapEl.value) {
    map = L.map(mapEl.value, { zoomControl: false }).setView([record.value.lat, record.value.lng], 14)
    addGaodeBasemap(map)
    L.circleMarker([record.value.lat, record.value.lng], {
      radius: 8,
      color: '#2563eb',
      fillColor: '#2563eb',
      fillOpacity: 0.85
    }).addTo(map)
    setTimeout(() => map?.invalidateSize(), 100)
  }
})

onUnmounted(() => {
  map?.remove()
  map = null
})
</script>

<style scoped>
.detail-status { margin-bottom: 12px; }
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
.map-box {
  height: 200px;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-top: 12px;
  border: 1px solid var(--color-border);
}
</style>
