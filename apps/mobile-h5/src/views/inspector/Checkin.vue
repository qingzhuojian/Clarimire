<template>
  <AppShell title="巡查打卡" show-back :show-tab="false" :show-logout="false">
    <section class="section-card">
      <p v-if="boundTaskId" class="bound-tip">已绑定任务 #{{ boundTaskId }}，打卡后继续后续流程</p>

      <div class="form-section">
        <div class="form-section__title">选择水库</div>
        <ReservoirSearchSelect
          v-model="form.reservoirName"
          :options="reservoirs"
          placeholder="输入关键字搜索水库"
          @change="onReservoirChange"
        />
      </div>

      <div class="form-section">
        <div class="form-section__title">位置地图</div>
        <div ref="mapEl" class="checkin-map"></div>
        <div class="location-box" style="margin-top:10px">
          <div class="location-box__coords">
            {{ form.lat != null ? `${form.lat}, ${form.lng}` : '尚未定位' }}
          </div>
          <p class="location-box__hint">开发环境可用右下角 DEV 面板模拟坐标</p>
        </div>
        <button class="btn btn-secondary" style="margin-top:10px" @click="getLocation">获取定位</button>
      </div>

      <div class="form-section">
        <div class="form-section__title">备注说明</div>
        <textarea v-model="form.remark" class="textarea" placeholder="边沿巡查可填写说明" />
      </div>

      <div v-if="result.zone" class="result-box">
        <span class="result-box__label">判定结果</span>
        <ZoneBadge :zone="result.zone" large />
        <span v-if="result.distanceM != null" class="result-box__label">距水库 {{ distanceLabel }}</span>
      </div>

      <button class="btn btn-primary" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '提交打卡' }}
      </button>
    </section>
  </AppShell>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { patrolAPI, systemAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import { addGaodeBasemap } from '@/utils/gaodeTiles'
import AppShell from '@/components/layout/AppShell.vue'
import ZoneBadge from '@/components/ui/ZoneBadge.vue'
import ReservoirSearchSelect from '@/components/ui/ReservoirSearchSelect.vue'

const route = useRoute()
const router = useRouter()
const { show: showToast } = useToast()
const reservoirs = ref([])
const submitting = ref(false)
const mapEl = ref(null)
const taskMeta = ref(null)
const boundTaskId = computed(() => form.taskId)
const result = reactive({ zone: '', distanceM: null })
const form = reactive({
  reservoirName: '', lat: null, lng: null, remark: '', taskId: null, checkinMode: 'gps'
})

const distanceLabel = computed(() => {
  const n = Number(result.distanceM)
  if (!Number.isFinite(n)) return ''
  if (Math.abs(n) >= 1000) return `${(n / 1000).toFixed(2)} 公里`
  return `${Math.round(n)} 米`
})

let map = null
let userMarker = null
let reservoirMarker = null

const initMap = async () => {
  await nextTick()
  if (!mapEl.value || map) return
  const center = form.lat && form.lng ? [form.lat, form.lng] : [40.48, 116.84]
  map = L.map(mapEl.value, { zoomControl: false }).setView(center, 12)
  addGaodeBasemap(map)
  setTimeout(() => map?.invalidateSize(), 150)
}

const updateUserMarker = () => {
  if (!map || form.lat == null || form.lng == null) return
  if (userMarker) userMarker.setLatLng([form.lat, form.lng])
  else {
    userMarker = L.circleMarker([form.lat, form.lng], {
      radius: 8,
      color: '#2563eb',
      fillColor: '#2563eb',
      fillOpacity: 0.9
    }).addTo(map)
  }
  map.setView([form.lat, form.lng], Math.max(map.getZoom(), 13))
}

const onReservoirChange = (name) => {
  const selected = name || form.reservoirName
  const r = reservoirs.value.find((x) => x.reservoirName === selected)
  if (!map || !r?.lat || !r?.lng) return
  const lat = Number(r.lat)
  const lng = Number(r.lng)
  if (reservoirMarker) reservoirMarker.setLatLng([lat, lng])
  else {
    reservoirMarker = L.circleMarker([lat, lng], {
      radius: 7,
      color: '#389e0d',
      fillColor: '#389e0d',
      fillOpacity: 0.85
    }).addTo(map)
  }
  if (!form.lat) map.setView([lat, lng], 12)
}

const readDevLocation = () => {
  const lat = localStorage.getItem('devLat')
  const lng = localStorage.getItem('devLng')
  if (lat && lng) {
    form.lat = Number(lat)
    form.lng = Number(lng)
    updateUserMarker()
  }
}

const onDevLocation = (e) => {
  form.lat = e.detail.lat
  form.lng = e.detail.lng
  updateUserMarker()
}

const getLocation = () => {
  readDevLocation()
  if (form.lat && form.lng) {
    updateUserMarker()
    showToast('已读取当前坐标', 'info')
    return
  }
  if (!navigator.geolocation) {
    form.lat = 40.485
    form.lng = 116.845
    updateUserMarker()
    showToast('浏览器不支持定位，已使用默认坐标', 'info')
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      form.lat = Number(pos.coords.latitude.toFixed(6))
      form.lng = Number(pos.coords.longitude.toFixed(6))
      updateUserMarker()
      showToast('定位成功', 'success')
    },
    () => {
      form.lat = 40.485
      form.lng = 116.845
      updateUserMarker()
      showToast('定位失败，已使用默认坐标', 'info')
    }
  )
}

const afterCheckin = () => {
  const id = form.taskId
  if (!id) {
    router.replace('/inspector')
    return
  }
  const type = taskMeta.value?.taskType || 'daily'
  if (type === 'daily') {
    router.replace(`/inspector/tasks/${id}/result`)
  } else {
    router.replace(`/inspector/tasks/${id}/followup`)
  }
}

const submit = async () => {
  if (!form.taskId) {
    showToast('请从日常/指派任务进入打卡', 'error')
    return
  }
  if (!form.reservoirName) {
    showToast('请选择水库', 'error')
    return
  }
  if (!form.lat || !form.lng) getLocation()
  submitting.value = true
  try {
    const res = await patrolAPI.checkin({ ...form })
    result.zone = res.locationZone || res.data?.locationZone
    result.distanceM = res.distanceM ?? res.data?.distanceM
    showToast(res.message || '打卡成功', 'success')
    setTimeout(afterCheckin, 600)
  } catch (e) {
    showToast(e.message || '打卡失败', 'error')
    const data = e.response?.data
    if (data) {
      result.zone = data.locationZone
      result.distanceM = data.distanceM
    }
  } finally {
    submitting.value = false
  }
}

watch(() => [form.lat, form.lng], () => updateUserMarker())

onMounted(async () => {
  const tid = route.query.taskId ? Number(route.query.taskId) : null
  if (tid) form.taskId = tid
  if (route.query.reservoir) form.reservoirName = String(route.query.reservoir)

  if (tid) {
    try {
      const tr = await patrolAPI.getTask(tid)
      taskMeta.value = tr.data
      if (!form.reservoirName && tr.data?.reservoirName) {
        form.reservoirName = tr.data.reservoirName
      }
    } catch {
      /* ignore */
    }
  }

  const res = await systemAPI.getReservoirLocations()
  reservoirs.value = res.data || []
  await initMap()
  if (form.reservoirName) onReservoirChange(form.reservoirName)
  readDevLocation()
  window.addEventListener('dev-location', onDevLocation)
})

onUnmounted(() => {
  window.removeEventListener('dev-location', onDevLocation)
  map?.remove()
  map = null
})
</script>

<style scoped>
.checkin-map {
  height: 180px;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  background: #e8eef5;
}
.bound-tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 8px 10px;
  border-radius: 8px;
}
</style>
