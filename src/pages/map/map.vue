<template>
  <view class="page-map" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="map-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">🗺️ 巡查地图</text>
      <view class="header-actions">
        <view class="action-btn-icon" @click="locateCurrentPosition">
          <text>📍</text>
        </view>
      </view>
    </view>

    <!-- 地图容器 -->
    <view class="map-container">
      <view class="map-wrapper" id="page-map"></view>

      <!-- 地图信息卡片 -->
      <view class="map-info-card">
        <view class="info-row">
          <text class="info-label">当前位置</text>
          <text class="info-value">{{ currentLocation.address || '定位中...' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">坐标</text>
          <text class="info-value">{{ currentLocation.lat ? `${currentLocation.lng.toFixed(6)}, ${currentLocation.lat.toFixed(6)}` : '--' }}</text>
        </view>
      </view>

      <!-- 地图操作按钮 -->
      <view class="map-overlay">
        <view class="overlay-btn" @click="locateCurrentPosition">
          <text>📍</text>
          <text>定位</text>
        </view>
        <view class="overlay-btn primary" @click="openClockIn">
          <text>✓</text>
          <text>打卡</text>
        </view>
      </view>
    </view>

    <!-- 打卡弹窗 -->
    <view class="modal-overlay" :class="{ show: showClockIn }" @click="closeClockIn">
      <view class="modal-content clockin-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">定位打卡</text>
          <text class="modal-close" @click="closeClockIn">×</text>
        </view>
        <view class="clockin-body">
          <view class="map-wrapper" id="clockin-map-page"></view>
          <text class="clockin-coords" v-if="clockInCoords">{{ clockInCoords }}</text>
          <text class="clockin-address" v-if="clockInAddress">{{ clockInAddress }}</text>
        </view>
        <button class="btn-clockin" @click="doClockIn" :disabled="clockInStatus === 'loading'">
          {{ clockInBtnText }}
        </button>
      </view>
    </view>

    <!-- 任务列表弹窗 -->
    <view class="modal-overlay" :class="{ show: showTaskList }" @click="closeTaskList">
      <view class="modal-content task-list-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">附近任务</text>
          <text class="modal-close" @click="closeTaskList">×</text>
        </view>
        <scroll-view class="task-list-body" scroll-y>
          <view
            class="task-item"
            v-for="task in nearbyTasks"
            :key="task.id"
            @click="viewTask(task)"
          >
            <view class="task-info">
              <text class="task-title">{{ task.title }}</text>
              <text class="task-location">📍 {{ task.location }}</text>
            </view>
            <text class="task-arrow">›</text>
          </view>
          <view v-if="nearbyTasks.length === 0" class="empty-tasks">
            <text>附近暂无任务</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { taskStore } from '@/utils/mockData'
import { showToast } from '@/utils/helper'

// Store
const themeStore = useThemeStore()
const theme = themeStore.theme

// 地图数据
const mapCenter = ref({ lat: 31.0, lng: 120.6 })
const mapScale = ref(16)
let pageMap = null
let pageMarker = null
let pageWatchId = null

// 位置数据
const currentLocation = ref({ lat: 0, lng: 0, address: '' })

// 打卡数据
const showClockIn = ref(false)
const clockInStatus = ref('idle') // idle, loading, success
const clockInCoords = ref('')
const clockInAddress = ref('')
let clockInMap = null
let clockInMarker = null

const clockInBtnText = computed(() => {
  switch (clockInStatus.value) {
    case 'success': return '确认打卡'
    case 'loading': return '定位中...'
    default: return '获取定位'
  }
})

// 任务列表
const showTaskList = ref(false)
const nearbyTasks = ref([])

// 初始化
onMounted(() => {
  initPageMap()
  doLocate()
})

onUnmounted(() => {
  if (pageMap) {
    pageMap.remove()
    pageMap = null
  }
  if (clockInMap) {
    clockInMap.remove()
    clockInMap = null
  }
  if (pageWatchId !== null && navigator.geolocation) {
    navigator.geolocation.clearWatch(pageWatchId)
  }
})

// 初始化页面地图
const initPageMap = () => {
  if (pageMap) return
  pageMap = L.map('page-map', {
    center: [31.0, 120.6],
    zoom: 14,
    zoomControl: true,
    attributionControl: true,
  })

  // OpenStreetMap 标准瓦片（显示完整道路、建筑、地形）
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(pageMap)

  // 容器尺寸确定后刷新地图大小
  pageMap.invalidateSize({ animate: false })
}

// 加载任务标记点
const loadTaskMarkers = () => {
  if (!pageMap) return
  const tasks = taskStore.getAll()

  tasks.forEach(task => {
    if (task.coords) {
      const priorityColor = {
        urgent: '#f87171',
        warning: '#e8b86a',
        normal: '#4ade80'
      }[task.priority] || '#4ade80'

      const icon = L.divIcon({
        html: `<div style="
          width:36px;height:36px;
          background:${priorityColor};
          border:3px solid #fff;
          border-radius:50% 50% 50% 0;
          transform:rotate(-45deg);
          box-shadow:0 2px 8px rgba(0,0,0,0.3);
          display:flex;align-items:center;justify-content:center;
        ">
          <div style="transform:rotate(45deg);font-size:14px;">📍</div>
        </div>`,
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36],
        className: '',
      })

      L.marker([task.coords.lat, task.coords.lng], { icon })
        .addTo(pageMap)
        .bindPopup(`<b>${task.title}</b><br><span style="font-size:12px;color:#666">${task.location}</span>`)
    }
  })
}

// 获取当前位置（浏览器 Geolocation API，免费无需密钥）
const doLocate = () => {
  if (!navigator.geolocation) {
    showToast('当前浏览器不支持定位')
    return
  }

  currentLocation.value.address = '定位中...'

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const { latitude, longitude } = pos.coords
      currentLocation.value = { lat: latitude, lng: longitude, address: '定位成功' }
      mapCenter.value = { lat: latitude, lng: longitude }

      if (pageMap) {
        pageMap.setView([latitude, longitude], 16, { animate: true })
        // 更新定位标记
        if (pageMarker) {
          pageMarker.setLatLng([latitude, longitude])
        } else {
          pageMarker = L.marker([latitude, longitude], {
            icon: L.divIcon({
              html: '<div style="width:28px;height:28px;border-radius:50%;background:#38bdf8;border:3px solid #fff;box-shadow:0 2px 8px rgba(56,189,248,0.6)"></div>',
              iconSize: [28, 28],
              iconAnchor: [14, 14],
            })
          }).addTo(pageMap)
        }
        // 加载任务标记
        loadTaskMarkers()

        // 逆地理编码
        fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=zh`)
          .then(r => r.json())
          .then(data => {
            currentLocation.value.address = data.display_name || '江苏省苏州市'
          })
          .catch(() => {
            currentLocation.value.address = '江苏省苏州市'
          })
      }
    },
    (err) => {
      currentLocation.value.address = '定位失败'
      showToast('获取定位失败，请检查权限')
    },
    { enableHighAccuracy: true, timeout: 15000 }
  )
}

// 刷新定位
const locateCurrentPosition = () => {
  doLocate()
  showToast('已刷新定位')
}

// 打卡弹窗
const openClockIn = () => {
  showClockIn.value = true
  clockInStatus.value = 'idle'
  clockInCoords.value = ''
  clockInAddress.value = ''
  setTimeout(() => {
    if (!clockInMap) {
      clockInMap = L.map('clockin-map-page', {
        center: [mapCenter.value.lat, mapCenter.value.lng],
        zoom: 15,
        zoomControl: false,
        attributionControl: false,
      })
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(clockInMap)
      clockInMarker = L.marker([mapCenter.value.lat, mapCenter.value.lng]).addTo(clockInMap)
      clockInMap.invalidateSize({ animate: false })
    }
  }, 100)
}

const closeClockIn = () => {
  showClockIn.value = false
  clockInStatus.value = 'idle'
  clockInCoords.value = ''
  clockInAddress.value = ''
}

const doClockIn = async () => {
  if (clockInStatus.value === 'success') {
    showToast('打卡成功')
    closeClockIn()
    return
  }

  clockInStatus.value = 'loading'
  clockInCoords.value = ''
  clockInAddress.value = ''

  try {
    const pos = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 15000 })
    })

    const { latitude, longitude } = pos.coords
    clockInCoords.value = `经度: ${longitude.toFixed(6)}  纬度: ${latitude.toFixed(6)}`

    if (clockInMap) {
      clockInMap.setView([latitude, longitude], 16)
      if (clockInMarker) clockInMarker.setLatLng([latitude, longitude])
      else clockInMarker = L.marker([latitude, longitude]).addTo(clockInMap)
    }

    // 逆地理编码
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=zh`)
      .then(r => r.json())
      .then(data => { clockInAddress.value = data.display_name || '江苏省苏州市' })
      .catch(() => { clockInAddress.value = '江苏省苏州市' })

    clockInStatus.value = 'success'
  } catch (err) {
    showToast('定位失败，请检查权限设置')
    clockInStatus.value = 'idle'
  }
}

// 任务相关
const loadNearbyTasks = () => {
  const tasks = taskStore.getAll()
  nearbyTasks.value = tasks.slice(0, 5)
}

const viewTask = (task) => {
  closeTaskList()
  uni.navigateTo({ url: `/pages/inspection-detail/inspection-detail?id=${task.id}` })
}

const closeTaskList = () => {
  showTaskList.value = false
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page-map {
  height: 100vh;
  background: var(--bg-screen);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域
.map-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 32rpx;
  background: var(--bg-card);
  position: relative;
  z-index: 10;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: var(--bg-panel);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: var(--text-body);
}

.page-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text);
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: var(--bg-panel);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

// 地图容器
.map-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  min-height: 0; /* 关键：flex 子元素需要 min-height:0 才能正确收缩 */

  .map-wrapper {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
}

// 地图信息卡片
.map-info-card {
  position: absolute;
  top: 24rpx;
  left: 24rpx;
  right: 24rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  padding: 24rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8rpx 0;
}

.info-label {
  font-size: 24rpx;
  color: var(--text-muted);
}

.info-value {
  font-size: 24rpx;
  color: var(--text-body);
  font-weight: 500;
}

// 地图操作按钮
.map-overlay {
  position: absolute;
  right: 24rpx;
  bottom: 120rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.overlay-btn {
  width: 96rpx;
  padding: 20rpx 0;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  color: var(--text-body);
}

// 打卡弹窗
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 999;
  display: none;
  align-items: center;
  justify-content: center;
  
  &.show {
    display: flex;
  }
}

.modal-content {
  background: var(--bg-card);
  border-radius: 28rpx;
  padding: 32rpx;
  width: 600rpx;
  max-width: 90vw;
}

.clockin-modal {
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
  }
  
  .modal-title {
    font-size: 34rpx;
    font-weight: 700;
    color: var(--text);
  }
  
  .modal-close {
    font-size: 48rpx;
    color: var(--text-muted);
  }
}

.clockin-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 0 24rpx;
}

.map-wrapper {
  width: 100%;
  height: 280rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.clockin-coords {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-bottom: 8rpx;
}

.clockin-address {
  font-size: 24rpx;
  color: var(--text-muted);
}

.btn-clockin {
  width: 100%;
  padding: 24rpx;
  background: linear-gradient(155deg, #a5e8ff 0%, #38bdf8 38%, #2563eb 72%, #1e40af 100%);
  color: #f0f9ff;
  border: none;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 700;
  
  &[disabled] {
    opacity: 0.6;
  }
}

// 任务列表弹窗
.task-list-modal {
  max-height: 60vh;
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
  }
  
  .modal-title {
    font-size: 34rpx;
    font-weight: 700;
    color: var(--text);
  }
  
  .modal-close {
    font-size: 48rpx;
    color: var(--text-muted);
  }
}

.task-list-body {
  max-height: 400rpx;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: var(--bg-panel);
  border-radius: 16rpx;
  margin-bottom: 12rpx;
}

.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.task-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text);
}

.task-location {
  font-size: 22rpx;
  color: var(--text-muted);
}

.task-arrow {
  font-size: 36rpx;
  color: var(--text-muted);
}

.empty-tasks {
  text-align: center;
  padding: 48rpx;
  color: var(--text-muted);
  font-size: 26rpx;
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .map-header,
  .map-info-card,
  .overlay-btn {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
    border: none;
  }

  .map-title,
  .card-title {
    color: #1e293b;
  }

  .card-info {
    color: #64748b;
  }
}
</style>
