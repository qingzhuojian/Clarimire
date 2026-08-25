import request from '../utils/request'

export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  userinfo: () => request.get('/auth/userinfo')
}

export const patrolAPI = {
  getTasks: (params) => request.get('/patrol/tasks', { params }),
  getTask: (id) => request.get(`/patrol/tasks/${id}`),
  updateTask: (data) => request.put('/patrol/tasks', data),
  completeTask: (id) => request.post(`/patrol/tasks/${id}/complete`),
  ensureDaily: (assigneeId) => request.post('/patrol/tasks/ensure-daily', { assigneeId }),
  getRecords: (params) => request.get('/patrol/records', { params }),
  checkin: (data) => request.post('/patrol/checkin', data),
  getDashboard: () => request.get('/patrol/dashboard')
}

export const issueAPI = {
  list: (params) => request.get('/issues/list', { params }),
  getById: (id) => request.get(`/issues/${id}`),
  create: (data) => request.post('/issues/create', data)
}

export const systemAPI = {
  getReservoirLocations: () => request.get('/system/reservoir-locations')
}

export const uploadAPI = {
  photo: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    // 不要手动设 Content-Type，否则缺少 boundary，后端会 500
    return request.post('/upload/photo', fd)
  }
}
