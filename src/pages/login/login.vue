<template>
  <view class="page-login" :class="{ 'theme-light': theme === 'light' }">
    <view class="login-content">
      <!-- Logo 区域 -->
      <view class="logo-section">
        <view class="logo-icon">
          <text>🌿</text>
        </view>
        <text class="app-name">环境巡查</text>
        <text class="app-slogan">共建美好生态家园</text>
      </view>

      <!-- 账号输入 -->
      <view class="form-section">
        <view class="input-group">
          <text class="input-icon">👤</text>
          <input
            class="input-field"
            v-model="account"
            type="text"
            placeholder="请输入账号"
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

        <view class="role-section">
          <view class="role-cards">
            <view
              class="role-card"
              :class="{ active: selectedRole === 'inspector' }"
              @click="selectedRole = 'inspector'"
            >
              <text class="role-icon">🛡️</text>
              <text class="role-name">巡查员</text>
              <text class="role-desc">专业环境巡查</text>
            </view>
            <view
              class="role-card"
              :class="{ active: selectedRole === 'public' }"
              @click="selectedRole = 'public'"
            >
              <text class="role-icon">👥</text>
              <text class="role-name">群众</text>
              <text class="role-desc">问题上报反馈</text>
            </view>
          </view>
        </view>

        <button class="btn-login" @click="handleLogin" :disabled="isLoading">
          <text v-if="!isLoading">登 录</text>
          <text v-else>登录中...</text>
        </button>

      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { mobileLogin } from '@/utils/api'

const themeStore = useThemeStore()
const theme = themeStore.theme

const account = ref('')
const password = ref('')
const selectedRole = ref('inspector')
const isLoading = ref(false)

const handleLogin = async () => {
  if (!account.value.trim()) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!password.value.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  isLoading.value = true

  try {
    // 调用真实登录接口
    const result = await mobileLogin(account.value.trim(), password.value.trim())

    // 保存 token
    if (result.token) {
      uni.setStorageSync('token', result.token)
    }

    // 保存用户信息到 store
    const userStore = useUserStore()
    userStore.login(result.role || 'inspector', account.value.trim(), {
      ...result,
      id: result.userId,
      realName: result.realName,
      dept: '北京市生态环境局',
      avatar: (result.realName || account.value).slice(0, 1),
      phone: result.phone || '',
      certified: true
    })

    uni.showToast({ title: '登录成功', icon: 'success' })

    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 800)
  } catch (err) {
    console.error('登录失败，使用演示模式:', err)
    // 登录失败时使用演示模式
    const userStore = useUserStore()
    userStore.login(selectedRole.value, account.value.trim())
    uni.showToast({ title: '演示模式登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 800)
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

.page-login {
  height: 100vh;
  background: transparent !important;
  color: var(--text-body);
  position: relative;
  overflow: hidden;
}

// 背景装饰
.login-bg {
  display: none !important;
}

.login-content {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 120rpx 40rpx 60rpx;
  position: relative;
  z-index: 1;
  background: transparent !important;
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

// 角色选择
.role-section {
  margin: 28rpx 0 36rpx;
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

// 登录按钮
.btn-login {
  width: 100%;
  padding: 28rpx;
  margin-top: 140rpx;
  background: linear-gradient(155deg, #60a5fa 0%, #3b82f6 50%, #2563eb 100%);
  color: #f0f9ff;
  border: none;
  border-radius: 22rpx;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 4rpx;
  box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.3);

  &[disabled] {
    opacity: 0.6;
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

  .role-card {
    background: #ffffff;
    border-color: rgba(0, 0, 0, 0.06);

    &.active {
      border-color: #3b82f6;
      background: rgba(59, 130, 246, 0.06);
    }
  }
}
</style>
