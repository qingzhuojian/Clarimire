/**
 * GeoScene 服务配置 - GeoScene 6.1
 * 图层索引与已发布服务 Beijing_Water_System 实际结构一致
 */
const GEOSCENE_BASE_URL = '/geoscene'
const REST_BASE_URL = `${GEOSCENE_BASE_URL}/rest/services`
const SERVICE_NAME = 'Beijing_Water_System'

export default {
  baseURL: GEOSCENE_BASE_URL,
  restBase: REST_BASE_URL,
  adminBase: `${GEOSCENE_BASE_URL}/admin`,

  services: {
    mapServer: `${REST_BASE_URL}/${SERVICE_NAME}/MapServer`,
    featureServer: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer`,

    /**
     * 各图层 FeatureServer URL（索引来自 Server 实际发布结果）
     * 0-居民地地名 1-水系线 2-水库 3-水系面 4-区县界
     */
    layers: {
      settlements: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer/0`,
      waterLines: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer/1`,
      reservoirs: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer/2`,
      waterAreas: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer/3`,
      districts: `${REST_BASE_URL}/${SERVICE_NAME}/FeatureServer/4`
    }
  },

  /** 图层叠放顺序（自下而上） */
  layerOrder: ['waterAreas', 'reservoirs', 'waterLines', 'settlements', 'districts'],

  /** 图层控制面板展示顺序 */
  layerPanelOrder: ['districts', 'waterAreas', 'reservoirs', 'waterLines', 'settlements'],

  /** 图层元数据，供 Map.vue 图层控制面板使用 */
  layerMeta: {
    districts: { id: 4, name: '行政区划', type: 'polygon', defaultVisible: true, sourceCrs: 'wgs84' },
    waterAreas: { id: 3, name: '河流', type: 'polygon', defaultVisible: true, sourceCrs: 'wgs84' },
    reservoirs: { id: 2, name: '水库', type: 'polygon', defaultVisible: true, sourceCrs: 'wgs84' },
    waterLines: { id: 1, name: '河流', type: 'line', defaultVisible: true, sourceCrs: 'wgs84' },
    settlements: { id: 0, name: '居民点', type: 'point', defaultVisible: false, sourceCrs: 'cgcs2000' }
  },

  webAdaptor: {
    restUrl: REST_BASE_URL,
    managerUrl: `${GEOSCENE_BASE_URL}/manager`,
    adminUrl: `${GEOSCENE_BASE_URL}/admin`
  },

  map: {
    center: [116.4074, 39.9042],
    zoom: 10,
    /**
     * tianditu | tianditu-satellite | gaode | gaode-satellite | osm | none
     * 推荐天地图：与 WGS84/CGCS2000 业务图层对齐，无需 GCJ-02
     */
    basemap: 'tianditu',
    /**
     * 仅高德底图需要 true；天地图/OSM/无底图必须为 false
     */
    useGcj02Offset: false,
    /** 也可写在 .env：VITE_TIANDITU_TOKEN=... */
    tiandituToken: ''
  },

  symbols: {
    settlement: {
      type: 'simple-marker',
      color: '#8B4513',
      size: 6,
      outline: { color: '#ffffff', width: 1 }
    },
    reservoir: {
      type: 'simple-fill',
      color: [0, 100, 200, 0.6],
      outline: { color: [0, 50, 150, 1], width: 1 }
    },
    waterArea: {
      type: 'simple-fill',
      color: [100, 180, 255, 0.5],
      outline: { color: [50, 150, 255, 1], width: 1 }
    },
    waterLine: {
      type: 'simple-line',
      color: [0, 120, 255, 0.8],
      width: 2
    },
    district: {
      type: 'simple-fill',
      color: [200, 200, 200, 0.3],
      outline: { color: '#666666', width: 1 }
    },
    /** 前端计算的监测点（非 Server 图层） */
    monitoring: {
      type: 'simple-marker',
      color: '#FF5722',
      size: 10,
      outline: { color: '#ffffff', width: 1.5 }
    }
  },

  query: {
    outFields: '*',
    outSR: 4326,
    f: 'json'
  }
}
