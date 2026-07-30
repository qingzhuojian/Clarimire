import { createSSRApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import './uni.scss'
import './styles/common.scss'

const pinia = createPinia()

export function createApp() {
  const app = createSSRApp(App)
  app.use(pinia)
  return {
    app
  }
}
