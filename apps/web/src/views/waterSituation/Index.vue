<template>
  <div class="dm-list-page water-situation-list">
    <div class="search-bar">
      <div class="search-bar-left">
        <el-form :model="searchForm" inline>
          <el-form-item label="库名">
            <el-select v-model="searchForm.reservoirName" placeholder="选择水库" clearable style="width: 180px;" @change="loadData">
              <el-option v-for="item in reservoirs" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px;"
              @change="loadData"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-bar-actions">
        <el-button type="primary" @click="handleImport">
          <el-icon><Upload /></el-icon>
          数据导入
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          数据导出
        </el-button>
        <el-button type="primary" @click="openDialog('add')">
          <el-icon><Plus /></el-icon>
          新增水情
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="reservoirName" label="库名" min-width="120" />
        <el-table-column prop="date" label="日期" min-width="120">
          <template #default="{ row }">{{ formatDate(row.date) }}</template>
        </el-table-column>
        <el-table-column prop="waterLevel" label="库水位(米)" min-width="110" />
        <el-table-column prop="storage" label="蓄水量(万立方米)" min-width="130" />
        <el-table-column prop="avgInflow" label="日平均入库流量(立方米/秒)" min-width="170" />
        <el-table-column prop="avgOutflow" label="日平均出库流量(立方米/秒)" min-width="170" />
        <el-table-column prop="yoyIncrease" label="比去年同期增减(万立方米)" min-width="180" />
        <el-table-column prop="totalCapacity" label="总库容(万立方米)" min-width="140" />
        <el-table-column prop="floodLevel" label="汛限水位(米)" min-width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog('edit', row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="库名" prop="reservoirName">
          <el-select v-model="form.reservoirName" placeholder="选择水库" style="width: 100%;">
            <el-option v-for="item in reservoirs" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker v-model="form.date" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="库水位(m)" prop="waterLevel">
          <el-input-number v-model="form.waterLevel" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="蓄水量(万m³)" prop="storage">
          <el-input-number v-model="form.storage" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="入库流量(m³/s)" prop="avgInflow">
          <el-input-number v-model="form.avgInflow" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="出库流量(m³/s)" prop="avgOutflow">
          <el-input-number v-model="form.avgOutflow" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="同比增减(万m³)" prop="yoyIncrease">
          <el-input-number v-model="form.yoyIncrease" :precision="2" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入数据" width="400px">
      <el-upload
        ref="uploadRef"
        drag
        :action="importUrl"
        :headers="{ Authorization: `Bearer ${token}` }"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :auto-upload="false"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传 xlsx/xls 文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImport">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { waterSituationAPI } from '@/api'
import { downloadBlob } from '@/utils/download'
import { Plus, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import '@/assets/data-list-page.css'

const loading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogTitle = ref('新增水情')
const formRef = ref(null)
const uploadRef = ref(null)
const allData = ref([])
const reservoirs = ref([])
const token = localStorage.getItem('token') || ''
const importUrl = '/api/waterSituation/import'

const searchForm = reactive({
  reservoirName: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return allData.value.slice(start, start + pagination.size)
})

const handlePageChange = () => {}

const form = reactive({
  id: null,
  reservoirName: '',
  date: '',
  waterLevel: null,
  storage: null,
  avgInflow: null,
  avgOutflow: null,
  yoyIncrease: null
})

const rules = {
  reservoirName: [{ required: true, message: '请选择水库', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(() => {
  loadData()
  loadReservoirs()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      reservoirName: searchForm.reservoirName || null,
      startDate: searchForm.dateRange?.[0] || null,
      endDate: searchForm.dateRange?.[1] || null
    }
    const res = await waterSituationAPI.list(params)
    if (res.code === 200) {
      allData.value = res.data || []
      pagination.total = allData.value.length
      if ((pagination.page - 1) * pagination.size >= pagination.total && pagination.page > 1) {
        pagination.page = 1
      }
    }
  } catch (error) {
    console.error('Load data error:', error)
  } finally {
    loading.value = false
  }
}

const loadReservoirs = async () => {
  try {
    const res = await waterSituationAPI.getReservoirs()
    if (res.code === 200) reservoirs.value = res.data
  } catch (error) {
    console.error('Load reservoirs error:', error)
  }
}

const resetSearch = () => {
  searchForm.reservoirName = ''
  searchForm.dateRange = []
  loadData()
}

const openDialog = (type, row = null) => {
  if (type === 'add') {
    dialogTitle.value = '新增水情'
    Object.keys(form).forEach((key) => {
      form[key] = key === 'date' ? new Date() : null
    })
    form.id = null
  } else {
    dialogTitle.value = '编辑水情'
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const api = form.id ? waterSituationAPI.update : waterSituationAPI.create
      const res = await api(form)
      if (res.code === 200) {
        ElMessage.success(dialogTitle.value + '成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('Submit error:', error)
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该数据？', '提示', { type: 'warning' })
    const res = await waterSituationAPI.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleImport = () => {
  importDialogVisible.value = true
}

const submitImport = () => {
  uploadRef.value?.submit()
}

const handleImportSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success(res.message || '导入成功')
    importDialogVisible.value = false
    loadData()
  } else {
    ElMessage.error(res.message || '导入失败')
  }
}

const handleImportError = () => {
  ElMessage.error('导入失败')
}

const handleExport = async () => {
  try {
    const blob = await waterSituationAPI.export([])
    downloadBlob(blob, '水情数据.xlsx')
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>
