/**
 * GeoScene REST API 调用
 */
import axios from 'axios'
import geosceneConfig from '../config/geoscene'

const geosceneRequest = axios.create({
  baseURL: geosceneConfig.restBase,
  timeout: 30000,
  params: {
    f: 'json'
  }
})

export const geosceneAPI = {
  queryFeatures: (layerUrl, params = {}) => {
    return geosceneRequest.get(`${layerUrl}/query`, {
      params: {
        where: params.where || '1=1',
        outFields: params.outFields || '*',
        outSR: geosceneConfig.query.outSR,
        ...params
      }
    })
  },

  getAllFeatures: (layerUrl) => {
    return geosceneAPI.queryFeatures(layerUrl, { where: '1=1' })
  },

  getById: (layerUrl, objectId) => {
    return geosceneAPI.queryFeatures(layerUrl, { objectIds: objectId })
  },

  queryByExtent: (layerUrl, extent) => {
    return geosceneAPI.queryFeatures(layerUrl, {
      geometry: JSON.stringify(extent),
      geometryType: 'esriGeometryEnvelope',
      spatialRel: 'esriSpatialRelIntersects'
    })
  },

  getServiceInfo: (layerUrl) => {
    return geosceneRequest.get(layerUrl)
  },

  getLayers: () => {
    return geosceneRequest.get(geosceneConfig.services.featureServer)
  },

  getMapServiceLayers: () => {
    return geosceneRequest.get(geosceneConfig.services.mapServer)
  }
}

export const settlementsAPI = {
  getAll: () => geosceneAPI.getAllFeatures(geosceneConfig.services.layers.settlements),
  getById: (id) => geosceneAPI.getById(geosceneConfig.services.layers.settlements, id),
  query: (params) => geosceneAPI.queryFeatures(geosceneConfig.services.layers.settlements, params)
}

export const reservoirAPI = {
  getAll: () => geosceneAPI.getAllFeatures(geosceneConfig.services.layers.reservoirs),
  getById: (id) => geosceneAPI.getById(geosceneConfig.services.layers.reservoirs, id),
  query: (params) => geosceneAPI.queryFeatures(geosceneConfig.services.layers.reservoirs, params)
}

export const hydrographyAPI = {
  getLine: () => geosceneAPI.getAllFeatures(geosceneConfig.services.layers.waterLines),
  getPolygon: () => geosceneAPI.getAllFeatures(geosceneConfig.services.layers.waterAreas)
}

export const boundaryAPI = {
  getAll: () => geosceneAPI.getAllFeatures(geosceneConfig.services.layers.districts)
}
