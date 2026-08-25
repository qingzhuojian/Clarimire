<template>
  <div class="statistics-analysis-container">
    <div class="shell-tabs">
      <div
        v-for="item in sidebarItems"
        :key="item.key"
        class="shell-tab"
        :class="{ active: activeBox === item.key }"
        @click="activeBox = item.key"
      >
        {{ item.label }}
      </div>
    </div>

    <div class="statistics-content">
        <!-- 水情统计 -->
        <div v-if="activeBox === 'waterStatistics'" class="water-statistics-panel">
          <div class="filter-bar">
            <el-select v-model="waterForm.reservoir" clearable placeholder="选择水库" style="width: 200px;">
              <el-option v-for="r in reservoirs" :key="r" :label="r" :value="r" />
            </el-select>
            <el-date-picker
              v-model="waterForm.range"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 280px;"
            />
            <el-select
              v-model="selectedCharts"
              multiple
              collapse-tags
              clearable
              placeholder="选择显示图表(至少1个)"
              style="width: 280px;"
            >
              <el-option label="水位分布" value="waterLevel" />
              <el-option label="蓄水量变化" value="waterStorage" />
              <el-option label="日均入库流量变化" value="waterInflow" />
              <el-option label="日均出库流量变化" value="waterOutflow" />
            </el-select>
            <el-button type="primary" @click="loadWaterStats">查询</el-button>
          </div>

          <div v-if="!waterQueried" class="chart-selection-tip">
            <el-empty :image-size="120">
              <template #description>
                <div class="tip-content">
                  <p>请先选择水库、日期范围与图表类型，再点击「查询」</p>
                </div>
              </template>
            </el-empty>
          </div>

          <template v-else>
            <div class="water-statistics-cards">
              <div v-for="item in waterCircles" :key="item.label" class="water-stat-circle">
                <div class="stat-circle-bg">
                  <div class="stat-circle-value">{{ item.value }}</div>
                </div>
                <div class="stat-circle-label">
                  <div>{{ item.label }}</div>
                  <div class="unit-text">{{ item.unit }}</div>
                </div>
              </div>
            </div>

            <div v-if="!selectedCharts.length" class="chart-selection-tip">
              <el-empty :image-size="100" description="请在上方选择要显示的图表类型" />
            </div>

            <div v-if="selectedCharts.includes('waterLevel')" class="chart-container">
              <h3>水位分布</h3>
              <div ref="waterLevelChartRef" class="chart" />
            </div>
            <div v-if="selectedCharts.includes('waterStorage')" class="chart-container">
              <h3>蓄水量变化</h3>
              <div ref="waterStorageChartRef" class="chart" />
            </div>
            <div v-if="selectedCharts.includes('waterInflow')" class="chart-container">
              <h3>日均入库流量变化</h3>
              <div ref="waterInflowChartRef" class="chart" />
            </div>
            <div v-if="selectedCharts.includes('waterOutflow')" class="chart-container">
              <h3>日均出库流量变化</h3>
              <div ref="waterOutflowChartRef" class="chart" />
            </div>
          </template>
        </div>

        <!-- 水情对比 -->
        <div v-else-if="activeBox === 'waterComparison'" class="water-comparison-panel">
          <div class="filter-bar">
            <el-select
              v-model="compareReservoirs"
              multiple
              collapse-tags
              clearable
              placeholder="选择水库(至少2个)"
              style="width: 320px;"
            >
              <el-option v-for="r in reservoirs" :key="r" :label="r" :value="r" />
            </el-select>
            <el-date-picker
              v-model="compareDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px;"
            />
            <el-select
              v-model="selectedComparisonCharts"
              multiple
              collapse-tags
              clearable
              placeholder="选择显示图表(至少1个)"
              style="width: 240px;"
            >
              <el-option label="水位对比" value="waterLevel" />
              <el-option label="蓄水量对比" value="storage" />
              <el-option label="入库流量对比" value="avgInflow" />
              <el-option label="出库流量对比" value="avgOutflow" />
            </el-select>
            <el-button type="primary" @click="loadWaterCompare">刷新</el-button>
          </div>

          <div v-if="!waterCompareQueried" class="chart-selection-tip">
            <el-empty :image-size="120">
              <template #description>
                <div class="tip-content">
                  <p>请先选择至少 2 个水库与图表类型，再点击「刷新」</p>
                </div>
              </template>
            </el-empty>
          </div>

          <template v-else>
            <div v-if="!selectedComparisonCharts.length" class="chart-selection-tip">
              <el-empty :image-size="100" description="请在上方选择要显示的对比图表类型" />
            </div>
            <div v-if="selectedComparisonCharts.includes('waterLevel')" class="chart-container">
              <h3>水位对比</h3>
              <div ref="compareWaterLevelRef" class="chart" />
            </div>
            <div v-if="selectedComparisonCharts.includes('storage')" class="chart-container">
              <h3>蓄水量对比</h3>
              <div ref="compareStorageRef" class="chart" />
            </div>
            <div v-if="selectedComparisonCharts.includes('avgInflow')" class="chart-container">
              <h3>入库流量对比</h3>
              <div ref="compareInflowRef" class="chart" />
            </div>
            <div v-if="selectedComparisonCharts.includes('avgOutflow')" class="chart-container">
              <h3>出库流量对比</h3>
              <div ref="compareOutflowRef" class="chart" />
            </div>
          </template>
        </div>

        <!-- 环境统计 -->
        <div v-else-if="activeBox === 'environmentStatistics'" class="environment-statistics-panel">
          <div class="filter-bar">
            <el-select v-model="envForm.reservoir" clearable placeholder="选择水库" style="width: 180px;">
              <el-option v-for="r in reservoirs" :key="r" :label="r" :value="r" />
            </el-select>
            <el-select v-model="envForm.year" clearable placeholder="选择年份" style="width: 120px;">
              <el-option v-for="y in years" :key="y" :label="y" :value="y" />
            </el-select>
            <el-select
              v-model="selectedEnvironmentCharts"
              multiple
              collapse-tags
              clearable
              placeholder="选择显示指标(至少1个)"
              style="width: 240px;"
            >
              <el-option label="氨氮" value="ammoniaNitrogen" />
              <el-option label="高锰酸盐指数" value="potassiumPermanganate" />
              <el-option label="化学需氧量" value="cod" />
              <el-option label="总氮" value="totalNitrogen" />
              <el-option label="总磷" value="totalPhosphorus" />
            </el-select>
            <el-button type="primary" @click="loadEnvStats">查询</el-button>
          </div>

          <div v-if="!envQueried" class="chart-selection-tip">
            <el-empty :image-size="120">
              <template #description>
                <div class="tip-content">
                  <p>请先选择水库、年份与指标类型，再点击「查询」</p>
                </div>
              </template>
            </el-empty>
          </div>

          <template v-else>
            <div class="environment-statistics-cards">
              <div v-for="item in envCircles" :key="item.label" class="environment-stat-circle">
                <div class="stat-circle-bg">
                  <div class="stat-circle-value">{{ item.value }}</div>
                </div>
                <div class="stat-circle-label">
                  <div>{{ item.label }}</div>
                  <div class="unit-text">{{ item.unit }}</div>
                </div>
              </div>
            </div>

            <div v-if="!selectedEnvironmentCharts.length" class="chart-selection-tip">
              <el-empty :image-size="100" description="请在上方选择要显示的指标类型" />
            </div>

            <div v-if="selectedEnvironmentCharts.includes('ammoniaNitrogen')" class="chart-container">
              <h3>氨氮指标分布</h3>
              <div ref="envAmmoniaRef" class="chart" />
            </div>
            <div v-if="selectedEnvironmentCharts.includes('potassiumPermanganate')" class="chart-container">
              <h3>高锰酸盐指数分布</h3>
              <div ref="envPermanganateRef" class="chart" />
            </div>
            <div v-if="selectedEnvironmentCharts.includes('cod')" class="chart-container">
              <h3>化学需氧量分布</h3>
              <div ref="envCodRef" class="chart" />
            </div>
            <div v-if="selectedEnvironmentCharts.includes('totalNitrogen')" class="chart-container">
              <h3>总氮指标分布</h3>
              <div ref="envNitrogenRef" class="chart" />
            </div>
            <div v-if="selectedEnvironmentCharts.includes('totalPhosphorus')" class="chart-container">
              <h3>总磷指标分布</h3>
              <div ref="envPhosphorusRef" class="chart" />
            </div>
          </template>
        </div>

        <!-- 环境对比 -->
        <div v-else class="environment-comparison-panel">
          <div class="filter-bar">
            <el-select
              v-model="envComparePoints"
              multiple
              collapse-tags
              clearable
              placeholder="监测点(水库，至少2个)"
              style="width: 320px;"
            >
              <el-option v-for="r in reservoirs" :key="r" :label="r" :value="r" />
            </el-select>
            <el-select v-model="envCompareYear" clearable placeholder="年份" style="width: 120px;">
              <el-option v-for="y in years" :key="y" :label="y" :value="y" />
            </el-select>
            <el-select
              v-model="selectedEnvCompareCharts"
              multiple
              collapse-tags
              clearable
              placeholder="选择对比指标(至少1个)"
              style="width: 240px;"
            >
              <el-option label="氨氮" value="ammoniaNitrogen" />
              <el-option label="COD" value="cod" />
              <el-option label="总磷" value="totalPhosphorus" />
            </el-select>
            <el-button type="primary" @click="loadEnvCompare">对比</el-button>
          </div>

          <div v-if="!envCompareQueried" class="chart-selection-tip">
            <el-empty :image-size="120">
              <template #description>
                <div class="tip-content">
                  <p>请先选择至少 2 个监测点、年份与指标，再点击「对比」</p>
                </div>
              </template>
            </el-empty>
          </div>

          <template v-else>
            <div v-if="!selectedEnvCompareCharts.length" class="chart-selection-tip">
              <el-empty :image-size="100" description="请在上方选择要对比的指标类型" />
            </div>
            <div v-if="selectedEnvCompareCharts.includes('ammoniaNitrogen')" class="chart-container">
              <h3>氨氮对比</h3>
              <div ref="envCompareAmmoniaRef" class="chart" />
            </div>
            <div v-if="selectedEnvCompareCharts.includes('cod')" class="chart-container">
              <h3>COD 对比</h3>
              <div ref="envCompareCodRef" class="chart" />
            </div>
            <div v-if="selectedEnvCompareCharts.includes('totalPhosphorus')" class="chart-container">
              <h3>总磷对比</h3>
              <div ref="envComparePhosphorusRef" class="chart" />
            </div>
          </template>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { waterSituationAPI, sectionMonitorAPI, warningAPI } from '@/api'
import '@/assets/statistics-page.css'

const sidebarItems = [
  { key: 'waterStatistics', label: '水情统计' },
  { key: 'waterComparison', label: '水情对比' },
  { key: 'environmentStatistics', label: '环境统计' },
  { key: 'environmentComparison', label: '环境对比' }
]

const route = useRoute()
const router = useRouter()
const activeBox = ref(route.query.tab || 'waterStatistics')

const reservoirs = ref([])
const years = ref([])
const reservoirMeta = ref([])

const waterForm = reactive({ reservoir: '', range: [] })
const envForm = reactive({ reservoir: '', year: null })
const compareReservoirs = ref([])
const compareDateRange = ref([])
const envComparePoints = ref([])
const envCompareYear = ref(null)

/** 图表类型默认不勾选，须用户选择后再查询 */
const selectedCharts = ref([])
const selectedComparisonCharts = ref([])
const selectedEnvironmentCharts = ref([])
const selectedEnvCompareCharts = ref([])

/** 仅点击查询/刷新/对比后才展示下方结果 */
const waterQueried = ref(false)
const waterCompareQueried = ref(false)
const envQueried = ref(false)
const envCompareQueried = ref(false)

const waterCircles = ref([])
const envCircles = ref([])

const waterLevelChartRef = ref(null)
const waterStorageChartRef = ref(null)
const waterInflowChartRef = ref(null)
const waterOutflowChartRef = ref(null)
const compareWaterLevelRef = ref(null)
const compareStorageRef = ref(null)
const compareInflowRef = ref(null)
const compareOutflowRef = ref(null)
const envAmmoniaRef = ref(null)
const envPermanganateRef = ref(null)
const envCodRef = ref(null)
const envNitrogenRef = ref(null)
const envPhosphorusRef = ref(null)
const envCompareAmmoniaRef = ref(null)
const envCompareCodRef = ref(null)
const envComparePhosphorusRef = ref(null)

const chartMap = new Map()

watch(activeBox, (tab) => {
  if (route.query.tab !== tab) {
    router.replace({ path: '/statistics', query: { tab } })
  }
})

watch(
  () => route.query.tab,
  (tab) => {
    if (sidebarItems.some((item) => item.key === tab)) {
      activeBox.value = tab
    }
  }
)

onMounted(async () => {
  const r1 = await waterSituationAPI.getReservoirs()
  reservoirs.value = r1.data || []
  const r2 = await sectionMonitorAPI.getYears()
  years.value = r2.data || []
  const r3 = await warningAPI.getReservoirs()
  reservoirMeta.value = r3.data || []
  // 不自动选中、不自动出图
})

onUnmounted(() => chartMap.forEach((c) => c?.dispose()))

const fmt = (d) => {
  if (!d) return ''
  const x = new Date(d)
  return `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, '0')}-${String(x.getDate()).padStart(2, '0')}`
}

const avg = (arr) => (arr.length ? (arr.reduce((a, b) => a + b, 0) / arr.length).toFixed(2) : '0.00')

const getChart = (el) => {
  if (!el) return null
  if (chartMap.has(el)) return chartMap.get(el)
  const chart = echarts.init(el)
  chartMap.set(el, chart)
  return chart
}

const filterByDateRange = (rows, range) => {
  if (!range?.length) return rows
  const [start, end] = range
  return rows.filter((r) => {
    const d = fmt(r.date)
    return d >= start && d <= end
  })
}

const loadWaterStats = async () => {
  if (!waterForm.reservoir) {
    ElMessage.warning('请先选择水库')
    return
  }
  if (!selectedCharts.value.length) {
    ElMessage.warning('请至少选择 1 个图表类型')
    return
  }

  const res = await waterSituationAPI.list({
    reservoirName: waterForm.reservoir,
    startDate: waterForm.range?.[0] || null,
    endDate: waterForm.range?.[1] || null
  })
  const rows = res.data || []
  const meta = reservoirMeta.value.find((r) => r.reservoirName === waterForm.reservoir) || {}
  const levels = rows.map((r) => Number(r.waterLevel)).filter((v) => !Number.isNaN(v))
  const storages = rows.map((r) => Number(r.storage)).filter((v) => !Number.isNaN(v))
  const inflows = rows.map((r) => Number(r.avgInflow)).filter((v) => !Number.isNaN(v))
  const outflows = rows.map((r) => Number(r.avgOutflow)).filter((v) => !Number.isNaN(v))

  waterCircles.value = [
    { label: '库水位', value: avg(levels), unit: '(米)' },
    { label: '蓄水量', value: storages.length ? Math.max(...storages).toFixed(0) : '0', unit: '(万立方米)' },
    { label: '日均入库流量', value: avg(inflows), unit: '(立方米/秒)' },
    { label: '日均出库流量', value: avg(outflows), unit: '(立方米/秒)' },
    { label: '总库容', value: meta.capacity ? Number(meta.capacity).toFixed(0) : '-', unit: '(万立方米)' },
    { label: '汛限水位', value: meta.floodLevel ? Number(meta.floodLevel).toFixed(2) : '-', unit: '(米)' }
  ]

  waterQueried.value = true
  await nextTick()
  const dates = rows.map((r) => fmt(r.date))
  const chartDefs = [
    { key: 'waterLevel', ref: waterLevelChartRef, field: 'waterLevel', type: 'line', unit: 'm' },
    { key: 'waterStorage', ref: waterStorageChartRef, field: 'storage', type: 'bar', unit: '万m³' },
    { key: 'waterInflow', ref: waterInflowChartRef, field: 'avgInflow', type: 'line', unit: 'm³/s' },
    { key: 'waterOutflow', ref: waterOutflowChartRef, field: 'avgOutflow', type: 'line', unit: 'm³/s' }
  ]
  chartDefs.forEach(({ key, ref: chartRef, field, type, unit }) => {
    if (!selectedCharts.value.includes(key)) return
    getChart(chartRef.value)?.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', name: unit },
      series: [{ type, data: rows.map((r) => r[field]), smooth: true }]
    })
  })
}

const renderCompareChart = (el, metric, unit, selected) => {
  waterSituationAPI.list({}).then((res) => {
    let rows = res.data || []
    rows = filterByDateRange(rows, compareDateRange.value)
    const dateSet = [...new Set(rows.map((r) => fmt(r.date)))].sort()
    getChart(el)?.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: selected },
      xAxis: { type: 'category', data: dateSet },
      yAxis: { type: 'value', name: unit },
      series: selected.map((name) => ({
        name,
        type: 'line',
        connectNulls: true,
        smooth: true,
        data: dateSet.map((d) => {
          const item = rows.find((r) => r.reservoirName === name && fmt(r.date) === d)
          return item ? item[metric] : null
        })
      }))
    })
  })
}

const loadWaterCompare = async () => {
  if (compareReservoirs.value.length < 2) {
    ElMessage.warning('请至少选择 2 个水库')
    return
  }
  if (!selectedComparisonCharts.value.length) {
    ElMessage.warning('请至少选择 1 个对比图表')
    return
  }

  waterCompareQueried.value = true
  await nextTick()
  const selected = [...compareReservoirs.value]
  const defs = [
    { key: 'waterLevel', ref: compareWaterLevelRef, metric: 'waterLevel', unit: 'm' },
    { key: 'storage', ref: compareStorageRef, metric: 'storage', unit: '万m³' },
    { key: 'avgInflow', ref: compareInflowRef, metric: 'avgInflow', unit: 'm³/s' },
    { key: 'avgOutflow', ref: compareOutflowRef, metric: 'avgOutflow', unit: 'm³/s' }
  ]
  defs.forEach(({ key, ref: chartRef, metric, unit }) => {
    if (selectedComparisonCharts.value.includes(key)) {
      renderCompareChart(chartRef.value, metric, unit, selected)
    }
  })
}

const loadEnvStats = async () => {
  if (!envForm.reservoir) {
    ElMessage.warning('请先选择水库')
    return
  }
  if (!envForm.year) {
    ElMessage.warning('请先选择年份')
    return
  }
  if (!selectedEnvironmentCharts.value.length) {
    ElMessage.warning('请至少选择 1 个指标')
    return
  }

  const res = await sectionMonitorAPI.list({ reservoirName: envForm.reservoir, year: envForm.year })
  const rows = (res.data || []).sort((a, b) => a.month - b.month)
  const pickAvg = (field) => {
    const vals = rows.map((r) => Number(r[field])).filter((v) => !Number.isNaN(v))
    return vals.length ? avg(vals) : '-'
  }
  envCircles.value = [
    { label: '氨氮', value: pickAvg('ammoniaNitrogen'), unit: '(mg/L)' },
    { label: '高锰酸盐', value: pickAvg('potassiumPermanganate'), unit: '(mg/L)' },
    { label: '化学需氧量', value: pickAvg('cod'), unit: '(mg/L)' },
    { label: '流量', value: pickAvg('flow'), unit: '(m³/s)' },
    { label: '水深', value: pickAvg('waterDepth'), unit: '(m)' },
    { label: '总氮', value: pickAvg('totalNitrogen'), unit: '(mg/L)' },
    { label: '总磷', value: pickAvg('totalPhosphorus'), unit: '(mg/L)' }
  ]

  envQueried.value = true
  await nextTick()
  const months = rows.map((r) => `${r.month}月`)
  const envChartDefs = [
    { key: 'ammoniaNitrogen', ref: envAmmoniaRef, field: 'ammoniaNitrogen', name: '氨氮' },
    { key: 'potassiumPermanganate', ref: envPermanganateRef, field: 'potassiumPermanganate', name: '高锰酸盐指数' },
    { key: 'cod', ref: envCodRef, field: 'cod', name: 'COD' },
    { key: 'totalNitrogen', ref: envNitrogenRef, field: 'totalNitrogen', name: '总氮' },
    { key: 'totalPhosphorus', ref: envPhosphorusRef, field: 'totalPhosphorus', name: '总磷' }
  ]
  envChartDefs.forEach(({ key, ref: chartRef, field, name }) => {
    if (!selectedEnvironmentCharts.value.includes(key)) return
    getChart(chartRef.value)?.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: 'mg/L' },
      series: [{ name, type: 'line', smooth: true, data: rows.map((r) => r[field]) }]
    })
  })
}

const loadEnvCompare = async () => {
  if (envComparePoints.value.length < 2) {
    ElMessage.warning('请至少选择 2 个监测点')
    return
  }
  if (!envCompareYear.value) {
    ElMessage.warning('请先选择年份')
    return
  }
  if (!selectedEnvCompareCharts.value.length) {
    ElMessage.warning('请至少选择 1 个对比指标')
    return
  }

  const res = await sectionMonitorAPI.list({ year: envCompareYear.value })
  const rows = res.data || []
  const points = [...envComparePoints.value]
  const months = [...Array(12)].map((_, i) => `${i + 1}月`)

  envCompareQueried.value = true
  await nextTick()
  const defs = [
    { key: 'ammoniaNitrogen', ref: envCompareAmmoniaRef, field: 'ammoniaNitrogen', name: '氨氮' },
    { key: 'cod', ref: envCompareCodRef, field: 'cod', name: 'COD' },
    { key: 'totalPhosphorus', ref: envComparePhosphorusRef, field: 'totalPhosphorus', name: '总磷' }
  ]
  defs.forEach(({ key, ref: chartRef, field }) => {
    if (!selectedEnvCompareCharts.value.includes(key)) return
    getChart(chartRef.value)?.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: points },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: 'mg/L' },
      series: points.map((point) => ({
        name: point,
        type: 'bar',
        data: [...Array(12)].map((_, m) => {
          const items = rows.filter((r) => r.reservoirName === point && r.month === m + 1)
          if (!items.length) return 0
          const sum = items.reduce((a, b) => a + (Number(b[field]) || 0), 0)
          return Number((sum / items.length).toFixed(3))
        })
      }))
    })
  })
}
</script>
