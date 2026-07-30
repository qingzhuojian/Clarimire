<template>
  <view class="page-inspection-detail" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>
    
    <!-- 顶部区域 -->
    <view class="detail-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">巡查详情</text>
    </view>
    
    <!-- 详情内容 -->
    <scroll-view class="detail-content" scroll-y>
      <!-- 基本信息卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">📋</text>
          <text class="card-title">基本信息</text>
        </view>
        <view class="card-grid">
          <view class="grid-item">
            <text class="grid-label">巡查编号</text>
            <text class="grid-value">{{ detail.id }}</text>
          </view>
          <view class="grid-item">
            <text class="grid-label">巡查人员</text>
            <text class="grid-value">{{ detail.inspector || '-' }}</text>
          </view>
          <view class="grid-item">
            <text class="grid-label">巡查地点</text>
            <text class="grid-value">{{ detail.location || '-' }}</text>
          </view>
          <view class="grid-item">
            <text class="grid-label">巡查类型</text>
            <text class="grid-value">{{ detail.type || '常规巡查' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 问题信息卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">⚠️</text>
          <text class="card-title">问题信息</text>
        </view>
        <view class="info-row">
          <text class="info-label">问题类型</text>
          <text class="info-value">{{ detail.problemType }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">严重程度</text>
          <view class="severity-badge" :style="{ background: getSeverityBg(detail.severity), color: getSeverityColor(detail.severity) }">
            {{ detail.severity }}
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">处理状态</text>
          <view class="status-badge" :style="{ background: getStatusBg(formatStatus(detail.status)), color: getStatusColor(formatStatus(detail.status)) }">
            {{ formatStatus(detail.status) }}
          </view>
        </view>
        <view class="desc-section">
          <text class="desc-label">问题描述</text>
          <text class="desc-content">{{ detail.description }}</text>
        </view>
        <view class="tags-section">
          <text class="desc-label">标签</text>
          <view class="tags-list">
            <text class="tag" v-for="tag in detail.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>
      </view>
      
      <!-- 现场照片卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">📷</text>
          <text class="card-title">现场照片</text>
          <text class="photo-count">{{ detail.photos.length }} 张</text>
        </view>
        <scroll-view class="photo-scroll" scroll-x>
          <view class="photo-list">
            <view class="photo-item" v-for="(photo, index) in detail.photos" :key="index">
              <image class="photo-img" :src="photo" mode="aspectFill" @click="previewPhoto(index)"></image>
            </view>
            <view v-if="detail.photos.length === 0" class="no-photo">
              <text>暂无照片</text>
            </view>
          </view>
        </scroll-view>
      </view>
      
      <!-- 处理记录卡片 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-icon">✅</text>
          <text class="card-title">处理记录</text>
        </view>
        <view class="card-grid">
          <view class="grid-item">
            <text class="grid-label">处理人员</text>
            <text class="grid-value">{{ detail.processor }}</text>
          </view>
          <view class="grid-item">
            <text class="grid-label">处理时间</text>
            <text class="grid-value">{{ detail.processTime }}</text>
          </view>
        </view>
        <view class="desc-section">
          <text class="desc-label">处理结果</text>
          <text class="desc-content">{{ detail.result }}</text>
        </view>
      </view>
      
      <!-- 时间线 -->
      <view class="timeline-card">
        <view class="card-header">
          <text class="card-icon">📅</text>
          <text class="card-title">操作时间线</text>
        </view>
        <view class="timeline">
          <view class="timeline-item" v-for="(item, index) in detail.timeline" :key="index" :class="{ completed: item.completed }">
            <view class="timeline-dot"></view>
            <view class="timeline-content">
              <text class="timeline-title">{{ item.title }}</text>
              <text class="timeline-time">{{ item.time }}</text>
              <text class="timeline-desc" v-if="item.desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部操作 -->
    <view class="bottom-actions safe-area-bottom">
      <view class="action-btn" @click="navigateToLocation">
        <text>🗺️</text>
        <text>导航</text>
      </view>
      <view class="action-btn primary" @click="clockIn">
        <text>✓</text>
        <text>打卡</text>
      </view>
      <view class="action-btn" @click="reportIssue">
        <text>📝</text>
        <text>上报问题</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { getMobileTaskDetail, getMobilePatrolRecordDetail } from '@/utils/api'

// Store
const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)

// 详情数据
const detail = ref({
  id: '',
  inspector: '',
  location: '',
  type: '',
  problemType: '无',
  severity: '一般',
  status: 'pending',
  description: '',
  tags: [],
  photos: [],
  processor: '',
  processTime: '',
  result: '',
  timeline: []
})

// 计算严重程度
const computeSeverity = (deadline) => {
  if (!deadline) return '一般'
  const now = Date.now()
  const dl = new Date(deadline).getTime()
  const hoursLeft = (dl - now) / 3600000
  if (hoursLeft < 0) return '严重'
  if (hoursLeft < 12) return '严重'
  if (hoursLeft < 48) return '一般'
  return '轻微'
}

// 格式化状态文本
const formatStatus = (status) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'processing': return '进行中'
    case 'completed': return '已完成'
    default: return status
  }
}

// 格式化时间
const formatTime = (isoStr) => {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// 生成时间线
const buildTimeline = (task) => {
  const now = new Date()
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  const created = task.createdAt ? new Date(task.createdAt) : now
  const deadline = task.deadline ? new Date(task.deadline) : null

  const steps = [
    { title: '任务创建', desc: '系统自动创建巡查任务', completed: true },
    { title: '任务派发', desc: `派发给 ${task.assigneeName || '巡查员'}`, completed: true },
    { title: '现场巡查', desc: '巡查员到达现场开始巡查', completed: task.status !== 'pending' },
    { title: '巡查完成', desc: '提交巡查结果', completed: task.status === 'completed' },
    { title: '任务完成', desc: deadline ? `截止 ${formatTime(task.deadline)}` : '任务审核通过', completed: task.status === 'completed' }
  ]
  return steps
}

// 加载详情
const loadDetail = async (id) => {
  try {
    const task = await getMobileTaskDetail(id)
    if (!task) return

    const severity = computeSeverity(task.deadline)
    detail.value = {
      id: task.id,
      inspector: task.assigneeName || task.creatorName || '',
      location: task.reservoirName || '',
      type: task.description || '常规巡查',
      problemType: '无',
      severity,
      status: task.status,
      description: task.description || '',
      tags: [],
      photos: [],
      processor: task.assigneeName || '',
      processTime: task.updatedAt ? formatTime(task.updatedAt) : '',
      result: task.status === 'completed' ? '任务已完成' : '',
      timeline: buildTimeline(task)
    }
  } catch (e) {
    console.error('加载任务详情失败', e)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 预览照片
const previewPhoto = (index) => {
  if (detail.value.photos.length > 0) {
    uni.previewImage({
      urls: detail.value.photos,
      current: index
    })
  }
}

// 导航
const navigateToLocation = () => {
  if (detail.value.location) {
    uni.openLocation({
      latitude: 40.0,
      longitude: 116.0,
      name: detail.value.location,
      fail: () => showToast('打开导航失败')
    })
  } else {
    showToast('暂无位置信息')
  }
}

// 打卡
const clockIn = () => {
  uni.navigateTo({ url: '/pages/map/map' })
}

// 上报问题
const reportIssue = () => {
  uni.navigateTo({ url: '/pages/report/report' })
}

// 辅助方法
const getSeverityBg = (severity) => {
  switch (severity) {
    case '严重': return 'rgba(248, 113, 113, 0.15)'
    case '一般': return 'rgba(232, 184, 106, 0.15)'
    default: return 'rgba(74, 222, 128, 0.15)'
  }
}

const getSeverityColor = (severity) => {
  switch (severity) {
    case '严重': return '#f87171'
    case '一般': return '#e8b86a'
    default: return '#4ade80'
  }
}

const getStatusBg = (status) => {
  switch (status) {
    case '已完成': return 'rgba(74, 222, 128, 0.15)'
    case '进行中': return 'rgba(96, 165, 250, 0.15)'
    default: return 'rgba(148, 163, 184, 0.15)'
  }
}

const getStatusColor = (status) => {
  switch (status) {
    case '已完成': return '#4ade80'
    case '进行中': return '#60a5fa'
    default: return '#94a3b8'
  }
}

// 加载巡查记录详情
const loadPatrolDetail = async (id) => {
  try {
    const record = await getMobilePatrolRecordDetail(id)
    if (!record) return

    const now = new Date()
    const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
    const createdAt = record.createdAt ? new Date(record.createdAt) : now
    const timeline = [
      { title: '巡查开始', desc: record.address || record.reservoirName || '现场巡查', completed: true },
      { title: '巡查完成', desc: `巡查员：${record.inspector || '未知'}`, completed: record.status === 'completed' },
      { title: '问题上报', desc: record.hasIssue ? `${record.issueType || '发现问题'}` : '无问题', completed: record.status === 'completed' },
      { title: '任务结束', desc: fmt(createdAt), completed: record.status === 'completed' }
    ]

    detail.value = {
      id: record.id,
      inspector: record.inspector || record.reporterName || record.assignedInspector || '未知巡查员',
      location: record.address || record.reservoirName || record.assignmentNote || '未知地点',
      type: record.issueType || record.situationDescription || '常规巡查',
      problemType: record.hasIssue ? (record.issueType || '有问题') : '无',
      severity: record.issueSeverity === 'high' || record.issueSeverity === 'medium' ? '严重' : '一般',
      status: record.status === 'completed' ? 'completed' : 'processing',
      description: record.description || (record.hasIssue ? `${record.issueType || '发现问题'}：${record.description || ''}` : '无异常情况'),
      tags: [],
      photos: [],
      processor: record.inspector || '',
      processTime: record.createdAt ? formatTime(record.createdAt) : '',
      result: record.status === 'completed' ? '巡查完成' : '巡查中',
      timeline
    }
  } catch (e) {
    console.error('加载巡查记录详情失败', e)
  }
}

// 生命周期 - 获取页面参数
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  const id = options.id || options.taskId
  if (!id) return
  if (options.type === 'patrol') {
    loadPatrolDetail(id)
  } else {
    loadDetail(id)
  }
})
</script>

<style lang="scss" scoped>
.page-inspection-detail {
  height: 100%;
  background: var(--bg-screen);
  color: var(--text-body);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
  flex-shrink: 0;
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
  flex-shrink: 0;
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
  min-height: 0;
  padding: 24rpx 32rpx;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

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

.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.grid-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.grid-label {
  font-size: 22rpx;
  color: var(--text-faint);
}

.grid-value {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-body);
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

.info-value {
  font-size: 26rpx;
  color: var(--text-body);
}

.severity-badge,
.status-badge {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.desc-section,
.tags-section {
  margin-top: 20rpx;
}

.desc-label,
.tags-label {
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

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  background: var(--bg-panel);
  font-size: 22rpx;
  color: var(--text-muted);
}

// 照片
.photo-scroll {
  width: 100%;
}

.photo-list {
  display: flex;
  gap: 16rpx;
}

.photo-item {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.photo-img {
  width: 100%;
  height: 100%;
}

.no-photo {
  padding: 48rpx;
  text-align: center;
  color: var(--text-muted);
  font-size: 26rpx;
}

// 时间线
.timeline-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.timeline {
  padding-left: 16rpx;
}

.timeline-item {
  position: relative;
  padding-left: 40rpx;
  padding-bottom: 32rpx;
  
  &:last-child {
    padding-bottom: 0;
  }
  
  .timeline-dot {
    position: absolute;
    left: 0;
    top: 8rpx;
    width: 20rpx;
    height: 20rpx;
    border-radius: 50%;
    background: var(--bg-panel);
    border: 4rpx solid var(--border);
  }
  
  &.completed .timeline-dot {
    background: var(--primary);
    border-color: var(--primary);
  }
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.timeline-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text);
}

.timeline-time {
  font-size: 22rpx;
  color: var(--text-muted);
}

.timeline-desc {
  font-size: 22rpx;
  color: var(--text-faint);
  margin-top: 4rpx;
}

// 底部操作
.bottom-actions {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 0;
  background: var(--bg-panel);
  border-radius: 20rpx;
  font-size: 22rpx;
  color: var(--text-muted);
  
  &.primary {
    background: linear-gradient(155deg, #38bdf8 0%, #2563eb 72%);
    color: #fff;
  }
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .detail-header,
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

  .timeline-item .timeline-dot {
    background: #e2e8f0;
  }

  .timeline-content {
    color: #64748b;
  }
}
</style>
