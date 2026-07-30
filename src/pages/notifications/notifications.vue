<template>
  <view class="page-notifications" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>
    
    <!-- 顶部区域 -->
    <view class="notifications-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">消息中心</text>
      <view class="header-actions">
        <view class="theme-toggle" @click="toggleTheme">
          <text>{{ theme === 'dark' ? '🌙' : '☀️' }}</text>
        </view>
        <view class="mark-read-btn" @click="markAllRead" v-if="unreadCount > 0">
          <text>全部已读</text>
        </view>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <view class="category-tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >
        全部
        <text class="tab-count" v-if="unreadCount > 0">{{ unreadCount }}</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'urgent' }"
        @click="switchTab('urgent')"
      >
        紧急
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'task' }"
        @click="switchTab('task')"
      >
        任务
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'system' }"
        @click="switchTab('system')"
      >
        系统
      </view>
    </view>
    
    <!-- 消息列表 -->
    <view class="notification-list">
      <!-- 按日期分组 -->
      <view v-for="(items, dateLabel) in groupedNotifications" :key="dateLabel">
        <view class="date-label">{{ dateLabel }}</view>
        
        <view 
          class="notification-item" 
          :class="{ unread: !item.isRead }"
          v-for="item in items" 
          :key="item.id"
          @click="viewNotification(item)"
        >
          <view class="notification-icon" :class="item.type">
            {{ getTypeIcon(item.type) }}
          </view>
          
          <view class="notification-content">
            <view class="notification-header">
              <text class="notification-title">{{ item.title }}</text>
              <view class="unread-dot" v-if="!item.isRead"></view>
            </view>
            <text class="notification-desc">{{ item.content }}</text>
            <text class="notification-time">{{ getRelativeTime(item.time) }}</text>
          </view>
          
          <view class="notification-action" v-if="item.actionRequired" @click.stop="handleAction(item)">
            <text>处理</text>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredNotifications.length === 0" class="empty-state">
        <text class="empty-icon">🔔</text>
        <text class="empty-text">暂无消息</text>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-more">
        <text>加载中...</text>
      </view>
    </view>
    
    <!-- 消息详情弹窗 -->
    <view class="modal-overlay" :class="{ show: showDetail }" @click="closeDetail">
      <view class="modal-content notification-detail-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">消息详情</text>
          <text class="modal-close" @click="closeDetail">×</text>
        </view>
        
        <scroll-view class="detail-body" scroll-y v-if="selectedNotification">
          <view class="detail-meta">
            <view class="detail-icon" :class="selectedNotification.type">
              {{ getTypeIcon(selectedNotification.type) }}
            </view>
            <text class="detail-time">{{ formatTime(selectedNotification.time) }}</text>
          </view>
          
          <text class="detail-title">{{ selectedNotification.title }}</text>
          
          <view class="detail-content">
            {{ selectedNotification.content }}
          </view>
          
          <view class="detail-attachments" v-if="selectedNotification.attachments">
            <!-- 附件区域 -->
          </view>
        </scroll-view>
        
        <view class="detail-actions" v-if="selectedNotification?.actionRequired">
          <button class="btn-primary" @click="handleAction(selectedNotification)">
            ✓ 确认收到
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { notificationStore } from '@/utils/mockData'
import { showToast, getRelativeTime, formatTime as formatTimeHelper } from '@/utils/helper'

// Store
const themeStore = useThemeStore()
const goBack = () => uni.navigateBack()
const theme = computed(() => themeStore.theme)

const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 响应式数据
const currentTab = ref('all')
const notifications = ref([])
const isLoading = ref(false)
const isRefreshing = ref(false)
const showDetail = ref(false)
const selectedNotification = ref(null)

// 计算属性
const filteredNotifications = computed(() => {
  if (currentTab.value === 'all') {
    return notifications.value
  }
  return notifications.value.filter(n => n.type === currentTab.value)
})

const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

const groupedNotifications = computed(() => {
  const groups = {}
  const sorted = [...filteredNotifications.value].sort((a, b) => 
    new Date(b.time) - new Date(a.time)
  )
  
  sorted.forEach(item => {
    const label = getDateLabel(item.time)
    if (!groups[label]) {
      groups[label] = []
    }
    groups[label].push(item)
  })
  
  return groups
})

// 初始化
onMounted(() => {
  loadNotifications()
})

const loadNotifications = () => {
  notifications.value = notificationStore.getAll()
}

const onRefresh = async () => {
  isRefreshing.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  loadNotifications()
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

// 标记已读
const markAllRead = () => {
  notificationStore.markAllAsRead()
  loadNotifications()
  showToast('已全部标记为已读')
}

// 查看详情
const viewNotification = (item) => {
  if (!item.isRead) {
    notificationStore.markAsRead(item.id)
    item.isRead = true
  }
  selectedNotification.value = item
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
}

// 处理操作
const handleAction = (item) => {
  item.actionRequired = false
  notificationStore.updateActionRequired(item.id, false)
  showToast('已确认')
  closeDetail()
}

const getTypeIcon = (type) => {
  switch (type) {
    case 'urgent': return '🚨'
    case 'task': return '📋'
    case 'system': return '⚙️'
    case 'process': return '🔄'
    default: return '📢'
  }
}

const getDateLabel = (date) => {
  const d = new Date(date)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const itemDate = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  
  if (itemDate.getTime() === today.getTime()) return '今天'
  if (itemDate.getTime() === yesterday.getTime()) return '昨天'
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}月${day}日`
}

const formatTime = (date) => {
  return formatTimeHelper(date)
}
</script>

<style lang="scss" scoped>
.page-notifications {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-screen);
  color: var(--text-body);
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域
.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text);
}

.header-actions {
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

.mark-read-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  background: var(--primary-dim);
  font-size: 24rpx;
  color: var(--primary);
}

// 分类标签
.category-tabs {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 24rpx;
  overflow-x: auto;
}

.tab-item {
  flex-shrink: 0;
  padding: 16rpx 28rpx;
  border-radius: 40rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  font-size: 26rpx;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 8rpx;
  
  &.active {
    background: var(--primary-dim);
    border-color: var(--primary);
    color: var(--primary);
  }
}

.tab-count {
  background: rgba(255, 255, 255, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  
  .active & {
    background: var(--primary);
    color: #fff;
  }
}

// 消息列表
.notification-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 32rpx;
  -webkit-overflow-scrolling: touch;
}

.date-label {
  font-size: 24rpx;
  color: var(--text-muted);
  font-weight: 600;
  padding: 24rpx 0 16rpx;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  
  &.unread {
    background: linear-gradient(135deg, var(--primary-dim) 0%, var(--bg-card) 100%);
    border-color: rgba(125, 211, 252, 0.2);
  }
}

.notification-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: var(--bg-panel);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
  
  &.urgent {
    background: rgba(248, 113, 113, 0.15);
  }
  
  &.task {
    background: rgba(59, 130, 246, 0.15);
  }
  
  &.system {
    background: rgba(139, 92, 246, 0.15);
  }
  
  &.process {
    background: rgba(16, 185, 129, 0.15);
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.notification-title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text);
  flex: 1;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #3b82f6;
  flex-shrink: 0;
}

.notification-desc {
  font-size: 24rpx;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.notification-time {
  font-size: 20rpx;
  color: var(--text-faint);
}

.notification-action {
  flex-shrink: 0;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  background: var(--primary-dim);
  color: var(--primary);
  font-size: 22rpx;
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
  
  &.show {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
  }
}

.notification-detail-modal {
  width: 100%;
  height: 80%;
  background: var(--bg-card);
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  
  .modal-header {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
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
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  min-height: 0;
  max-height: calc(100% - 120rpx);
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.detail-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: var(--bg-panel);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  
  &.urgent {
    background: rgba(248, 113, 113, 0.15);
  }
}

.detail-time {
  font-size: 24rpx;
  color: var(--text-muted);
}

.detail-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text);
  display: block;
  margin-bottom: 20rpx;
}

.detail-content {
  font-size: 28rpx;
  color: var(--text-body);
  line-height: 1.8;
}

.detail-actions {
  flex-shrink: 0;
  padding-top: 24rpx;
  border-top: 1px solid var(--border);
}

.btn-primary {
  width: 100%;
  padding: 24rpx;
  background: linear-gradient(155deg, #38bdf8 0%, #2563eb 72%);
  color: #fff;
  border: none;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 700;
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .page-title {
    color: #1e293b;
  }

  .theme-toggle {
    background: #ffffff;
    border: none;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
    color: #1e293b;
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
    }
  }

  .notification-item {
    background: #ffffff;
    box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .notification-title {
    color: #1e293b;
  }

  .notification-content,
  .notification-time {
    color: #64748b;
  }

  .empty-state {
    color: #64748b;
  }

  .modal-overlay {
    background: rgba(0, 0, 0, 0.3);
  }

  .notification-detail-modal {
    background: #ffffff;
  }

  .modal-title,
  .detail-title {
    color: #1e293b;
  }

  .modal-close,
  .detail-time,
  .detail-content {
    color: #64748b;
  }
}
</style>
