<template>
  <div class="dm-list-page section-monitor-list">
    <div class="search-bar">
      <div class="search-bar-left">
        <el-form :model="searchForm" inline>
          <el-form-item label="水库名称">
            <el-select v-model="searchForm.reservoirName" placeholder="选择水库" clearable style="width: 180px;" @change="loadData">
              <el-option v-for="item in reservoirs" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="年份">
            <el-select v-model="searchForm.year" placeholder="选择年份" clearable style="width: 120px;" @change="loadData">
              <el-option v-for="item in years" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="月份">
            <el-select v-model="searchForm.month" placeholder="选择月份" clearable style="width: 100px;" @change="loadData">
              <el-option v-for="item in 12" :key="item" :label="`${item}月`" :value="item" />
            </el-select>
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
          新增监测断面
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="monitorPointName" label="监测点名称" min-width="140" />
        <el-table-column prop="reservoirName" label="水库名称" min-width="120" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="ammoniaNitrogen" label="氨氮(mg/L)" min-width="110" />
        <el-table-column prop="potassiumPermanganate" label="高锰酸盐指数(mg/L)" min-width="150" />
        <el-table-column prop="cod" label="化学需氧量(mg/L)" min-width="140" />
        <el-table-column prop="flow" label="流量(m³/s)" min-width="110" />
        <el-table-column prop="waterDepth" label="水深(m)" min-width="100" />
        <el-table-column prop="totalNitrogen" label="总氮(mg/L)" min-width="110" />
        <el-table-column prop="totalPhosphorus" label="总磷(mg/L)" min-width="110" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="监测点名称" prop="monitorPointName">
              <el-input v-model="form.monitorPointName" placeholder="请输入监测点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="水库名称" prop="reservoirName">
              <el-select v-model="form.reservoirName" placeholder="选择水库" style="width: 100%;">
                <el-option v-for="item in reservoirs" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年份" prop="year">
              <el-input-number v-model="form.year" :min="2000" :max="2100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月份" prop="month">
              <el-input-number v-model="form.month" :min="1" :max="12" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="溶解氧(mg/L)" prop="oxygen">
              <el-input-number v-model="form.oxygen" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高锰酸盐指数" prop="potassiumPermanganate">
              <el-input-number v-model="form.potassiumPermanganate" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="COD(mg/L)" prop="cod">
              <el-input-number v-model="form.cod" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流量(m³/s)" prop="flow">
              <el-input-number v-model="form.flow" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="水深(m)" prop="waterDepth">
              <el-input-number v-model="form.waterDepth" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总氮(mg/L)" prop="totalNitrogen">
              <el-input-number v-model="form.totalNitrogen" :precision="3" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总磷(mg/L)" prop="totalPhosphorus">
              <el-input-number v-model="form.totalPhosphorus" :precision="3" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="氨氮(mg/L)" prop="ammoniaNitrogen">
              <el-input-number v-model="form.ammoniaNitrogen" :precision="3" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { sectionMonitorAPI } from '@/api'
import { downloadBlob } from '@/utils/download'
import { Plus, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import '@/assets/data-list-page.css'

const loading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogTitle = ref('新增监测断面')
const formRef = ref(null)
const uploadRef = ref(null)
const allData = ref([])
const reservoirs = ref([])
const years = ref([2024, 2023, 2022])
const token = localStorage.getItem('token') || ''
const importUrl = '/api/sectionMonitor/import'

const searchForm = reactive({
  reservoirName: '',
  year: null,
  month: null
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
  monitorPointName: '',
  reservoirName: '',
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  oxygen: null,
  potassiumPermanganate: null,
  cod: null,
  flow: null,
  waterDepth: null,
  totalNitrogen: null,
  totalPhosphorus: null,
  ammoniaNitrogen: null
})

const rules = {
  monitorPointName: [{ required: true, message: '请输入监测点名称', trigger: 'blur' }],
  reservoirName: [{ required: true, message: '请选择水库', trigger: 'change' }],
  year: [{ required: true, message: '请输入年份', trigger: 'blur' }],
  month: [{ required: true, message: '请输入月份', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadReservoirs()
  loadYears()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      reservoirName: searchForm.reservoirName || null,
      year: searchForm.year || null,
      month: searchForm.month || null
    }
    const res = await sectionMonitorAPI.list(params)
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
    const res = await sectionMonitorAPI.getReservoirs()
    if (res.code === 200) reservoirs.value = res.data
  } catch (error) {
    console.error('Load reservoirs error:', error)
  }
}

const loadYears = async () => {
  try {
    const res = await sectionMonitorAPI.getYears()
    if (res.code === 200) years.value = res.data
  } catch (error) {
    console.error('Load years error:', error)
  }
}

const resetSearch = () => {
  searchForm.reservoirName = ''
  searchForm.year = null
  searchForm.month = null
  loadData()
}

const openDialog = (type, row = null) => {
  if (type === 'add') {
    dialogTitle.value = '新增监测断面'
    Object.keys(form).forEach((key) => {
      if (key === 'year') form[key] = new Date().getFullYear()
      else if (key === 'month') form[key] = new Date().getMonth() + 1
      else form[key] = null
    })
    form.id = null
  } else {
    dialogTitle.value = '编辑监测断面'
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const api = form.id ? sectionMonitorAPI.update : sectionMonitorAPI.create
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
    const res = await sectionMonitorAPI.delete(id)
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
    const blob = await sectionMonitorAPI.export([])
    downloadBlob(blob, '监测断面数据.xlsx')
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}
</script>
