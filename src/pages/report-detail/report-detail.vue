<template>
  <view class="page-report-detail" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>
    
    <!-- 顶部区域 -->
    <view class="detail-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">上报详情</text>
    </view>
    
    <!-- 详情内容 -->
    <scroll-view class="detail-content" scroll-y>
      <!-- 状态卡片 -->
      <view class="status-card">
        <view class="status-icon" :class="report.statusClass">
          {{ report.statusIcon }}
        </view>
        <view class="status-info">
          <text class="status-text">{{ report.status }}</text>
          <text class="status-time">{{ report.time }}</text>
        </view>
      </view>
      
      <!-- 问题信息卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">📋</text>
          <text class="card-title">问题信息</text>
        </view>
        <view class="info-row">
          <text class="info-label">问题类型</text>
          <view class="category-badge" :style="{ background: getCategoryBg(report.category), color: getCategoryColor(report.category) }">
            {{ report.category }}
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">紧急程度</text>
          <view class="urgency-badge" :style="{ background: getUrgencyBg(report.urgency), color: getUrgencyColor(report.urgency) }">
            {{ report.urgency }}
          </view>
        </view>
        <view class="desc-section">
          <text class="desc-label">问题描述</text>
          <text class="desc-content">{{ report.description }}</text>
        </view>
      </view>
      
      <!-- 位置信息卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">📍</text>
          <text class="card-title">位置信息</text>
        </view>
        <view class="location-map">
          <map
            class="mini-map"
            :latitude="report.lat"
            :longitude="report.lng"
            :markers="[{ id: 1, latitude: report.lat, longitude: report.lng, iconPath: '/static/marker.png', width: 30, height: 40 }]"
            scale="16"
          ></map>
        </view>
        <text class="location-text">{{ report.location }}</text>
        <text class="coords-text">坐标：{{ report.lng.toFixed(6) }}, {{ report.lat.toFixed(6) }}</text>
      </view>
      
      <!-- 照片卡片 -->
      <view class="detail-card" v-if="report.photos.length > 0">
        <view class="card-header">
          <text class="card-icon">📷</text>
          <text class="card-title">现场照片</text>
          <text class="photo-count">{{ report.photos.length }} 张</text>
        </view>
        <view class="photo-grid">
          <image 
            class="photo-img" 
            v-for="(photo, index) in report.photos" 
            :key="index"
            :src="photo" 
            mode="aspectFill"
            @click="previewPhoto(index)"
          ></image>
        </view>
      </view>
      
      <!-- 处理进度卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">📊</text>
          <text class="card-title">处理进度</text>
        </view>
        <view class="progress-section">
          <view class="progress-item" :class="{ active: report.status === '待处理' || report.status === '处理中' || report.status === '已完成' }">
            <view class="progress-dot">1</view>
            <text class="progress-text">提交成功</text>
            <text class="progress-time">{{ report.time }}</text>
          </view>
          <view class="progress-line" :class="{ active: report.status === '处理中' || report.status === '已完成' }"></view>
          <view class="progress-item" :class="{ active: report.status === '处理中' || report.status === '已完成' }">
            <view class="progress-dot">2</view>
            <text class="progress-text">受理中</text>
            <text class="progress-time" v-if="report.status !== '待处理'">{{ report.acceptTime }}</text>
          </view>
          <view class="progress-line" :class="{ active: report.status === '已完成' }"></view>
          <view class="progress-item" :class="{ active: report.status === '已完成' }">
            <view class="progress-dot">3</view>
            <text class="progress-text">已完成</text>
            <text class="progress-time" v-if="report.status === '已完成'">{{ report.completeTime }}</text>
          </view>
        </view>
      </view>
      
      <!-- 处理结果卡片 -->
      <view class="detail-card" v-if="report.processResult">
        <view class="card-header">
          <text class="card-icon">✅</text>
          <text class="card-title">处理结果</text>
        </view>
        <text class="result-content">{{ report.processResult }}</text>
      </view>
      
      <!-- 上报信息 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">ℹ️</text>
          <text class="card-title">上报信息</text>
        </view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">上报编号</text>
            <text class="info-value">{{ report.id }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">上报时间</text>
            <text class="info-value">{{ report.time }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部操作 -->
    <view class="bottom-actions safe-area-bottom" v-if="report.status !== '已完成'">
      <view class="action-btn secondary" @click="viewOnMap">
        <text>🗺️</text>
        <text>地图</text>
      </view>
      <view class="action-btn primary" @click="addComment">
        <text>💬</text>
        <text>补充说明</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { getMobileReportDetail } from '@/utils/api'

// Store
const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)

// 上报详情数据
const report = ref({
  id: '',
  category: '',
  urgency: '',
  status: '',
  statusIcon: '',
  statusClass: '',
  time: '',
  acceptTime: '',
  completeTime: '',
  location: '',
  lat: 0,
  lng: 0,
  description: '',
  photos: [],
  processResult: ''
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 加载数据
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page?.options || {}
  const id = options.id

  if (id) {
    loadReportDetail(id)
  }
})

const loadReportDetail = async (id) => {
  try {
    const data = await getMobileReportDetail(id)
    if (!data) return

    const statusMap = { pending: '待处理', assigned: '已分配', processing: '处理中', completed: '已完成' }
    const statusClassMap = { pending: 'pending', assigned: 'pending', processing: 'processing', completed: 'completed' }
    const statusIconMap = { pending: '⏳', assigned: '⏳', processing: '🔄', completed: '✅' }
    const urgencyMap = { low: '轻微', medium: '一般', high: '紧急', critical: '非常紧急' }

    const status = statusMap[data.status] || '待处理'
    const photos = data.photos ? (Array.isArray(data.photos) ? data.photos : JSON.parse(data.photos || '[]')) : []

    report.value = {
      id: data.id,
      category: data.reservoirName || data.address || '问题上报',
      urgency: urgencyMap[data.severity] || '一般',
      status: status,
      statusIcon: statusIconMap[data.status] || '⏳',
      statusClass: statusClassMap[data.status] || 'pending',
      time: data.createdAt ? new Date(data.createdAt).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-') : '',
      acceptTime: data.assignmentTime ? new Date(data.assignmentTime).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-') : '',
      completeTime: data.completionTime ? new Date(data.completionTime).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-') : '',
      location: data.address || '',
      lat: data.latitude ? parseFloat(data.latitude) : 0,
      lng: data.longitude ? parseFloat(data.longitude) : 0,
      description: data.description || '',
      photos: photos,
      processResult: data.processingResult || data.notes || ''
    }
  } catch (e) {
    console.error('加载上报详情失败', e)
  }
}

// 预览照片
const previewPhoto = (index) => {
  if (report.value.photos.length > 0) {
    uni.previewImage({
      urls: report.value.photos,
      current: index
    })
  }
}

// 地图
const viewOnMap = () => {
  uni.openLocation({
    latitude: report.value.lat,
    longitude: report.value.lng,
    name: report.value.location
  })
}

// 补充说明
const addComment = () => {
  uni.showModal({
    title: '补充说明',
    editable: true,
    placeholderText: '请输入补充内容...',
    success: (res) => {
      if (res.confirm && res.content) {
        showToast('补充说明已提交')
      }
    }
  })
}

// 辅助方法
const getCategoryBg = (category) => {
  const sev = (report.value.urgency || '').replace('非常紧急', '').replace('紧急', '').replace('一般', '').replace('轻微', '')
  switch (report.value.urgency) {
    case '非常紧急': return 'rgba(239, 68, 68, 0.15)'
    case '紧急': return 'rgba(249, 115, 22, 0.15)'
    case '一般': return 'rgba(245, 158, 11, 0.15)'
    case '轻微': return 'rgba(59, 130, 246, 0.15)'
    default: return 'rgba(100, 116, 139, 0.15)'
  }
}

const getCategoryColor = (category) => {
  switch (report.value.urgency) {
    case '非常紧急': return '#ef4444'
    case '紧急': return '#f97316'
    case '一般': return '#f59e0b'
    case '轻微': return '#3b82f6'
    default: return '#64748b'
  }
}

const getUrgencyBg = (urgency) => {
  switch (urgency) {
    case '非常紧急': return 'rgba(239, 68, 68, 0.15)'
    case '紧急': return 'rgba(249, 115, 22, 0.15)'
    case '一般': return 'rgba(245, 158, 11, 0.15)'
    case '轻微': return 'rgba(59, 130, 246, 0.15)'
    default: return 'rgba(100, 116, 139, 0.15)'
  }
}

const getUrgencyColor = (urgency) => {
  switch (urgency) {
    case '非常紧急': return '#ef4444'
    case '紧急': return '#f97316'
    case '一般': return '#f59e0b'
    case '轻微': return '#3b82f6'
    default: return '#64748b'
  }
}
</script>

<style lang="scss" scoped>
.page-report-detail {
  min-height: 100vh;
  background: var(--bg-screen);
  color: var(--text-body);
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域
.detail-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  background: var(--bg-card);
  position: sticky;
  top: 0;
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
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text);
}

// 详情内容
.detail-content {
  flex: 1;
  padding: 24rpx 32rpx 140rpx;
}

// 状态卡片
.status-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 24rpx;
  margin-bottom: 24rpx;
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  
  &.pending {
    background: rgba(148, 163, 184, 0.15);
  }
  
  &.processing {
    background: rgba(59, 130, 246, 0.15);
    animation: pulse 2s ease-in-out infinite;
  }
  
  &.completed {
    background: rgba(74, 222, 128, 0.15);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-text {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text);
}

.status-time {
  font-size: 24rpx;
  color: var(--text-muted);
}

// 详情卡片
.detail-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.card-icon {
  font-size: 32rpx;
}

.card-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text);
}

.photo-count {
  font-size: 24rpx;
  color: var(--text-muted);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1px solid var(--border);
}

.info-label {
  font-size: 26rpx;
  color: var(--text-muted);
}

.category-badge,
.urgency-badge {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.desc-section {
  margin-top: 20rpx;
}

.desc-label {
  font-size: 22rpx;
  color: var(--text-faint);
  display: block;
  margin-bottom: 12rpx;
}

.desc-content {
  font-size: 26rpx;
  color: var(--text-body);
  line-height: 1.6;
}

// 位置信息
.location-map {
  width: 100%;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.mini-map {
  width: 100%;
  height: 100%;
}

.location-text {
  font-size: 26rpx;
  color: var(--text-body);
  display: block;
  margin-bottom: 8rpx;
}

.coords-text {
  font-size: 22rpx;
  color: var(--text-faint);
}

// 照片
.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.photo-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12rpx;
}

// 处理进度
.progress-section {
  display: flex;
  align-items: flex-start;
}

.progress-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0.4;
  
  &.active {
    opacity: 1;
    
    .progress-dot {
      background: var(--primary);
      border-color: var(--primary);
    }
  }
}

.progress-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: var(--bg-panel);
  border: 4rpx solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #fff;
  margin-bottom: 12rpx;
}

.progress-text {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6rpx;
}

.progress-time {
  font-size: 20rpx;
  color: var(--text-muted);
}

.progress-line {
  flex: 1;
  height: 4rpx;
  background: var(--border);
  margin-top: 18rpx;
  
  &.active {
    background: var(--primary);
  }
}

// 处理结果
.result-content {
  font-size: 26rpx;
  color: var(--text-body);
  line-height: 1.8;
  white-space: pre-line;
}

// 上报信息
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-label {
  font-size: 22rpx;
  color: var(--text-faint);
}

.info-value {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-body);
}

// 底部操作
.bottom-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  background: var(--bg-card);
  border-top: 1px solid var(--border);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  
  &.secondary {
    background: var(--bg-panel);
    color: var(--text-body);
  }
  
  &.primary {
    background: linear-gradient(155deg, #38bdf8 0%, #2563eb 72%);
    color: #fff;
  }
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .detail-header,
  .status-card,
  .detail-card,
  .bottom-actions {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .detail-title,
  .section-title {
    color: #1e293b;
  }

  .section-content {
    color: #64748b;
  }

  .status-text {
    color: #64748b;
  }
}
</style>
