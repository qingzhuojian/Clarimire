<template>
  <view class="page-home" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 - 固定 128rpx -->
    <view class="home-header">
      <view class="header-left">
        <view class="greeting">
          <text class="greeting-text">{{ greeting }}</text>
        </view>
      </view>
      <view class="header-right">
        <view class="theme-toggle" @click="toggleTheme">
          <text>{{ theme === 'dark' ? '🌙' : '☀️' }}</text>
        </view>
        <view class="notification-btn" @click="goToNotifications">
          <text class="icon">🔔</text>
          <view v-if="unreadCount > 0" class="badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</view>
        </view>
        <view class="mine-btn" @click="goToMine">
          <text class="icon">👤</text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 - flex布局，上半部分1/3，下半部分2/3 -->
    <view class="main-content" style="padding-left: 20rpx; padding-right: 20rpx;">
      <!-- 上半部分 - 根据角色显示不同内容 -->
      <view class="top-section">
        <!-- 巡查员顶部区域 -->
        <template v-if="userStore.userRole === 'inspector'">
          <!-- 快捷入口 -->
          <view class="quick-actions">
            <view class="actions-row">
              <view class="action-item" @click="goToTasks">
                <text class="action-text">巡查任务</text>
              </view>
              <view class="action-item" @click="openClockIn">
                <text class="action-text">定位打卡</text>
              </view>
            </view>
            <view class="actions-row">
              <view class="action-item" @click="goToInspectionRecords">
                <text class="action-text">巡查记录</text>
              </view>
              <view class="action-item" @click="goToReport">
                <text class="action-text">问题上报</text>
              </view>
            </view>
          </view>
        </template>

        <!-- 群众顶部区域 -->
        <template v-else>
          <!-- 群众上报入口 -->
          <view class="report-entrance" @click="goToReport">
            <view class="entrance-icon">📝</view>
            <view class="entrance-content">
              <text class="entrance-title">群众上报</text>
              <text class="entrance-desc">发现环境问题，一键上报</text>
            </view>
            <text class="entrance-arrow">›</text>
          </view>
        </template>
      </view>

      <!-- 下半部分 - 任务列表 (约2/3) -->
      <scroll-view class="bottom-section" scroll-y>
        <!-- 巡查员底部视图 -->
        <view v-if="userStore.userRole === 'inspector'" class="inspector-view">
          <!-- 当前任务 -->
          <view class="section">
            <view class="section-header">
              <text class="section-title">当前任务</text>
              <text class="section-more" @click="goToTasks">查看全部 ›</text>
            </view>
            <view class="task-card" v-for="task in currentTasks" :key="task.id" @click="viewTaskDetail(task)">
              <view class="task-header">
                <view class="task-priority" :style="{ background: getPriorityBg(getTaskPriority(task)), color: getPriorityColor(getTaskPriority(task)) }">
                  {{ getPriorityText(getTaskPriority(task)) }}
                </view>
                <text class="task-type">{{ task.description || '常规巡查' }}</text>
              </view>
              <text class="task-title">{{ task.title }}</text>
              <view class="task-info">
                <text class="task-location">📍 {{ task.reservoirName }}</text>
                <text class="task-deadline">⏰ {{ formatDeadline(task.deadline) }}</text>
              </view>
            </view>
            <view v-if="currentTasks.length === 0" class="empty-task">
              <text>暂无待处理任务</text>
            </view>
          </view>

          <!-- 最近巡查记录 -->
          <view class="section">
            <view class="section-header">
              <text class="section-title">最近巡查</text>
            </view>
            <view class="record-item" v-for="record in recentRecords" :key="record.id" @click="viewInspectionDetail(record)">
              <view class="record-left">
                <text class="record-title">{{ record.inspector || '未知巡查员' }}</text>
                <text class="record-info">{{ record.address || record.reservoirName || '未知地点' }} · {{ formatRecordTime(record.createdAt) }}</text>
              </view>
              <view class="record-status" :style="{ color: getStatusColor(record.status) }">
                {{ record.status }}
              </view>
            </view>
          </view>
        </view>

        <!-- 群众底部视图 - 最近上报 -->
        <view v-else class="public-view">
          <!-- 最近上报 -->
          <view class="section">
            <view class="section-header">
              <text class="section-title">最近上报</text>
              <text class="section-more" @click="goToMyReports">查看全部 ›</text>
            </view>
            <view v-if="myReports.length > 0">
              <view class="report-item" v-for="report in myReports.slice(0, 3)" :key="report.id" @click="viewReportDetail(report)">
                <view class="report-left">
                  <view class="report-title-row">
                    <text class="report-title">{{ report.description || report.address }}</text>
                    <view class="report-status-tag" :class="getReportStatusClass(report.status)">
                      {{ getReportStatusText(report.status) }}
                    </view>
                  </view>
                  <text class="report-info">{{ report.address }} · {{ formatReportTime(report.createdAt) }}</text>
                </view>
                <text class="report-arrow">›</text>
              </view>
            </view>
            <view v-else class="empty-report">
              <text>暂无上报记录</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 打卡弹窗 -->
    <view class="modal-overlay" :class="{ show: showClockIn }" @click="closeClockIn">
      <view class="modal-content clockin-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">定位打卡</text>
          <text class="modal-close" @click="closeClockIn">×</text>
        </view>
        <view class="clockin-body">
          <!-- 地图容器 -->
          <view class="map-wrapper" id="clockin-map"></view>
          <text class="clockin-coords" v-if="clockInCoords">{{ clockInCoords }}</text>
          <text class="clockin-address" v-if="clockInAddress">{{ clockInAddress }}</text>
          <view class="clockin-actions">
            <view class="clockin-refresh" @click="refreshClockInLocation">
              <text>🔄</text>
              <text>刷新定位</text>
            </view>
            <view class="clockin-virtual" @click="showClockInVirtualModal">
              <text>📍</text>
              <text>虚拟定位</text>
            </view>
          </view>
        </view>
        <button class="btn-clockin" @click="doClockIn" :disabled="clockInStatus === 'loading'">
          {{ clockInBtnText }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { taskStore, getMockInspectionRecords, virtualLocationStore, CITY_COORDS } from '@/utils/mockData'
import { reportStore } from '@/utils/mockData'
import {
  showToast,
  getGreeting,
  getRelativeTime,
  getStatusColor
} from '@/utils/helper'
import {
  getDashboardStats,
  getPatrolStats,
  getMobilePendingTasks,
  getMobilePatrolRecords,
  getMyMobileReports,
  patrolCheckin
} from '@/utils/api'

// Store
const themeStore = useThemeStore()
const userStore = useUserStore()

// 响应式数据 — 初始值与 store 保持同步，避免渲染时闪烁
const theme = ref(themeStore.theme)
const userRole = ref('')
const userName = ref('巡查员')

const greeting = computed(() => getGreeting())

// 统计数据
const todayStats = ref({ completed: 3, total: 8 })
const pendingTasks = ref(2)
const monthlyStats = ref(24)
const unreadCount = ref(2)

// 任务和记录
const currentTasks = ref([])
const recentRecords = ref([])
const myReports = ref([])


// 打卡相关
const showClockIn = ref(false)
const clockInStatus = ref('idle') // idle, loading, success
const clockInCoords = ref('')
const clockInAddress = ref('')
let clockInMap = null
let clockInMarker = null
let clockInWatcherId = null

// 虚拟定位状态
const isClockInVirtualEnabled = computed(() => virtualLocationStore.isEnabled())

// 显示打卡虚拟定位选择弹窗
const showClockInVirtualModal = () => {
  const cities = Object.entries(CITY_COORDS).map(([name, data]) => ({
    name,
    lat: data.lat,
    lng: data.lng,
    address: data.address
  }))
  
  uni.showActionSheet({
    itemList: ['关闭虚拟定位', ...cities.map(c => c.name)],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 关闭虚拟定位
        virtualLocationStore.clear()
        showToast('已关闭虚拟定位')
      } else {
        const selectedCity = cities[res.tapIndex - 1]
        virtualLocationStore.set({
          enabled: true,
          lat: selectedCity.lat,
          lng: selectedCity.lng,
          address: selectedCity.address,
          city: selectedCity.name
        })
        clockInCoords.value = `经度: ${selectedCity.lng.toFixed(6)}  纬度: ${selectedCity.lat.toFixed(6)}`
        clockInAddress.value = selectedCity.address
        clockInStatus.value = 'success'
        
        // 更新地图
        if (clockInMap) {
          clockInMap.setView([selectedCity.lat, selectedCity.lng], 16, { animate: true })
          if (clockInMarker) {
            clockInMarker.setLatLng([selectedCity.lat, selectedCity.lng])
          } else {
            clockInMarker = L.marker([selectedCity.lat, selectedCity.lng]).addTo(clockInMap)
          }
        }
        
        showToast(`已切换到 ${selectedCity.name}`)
      }
    }
  })
}

// 刷新打卡定位
const refreshClockInLocation = () => {
  if (isClockInVirtualEnabled.value) {
    const coords = virtualLocationStore.getCoords()
    if (coords) {
      clockInCoords.value = `经度: ${coords.lng.toFixed(6)}  纬度: ${coords.lat.toFixed(6)}`
      const config = virtualLocationStore.get()
      clockInAddress.value = config.address
      return
    }
  }
  doClockIn()
}

const clockInBtnText = computed(() => {
  switch (clockInStatus.value) {
    case 'success': return '确认打卡'
    case 'loading': return '定位中...'
    default: return '获取定位'
  }
})

// 初始化
onMounted(() => {
  // theme 已在 store initTheme 时通过 html[data-theme] 属性生效
  // 此处同步本地状态并加载数据
  theme.value = themeStore.theme
  userRole.value = userStore.userRole
  // 根据身份显示不同的默认名称
  if (userStore.userRole === 'public') {
    userName.value = '群众'
  } else if (userStore.userRole === 'inspector') {
    userName.value = userStore.currentUser?.name || '巡查员'
  } else {
    userName.value = '群众'
  }
  loadData()
})

const loadData = async () => {
  try {
    // 并行加载多个接口数据
    const [dashboardRes, patrolRes] = await Promise.all([
      getDashboardStats().catch(() => null),
      getPatrolStats().catch(() => null)
    ])

    // 更新统计数据
    if (dashboardRes) {
      todayStats.value = {
        completed: dashboardRes.todayCompleted || 0,
        total: dashboardRes.todayTotal || 0
      }
      pendingTasks.value = dashboardRes.pendingTasks || 0
      monthlyStats.value = dashboardRes.monthlyStats || dashboardRes.totalCheckins || 0
    }

      // 加载巡查任务
    try {
      // 不传 assigneeId，后端 findAllPending 查全部 pending 任务，前端自己过滤
      const tasks = await getMobileTaskList({ page: 1, pageSize: 20 })
      let taskList = []
      if (Array.isArray(tasks)) {
        taskList = tasks
      } else if (tasks?.list && Array.isArray(tasks.list)) {
        taskList = tasks.list
      }
      currentTasks.value = taskList
        .filter(t => t.status === 'pending' || t.status === 'processing')
        .slice(0, 2)
    } catch (e) {
      // 降级到本地 mock
      const localTasks = taskStore.getAll()
      if (Array.isArray(localTasks)) {
        currentTasks.value = localTasks.filter(t => t.status === 'pending' || t.status === 'in_progress').slice(0, 2)
      } else {
        currentTasks.value = []
      }
    }

    // 加载巡查记录（只显示当前巡查员的）
    try {
      const inspectorName = userStore.currentUser?.name || ''
      const recordsRes = await getMobilePatrolRecords({ page: 1, pageSize: 3 })
      // 确保返回的是数组并过滤当前巡查员的记录
      let records = []
      if (Array.isArray(recordsRes)) {
        records = recordsRes
      } else if (recordsRes?.list && Array.isArray(recordsRes.list)) {
        records = recordsRes.list
      }
      // 前端过滤：只显示当前巡查员的记录
      if (inspectorName) {
        records = records.filter(r => r.inspector === inspectorName)
      }
      recentRecords.value = records.slice(0, 3).map(r => ({ ...r, _type: 'patrol' }))
    } catch (e) {
      // 降级到本地 mock
      const mockRecords = getMockInspectionRecords()
      recentRecords.value = Array.isArray(mockRecords) ? mockRecords.slice(0, 3) : []
    }

    // 加载群众上报
    if (userStore.userRole === 'public') {
      try {
        const reports = await getMyMobileReports(userStore.currentUser?.username || '')
        myReports.value = (reports || []).slice(0, 3)
      } catch (e) {
        myReports.value = reportStore.getAll().slice(0, 3)
      }
    }
  } catch (err) {
    console.error('加载数据失败:', err)
  }
}

// 方法
const toggleTheme = () => {
  themeStore.toggleTheme()
  theme.value = themeStore.theme
}

const goToNotifications = () => {
  uni.navigateTo({ url: '/pages/notifications/notifications' })
}

const goToMine = () => {
  uni.navigateTo({ url: '/pages/mine/mine' })
}

const goToTasks = () => {
  uni.navigateTo({ url: '/pages/tasks/tasks' })
}

const goToInspectionRecords = () => {
  uni.navigateTo({ url: '/pages/inspection-records/inspection-records' })
}

const goToReport = () => {
  uni.navigateTo({ url: '/pages/report/report' })
}

const goToInspections = () => {
  uni.navigateTo({ url: '/pages/patrol-records/patrol-records' })
}

const goToMyReports = () => {
  uni.navigateTo({ url: '/pages/my-reports/my-reports' })
}

const viewTaskDetail = (task) => {
  uni.navigateTo({ url: `/pages/inspection-detail/inspection-detail?id=${task.id}&type=task` })
}

const viewInspectionDetail = (record) => {
  uni.navigateTo({ url: `/pages/inspection-detail/inspection-detail?id=${record.id}&type=patrol` })
}

const viewReportDetail = (report) => {
  uni.navigateTo({ url: `/pages/report-detail/report-detail?id=${report.id}` })
}

// 群众上报辅助方法
const getReportStatusClass = (status) => {
  switch (status) {
    case 'completed': return 'resolved'
    case 'processing': return 'processing'
    default: return 'pending'
  }
}

const getReportStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'processing': return '处理中'
    case 'assigned': return '已分配'
    default: return '待处理'
  }
}

const formatReportTime = (isoStr) => {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// 打卡功能
const openClockIn = () => {
  showClockIn.value = true
  clockInStatus.value = 'idle'
  clockInCoords.value = ''
  clockInAddress.value = ''

  // 延迟初始化地图，确保 DOM 已渲染
  setTimeout(() => {
    initClockInMap()
  }, 100)
}

const closeClockIn = () => {
  showClockIn.value = false
  clockInStatus.value = 'idle'
  clockInCoords.value = ''
  clockInAddress.value = ''

  // 清理地图
  if (clockInMap) {
    clockInMap.remove()
    clockInMap = null
    clockInMarker = null
  }
  if (clockInWatcherId !== null) {
    uni.stopLocationUpdate && uni.stopLocationUpdate({ type: 'gcj02' })
    clockInWatcherId = null
  }
}

const initClockInMap = () => {
  // 初始化 Leaflet 地图（默认中心为中国）
  if (!clockInMap) {
    clockInMap = L.map('clockin-map', {
      center: [31.0, 120.6], // 苏州附近
      zoom: 14,
      zoomControl: true,
      attributionControl: false,
    })

    // 使用高德地图瓦片，避免 OSM 瓦片在某些网络环境下加载为空白
    L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
      maxZoom: 19,
      subdomains: ['1', '2', '3', '4']
    }).addTo(clockInMap)

    // 默认标记
    clockInMarker = L.marker([31.0, 120.6], { draggable: false }).addTo(clockInMap)
  }
}

const doClockIn = async () => {
  if (clockInStatus.value === 'success') {
    // 确认打卡，调用后端签到接口
    try {
      await patrolCheckin({
        lat: parseFloat(clockInCoords.value.match(/纬度: ([\d.]+)/)?.[1] || '0'),
        lng: parseFloat(clockInCoords.value.match(/经度: ([\d.]+)/)?.[1] || '0'),
        address: clockInAddress.value,
        inspector: userStore.currentUser?.name || '',
        time: new Date().toISOString()
      })
      showToast('打卡成功')
    } catch (err) {
      showToast('打卡成功（演示模式）')
    }
    closeClockIn()
    return
  }

  clockInStatus.value = 'loading'
  clockInCoords.value = ''
  clockInAddress.value = ''

  try {
    // 使用浏览器 Geolocation API（免费，无需密钥）
    await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (res) => resolve(res),
        (err) => reject(err),
        { enableHighAccuracy: true, timeout: 15000 }
      )
    }).then((pos) => {
      const { latitude, longitude } = pos.coords
      clockInCoords.value = `经度: ${longitude.toFixed(6)}  纬度: ${latitude.toFixed(6)}`

      // 更新地图中心 + 标记
      if (clockInMap) {
        clockInMap.setView([latitude, longitude], 16, { animate: true })
        if (clockInMarker) clockInMarker.setLatLng([latitude, longitude])
        else clockInMarker = L.marker([latitude, longitude]).addTo(clockInMap)
      }

      // 使用 Nominatim 免费逆地理编码（无密钥）
      fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=zh`)
        .then(r => r.json())
        .then(data => {
          clockInAddress.value = data.display_name || '江苏省苏州市'
        })
        .catch(() => {
          clockInAddress.value = '江苏省苏州市'
        })

      clockInStatus.value = 'success'
    }).catch((err) => {
      showToast('定位失败，请检查权限设置')
      clockInStatus.value = 'idle'
    })
  } catch (err) {
    showToast('定位失败，请检查权限设置')
    clockInStatus.value = 'idle'
  }
}

// 辅助方法
const formatDeadline = (deadline) => {
  if (!deadline) return ''
  const d = new Date(deadline)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const formatRecordTime = (isoStr) => {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const getTaskPriority = (task) => {
  if (!task.deadline) return 'normal'
  const now = Date.now()
  const deadline = new Date(task.deadline).getTime()
  const hoursLeft = (deadline - now) / 3600000
  if (hoursLeft < 0) return 'urgent'
  if (hoursLeft < 12) return 'urgent'
  if (hoursLeft < 48) return 'warning'
  return 'normal'
}

const getPriorityBg = (priority) => {
  switch (priority) {
    case 'urgent': return 'rgba(248, 113, 113, 0.15)'
    case 'warning': return 'rgba(232, 184, 106, 0.15)'
    default: return 'rgba(74, 222, 128, 0.15)'
  }
}

const getPriorityColor = (priority) => {
  switch (priority) {
    case 'urgent': return '#f87171'
    case 'warning': return '#e8b86a'
    default: return '#4adf82'
  }
}

const getPriorityText = (priority) => {
  switch (priority) {
    case 'urgent': return '紧急'
    case 'warning': return '重要'
    default: return '一般'
  }
}

const getUrgencyBg = (urgency) => {
  switch (urgency) {
    case 'urgent': return 'rgba(248, 113, 113, 0.15)'
    case 'warning': return 'rgba(232, 184, 106, 0.15)'
    default: return 'rgba(74, 222, 128, 0.15)'
  }
}

const getUrgencyColor = (urgency) => {
  switch (urgency) {
    case 'urgent': return '#f87171'
    case 'warning': return '#e8b86a'
    default: return '#4ade80'
  }
}

const getUrgencyText = (urgency) => {
  switch (urgency) {
    case 'urgent': return '紧急'
    case 'warning': return '一般'
    default: return '轻微'
  }
}
</script>

<style lang="scss" scoped>
page {
  height: 100%;
}

.page-home {
  height: 100vh;
  background: var(--bg-screen);
  color: var(--text-body);
  display: flex;
  flex-direction: column;
}

// 状态栏
.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域 - 固定 100rpx
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100rpx;
  padding: 0 24rpx;
  flex-shrink: 0;
}

// 主要内容区域
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20rpx;
  overflow: hidden;
  min-height: 0;
}

// 上半部分 - 统计卡片 + 快捷入口 (约1/3)
.top-section {
  padding-top: 20rpx;
  padding-bottom: 20rpx;
}

// 下半部分 - 列表区域 (约2/3)
.bottom-section {
  flex: 1;
  min-height: 0;
  padding-bottom: 60rpx;
}

.greeting {
  display: flex;
  flex-direction: column;
  margin-top: 30rpx;
}

.greeting-text {
  font-size: 28rpx;
  color: var(--text-muted);
  margin-bottom: 8rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.theme-toggle {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: var(--emoji-color);
}

.notification-btn {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--emoji-color);

  .icon {
    font-size: 32rpx;
    color: var(--emoji-color);
  }
  
  .badge {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    min-width: 32rpx;
    height: 32rpx;
    border-radius: 16rpx;
    background: #f87171;
    color: #fff;
    font-size: 20rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8rpx;
  }
}

.mine-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--emoji-color);

  .icon {
    font-size: 32rpx;
    color: var(--emoji-color);
  }
}

// 统计卡片
.stats-card {
  padding: 24rpx 20rpx;
  background: linear-gradient(145deg, #142347 0%, #1a2d55 100%);
  border-radius: 22rpx;
  border: 1px solid rgba(80, 140, 220, 0.18);
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-label {
  font-size: 22rpx;
  color: var(--text-muted);
  margin-bottom: 8rpx;
}

.stats-value {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text);

  &.warning {
    color: #fbbf24;
  }

  &.success {
    color: #34d399;
  }
}

.stats-unit {
  font-size: 20rpx;
  color: var(--text-muted);
  margin-top: 4rpx;
}

.stats-divider {
  width: 1px;
  height: 60rpx;
  background: var(--border);
}

// 快捷入口
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 18rpx;
}

.actions-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.action-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 26rpx 0;
  background: rgba(224, 242, 254, 0.55);
  border-radius: 18rpx;
}

.action-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #0c4a6e;
}

// 巡查员视图和群众视图
.inspector-view,
.public-view {
  display: flex;
  flex-direction: column;
}

// 群众上报入口
.report-entrance {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  background: linear-gradient(145deg, #142347 0%, #1a2d55 100%);
  border: 1px solid rgba(80, 140, 220, 0.18);
  border-radius: 18rpx;
  margin-bottom: 16rpx;
}

.entrance-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: rgba(59, 130, 246, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.entrance-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.entrance-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6rpx;
}

.entrance-desc {
  font-size: 22rpx;
  color: var(--text-muted);
}

.entrance-arrow {
  font-size: 36rpx;
  color: var(--text-muted);
  flex-shrink: 0;
}

// Section
.section {
  margin-bottom: 16rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text);
}

.section-more {
  font-size: 22rpx;
  color: var(--text-muted);
}

// 任务卡片
.task-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  padding: 18rpx;
  margin-bottom: 12rpx;
}

.task-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.task-priority {
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  font-size: 18rpx;
  font-weight: 600;
}

.task-type {
  font-size: 20rpx;
  color: var(--text-muted);
}

.task-title {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 12rpx;
  display: block;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.task-location,
.task-deadline {
  font-size: 20rpx;
  color: var(--text-muted);
}

// 记录项
.record-item {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  padding: 18rpx;
  margin-bottom: 10rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.record-title {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text);
}

.record-info {
  font-size: 20rpx;
  color: var(--text-muted);
}

.record-status {
  font-size: 22rpx;
  font-weight: 600;
}

// 上报项
.report-item {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  padding: 18rpx;
  margin-bottom: 10rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.report-left {
  flex: 1;
}

.report-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 6rpx;
}

.report-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text);
}

.report-urgency {
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  font-size: 18rpx;
  font-weight: 600;
}

.report-info {
  font-size: 20rpx;
  color: var(--text-muted);
}

.report-status {
  font-size: 22rpx;
  font-weight: 600;
}

.report-status-tag {
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  font-size: 18rpx;
  font-weight: 600;
  flex-shrink: 0;

  &.pending { background: rgba(148, 163, 184, 0.12); color: #94a3b8; }
  &.processing { background: rgba(251, 191, 36, 0.12); color: #fbbf24; }
  &.resolved { background: rgba(74, 222, 128, 0.12); color: #34d399; }
}

.report-arrow {
  font-size: 28rpx;
  color: var(--text-faint);
  flex-shrink: 0;
  margin-top: 6rpx;
}

.empty-report {
  text-align: center;
  padding: 28rpx;
  font-size: 24rpx;
  color: var(--text-muted);
}

.empty-report .link {
  color: #60a5fa;
}

// 空状态
.empty-task {
  text-align: center;
  padding: 40rpx;
  color: var(--text-muted);
  font-size: 24rpx;
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
  height: 400rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
  background: #e5e7eb;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.95); }
}

.clockin-coords {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-bottom: 8rpx;
}

.clockin-address {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-bottom: 16rpx;
}

.clockin-actions {
  display: flex;
  gap: 16rpx;
  width: 100%;
  margin-bottom: 16rpx;
}

.clockin-refresh,
.clockin-virtual {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.clockin-refresh {
  color: var(--primary);
  background: rgba(125, 211, 252, 0.1);
}

.clockin-virtual {
  color: var(--accent);
  background: rgba(168, 85, 247, 0.1);
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

// 亮色主题适配
.theme-light {
  background: #f8fafc !important;

  .page-home {
    background: #f8fafc !important;
  }

  .main-content {
    background: #f8fafc;
  }

  .top-section,
  .bottom-section {
    background: #f8fafc;
  }

  .stats-card {
    background: linear-gradient(145deg, #f1f5f9 0%, #e2e8f0 100%);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
    border: none;
  }

  .section {
    background: #f8fafc;
  }

  .quick-actions {
    background: #f8fafc;
  }

  .report-entrance {
    background: #ffffff;
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  .entrance-title {
    color: #0f172a;
  }

  .entrance-desc {
    color: #64748b;
  }

  .entrance-arrow {
    color: #94a3b8;
  }

  .stats-value,
  .stats-label,
  .stats-unit {
    color: #334155;
  }

  .stats-value.warning {
    color: #d97706;
  }

  .stats-value.success {
    color: #059669;
  }

  .stats-divider {
    background: rgba(0, 0, 0, 0.06);
  }

  .task-card,
  .record-item,
  .report-item {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .task-title,
  .record-title,
  .report-title {
    color: #0f172a;
  }

  .task-info,
  .record-info,
  .report-info,
  .task-location,
  .task-deadline,
  .task-type {
    color: #64748b;
  }

  .section-title,
  .section-more {
    color: #334155;
  }

  .section-more {
    color: #64748b;
  }

  .greeting-text {
    color: #0f172a;
  }

  .theme-toggle,
  .notification-btn {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
    color: #0f172a;
  }

  .icon {
    color: #0f172a;
  }

  .action-btn {
    background: #f1f5f9 !important;
    color: #64748b !important;
    border: none !important;
  }

  .empty-task {
    color: #64748b;
  }

  .modal-overlay {
    background: rgba(0, 0, 0, 0.3);
  }

  .clockin-modal {
    background: #ffffff;
  }

  .modal-title,
  .clockin-status {
    color: #0f172a;
  }

  .modal-close {
    color: #64748b;
  }

  .clockin-coords,
  .clockin-address {
    color: #64748b;
  }
}
</style>
