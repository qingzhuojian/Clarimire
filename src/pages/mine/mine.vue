<template>
  <view class="page-mine" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="mine-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">个人中心</text>
      <view class="header-actions">
        <view class="theme-toggle" @click="toggleTheme">
          <text>{{ theme === 'dark' ? '🌙' : '☀️' }}</text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content" style="padding-left: 5rpx; padding-right: 5rpx;">
      <!-- 上半部分 - 用户信息 + 统计 (约1/3) -->
      <view class="top-section">
        <!-- 用户信息卡片 -->
        <view class="profile-card">
          <view class="profile-avatar">{{ userStore.currentUser?.avatar || '访' }}</view>
          <view class="profile-info">
            <text class="profile-name">{{ userStore.currentUser?.name || '未登录' }}</text>
            <text class="profile-dept">{{ userStore.currentUser?.dept || '请先登录' }}</text>
            <view class="profile-badge" v-if="userStore.currentUser?.certified">
              <text class="badge-icon">✓</text>
              <text class="badge-text">已认证</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 下半部分 - 菜单列表 (约2/3) -->
      <view class="bottom-section">
        <!-- 菜单列表 -->
        <view class="menu-list">
          <!-- 巡查员菜单 -->
          <block v-if="userStore.userRole === 'inspector' || !userStore.userRole">
            <view class="menu-item" @click="goToMyTasks">
              <view class="menu-icon" style="background: rgba(59, 130, 246, 0.15);">📋</view>
              <text class="menu-text">我的任务</text>
              <text class="menu-arrow">›</text>
            </view>
            <view class="menu-item" @click="goToFeedback">
              <view class="menu-icon" style="background: rgba(16, 185, 129, 0.15);">📝</view>
              <text class="menu-text">已反馈</text>
              <text class="menu-arrow">›</text>
            </view>
          </block>

          <!-- 群众菜单 -->
          <block v-else>
            <view class="menu-item" @click="goToMyReports">
              <view class="menu-icon" style="background: rgba(59, 130, 246, 0.15);">📋</view>
              <text class="menu-text">我的上报</text>
              <text class="menu-arrow">›</text>
            </view>
          </block>

          <view class="menu-item" @click="goToAnnouncements">
            <view class="menu-icon" style="background: rgba(139, 92, 246, 0.15);">📢</view>
            <text class="menu-text">通知公告</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <!-- 设置列表 -->
        <view class="menu-list">
          <view class="menu-item" @click="goToSettings">
            <view class="menu-icon" style="background: rgba(100, 116, 139, 0.15);">⚙️</view>
            <text class="menu-text">设置</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>

        <!-- 退出登录 -->
        <view class="menu-list" v-if="isLoggedIn">
          <view class="menu-item logout-item" @click="logout">
            <view class="menu-icon" style="background: rgba(239, 68, 68, 0.15);">🚪</view>
            <text class="menu-text logout-text">退出登录</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 账号切换弹窗 -->
    <view class="modal-overlay" :class="{ show: showAccountModal }" @click="closeAccountModal">
      <view class="modal-content account-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择登录身份</text>
          <text class="modal-close" @click="closeAccountModal">×</text>
        </view>

        <view class="account-list">
          <view class="account-item" @click="loginAs('inspector')">
            <view class="account-icon">巡</view>
            <view class="account-info">
              <text class="account-name">巡查员账号</text>
              <text class="account-desc">张明 · 江北生态环境分局</text>
            </view>
            <view class="account-check" v-if="userStore.userRole === 'inspector'">✓</view>
          </view>

          <view class="account-item" @click="loginAs('public')">
            <view class="account-icon">群</view>
            <view class="account-info">
              <text class="account-name">群众账号</text>
              <text class="account-desc">普通用户 · 已认证</text>
            </view>
            <view class="account-check" v-if="userStore.userRole === 'public'">✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { showToast, showConfirm } from '@/utils/helper'

// 内联的 reportStore 实现
const reportStore = {
  getAll() {
    try {
      const data = uni.getStorageSync('envInspectionReports')
      return data ? JSON.parse(data) : []
    } catch (e) {
      return []
    }
  },
  getCount() {
    return this.getAll().length
  }
}

// Store
const themeStore = useThemeStore()
const userStore = useUserStore()
const theme = computed(() => themeStore.theme)
const isLoggedIn = computed(() => !!userStore.currentUser)

const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 响应式数据
const showAccountModal = ref(false)

// 统计数据
const stats = ref({
  reports: 5,
  notifications: 3,
  feedback: 0
})

// 页面跳转
const goBack = () => uni.navigateBack()

const goToHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

const goToMyReports = () => {
  uni.navigateTo({ url: '/pages/my-reports/my-reports' })
}

const goToFeedback = () => {
  uni.navigateTo({ url: '/pages/feedback/feedback' })
}

const goToAnnouncements = () => {
  uni.navigateTo({ url: '/pages/notifications/notifications' })
}

const goToSettings = () => {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

const goToMyTasks = () => {
  uni.navigateTo({ url: '/pages/tasks/tasks' })
}

// 更新统计数据
const updateStats = () => {
  stats.value.feedback = reportStore.getCount()
}

// 账号操作
const switchAccount = () => {
  showAccountModal.value = true
}

const closeAccountModal = () => {
  showAccountModal.value = false
}

const loginAs = (role) => {
  userStore.login(role)
  closeAccountModal()
  showToast(`已切换为${role === 'inspector' ? '巡查员' : '群众'}账号`)
}

const loginAsInspector = () => {
  loginAs('inspector')
}

const logout = async () => {
  const confirmed = await showConfirm('退出登录', '确定要退出当前账号吗？')
  if (confirmed) {
    userStore.logout()
    showToast('已退出登录')
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/login' })
    }, 500)
  }
}

// 页面加载时更新统计数据
onMounted(() => {
  updateStats()
})
</script>

<style lang="scss" scoped>
.page-mine {
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
.mine-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx 16rpx;
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

.back-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: var(--text);
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

// 主要内容区域
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-left: 5rpx;
  padding-right: 5rpx;
  padding-bottom: 100rpx;
}

// 上半部分 - 用户信息 + 统计 (约1/3)
.top-section {
  padding-top: 8rpx;
  padding-bottom: 16rpx;
}

// 下半部分 - 菜单列表 (约2/3)
.bottom-section {
  flex: 1;
}

// 用户信息卡片
.profile-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  background: linear-gradient(145deg, #142347 0%, #1a2d55 100%);
  border-radius: 24rpx;
  border: 1px solid rgba(80, 140, 220, 0.18);
  margin-bottom: 20rpx;
}

.profile-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #60a5fa 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 10rpx 30rpx rgba(96, 165, 250, 0.35);
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.profile-name {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8rpx;
}

.profile-dept {
  font-size: 22rpx;
  color: var(--text-muted);
  margin-bottom: 10rpx;
}

.profile-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  background: rgba(52, 211, 153, 0.15);

  .badge-icon {
    font-size: 16rpx;
    color: #34d399;
  }

  .badge-text {
    font-size: 18rpx;
    color: #34d399;
  }
}

// 快捷统计
.quick-stats {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 20rpx;
  color: var(--text-muted);
}

.stat-divider {
  width: 1px;
  height: 48rpx;
  background: var(--border);
}

// 菜单列表
.menu-list {
  margin: 0 0 16rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 18rpx;
  margin-bottom: 12rpx;
}

.menu-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  margin-right: 14rpx;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: 24rpx;
  color: var(--text);
}

.menu-arrow {
  font-size: 32rpx;
  color: var(--text-muted);
}

.logout-item {
  border-color: rgba(239, 68, 68, 0.25);
}

.logout-text {
  color: #f87171;
}

// 账号区域
.account-section {
  margin: 0 0;
}

.auth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  margin-top: 20rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 18rpx;
  font-size: 26rpx;
  color: #60a5fa;

  &.danger {
    color: #f87171;
  }
}

// 账号切换弹窗
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 14, 31, 0.85);
  z-index: 999;
  display: none;
  align-items: flex-end;
  justify-content: center;

  &.show {
    display: flex;
  }
}

.account-modal {
  width: 100%;
  background: var(--bg-card);
  border-radius: 28rpx 28rpx 0 0;
  padding: 28rpx;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28rpx;
  }

  .modal-title {
    font-size: 32rpx;
    font-weight: 700;
    color: var(--text);
  }

  .modal-close {
    font-size: 44rpx;
    color: var(--text-muted);
  }
}

.account-list {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.account-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: var(--bg-panel);
  border-radius: 18rpx;
  margin-bottom: 12rpx;
}

.account-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #60a5fa 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
  margin-right: 18rpx;
  flex-shrink: 0;
}

.account-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.account-name {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.account-desc {
  font-size: 20rpx;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.account-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #60a5fa;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

// 亮色主题
.theme-light {
  background: #f8fafc !important;

  .profile-card {
    background: linear-gradient(145deg, #f1f5f9 0%, #e2e8f0 100%);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
    border: none;
  }

  .profile-name {
    color: #0f172a;
  }

  .profile-dept {
    color: #64748b;
  }

  .quick-stats,
  .menu-item,
  .auth-btn {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .stat-value,
  .stat-label,
  .menu-text,
  .menu-arrow {
    color: #334155;
  }

  .stat-label,
  .menu-arrow {
    color: #64748b;
  }

  .account-item {
    background: #ffffff;
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  .account-name {
    color: #0f172a;
  }

  .account-desc {
    color: #64748b;
  }

  .modal-overlay {
    background: rgba(0, 0, 0, 0.3);
  }

  .account-modal {
    background: #ffffff;
  }

  .modal-title {
    color: #0f172a;
  }

  .modal-close {
    color: #64748b;
  }
}
</style>
