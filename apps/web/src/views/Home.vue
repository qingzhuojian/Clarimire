<template>
  <div class="map-container">
    <div ref="mapRef" class="map-view"></div>

    <!-- 图层控制：复制地图编辑面板，去掉监测点/居民点 -->
    <div class="control-panel" :class="{ 'panel-hidden': !showControlPanel }">
      <div class="panel-header">
        <h3>图层控制</h3>
        <div class="panel-header-actions">
          <div class="basemap-switcher">
            <button
              type="button"
              class="basemap-switch-btn"
              :class="{ active: showBasemapMenu }"
              title="切换底图"
              @click.stop="toggleBasemapMenu"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="M4 6.5 12 3l8 3.5v11L12 21l-8-3.5v-11Zm8 9.2 6.5-2.9V8.3L12 11.2 5.5 8.3v4.5L12 15.7Z"
                  fill="currentColor"
                />
              </svg>
            </button>
            <div v-show="showBasemapMenu" class="basemap-menu" @click.stop>
              <label
                v-for="mode in BASEMAP_MODES"
                :key="mode.id"
                class="basemap-option"
                :class="{ active: basemapMode === mode.id }"
              >
                <input
                  type="radio"
                  name="home-basemap"
                  :value="mode.id"
                  v-model="basemapMode"
                  @change="switchBasemap(mode.id)"
                />
                <span>{{ mode.label }}</span>
              </label>
            </div>
          </div>
          <button type="button" class="panel-toggle-btn" @click="showControlPanel = !showControlPanel">
            {{ showControlPanel ? '∧' : '∨' }}
          </button>
        </div>
      </div>
      <div v-show="showControlPanel" class="panel-content">
        <div v-for="item in layerItems" :key="item.key" class="layer-control">
          <label>
            <input
              type="checkbox"
              v-model="item.visible"
              @change="toggleLayer(item.key, item.visible)"
            />
            {{ item.label }}
          </label>
          <div class="layer-color-control">
            <div
              class="color-preview"
              :style="{ backgroundColor: layerColors[item.colorKey] }"
              @click="triggerColorInput(item.colorKey)"
            ></div>
            <input
              type="color"
              :ref="(el) => setColorInputRef(item.colorKey, el)"
              v-model="layerColors[item.colorKey]"
              class="color-input"
              @change="applyLayerColor(item.key)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 左下：地图服务信息 -->
    <div class="service-info-panel">
      <h4>地图服务信息</h4>
      <div class="info-item">
        <span class="info-label">数据来源:</span>
        <span class="info-value">GeoScene FeatureServer</span>
      </div>
      <div class="info-item">
        <span class="info-label">服务名:</span>
        <span class="info-value">Beijing_Water_System</span>
      </div>
      <div class="info-item">
        <span class="info-label">底图:</span>
        <span class="info-value">天地图</span>
      </div>
    </div>

    <!-- 右下：各区水库数量统计（与左下卡片底边对齐） -->
    <div class="stats-panel">
      <h4>各区水库数量统计</h4>
      <div v-if="!districtStats.length" class="stats-empty">暂无统计数据</div>
      <div v-else class="stats-list">
        <div v-for="row in districtStats" :key="row.name" class="stats-row">
          <span class="stats-name" :title="row.name">{{ row.name }}</span>
          <div class="stats-bar-track">
            <div class="stats-bar-fill" :style="{ width: `${(row.count / maxDistrictCount) * 100}%` }"></div>
          </div>
          <span class="stats-count">{{ row.count }}</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading">正在加载地图数据...</div>
    <div v-if="layerError" class="layer-error">{{ layerError }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import Point from '@geoscene/core/geometry/Point'
import { execute as containsExecute } from '@geoscene/core/geometry/operators/containsOperator'
import { createMapView } from '@/geoscene/initMapView'
import { addBusinessLayersToMap, reorderBusinessLayers } from '@/geoscene/layers/createBusinessLayers'
import { BASEMAP_MODES, applyMapBasemap } from '@/geoscene/basemaps/basemapModes'
import {
  DEFAULT_LAYER_COLORS,
  loadBusinessLayers,
  applyLayerRenderer
} from '@/geoscene/layers/layerStyle'
import { getGeometryCentroid } from '@/geoscene/utils/getGeometryCentroid'
import { resolveNameField } from '@/geoscene/utils/resolveNameField'

/** 首页业务图层：不含监测点、居民点 */
const HOME_LAYER_KEYS = ['districts', 'waterAreas', 'waterLines', 'reservoirs']

const mapRef = ref(null)
const loading = ref(true)
const layerError = ref('')
const showControlPanel = ref(true)
const showBasemapMenu = ref(false)
const basemapMode = ref('tianditu')
const districtStats = ref([])
let view = null
let mapInstance = null
let mapAlive = true
const layerInstances = {}
const colorInputRefs = {}

const layerItems = reactive([
  { key: 'districts', label: '行政区划', visible: true, colorKey: 'districts' },
  {
    key: 'rivers',
    label: '河流',
    visible: true,
    colorKey: 'waterLines',
    layerKeys: ['waterLines', 'waterAreas']
  },
  { key: 'reservoirs', label: '水库', visible: true, colorKey: 'reservoirs' }
])

const layerColors = reactive({
  districts: DEFAULT_LAYER_COLORS.districts,
  waterLines: DEFAULT_LAYER_COLORS.waterLines,
  reservoirs: DEFAULT_LAYER_COLORS.reservoirs
})

const maxDistrictCount = computed(() =>
  Math.max(1, ...districtStats.value.map((r) => r.count))
)

const getLayerColorMap = () => ({
  districts: layerColors.districts,
  waterAreas: layerColors.waterLines,
  waterLines: layerColors.waterLines,
  reservoirs: layerColors.reservoirs
})

const setColorInputRef = (key, el) => {
  if (el) colorInputRefs[key] = el
}

const triggerColorInput = (key) => {
  colorInputRefs[key]?.click()
}

onMounted(async () => {
  await nextTick()
  document.addEventListener('click', closeBasemapMenu)
  initMap()
})

onUnmounted(() => {
  mapAlive = false
  document.removeEventListener('click', closeBasemapMenu)
  view?.destroy()
  view = null
  mapInstance = null
})

const toggleBasemapMenu = () => {
  showBasemapMenu.value = !showBasemapMenu.value
}

const closeBasemapMenu = () => {
  showBasemapMenu.value = false
}

const switchBasemap = (mode) => {
  basemapMode.value = mode
  showBasemapMenu.value = false
  if (mapInstance) applyMapBasemap(mapInstance, mode, view)
}

const initMap = async () => {
  layerError.value = ''
  try {
    const { map, view: mapView } = await createMapView(mapRef.value, {
      zoom: 10,
      basemapMode: basemapMode.value
    })
    if (!mapAlive) return
    mapInstance = map
    view = mapView
    Object.assign(
      layerInstances,
      addBusinessLayersToMap(map, {
        popupEnabled: false,
        keys: HOME_LAYER_KEYS
      })
    )

    layerItems.forEach((item) => toggleLayer(item.key, item.visible))

    const loadResults = await loadBusinessLayers(layerInstances, getLayerColorMap(), map)
    if (!mapAlive) return
    reorderBusinessLayers(map, layerInstances)

    const failedLayers = loadResults.filter((r) => !r.ok)
    if (failedLayers.length === loadResults.length) {
      layerError.value = '业务图层未加载，请确认 GeoScene 服务已启动（6443）'
      ElMessage.warning(layerError.value)
    } else if (failedLayers.length) {
      console.warn('部分图层加载失败:', failedLayers.map((r) => r.key).join(', '))
      layerError.value = `部分图层加载失败：${failedLayers.map((r) => r.key).join('、')}`
    }

    await fitMapToLayers()
    await buildDistrictStats()
  } catch (e) {
    console.error(e)
    layerError.value = '地图加载失败，请查看控制台错误信息'
    ElMessage.error('地图加载失败')
  } finally {
    if (mapAlive) loading.value = false
  }
}

const fitMapToLayers = async () => {
  if (!view) return
  const layers = HOME_LAYER_KEYS.map((key) => layerInstances[key]).filter((layer) => layer?.fullExtent)
  if (!layers.length) return
  try {
    let extent = layers[0].fullExtent.clone()
    for (let i = 1; i < layers.length; i++) {
      extent = extent.union(layers[i].fullExtent)
    }
    await view.goTo(extent.expand(1.08))
  } catch {
    // ignore
  }
}

const resolveDistrictName = (attrs = {}) => {
  const candidates = ['NAME', 'name', '名称', '区县', '区县名', 'XZQMC', 'COUNTY', '县名']
  const field = candidates.find((k) => attrs[k] != null && String(attrs[k]).trim() !== '')
  return field ? String(attrs[field]).trim() : '未知区县'
}

const buildDistrictStats = async () => {
  const reservoirLayer = layerInstances.reservoirs
  const districtLayer = layerInstances.districts
  if (!reservoirLayer || !districtLayer) {
    districtStats.value = []
    return
  }

  try {
    const [reservoirResult, districtResult] = await Promise.all([
      reservoirLayer.queryFeatures({ where: '1=1', outFields: ['*'], returnGeometry: true }),
      districtLayer.queryFeatures({ where: '1=1', outFields: ['*'], returnGeometry: true })
    ])
    if (!mapAlive) return

    const counts = {}
    districtResult.features.forEach((d) => {
      const name = resolveDistrictName(d.attributes || {})
      counts[name] = counts[name] || 0
    })

    reservoirResult.features.forEach((feature) => {
      const centroid = getGeometryCentroid(feature.geometry)
      if (!centroid) return

      const point =
        centroid.type === 'point'
          ? centroid
          : new Point({
              x: centroid.x,
              y: centroid.y,
              spatialReference: feature.geometry.spatialReference
            })

      let matched = '未分区'
      for (const district of districtResult.features) {
        if (!district.geometry) continue
        try {
          if (containsExecute(district.geometry, point)) {
            matched = resolveDistrictName(district.attributes || {})
            break
          }
        } catch {
          // ignore geometry mismatch
        }
      }
      counts[matched] = (counts[matched] || 0) + 1
    })

    // 若空间归属失败，按水库名称字段兜底计数
    const totalMatched = Object.values(counts).reduce((s, n) => s + n, 0)
    if (!totalMatched && reservoirResult.features.length) {
      const fallback = {}
      reservoirResult.features.forEach((f) => {
        const { value } = resolveNameField(f.attributes || {})
        const key = value || '未命名'
        fallback[key] = (fallback[key] || 0) + 1
      })
      districtStats.value = Object.entries(fallback)
        .map(([name, count]) => ({ name, count }))
        .sort((a, b) => b.count - a.count)
      return
    }

    districtStats.value = Object.entries(counts)
      .filter(([, count]) => count > 0)
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
  } catch (e) {
    console.warn('各区水库统计失败', e)
    districtStats.value = []
  }
}

const toggleLayer = (key, visible) => {
  const item = layerItems.find((l) => l.key === key)
  if (!item) return
  const nextVisible = visible ?? item.visible
  item.visible = nextVisible
  const keys = item.layerKeys || [key]
  keys.forEach((layerKey) => {
    if (layerInstances[layerKey]) layerInstances[layerKey].visible = nextVisible
  })
}

const applyLayerColor = (key) => {
  if (key === 'rivers') {
    if (layerInstances.waterLines) {
      applyLayerRenderer(layerInstances.waterLines, 'waterLines', layerColors.waterLines)
    }
    if (layerInstances.waterAreas) {
      applyLayerRenderer(layerInstances.waterAreas, 'waterAreas', layerColors.waterLines)
    }
    return
  }
  const layer = layerInstances[key]
  if (layer && layerColors[key]) applyLayerRenderer(layer, key, layerColors[key])
}
</script>

<style scoped>
.map-container {
  flex: 1;
  position: relative;
  height: calc(100vh - 56px);
  background: white;
  overflow: hidden;
}

.map-view {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.control-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(232, 232, 232, 0.8);
  padding: 16px 20px;
  z-index: 1000;
  min-width: 220px;
  max-width: 280px;
  transition: all 0.3s ease;
}

.control-panel.panel-hidden {
  min-width: auto;
  padding: 10px 14px;
}

.control-panel.panel-hidden .panel-header {
  margin-bottom: 0;
  justify-content: center;
}

.control-panel.panel-hidden .panel-header h3 {
  display: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.panel-header h3 {
  margin: 0;
  color: #262626;
  font-size: 15px;
  font-weight: 600;
}

.basemap-switcher {
  position: relative;
}

.basemap-switch-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #595959;
  cursor: pointer;
  transition: all 0.2s;
}

.basemap-switch-btn svg {
  width: 18px;
  height: 18px;
}

.basemap-switch-btn:hover,
.basemap-switch-btn.active {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.basemap-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 132px;
  padding: 6px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  z-index: 1300;
}

.basemap-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  color: #262626;
  font-size: 13px;
}

.basemap-option:hover,
.basemap-option.active {
  background: #e6f7ff;
  color: #1890ff;
}

.basemap-option input {
  margin: 0;
}

.panel-toggle-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #595959;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
}

.panel-toggle-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.layer-control {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.layer-control:last-child {
  margin-bottom: 0;
}

.layer-control label {
  display: flex;
  align-items: center;
  color: #262626;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
}

.layer-control input[type='checkbox'] {
  margin-right: 8px;
}

.layer-color-control {
  position: relative;
  display: flex;
  align-items: center;
}

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 2px solid #ddd;
  cursor: pointer;
}

.color-input {
  position: absolute;
  width: 24px;
  height: 24px;
  opacity: 0;
  pointer-events: none;
}

.service-info-panel,
.stats-panel {
  position: absolute;
  bottom: 20px;
  background: rgba(255, 255, 255, 0.98);
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(232, 232, 232, 0.8);
  z-index: 1000;
}

.service-info-panel {
  left: 20px;
  max-width: 280px;
}

.stats-panel {
  right: 20px;
  width: 300px;
  max-height: 280px;
  display: flex;
  flex-direction: column;
}

.service-info-panel h4,
.stats-panel h4 {
  margin: 0 0 10px 0;
  color: #262626;
  font-size: 0.95rem;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  margin-bottom: 8px;
  font-size: 0.8rem;
  line-height: 1.3;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: bold;
  color: #555;
  margin-right: 6px;
  display: inline-block;
  min-width: 65px;
}

.info-value {
  color: #333;
  word-break: break-all;
  display: inline-block;
  max-width: 200px;
}

.stats-empty {
  font-size: 13px;
  color: #999;
  padding: 8px 0;
}

.stats-list {
  overflow-y: auto;
  flex: 1;
  min-height: 0;
  padding-right: 4px;
}

.stats-row {
  display: grid;
  grid-template-columns: 72px 1fr 28px;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.stats-row:last-child {
  margin-bottom: 0;
}

.stats-name {
  font-size: 12px;
  color: #444;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-bar-track {
  height: 10px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.stats-bar-fill {
  height: 100%;
  background: #e8483c;
  border-radius: 4px;
  min-width: 2px;
  transition: width 0.3s ease;
}

.stats-count {
  font-size: 12px;
  color: #333;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 20px 40px;
  border-radius: 8px;
  z-index: 2000;
  font-size: 1.1rem;
}

.layer-error {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff7e6;
  color: #ad6800;
  border: 1px solid #ffd591;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  z-index: 1500;
  max-width: 90%;
  text-align: center;
}

@media (max-width: 768px) {
  .control-panel {
    top: 10px;
    left: 10px;
    min-width: 180px;
    padding: 12px;
  }

  .service-info-panel,
  .stats-panel {
    bottom: 10px;
    max-width: 46%;
    padding: 12px;
  }

  .service-info-panel {
    left: 10px;
  }

  .stats-panel {
    right: 10px;
    width: auto;
    max-height: 220px;
  }

  .stats-row {
    grid-template-columns: 56px 1fr 24px;
    gap: 6px;
  }
}
</style>
