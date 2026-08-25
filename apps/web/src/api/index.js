import request from '../utils/request'
import apiConfig from '../config/api'

export const authAPI = {
  login: (data) => request.post(apiConfig.auth.login, data),
  register: (data) => request.post(apiConfig.auth.register, data),
  getUserInfo: () => request.get(apiConfig.auth.userinfo)
}

export const waterSituationAPI = {
  list: (params) => request.get(apiConfig.waterSituation.list, { params }),
  detail: (id) => request.get(`${apiConfig.waterSituation.detail}/${id}`),
  create: (data) => request.post(apiConfig.waterSituation.create, data),
  update: (data) => request.put(apiConfig.waterSituation.update, data),
  delete: (id) => request.delete(`${apiConfig.waterSituation.detail}/${id}`),
  deleteBatch: (ids) => request.delete(`${apiConfig.waterSituation.detail}/batch`, { data: ids }),
  import: (formData) => request.post(apiConfig.waterSituation.import, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  export: (ids) => request.post(apiConfig.waterSituation.export, ids, {
    responseType: 'blob'
  }),
  getReservoirs: () => request.get(apiConfig.waterSituation.reservoirs)
}

export const sectionMonitorAPI = {
  list: (params) => request.get(apiConfig.sectionMonitor.list, { params }),
  detail: (id) => request.get(`${apiConfig.sectionMonitor.detail}/${id}`),
  create: (data) => request.post(apiConfig.sectionMonitor.create, data),
  update: (data) => request.put(apiConfig.sectionMonitor.update, data),
  delete: (id) => request.delete(`${apiConfig.sectionMonitor.detail}/${id}`),
  deleteBatch: (ids) => request.delete(`${apiConfig.sectionMonitor.detail}/batch`, { data: ids }),
  import: (formData) => request.post(apiConfig.sectionMonitor.import, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  export: (ids) => request.post(apiConfig.sectionMonitor.export, ids, { responseType: 'blob' }),
  getReservoirs: () => request.get(apiConfig.sectionMonitor.reservoirs),
  getYears: () => request.get(apiConfig.sectionMonitor.years)
}

export const warningAPI = {
  list: (params) => request.get(apiConfig.warning.list, { params }),
  getReservoirs: () => request.get(apiConfig.warning.reservoirs),
  evaluate: (data) => request.post(apiConfig.warning.evaluate, data),
  evaluateEnv: (data) => request.post(apiConfig.warning.evaluateEnv, data)
}

export const patrolAPI = {
  getTasks: (params) => request.get(apiConfig.patrol.tasks, { params }),
  getTask: (id) => request.get(`${apiConfig.patrol.taskDetail}/${id}`),
  createTask: (data) => request.post(apiConfig.patrol.tasks, data),
  updateTask: (data) => request.put(apiConfig.patrol.tasks, data),
  deleteTask: (id) => request.delete(`${apiConfig.patrol.taskDetail}/${id}`),
  getRecords: (params) => request.get(apiConfig.patrol.records, { params }),
  checkin: (data) => request.post(apiConfig.patrol.checkin, data),
  getDashboard: () => request.get(apiConfig.patrol.dashboard)
}

export const issueAPI = {
  list: (params) => request.get(apiConfig.issues.list, { params }),
  detail: (id) => request.get(`${apiConfig.issues.detail}/${id}`),
  create: (data) => request.post(apiConfig.issues.create, data),
  update: (data) => request.put(apiConfig.issues.update, data),
  review: (data) => request.post(apiConfig.issues.review, data)
}

export const systemAPI = {
  getUsers: (params) => request.get(apiConfig.system.users, { params }),
  createUser: (data) => request.post(apiConfig.system.users, data),
  updateUser: (data) => request.put(apiConfig.system.users, data),
  deleteUser: (id) => request.delete(`${apiConfig.system.users}/${id}`),
  getCheckinPolicy: () => request.get(apiConfig.system.checkinPolicy),
  updateCheckinPolicy: (data) => request.put(apiConfig.system.checkinPolicy, data),
  getReservoirLocations: () => request.get(apiConfig.system.reservoirLocations)
}
