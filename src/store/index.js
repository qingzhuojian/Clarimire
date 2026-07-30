import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

export { useThemeStore } from './theme'
export { useUserStore } from './user'
export { useTabBarStore } from './tabBar'
