<template>
  <view class="page-tasks" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="tasks-header">
      <view class="header-left">
        <view class="back-btn" @click="goBack">
          <text>←</text>
        </view>
        <view class="header-titles">
          <text class="page-title">巡查任务</text>
          <text class="page-subtitle">{{ subtitle }}</text>
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
        <span class="tab-count">{{ getCountByStatus('all') }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'pending' }"
        @click="switchTab('pending')"
      >
        待处理
        <span class="tab-count">{{ getCountByStatus('pending') }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'processing' }"
        @click="switchTab('processing')"
      >
        进行中
        <span class="tab-count">{{ getCountByStatus('processing') }}</span>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'completed' }"
        @click="switchTab('completed')"
      >
        已完成
        <span class="tab-count">{{ getCountByStatus('completed') }}</span>
      </view>
    </scroll-view>
    
    <!-- 任务列表 -->
    <scroll-view 
      class="task-list" 
      scroll-y 
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view 
        class="task-item" 
        v-for="task in filteredTasks" 
        :key="task.id"
        @click="viewTaskDetail(task)"
      >
        <view class="task-card">
          <view class="task-header">
            <view class="task-priority" :style="{ background: getTaskPriorityBg(task), color: getTaskPriorityColor(task) }">
              {{ getTaskPriorityText(task) }}
            </view>
            <view class="task-status" :style="{ color: getStatusColor(task.status) }">
              {{ getStatusText(task.status) }}
            </view>
          </view>
          
          <text class="task-title">{{ task.title }}</text>
          
          <view class="task-meta">
            <view class="meta-item">
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ task.reservoirName }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">⏰</text>
              <text class="meta-text">截止 {{ formatDeadline(task.deadline) }}</text>
            </view>
          </view>
          
          <view class="task-actions">
            <view class="action-btn" @click.stop="navigateToLocation(task)">
              <text>🗺️</text> 导航
            </view>
            <view class="action-btn primary" @click.stop="startTask(task)">
              <text>{{ task.status === 'pending' ? '▶️' : task.status === 'processing' ? '⏹️' : '✓' }}</text> {{ task.status === 'pending' ? '开始任务' : task.status === 'processing' ? '结束任务' : '已完成' }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务</text>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-more">
        <text>加载中...</text>
      </view>
    </scroll-view>
    
    <!-- 任务详情弹窗 -->
    <view class="modal-overlay" :class="{ show: showDetail }" @click="closeDetail">
      <view class="modal-content task-detail-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">任务详情</text>
          <text class="modal-close" @click="closeDetail">×</text>
        </view>
        
        <scroll-view class="detail-body" scroll-y v-if="selectedTask">
          <view class="detail-section">
            <view class="detail-row">
              <view class="task-priority" :style="{ background: getTaskPriorityBg(selectedTask), color: getTaskPriorityColor(selectedTask) }">
                {{ getTaskPriorityText(selectedTask) }}
              </view>
              <view class="task-status" :style="{ color: getStatusColor(selectedTask.status) }">
                {{ getStatusText(selectedTask.status) }}
              </view>
            </view>
            
            <text class="detail-title">{{ selectedTask.title }}</text>
            <text class="detail-subtitle">{{ selectedTask.description }}</text>
          </view>
          
          <view class="detail-section">
            <text class="section-label">📍 巡查地点</text>
            <text class="section-content">{{ selectedTask.reservoirName }}</text>
          </view>
          
          <view class="detail-section">
            <text class="section-label">📝 任务描述</text>
            <text class="section-content">{{ selectedTask.description }}</text>
          </view>
          
          <view class="detail-section">
            <text class="section-label">⏰ 截止时间</text>
            <text class="section-content">{{ selectedTask.deadline }}</text>
          </view>
          
          <view class="detail-section" v-if="selectedTask.tags && selectedTask.tags.length">
            <text class="section-label">🏷️ 标签</text>
            <view class="tag-list">
              <text class="tag" v-for="tag in selectedTask.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>
        </scroll-view>
        
        <view class="detail-actions">
          <view class="action-btn" @click="navigateToLocation(selectedTask)">
            🗺️ 导航
          </view>
          <view class="action-btn primary" @click="startTask(selectedTask)">
            {{ selectedTask?.status === 'pending' ? '▶️' : selectedTask?.status === 'processing' ? '⏹️' : '✓' }} {{ selectedTask?.status === 'pending' ? '开始任务' : selectedTask?.status === 'processing' ? '结束任务' : '已完成' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { getMobileTaskList } from '@/utils/api'

// Store
const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)

// 响应式数据
const currentTab = ref('all')
const tasks = ref([])
const isLoading = ref(false)
const isRefreshing = ref(false)
const showDetail = ref(false)
const selectedTask = ref(null)

// 计算属性
const subtitle = computed(() => {
  const pending = tasks.value.filter(t => t.status === 'pending').length
  const inProgress = tasks.value.filter(t => t.status === 'processing').length
  return `待处理 ${pending} 项 · 进行中 ${inProgress} 项`
})

const filteredTasks = computed(() => {
  if (currentTab.value === 'all') {
    return tasks.value
  }
  return tasks.value.filter(t => t.status === currentTab.value)
})

// 初始化
onMounted(() => {
  loadTasks()
})

const loadTasks = async () => {
  isLoading.value = true
  try {
    const status = currentTab.value !== 'all' ? currentTab.value : null
    const res = await getMobileTaskList({ page: 1, pageSize: 50, ...(status && { status }) })
    if (Array.isArray(res)) {
      tasks.value = res
    } else if (res?.list && Array.isArray(res.list)) {
      tasks.value = res.list
    } else {
      tasks.value = []
    }
  } catch (e) {
    console.error('加载任务失败', e)
    tasks.value = []
  } finally {
    isLoading.value = false
  }
}

const onRefresh = async () => {
  isRefreshing.value = true
  await loadTasks()
  isRefreshing.value = false
  showToast('刷新成功')
}

const loadMore = () => {
  // 分页加载逻辑
}

// 分类切换
const switchTab = (tab) => {
  currentTab.value = tab
}

const getCountByStatus = (status) => {
  if (status === 'all') return tasks.value.length
  return tasks.value.filter(t => t.status === status).length
}

// 任务操作
const viewTaskDetail = (task) => {
  selectedTask.value = task
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
}

const startTask = (task) => {
  if (task.status === 'pending') {
    task.status = 'processing'
    loadTasks()
    closeDetail()
    showToast('任务已开始')
  } else if (task.status === 'processing') {
    task.status = 'completed'
    loadTasks()
    closeDetail()
    showToast('任务已完成')
  }
}

const navigateToLocation = (task) => {
  if (task.latitude && task.longitude) {
    uni.openLocation({
      latitude: parseFloat(task.latitude),
      longitude: parseFloat(task.longitude),
      name: task.reservoirName,
      fail: () => showToast('打开导航失败')
    })
  } else {
    showToast('暂无位置信息')
  }
}

// 辅助方法
const toggleTheme = () => {
  themeStore.toggleTheme()
}

const goBack = () => {
  uni.navigateBack()
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

const getTaskPriorityBg = (task) => {
  const p = typeof task === 'string' ? task : getTaskPriority(task)
  switch (p) {
    case 'urgent': return 'rgba(248, 113, 113, 0.15)'
    case 'warning': return 'rgba(232, 184, 106, 0.15)'
    default: return 'rgba(74, 222, 128, 0.15)'
  }
}

const getTaskPriorityColor = (task) => {
  const p = typeof task === 'string' ? task : getTaskPriority(task)
  switch (p) {
    case 'urgent': return '#f87171'
    case 'warning': return '#e8b86a'
    default: return '#4ade80'
  }
}

const getTaskPriorityText = (task) => {
  const p = typeof task === 'string' ? task : getTaskPriority(task)
  switch (p) {
    case 'urgent': return '紧急'
    case 'warning': return '重要'
    default: return '一般'
  }
}

const getStatusColor = (status) => {
  switch (status) {
    case 'completed': return '#4ade80'
    case 'processing': return '#60a5fa'
    default: return '#94a3b8'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'processing': return '进行中'
    case 'pending': return '待处理'
    default: return status
  }
}

const formatDeadline = (deadline) => {
  const d = new Date(deadline)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}
</script>

<style lang="scss" scoped>
.page-tasks {
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
.tasks-header {
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

.page-subtitle {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-top: 8rpx;
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
  padding: 0 32rpx 12rpx;
  margin-left: -16rpx;
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
  padding: 8rpx 20rpx;
  border-radius: 40rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  font-size: 24rpx;
  color: var(--text-muted);
  vertical-align: middle;
  margin-right: 12rpx;
  
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
  
  .active & {
    background: var(--primary);
    color: #fff;
  }
}

// 任务列表
.task-list {
  height: calc(100vh - 280rpx);
  padding: 0 32rpx;
  background: var(--bg-screen);
  overflow-y: auto;
}

.task-item {
  margin-bottom: 12rpx;
  overflow: visible;
}

.task-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  padding: 20rpx;
}

.task-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.task-priority {
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 20rpx;
  font-weight: 600;
}

.task-status {
  font-size: 22rpx;
  font-weight: 600;
}

.task-title {
  font-size: 26rpx;
  font-weight: 700;
  color: var(--text);
  display: block;
  margin-bottom: 8rpx;
}

.task-meta {
  flex-shrink: 0;
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

.task-tags {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: var(--bg-panel);
  font-size: 20rpx;
  color: var(--text-muted);
}

/* 标签白天模式 */
html[data-theme="light"] .tag,
.theme-light .tag {
  background: #f1f5f9 !important;
  color: #64748b !important;
}

.task-actions {
  flex-shrink: 0;
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
}

// 加载状态
.loading-more {
  text-align: center;
  padding: 32rpx;
  color: var(--text-muted);
  font-size: 24rpx;
}

// 详情弹窗
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 999;
  display: none;
  flex-direction: column;
  
  &.show {
    display: flex;
  }
}

.task-detail-modal {
  width: 100%;
  height: 100%;
  background: var(--bg-card);
  padding: 32rpx;
  padding-top: calc(32rpx + env(safe-area-inset-top));
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
    flex-shrink: 0;
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

.detail-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.detail-section {
  margin-bottom: 32rpx;
}

.detail-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.detail-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text);
  display: block;
  margin-bottom: 8rpx;
}

.detail-subtitle {
  font-size: 24rpx;
  color: var(--text-muted);
}

.section-label {
  font-size: 24rpx;
  color: var(--text-muted);
  display: block;
  margin-bottom: 12rpx;
}

.section-content {
  font-size: 28rpx;
  color: var(--text-body);
  line-height: 1.6;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.detail-actions {
  flex-shrink: 0;
  display: flex;
  gap: 16rpx;
  padding-top: 24rpx;
  border-top: 1px solid var(--border);
  
  .action-btn {
    flex: 1;
  }
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

  .page-subtitle {
    color: #64748b;
  }

  .theme-toggle {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
    color: #1e293b;
  }

  .task-list {
    background: #ffffff !important;
  }

  .category-tabs {
    background: #ffffff;
    border-bottom-color: rgba(0, 0, 0, 0.06);
  }

  .tab-item {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
    color: #64748b;
    &.active {
      color: #0284c7;
      border-bottom-color: #0284c7;
    }
  }

  .task-card,
  .tab-item {
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

  .task-actions {
    border-top-color: rgba(0, 0, 0, 0.06) !important;
  }

  .tag {
    background: #f1f5f9 !important;
    color: #64748b !important;
  }

  .task-tags {
    background: #ffffff;
  }

  .task-title {
    color: #1e293b;
  }

  .task-meta .meta-text,
  .task-location {
    color: #64748b;
  }

  .task-status {
    color: #64748b;
  }

  .empty-state {
    color: #64748b;
  }

  .empty-icon {
    opacity: 0.5;
  }

  .modal-overlay {
    background: rgba(0, 0, 0, 0.3);
  }

  .task-detail-modal {
    background: #ffffff;
  }

  .modal-title,
  .detail-title,
  .section-content {
    color: #1e293b;
  }

  .modal-close,
  .section-label,
  .detail-subtitle {
    color: #64748b;
  }
}
</style>
