<template>
  <div class="ops-page">
    <div class="page-card">
    <div class="toolbar">
      <el-select v-model="filters.locationZone" placeholder="区域" clearable style="width:120px" @change="loadData">
        <el-option label="核心区" value="core" />
        <el-option label="缓冲圈" value="buffer" />
        <el-option label="异地" value="remote" />
      </el-select>
      <el-input v-model="filters.reservoirName" placeholder="水库名称" clearable style="width:160px" @clear="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" stripe>
      <el-table-column prop="realName" label="巡查员" width="100" />
      <el-table-column prop="reservoirName" label="水库" width="120" />
      <el-table-column prop="locationZone" label="区域" width="90">
        <template #default="{ row }">
          <el-tag :type="zoneType(row.locationZone)">{{ zoneLabel(row.locationZone) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="distanceM" label="距离(m)" width="90" />
      <el-table-column prop="checkinMode" label="模式" width="80" />
      <el-table-column prop="lat" label="纬度" width="110" />
      <el-table-column prop="lng" label="经度" width="110" />
      <el-table-column prop="remark" label="备注" min-width="120" />
      <el-table-column prop="createTime" label="打卡时间" width="170" />
    </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { patrolAPI } from '@/api'

const tableData = ref([])
const filters = reactive({ locationZone: '', reservoirName: '' })

const zoneLabel = (z) => ({ core: '核心', buffer: '缓冲', remote: '异地' }[z] || z)
const zoneType = (z) => ({ core: 'success', buffer: 'warning', remote: 'danger' }[z] || 'info')

const loadData = async () => {
  const res = await patrolAPI.getRecords(filters)
  tableData.value = res.data || []
}

onMounted(loadData)
</script>
