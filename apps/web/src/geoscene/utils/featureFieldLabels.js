/** 跳过：系统字段 + 对业务无意义的技术编号 */
const SKIP_FIELDS = new Set([
  'objectid',
  'fid',
  'oid',
  'shape',
  'geometry',
  'globalid',
  'gdb_geomattr_data',
  'override',
  '__selectkey',
  // 开放地图内部编号、原始要素号等，对展示无意义
  'osmid',
  'osm_id',
  'osm id',
  'orig_fid',
  'origfid',
  'orig fid',
  // 拼音一般非必要展示
  'pyname',
  'pinyin',
  'ename'
])

/** 运行时从图层 fields.alias 注入的中文别名 */
const RUNTIME_ALIASES = Object.create(null)

/** 字段名归一化：去空格下划线、小写，便于匹配 osm id / OSM_ID */
function normKey(key) {
  return String(key || '')
    .toLowerCase()
    .replace(/[\s_\-]+/g, '')
}

/**
 * 英文字段 → 中文名
 */
const FIELD_LABELS = {
  name: '名称',
  名称: '名称',
  库名: '库名',
  fname: '名称',
  cname: '中文名',
  code: '编码',
  type: '类型',
  kind: '类型代码',
  level: '等级',
  grade: '等级',
  class: '类别',
  fclass: '要素分类',
  region: '所属地区',
  district: '行政区',
  admincode: '行政区划代码',
  gnid: '政区代码',
  xzname: '乡镇名称',
  area: '面积',
  面积: '面积',
  length: '长度',
  长度: '长度',
  perimeter: '周长',
  depth: '水深',
  水深: '水深',
  capacity: '库容',
  库容: '库容',
  volume: '库容',
  river: '所属河流',
  basin: '流域',
  system: '水系',
  year: '年份',
  month: '月份',
  date: '日期',
  remark: '备注',
  note: '备注',
  address: '地址',
  population: '人口',
  x: '经度',
  y: '纬度',
  lon: '经度',
  lat: '纬度',
  longitude: '经度',
  latitude: '纬度',
  shapearea: '面积',
  shapelength: '长度',
  shapeleng: '长度',
  reservoirname: '水库名称',
  monitorpointname: '监测点名称',
  ammonianitrogen: '氨氮',
  cod: '化学需氧量',
  totalphosphorus: '总磷',
  dissolvedoxygen: '溶解氧',
  ph: '酸碱度',
  turbidity: '浊度',
  temperature: '水温',
  waterlevel: '水位'
}

/** 英文取值 → 中文 */
const VALUE_LABELS = {
  reservoir: '水库',
  river: '河流',
  riverbank: '河岸',
  stream: '溪流',
  water: '水体',
  lake: '湖泊',
  canal: '运河',
  drain: '沟渠',
  ditch: '水渠',
  wetland: '湿地',
  dock: '码头',
  basin: '蓄水池',
  pond: '池塘',
  dam: '大坝',
  weir: '堰',
  waterfall: '瀑布',
  coastline: '海岸线',
  administrative: '行政区',
  residential: '居民点',
  village: '村庄',
  town: '镇',
  city: '城市',
  county: '县',
  ai: '居民点',
  a1: '一级',
  a2: '二级',
  a3: '三级'
}

/** 字段单位 */
const FIELD_UNITS = {
  ammonianitrogen: 'mg/L',
  ammonia_nitrogen: 'mg/L',
  cod: 'mg/L',
  totalphosphorus: 'mg/L',
  dissolvedoxygen: 'mg/L',
  turbidity: 'NTU',
  temperature: '℃',
  waterlevel: 'm',
  depth: 'm',
  水深: 'm',
  year: '年',
  month: '月'
}

/**
 * @param {string} key
 */
function shouldSkipField(key) {
  if (!key) return true
  const lower = String(key).toLowerCase()
  const compact = normKey(key)
  if (SKIP_FIELDS.has(lower) || SKIP_FIELDS.has(compact)) return true
  if (compact === 'shape' || compact === 'geometry') return true
  return false
}

/**
 * 从已加载的 FeatureLayer 收集中文别名
 * @param {import('@geoscene/core/layers/FeatureLayer').default|null} layer
 */
export function registerLayerFieldAliases(layer) {
  const fields = layer?.fields
  if (!Array.isArray(fields)) return
  for (const f of fields) {
    if (!f?.name) continue
    const alias = f.alias
    if (alias && /[\u4e00-\u9fff]/.test(String(alias))) {
      RUNTIME_ALIASES[f.name] = String(alias)
      RUNTIME_ALIASES[normKey(f.name)] = String(alias)
    }
  }
}

/**
 * @param {import('@geoscene/core/layers/Layer').default|null} layer
 * @param {string} fieldName
 */
export function getFieldChineseLabel(layer, fieldName) {
  const compact = normKey(fieldName)
  if (RUNTIME_ALIASES[fieldName]) return RUNTIME_ALIASES[fieldName]
  if (RUNTIME_ALIASES[compact]) return RUNTIME_ALIASES[compact]
  if (FIELD_LABELS[compact]) return FIELD_LABELS[compact]
  if (FIELD_LABELS[fieldName]) return FIELD_LABELS[fieldName]

  const fields = layer?.fields
  if (Array.isArray(fields)) {
    const meta = fields.find((f) => f.name === fieldName || normKey(f.name) === compact)
    const alias = meta?.alias
    if (alias && /[\u4e00-\u9fff]/.test(alias)) return alias
    if (alias && FIELD_LABELS[normKey(alias)]) return FIELD_LABELS[normKey(alias)]
  }

  if (/[\u4e00-\u9fff]/.test(fieldName)) return fieldName
  return '其他属性'
}

/**
 * @param {*} raw
 */
function translateValue(raw) {
  if (raw == null || raw === '') return '—'
  const text = String(raw).trim()
  if (!text) return '—'
  const mapped = VALUE_LABELS[text.toLowerCase()]
  if (mapped) return mapped
  return text
}

/**
 * @param {string} fieldName
 * @param {*} raw
 */
export function formatFieldValue(fieldName, raw) {
  if (raw == null || raw === '') return '—'

  const compact = normKey(fieldName)
  const lower = String(fieldName).toLowerCase()

  // 分类/类型类字段：优先翻译英文取值
  if (
    compact === 'fclass' ||
    compact === 'class' ||
    compact === 'type' ||
    compact === 'kind' ||
    compact === '类别' ||
    compact === '类型'
  ) {
    return translateValue(raw)
  }

  // 面积：投影坐标系多为平方米
  if (compact.includes('area') || fieldName === '面积') {
    const n = Number(raw)
    if (Number.isFinite(n)) {
      if (Math.abs(n) >= 1e6) return `${(n / 1e6).toFixed(2)} 平方公里`
      if (Math.abs(n) >= 1e4) return `${(n / 1e4).toFixed(2)} 公顷`
      return `${n.toFixed(0)} 平方米`
    }
  }

  // 长度/周长：多为米
  if (
    compact.includes('length') ||
    compact.includes('leng') ||
    compact.includes('perimeter') ||
    fieldName === '长度' ||
    fieldName === '周长'
  ) {
    const n = Number(raw)
    if (Number.isFinite(n)) {
      if (Math.abs(n) >= 1000) return `${(n / 1000).toFixed(2)} 公里`
      return `${n.toFixed(1)} 米`
    }
  }

  // 库容
  if (compact.includes('capacity') || compact.includes('volume') || fieldName === '库容') {
    const n = Number(raw)
    if (Number.isFinite(n)) {
      if (Math.abs(n) >= 1e8) return `${(n / 1e8).toFixed(2)} 亿立方米`
      if (Math.abs(n) >= 1e4) return `${(n / 1e4).toFixed(2)} 万立方米`
      return `${n} 立方米`
    }
    return translateValue(raw)
  }

  const unit = FIELD_UNITS[compact] || FIELD_UNITS[lower]
  if (typeof raw === 'number' && Number.isFinite(raw)) {
    const num = Number.isInteger(raw) ? String(raw) : String(Math.round(raw * 1000) / 1000)
    return unit ? `${num} ${unit}` : num
  }

  const text = String(raw)
  if (unit && !/mg\/L|立方米|公里|米|℃|NTU|公顷|平方公里/.test(text)) {
    const n = Number(text)
    if (Number.isFinite(n)) {
      return `${Number.isInteger(n) ? n : Math.round(n * 1000) / 1000} ${unit}`
    }
  }

  // 纯英文单词尝试翻译；否则原样（数字编码等保留）
  if (/^[A-Za-z][A-Za-z0-9_\-]*$/.test(text)) {
    return translateValue(text)
  }
  return text
}

/**
 * 将要素属性转为中文标签 + 带单位数值列表
 * @param {import('@geoscene/core/Graphic').default} graphic
 * @returns {Array<{ label: string, value: string }>}
 */
export function buildIdentifyFields(graphic) {
  const attrs = graphic?.attributes || {}
  const layer = graphic?.layer || null
  const rows = []
  const seenLabels = new Set()

  for (const [key, raw] of Object.entries(attrs)) {
    if (shouldSkipField(key)) continue
    if (raw == null || raw === '') continue
    const label = getFieldChineseLabel(layer, key)
    const value = formatFieldValue(key, raw)
    if (value === '—') continue
    // 同名中文标签去重（保留先出现的）
    if (seenLabels.has(label)) continue
    seenLabels.add(label)
    rows.push({ label, value })
  }

  if (layer?.title) {
    rows.unshift({ label: '所属图层', value: layer.title })
  }

  return rows
}

/**
 * 从属性对象列出可用于标注的字段（中文 label + 真实字段名）
 * @param {Record<string, *>} attrs
 * @param {import('@geoscene/core/layers/Layer').default|null} [layer]
 * @returns {Array<{ value: string, label: string }>}
 */
export function listAnnotatableFields(attrs = {}, layer = null) {
  const rows = []
  const seenLabels = new Set()
  for (const key of Object.keys(attrs)) {
    if (shouldSkipField(key)) continue
    const label = getFieldChineseLabel(layer, key)
    // 无法给出有意义中文名的字段不进入标注下拉
    if (label === '其他属性') continue
    if (seenLabels.has(label)) continue
    seenLabels.add(label)
    rows.push({ value: key, label })
  }
  rows.sort((a, b) => {
    const score = (item) => {
      const n = normKey(item.value)
      if (n === 'name' || item.label === '名称') return 0
      if (item.label.includes('名称') || item.label.includes('库名')) return 1
      return 2
    }
    return score(a) - score(b)
  })
  return rows
}

/**
 * 是否应跳过该字段（供标注/识别共用）
 * @param {string} key
 */
export function isSkippableAttributeField(key) {
  return shouldSkipField(key)
}
