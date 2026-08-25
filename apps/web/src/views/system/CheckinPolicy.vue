<template>
  <div class="ops-page ops-page--center">
    <div class="page-card policy-panel">
    <el-form :model="form" label-width="160px">
      <el-form-item label="核心区半径(米)">
        <el-input-number v-model="form.coreRadiusM" :min="50" :max="2000" />
      </el-form-item>
      <el-form-item label="缓冲圈半径(米)">
        <el-input-number v-model="form.bufferRadiusM" :min="100" :max="5000" />
      </el-form-item>
      <el-form-item label="允许异地补卡">
        <el-switch v-model="form.remoteEnabled" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="演示模式">
        <el-switch v-model="form.demoMode" :active-value="1" :inactive-value="0" />
        <span class="policy-hint">开启后所有打卡视为核心区</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存策略</el-button>
      </el-form-item>
    </el-form>

    <el-divider />
    <p class="policy-note">说明：核心区=距水库参考点 ≤ 核心区半径；缓冲圈=核心区半径～缓冲圈半径；超出为异地。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { systemAPI } from '@/api'

const form = reactive({
  id: null, coreRadiusM: 200, bufferRadiusM: 500, remoteEnabled: 1, demoMode: 0
})

const loadData = async () => {
  const res = await systemAPI.getCheckinPolicy()
  if (res.data) Object.assign(form, res.data)
}

const save = async () => {
  await systemAPI.updateCheckinPolicy(form)
  ElMessage.success('策略已保存')
}

onMounted(loadData)
</script>
