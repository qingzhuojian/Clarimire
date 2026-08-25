<template>
  <div class="dev-panel" v-if="visible">
    <div class="dev-header" @click="collapsed = !collapsed" title="开发环境：手动模拟 GPS 坐标，用于测试打卡区域">开发 · 模拟坐标 {{ collapsed ? '▸' : '▾' }}</div>
    <div v-if="!collapsed" class="dev-body">
      <input v-model.number="lat" class="input" placeholder="纬度 lat" />
      <input v-model.number="lng" class="input" placeholder="经度 lng" />
      <button class="btn btn-secondary" @click="apply">应用坐标</button>
      <button class="btn btn-secondary" @click="preset('miyun')">密云核心</button>
      <button class="btn btn-secondary" @click="preset('buffer')">缓冲圈</button>
      <button class="btn btn-secondary" @click="preset('remote')">异地江苏</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const visible = import.meta.env.DEV
const collapsed = ref(true)
const lat = ref(40.485)
const lng = ref(116.845)

const presets = {
  miyun: { lat: 40.485, lng: 116.845 },
  buffer: { lat: 40.488, lng: 116.850 },
  remote: { lat: 32.060, lng: 118.796 }
}

const apply = () => {
  localStorage.setItem('devLat', String(lat.value))
  localStorage.setItem('devLng', String(lng.value))
  window.dispatchEvent(new CustomEvent('dev-location', { detail: { lat: lat.value, lng: lng.value } }))
}

const preset = (key) => {
  const p = presets[key]
  lat.value = p.lat
  lng.value = p.lng
  apply()
}
</script>

<style scoped>
.dev-panel {
  position: fixed;
  right: 8px;
  bottom: calc(12px + env(safe-area-inset-bottom));
  z-index: 9999;
  width: 168px;
  background: #1f1f1f;
  color: #fff;
  border-radius: 10px;
  font-size: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}
.dev-header { padding: 8px; cursor: pointer; font-weight: 600; }
.dev-body { padding: 0 8px 8px; }
.dev-body .input { font-size: 12px; padding: 6px; margin-bottom: 6px; }
.dev-body .btn { font-size: 12px; padding: 6px; margin-top: 4px; }
</style>
