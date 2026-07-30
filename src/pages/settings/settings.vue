<template>
  <view class="page-settings" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>
    
    <!-- 顶部区域 -->
    <view class="settings-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">个人设置</text>
    </view>
    
    <!-- 设置列表 -->
    <view class="settings-content">
      <!-- 账号安全 -->
      <view class="settings-section">
        <text class="section-title">账号安全</text>
        <view class="settings-item" @click="changePassword">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(59, 130, 246, 0.15);">🔐</view>
            <text class="item-text">修改密码</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>
      
      <!-- 紧急联络 -->
      <view class="settings-section">
        <text class="section-title">紧急联络</text>
        <view class="settings-item" @click="editEmergencyContact">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(16, 185, 129, 0.15);">📞</view>
            <text class="item-text">紧急联系人</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>
      
      <!-- 消息通知 -->
      <view class="settings-section">
        <text class="section-title">消息通知</text>
        <view class="settings-item" @click="goToNotificationSettings">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(245, 158, 11, 0.15);">🔔</view>
            <text class="item-text">通知设置</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
        <view class="settings-item" @click="goToDNDMode">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(139, 92, 246, 0.15);">🌙</view>
            <text class="item-text">免打扰模式</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="dndEnabled" @change="toggleDND" color="#2563eb" />
          </view>
        </view>
      </view>
      
      <!-- 外观 -->
      <view class="settings-section">
        <text class="section-title">外观</text>
        <view class="settings-item">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(100, 116, 139, 0.15);">🎨</view>
            <text class="item-text">深色模式</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="theme === 'dark'" @change="toggleTheme" color="#2563eb" />
          </view>
        </view>
      </view>
      
      <!-- 关于 -->
      <view class="settings-section">
        <text class="section-title">关于</text>
        <view class="settings-item">
          <view class="item-left">
            <view class="item-icon" style="background: rgba(107, 114, 128, 0.15);">ℹ️</view>
            <text class="item-text">版本信息</text>
          </view>
          <text class="version-text">1.0.0</text>
        </view>
      </view>
      
      <!-- 退出登录 -->
      <view class="logout-section">
        <view class="logout-btn" @click="logout">
          <text>退出登录</text>
        </view>
      </view>
    </view>
    
    <!-- 修改密码弹窗 -->
    <view class="modal-overlay" :class="{ show: showPasswordModal }" @click="closePasswordModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <text class="modal-close" @click="closePasswordModal">×</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <text class="form-label">当前密码</text>
            <input class="form-input" type="password" v-model="passwordForm.current" placeholder="请输入当前密码" />
          </view>
          <view class="form-group">
            <text class="form-label">新密码</text>
            <input class="form-input" type="password" v-model="passwordForm.new" placeholder="请输入新密码（6位以上）" />
          </view>
          <view class="form-group">
            <text class="form-label">确认新密码</text>
            <input class="form-input" type="password" v-model="passwordForm.confirm" placeholder="请再次输入新密码" />
          </view>
          <button class="btn-primary" @click="savePassword">保存</button>
        </view>
      </view>
    </view>
    
    <!-- 紧急联系人弹窗 -->
    <view class="modal-overlay" :class="{ show: showContactModal }" @click="closeContactModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">紧急联系人</text>
          <text class="modal-close" @click="closeContactModal">×</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <text class="form-label">联系人姓名</text>
            <input class="form-input" v-model="contactForm.name" placeholder="请输入联系人姓名" />
          </view>
          <view class="form-group">
            <text class="form-label">联系电话</text>
            <input class="form-input" type="tel" v-model="contactForm.phone" placeholder="请输入联系电话" />
          </view>
          <view class="form-group">
            <text class="form-label">与本人关系</text>
            <input class="form-input" v-model="contactForm.relation" placeholder="如：配偶、父母、同事" />
          </view>
          <button class="btn-primary" @click="saveContact">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { showToast, showConfirm } from '@/utils/helper'

// Store
const themeStore = useThemeStore()
const userStore = useUserStore()
const theme = computed(() => themeStore.theme)

// 响应式数据
const dndEnabled = ref(false)
const showPasswordModal = ref(false)
const showContactModal = ref(false)

const passwordForm = ref({
  current: '',
  new: '',
  confirm: ''
})

const contactForm = ref({
  name: '',
  phone: '',
  relation: ''
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 主题切换
const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 免打扰模式
const toggleDND = (e) => {
  dndEnabled.value = e.detail.value
  showToast(e.detail.value ? '已开启免打扰模式' : '已关闭免打扰模式')
}

// 修改密码
const changePassword = () => {
  showPasswordModal.value = true
}

const closePasswordModal = () => {
  showPasswordModal.value = false
  passwordForm.value = { current: '', new: '', confirm: '' }
}

const savePassword = () => {
  if (!passwordForm.value.current) {
    showToast('请输入当前密码')
    return
  }
  if (!passwordForm.value.new || passwordForm.value.new.length < 6) {
    showToast('新密码至少6位')
    return
  }
  if (passwordForm.value.new !== passwordForm.value.confirm) {
    showToast('两次密码不一致')
    return
  }
  closePasswordModal()
  showToast('密码修改成功')
}

// 紧急联系人
const editEmergencyContact = () => {
  showContactModal.value = true
}

const closeContactModal = () => {
  showContactModal.value = false
  contactForm.value = { name: '', phone: '', relation: '' }
}

const saveContact = () => {
  if (!contactForm.value.name) {
    showToast('请输入联系人姓名')
    return
  }
  if (!contactForm.value.phone) {
    showToast('请输入联系电话')
    return
  }
  closeContactModal()
  showToast('紧急联系人已保存')
}

// 通知设置
const goToNotificationSettings = () => {
  uni.navigateTo({ url: '/pages/notification-settings/notification-settings' })
}

// 免打扰模式设置
const goToDNDMode = () => {
  showToast('免打扰模式设置')
}

// 退出登录
const logout = async () => {
  const confirmed = await showConfirm('退出登录', '确定要退出当前账号吗？')
  if (confirmed) {
    userStore.logout()
    showToast('已退出登录')
    uni.reLaunch({ url: '/pages/index/index' })
  }
}
</script>

<style lang="scss" scoped>
.page-settings {
  min-height: 100vh;
  background: var(--bg-screen);
  color: var(--text-body);
}

.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域
.settings-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 32rpx;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: var(--bg-card);
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

// 内容区域
.settings-content {
  padding: 0 32rpx 40rpx;
}

.settings-section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-muted);
  display: block;
  margin-bottom: 16rpx;
  padding: 0 8rpx;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  margin-bottom: 12rpx;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  margin-right: 20rpx;
}

.item-text {
  font-size: 28rpx;
  color: var(--text);
}

.item-arrow {
  font-size: 36rpx;
  color: var(--text-muted);
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.version-text {
  font-size: 24rpx;
  color: var(--text-muted);
}

.toggle-switch {
  transform: scale(0.85);
}

// 退出登录
.logout-section {
  margin-top: 60rpx;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28rpx;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  font-size: 28rpx;
  color: #f87171;
}

// 弹窗
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
  width: 640rpx;
  max-width: 90vw;
  background: var(--bg-card);
  border-radius: 28rpx;
  padding: 32rpx;
}

.modal-header {
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

.modal-body {
  padding-bottom: 20rpx;
}

.form-group {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: var(--text-muted);
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  padding: 20rpx 24rpx;
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  font-size: 28rpx;
  color: var(--text-body);
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
  margin-top: 16rpx;
}

// 亮色主题
.theme-light {
  background: #ffffff;

  .back-btn,
  .settings-item,
  .logout-btn {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .page-title,
  .item-text {
    color: #1e293b;
  }

  .item-arrow,
  .version-text {
    color: #64748b;
  }

  .section-title {
    color: #64748b;
  }

  .form-input {
    background: #ffffff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    color: #1e293b;
  }

  .form-label {
    color: #64748b;
  }

  .modal-overlay {
    background: rgba(0, 0, 0, 0.3);
  }

  .modal-content {
    background: #ffffff;
  }

  .modal-title {
    color: #1e293b;
  }

  .modal-close {
    color: #64748b;
  }
}
</style>
