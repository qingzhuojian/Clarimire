<template>
  <div class="warning-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in warningStats" :key="index">
        <div class="warning-card" :class="item.level">
          <div class="warning-icon">
            <el-icon :size="36"><WarningFilled /></el-icon>
          </div>
          <div class="warning-info">
            <div class="warning-count">{{ item.count }}</div>
            <div class="warning-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <div class="card">
          <div class="card-title">预警列表</div>
          <el-table :data="warningList" stripe style="width: 100%">
            <el-table-column prop="time" label="预警时间" width="160" />
            <el-table-column prop="type" label="预警类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeColor(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="预警级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelColor(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" />
            <el-table-column prop="description" label="预警描述" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="viewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; justify-content: flex-end;"
          />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card">
          <div class="card-title">预警趋势</div>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </div>
        <div class="card" style="margin-top: 20px;">
          <div class="card-title">预警规则配置</div>
          <el-form :model="ruleForm" label-width="100px">
            <el-form-item label="氨氮阈值">
              <el-input-number v-model="ruleForm.ammoniaThreshold" :min="0" :max="10" :step="0.1" />
              <span class="unit">mg/L</span>
            </el-form-item>
            <el-form-item label="总磷阈值">
              <el-input-number v-model="ruleForm.phosphorusThreshold" :min="0" :max="1" :step="0.01" />
              <span class="unit">mg/L</span>
            </el-form-item>
            <el-form-item label="COD阈值">
              <el-input-number v-model="ruleForm.codThreshold" :min="0" :max="100" :step="1" />
              <span class="unit">mg/L</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveRules">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="预警详情" width="600px">
      <el-descriptions :column="2" border v-if="currentWarning">
        <el-descriptions-item label="预警时间">{{ currentWarning.time }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">
          <el-tag :type="getTypeColor(currentWarning.type)">{{ currentWarning.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警级别">
          <el-tag :type="getLevelColor(currentWarning.level)">{{ currentWarning.level }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentWarning.location }}</el-descriptions-item>
        <el-descriptions-item label="预警描述" :column="2">{{ currentWarning.description }}</el-descriptions-item>
        <el-descriptions-item label="建议措施" :column="2">{{ currentWarning.suggestion }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { warningAPI } from '@/api'
import { ElMessage } from 'element-plus'

const trendChartRef = ref(null)
let trendChart = null

const detailDialogVisible = ref(false)
const currentWarning = ref(null)

const warningStats = reactive([
  { level: 'red', count: 0, label: '危险' },
  { level: 'orange', count: 0, label: '警戒' },
  { level: 'yellow', count: 0, label: '注意' },
  { level: 'blue', count: 0, label: '正常' }
])

const warningList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const ruleForm = reactive({
  ammoniaThreshold: 1.0,
  phosphorusThreshold: 0.05,
  codThreshold: 20
})

onMounted(async () => {
  await loadWarnings()
  initTrendChart()
})

onUnmounted(() => {
  trendChart?.dispose()
})

const loadWarnings = async () => {
  try {
    const res = await warningAPI.list()
    const rows = (res.data || []).map((item) => ({
      time: item.createTime,
      type: item.warningType,
      level: item.warningLevel,
      location: item.reservoirName,
      description: item.description,
      suggestion: item.suggestion
    }))
    warningList.value = rows
    pagination.total = rows.length
    updateStats(rows)
  } catch (e) {
    console.error(e)
  }
}

const updateStats = (rows) => {
  const counts = { 危险: 0, 警戒: 0, 注意: 0, 正常: 0 }
  rows.forEach((r) => {
    if (counts[r.level] !== undefined) counts[r.level]++
  })
  warningStats[0].count = counts['危险']
  warningStats[1].count = counts['警戒']
  warningStats[2].count = counts['注意']
  warningStats[3].count = counts['正常']
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['危险', '警戒'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value', name: '预警数量' },
    series: [
      { name: '危险', type: 'line', data: [0, 0, 0, 0, 0, 0] },
      { name: '警戒', type: 'line', data: [0, 0, 0, 0, 0, 0] }
    ]
  })
}

const getTypeColor = (type) => {
  const map = { '水质异常': 'danger', '水位预警': 'warning', '设备故障': 'info' }
  return map[type] || 'info'
}

const getLevelColor = (level) => {
  const map = { '危险': 'danger', '警戒': 'warning', '注意': 'info', '正常': 'primary' }
  return map[level] || 'info'
}

const viewDetail = (row) => {
  currentWarning.value = row
  detailDialogVisible.value = true
}

const saveRules = () => {
  ElMessage.success('预警规则已保存（本地配置）')
}
</script>

<style scoped>
.warning-container {
  padding: 0;
}

.warning-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.warning-card.red .warning-icon { color: #f5222d; }
.warning-card.orange .warning-icon { color: #fa8c16; }
.warning-card.yellow .warning-icon { color: #fadb14; }
.warning-card.blue .warning-icon { color: #1890ff; }

.warning-count {
  font-size: 32px;
  font-weight: 600;
  color: #333;
}

.warning-label {
  font-size: 14px;
  color: #666;
}

.unit {
  margin-left: 8px;
  color: #999;
}
</style>
