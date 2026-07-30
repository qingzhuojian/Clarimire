// 后端 API 配置
// 将 USE_MOCK_DATA 改为 false，并配置正确的后端地址，即可接入真实后端

const CONFIG = {
  // 是否使用模拟数据（开发阶段设为 true，上线前改为 false）
  USE_MOCK_DATA: false,

  // 后端接口基础地址（请根据实际部署地址修改）
  API_BASE_URL: 'http://localhost:8080',

  // 请求超时时间（毫秒）
  REQUEST_TIMEOUT: 15000,

  // 是否在控制台打印请求日志（开发阶段方便调试）
  ENABLE_LOG: true
}

export default CONFIG
