<template>
  <div class="simulation-page">
    <div class="simulation-page-content">
      <div ref="mapRef" class="sim-map"></div>

      <div class="layer-control-panel">
        <h3>图层控制</h3>
        <div v-for="item in layerControls" :key="item.key" class="layer-control">
          <label>
            <input type="checkbox" v-model="item.visible" @change="toggleLayer(item.key, item.visible)" />
            {{ item.name }}
          </label>
          <div
            class="color-preview"
            :style="{ backgroundColor: DEFAULT_LAYER_COLORS[item.colorKey] }"
          ></div>
        </div>
      </div>

      <div class="simulation-panel">
        <h4>污染扩散模拟</h4>

        <div class="mode-switch">
          <button type="button" class="mode-chip" :class="{ active: mode === 'lake' }" @click="setMode('lake')">
            湖泊模拟
          </button>
          <button type="button" class="mode-chip river" :class="{ active: mode === 'river' }" @click="setMode('river')">
            河流模拟
          </button>
        </div>

        <p class="simulation-hint">
          <template v-if="mode === 'lake'">
            在地图上点击水库面选择污染源位置；仅用下拉选库时才默认库心。设好参数后开始模拟。
          </template>
          <template v-else>
            点击水系线选择河流（线会被点亮），并在线上点选污染源（品红菱形）；未点选时默认河段起点。
          </template>
        </p>

        <div v-if="mode === 'lake'" class="panel-section">
          <div class="panel-section-title">湖泊模拟参数</div>
          <div class="form-row">
            <label>污染源水库</label>
            <el-select
              v-model="lake.reservoir"
              clearable
              placeholder="请选择水库"
              style="width: 100%"
              @change="onLakeReservoirChange"
            >
              <el-option v-for="n in reservoirs" :key="n" :label="n" :value="n" />
            </el-select>
          </div>
          <div class="form-row">
            <label>污染源位置</label>
            <el-input
              :model-value="
                lake.source
                  ? `${lake.source.lat.toFixed(5)}, ${lake.source.lng.toFixed(5)}`
                  : '请在地图上点击水库面选择污染源'
              "
              readonly
            />
          </div>
          <div class="form-row">
            <label>污染物类型</label>
            <el-select v-model="lake.pollutant" style="width: 100%">
              <el-option label="氨氮" value="ammonia" />
              <el-option label="总磷" value="phosphorus" />
              <el-option label="COD" value="cod" />
            </el-select>
          </div>
          <div class="form-row">
            <label>泄漏量 (kg)</label>
            <el-input-number v-model="lake.mass" :min="10" :max="100000" style="width: 100%" />
          </div>
          <div class="form-row">
            <label>持续时间 (h)</label>
            <el-slider v-model="lake.hours" :min="1" :max="72" show-input />
          </div>
          <div class="form-row">
            <label>流速 (m/s)</label>
            <el-input-number v-model="lake.velocity" :min="0.05" :max="5" :step="0.1" style="width: 100%" />
          </div>
          <div class="form-row">
            <label>扩散系数</label>
            <el-input-number v-model="lake.diffusivity" :min="0.1" :max="50" :step="0.5" style="width: 100%" />
          </div>
        </div>

        <div v-else class="panel-section">
          <div class="panel-section-title">河流模拟参数</div>
          <div class="form-row">
            <label>已选河流</label>
            <el-input :model-value="river.name || '请在地图上点击水系线'" readonly />
          </div>
          <div class="form-row">
            <label>污染源位置</label>
            <el-input
              :model-value="
                river.source
                  ? `${river.source.lat.toFixed(5)}, ${river.source.lng.toFixed(5)}`
                  : '默认取河段起点，或再点线上设源'
              "
              readonly
            />
          </div>
          <div class="form-row">
            <label>污染物类型</label>
            <el-select v-model="river.pollutant" style="width: 100%">
              <el-option label="氨氮" value="ammonia" />
              <el-option label="总磷" value="phosphorus" />
              <el-option label="COD" value="cod" />
            </el-select>
          </div>
          <div class="form-row">
            <label>污染物质量 (kg)</label>
            <el-input-number v-model="river.mass" :min="1" :max="50000" style="width: 100%" />
          </div>
          <div class="form-row">
            <label>流速 (m/s)</label>
            <el-input-number v-model="river.velocity" :min="0.1" :max="10" :step="0.1" style="width: 100%" />
          </div>
          <div class="form-row">
            <label>纵向弥散系数</label>
            <el-input-number v-model="river.diffusivity" :min="0.5" :max="100" :step="0.5" style="width: 100%" />
          </div>
          <div class="form-row">
            <label>河宽 / 水深 (m)</label>
            <div class="inline-nums">
              <el-input-number v-model="river.width" :min="1" :max="500" />
              <el-input-number v-model="river.depth" :min="0.5" :max="50" :step="0.5" />
            </div>
          </div>
          <div class="form-row">
            <label>模拟时长 (h)</label>
            <el-slider v-model="river.hours" :min="1" :max="72" show-input />
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" :loading="running" @click="startSim">开始模拟</el-button>
          <el-button @click="resetSim">重置</el-button>
        </div>

        <div v-if="hasResult && frames.length > 1" class="panel-section">
          <div class="panel-section-title">时间控制</div>
          <div class="anim-row">
            <el-button size="small" @click="togglePlay">{{ playing ? '暂停' : '播放' }}</el-button>
            <el-slider
              v-model="frameIdx"
              :min="0"
              :max="Math.max(0, frames.length - 1)"
              :show-tooltip="false"
              @change="renderFrame"
            />
            <el-select
              v-model="playSpeed"
              size="small"
              class="play-speed-select"
              @change="onPlaySpeedChange"
            >
              <el-option :value="0.5" label="0.5×" />
              <el-option :value="1" label="1×" />
              <el-option :value="2" label="2×" />
              <el-option :value="4" label="4×" />
            </el-select>
          </div>
          <div class="anim-meta">当前 {{ frames[frameIdx]?.label || '-' }} · 峰值帧 {{ peakLabel }}</div>
        </div>

        <div v-if="hasResult" class="panel-section">
          <div class="panel-section-title">模拟结果分析</div>
          <div ref="chartRef" class="result-chart"></div>
        </div>

        <div v-if="hasResult" class="panel-section">
          <div class="panel-section-title">影响范围统计</div>
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-label">{{ summary.rangeLabel }}</span>
              <span class="stat-value">{{ summary.rangeText }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">{{ summary.extraLabel }}</span>
              <span class="stat-value">{{ summary.extraText }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">峰值浓度</span>
              <span class="stat-value">{{ summary.peak }} mg/L</span>
            </div>
          </div>
        </div>

        <div v-if="hasResult && points.length" class="panel-section">
          <div class="panel-section-title">浓度分布</div>
          <div class="concentration-list">
            <div v-for="(p, i) in points" :key="i" class="concentration-item">
              <span class="location">{{ p.label }}</span>
              <span class="value" :style="{ color: p.color }">{{ p.value }} mg/L</span>
            </div>
          </div>
        </div>

        <div class="panel-section">
          <div class="panel-section-title">预警信息</div>
          <el-alert
            v-if="tip.show"
            :title="tip.title"
            :type="tip.type"
            :description="tip.desc"
            show-icon
            :closable="false"
          />
          <div v-else class="no-alert">完成模拟后将显示预警信息</div>
          <el-button v-if="tip.show" class="goto-warning-btn" type="warning" plain @click="goWarning">
            前往预警分析
          </el-button>
        </div>
      </div>

      <div class="pollution-legend">
        <h4>污染物浓度图例</h4>
        <div class="color-bar"></div>
        <div class="legend-labels"><span>低浓度</span><span>高浓度</span></div>
        <div class="legend-scale">
          <div class="scale-item"><div class="scale-color" style="background-color:#00ff00"></div><span>0-25%</span></div>
          <div class="scale-item"><div class="scale-color" style="background-color:#ffff00"></div><span>25-50%</span></div>
          <div class="scale-item"><div class="scale-color" style="background-color:#ff8000"></div><span>50-75%</span></div>
          <div class="scale-item"><div class="scale-color" style="background-color:#ff0000"></div><span>75-100%</span></div>
        </div>
      </div>

      <div v-if="loading" class="loading-overlay">正在加载地图数据...</div>
      <div v-if="errorText" class="layer-error">{{ errorText }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'
import Point from '@geoscene/core/geometry/Point'
import { execute as containsExecute } from '@geoscene/core/geometry/operators/containsOperator'
import '@/assets/simulation-page.css'
import { getGeometryCentroid } from '@/geoscene/utils/getGeometryCentroid'
import { createMapView } from '@/geoscene/initMapView'
import { addBusinessLayersToMap, reorderBusinessLayers } from '@/geoscene/layers/createBusinessLayers'
import { DEFAULT_LAYER_COLORS, loadBusinessLayers } from '@/geoscene/layers/layerStyle'
import { normalizeReservoirName } from '@/geoscene/reservoirNames'
import { resolveNameField } from '@/geoscene/utils/resolveNameField'
import { LakeDiffusionModel, concentrationColor, buildLocalGrid } from '@/geoscene/utils/lakeDiffusion'
import {
  RiverDiffusionModel,
  extractPolylineCoords,
  samplePolyline,
  nearestOnPolyline,
  downstreamSamples
} from '@/geoscene/utils/riverDiffusion'
import { lonLatFromMapPoint, lonLatFromPointLike, isValidLonLat } from '@/geoscene/utils/lonLat'
import * as webMercatorUtils from '@geoscene/core/geometry/support/webMercatorUtils'
import { waterSituationAPI, warningAPI } from '@/api'

/** 选中样式：避开污染图例的红/橙/黄 */
const PICK_CYAN = [0, 210, 230]
const PICK_MAGENTA = '#C511A0'

const INDICATOR = { ammonia: 'ammonia_nitrogen', phosphorus: 'total_phosphorus', cod: 'cod' }
const KEYS = ['districts', 'waterAreas', 'waterLines', 'reservoirs']
const LAYER_UI = [
  { key: 'districts', name: '行政区划', colorKey: 'districts' },
  {
    key: 'rivers',
    name: '河流',
    colorKey: 'waterLines',
    layerKeys: ['waterLines', 'waterAreas']
  },
  { key: 'reservoirs', name: '水库', colorKey: 'reservoirs' }
]

const route = useRoute()
const router = useRouter()

const mapRef = ref(null)
const chartRef = ref(null)
const loading = ref(true)
const errorText = ref('')
const reservoirs = ref([])
const running = ref(false)
const hasResult = ref(false)
const mode = ref('lake')

const lake = reactive({
  reservoir: '',
  source: null,
  pollutant: 'ammonia',
  mass: 1000,
  hours: 24,
  velocity: 0.3,
  diffusivity: 10
})
const river = reactive({
  name: '',
  source: null,
  pollutant: 'ammonia',
  mass: 100,
  hours: 24,
  velocity: 1.5,
  diffusivity: 10,
  width: 20,
  depth: 3
})
const summary = reactive({
  rangeLabel: '扩散半径',
  rangeText: '0 km',
  extraLabel: '影响面积',
  extraText: '0',
  peak: '0'
})
const points = ref([])
const tip = reactive({
  show: false,
  title: '',
  type: 'info',
  desc: '',
  pollutant: 'ammonia',
  peak: 0,
  target: ''
})
const frames = ref([])
const frameIdx = ref(0)
const colorPeak = ref(1)
const playSpeed = ref(1)
const PLAY_BASE_MS = 700
const playing = ref(false)
let playTimer = null

const peakLabel = computed(() => {
  if (!frames.value.length) return '-'
  let best = -1
  let label = frames.value[0].label
  frames.value.forEach((f) => {
    if (f.peak >= best) {
      best = f.peak
      label = f.label
    }
  })
  return label
})

const layerControls = reactive(
  LAYER_UI.map((item) => ({
    ...item,
    visible: true
  }))
)

let view = null
let chart = null
let resultLayer = null
let pickLayer = null
let reservoirLayer = null
let waterLineLayer = null
const layerMap = {}
let lakeGeom = null
let riverGeom = null
let riverCoords = []
let mapAlive = true

onMounted(async () => {
  await nextTick()
  mapAlive = true
  applyQuery()
  await initMap()
  if (mapAlive) await loadReservoirs()
  if (mapAlive && lake.reservoir) await selectLake(lake.reservoir, null)
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  mapAlive = false
  stopPlay()
  window.removeEventListener('resize', onResize)
  view?.destroy()
  view = null
  chart?.dispose()
})

function applyQuery() {
  const q = route.query || {}
  if (q.mode === 'river' || q.mode === 'lake') mode.value = q.mode
  if (q.source) lake.reservoir = String(q.source)
  if (q.pollutant && INDICATOR[q.pollutant]) {
    lake.pollutant = q.pollutant
    river.pollutant = q.pollutant
  }
  if (q.value != null && !Number.isNaN(Number(q.value))) {
    const v = Number(q.value)
    lake.mass = Math.max(50, Math.round(v * 800))
    river.mass = Math.max(10, Math.round(v * 80))
  }
}

function onResize() {
  chart?.resize()
}

function setMode(next) {
  if (mode.value === next) return
  stopPlay()
  clearResult()
  pickLayer?.removeAll()
  mode.value = next
  if (next === 'river' && waterLineLayer) {
    waterLineLayer.visible = true
    if (layerMap.waterAreas) layerMap.waterAreas.visible = true
    const c = layerControls.find((x) => x.key === 'rivers')
    if (c) c.visible = true
  }
}

async function initMap() {
  errorText.value = ''
  try {
    const created = await createMapView(mapRef.value, { zoom: 9 })
    if (!mapAlive) {
      created.view.destroy()
      return
    }
    view = created.view
    Object.assign(layerMap, addBusinessLayersToMap(created.map, { popupEnabled: false, keys: KEYS }))
    layerControls.forEach((item) => toggleLayer(item.key, item.visible))
    const colors = {
      districts: DEFAULT_LAYER_COLORS.districts,
      waterAreas: DEFAULT_LAYER_COLORS.waterLines,
      waterLines: DEFAULT_LAYER_COLORS.waterLines,
      reservoirs: DEFAULT_LAYER_COLORS.reservoirs
    }
    const loads = await loadBusinessLayers(layerMap, colors, created.map)
    if (!mapAlive) return

    reorderBusinessLayers(created.map, layerMap)
    reservoirLayer = layerMap.reservoirs
    waterLineLayer = layerMap.waterLines

    const failed = loads.filter((r) => !r.ok && !r.cancelled)
    if (failed.length === loads.length) {
      errorText.value = '业务图层未加载，请确认 GeoScene 服务已启动（6443）'
      ElMessage.warning(errorText.value)
    } else if (failed.length) {
      errorText.value = `部分图层加载失败：${failed.map((r) => r.key).join('、')}`
    }

    pickLayer = new GraphicsLayer({ title: '选中高亮', listMode: 'hide' })
    resultLayer = new GraphicsLayer({ title: '扩散结果', listMode: 'hide' })
    created.map.add(pickLayer)
    created.map.add(resultLayer)
    view.on('click', onMapClick)
    await fitMap()
  } catch (e) {
    if (!mapAlive || String(e?.message || '').includes('destroyed')) return
    console.error(e)
    errorText.value = '地图加载失败，请查看控制台错误信息'
    ElMessage.error('地图加载失败')
  } finally {
    if (mapAlive) loading.value = false
  }
}

async function onMapClick(event) {
  if (!view) return
  const hit = await view.hitTest(event)
  const mp = event.mapPoint

  if (mode.value === 'lake') {
    const g = hit.results.find((r) => r.graphic?.layer === reservoirLayer)?.graphic
    if (!g?.attributes) return
    const { value } = resolveNameField(g.attributes)
    const name = normalizeReservoirName(value)
    if (!name || !reservoirs.value.includes(name)) return
    await selectLake(name, mp)
    return
  }

  const line = hit.results.find((r) => r.graphic?.layer === waterLineLayer)?.graphic
  if (!line?.geometry) return
  const attrs = line.attributes || {}
  const { value } = resolveNameField(attrs)
  river.name = value || attrs.NAME || attrs.name || '未命名河段'
  riverGeom = line.geometry
  riverCoords = extractPolylineCoords(line.geometry)

  const clickLl = lonLatFromMapPoint(mp)
  if (clickLl && riverCoords.length) {
    const near = nearestOnPolyline(riverCoords, clickLl.lng, clickLl.lat)
    if (near && near.distToClick < 800) {
      river.source = { lng: near.lng, lat: near.lat, distance: near.distance }
      showRiverPick(riverGeom, river.source)
      softGoToLonLat(near.lng, near.lat)
      ElMessage.success(`已选择河流「${river.name}」并设置污染源`)
      return
    }
  }
  if (riverCoords.length) {
    river.source = { lng: riverCoords[0][0], lat: riverCoords[0][1], distance: 0 }
  }
  showRiverPick(riverGeom, river.source)
  if (river.source) softGoToLonLat(river.source.lng, river.source.lat)
  ElMessage.success(`已选择河流：${river.name}（污染源默认起点，可再点线上位置）`)
}

async function onLakeReservoirChange(name) {
  if (!name) {
    lake.source = null
    lakeGeom = null
    pickLayer?.removeAll()
    return
  }
  await selectLake(name, null)
}

async function selectLake(name, mapPoint) {
  lake.reservoir = name
  const feature = await findFeature(name)
  if (!feature?.geometry) {
    ElMessage.warning('未找到对应水库面')
    return
  }
  lakeGeom = feature.geometry

  // 地图点选：hitTest 已命中水库面，直接用点击坐标（不再依赖 contains，避免 SR 误判成库心）
  let ll = mapPoint ? lonLatFromMapPoint(mapPoint) : null
  const fromClick = !!(ll && isValidLonLat(ll.lng, ll.lat))
  if (!fromClick) {
    ll = lonLatFromPointLike(getGeometryCentroid(feature.geometry), feature.geometry.spatialReference)
  }
  if (!ll || !isValidLonLat(ll.lng, ll.lat)) {
    ElMessage.error('无法确定污染源位置')
    return
  }
  lake.source = { lng: ll.lng, lat: ll.lat }
  showLakePick(feature.geometry, lake.source)
  await softGoToLonLat(ll.lng, ll.lat)
  ElMessage.success(fromClick ? `已选择「${name}」，污染源为点击位置` : `已选择「${name}」，污染源默认库心（可再点水库面改位置）`)
}

function sourcePoint(source) {
  if (!source || !isValidLonLat(source.lng, source.lat)) return null
  return pointInViewSr(source.lng, source.lat)
}

function showLakePick(geometry, source) {
  pickLayer?.removeAll()
  if (!geometry || !pickLayer) return
  pickLayer.add(
    new Graphic({
      geometry,
      symbol: {
        type: 'simple-fill',
        color: [...PICK_CYAN, 0.14],
        outline: { color: [...PICK_CYAN, 1], width: 3 }
      }
    })
  )
  const pt = sourcePoint(source)
  if (pt) {
    pickLayer.add(
      new Graphic({
        geometry: pt,
        symbol: {
          type: 'simple-marker',
          style: 'diamond',
          color: PICK_MAGENTA,
          size: 16,
          outline: { color: '#fff', width: 2 }
        }
      })
    )
  }
}

function showRiverPick(geometry, source) {
  pickLayer?.removeAll()
  if (!pickLayer) return
  if (geometry) {
    pickLayer.add(
      new Graphic({
        geometry,
        symbol: { type: 'simple-line', color: [...PICK_CYAN, 1], width: 5 }
      })
    )
  }
  const pt = sourcePoint(source)
  if (pt) {
    pickLayer.add(
      new Graphic({
        geometry: pt,
        symbol: {
          type: 'simple-marker',
          style: 'diamond',
          color: PICK_MAGENTA,
          size: 16,
          outline: { color: '#fff', width: 2 }
        }
      })
    )
  }
}

/** 居中到污染源，不强制改 zoom，避免相机飞出导致底图“消失” */
async function softGoToLonLat(lng, lat) {
  if (!view || !isValidLonLat(lng, lat)) return
  try {
    await view.goTo({ target: new Point({ longitude: lng, latitude: lat, spatialReference: { wkid: 4326 } }) })
  } catch (_) {}
}

function highlight(geometry, type) {
  // 兼容旧调用：按类型走新的点亮逻辑
  if (type === 'polygon') {
    showLakePick(geometry, lake.source)
  } else {
    showRiverPick(geometry, river.source)
  }
}

function drawSource(lng, lat) {
  if (mode.value === 'river') {
    river.source = { ...(river.source || {}), lng, lat, distance: river.source?.distance ?? 0 }
    showRiverPick(riverGeom, river.source)
  } else {
    lake.source = { lng, lat }
    showLakePick(lakeGeom, lake.source)
  }
}

async function fitMap() {
  if (!view) return
  const list = KEYS.map((key) => layerMap[key]).filter((l) => l?.fullExtent)
  if (!list.length) return
  try {
    let extent = list[0].fullExtent.clone()
    for (let i = 1; i < list.length; i++) extent = extent.union(list[i].fullExtent)
    await view.goTo(extent.expand(1.08))
  } catch (_) {}
}

function toggleLayer(key, visible) {
  const item = layerControls.find((x) => x.key === key)
  const keys = item?.layerKeys || [key]
  keys.forEach((k) => {
    if (layerMap[k]) layerMap[k].visible = visible
  })
}

async function loadReservoirs() {
  const res = await waterSituationAPI.getReservoirs()
  reservoirs.value = res.data || []
}

function thresholdOf(p) {
  if (p === 'phosphorus') return 0.2
  if (p === 'cod') return 20
  return 1
}

function pointInPolygonSr(lng, lat, spatialReference) {
  const wkid = spatialReference?.wkid || spatialReference?.latestWkid
  if (wkid === 3857 || wkid === 102100 || wkid === 102113) {
    const [x, y] = webMercatorUtils.lngLatToXY(lng, lat)
    return new Point({ x, y, spatialReference })
  }
  return new Point({
    longitude: lng,
    latitude: lat,
    spatialReference: spatialReference || { wkid: 4326 }
  })
}

function inPolygon(polygon, lng, lat) {
  if (!polygon || !isValidLonLat(lng, lat)) return false
  try {
    return containsExecute(polygon, pointInPolygonSr(lng, lat, polygon.spatialReference))
  } catch (_) {
    return true
  }
}

async function findFeature(name) {
  if (!reservoirLayer) return null
  const result = await reservoirLayer.queryFeatures({
    where: '1=1',
    outFields: ['*'],
    returnGeometry: true
  })
  for (const f of result.features) {
    const { value } = resolveNameField(f.attributes || {})
    if (normalizeReservoirName(value) === name) return f
  }
  return null
}

function clearResult() {
  stopPlay()
  resultLayer?.removeAll()
  hasResult.value = false
  points.value = []
  frames.value = []
  frameIdx.value = 0
  colorPeak.value = 1
  tip.show = false
  summary.rangeText = '0 km'
  summary.extraText = '0'
  summary.peak = '0'
  chart?.dispose()
  chart = null
}

function initChart() {
  if (!chartRef.value) return
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['污染浓度', '安全阈值'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '18%', containLabel: true },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', name: '浓度(mg/L)' },
    series: [
      { name: '污染浓度', type: 'line', data: [], smooth: true, itemStyle: { color: '#1976d2' } },
      {
        name: '安全阈值',
        type: 'line',
        data: [],
        lineStyle: { type: 'dashed', color: '#f44336' },
        itemStyle: { color: '#f44336' }
      }
    ]
  })
}

function toHex(rgba) {
  const [r, g, b] = rgba
  return `#${[r, g, b].map((v) => Number(v).toString(16).padStart(2, '0')).join('')}`
}

async function startSim() {
  if (mode.value === 'lake') await runLake()
  else await runRiver()
}

async function runLake() {
  if (!lake.reservoir) {
    ElMessage.warning('请先选择污染源水库')
    return
  }
  running.value = true
  stopPlay()
  resultLayer?.removeAll()
  try {
    const feature = await findFeature(lake.reservoir)
    if (!feature?.geometry) {
      ElMessage.error('未找到对应水库面要素')
      return
    }
    lakeGeom = feature.geometry
    if (!lake.source || !isValidLonLat(lake.source.lng, lake.source.lat)) {
      const c = lonLatFromPointLike(getGeometryCentroid(feature.geometry), feature.geometry.spatialReference)
      if (!c || !isValidLonLat(c.lng, c.lat)) {
        ElMessage.error('无法计算污染源位置')
        return
      }
      lake.source = { lng: c.lng, lat: c.lat }
    }
    showLakePick(feature.geometry, lake.source)
    const origin = lake.source

    const model = new LakeDiffusionModel({
      Ex: lake.diffusivity,
      Ey: lake.diffusivity,
      vx: lake.velocity,
      vy: lake.velocity * 0.3,
      m: lake.mass
    })
    const hours = lake.hours
    const thr = thresholdOf(lake.pollutant)
    const labels = []
    const series = []
    const thrSeries = []
    const built = []
    const half = Math.min(6000, Math.sqrt(4 * model.Ex * hours * 3600) + model.vx * hours * 3600)
    const candidates = buildLocalGrid(
      { longitude: origin.lng, latitude: origin.lat },
      half,
      Math.max(350, half / 8)
    )
    const inside = candidates.filter((p) => inPolygon(feature.geometry, p.lng, p.lat))
    // contains 失败时不要丢掉网格，否则动画无点可画
    const grid = inside.length ? inside : candidates
    const step = Math.max(1, Math.floor(hours / 12))

    for (let h = 0; h <= hours; h += step) {
      const t = Math.max(1, h * 3600)
      const cells = grid.map((p) => ({ ...p, c: model.calculate(t, p.xM, p.yM) }))
      const peak = cells.reduce((m, c) => Math.max(m, c.c), 0)
      labels.push(`${h}h`)
      series.push(Number(peak.toFixed(3)))
      thrSeries.push(thr)
      built.push({ label: `${h}h`, peak, cells })
    }

    const finalPeak = series.length ? Math.max(...series) : 0
    const radiusM = Math.min(8000, Math.sqrt(4 * model.Ex * hours * 3600) + model.vx * hours * 3600)
    summary.rangeLabel = '扩散半径'
    summary.rangeText = `${(radiusM / 1000).toFixed(2)} km`
    summary.extraLabel = '影响面积'
    summary.extraText = `${(Math.PI * (radiusM / 1000) ** 2).toFixed(2)} km²`
    summary.peak = Number(finalPeak).toFixed(2)

    points.value = (built[built.length - 1]?.cells || [])
      .filter((c) => c.c > 0)
      .sort((a, b) => b.c - a.c)
      .slice(0, 5)
      .map((c, i) => ({
        label: i === 0 ? '污染源附近' : `采样点 ${i}`,
        value: Number(c.c.toFixed(3)),
        color: toHex(concentrationColor(c.c, finalPeak || 1))
      }))

    frames.value = built
    colorPeak.value = Math.max(finalPeak, 1e-6)
    frameIdx.value = 0
    hasResult.value = true
    renderFrame()
    await nextTick()
    initChart()
    chart?.setOption({ xAxis: { data: labels }, series: [{ data: series }, { data: thrSeries }] })
    await pushTip({ target: lake.reservoir, pollutant: lake.pollutant, peak: finalPeak, hours, thr })
    await softGoToLonLat(origin.lng, origin.lat)
    ElMessage.success('湖泊模拟完成')
    startPlay()
  } catch (e) {
    console.error(e)
    ElMessage.error('模拟失败')
  } finally {
    running.value = false
  }
}

async function runRiver() {
  if (!riverCoords.length) {
    ElMessage.warning('请先在地图上点击水系线选择河流')
    return
  }
  if (!river.source || !isValidLonLat(river.source.lng, river.source.lat)) {
    river.source = { lng: riverCoords[0][0], lat: riverCoords[0][1], distance: 0 }
  }
  showRiverPick(riverGeom, river.source)
  running.value = true
  stopPlay()
  resultLayer?.removeAll()
  try {
    const model = new RiverDiffusionModel({
      Ex: river.diffusivity,
      vx: river.velocity,
      H: river.depth,
      W: river.width,
      m: river.mass,
      K: 1e-5
    })
    const sampled = samplePolyline(riverCoords, 250)
    const down = downstreamSamples(sampled, river.source.distance)
    if (down.length < 2) {
      ElMessage.warning('河段采样点过少，请换一条更长的水系线')
      return
    }

    const hours = river.hours
    const thr = thresholdOf(river.pollutant)
    const labels = []
    const series = []
    const thrSeries = []
    const built = []
    const step = Math.max(1, Math.floor(hours / 12))
    for (let h = 0; h <= hours; h += step) {
      const t = Math.max(1, h * 3600)
      const cells = down.map((p) => ({
        lng: p.lng,
        lat: p.lat,
        x: p.x,
        c: model.calculate(t, p.x)
      }))
      const peak = cells.reduce((m, c) => Math.max(m, c.c), 0)
      labels.push(`${h}h`)
      series.push(Number(peak.toFixed(4)))
      thrSeries.push(thr)
      built.push({ label: `${h}h`, peak, cells })
    }

    const finalPeak = series.length ? Math.max(...series) : 0
    const affected = (built[built.length - 1]?.cells || []).filter((c) => c.c > 1e-4)
    const maxX = affected.reduce((m, c) => Math.max(m, c.x), 0)
    summary.rangeLabel = '影响河长'
    summary.rangeText = `${(maxX / 1000).toFixed(2)} km`
    summary.extraLabel = '采样点数'
    summary.extraText = String(down.length)
    summary.peak = Number(finalPeak).toFixed(4)

    points.value = affected
      .sort((a, b) => b.c - a.c)
      .slice(0, 5)
      .map((c, i) => ({
        label: i === 0 ? '污染源下游' : `下游 ${(c.x / 1000).toFixed(2)} km`,
        value: Number(c.c.toFixed(4)),
        color: toHex(concentrationColor(c.c, finalPeak || 1))
      }))

    frames.value = built
    colorPeak.value = Math.max(finalPeak, 1e-6)
    frameIdx.value = 0
    hasResult.value = true
    renderFrame()
    await nextTick()
    initChart()
    chart?.setOption({ xAxis: { data: labels }, series: [{ data: series }, { data: thrSeries }] })
    await pushTip({
      target: river.name || '河流段',
      pollutant: river.pollutant,
      peak: finalPeak,
      hours,
      thr
    })
    await softGoToLonLat(river.source.lng, river.source.lat)
    ElMessage.success('河流模拟完成')
    startPlay()
  } catch (e) {
    console.error(e)
    ElMessage.error('模拟失败')
  } finally {
    running.value = false
  }
}

function renderFrame() {
  const frame = frames.value[frameIdx.value]
  if (!frame || !resultLayer) return
  resultLayer.removeAll()
  const globalP = Math.max(colorPeak.value, 1e-6)
  // 用本帧峰值保证点可见；下限挂到全局峰值的 15%，后期仍能看出变淡
  const peak = Math.max(frame.peak, globalP * 0.15, 1e-6)
  const minRatio = mode.value === 'lake' ? 0.008 : 0.005
  const graphics = []
  frame.cells.forEach((cell) => {
    if (cell.c <= 0) return
    if (!isValidLonLat(cell.lng, cell.lat)) return
    const ratio = cell.c / peak
    if (ratio < minRatio) return
    graphics.push(
      new Graphic({
        geometry: pointInViewSr(cell.lng, cell.lat),
        symbol: {
          type: 'simple-marker',
          color: concentrationColor(cell.c, peak),
          size: 8 + ratio * 12,
          outline: { color: [30, 30, 30, 0.4], width: 0.5 }
        }
      })
    )
  })
  // 兜底：过滤过严时至少画出本帧最浓的点，避免“没动画”
  if (!graphics.length) {
    const top = [...frame.cells]
      .filter((c) => c.c > 0 && isValidLonLat(c.lng, c.lat))
      .sort((a, b) => b.c - a.c)
      .slice(0, 40)
    const localPeak = Math.max(top[0]?.c || 0, 1e-6)
    top.forEach((cell) => {
      const ratio = cell.c / localPeak
      graphics.push(
        new Graphic({
          geometry: pointInViewSr(cell.lng, cell.lat),
          symbol: {
            type: 'simple-marker',
            color: concentrationColor(cell.c, localPeak),
            size: 8 + ratio * 12,
            outline: { color: [30, 30, 30, 0.4], width: 0.5 }
          }
        })
      )
    })
  }
  graphics.forEach((g) => resultLayer.add(g))
}

function pointInViewSr(lng, lat) {
  const sr = view?.spatialReference
  const wkid = sr?.wkid || sr?.latestWkid
  if (wkid === 3857 || wkid === 102100 || wkid === 102113) {
    const [x, y] = webMercatorUtils.lngLatToXY(lng, lat)
    return new Point({ x, y, spatialReference: sr })
  }
  return new Point({ longitude: lng, latitude: lat, spatialReference: { wkid: 4326 } })
}

function startPlay() {
  stopPlay()
  if (frames.value.length < 2) return
  playing.value = true
  const ms = Math.max(80, Math.round(PLAY_BASE_MS / (playSpeed.value || 1)))
  playTimer = setInterval(() => {
    frameIdx.value = (frameIdx.value + 1) % frames.value.length
    renderFrame()
  }, ms)
}

function stopPlay() {
  playing.value = false
  if (playTimer) {
    clearInterval(playTimer)
    playTimer = null
  }
}

function togglePlay() {
  if (playing.value) stopPlay()
  else startPlay()
}

function onPlaySpeedChange() {
  if (playing.value) startPlay()
}

async function pushTip({ target, pollutant, peak, hours, thr }) {
  tip.pollutant = pollutant
  tip.peak = peak
  tip.target = target
  if (peak > thr * 2) {
    tip.show = true
    tip.title = '红色预警'
    tip.type = 'error'
    tip.desc = `${target} 泄漏模拟峰值浓度 ${peak.toFixed(3)} mg/L，建议立即启动应急响应。`
  } else if (peak > thr) {
    tip.show = true
    tip.title = '橙色预警'
    tip.type = 'warning'
    tip.desc = `预计 ${hours} 小时内污染物浓度将超过安全阈值，请加强监测。`
  } else {
    tip.show = true
    tip.title = '蓝色提示'
    tip.type = 'info'
    tip.desc = '模拟浓度处于可控范围，建议持续跟踪。'
  }
  if (peak > thr) {
    try {
      await warningAPI.evaluateEnv({
        reservoirName: target,
        indicator: INDICATOR[pollutant] || 'ammonia_nitrogen',
        value: Number(peak.toFixed(4))
      })
    } catch (e) {
      console.warn('写入预警记录失败', e)
    }
  }
}

function goWarning() {
  router.push({
    path: '/warning',
    query: {
      mode: 'environment',
      reservoir: tip.target || lake.reservoir,
      indicator: INDICATOR[tip.pollutant] || 'ammonia_nitrogen',
      value: String(tip.peak || '')
    }
  })
}

function resetSim() {
  stopPlay()
  clearResult()
  pickLayer?.removeAll()
  lakeGeom = null
  riverGeom = null
  riverCoords = []
  lake.reservoir = ''
  lake.source = null
  lake.pollutant = 'ammonia'
  lake.mass = 1000
  lake.hours = 24
  lake.velocity = 0.3
  lake.diffusivity = 10
  river.name = ''
  river.source = null
  river.pollutant = 'ammonia'
  river.mass = 100
  river.hours = 24
  river.velocity = 1.5
  river.diffusivity = 10
  river.width = 20
  river.depth = 3
}
</script>
