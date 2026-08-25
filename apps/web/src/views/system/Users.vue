<template>
  <div class="ops-page">
    <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增用户</el-button>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="tableData" stripe border>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="100" />
      <el-table-column prop="role" label="角色" min-width="100" />
      <el-table-column prop="phone" label="电话" min-width="130" />
      <el-table-column label="异地打卡" min-width="110" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.allowRemoteCheckin" :active-value="1" :inactive-value="0" @change="saveRow(row)" />
        </template>
      </el-table-column>
      <el-table-column label="移动端" min-width="100" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.mobileEnabled" :active-value="1" :inactive-value="0" @change="saveRow(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="140" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)" :disabled="row.username === 'admin'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item v-if="!form.id" label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="admin" />
            <el-option label="巡查员" value="inspector" />
            <el-option label="群众" value="public" />
          </el-select>
        </el-form-item>
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
import { systemAPI } from '@/api'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null, username: '', password: '', realName: '', phone: '', role: 'inspector'
})

const loadData = async () => {
  const res = await systemAPI.getUsers()
  tableData.value = res.data || []
}

const openDialog = (row) => {
  if (row) Object.assign(form, { ...row, password: '' })
  else Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 'inspector' })
  dialogVisible.value = true
}

const save = async () => {
  if (form.id) await systemAPI.updateUser(form)
  else await systemAPI.createUser(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const saveRow = async (row) => {
  await systemAPI.updateUser(row)
  ElMessage.success('已更新')
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除？')
  await systemAPI.deleteUser(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>
