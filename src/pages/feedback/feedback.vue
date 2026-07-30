<template>
  <view class="page-feedback" :class="{ 'theme-light': theme === 'light' }">
    <!-- 状态栏占位 -->
    <view class="status-bar safe-area-top"></view>

    <!-- 顶部区域 -->
    <view class="page-header">
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      <text class="page-title">已反馈</text>
      <view class="header-right"></view>
    </view>

    <!-- 搜索区域 -->
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="searchKeyword" 
          placeholder="搜索问题、地点..."
          @input="handleSearch"
        />
        <text class="search-clear" v-if="searchKeyword" @click="clearSearch">✕</text>
      </view>
      
      <!-- 筛选标签 -->
      <view class="filter-tabs">
        <view 
          class="filter-tab" 
          :class="{ active: filterType === 'all' }"
          @click="setFilter('all')"
        >
          全部
        </view>
        <view 
          class="filter-tab" 
          :class="{ active: filterType === 'report' }"
          @click="setFilter('report')"
        >
          问题上报
        </view>
        <view 
          class="filter-tab" 
          :class="{ active: filterType === 'inspection' }"
          @click="setFilter('inspection')"
        >
          巡查记录
        </view>
      </view>
    </view>

    <!-- 反馈列表 -->
    <scroll-view class="feedback-list" scroll-y :style="{ height: 'calc(100vh - 300rpx)' }">
      <!-- 问题上报 -->
      <view v-if="(filterType === 'all' || filterType === 'report') && filteredReports.length > 0">
        <view class="section-header" v-if="filterType === 'all'">
          <text class="section-title">问题上报</text>
        </view>
        <view class="feedback-item" v-for="item in filteredReports" :key="'report-' + item.id" @click="viewReportDetail(item)">
          <view class="feedback-header">
            <view class="feedback-category category-report">
              问题上报
            </view>
          </view>
          <text class="feedback-desc">{{ item.description || item.title }}</text>
          <view class="feedback-meta">
            <text class="feedback-time">{{ formatTime(item.submittedAt || item.time) }}</text>
            <text class="feedback-location" v-if="item.location && (item.location.address || item.location)">📍 {{ item.location.address || item.location }}</text>
          </view>
        </view>
      </view>

      <!-- 巡查记录 -->
      <view v-if="(filterType === 'all' || filterType === 'inspection') && filteredInspections.length > 0">
        <view class="section-header" v-if="filterType === 'all'">
          <text class="section-title">巡查记录</text>
        </view>
        <view class="feedback-item" v-for="item in filteredInspections" :key="'inspection-' + item.id" @click="viewInspectionDetail(item)">
          <view class="feedback-header">
            <view class="feedback-category category-inspection">
              {{ item.type || '巡查上报' }}
            </view>
          </view>
          <text class="feedback-desc">{{ getProblemTypeLabel(item.reservoirName) || item.name || item.description || item.title }}</text>
          <view class="feedback-meta">
            <text class="feedback-time">{{ formatTime(item.submittedAt || item.time) }}</text>
            <text class="feedback-location" v-if="item.address || item.location">📍 {{ item.address || item.location }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="filteredList.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无记录</text>
        <text class="empty-hint">{{ searchKeyword ? '尝试其他关键词搜索' : '您的问题上报和巡查记录将显示在这里' }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'
import { reportStore, getMockReports, getMockInspectionRecords } from '@/utils/mockData'
import { showToast } from '@/utils/helper'
import { getMobilePatrolRecords } from '@/utils/api'

// Store
const themeStore = useThemeStore()
const userStore = useUserStore()
const theme = computed(() => themeStore.theme)
const userRole = computed(() => userStore.userRole || 'public')

// 搜索和筛选
const searchKeyword = ref('')
const filterType = ref('all') // all, report, inspection

// 问题类型映射
const problemTypeMap = {
  'none': '无问题',
  'floating_debris': '水面浮杂',
  'water_quality': '水质异常',
  'shore_garbage': '岸堆垃圾',
  'sewage_drain': '污水直排',
  'floating_oil': '水面浮油',
  'other': '其他'
}

// 获取中文问题类型
const getProblemTypeLabel = (type) => {
  return problemTypeMap[type] || type || '问题'
}

// 问题上报列表
const reportList = computed(() => {
  // 从 Store 获取
  const stored = reportStore.getAll(userRole.value)
  // 从本地存储获取新上报的记录
  const localReports = uni.getStorageSync('localReports') || []
  // 合并并去重（按 id）
  const allReports = [...localReports, ...stored]
  const uniqueReports = allReports.filter((item, index, self) =>
    index === self.findIndex(t => t.id === item.id)
  )
  return uniqueReports.length > 0 ? uniqueReports : getMockReports()
})

// 巡查记录列表
const inspectionList = ref([])

onMounted(async () => {
  // 优先从本地存储读取
  const localReports = uni.getStorageSync('localInspectionReports') || []
  
  try {
    const res = await getMobilePatrolRecords({ page: 1, pageSize: 50 })
    let list = []
    if (Array.isArray(res)) {
      list = res
    } else if (res?.list && Array.isArray(res.list)) {
      list = res.list
    }
    // 合并本地记录和API记录，去重
    const allRecords = [...localReports, ...list]
    const uniqueRecords = allRecords.filter((item, index, self) =>
      index === self.findIndex(t => t.id === item.id)
    )
    inspectionList.value = uniqueRecords
  } catch (e) {
    console.error('加载巡查记录失败', e)
    // API失败时只用本地记录或mock
    inspectionList.value = localReports.length > 0 ? localReports : getMockInspectionRecords()
  }
})

// 过滤后的列表
const filteredReports = computed(() => {
  if (!searchKeyword.value) return reportList.value
  const keyword = searchKeyword.value.toLowerCase()
  return reportList.value.filter(item => {
    const desc = (item.description || item.title || '').toLowerCase()
    const location = (item.location?.address || item.location || '').toLowerCase()
    const category = (item.category || '').toLowerCase()
    return desc.includes(keyword) || location.includes(keyword) || category.includes(keyword)
  })
})

const filteredInspections = computed(() => {
  if (!searchKeyword.value) return inspectionList.value
  const keyword = searchKeyword.value.toLowerCase()
  return inspectionList.value.filter(item => {
    const name = (item.name || item.title || '').toLowerCase()
    const location = (item.location || '').toLowerCase()
    const type = (item.type || '').toLowerCase()
    return name.includes(keyword) || location.includes(keyword) || type.includes(keyword)
  })
})

const filteredList = computed(() => {
  if (filterType.value === 'report') return filteredReports.value
  if (filterType.value === 'inspection') return filteredInspections.value
  return [...filteredReports.value, ...filteredInspections.value]
})

// 筛选类型
const setFilter = (type) => {
  filterType.value = type
}

// 搜索处理
const handleSearch = () => {
  // 实时搜索，不需要额外处理
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

// 获取分类样式
const getCategoryClass = (category) => {
  const map = {
    '水污染': 'category-water',
    '大气污染': 'category-air',
    '固废污染': 'category-waste',
    '噪声污染': 'category-noise',
    '生态破坏': 'category-ecology',
    '其他问题': 'category-other'
  }
  return map[category] || 'category-other'
}

// 获取状态样式
const getStatusClass = (status) => {
  const map = {
    '待处理': 'status-pending',
    '处理中': 'status-processing',
    '已处理': 'status-done',
    '已驳回': 'status-rejected'
  }
  return map[status] || 'status-pending'
}

// 获取巡查状态样式
const getInspectionStatusClass = (status) => {
  const map = {
    '已完成': 'status-done',
    '进行中': 'status-processing',
    '待处理': 'status-pending'
  }
  return map[status] || 'status-pending'
}

// 查看问题上报详情
const viewReportDetail = (item) => {
  uni.navigateTo({
    url: `/pages/report-detail/report-detail?id=${item.id}&type=feedback`
  })
}

// 查看巡查记录详情
const viewInspectionDetail = (item) => {
  uni.navigateTo({
    url: `/pages/inspection-detail/inspection-detail?id=${item.id}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page-feedback {
  min-height: 100vh;
  height: 100vh;
  background: var(--bg-screen);
  color: var(--text-body);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 状态栏
.status-bar {
  height: constant(safe-area-inset-top);
  height: env(safe-area-inset-top);
}

// 顶部区域
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
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
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text);
}

.header-right {
  width: 64rpx;
}

// 搜索区域
.search-section {
  padding: 0 32rpx 24rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 16rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text);
  background: transparent;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-clear {
  font-size: 24rpx;
  color: var(--text-muted);
  padding: 8rpx;
}

// 筛选标签
.filter-tabs {
  display: flex;
  gap: 16rpx;
}

.filter-tab {
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: var(--text-muted);
  background: var(--bg-card);
  border: 1px solid var(--border);
  transition: all 0.2s;
}

.filter-tab.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #ffffff;
}

// 反馈列表
.feedback-list {
  flex: 1;
  padding: 0 32rpx 200rpx;
}

.section-header {
  padding: 24rpx 0 16rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-muted);
}

.feedback-item {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 32rpx;
  position: relative;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.feedback-source {
  display: flex;
  align-items: center;
}

.source-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
}

.source-tag.inspection-tag {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  font-size: 24rpx;
  font-weight: 500;
}

.feedback-category {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.feedback-category.category-water {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.feedback-category.category-air {
  background: rgba(168, 85, 247, 0.15);
  color: #a78bfa;
}

.feedback-category.category-waste {
  background: rgba(251, 146, 60, 0.15);
  color: #fb923c;
}

.feedback-category.category-noise {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.feedback-category.category-ecology {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
}

.feedback-category.category-other {
  background: rgba(156, 163, 175, 0.15);
  color: #9ca3af;
}

.feedback-category.category-inspection {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.feedback-category.category-report {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.feedback-desc {
  display: block;
  font-size: 28rpx;
  color: var(--text);
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.feedback-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.feedback-time,
.feedback-location {
  font-size: 22rpx;
  color: var(--text-muted);
}

.feedback-location {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feedback-status-bar {
  position: absolute;
  top: 28rpx;
  right: 28rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.feedback-status-bar.status-pending {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.feedback-status-bar.status-processing {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.feedback-status-bar.status-done {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
}

.feedback-status-bar.status-rejected {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
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
  font-size: 96rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: var(--text-muted);
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: var(--text-faint);
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

  .search-bar {
    background: #f8fafc;
    border-color: rgba(0, 0, 0, 0.08);
  }

  .search-input {
    color: #1e293b;
  }

  .search-input::placeholder {
    color: #94a3b8;
  }

  .filter-tab {
    background: #f8fafc;
    border-color: rgba(0, 0, 0, 0.08);
    color: #64748b;
  }

  .filter-tab.active {
    background: #0284c7;
    border-color: #0284c7;
    color: #ffffff;
  }

  .feedback-item {
    background: #ffffff;
    box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
    border: none;
  }

  .feedback-desc {
    color: #1e293b;
  }

  .feedback-time,
  .feedback-location {
    color: #64748b;
  }

  .empty-text {
    color: #64748b;
  }

  .empty-hint {
    color: #94a3b8;
  }
}
</style>
