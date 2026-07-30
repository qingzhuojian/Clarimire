<template>
  <view class="page-my-reports" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="reports-header">
      <view class="header-left">
        <view class="back-btn" @click="goBack">
          <text>←</text>
        </view>
        <view class="header-titles">
          <text class="page-title">我的上报</text>
        </view>
      </view>
      <view class="header-right">
        <view class="theme-toggle" @click="toggleTheme">
          <text>{{ theme === 'dark' ? '🌙' : '☀️' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <scroll-view class="category-tabs" scroll-x="true">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >
        全部
        <span class="tab-count">{{ reports.length }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'pending' }"
        @click="switchTab('pending')"
      >
        待处理
        <span class="tab-count">{{ countByStatus('pending') }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'processing' }"
        @click="switchTab('processing')"
      >
        处理中
        <span class="tab-count">{{ countByStatus('processing') + countByStatus('assigned') }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'completed' }"
        @click="switchTab('completed')"
      >
        已完成
        <span class="tab-count">{{ countByStatus('completed') }}</span>
      </view>
    </scroll-view>
    
    <!-- 上报列表 -->
    <scroll-view 
      class="report-list" 
      scroll-y 
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view 
        class="report-item" 
        v-for="report in filteredReports" 
        :key="report.id"
        @click="viewDetail(report)"
      >
        <view class="report-card">
          <!-- 卡片头部 -->
          <view class="report-header">
            <view class="report-priority" :style="{ background: getCategoryBg(report.severity), color: getCategoryColor(report.severity) }">
              {{ getUrgencyName(report.severity) }}
            </view>
            <view class="report-status" :style="{ color: getStatusColor(report.status) }">
              {{ getStatusText(report.status) }}
            </view>
          </view>
          
          <!-- 问题描述 -->
          <text class="report-title">{{ report.description }}</text>
          
          <!-- 位置信息 -->
          <view class="report-meta">
            <view class="meta-item">
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ report.reservoirName || report.address || '未知地点' }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">🕐</text>
              <text class="meta-text">{{ formatTime(report.createdAt) }}</text>
            </view>
          </view>
          
          <!-- 处理结果（如果有） -->
          <view class="report-result" v-if="report.processingResult">
            <text class="result-label">处理结果：</text>
            <text class="result-text">{{ report.processingResult }}</text>
          </view>
          
          <!-- 操作按钮 -->
          <view class="report-actions">
            <view class="action-btn" @click.stop="deleteReport(report)">
              <text>🗑️</text> 删除
            </view>
            <view class="action-btn primary" @click.stop="viewDetail(report)">
              <text>📋</text> 查看详情
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredReports.length === 0" class="empty-state">
        <text class="empty-icon">{{ currentTab === 'all' ? '📋' : currentTab === 'pending' ? '⏳' : currentTab === 'processing' ? '🔧' : '✅' }}</text>
        <text class="empty-text">{{ getEmptyTitle() }}</text>
        <button class="btn-report-now" @click="goToReport" v-if="currentTab === 'all'">
          立即上报
        </button>
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
import { onShow } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { showToast, showConfirm } from '@/utils/helper'
import { getMyMobileReports } from '@/utils/api'

const themeStore = useThemeStore()
const userStore = useUserStore()
const theme = computed(() => themeStore.theme)
const userRole = computed(() => userStore.userRole || 'public')

const currentTab = ref('all')
const isRefreshing = ref(false)
const isLoading = ref(false)
// 页面加载时直接从本地存储读取，避免异步延迟
const reports = ref(uni.getStorageSync('localReports') || [])

const loadReports = async () => {
  try {
    isLoading.value = true
    const role = userStore.userRole || 'public'
    const savedUser = uni.getStorageSync('envInspectionUser') || {}
    const username = userStore.currentUser?.username || savedUser.username ||
      (role === 'public' ? 'public1' : role === 'inspector' ? 'inspector1' : '')

    // 加载 API 数据
    const res = await getMyMobileReports(username)

    // 加载本地记录（本地存储只有当前设备的数据，不需要按 reporter 筛选）
    const localReports = uni.getStorageSync('localReports') || []
    console.log('【DEBUG】localReports 原始数据:', JSON.stringify(localReports))
    console.log('加载本地记录数量:', localReports.length, 'API用户名:', username, '角色:', role)

    const apiReports = Array.isArray(res) ? res : (res?.list || [])
    console.log('API返回记录数量:', apiReports.length)
    // 显示调试信息
    if (localReports.length === 0) {
      showToast('本地无新上报记录，请先提交')
    } else {
      showToast(`本地有${localReports.length}条新记录`)
    }

    // 合并时按时间倒序
    const merged = [...localReports, ...apiReports].sort((a, b) => {
      const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0
      const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0
      return tb - ta
    })
    reports.value = merged
    showToast(`加载完成：本地${localReports.length}条，API${apiReports.length}条`)
  } catch (e) {
    console.error('加载上报记录失败', e)
    reports.value = []
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadReports()
})

onShow(() => {
  // 每次页面显示时刷新数据，确保刚上报的记录能立即看到
  console.log('onShow 触发，开始加载...')
  loadReports()
})

const filteredReports = computed(() => {
  if (currentTab.value === 'all') return reports.value
  if (currentTab.value === 'processing') {
    return reports.value.filter(r => r.status === 'processing' || r.status === 'assigned')
  }
  return reports.value.filter(r => r.status === currentTab.value)
})

const countByStatus = (status) => {
  if (status === 'processing') {
    return reports.value.filter(r => r.status === 'processing' || r.status === 'assigned').length
  }
  return reports.value.filter(r => r.status === status).length
}

const switchTab = (tab) => { currentTab.value = tab }

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const onRefresh = async () => {
  isRefreshing.value = true
  await loadReports()
  isRefreshing.value = false
  showToast('刷新成功')
}

const deleteReport = async (report) => {
  showToast('删除功能已禁用')
}

const goBack = () => uni.navigateBack()
const goToReport = () => uni.navigateTo({ url: '/pages/report/report' })
const viewDetail = (report) => uni.navigateTo({ url: `/pages/report-detail/report-detail?id=${report.id}` })

const formatTime = (isoStr) => {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const getStatusColor = (status) => {
  switch (status) {
    case 'completed': return '#4ade80'
    case 'processing': return '#60a5fa'
    case 'assigned': return '#60a5fa'
    default: return '#94a3b8'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'processing': return '处理中'
    case 'assigned': return '已分配'
    default: return '待处理'
  }
}

const getCategoryBg = (sev) => {
  switch (sev) {
    case 'critical': return 'rgba(248, 113, 113, 0.15)'
    case 'high': return 'rgba(249, 115, 22, 0.15)'
    case 'medium': return 'rgba(232, 184, 106, 0.15)'
    case 'low': return 'rgba(59, 130, 246, 0.15)'
    default: return 'rgba(148, 163, 184, 0.12)'
  }
}

const getCategoryColor = (sev) => {
  switch (sev) {
    case 'critical': return '#f87171'
    case 'high': return '#f97316'
    case 'medium': return '#e8b86a'
    case 'low': return '#3b82f6'
    default: return '#94a3b8'
  }
}

const getUrgencyName = (sev) => {
  switch (sev) {
    case 'critical': return '非常紧急'
    case 'high': return '紧急'
    case 'medium': return '一般'
    case 'low': return '轻微'
    default: return '待定'
  }
}

const getEmptyTitle = () => {
  switch (currentTab.value) {
    case 'pending': return '暂无待处理的上报'
    case 'processing': return '暂无处理中的上报'
    case 'completed': return '暂无已完成的上报'
    default: return '暂无上报记录'
  }
}
</script>

<style lang="scss" scoped>
.page-my-reports {
  min-height: 100%;
  background: var(--bg-screen);
  color: var(--text-body);
  position: relative;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
  flex-shrink: 0;
}

// 顶部区域
.reports-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16rpx 32rpx 12rpx;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-titles {
  display: flex;
  flex-direction: column;
}

.back-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  flex-shrink: 0;
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text);
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

// 分类标签
.category-tabs {
  flex-shrink: 0;
  display: flex;
  flex-direction: row;
  padding: 0 32rpx 16rpx;
  white-space: nowrap;
  overflow-x: auto;
  
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tab-item {
  display: inline-block;
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 40rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  font-size: 24rpx;
  color: var(--text-muted);
  vertical-align: middle;
  margin-right: 8rpx;
  
  &:last-child {
    margin-right: 0;
  }
  
  &.active {
    background: var(--primary-dim);
    border-color: var(--primary);
    color: var(--primary);
  }
}

.tab-count {
  display: inline;
  background: rgba(255, 255, 255, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
  font-size: 18rpx;
  color: var(--text-muted);
  margin-left: 8rpx;
  
  .active & {
    background: var(--primary);
    color: #fff;
  }
}

// 上报列表
.report-list {
  height: calc(100vh - 280rpx);
  padding: 0 32rpx;
  background: var(--bg-screen);
  overflow-y: auto;
}

.report-item {
  margin-bottom: 12rpx;
  overflow: visible;
}

.report-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  padding: 20rpx;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.report-priority {
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 20rpx;
  font-weight: 600;
}

.report-status {
  font-size: 22rpx;
  font-weight: 600;
}

.report-title {
  font-size: 26rpx;
  font-weight: 700;
  color: var(--text);
  display: block;
  margin-bottom: 8rpx;
  line-height: 1.5;
}

.report-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  margin-bottom: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.meta-icon {
  font-size: 24rpx;
  color: #2563eb;
}

.meta-text {
  font-size: 24rpx;
  color: var(--text-muted);
}

.report-result {
  background: rgba(74, 222, 128, 0.06);
  border: 1px solid rgba(74, 222, 128, 0.15);
  border-radius: 12rpx;
  padding: 12rpx 16rpx;
  margin-bottom: 8rpx;
}

.result-label {
  font-size: 22rpx;
  color: #4ade80;
  font-weight: 600;
}

.result-text {
  font-size: 22rpx;
  color: var(--text-body);
}

.report-actions {
  display: flex;
  gap: 12rpx;
  padding-top: 12rpx;
  border-top: 1px solid var(--border);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 12rpx 16rpx;
  border-radius: 12rpx;
  background: var(--bg-panel);
  font-size: 24rpx;
  color: var(--text-muted);
  
  &.primary {
    background: linear-gradient(155deg, #38bdf8 0%, #2563eb 72%);
    color: #fff;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: var(--text-muted);
  margin-bottom: 40rpx;
}

.loading-more {
  text-align: center;
  padding: 32rpx;
  color: var(--text-muted);
  font-size: 24rpx;
}

.btn-report-now {
  padding: 20rpx 60rpx;
  background: linear-gradient(155deg, #38bdf8 0%, #2563eb 72%);
  color: #fff;
  border: none;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 700;
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .back-btn {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  }

  .page-title {
    color: #1e293b;
  }

  .theme-toggle {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
    color: #1e293b;
  }

  .report-list {
    background: #ffffff !important;
  }

  .category-tabs {
    background: #ffffff;
  }

  .tab-item {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
    color: #64748b;
    &.active {
      color: #0284c7;
    }
  }

  .report-card {
    background: #ffffff;
    box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .action-btn {
    background: #f8fafc !important;
    color: #64748b !important;
  }

  .action-btn.primary {
    background: linear-gradient(155deg, #38bdf8 0%, #0284c7 72%) !important;
    color: #ffffff !important;
  }

  .report-result {
    background: rgba(2, 132, 199, 0.06) !important;
    border-color: rgba(2, 132, 199, 0.15) !important;
  }

  .report-actions {
    border-top-color: rgba(0, 0, 0, 0.06) !important;
  }

  .report-title {
    color: #1e293b;
  }

  .empty-state {
    color: #64748b;
  }

  .empty-icon {
    opacity: 0.5;
  }
}
</style>
