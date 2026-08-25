import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  // FormData 交由浏览器自动带 boundary
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

request.interceptors.response.use(res => {
  const data = res.data
  if (data.code && data.code !== 200) {
    return Promise.reject(new Error(data.message || '请求失败'))
  }
  return data
}, err => Promise.reject(err))

export default request
