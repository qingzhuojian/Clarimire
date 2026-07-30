<template>
  <view class="page-task-inspection" :class="{ 'theme-light': theme === 'light' }">
    <!-- 顶部区域 -->
    <view class="inspection-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">巡查记录</text>
      <view class="header-right"></view>
    </view>

    <!-- 主表单区域 -->
    <view class="inspection-form">

      <!-- 一、基本信息 -->
      <view class="form-section-title">
        <text class="section-num">01</text>
        <text class="section-name">基本信息</text>
      </view>

      <view class="form-section">
        <!-- 巡查人员 -->
        <view class="form-row">
          <text class="form-label required">巡查人员</text>
          <input class="form-input" v-model="formData.inspectorName" placeholder="请输入巡查人员姓名" />
        </view>
        <!-- 巡查日期 -->
        <view class="form-row">
          <text class="form-label required">巡查日期</text>
          <input class="form-input date-native" type="date" v-model="formData.inspectionDate" :min="todayStr" />
        </view>
        <!-- 巡查地点 -->
        <view class="form-row">
          <text class="form-label required">巡查地点</text>
          <view class="location-field">
            <view class="location-input-group">
              <input class="form-input" v-model="formData.location" placeholder="请输入巡查地点" />
              <view class="location-btn" @click="getCurrentLocation">📍</view>
            </view>
            <view v-if="formData.locationCoords" class="location-coords">{{ formData.locationCoords }}</view>
          </view>
        </view>
      </view>

      <!-- 二、问题选择 -->
      <view class="form-section-title">
        <text class="section-num">02</text>
        <text class="section-name">问题选择</text>
      </view>

      <view class="form-section">
        <!-- 问题类型下拉选择 -->
        <view class="form-row">
          <text class="form-label required">问题类型</text>
          <picker class="form-picker" mode="selector" :range="problemTypes" range-key="label" @change="onProblemTypeChange">
            <view class="picker-display" :class="{ placeholder: !formData.problemType }">
              <text>{{ formData.problemType ? problemTypes.find(p => p.value === formData.problemType)?.label : '请选择问题类型' }}</text>
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
        </view>

        <!-- 补充说明 -->
        <view class="form-row">
          <text class="form-label">补充说明</text>
          <textarea class="form-textarea" v-model="formData.problemNote" placeholder="" rows="3"></textarea>
        </view>
      </view>

      <!-- 三、图片上传 -->
      <view class="form-section-title">
        <text class="section-num">03</text>
        <text class="section-name">图片上传</text>
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
        <view class="upload-hint">
          <text>必填项，支持多图上传（最多20张）</text>
        </view>
      </view>

      <!-- 四、问题处置 -->
      <view class="form-section-title">
        <text class="section-num">04</text>
        <text class="section-name">问题处置</text>
      </view>

      <view class="form-section">
        <!-- 现场处置 -->
        <view class="form-row">
          <text class="form-label required">现场处置</text>
          <picker class="form-picker" mode="selector" :range="onSiteOptions" range-key="label" @change="e => formData.onSiteAction = onSiteOptions[e.detail.value].value">
            <view class="picker-display" :class="{ placeholder: !formData.onSiteAction }">
              <text>{{ formData.onSiteAction ? onSiteOptions.find(o => o.value === formData.onSiteAction)?.label : '请选择现场处置方式' }}</text>
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
        </view>
        <!-- 后续处理 -->
        <view class="form-row">
          <text class="form-label required">后续处理</text>
          <picker class="form-picker" mode="selector" :range="followUpOptions" range-key="label" @change="e => formData.followUpAction = followUpOptions[e.detail.value].value">
            <view class="picker-display" :class="{ placeholder: !formData.followUpAction }">
              <text>{{ formData.followUpAction ? followUpOptions.find(o => o.value === formData.followUpAction)?.label : '请选择后续处理方式' }}</text>
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
        </view>
      </view>

    </view>

    <!-- 底部操作栏 - 放在 scroll-view 外面但紧贴其下 -->
    <view class="inspection-footer">
      <view class="footer-btn draft-btn" @click="saveDraft" :class="{ loading: isDrafting }">
        <text v-if="!isDrafting">保存草稿</text>
        <text v-else>保存中...</text>
      </view>
      <view class="footer-btn submit-btn" @click="submitInspection" :class="{ loading: isSubmitting }">
        <text v-if="!isSubmitting">正式提交</text>
        <text v-else>提交中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { showToast } from '@/utils/helper'
import { useUserStore } from '@/store/user'
import { mobileReport } from '@/utils/api'
import { getLocation } from '@/utils/helper'

const themeStore = useThemeStore()
const theme = computed(() => themeStore.theme)
const userStore = useUserStore()

const todayStr = new Date().toISOString().split('T')[0]

// 问题类型选项
const problemTypes = [
  { label: '无问题', value: 'none' },
  { label: '水面浮杂', value: 'floating_debris' },
  { label: '水质异常', value: 'water_quality' },
  { label: '岸堆垃圾', value: 'shore_garbage' },
  { label: '污水直排', value: 'sewage_drain' },
  { label: '水面浮油', value: 'floating_oil' },
  { label: '其他', value: 'other' },
]

// 现场处置选项
const onSiteOptions = [
  { label: '正常无需处置', value: 'none' },
  { label: '现场清理垃圾', value: 'clean' },
  { label: '当场制止排污行为', value: 'stop_pollution' },
]

// 后续处理选项
const followUpOptions = [
  { label: '无需上报', value: 'none' },
  { label: '上报管理人员', value: 'report_manager' },
  { label: '移交环保部门', value: 'transfer_environment' },
]

// 表单数据
const formData = reactive({
  inspectorName: '',
  inspectionDate: '',
  location: '',
  locationDetail: '',
  locationCoords: '',
  problemType: '',
  problemNote: '',
  photos: [],
  onSiteAction: '',
  followUpAction: '',
})

const isDrafting = ref(false)
const isSubmitting = ref(false)

// 初始化：填入当前用户姓名
onMounted(() => {
  formData.inspectorName = userStore.currentUser?.name || ''
  formData.inspectionDate = todayStr

  // 巡查记录页不需要给 tabBar 留空间
  const fixPadding = () => {
    try {
      const wrapperEl = document.querySelector('uni-page-wrapper')
      if (wrapperEl) {
        wrapperEl.style.paddingBottom = '0'
        const innerPage = wrapperEl.querySelector('page')
        if (innerPage) innerPage.style.paddingBottom = '0'
      }
    } catch (e) {}
  }
  fixPadding()
  requestAnimationFrame(fixPadding)
  setTimeout(fixPadding, 100)
})

// 问题类型单选变更
const onProblemTypeChange = (e) => {
  const selected = problemTypes[e.detail.value]
  formData.problemType = selected.value
}

const goBack = () => {
  uni.navigateBack()
}

// 自动定位并获取当前位置地址
const getCurrentLocation = async () => {
  try {
    const res = await getLocation()
    const lat = res.latitude
    const lng = res.longitude
    formData.locationCoords = `纬度: ${lat.toFixed(6)}, 经度: ${lng.toFixed(6)}`

    try {
      const addressRes = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=zh`)
      const addressData = await addressRes.json()
      if (addressData.display_name) {
        formData.location = addressData.display_name
      }
    } catch (e) {
      console.warn('逆地理编码失败，仅保留坐标', e)
    }

    showToast('已定位当前位置')
  } catch (e) {
    showToast('定位失败，请手动输入地点')
  }
}

// 选择图片
const chooseImage = async () => {
  const count = 20 - formData.photos.length
  if (count <= 0) {
    showToast('已达最大数量限制')
    return
  }
  try {
    const res = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: resolve,
        fail: reject,
      })
    })
    if (res.tempFilePaths) {
      formData.photos.push(...res.tempFilePaths)
    }
  } catch (e) {
    if (e.errMsg !== 'chooseImage:fail cancel') {
      console.error('选择图片失败:', e)
    }
  }
}

// 移除图片
const removeImage = (index) => {
  formData.photos.splice(index, 1)
}

// 预览图片
const previewImage = (current) => {
  uni.previewImage({
    urls: formData.photos,
    current: formData.photos[current],
  })
}

// 校验必填项
const validate = () => {
  if (!formData.inspectorName) {
    showToast('请填写巡查人员')
    return false
  }
  if (!formData.inspectionDate) {
    showToast('请选择巡查日期')
    return false
  }
  if (!formData.location) {
    showToast('请填写巡查地点')
    return false
  }
  if (!formData.problemType) {
    showToast('请选择问题类型')
    return false
  }
  if (formData.photos.length === 0) {
    showToast('请上传至少一张照片')
    return false
  }
  if (!formData.onSiteAction) {
    showToast('请选择现场处置方式')
    return false
  }
  if (!formData.followUpAction) {
    showToast('请选择后续处理方式')
    return false
  }
  return true
}

// 保存草稿
const saveDraft = async () => {
  isDrafting.value = true
  try {
    const draftData = {
      ...formData,
      _draft: true,
      _savedAt: new Date().toISOString(),
    }
    uni.setStorageSync('inspection_draft', draftData)
    showToast('草稿已保存')
  } catch (e) {
    showToast('保存失败')
    console.error(e)
  } finally {
    isDrafting.value = false
  }
}

// 正式提交
const submitInspection = async () => {
  if (!validate()) return

  isSubmitting.value = true
  try {
    // 提交后清除草稿
    uni.removeStorageSync('inspection_draft')

    // 调用真实 API 上报到后端
    const severityMap = {
      'none': 'low',
      'floating_debris': 'low',
      'water_quality': 'high',
      'shore_garbage': 'medium',
      'sewage_drain': 'high',
      'floating_oil': 'high',
      'other': 'medium',
    }
    await mobileReport({
      reporterName: formData.inspectorName,
      reporterRole: userStore.userRole === 'inspector' ? 'inspector' : 'public',
      reservoirName: formData.problemType,
      description: formData.problemNote,
      photos: JSON.stringify(formData.photos),
      severity: severityMap[formData.problemType] || 'low',
      latitude: parseFloat(formData.locationCoords.match(/纬度[:：]\s*([\d.]+)/)?.[1] || '0'),
      longitude: parseFloat(formData.locationCoords.match(/经度[:：]\s*([\d.]+)/)?.[1] || '0'),
      address: formData.location,
    })

    uni.showModal({
      title: '提交成功',
      content: '巡查记录已正式提交，存入历史台账',
      showCancel: false,
      success: () => {
        uni.navigateBack()
      }
    })
  } catch (e) {
    showToast('提交失败，请重试')
    console.error(e)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
/* ===== 基础布局 ===== */
.page-task-inspection {
  height: 640px;
  width: 100%;
  max-width: 100%;
  padding: 48rpx 48rpx;
  box-sizing: border-box;
  background: var(--bg-screen);
  color: var(--text-body);
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  position: relative;
}

/* ===== 顶部导航 ===== */
.inspection-header {
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

/* ===== 表单主区域 ===== */
.inspection-form {
  padding: 32rpx;
  box-sizing: border-box;
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

.section-tip {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-left: 8rpx;
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
  &.required::after {
    content: ' *';
    color: #ef4444;
  }
}

.location-input-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
  width: 100%;
}

.location-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  flex-shrink: 0;
}

.location-input-group .form-input {
  flex: 1;
  min-width: 0;
  width: 100%;
}

.location-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  min-width: 0;
}

.location-coords {
  font-size: 24rpx;
  color: var(--text-body);
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  padding: 0 16rpx;
  height: 60rpx;
  line-height: 60rpx;
  white-space: nowrap;
  overflow-x: auto;
  text-overflow: clip;
}

.date-native {
  color-scheme: dark;
  &::-webkit-calendar-picker-indicator {
    filter: invert(0.7);
    cursor: pointer;
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

/* ===== Picker 样式 ===== */
.form-picker {
  flex: 1;
}

.picker-display {
  height: 72rpx;
  background: var(--bg-press);
  border: 1px solid var(--border);
  border-radius: 12rpx;
  padding: 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 28rpx;
  color: var(--text-body);
  &.placeholder {
    color: var(--text-muted);
  }
}

.picker-arrow {
  font-size: 20rpx;
  color: var(--text-muted);
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
  font-size: 24rpx;
  color: var(--text-muted);
}

/* ===== 底部操作栏 ===== */
.inspection-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1px solid var(--border);
  background: var(--bg-panel);
}

.footer-btn {
  flex: 1;
  height: 80rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  transition: all 0.2s;
  cursor: pointer;
  &.loading {
    opacity: 0.7;
  }
}

.draft-btn {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-secondary);
}

.submit-btn {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
  box-shadow: 0 4rpx 20rpx rgba(16, 185, 129, 0.3);
}
</style>
