<template>
  <view class="page-register" :class="{ 'theme-light': theme === 'light' }">
    <view class="register-content">
      <!-- Logo 区域 -->
      <view class="logo-section">
        <view class="logo-icon">
          <text>🌿</text>
        </view>
        <text class="app-name">环境巡查</text>
        <text class="app-slogan">共建美好生态家园</text>
      </view>

      <!-- 注册表单 -->
      <view class="form-section">
        <view class="input-group">
          <text class="input-icon">📱</text>
          <input
            class="input-field"
            v-model="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input
            class="input-field"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input
            class="input-field"
            v-model="confirmPassword"
            type="password"
            placeholder="请确认密码"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="role-cards">
          <view
            class="role-card"
            :class="{ active: selectedRole === 'inspector' }"
            @click="selectedRole = 'inspector'"
          >
            <text class="role-icon">🛡️</text>
            <text class="role-name">巡查员</text>
          </view>
          <view
            class="role-card"
            :class="{ active: selectedRole === 'public' }"
            @click="selectedRole = 'public'"
          >
            <text class="role-icon">👥</text>
            <text class="role-name">群众</text>
          </view>
        </view>

        <button class="btn-register" @click="handleRegister" :disabled="isLoading">
          <text v-if="!isLoading">注册</text>
          <text v-else>注册中...</text>
        </button>

        <view class="switch-text" @click="goToLogin">
          <text>已有账号？去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useThemeStore } from '@/store/theme'
import { mobileRegister } from '@/utils/api'

const themeStore = useThemeStore()
const theme = themeStore.theme

const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const selectedRole = ref('inspector')
const isLoading = ref(false)

const goToLogin = () => {
  uni.navigateBack()
}

const handleRegister = async () => {
  if (!/^1\d{10}$/.test(phone.value.trim())) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  isLoading.value = true

  try {
    await mobileRegister({
      phone: phone.value.trim(),
      password: password.value,
      role: selectedRole.value
    })

    uni.showToast({ title: '注册成功', icon: 'success' })

    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch (err) {
    console.error('注册失败:', err)
    uni.showToast({ title: err.message || '注册失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
/* 穿透全局背景设置 */
:deep(page),
:deep(uni-page-body),
page,
uni-page-body {
  background: transparent !important;
}

.page-register {
  min-height: 100vh;
  background: transparent !important;
  color: var(--text-body);
  position: relative;
  padding-bottom: env(safe-area-inset-bottom);
}

// 背景装饰
.register-bg {
  display: none !important;
}

.register-content {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 120rpx 40rpx 40rpx;
  position: relative;
  z-index: 1;
  background: transparent !important;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  box-sizing: border-box;
}

// Logo 区域
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.logo-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 28rpx;
  background: linear-gradient(145deg, #142347 0%, #1a2d55 100%);
  border: 1px solid rgba(80, 140, 220, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(96, 165, 250, 0.15);
}

.app-name {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8rpx;
}

.app-slogan {
  font-size: 24rpx;
  color: var(--text-muted);
}

// 表单区域
.form-section {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.input-group {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 18rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 20rpx;
}

.input-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
}

.input-field {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-body);
}

.input-placeholder {
  color: var(--text-muted);
  font-size: 26rpx;
}

// 身份选择
.role-section {
  margin: 28rpx 0 36rpx;
}

.role-label {
  font-size: 24rpx;
  color: var(--text-muted);
  display: block;
  margin-bottom: 16rpx;
  text-align: center;
}

.role-cards {
  display: flex;
  gap: 24rpx;
}

.role-card {
  flex: 1;
  background: var(--bg-card);
  border: 2px solid var(--border);
  border-radius: 20rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.2s;

  &.active {
    border-color: #60a5fa;
    background: rgba(96, 165, 250, 0.12);
    box-shadow: 0 4rpx 16rpx rgba(96, 165, 250, 0.2);
  }
}

.role-icon {
  font-size: 28rpx;
  margin-bottom: 10rpx;
}

.role-name {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6rpx;
}

.role-desc {
  font-size: 18rpx;
  color: var(--text-muted);
}

// 注册按钮
.btn-register {
  width: 100%;
  padding: 28rpx;
  background: linear-gradient(155deg, #60a5fa 0%, #3b82f6 50%, #2563eb 100%);
  color: #f0f9ff;
  border: none;
  border-radius: 22rpx;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 4rpx;
  box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.3);
  margin-top: 150rpx;

  &[disabled] {
    opacity: 0.6;
  }
}

.switch-text {
  text-align: center;
  margin-top: 24rpx;

  text {
    font-size: 24rpx;
    color: #60a5fa;
  }
}

// 亮色主题
.theme-light {
  background: #f8fafc !important;

  .logo-icon {
    background: linear-gradient(145deg, #f1f5f9 0%, #e2e8f0 100%);
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  }

  .app-name {
    color: #0f172a;
  }

  .input-group {
    background: #ffffff;
    border-color: rgba(0, 0, 0, 0.06);
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  }

  .input-field {
    color: #0f172a;
  }
}
</style>
