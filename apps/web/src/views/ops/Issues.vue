<template>
  <div class="ops-page">
    <div class="page-card">
    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待审核" value="pending" />
        <el-option label="审核中" value="reviewing" />
        <el-option label="已指派" value="assigned" />
        <el-option label="已解决" value="resolved" />
        <el-option label="已关闭" value="closed" />
      </el-select>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="tableData" stripe border>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="issueType" label="类型" min-width="100" />
      <el-table-column prop="reporterName" label="上报人" min-width="110" />
      <el-table-column prop="reservoirName" label="水库" min-width="130" />
      <el-table-column prop="status" label="状态" min-width="110" />
      <el-table-column prop="createTime" label="时间" min-width="180" />
      <el-table-column label="操作" min-width="220" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row, 'approve')">审核</el-button>
          <el-button link type="warning" @click="openAssign(row)">派单</el-button>
          <el-button link type="success" @click="doReview(row, 'resolve')">结案</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="assignVisible" title="指派巡查员" width="400px">
      <el-select v-model="assigneeId" style="width:100%">
        <el-option v-for="u in inspectors" :key="u.id" :label="u.realName" :value="u.id" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认派单</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { issueAPI, systemAPI } from '@/api'

const tableData = ref([])
const inspectors = ref([])
const filters = reactive({ status: '' })
const assignVisible = ref(false)
const assigneeId = ref(null)
const currentRow = ref(null)

const loadData = async () => {
  const res = await issueAPI.list(filters)
  tableData.value = res.data || []
}

const loadInspectors = async () => {
  const res = await systemAPI.getUsers({ role: 'inspector' })
  inspectors.value = res.data || []
}

const doReview = async (row, action) => {
  await issueAPI.review({ id: row.id, action })
  ElMessage.success('操作成功')
  loadData()
}

const openReview = (row, action) => doReview(row, action)

const openAssign = (row) => {
  currentRow.value = row
  assigneeId.value = null
  assignVisible.value = true
}

const confirmAssign = async () => {
  if (!assigneeId.value) return ElMessage.warning('请选择巡查员')
  await issueAPI.review({ id: currentRow.value.id, action: 'assign', assigneeId: assigneeId.value })
  ElMessage.success('已派单并生成任务')
  assignVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
  loadInspectors()
})
</script>
