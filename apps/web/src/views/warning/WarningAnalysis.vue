<template>
  <div class="warning-page">
    <div class="warning-page-content">
      <div ref="mapRef" class="warning-map"></div>

      <div class="map-tip-bar">
        {{
          warningMode === 'waterLevel'
            ? '点击水库面，输入当前水位进行预警评估'
            : '点击监测点，输入指标数值进行环境预警评估'
        }}
      </div>

      <div class="mode-panel">
        <div class="mode-label">预警模式</div>
        <div class="mode-buttons">
          <button
            type="button"
            class="mode-btn"
            :class="{ active: warningMode === 'waterLevel' }"
            @click="setWarningMode('waterLevel')"
          >
            水位预警
          </button>
          <button
            type="button"
            class="mode-btn"
            :class="{ active: warningMode === 'environment' }"
            @click="setWarningMode('environment')"
          >
            环境预警
          </button>
        </div>
      </div>

      <div class="control-panel">
        <h3>图层控制</h3>
        <div v-for="item in layerControls" :key="item.key" class="layer-control">
          <label>
            <input
              type="checkbox"
              v-model="item.visible"
              @change="toggleLayer(item.key, item.visible)"
            />
            {{ item.name }}
          </label>
          <div
            class="color-preview"
            :style="{ backgroundColor: DEFAULT_LAYER_COLORS[item.key] }"
          ></div>
        </div>
        <div v-if="warningMode === 'environment'" class="layer-control">
          <label>
            <input type="checkbox" v-model="monitoringVisible" @change="toggleMonitoringLayer" />
            监测断面
          </label>
          <div class="color-preview point-preview" style="background-color: #2196f3; border-radius: 50%"></div>
        </div>
      </div>

      <div class="right-statusbar" ref="rightPanelRef">
        <div class="status-section">
          <div class="status-title status-title-row">
            <span>{{ warningMode === 'waterLevel' ? '水位评估' : '环境评估' }}</span>
            <button
              v-if="selectedReservoir"
              type="button"
              class="collapse-btn"
              @click="summaryExpanded = !summaryExpanded"
            >
              {{ summaryExpanded ? '收起摘要' : '展开摘要' }}
            </button>
          </div>
          <div class="status-content">
            <div class="form-row">
              <label>水库</label>
              <el-input
                v-model="selectedReservoir"
                readonly
                placeholder="请在地图上选择"
                class="form-value"
              />
            </div>

            <div v-if="selectedReservoir && summaryExpanded" class="reservoir-summary">
              <div class="summary-title">水库关键水位</div>
              <div class="summary-grid">
                <div class="summary-item">
                  <span class="summary-label">汛限水位</span>
                  <span class="summary-value">{{ formatMetaLevel(selectedMeta?.floodLevel) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">历史最高</span>
                  <span class="summary-value">{{ formatMetaLevel(selectedMeta?.maxLevel) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">多年平均</span>
                  <span class="summary-value">{{ formatMetaLevel(selectedMeta?.avgLevel) }}</span>
                </div>
              </div>
              <div class="summary-hint">对照上方阈值输入测试水位，再点「评估预警」</div>
            </div>

            <template v-if="warningMode === 'waterLevel'">
              <div class="form-row">
                <label>当前水位 (m)</label>
                <el-input-number
                  v-model="inputLevel"
                  :precision="2"
                  class="form-value"
                  style="width: 100%"
                />
              </div>
            </template>
            <template v-else>
              <div class="form-row">
                <label>指标</label>
                <el-select v-model="envIndicator" class="form-value" style="width: 100%">
                  <el-option label="氨氮 (mg/L)" value="ammonia_nitrogen" />
                  <el-option label="COD (mg/L)" value="cod" />
                  <el-option label="总磷 (mg/L)" value="total_phosphorus" />
                </el-select>
              </div>
              <div class="form-row">
                <label>监测值</label>
                <el-input-number
                  v-model="envValue"
                  :precision="3"
                  :min="0"
                  class="form-value"
                  style="width: 100%"
                />
              </div>
            </template>
            <el-button
              type="primary"
              class="evaluate-btn"
              :disabled="!selectedReservoir"
              @click="evaluate"
            >
              评估预警
            </el-button>
            <div
              v-if="analysisResult"
              class="result-snippet"
              :class="resultSnippetClass"
            >
              <strong>{{ analysisResult.status }}</strong>
              <div>{{ analysisResult.summary }}</div>
            </div>
            <button
              v-if="analysisResult"
              type="button"
              class="detail-link"
              @click="openAnalysisModal"
            >
              查看详细分析 →
            </button>
            <el-button
              v-if="analysisResult && warningMode === 'environment'"
              class="goto-sim-btn"
              type="primary"
              plain
              @click="goSimulation"
            >
              前往污染模拟
            </el-button>
          </div>
        </div>

        <div class="status-section">
          <div class="status-title">最近预警</div>
          <div class="status-content warning-table-wrap">
            <el-table :data="warningList.slice(0, 8)" size="small">
              <el-table-column prop="reservoirName" label="水库" width="80" />
              <el-table-column prop="warningLevel" label="级别" width="60" />
              <el-table-column prop="warningType" label="类型" width="70" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
      </div>

      <div class="legend-container">
        <div class="legend-items">
          <template v-if="warningMode === 'waterLevel'">
            <div class="legend-item">
              <div class="legend-color danger-color"></div>
              <span class="legend-text">危险水位</span>
            </div>
            <div class="legend-item">
              <div class="legend-color warning-color"></div>
              <span class="legend-text">警戒水位</span>
            </div>
            <div class="legend-item">
              <div class="legend-color normal-color"></div>
              <span class="legend-text">正常水位</span>
            </div>
          </template>
          <template v-else>
            <div class="legend-item">
              <div class="legend-color env-red"></div>
              <span class="legend-text">红色预警</span>
            </div>
            <div class="legend-item">
              <div class="legend-color env-orange"></div>
              <span class="legend-text">橙色预警</span>
            </div>
            <div class="legend-item">
              <div class="legend-color env-yellow"></div>
              <span class="legend-text">黄色预警</span>
            </div>
            <div class="legend-item">
              <div class="legend-color normal-color"></div>
              <span class="legend-text">正常</span>
            </div>
          </template>
        </div>
      </div>

      <div v-if="loading" class="loading-overlay">正在加载地图数据...</div>
      <div v-if="layerError" class="layer-error">{{ layerError }}</div>
    </div>

    <!-- 分析结果弹窗 -->
    <div v-if="showAnalysisModal" class="analysis-modal">
      <div
        ref="analysisModalRef"
        class="analysis-modal-content"
        @mousedown="startDrag($event, analysisModalRef, dragState)"
      >
        <div class="analysis-modal-header">
          <div class="drag-handle">
            <div class="drag-dots">
              <span v-for="n in 6" :key="n"></span>
            </div>
          </div>
          <h3>
            {{ selectedReservoir }} -
            {{ warningMode === 'waterLevel' ? '水位预警分析结果' : '环境预警分析结果' }}
          </h3>
          <button type="button" class="close-btn" @click="closeAnalysisModal">&times;</button>
        </div>

        <div v-if="analysisResult" class="analysis-modal-body">
          <div class="status-display-section">
            <div class="status-indicator" :class="statusIndicatorClass">
              {{ analysisResult.status }}
            </div>
          </div>

          <div v-if="warningMode === 'waterLevel'" class="water-level-comparison">
            <h5>水位对比分析</h5>
            <div class="comparison-table">
              <div class="comparison-row header">
                <div class="label">项目</div>
                <div class="value">水位(米)</div>
                <div class="difference">与输入水位差值</div>
              </div>
              <div
                v-for="row in waterComparisonRows"
                :key="row.label"
                class="comparison-row"
              >
                <div class="label">{{ row.label }}</div>
                <div class="value" :class="row.valueClass">{{ row.value }}</div>
                <div class="difference" :class="row.diffClass">{{ row.diff }}</div>
              </div>
            </div>
          </div>

          <div v-else class="water-level-comparison">
            <h5>指标对比分析</h5>
            <div class="comparison-table">
              <div class="comparison-row header">
                <div class="label">阈值等级</div>
                <div class="value">阈值</div>
                <div class="difference">与输入差值</div>
              </div>
              <div
                v-for="row in envComparisonRows"
                :key="row.label"
                class="comparison-row"
              >
                <div class="label">{{ row.label }}</div>
                <div class="value">{{ row.value }}</div>
                <div class="difference" :class="row.diffClass">{{ row.diff }}</div>
              </div>
            </div>
          </div>

          <div class="analysis-summary">
            <h5>分析结论</h5>
            <div class="summary-content">{{ analysisResult.summary }}</div>
            <div
              v-if="warningMode === 'waterLevel' && showPlanButton"
              class="emergency-plan-section"
            >
              <button
                type="button"
                class="emergency-plan-btn"
                :class="analysisResult.status === '危险' ? 'danger-plan' : 'warning-plan'"
                @click="openPlanModal"
              >
                <span>⚠</span>
                <span>查看应急预案</span>
                <span>→</span>
              </button>
            </div>
          </div>
        </div>

        <div class="analysis-modal-footer">
          <button type="button" class="btn-close" @click="closeAnalysisModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 预案弹窗 -->
    <div v-if="showPlanModal" class="analysis-modal plan-modal">
      <div
        ref="planModalRef"
        class="analysis-modal-content"
        @mousedown="startDrag($event, planModalRef, planDragState)"
      >
        <div class="analysis-modal-header">
          <div class="drag-handle">
            <div class="drag-dots">
              <span v-for="n in 6" :key="`p-${n}`"></span>
            </div>
          </div>
          <h3>{{ planModalTitle }}</h3>
          <button type="button" class="close-btn" @click="closePlanModal">&times;</button>
        </div>

        <div class="analysis-modal-body">
          <div class="plan-info-section">
            <div
              class="plan-status-indicator"
              :class="analysisResult?.status === '危险' ? 'status-danger' : 'status-warning'"
            >
              {{ analysisResult?.status === '危险' ? '危险水位预案' : '警戒水位预案' }}
            </div>
          </div>
          <div class="current-level-info">
            <h5>当前状况</h5>
            <div class="level-info-content">
              <p><strong>水库名称：</strong>{{ selectedReservoir }}</p>
              <p v-if="analysisResult?.inputValue != null">
                <strong>输入水位：</strong>{{ analysisResult.inputValue }} 米
              </p>
              <p>
                <strong>预警等级：</strong>
                {{ analysisResult?.status === '危险' ? '一级（危险）' : '二级（警戒）' }}
              </p>
            </div>
          </div>
          <div class="emergency-measures">
            <h5>应急措施</h5>
            <div class="measures-list">
              <div
                v-for="(link, index) in planLinks"
                :key="index"
                class="measure-item"
                :class="analysisResult?.status === '危险' ? 'danger-measure' : 'warning-measure'"
                @click="openPlan(link.url)"
              >
                <div class="measure-number">{{ index + 1 }}</div>
                <div class="measure-content">{{ link.label }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="analysis-modal-footer">
          <button type="button" class="btn-close" @click="closePlanModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import '@/assets/warning-page.css'
import geosceneConfig from '@/config/geoscene'
import { createMapView } from '@/geoscene/initMapView'
import { addBusinessLayersToMap, reorderBusinessLayers } from '@/geoscene/layers/createBusinessLayers'
import { createMonitoringLayer } from '@/geoscene/layers/createMonitoringLayer'
import { DEFAULT_LAYER_COLORS, loadBusinessLayers } from '@/geoscene/layers/layerStyle'
import { applyWarningRenderer } from '@/geoscene/renderers/warningRenderer'
import { applyMonitoringWarningColors } from '@/geoscene/renderers/monitoringWarningRenderer'
import { normalizeReservoirName } from '@/geoscene/reservoirNames'
import { mapStatusToLayerValues, resolveNameField } from '@/geoscene/utils/resolveNameField'
import { createSelectionHighlight } from '@/geoscene/tools/warning/selectionHighlight'
import { warningAPI, sectionMonitorAPI } from '@/api'

const WARNING_LAYER_KEYS = ['districts', 'waterLines', 'reservoirs']
const WARNING_LAYERS = [
  { key: 'districts', name: '行政区划' },
  { key: 'waterLines', name: '河流' },
  { key: 'reservoirs', name: '水库' }
]

const INDICATOR_TO_POLLUTANT = {
  ammonia_nitrogen: 'ammonia',
  total_phosphorus: 'phosphorus',
  cod: 'cod'
}

const route = useRoute()
const router = useRouter()

const mapRef = ref(null)
const analysisModalRef = ref(null)
const planModalRef = ref(null)
const rightPanelRef = ref(null)
const loading = ref(true)
const layerError = ref('')
const warningMode = ref('waterLevel')
const selectedReservoir = ref('')
const inputLevel = ref(null)
const envIndicator = ref('ammonia_nitrogen')
const envValue = ref(null)
const analysisResult = ref(null)
const warningList = ref([])
const statusMap = ref({})
const envStatusMap = ref({})
const showAnalysisModal = ref(false)
const showPlanModal = ref(false)
const monitoringVisible = ref(true)
const summaryExpanded = ref(true)
const reservoirMetaList = ref([])

let view = null
const layerInstances = {}
let reservoirLayer = null
let monitoringLayer = null
let selectionHighlight = null
let mapAlive = true

const dragState = { dragging: false, startX: 0, startY: 0, origLeft: 0, origTop: 0, onMove: null, onUp: null }
const planDragState = { dragging: false, startX: 0, startY: 0, origLeft: 0, origTop: 0, onMove: null, onUp: null }

const layerControls = reactive(
  WARNING_LAYERS.map(({ key, name }) => ({
    key,
    name,
    visible: geosceneConfig.layerMeta[key]?.defaultVisible !== false
  }))
)

const getStatusClass = (status) => {
  if (status === '危险' || status === '红色') return 'danger'
  if (status === '警戒' || status === '橙色' || status === '黄色') return 'warning'
  return 'normal'
}

const resultSnippetClass = computed(() =>
  analysisResult.value ? getStatusClass(analysisResult.value.status) : 'normal'
)

const selectedMeta = computed(() => {
  const name = selectedReservoir.value
  if (!name) return null
  return (
    reservoirMetaList.value.find((r) => normalizeReservoirName(r.reservoirName) === name) || null
  )
})

const formatMetaLevel = (val) => {
  if (val == null || val === '') return '-'
  const n = Number(val)
  return Number.isNaN(n) ? '-' : `${n.toFixed(2)} m`
}

const statusIndicatorClass = computed(() => {
  const s = analysisResult.value?.status
  if (s === '危险' || s === '红色') return 'status-danger'
  if (s === '警戒' || s === '橙色' || s === '黄色') return 'status-warning'
  return 'status-normal'
})

const formatDiff = (input, target) => {
  const diff = input - target
  const sign = diff >= 0 ? '+' : '-'
  return {
    text: `${sign}${Math.abs(diff).toFixed(2)}米`,
    class: diff >= 0 ? 'above' : 'below'
  }
}

const waterComparisonRows = computed(() => {
  const d = analysisResult.value
  if (!d || d.inputValue == null) return []
  const input = Number(d.inputValue)
  const rows = []
  const pushRow = (label, value, valueClass) => {
    const v = Number(value)
    if (Number.isNaN(v)) return
    const { text, class: diffClass } = formatDiff(input, v)
    rows.push({ label, value: v.toFixed(2), valueClass, diff: text, diffClass })
  }
  pushRow('汛限水位', d.floodLimit, 'limit')
  pushRow('历史最高水位', d.maxLevel, 'max')
  if (d.avgLevel != null) pushRow('多年平均水位', d.avgLevel, 'avg')
  return rows
})

const envComparisonRows = computed(() => {
  const d = analysisResult.value
  if (!d || d.inputValue == null) return []
  const input = Number(d.inputValue)
  const row = (label, value) => {
    const v = Number(value)
    if (Number.isNaN(v)) return null
    const diff = input - v
    return {
      label,
      value: v.toFixed(3),
      diff: `${diff >= 0 ? '+' : ''}${diff.toFixed(3)}`,
      diffClass: diff >= 0 ? 'above' : 'below'
    }
  }
  return [
    row('黄色阈值', d.yellowThreshold),
    row('橙色阈值', d.orangeThreshold),
    row('红色阈值', d.redThreshold)
  ].filter(Boolean)
})

const planLinks = computed(() => {
  if (warningMode.value !== 'waterLevel' || selectedReservoir.value !== '密云水库' || !analysisResult.value) {
    return []
  }
  const status = analysisResult.value.status
  if (status === '警戒') {
    return [
      { label: '加密大坝巡查，向下游滚动报送水情', url: '/plans/miyun-warning-1.html' },
      { label: '启动二级应急响应，视雨情控制泄洪', url: '/plans/miyun-warning-2.html' },
      { label: '加强监测并准备应急抢险物资', url: '/plans/miyun-warning-3.html' }
    ]
  }
  if (status === '危险') {
    return [
      { label: '开启泄洪设施，组织下游人员转移', url: '/plans/miyun-danger-1.html' },
      { label: '启动一级应急响应，24小时不间断监测', url: '/plans/miyun-danger-2.html' }
    ]
  }
  return []
})

const showPlanButton = computed(() => planLinks.value.length > 0)

const planModalTitle = computed(() => {
  if (!analysisResult.value) return '应急预案'
  return `${selectedReservoir.value} - ${analysisResult.value.status === '危险' ? '危险' : '警戒'}水位应急预案`
})

const openPlan = (url) => {
  window.open(url, '_blank')
}

const openPlanModal = () => {
  if (!planLinks.value.length) return
  showPlanModal.value = true
  nextTick(() => placeModal(planModalRef, { preferRight: true }))
}

const openAnalysisModal = () => {
  showAnalysisModal.value = true
  nextTick(() => placeModal(analysisModalRef, { preferLeft: true }))
}

const closeAnalysisModal = () => {
  showAnalysisModal.value = false
  resetModalPosition(analysisModalRef)
}

const closePlanModal = () => {
  showPlanModal.value = false
  resetModalPosition(planModalRef)
}

const resetModalPosition = (modalRef) => {
  if (!modalRef.value) return
  modalRef.value.style.left = ''
  modalRef.value.style.top = ''
  modalRef.value.style.transform = ''
  modalRef.value.style.position = ''
}

const placeModal = (modalRef, { preferLeft = false, preferRight = false } = {}) => {
  const el = modalRef.value
  if (!el) return
  const w = el.offsetWidth || 450
  const h = el.offsetHeight || 420
  let left = Math.max(24, (window.innerWidth - w) / 2)
  let top = Math.max(72, (window.innerHeight - h) / 2)
  if (preferLeft) left = Math.max(24, left - 160)
  if (preferRight) left = Math.min(window.innerWidth - w - 24, left + 200)
  // 避开右侧评估面板
  const panelW = rightPanelRef.value?.offsetWidth || 400
  if (left + w > window.innerWidth - panelW - 16) {
    left = Math.max(24, window.innerWidth - panelW - w - 24)
  }
  el.style.position = 'fixed'
  el.style.left = `${left}px`
  el.style.top = `${top}px`
  el.style.transform = 'none'
  el.style.margin = '0'
}

const startDrag = (e, modalRef, state) => {
  if (!e.target.closest('.analysis-modal-header') || e.target.closest('.close-btn') || !modalRef.value) {
    return
  }
  const el = modalRef.value
  const rect = el.getBoundingClientRect()
  el.style.position = 'fixed'
  el.style.left = `${rect.left}px`
  el.style.top = `${rect.top}px`
  el.style.transform = 'none'
  el.style.margin = '0'
  state.dragging = true
  state.startX = e.clientX
  state.startY = e.clientY
  state.origLeft = rect.left
  state.origTop = rect.top
  state.onMove = (ev) => onDrag(ev, modalRef, state)
  state.onUp = () => stopDrag(state)
  document.addEventListener('mousemove', state.onMove)
  document.addEventListener('mouseup', state.onUp)
  e.preventDefault()
}

const onDrag = (e, modalRef, state) => {
  if (!state.dragging || !modalRef.value) return
  const el = modalRef.value
  const w = el.offsetWidth
  const h = el.offsetHeight
  let left = state.origLeft + (e.clientX - state.startX)
  let top = state.origTop + (e.clientY - state.startY)
  left = Math.max(0, Math.min(window.innerWidth - Math.min(w, 120), left))
  top = Math.max(0, Math.min(window.innerHeight - 48, top))
  el.style.left = `${left}px`
  el.style.top = `${top}px`
}

const stopDrag = (state) => {
  state.dragging = false
  if (state.onMove) document.removeEventListener('mousemove', state.onMove)
  if (state.onUp) document.removeEventListener('mouseup', state.onUp)
  state.onMove = null
  state.onUp = null
}

onMounted(async () => {
  await nextTick()
  mapAlive = true
  applyQueryPrefill()
  await Promise.all([initMap(), loadReservoirMeta(), loadWarnings()])
})

const applyQueryPrefill = () => {
  const q = route.query || {}
  if (q.mode === 'environment' || q.mode === 'waterLevel') {
    warningMode.value = q.mode
  }
  if (q.reservoir) selectedReservoir.value = String(q.reservoir)
  if (q.indicator) envIndicator.value = String(q.indicator)
  if (q.value != null && !Number.isNaN(Number(q.value))) {
    envValue.value = Number(q.value)
  }
}

const goSimulation = () => {
  router.push({
    path: '/simulation',
    query: {
      mode: 'lake',
      source: selectedReservoir.value || '',
      pollutant: INDICATOR_TO_POLLUTANT[envIndicator.value] || 'ammonia',
      value: envValue.value != null ? String(envValue.value) : ''
    }
  })
}

onUnmounted(() => {
  mapAlive = false
  stopDrag(dragState)
  stopDrag(planDragState)
  selectionHighlight?.destroy()
  selectionHighlight = null
  view?.destroy()
  view = null
})

const getLayerColorMap = () =>
  Object.fromEntries(WARNING_LAYERS.map(({ key }) => [key, DEFAULT_LAYER_COLORS[key]]))

const loadReservoirMeta = async () => {
  try {
    const res = await warningAPI.getReservoirs()
    reservoirMetaList.value = res.data || []
  } catch (e) {
    console.warn('水库元数据加载失败', e)
    reservoirMetaList.value = []
  }
}

const findReservoirFeature = async (name) => {
  if (!reservoirLayer || !name) return null
  try {
    const result = await reservoirLayer.queryFeatures({
      where: '1=1',
      outFields: ['*'],
      returnGeometry: true
    })
    return (
      result.features.find((f) => {
        const { value } = resolveNameField(f.attributes || {})
        return normalizeReservoirName(value) === name
      }) || null
    )
  } catch (e) {
    console.warn('查询水库要素失败', e)
    return null
  }
}

const highlightSelectedReservoir = async (name, geometry) => {
  if (!selectionHighlight) return
  if (!name) {
    selectionHighlight.clear()
    return
  }
  let geom = geometry
  if (!geom) {
    const feature = await findReservoirFeature(name)
    geom = feature?.geometry || null
  }
  if (!geom || !mapAlive) {
    selectionHighlight.clear()
    return
  }
  selectionHighlight.set(geom)
  try {
    await view?.goTo({ target: geom, zoom: Math.max(view.zoom, 11) })
  } catch {
    // ignore
  }
}

const selectReservoir = async (name, geometry = null) => {
  const normalized = normalizeReservoirName(name || '')
  selectedReservoir.value = normalized
  analysisResult.value = null
  summaryExpanded.value = true
  await highlightSelectedReservoir(normalized, geometry)
}

const initMap = async () => {
  layerError.value = ''
  try {
    const { map, view: mapView } = await createMapView(mapRef.value, { zoom: 9 })
    if (!mapAlive) {
      mapView.destroy()
      return
    }
    view = mapView
    Object.assign(
      layerInstances,
      addBusinessLayersToMap(map, { popupEnabled: false, keys: WARNING_LAYER_KEYS })
    )

    layerControls.forEach((item) => {
      const layer = layerInstances[item.key]
      if (layer) layer.visible = item.visible
    })

    const loadResults = await loadBusinessLayers(layerInstances, getLayerColorMap(), map)
    if (!mapAlive) return

    reorderBusinessLayers(map, layerInstances)
    reservoirLayer = layerInstances.reservoirs
    selectionHighlight = createSelectionHighlight(view)

    const failedLayers = loadResults.filter((r) => !r.ok && !r.cancelled)
    if (failedLayers.length === loadResults.length) {
      layerError.value = '业务图层未加载，请确认 GeoScene 服务已启动（6443）'
      ElMessage.warning(layerError.value)
    } else if (failedLayers.length) {
      layerError.value = `部分图层加载失败：${failedLayers.map((r) => r.key).join('、')}`
    }

    let monitorData = []
    try {
      const monitorRes = await sectionMonitorAPI.list({})
      monitorData = monitorRes.data || []
    } catch (e) {
      console.warn('监测数据加载失败，监测点将仅显示水库位置', e)
    }
    if (!mapAlive) return

    if (reservoirLayer && !reservoirLayer.destroyed) {
      monitoringLayer = await createMonitoringLayer(reservoirLayer, monitorData)
      if (!mapAlive) return
      map.add(monitoringLayer)
      monitoringLayer.visible = warningMode.value === 'environment' && monitoringVisible.value
    }

    view.on('click', async (event) => {
      if (!mapAlive || !view) return
      const hit = await view.hitTest(event)
      if (warningMode.value === 'waterLevel') {
        const graphic = hit.results.find((r) => r.graphic?.layer === reservoirLayer)?.graphic
        if (!graphic?.attributes) return
        const { value } = resolveNameField(graphic.attributes)
        await selectReservoir(value, graphic.geometry)
      } else {
        const graphic = hit.results.find((r) => r.graphic?.layer === monitoringLayer)?.graphic
        if (!graphic?.attributes) return
        const name = graphic.attributes.reservoirName || ''
        if (graphic.attributes.ammoniaNitrogen != null) {
          envValue.value = graphic.attributes.ammoniaNitrogen
        }
        await selectReservoir(name)
      }
    })

    await fitMapToLayers()
    if (selectedReservoir.value) {
      await highlightSelectedReservoir(selectedReservoir.value)
    }
  } catch (e) {
    if (!mapAlive || String(e?.message || '').includes('destroyed')) return
    console.error(e)
    layerError.value = '地图加载失败，请查看控制台错误信息'
    ElMessage.error('地图加载失败')
  } finally {
    if (mapAlive) loading.value = false
  }
}

const fitMapToLayers = async () => {
  if (!view) return
  const layers = WARNING_LAYERS.map(({ key }) => layerInstances[key]).filter((l) => l?.fullExtent)
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

const toggleLayer = (key, visible) => {
  const layer = layerInstances[key]
  if (layer) layer.visible = visible
}

const toggleMonitoringLayer = () => {
  if (monitoringLayer) {
    monitoringLayer.visible = warningMode.value === 'environment' && monitoringVisible.value
  }
}

const setWarningMode = (mode) => {
  if (warningMode.value === mode) return
  warningMode.value = mode
  selectedReservoir.value = ''
  analysisResult.value = null
  inputLevel.value = null
  envValue.value = null
  selectionHighlight?.clear()
  toggleMonitoringLayer()
}

const loadWarnings = async () => {
  try {
    const res = await warningAPI.list()
    warningList.value = res.data || []
  } catch (e) {
    console.warn(e)
  }
}

const evaluate = async () => {
  if (!selectedReservoir.value) return
  try {
    if (warningMode.value === 'waterLevel') {
      if (inputLevel.value == null) {
        ElMessage.warning('请输入当前水位')
        return
      }
      const res = await warningAPI.evaluate({
        reservoirName: selectedReservoir.value,
        waterLevel: inputLevel.value
      })
      analysisResult.value = res.data
      statusMap.value[selectedReservoir.value] = res.data.status
      const { statusMap: layerStatusMap, nameField } = await mapStatusToLayerValues(
        reservoirLayer,
        statusMap.value
      )
      applyWarningRenderer(reservoirLayer, layerStatusMap, nameField)
      await highlightSelectedReservoir(selectedReservoir.value)
    } else {
      if (envValue.value == null) {
        ElMessage.warning('请输入监测值')
        return
      }
      const res = await warningAPI.evaluateEnv({
        reservoirName: selectedReservoir.value,
        indicator: envIndicator.value,
        value: envValue.value
      })
      analysisResult.value = res.data
      envStatusMap.value[selectedReservoir.value] = res.data.status
      applyMonitoringWarningColors(monitoringLayer, envStatusMap.value)
      await highlightSelectedReservoir(selectedReservoir.value)
    }
    await loadWarnings()
    openAnalysisModal()
    ElMessage.success('评估完成')
  } catch (e) {
    console.error(e)
    ElMessage.error('评估失败')
  }
}
</script>
