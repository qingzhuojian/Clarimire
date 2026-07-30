<template>
  <view class="page-report">

    <!-- 顶部导航 -->
    <view class="report-header">
      <view class="back-btn" @click="goHome">
        <text>←</text>
      </view>
      <text class="page-title">问题上报</text>
      <view class="header-right"></view>
    </view>

    <!-- 主表单区域（可滚动） -->
    <scroll-view class="report-form" scroll-y>

      <!-- 一、任务编号 -->
      <view class="form-section-title">
        <text class="section-num">01</text>
        <text class="section-name">任务编号</text>
      </view>
      <view class="form-section">
        <view class="form-row readonly-row">
          <text class="form-label">任务编号</text>
          <text class="form-value task-id">{{ taskId }}</text>
        </view>
      </view>

      <!-- 二、上报人信息 -->
      <view class="form-section-title">
        <text class="section-num">02</text>
        <text class="section-name">上报人信息</text>
      </view>
      <view class="form-section">
        <view class="form-row">
          <text class="form-label">上报人</text>
          <input class="form-input" v-model="formData.reporterName" placeholder="请输入上报人姓名" />
        </view>
        <view class="form-row">
          <text class="form-label">联系电话</text>
          <input class="form-input" v-model="formData.reporterPhone" type="number" placeholder="请输入联系电话" />
        </view>
      </view>

      <!-- 三、上报时间 -->
      <view class="form-section-title">
        <text class="section-num">03</text>
        <text class="section-name">上报时间</text>
      </view>
      <view class="form-section">
        <view class="form-row">
          <text class="form-label">上报时间</text>
          <text class="form-value location-text" @click="showTimePicker">{{ formData.foundTime || '请选择' }}</text>
        </view>
      </view>

      <!-- 四、上报地点 -->
      <view class="form-section-title">
        <text class="section-num">04</text>
        <text class="section-name">上报地点</text>
      </view>
      <view class="form-section">
        <view class="form-row">
          <text class="form-label">当前位置</text>
          <view class="location-field">
            <text class="form-value" :class="{ empty: !formData.latitude }">
              {{ formData.latitude ? `${formData.latitude.toFixed(6)}, ${formData.longitude.toFixed(6)}` : '点击获取定位' }}
            </text>
            <view class="location-btn" @click="chooseLocationOnMap">📍</view>
          </view>
        </view>
        <view class="form-row">
          <text class="form-label">详细地址</text>
          <input class="form-input" v-model="formData.addressDetail" placeholder="补充楼栋、门牌号等" />
        </view>
      </view>

      <!-- 五、问题类型 -->
      <view class="form-section-title">
        <text class="section-num">05</text>
        <text class="section-name">问题类型</text>
      </view>
      <view class="form-section">
        <view class="form-row category-grid">
          <view
            class="category-pill"
            v-for="cat in categories"
            :key="cat.key"
            :class="{ selected: formData.category === cat.key }"
            @click="formData.category = cat.key"
          >
            <text>{{ cat.name }}</text>
          </view>
        </view>
      </view>

      <!-- 六、问题描述 -->
      <view class="form-section-title">
        <text class="section-num">06</text>
        <text class="section-name">问题描述</text>
      </view>
      <view class="form-section">
        <view class="form-row">
          <text class="form-label">问题描述</text>
          <textarea
            class="form-textarea"
            v-model="formData.description"
            rows="4"
          ></textarea>
        </view>
      </view>

      <!-- 七、现场照片 -->
      <view class="form-section-title">
        <text class="section-num">07</text>
        <text class="section-name">现场照片</text>
      </view>
      <view class="form-section">
        <view class="image-upload-grid">
          <view
            v-for="(img, idx) in formData.photos"
            :key="idx"
            class="uploaded-image"
          >
            <image class="preview-img" :src="img" mode="aspectFill" @click="previewImage(idx)"></image>
            <view class="remove-btn" @click="removeImage(idx)">✕</view>
          </view>
          <view v-if="formData.photos.length < 20" class="upload-add-btn" @click="chooseImage">
            <text class="add-icon">+</text>
            <text class="add-text">添加照片</text>
          </view>
        </view>
      </view>

      <!-- 九、问题处理 -->
      <view class="form-section-title">
        <text class="section-num">09</text>
        <text class="section-name">问题处理</text>
      </view>
      <view class="form-section">
        <view class="form-row">
          <text class="form-label">处理方式</text>
          <view class="process-radio-group">
            <view
              class="radio-item"
              :class="{ selected: formData.processType === 'on_site' }"
              @click="formData.processType = 'on_site'"
            >
              <view class="radio-circle">
                <view class="radio-dot" v-if="formData.processType === 'on_site'"></view>
              </view>
              <text class="radio-label">现场处理</text>
            </view>
            <view
              class="radio-item"
              :class="{ selected: formData.processType === 'follow_up' }"
              @click="showManagerPicker"
            >
              <view class="radio-circle">
                <view class="radio-dot" v-if="formData.processType === 'follow_up'"></view>
              </view>
              <text class="radio-label">上报管理</text>
            </view>
          </view>
        </view>

        <!-- 上报管理：显示已选管理人员 -->
        <view class="form-row" v-if="formData.processType === 'follow_up' && formData.managerId">
          <text class="form-label">管理人员</text>
          <text class="form-value manager-name">{{ getManagerName(formData.managerId) }}</text>
        </view>

        <!-- 现场处理：显示处理方法 -->
        <view class="form-row" v-if="formData.processType === 'on_site'">
          <text class="form-label">处理方法</text>
          <textarea
            class="form-textarea"
            v-model="formData.processMethod"
            rows="3"
          ></textarea>
        </view>
      </view>

      <!-- 间距 -->
      <view style="height: 32rpx;"></view>

    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="report-footer">
      <view class="footer-btn submit-btn" @click="submitReport" :class="{ loading: isSubmitting }">
        <text v-if="!isSubmitting">提交</text>
        <text v-else>提交中...</text>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { useUserStore } from '@/store/user'
import { submitMobileReport } from '@/utils/api'

const themeStore = useThemeStore()
const userStore = useUserStore()

const taskId = ref('')
const isSubmitting = ref(false)

const formData = ref({
  reporterName: '',
  reporterPhone: '',
  foundTime: '',
  latitude: null,
  longitude: null,
  address: '',
  addressDetail: '',
  category: '',
  photos: [],
  description: '',
  processType: 'on_site',
  managerId: '',
  processMethod: '',
})

const categories = [
  { key: 'water_quality', name: '水质异常' },
  { key: 'sewage', name: '污水直排' },
  { key: 'garbage', name: '垃圾堆放' },
  { key: 'odor', name: '异味污染' },
  { key: 'other', name: '其他问题' },
]

const managers = [
  { id: 'M001', name: '张三' },
  { id: 'M002', name: '李四' },
]

// 自动生成任务编号
const generateTaskId = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const mi = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  taskId.value = `REP-${y}${m}${d}-${h}${mi}${s}-${rand}`
}

const goHome = () => uni.reLaunch({ url: '/pages/index/index' })

const getManagerName = (id) => managers.find(m => m.id === id)?.name || id

// 时间选择器
const showTimePicker = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const defaultValue = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`

  uni.showModal({
    title: '选择日期',
    editable: true,
    placeholderText: '格式: 2024-01-01 14:30',
    content: defaultValue + ' ' + String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0'),
    success: (res) => {
      if (res.confirm && res.content) {
        const input = res.content.trim()
        // 支持 YYYY-MM-DD 或 YYYY-MM-DD HH:mm 格式
        if (/^\d{4}-\d{2}-\d{2}( \d{2}:\d{2})?$/.test(input)) {
          formData.value.foundTime = input
        } else {
          uni.showToast({ title: '格式错误', icon: 'none' })
        }
      }
    }
  })
}

// 获取定位
const chooseLocationOnMap = () => {
  uni.showLoading({ title: '定位中...', mask: true })

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords
        formData.value.latitude = latitude
        formData.value.longitude = longitude
        uni.hideLoading()
      },
      () => {
        uni.hideLoading()
        showToast('定位失败，请检查权限')
      },
      { enableHighAccuracy: true, timeout: 15000 }
    )
  } else {
    uni.hideLoading()
    showToast('浏览器不支持定位')
  }
}

const showManagerPicker = () => {
  formData.value.processType = 'follow_up'
  uni.showActionSheet({
    itemList: managers.map(m => m.name),
    success: (res) => {
      formData.value.managerId = managers[res.tapIndex].id
    }
  })
}

const chooseImage = () => {
  if (formData.value.photos.length >= 20) { showToast('最多添加20张照片'); return }
  uni.chooseImage({
    count: 20 - formData.value.photos.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.value.photos.push(...res.tempFilePaths)
      showToast(`已添加 ${res.tempFilePaths.length} 张照片`)
    }
  })
}

const removeImage = (index) => formData.value.photos.splice(index, 1)

const previewImage = (index) => {
  uni.previewImage({ urls: formData.value.photos, current: index })
}

const submitReport = async () => {
  if (!formData.value.foundTime) { showToast('请选择发现时间'); return }
  if (!formData.value.latitude) { showToast('请获取问题位置'); return }
  if (!formData.value.category) { showToast('请选择问题类型'); return }
  if (!formData.value.photos.length) { showToast('请上传现场照片'); return }
  if (!formData.value.description.trim()) { showToast('请填写问题描述'); return }
  if (formData.value.processType === 'on_site' && !formData.value.processMethod.trim()) {
    showToast('请填写处理方法'); return
  }
  if (formData.value.processType === 'follow_up' && !formData.value.managerId) {
    showToast('请选择管理人员'); return
  }

  isSubmitting.value = true

  const catMap = {
    water_quality: 'water_quality',
    sewage: 'sewage',
    garbage: 'garbage',
    odor: 'odor',
    other: 'other',
  }

  const severityMap = {
    water_quality: 'high',
    sewage: 'high',
    garbage: 'medium',
    odor: 'medium',
    other: 'low',
  }

  try {
    const userRole = userStore.userRole === 'inspector' ? 'inspector' : 'public'

    // 调用真实 API 上报到后端 issue_reports 表
    const fullAddress = formData.value.address + (formData.value.addressDetail ? ' ' + formData.value.addressDetail : '')
    await submitMobileReport({
      reservoirName: formData.value.category ? catMap[formData.value.category] : 'other',
      description: formData.value.description,
      photos: JSON.stringify(formData.value.photos),
      severity: severityMap[formData.value.category] || 'low',
      reporterName: userStore.currentUser?.name || userStore.currentUser?.username || '未知用户',
      reporterRole: userRole,
      address: fullAddress,
      latitude: formData.value.latitude,
      longitude: formData.value.longitude,
      foundTime: formData.value.foundTime,
    })

    isSubmitting.value = false
    showToast('上报成功！')

    setTimeout(() => goHome(), 1200)
  } catch (err) {
    isSubmitting.value = false
    showToast('上报失败，请重试')
    console.error('上报失败:', err)
  }
}

onMounted(() => {
  generateTaskId()
  if (userStore.profile?.name) {
    formData.value.reporterName = userStore.profile.name
  }
})
</script>

<style lang="scss" scoped>
/* ===== 基础布局 ===== */
.page-report {
  height: 640px;
  background: var(--bg-screen);
  color: var(--text-body);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* ===== 顶部导航 ===== */
.report-header {
  position: sticky;
  top: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-top: calc(20rpx + constant(safe-area-inset-top));
  padding-top: calc(20rpx + env(safe-area-inset-top));
  background: var(--bg-panel);
  border-bottom: 1px solid var(--border);
  height: 100rpx;
  box-sizing: border-box;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: var(--text-body);
  cursor: pointer;
}

.page-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-body);
}

.header-right {
  width: 64rpx;
}

/* ===== 表单主区域（填满中间） ===== */
.report-form {
  flex: 1;
  height: 0;
  padding: 32rpx;
  box-sizing: border-box;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* ===== 分组标题 ===== */
.form-section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
  margin-top: 12rpx;
}

.section-num {
  font-size: 40rpx;
  font-weight: 800;
  color: #7dd3fc;
  font-family: monospace;
}

.section-name {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-body);
}

/* ===== 表单区块 ===== */
.form-section {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  padding: 16rpx;
  margin-bottom: 32rpx;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  min-height: 72rpx;
  & + & {
    margin-top: 16rpx;
  }
}

.form-label {
  font-size: 28rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
  width: 140rpx;
  line-height: 72rpx;
}

.form-input {
  flex: 1;
  height: 72rpx;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  padding: 0 16rpx;
  font-size: 28rpx;
  color: var(--text-body);
  &::placeholder {
    color: var(--text-muted);
  }
}

.location-field {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.location-input {
  flex: 1;
}

.location-btn {
  width: 72rpx;
  height: 72rpx;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
  &:active {
    background: var(--bg-hover);
  }
}

.form-textarea {
  flex: 1;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  padding: 12rpx 16rpx;
  font-size: 28rpx;
  color: var(--text-body);
  resize: none;
  line-height: 1.5;
  &::placeholder {
    color: var(--text-muted);
  }
}

.form-value {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-body);
  line-height: 72rpx;
}

.location-text {
  color: var(--text-body);
  &.empty {
    color: var(--text-muted);
  }
}

.coord-text {
  color: var(--text-secondary);
  font-size: 24rpx;
  font-family: monospace;
}

.readonly-row .form-label {
  color: var(--text-muted);
}

/* ===== 问题类型 ===== */
.category-grid {
  flex-direction: column;
  min-height: unset;
}

.category-pill {
  display: flex;
  align-items: center;
  padding: 20rpx 16rpx;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-body);
  cursor: pointer;
  transition: all 0.2s;
  & + & {
    margin-top: 12rpx;
  }
  &.selected {
    border-color: #7dd3fc;
    background: rgba(125, 211, 252, 0.1);
    color: #7dd3fc;
    box-shadow: 0 0 0 1px #7dd3fc;
  }
  &:active {
    transform: scale(0.98);
  }
}

/* ===== 处理方式 ===== */
.process-radio-group {
  flex: 1;
  display: flex;
  gap: 16rpx;
}

.radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;
  &.selected {
    border-color: #7dd3fc;
    background: rgba(125, 211, 252, 0.1);
    .radio-circle {
      border-color: #7dd3fc;
      background: #7dd3fc;
    }
    .radio-label {
      color: #7dd3fc;
    }
  }
  &:active {
    transform: scale(0.98);
  }
}

.radio-circle {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.radio-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: #fff;
}

.radio-label {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-body);
}

/* ===== 图片上传 ===== */
.image-upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.uploaded-image {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
}

.remove-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom-left-radius: 8rpx;
  cursor: pointer;
}

.upload-add-btn {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed var(--border);
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s;
  &:active {
    background: var(--bg-press);
    border-color: #7dd3fc;
  }
}

.add-icon {
  font-size: 56rpx;
  font-weight: 300;
  color: var(--text-muted);
  line-height: 1;
}

.add-text {
  font-size: 24rpx;
  color: var(--text-muted);
}

.upload-hint {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: var(--text-muted);
}

/* ===== 底部提交栏 ===== */
.report-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1px solid var(--border);
  background: var(--bg-panel);
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 22rpx;
  font-size: 32rpx;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  &:active { opacity: 0.85; transform: scale(0.98); }
  &.loading { opacity: 0.6; }
}

.draft-btn {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-body);
}

.submit-btn {
  background: linear-gradient(135deg, #38bdf8 0%, #0284c7 100%);
  color: #fff;
}
</style>
