<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="card">
          <div class="card-title">水情统计分析</div>
          <div class="search-form">
            <el-select v-model="searchForm.reservoirName" placeholder="选择水库" clearable style="width: 200px;">
              <el-option v-for="item in reservoirs" :key="item" :label="item" :value="item" />
            </el-select>
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px;"
            />
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
          <div ref="chartRef" style="height: 400px;"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="card">
          <div class="card-title">水质指标分析</div>
          <div ref="waterQualityChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-title">水库容量对比</div>
          <div ref="capacityChartRef" style="height: 350px;"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { waterSituationAPI, sectionMonitorAPI } from '@/api'

const chartRef = ref(null)
const waterQualityChartRef = ref(null)
const capacityChartRef = ref(null)

let mainChart = null
let waterQualityChart = null
let capacityChart = null

const reservoirs = ref([])
const searchForm = reactive({
  reservoirName: '',
  dateRange: []
})

onMounted(() => {
  initCharts()
  loadReservoirs()
  loadData()
})

onUnmounted(() => {
  mainChart?.dispose()
  waterQualityChart?.dispose()
  capacityChart?.dispose()
})

const initCharts = () => {
  mainChart = echarts.init(chartRef.value)
  waterQualityChart = echarts.init(waterQualityChartRef.value)
  capacityChart = echarts.init(capacityChartRef.value)

  // 主趋势图配置
  mainChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['库水位', '蓄水量', '入库流量'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: [
      { type: 'value', name: '水位(m)' },
      { type: 'value', name: '流量(m³/s)' }
    ],
    series: [
      { name: '库水位', type: 'line', data: [145.5, 144.8, 146.2, 147.5, 148.0, 147.8] },
      { name: '蓄水量', type: 'line', yAxisIndex: 1, data: [28500, 27800, 29500, 31000, 32000, 31500] },
      { name: '入库流量', type: 'bar', data: [12.5, 10.2, 15.8, 18.5, 20.0, 16.5] }
    ]
  })

  // 水质指标配置
  waterQualityChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总氮', '总磷', '氨氮'] },
    radar: {
      indicator: [
        { name: '总氮', max: 2.0 },
        { name: '总磷', max: 0.1 },
        { name: '氨氮', max: 1.0 },
        { name: 'COD', max: 20 },
        { name: '高锰酸盐', max: 8 }
      ]
    },
    series: [{
      type: 'radar',
      data: [{
        value: [1.2, 0.03, 0.4, 12, 3.2],
        name: '水质指标'
      }]
    }]
  })

  // 水库容量配置
  capacityChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'bar',
      data: [
        { value: 28500, itemStyle: { color: '#1890ff' } },
        { value: 5600, itemStyle: { color: '#52c41a' } },
        { value: 850, itemStyle: { color: '#faad14' } },
        { value: 350, itemStyle: { color: '#f5222d' } }
      ]
    }],
    xAxis: { type: 'category', data: ['密云水库', '官厅水库', '怀柔水库', '十三陵水库'] },
    yAxis: { type: 'value', name: '蓄水量(万m³)' }
  })
}

const loadReservoirs = async () => {
  try {
    const res = await waterSituationAPI.getReservoirs()
    if (res.code === 200) {
      reservoirs.value = res.data
    }
  } catch (error) {
    console.error('Load reservoirs error:', error)
  }
}

const loadData = async () => {
  // 数据加载逻辑
}

const resetSearch = () => {
  searchForm.reservoirName = ''
  searchForm.dateRange = []
}
</script>

<style scoped>
.statistics-container {
  padding: 0;
}
</style>
