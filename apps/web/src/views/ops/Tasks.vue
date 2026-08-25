<template>
  <div class="ops-page">
    <div class="page-card">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8"><el-statistic title="待办任务" :value="stats.pendingTasks" /></el-col>
      <el-col :span="8"><el-statistic title="待审核问题" :value="stats.pendingIssues" /></el-col>
      <el-col :span="8"><el-statistic title="进行中" :value="stats.inProgressTasks" /></el-col>
    </el-row>

    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待处理" value="pending" />
        <el-option label="已指派" value="assigned" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
      </el-select>
      <el-button type="primary" @click="openDialog()">新建任务</el-button>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="tableData" stripe>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="taskType" label="类型" width="100" />
      <el-table-column prop="reservoirName" label="水库" width="120" />
      <el-table-column prop="assigneeName" label="巡查员" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="dueTime" label="截止时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑任务' : '新建任务'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.taskType" style="width:100%">
            <el-option label="日常" value="daily" />
            <el-option label="指派" value="assigned" />
            <el-option label="突发" value="emergency" />
          </el-select>
        </el-form-item>
        <el-form-item label="水库"><el-input v-model="form.reservoirName" /></el-form-item>
        <el-form-item label="巡查员">
          <el-select v-model="form.assigneeId" style="width:100%" @change="onAssigneeChange">
            <el-option v-for="u in inspectors" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待处理" value="pending" />
            <el-option label="已指派" value="assigned" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.dueTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patrolAPI, systemAPI } from '@/api'

const tableData = ref([])
const inspectors = ref([])
const dialogVisible = ref(false)
const stats = reactive({ pendingTasks: 0, pendingIssues: 0, inProgressTasks: 0 })
const filters = reactive({ status: '' })
const form = reactive({
  id: null, title: '', taskType: 'assigned', reservoirName: '', assigneeId: null,
  assigneeName: '', status: 'assigned', dueTime: '', description: ''
})

const loadData = async () => {
  const [taskRes, dashRes] = await Promise.all([
    patrolAPI.getTasks(filters),
    patrolAPI.getDashboard()
  ])
  tableData.value = taskRes.data || []
  Object.assign(stats, dashRes.data || {})
}

const loadInspectors = async () => {
  const res = await systemAPI.getUsers({ role: 'inspector' })
  inspectors.value = res.data || []
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: null, title: '', taskType: 'assigned', reservoirName: '', assigneeId: null,
      assigneeName: '', status: 'assigned', dueTime: '', description: ''
    })
  }
  dialogVisible.value = true
}

const onAssigneeChange = (id) => {
  const u = inspectors.value.find(i => i.id === id)
  form.assigneeName = u ? u.realName : ''
}

const save = async () => {
  if (form.id) {
    await patrolAPI.updateTask(form)
  } else {
    await patrolAPI.createTask(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该任务？')
  await patrolAPI.deleteTask(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => {
  loadData()
  loadInspectors()
})
</script>
