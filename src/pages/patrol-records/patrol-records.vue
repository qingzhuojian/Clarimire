<template>
  <view class="page-patrol-records" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="page-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">巡查记录</text>
      <view class="header-right"></view>
    </view>

    <!-- 统计概览 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-value">{{ totalCount }}</text>
        <text class="stat-label">全部</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ completedCount }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ pendingCount }}</text>
        <text class="stat-label">待处理</text>
      </view>
    </view>

    <!-- Tab 切换 -->
    <view class="category-tabs">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >
        全部
        <span class="tab-count">{{ totalCount }}</span>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'completed' }"
        @click="switchTab('completed')"
      >
        已完成
        <span class="tab-count">{{ completedCount }}</span>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'pending' }"
        @click="switchTab('pending')"
      >
        待处理
        <span class="tab-count">{{ pendingCount }}</span>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view
      class="record-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="record-item"
        v-for="record in filteredRecords"
        :key="record.id"
        @click="viewRecordDetail(record)"
      >
        <view class="record-card">
          <!-- 状态指示 -->
          <view class="record-status-bar" :style="{ background: getStatusColor(record.status) }"></view>

          <!-- 记录头部 -->
          <view class="record-header">
            <view class="record-info">
              <text class="record-location">📍 {{ record.address || record.reservoirName || '未知地点' }}</text>
              <text class="record-time">⏰ {{ formatTime(record.createdAt) }}</text>
            </view>
            <view class="record-badge" :style="{ color: getStatusColor(record.status), background: getStatusBg(record.status) }">
              {{ getStatusText(record.status) }}
            </view>
          </view>

          <!-- 巡查人员 -->
          <view class="record-body">
            <text class="record-inspector">👤 {{ record.inspector || '未知巡查员' }}</text>
            <text v-if="record.hasIssue" class="record-issue">⚠️ {{ record.issueType || '有问题' }}</text>
            <text v-else class="record-normal">✅ 无异常</text>
          </view>

          <!-- 描述 -->
          <text v-if="record.description" class="record-desc">{{ record.description }}</text>

          <!-- 操作按钮 -->
          <view class="record-actions">
            <view class="action-btn" @click.stop="navigateToLocation(record)">
              <text>🗺️</text> 导航
            </view>
            <view class="action-btn primary" @click.stop="viewRecordDetail(record)">
              <text>📋</text> 查看详情
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="filteredRecords.length === 0 && !isLoading" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无记录</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-more">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { getMobilePatrolRecords } from '@/utils/api'

// Store
const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)

// 响应式数据
const currentTab = ref('all')
const records = ref([])
const isLoading = ref(false)
const isRefreshing = ref(false)
const totalCount = ref(0)
const completedCount = ref(0)
const pendingCount = ref(0)

// 计算属性
const filteredRecords = computed(() => {
  if (currentTab.value === 'all') return records.value
  return records.value.filter(r => r.status === currentTab.value)
})

// 初始化
onMounted(() => {
  loadRecords()
})

const loadRecords = async () => {
  isLoading.value = true
  try {
    const res = await getMobilePatrolRecords({ page: 1, pageSize: 50 })
    let list = []
    if (Array.isArray(res)) {
      list = res
    } else if (res?.list && Array.isArray(res.list)) {
      list = res.list
    }
    records.value = list
    totalCount.value = list.length
    completedCount.value = list.filter(r => r.status === 'completed').length
    pendingCount.value = list.filter(r => r.status !== 'completed').length
  } catch (e) {
    console.error('加载巡查记录失败', e)
    records.value = []
  } finally {
    isLoading.value = false
  }
}

const onRefresh = async () => {
  isRefreshing.value = true
  await loadRecords()
  isRefreshing.value = false
  showToast('刷新成功')
}

const loadMore = () => {}

const switchTab = (tab) => {
  currentTab.value = tab
}

const viewRecordDetail = (record) => {
  uni.navigateTo({ url: `/pages/inspection-detail/inspection-detail?id=${record.id}&type=patrol` })
}

const navigateToLocation = (record) => {
  if (record.latitude && record.longitude) {
    uni.openLocation({
      latitude: parseFloat(record.latitude),
      longitude: parseFloat(record.longitude),
      name: record.address || record.reservoirName || '巡查地点',
      fail: () => showToast('打开导航失败')
    })
  } else {
    showToast('暂无位置信息')
  }
}

const formatTime = (isoStr) => {
  if (!isoStr) return '-'
  const d = new Date(isoStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const getStatusColor = (status) => {
  switch (status) {
    case 'completed': return '#4ade80'
    case 'pending': return '#f87171'
    default: return '#60a5fa'
  }
}

const getStatusBg = (status) => {
  switch (status) {
    case 'completed': return 'rgba(74, 222, 128, 0.15)'
    case 'pending': return 'rgba(248, 113, 113, 0.15)'
    default: return 'rgba(96, 165, 250, 0.15)'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'pending': return '待处理'
    default: return '进行中'
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.status-bar {
  height: 44px;
}

.page-header {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  background: var(--bg-card);
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: var(--text-body);
}

.page-title {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--text-title);
}

.header-right {
  width: 60rpx;
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 24rpx;
  padding: 28rpx 32rpx;
  background: var(--bg-card);
  border-radius: 16rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-title);
}

.stat-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-top: 6rpx;
}

.stat-divider {
  width: 1px;
  height: 60rpx;
  background: var(--border-color);
}

.category-tabs {
  display: flex;
  padding: 0 24rpx 16rpx;
  gap: 16rpx;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 28rpx;
  color: var(--text-secondary);
  border-radius: 40rpx;
  background: var(--bg-card);
  transition: all 0.2s;

  &.active {
    color: #3b82f6;
    background: rgba(59, 130, 246, 0.1);
  }
}

.tab-count {
  font-size: 22rpx;
  padding: 2rpx 8rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 20rpx;
}

.record-list {
  flex: 1;
  padding: 0 24rpx 24rpx;
}

.record-item {
  margin-bottom: 24rpx;
}

.record-card {
  background: var(--bg-card);
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
}

.record-status-bar {
  height: 6rpx;
  width: 100%;
}

.record-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24rpx 24rpx 0;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
}

.record-location {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-title);
}

.record-time {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.record-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.record-body {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
}

.record-inspector {
  font-size: 26rpx;
  color: var(--text-body);
}

.record-issue {
  font-size: 26rpx;
  color: #f87171;
}

.record-normal {
  font-size: 26rpx;
  color: #4ade80;
}

.record-desc {
  display: block;
  padding: 0 24rpx 16rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.record-actions {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx 24rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
  background: var(--bg-screen);
  border-radius: 8rpx;

  &.primary {
    color: #3b82f6;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.empty-icon {
  font-size: 80rpx;
}

.empty-text {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.loading-more {
  text-align: center;
  padding: 32rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
}
</style>
