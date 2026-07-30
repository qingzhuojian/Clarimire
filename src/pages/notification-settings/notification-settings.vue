<template>
  <view class="page-notification-settings" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>
    
    <!-- 顶部区域 -->
    <view class="settings-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">通知设置</text>
    </view>
    
    <!-- 设置列表 -->
    <view class="settings-content">
      <!-- 免打扰设置 -->
      <view class="settings-section">
        <text class="section-title">免打扰模式</text>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">勿扰模式</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.dnd" @change="toggleSetting('dnd')" color="#2563eb" />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">短信提醒</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.sms" @change="toggleSetting('sms')" color="#2563eb" />
          </view>
        </view>
      </view>
      
      <!-- 提醒方式 -->
      <view class="settings-section">
        <text class="section-title">提醒方式</text>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">声音提醒</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.sound" @change="toggleSetting('sound')" color="#2563eb" />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">震动提醒</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.vibrate" @change="toggleSetting('vibrate')" color="#2563eb" />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">弹窗提醒</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.popup" @change="toggleSetting('popup')" color="#2563eb" />
          </view>
        </view>
      </view>
      
      <!-- 通知类型 -->
      <view class="settings-section">
        <text class="section-title">通知类型</text>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">任务提醒</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.task" @change="toggleSetting('task')" color="#2563eb" />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">巡查消息</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.inspection" @change="toggleSetting('inspection')" color="#2563eb" />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-text">系统公告</text>
          </view>
          <view class="toggle-switch">
            <switch :checked="settings.announce" @change="toggleSetting('announce')" color="#2563eb" />
          </view>
        </view>
      </view>
      
      <!-- 免打扰时段 -->
      <view class="settings-section">
        <text class="section-title">免打扰时段</text>
        <view class="settings-item clickable" @click="selectTime('start')">
          <view class="item-left">
            <text class="item-text">开始时间</text>
          </view>
          <view class="item-right">
            <text class="time-value">{{ settings.dndStart }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item clickable" @click="selectTime('end')">
          <view class="item-left">
            <text class="item-text">结束时间</text>
          </view>
          <view class="item-right">
            <text class="time-value">{{ settings.dndEnd }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'

// Store
const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)

// 设置数据
const settings = ref({
  dnd: false,
  sms: true,
  sound: true,
  vibrate: true,
  popup: true,
  task: true,
  inspection: true,
  announce: true,
  dndStart: '22:00',
  dndEnd: '07:00'
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 切换设置
const toggleSetting = (key) => {
  settings.value[key] = !settings.value[key]
  showToast('设置已保存')
}

// 选择时间
const selectTime = (type) => {
  const currentTime = type === 'start' ? settings.value.dndStart : settings.value.dndEnd
  const [hours, minutes] = currentTime.split(':').map(Number)
  
  uni.showModal({
    title: `选择${type === 'start' ? '开始' : '结束'}时间`,
    editable: true,
    placeholderText: '请输入时间（格式：HH:MM）',
    success: (res) => {
      if (res.confirm && res.content) {
        const timeRegex = /^([01]?[0-9]|2[0-3]):([0-5][0-9])$/
        if (timeRegex.test(res.content)) {
          if (type === 'start') {
            settings.value.dndStart = res.content
          } else {
            settings.value.dndEnd = res.content
          }
          showToast('时间已设置')
        } else {
          showToast('请输入正确的时间格式（HH:MM）')
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-notification-settings {
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
  
  &.clickable {
    cursor: pointer;
  }
}

.item-left {
  display: flex;
  align-items: center;
}

.item-text {
  font-size: 28rpx;
  color: var(--text);
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.time-value {
  font-size: 28rpx;
  color: var(--primary);
}

.item-arrow {
  font-size: 36rpx;
  color: var(--text-muted);
}

.toggle-switch {
  transform: scale(0.85);
}

// 亮色主题
.theme-light {
  background: #ffffff !important;

  .back-btn,
  .settings-item {
    background: #ffffff;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .page-title,
  .item-text {
    color: #1e293b;
  }

  .item-arrow {
    color: #64748b;
  }
}
</style>
