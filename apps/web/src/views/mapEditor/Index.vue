<template>
  <div class="map-editor-page">
    <div class="map-editor-page-content">
      <div ref="mapRef" class="editor-map"></div>

      <!-- 图层控制 -->
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
                    name="editor-basemap"
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
            <div v-if="item.colorKey" class="layer-color-control">
              <div
                class="color-preview"
                :class="{ 'point-preview': item.isPoint }"
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

      <!-- 工具栏：参考 ArcGIS 菜单栏，按功能分组 -->
      <div class="editor-menubar">
        <div class="toolbar-group">
          <span class="toolbar-group-label">导航</span>
          <div class="toolbar-group-tools">
            <div class="zoom-dropdown-container">
              <img
                src="/icons/放大.png"
                alt="放大"
                title="放大"
                :class="{ 'active-tool': activeTool === 'zoomIn' || activeTool === 'rectangleZoom' }"
                @click="toggleZoomDropdown"
              />
              <div v-if="showZoomDropdown" class="zoom-dropdown">
                <div class="zoom-option" @click="activateZoomIn">按点放大</div>
                <div class="zoom-option" @click="activateRectZoom">拉框放大</div>
              </div>
            </div>
            <div class="zoom-dropdown-container">
              <img
                src="/icons/缩小.png"
                alt="缩小"
                title="缩小"
                :class="{ 'active-tool': activeTool === 'zoomOut' || activeTool === 'rectangleZoomOut' }"
                @click="toggleZoomOutDropdown"
              />
              <div v-if="showZoomOutDropdown" class="zoom-dropdown">
                <div class="zoom-option" @click="activateZoomOut">按点缩小</div>
                <div class="zoom-option" @click="activateRectZoomOut">拉框缩小</div>
              </div>
            </div>
            <img src="/icons/全图.png" alt="全图" title="全图" @click="viewFullExtent" />
            <img
              src="/icons/手.png"
              alt="平移"
              title="平移"
              :class="{ 'active-tool': activeTool === 'pan' }"
              @click="activatePan"
            />
            <img
              src="/icons/左箭头.png"
              alt="返回上一视图"
              title="返回上一视图"
              class="toolbar-nav"
              :class="{ 'disabled-tool': !canGoBack }"
              @click="goToPreviousView"
            />
            <img
              src="/icons/右箭头.png"
              alt="转至下一视图"
              title="转至下一视图"
              class="toolbar-nav"
              :class="{ 'disabled-tool': !canGoForward }"
              @click="goToNextView"
            />
          </div>
        </div>

        <div class="toolbar-divider"></div>

        <div class="toolbar-group">
          <span class="toolbar-group-label">选择</span>
          <div class="toolbar-group-tools">
            <img
              src="/icons/选择要素.png"
              alt="选择要素"
              title="选择要素"
              :class="{ 'active-tool': activeTool === 'selectFeature' }"
              @click="activateSelectFeature"
            />
            <div
              v-if="selectionCount > 0"
              class="clear-selection-btn"
              title="清除所有选择"
              @click="clearAllSelections"
            >
              🗑
            </div>
          </div>
        </div>

        <div class="toolbar-divider"></div>

        <div class="toolbar-group">
          <span class="toolbar-group-label">查询</span>
          <div class="toolbar-group-tools">
            <img
              src="/icons/查询.png"
              alt="查询"
              title="查询"
              :class="{ 'active-tool': showQueryModal }"
              @click="toggleQuery"
            />
            <img
              src="/icons/识别要素.png"
              alt="识别要素"
              title="识别要素"
              :class="{ 'active-tool': activeTool === 'identifyFeature' }"
              @click="activateIdentify"
            />
          </div>
        </div>

        <div class="toolbar-divider"></div>

        <div class="toolbar-group">
          <span class="toolbar-group-label">分析</span>
          <div class="toolbar-group-tools">
            <img
              src="/icons/渲染.png"
              alt="渲染"
              title="水库监测数据可视化渲染"
              :class="{ 'active-tool': showRenderModal }"
              @click="toggleRender"
            />
            <img
              src="/icons/标注.png"
              alt="标注"
              title="标注"
              :class="{ 'active-tool': showAnnotationPanel }"
              @click="toggleAnnotation"
            />
          </div>
        </div>
      </div>

      <!-- 识别类型：点 / 线 / 面 -->
      <div v-if="activeTool === 'identifyFeature'" class="identify-type-bar">
        <span class="identify-type-label">识别类型</span>
        <button
          v-for="opt in identifyTypeOptions"
          :key="opt.value"
          type="button"
          class="identify-type-btn"
          :class="{ active: identifyGeomType === opt.value }"
          @click="setIdentifyGeomType(opt.value)"
        >
          {{ opt.label }}
        </button>
        <span class="identify-type-hint">{{ identifyGeomType ? '请在地图上点击目标要素' : '请先选择点 / 线 / 面' }}</span>
      </div>

      <!-- 查询弹窗 -->
      <div v-if="showQueryModal" class="search-bar-modal">
        <div class="search-header">
          <h3>图层查询</h3>
          <span class="search-close" @click="toggleQuery">×</span>
        </div>
        <label class="layer-selection-label">选择查询图层：</label>
        <el-select v-model="queryLayerKey" style="width: 100%">
          <el-option v-for="item in queryableLayers" :key="item.key" :label="item.label" :value="item.key" />
        </el-select>
        <div class="search-input-container">
          <input v-model="queryKeyword" class="search-input" placeholder="输入名称关键字" @keyup.enter="runQuery" />
          <button type="button" class="search-btn" @click="runQuery">查询</button>
        </div>
        <div v-if="queryResults.length" class="query-results">
          <div v-for="(row, index) in queryResults" :key="index" class="query-result-item">
            <span>{{ row.name }}</span>
            <button type="button" class="query-locate-btn" @click="locateFeature(row)">定位</button>
          </div>
        </div>
      </div>

      <!-- 渲染弹窗 -->
      <div v-if="showRenderModal" class="render-modal">
        <div class="render-modal-header">
          <span>数据分级渲染</span>
          <span class="search-close" @click="toggleRender">×</span>
        </div>
        <div class="render-row">
          <label>图层</label>
          <select v-model="selectedRenderLayer" class="render-select" @change="onRenderLayerChange">
            <option value="monitoring">监测点</option>
            <option value="reservoirs">水库</option>
          </select>
        </div>
        <div class="render-row">
          <label>字段</label>
          <select v-model="renderField" class="render-select">
            <option v-for="field in currentRenderFields" :key="field.value" :value="field.value">
              {{ field.label }}
            </option>
          </select>
        </div>
        <div class="render-row">
          <label>分级</label>
          <select v-model="gradeCount" class="render-select">
            <option v-for="n in 6" :key="n" :value="n">{{ n }}级</option>
          </select>
        </div>
        <div class="render-row">
          <button type="button" class="render-btn" @click="handleRender">渲染</button>
          <button type="button" class="render-reset-btn" @click="resetRender">重置</button>
        </div>
        <div v-if="renderMessage" class="render-error">{{ renderMessage }}</div>
      </div>

      <!-- 标注面板 -->
      <div v-if="showAnnotationPanel" class="annotation-control-panel">
        <div class="annotation-panel-header">
          <span>标注控制</span>
          <span class="search-close" @click="toggleAnnotation">×</span>
        </div>

        <div class="annotation-section">
          <h5>图层和字段</h5>
          <select v-model="annotationLayerKey" class="annotation-select" @change="loadAnnotationFields">
            <option v-for="item in annotationLayerOptions" :key="item.key" :value="item.key">
              {{ item.label }}
            </option>
          </select>
          <select v-model="annotationField" class="annotation-select">
            <option v-for="field in annotationFields" :key="field.value" :value="field.value">
              {{ field.label }}
            </option>
          </select>
          <div class="annotation-actions">
            <button type="button" class="annotation-btn" @click="generateAnnotations">生成字段标注</button>
            <button type="button" class="render-reset-btn annotation-clear-btn" @click="clearAnnotations">
              清除所有标注
            </button>
          </div>
        </div>

        <div class="annotation-section">
          <h5>显示与缩放</h5>
          <label class="annotation-check-row">
            <input v-model="annotationSettings.visible" type="checkbox" @change="syncAnnotationVisibility" />
            显示标注
          </label>
          <div class="annotation-form-row">
            <label>最小缩放</label>
            <el-input-number
              v-model="annotationSettings.minZoom"
              :min="1"
              :max="22"
              :step="1"
              size="small"
              controls-position="right"
              @change="syncAnnotationVisibility"
            />
          </div>
          <div class="annotation-form-row">
            <label>最大缩放</label>
            <el-input-number
              v-model="annotationSettings.maxZoom"
              :min="1"
              :max="22"
              :step="1"
              size="small"
              controls-position="right"
              @change="syncAnnotationVisibility"
            />
          </div>
          <div class="annotation-hint">当前缩放：{{ currentMapZoom }}</div>
        </div>

        <div class="annotation-section annotation-section-last">
          <h5>样式设置</h5>
          <div class="annotation-form-row">
            <label>字号</label>
            <el-input-number
              v-model="annotationSettings.fontSize"
              :min="10"
              :max="24"
              :step="1"
              size="small"
              controls-position="right"
              @change="applyAnnotationStyleNow"
            />
          </div>
          <div class="annotation-form-row">
            <label>文字颜色</label>
            <input
              v-model="annotationSettings.color"
              type="color"
              class="annotation-color"
              @input="applyAnnotationStyleNow"
            />
          </div>
          <div class="annotation-form-row">
            <label>描边颜色</label>
            <input
              v-model="annotationSettings.haloColor"
              type="color"
              class="annotation-color"
              @input="applyAnnotationStyleNow"
            />
          </div>
          <div class="annotation-form-row">
            <label>描边宽度</label>
            <el-input-number
              v-model="annotationSettings.haloSize"
              :min="0"
              :max="4"
              :step="0.5"
              size="small"
              controls-position="right"
              @change="applyAnnotationStyleNow"
            />
          </div>
        </div>
      </div>

      <!-- 渲染图例 -->
      <div v-if="showRenderLegend" class="legend-panel">
        <div class="legend-header">
          <h4>{{ renderLegendTitle }}</h4>
          <span class="legend-close" @click="showRenderLegend = false">×</span>
        </div>
        <div v-for="(grade, index) in renderLegendGrades" :key="index" class="legend-item-row">
          <span class="legend-color-box" :style="{ backgroundColor: grade.color }"></span>
          <span>{{ grade.min }} - {{ grade.max }} ({{ grade.count }}个)</span>
        </div>
      </div>

      <!-- 识别结果：要素旁属性弹窗 -->
      <div
        v-if="identifyInfo.visible"
        class="identify-popup"
        :style="{ left: `${identifyInfo.left}px`, top: `${identifyInfo.top}px` }"
      >
        <div class="identify-popup-header">
          <span class="identify-title">{{ identifyInfo.name }}</span>
          <button type="button" class="identify-popup-close" title="关闭" @click="clearIdentify">×</button>
        </div>
        <div class="identify-popup-body">
          <div v-for="(item, index) in identifyInfo.fields" :key="index" class="identify-row">
            <span class="identify-label">{{ item.label }}</span>
            <span class="identify-value">{{ item.value }}</span>
          </div>
          <div v-if="!identifyInfo.fields.length" class="identify-empty">暂无属性</div>
        </div>
      </div>

      <!-- 水库详情 -->
      <div v-if="selectedReservoir && activeTool !== 'selectFeature'" class="reservoir-img-panel">
        <span class="img-panel-close" @click="closeReservoirPanel">×</span>
        <div class="reservoir-img-title">{{ selectedReservoir }}</div>
        <img
          v-show="!imgError"
          :src="reservoirImgUrl"
          :alt="selectedReservoir"
          class="reservoir-img-main"
          @error="imgError = true"
        />
        <div v-if="detailInfo" class="reservoir-details">
          <div class="details-title">水库详细信息</div>
          <div class="details-table">
            <div v-for="(val, key) in detailInfo" :key="key" class="detail-row">
              <span class="detail-label">{{ key }}：</span>
              <span class="detail-value">{{ val }}</span>
            </div>
          </div>
          <div class="reservoir-buttons">
            <button type="button" class="reservoir-btn flood-history-btn" @click="showFloodHistoryPanel = true">
              防洪历史
            </button>
          </div>
        </div>
      </div>

      <!-- 防洪历史 -->
      <div v-if="showFloodHistoryPanel" class="flood-history-panel">
        <span class="history-panel-close" @click="showFloodHistoryPanel = false">×</span>
        <div class="history-title">{{ selectedReservoir }} - 防洪历史</div>
        <div v-for="(item, idx) in floodHistoryList" :key="idx" class="history-section">
          <h4>{{ item.title }}</h4>
          <p>{{ item.content }}</p>
        </div>
      </div>

      <div v-if="loading" class="loading-overlay">正在加载地图数据...</div>
      <div v-if="layerError" class="layer-error">{{ layerError }}</div>
    </div>
  </div>
</template>

<script setup>
import GraphicsLayer from '@geoscene/core/layers/GraphicsLayer'
import Graphic from '@geoscene/core/Graphic'
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import '@/assets/map-editor-page.css'
import geosceneConfig from '@/config/geoscene'
import { createMapView } from '@/geoscene/initMapView'
import { addBusinessLayersToMap, reorderBusinessLayers } from '@/geoscene/layers/createBusinessLayers'
import { createMonitoringLayer } from '@/geoscene/layers/createMonitoringLayer'
import { BASEMAP_MODES, applyMapBasemap } from '@/geoscene/basemaps/basemapModes'
import { resolveNameField, mapStatusToLayerValues } from '@/geoscene/utils/resolveNameField'
import { normalizeReservoirName } from '@/geoscene/reservoirNames'
import {
  RESERVOIR_DETAILS,
  getFloodHistory,
  getReservoirImageUrl
} from '@/geoscene/data/reservoirMeta'
import { enableRectangleZoom } from '@/geoscene/tools/rectangleZoom'
import { equalCountClassification } from '@/geoscene/tools/mapEditor/classification'
import {
  applyMonitoringClassBreaks,
  applyNameColorRenderer,
  resetLayerStyle
} from '@/geoscene/tools/mapEditor/renderLayer'
import { createSelectionManager } from '@/geoscene/tools/mapEditor/selectionHighlight'
import { createFieldAnnotations, removeAnnotationLayer, applyAnnotationStyle } from '@/geoscene/tools/mapEditor/annotations'
import { buildIdentifyFields, registerLayerFieldAliases, listAnnotatableFields } from '@/geoscene/utils/featureFieldLabels'
import { getGeometryCentroid } from '@/geoscene/utils/getGeometryCentroid'
import { sectionMonitorAPI, waterSituationAPI } from '@/api'
import {
  applyLayerRenderer,
  DEFAULT_LAYER_COLORS,
  DEFAULT_MONITORING_COLOR,
  loadBusinessLayers
} from '@/geoscene/layers/layerStyle'

const EDITOR_LAYER_KEYS = ['districts', 'waterLines', 'reservoirs', 'waterAreas', 'settlements']

const mapRef = ref(null)
const loading = ref(true)
const layerError = ref('')
const showControlPanel = ref(true)
const showBasemapMenu = ref(false)
const basemapMode = ref('tianditu')
const showQueryModal = ref(false)
const showRenderModal = ref(false)
const showAnnotationPanel = ref(false)
const showZoomDropdown = ref(false)
const showZoomOutDropdown = ref(false)
const showFloodHistoryPanel = ref(false)
const showRenderLegend = ref(false)
const activeTool = ref('pan')
const queryLayerKey = ref('reservoirs')
const queryKeyword = ref('')
const queryResults = ref([])
const selectedReservoir = ref('')
const imgError = ref(false)
const renderMessage = ref('')
const renderLegendTitle = ref('')
const renderLegendGrades = ref([])
const selectedRenderLayer = ref('monitoring')
const renderField = ref('ammoniaNitrogen')
const gradeCount = ref(3)
const annotationLayerKey = ref('reservoirs')
const annotationField = ref('')
const annotationFields = ref([])
const currentMapZoom = ref(9)
const annotationSettings = reactive({
  visible: true,
  minZoom: 8,
  maxZoom: 18,
  fontSize: 12,
  color: '#1a1a1a',
  haloColor: '#ffffff',
  haloSize: 1.5
})
const selectionCount = ref(0)
const canGoBack = ref(false)
const canGoForward = ref(false)

const identifyGeomType = ref('')
const identifyTypeOptions = [
  { value: 'point', label: '点' },
  { value: 'polyline', label: '线' },
  { value: 'polygon', label: '面' }
]
const identifyInfo = ref({
  visible: false,
  name: '',
  fields: [],
  left: 0,
  top: 0
})
const reservoirStatsMap = ref({})

let view = null
let mapInstance = null
let layerMap = {}
let monitorLayer = null
let rectZoomTool = null
let selectionManager = null
let identifyHighlightLayer = null
let identifyAnchorPoint = null
let identifyWatchHandle = null
let annotationLayer = null
let annotationZoomHandle = null
let viewHistory = []
let historyIndex = -1
let suppressHistory = false
/** 组件卸载后阻止异步地图初始化继续写图层 */
let mapAlive = true
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
  { key: 'reservoirs', label: '水库', visible: true, colorKey: 'reservoirs' },
  { key: 'monitoring', label: '监测点', visible: true, colorKey: 'monitoring', isPoint: true },
  { key: 'settlements', label: '居民点', visible: false, colorKey: 'settlements', isPoint: true }
])

const layerColors = reactive({
  ...DEFAULT_LAYER_COLORS,
  monitoring: DEFAULT_MONITORING_COLOR
})

const queryableLayers = [
  { key: 'reservoirs', label: '水库' },
  { key: 'waterAreas', label: '河流' },
  { key: 'waterLines', label: '河流' },
  { key: 'settlements', label: '居民点' }
]

const monitoringRenderFields = [
  { value: 'ammoniaNitrogen', label: '氨氮 (mg/L)' },
  { value: 'cod', label: 'COD (mg/L)' },
  { value: 'totalPhosphorus', label: '总磷 (mg/L)' }
]

const reservoirRenderFields = [
  { value: 'avgWaterLevel', label: '多年平均库水位 (m)' },
  { value: 'avgStorage', label: '多年平均蓄水量 (万m³)' },
  { value: 'avgInflow', label: '多年日平均入库流量 (m³/s)' },
  { value: 'avgOutflow', label: '多年日平均出库流量 (m³/s)' }
]

const annotationLayerOptions = [
  { key: 'reservoirs', label: '水库' },
  { key: 'waterAreas', label: '河流（水系面）' },
  { key: 'waterLines', label: '河网（水系线）' },
  { key: 'settlements', label: '居民点' },
  { key: 'districts', label: '行政区划' }
]

const currentRenderFields = computed(() =>
  selectedRenderLayer.value === 'monitoring' ? monitoringRenderFields : reservoirRenderFields
)

const detailInfo = computed(() => RESERVOIR_DETAILS[selectedReservoir.value] || null)
const reservoirImgUrl = computed(() => getReservoirImageUrl(selectedReservoir.value))
const floodHistoryList = computed(() => getFloodHistory(selectedReservoir.value))

const setColorInputRef = (key, el) => {
  if (el) colorInputRefs[key] = el
}

const triggerColorInput = (colorKey) => {
  colorInputRefs[colorKey]?.click()
}

onMounted(async () => {
  await nextTick()
  mapAlive = true
  document.addEventListener('click', closeBasemapMenu)
  await initMap()
})

onUnmounted(() => {
  mapAlive = false
  document.removeEventListener('click', closeBasemapMenu)
  disableRectZoom()
  clearAnnotations()
  clearIdentify()
  identifyWatchHandle?.remove?.()
  identifyWatchHandle = null
  annotationZoomHandle?.remove?.()
  annotationZoomHandle = null
  view?.destroy()
  view = null
  mapInstance = null
})

const disableRectZoom = () => {
  rectZoomTool?.disable()
  rectZoomTool = null
}

const closeToolPanels = () => {
  showZoomDropdown.value = false
  showZoomOutDropdown.value = false
  showBasemapMenu.value = false
}

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
      zoom: 9,
      basemapMode: basemapMode.value
    })
    if (!mapAlive) {
      mapView.destroy()
      return
    }
    mapInstance = map
    view = mapView
    layerMap = addBusinessLayersToMap(map, { popupEnabled: true, keys: EDITOR_LAYER_KEYS })

    layerItems.forEach((item) => {
      if (item.key === 'monitoring') return
      const keys = item.layerKeys || [item.key]
      keys.forEach((layerKey) => {
        const layer = layerMap[layerKey]
        if (layer) layer.visible = item.visible
      })
    })

    const colorMap = Object.fromEntries(EDITOR_LAYER_KEYS.map((key) => [key, layerColors[key]]))
    const loadResults = await loadBusinessLayers(layerMap, colorMap, map)
    if (!mapAlive) return

    Object.values(layerMap).forEach((layer) => registerLayerFieldAliases(layer))

    reorderBusinessLayers(map, layerMap)

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
    await loadReservoirStats()
    if (!mapAlive) return

    if (layerMap.reservoirs && !layerMap.reservoirs.destroyed) {
      monitorLayer = await createMonitoringLayer(layerMap.reservoirs, monitorData, {
        color: layerColors.monitoring,
        visible: layerItems.find((l) => l.key === 'monitoring')?.visible !== false
      })
      if (!mapAlive) return
      map.add(monitorLayer)
    }

    selectionManager = createSelectionManager(view)
    identifyHighlightLayer = new GraphicsLayer({ title: '识别高亮', listMode: 'hide' })
    view.map.add(identifyHighlightLayer)
    identifyWatchHandle = view.watch('extent', () => {
      if (identifyInfo.value.visible) updateIdentifyPopupPosition()
    })
    currentMapZoom.value = Math.round(view.zoom || 9)
    annotationZoomHandle = view.watch('zoom', (z) => {
      currentMapZoom.value = Math.round(z || 0)
      syncAnnotationVisibility()
    })
    setupViewHistory()
    await loadAnnotationFields()
    if (!mapAlive) return

    view.on('click', handleMapClick)
    await fitMapToLayers()
    pushViewHistory(true)
  } catch (e) {
    if (!mapAlive || String(e?.message || '').includes('destroyed')) return
    console.error(e)
    layerError.value = '地图加载失败，请查看控制台错误信息'
    ElMessage.error('地图加载失败')
  } finally {
    if (mapAlive) loading.value = false
  }
}

const loadReservoirStats = async () => {
  try {
    const res = await waterSituationAPI.list({ page: 1, pageSize: 5000 })
    const rows = res.data?.records || res.data?.list || res.data || []
    const groups = {}
    for (const row of rows) {
      const name = row.reservoirName
      if (!name) continue
      if (!groups[name]) {
        groups[name] = { count: 0, waterLevel: 0, storage: 0, inflow: 0, outflow: 0 }
      }
      groups[name].count += 1
      groups[name].waterLevel += Number(row.waterLevel) || 0
      groups[name].storage += Number(row.storage) || 0
      groups[name].inflow += Number(row.inflow) || 0
      groups[name].outflow += Number(row.outflow) || 0
    }
    const stats = {}
    Object.entries(groups).forEach(([name, g]) => {
      stats[name] = {
        avgWaterLevel: g.waterLevel / g.count,
        avgStorage: g.storage / g.count,
        avgInflow: g.inflow / g.count,
        avgOutflow: g.outflow / g.count
      }
    })
    reservoirStatsMap.value = stats
  } catch (e) {
    console.warn('加载水情统计失败', e)
  }
}

const setupViewHistory = () => {
  view.watch('stationary', (isStationary) => {
    if (isStationary && !suppressHistory) pushViewHistory()
  })
}

const pushViewHistory = (force = false) => {
  if (!view) return
  const entry = {
    center: [view.center.longitude, view.center.latitude],
    zoom: view.zoom
  }
  const last = viewHistory[historyIndex]
  if (
    !force &&
    last &&
    last.zoom === entry.zoom &&
    Math.abs(last.center[0] - entry.center[0]) < 0.0001 &&
    Math.abs(last.center[1] - entry.center[1]) < 0.0001
  ) {
    return
  }
  viewHistory = viewHistory.slice(0, historyIndex + 1)
  viewHistory.push(entry)
  historyIndex = viewHistory.length - 1
  updateNavigationButtons()
}

const updateNavigationButtons = () => {
  canGoBack.value = historyIndex > 0
  canGoForward.value = historyIndex < viewHistory.length - 1
}

const goToViewEntry = async (entry) => {
  if (!view || !entry) return
  suppressHistory = true
  await view.goTo({ center: entry.center, zoom: entry.zoom })
  suppressHistory = false
}

const goToPreviousView = async () => {
  if (!canGoBack.value) return
  historyIndex -= 1
  await goToViewEntry(viewHistory[historyIndex])
  updateNavigationButtons()
}

const goToNextView = async () => {
  if (!canGoForward.value) return
  historyIndex += 1
  await goToViewEntry(viewHistory[historyIndex])
  updateNavigationButtons()
}

const handleMapClick = async (event) => {
  if (showQueryModal.value) return
  const hit = await view.hitTest(event)
  const graphic = pickIdentifyGraphic(hit.results)

  if (activeTool.value === 'selectFeature') {
    if (!graphic) {
      if (selectionCount.value) clearAllSelections()
      return
    }
    selectionManager.toggle(graphic)
    selectionCount.value = selectionManager.count()
    return
  }

  if (activeTool.value === 'identifyFeature') {
    if (!identifyGeomType.value) {
      ElMessage.warning('请先选择识别类型：点 / 线 / 面')
      return
    }
    if (!graphic) {
      clearIdentify()
      ElMessage.info(`未点中${identifyTypeLabel()}要素`)
      return
    }
    showIdentifyInfo(graphic, event)
    return
  }

  if (!graphic) return
  if (graphic.layer === layerMap.reservoirs && graphic.attributes) {
    const { value } = resolveNameField(graphic.attributes)
    selectedReservoir.value = normalizeReservoirName(value)
    imgError.value = false
    showFloodHistoryPanel.value = false
  }
}

function identifyTypeLabel() {
  const opt = identifyTypeOptions.find((o) => o.value === identifyGeomType.value)
  return opt?.label || ''
}

function isIdentifyHighlightLayer(layer) {
  return layer === identifyHighlightLayer || layer === selectionManager?.layer
}

function matchesIdentifyGeomType(geometry) {
  if (!geometry || !identifyGeomType.value) return false
  const t = geometry.type
  if (identifyGeomType.value === 'point') return t === 'point' || t === 'multipoint'
  if (identifyGeomType.value === 'polyline') return t === 'polyline'
  if (identifyGeomType.value === 'polygon') return t === 'polygon' || t === 'multipolygon'
  return false
}

function pickIdentifyGraphic(results = []) {
  return results.find((r) => {
    const g = r.graphic
    if (!g?.geometry) return false
    if (isIdentifyHighlightLayer(g.layer)) return false
    if (activeTool.value === 'identifyFeature') return matchesIdentifyGeomType(g.geometry)
    return true
  })?.graphic
}

function identifyHighlightSymbol(geometry) {
  const type = geometry?.type
  if (type === 'polyline') {
    return { type: 'simple-line', color: [0, 200, 255, 1], width: 5 }
  }
  if (type === 'point' || type === 'multipoint') {
    return {
      type: 'simple-marker',
      style: 'circle',
      color: [0, 200, 255, 0.85],
      size: 16,
      outline: { color: [255, 255, 255, 1], width: 2 }
    }
  }
  return {
    type: 'simple-fill',
    color: [0, 200, 255, 0.28],
    outline: { color: [0, 180, 230, 1], width: 3 }
  }
}

function setIdentifyHighlight(graphic) {
  identifyHighlightLayer?.removeAll()
  if (!graphic?.geometry || !identifyHighlightLayer) return
  identifyHighlightLayer.add(
    new Graphic({
      geometry: graphic.geometry,
      symbol: identifyHighlightSymbol(graphic.geometry)
    })
  )
}

function clearIdentify() {
  identifyHighlightLayer?.removeAll()
  identifyAnchorPoint = null
  identifyInfo.value = { visible: false, name: '', fields: [], left: 0, top: 0 }
}

function updateIdentifyPopupPosition() {
  if (!view || !identifyAnchorPoint || !identifyInfo.value.visible) return
  try {
    const screen = view.toScreen(identifyAnchorPoint)
    if (!screen || !Number.isFinite(screen.x) || !Number.isFinite(screen.y)) return
    const container = mapRef.value?.parentElement || mapRef.value
    const maxW = container?.clientWidth || 400
    const maxH = container?.clientHeight || 400
    const popupW = 280
    const popupH = 240
    let left = screen.x + 14
    let top = screen.y + 14
    if (left + popupW > maxW - 8) left = Math.max(8, screen.x - popupW - 14)
    if (top + popupH > maxH - 8) top = Math.max(8, screen.y - popupH - 14)
    if (left < 8) left = 8
    if (top < 8) top = 8
    identifyInfo.value = { ...identifyInfo.value, left, top }
  } catch (_) {}
}

const showIdentifyInfo = (graphic, event) => {
  const attrs = graphic.attributes || {}
  const { value } = resolveNameField(attrs)
  const fields = buildIdentifyFields(graphic)
  const name =
    value ||
    attrs.reservoirName ||
    attrs.monitorPointName ||
    graphic.layer?.title ||
    '识别要素'

  identifyAnchorPoint =
    event?.mapPoint ||
    getGeometryCentroid(graphic.geometry) ||
    graphic.geometry?.extent?.center ||
    null

  setIdentifyHighlight(graphic)

  let left = 24
  let top = 120
  try {
    if (view && identifyAnchorPoint) {
      const screen = view.toScreen(identifyAnchorPoint)
      if (screen) {
        left = screen.x + 14
        top = screen.y + 14
      }
    }
  } catch (_) {}

  identifyInfo.value = {
    visible: true,
    name,
    fields,
    left,
    top
  }
  nextTick(() => updateIdentifyPopupPosition())
}

const setIdentifyGeomType = (type) => {
  identifyGeomType.value = type
  clearIdentify()
  ElMessage.success(`已选择识别「${identifyTypeLabel()}」要素，请在地图上点击`)
}

const fitMapToLayers = async () => {
  if (!view) return
  const layers = EDITOR_LAYER_KEYS.map((key) => layerMap[key]).filter((l) => l?.fullExtent)
  if (!layers.length) return
  try {
    let extent = layers[0].fullExtent.clone()
    for (let i = 1; i < layers.length; i++) extent = extent.union(layers[i].fullExtent)
    suppressHistory = true
    await view.goTo(extent.expand(1.08))
    suppressHistory = false
    pushViewHistory(true)
  } catch {
    suppressHistory = false
  }
}

const toggleLayer = (key, visible) => {
  const item = layerItems.find((l) => l.key === key)
  if (!item) return
  const nextVisible = visible ?? item.visible
  item.visible = nextVisible
  if (key === 'monitoring') {
    if (monitorLayer) monitorLayer.visible = !!nextVisible
    return
  }
  const keys = item.layerKeys || [key]
  keys.forEach((layerKey) => {
    if (layerMap[layerKey]) layerMap[layerKey].visible = nextVisible
  })
}

const applyMonitoringColor = (color) => {
  if (!monitorLayer) return
  monitorLayer.graphics.forEach((graphic) => {
    graphic.symbol = {
      type: 'simple-marker',
      color,
      size: 10,
      outline: { color: '#ffffff', width: 1.5 }
    }
  })
}

const applyLayerColor = (key) => {
  if (key === 'monitoring') {
    applyMonitoringColor(layerColors.monitoring)
    return
  }
  if (key === 'rivers') {
    if (layerMap.waterLines && layerColors.waterLines) {
      applyLayerRenderer(layerMap.waterLines, 'waterLines', layerColors.waterLines)
    }
    if (layerMap.waterAreas && layerColors.waterLines) {
      applyLayerRenderer(layerMap.waterAreas, 'waterAreas', layerColors.waterLines)
    }
    return
  }
  const layer = layerMap[key]
  if (layer && layerColors[key]) applyLayerRenderer(layer, key, layerColors[key])
}

const setActiveTool = (tool) => {
  closeToolPanels()
  disableRectZoom()
  if (activeTool.value === 'identifyFeature' && tool !== 'identifyFeature') {
    clearIdentify()
    identifyGeomType.value = ''
  }
  activeTool.value = tool
}

const toggleZoomDropdown = () => {
  showZoomOutDropdown.value = false
  showZoomDropdown.value = !showZoomDropdown.value
}

const toggleZoomOutDropdown = () => {
  showZoomDropdown.value = false
  showZoomOutDropdown.value = !showZoomOutDropdown.value
}

const activateZoomIn = () => {
  setActiveTool('zoomIn')
  if (view) view.zoom += 1
}

const activateRectZoom = () => {
  setActiveTool('rectangleZoom')
  rectZoomTool = enableRectangleZoom(view, { mode: 'in' })
  ElMessage.info('在地图上拖拽绘制矩形框选放大区域')
}

const activateZoomOut = () => {
  setActiveTool('zoomOut')
  if (view) view.zoom -= 1
}

const activateRectZoomOut = () => {
  setActiveTool('rectangleZoomOut')
  rectZoomTool = enableRectangleZoom(view, { mode: 'out' })
  ElMessage.info('在地图上拖拽绘制矩形框选缩小区域')
}

const viewFullExtent = async () => {
  setActiveTool('pan')
  await fitMapToLayers()
}

const activatePan = () => setActiveTool('pan')

const activateSelectFeature = () => {
  setActiveTool('selectFeature')
  showQueryModal.value = false
  showRenderModal.value = false
  showAnnotationPanel.value = false
}

const activateIdentify = () => {
  setActiveTool('identifyFeature')
  showQueryModal.value = false
  showRenderModal.value = false
  showAnnotationPanel.value = false
  identifyGeomType.value = ''
  clearIdentify()
  ElMessage.info('请先选择识别类型：点 / 线 / 面')
}

const clearAllSelections = () => {
  selectionManager?.clear()
  selectionCount.value = 0
  ElMessage.info('已清除所有要素选择')
}

const toggleQuery = () => {
  showQueryModal.value = !showQueryModal.value
  if (showQueryModal.value) {
    setActiveTool('query')
  } else {
    setActiveTool('pan')
  }
}

const toggleRender = () => {
  showRenderModal.value = !showRenderModal.value
  if (showRenderModal.value) setActiveTool('render')
}

const toggleAnnotation = () => {
  showAnnotationPanel.value = !showAnnotationPanel.value
}

const onRenderLayerChange = () => {
  renderField.value =
    selectedRenderLayer.value === 'monitoring' ? 'ammoniaNitrogen' : 'avgWaterLevel'
}

const handleRender = async () => {
  renderMessage.value = ''
  try {
    if (selectedRenderLayer.value === 'monitoring') {
      const values = []
      const nameValueMap = {}
      monitorLayer?.graphics.forEach((g) => {
        const v = Number(g.attributes?.[renderField.value])
        const name = g.attributes?.reservoirName
        if (!Number.isNaN(v) && name) {
          values.push(v)
          nameValueMap[name] = v
        }
      })
      if (values.length < gradeCount.value) {
        renderMessage.value = '监测点有效数据不足，无法分级渲染'
        return
      }
      const grades = equalCountClassification(values, gradeCount.value)
      applyMonitoringClassBreaks(monitorLayer, nameValueMap, grades)
      renderLegendTitle.value = `${monitoringRenderFields.find((f) => f.value === renderField.value)?.label || renderField.value} 等量分级`
      renderLegendGrades.value = grades.map((g) => ({ ...g, count: values.filter((v) => v >= g.min && v <= g.max).length }))
      showRenderLegend.value = true
      renderMessage.value = `渲染完成，共 ${values.length} 个监测点`
    } else {
      const values = []
      const valueByName = {}
      Object.entries(reservoirStatsMap.value).forEach(([name, stats]) => {
        const v = Number(stats[renderField.value])
        if (!Number.isNaN(v)) {
          values.push(v)
          valueByName[name] = v
        }
      })
      if (values.length < gradeCount.value) {
        renderMessage.value = '水库水情统计数据不足，无法分级渲染'
        return
      }
      const grades = equalCountClassification(values, gradeCount.value)
      const colorByNormalized = {}
      Object.entries(valueByName).forEach(([name, value]) => {
        const grade = grades.find((g) => value >= g.min && value <= g.max)
        if (grade) colorByNormalized[name] = grade.color
      })
      const { statusMap, nameField } = await mapStatusToLayerValues(
        layerMap.reservoirs,
        colorByNormalized
      )
      applyNameColorRenderer(layerMap.reservoirs, 'reservoirs', nameField, statusMap)
      renderLegendTitle.value = `${reservoirRenderFields.find((f) => f.value === renderField.value)?.label || renderField.value} 等量分级`
      renderLegendGrades.value = grades.map((g) => ({ ...g, count: values.filter((v) => v >= g.min && v <= g.max).length }))
      showRenderLegend.value = true
      renderMessage.value = `渲染完成，共 ${values.length} 个水库`
    }
  } catch (e) {
    console.error(e)
    renderMessage.value = '分级渲染失败'
  }
}

const resetRender = () => {
  if (selectedRenderLayer.value === 'monitoring') {
    applyMonitoringColor(layerColors.monitoring)
  } else {
    resetLayerStyle(layerMap.reservoirs, 'reservoirs', layerColors.reservoirs)
  }
  showRenderLegend.value = false
  renderMessage.value = ''
}

const loadAnnotationFields = async () => {
  const layer = layerMap[annotationLayerKey.value]
  if (!layer) return
  try {
    await layer.load()
    registerLayerFieldAliases(layer)
    const result = await layer.queryFeatures({ where: '1=1', outFields: ['*'], num: 1 })
    const attrs = result.features[0]?.attributes || {}
    const fields = listAnnotatableFields(attrs, layer)
    annotationFields.value = fields
    const stillValid = fields.some((f) => f.value === annotationField.value)
    if (!stillValid) {
      annotationField.value = fields[0]?.value || ''
    }
  } catch (e) {
    console.warn(e)
    annotationFields.value = []
    annotationField.value = ''
  }
}

const syncAnnotationVisibility = () => {
  if (!annotationLayer) return
  const zoom = view?.zoom ?? currentMapZoom.value
  const minZ = Number(annotationSettings.minZoom) || 1
  const maxZ = Number(annotationSettings.maxZoom) || 22
  const inRange = zoom >= minZ && zoom <= maxZ
  annotationLayer.visible = !!annotationSettings.visible && inRange
}

const applyAnnotationStyleNow = () => {
  if (!annotationLayer) return
  applyAnnotationStyle(annotationLayer, {
    fontSize: annotationSettings.fontSize,
    color: annotationSettings.color,
    haloColor: annotationSettings.haloColor,
    haloSize: annotationSettings.haloSize
  })
}

const generateAnnotations = async () => {
  if (!annotationField.value) {
    ElMessage.warning('请选择标注字段')
    return
  }
  const layer = layerMap[annotationLayerKey.value]
  if (!layer) {
    ElMessage.warning('图层未加载')
    return
  }
  clearAnnotations()
  try {
    annotationLayer = await createFieldAnnotations(view, layer, annotationField.value, {
      visible: annotationSettings.visible,
      fontSize: annotationSettings.fontSize,
      color: annotationSettings.color,
      haloColor: annotationSettings.haloColor,
      haloSize: annotationSettings.haloSize
    })
    syncAnnotationVisibility()
    ElMessage.success('标注已生成')
  } catch (e) {
    console.error(e)
    ElMessage.error('生成标注失败')
  }
}

const clearAnnotations = () => {
  if (annotationLayer) {
    removeAnnotationLayer(view, annotationLayer)
    annotationLayer = null
  }
}

const runQuery = async () => {
  const layer = layerMap[queryLayerKey.value]
  if (!layer) return
  const result = await layer.queryFeatures({ where: '1=1', outFields: ['*'], returnGeometry: true })
  const keyword = queryKeyword.value.trim()
  queryResults.value = result.features
    .map((f) => ({ name: resolveNameField(f.attributes || {}).value || '未命名', graphic: f }))
    .filter((item) => !keyword || item.name.includes(keyword))
    .slice(0, 50)
  if (!queryResults.value.length) ElMessage.info('未查询到匹配要素')
}

const locateFeature = async (row) => {
  if (!row.graphic?.geometry || !view) return
  await view.goTo({ target: row.graphic.geometry, zoom: 12 })
  const { value } = resolveNameField(row.graphic.attributes || {})
  if (queryLayerKey.value === 'reservoirs') {
    selectedReservoir.value = normalizeReservoirName(value)
    imgError.value = false
    showFloodHistoryPanel.value = false
  }
}

const closeReservoirPanel = () => {
  selectedReservoir.value = ''
  showFloodHistoryPanel.value = false
}
</script>
