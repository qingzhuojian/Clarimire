import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    theme: 'dark'
  }),

  actions: {
    initTheme() {
      try {
        const saved = uni.getStorageSync('envInspectionTheme')
        if (saved) {
          this.theme = saved
        }
        this.applyTheme(this.theme)
      } catch (e) {
        this.applyTheme(this.theme)
      }
    },

    toggleTheme() {
      const next = this.theme === 'dark' ? 'light' : 'dark'
      this.theme = next
      try {
        uni.setStorageSync('envInspectionTheme', next)
      } catch (e) {}
      this.applyTheme(next)
    },

    applyTheme(theme) {
      // 更新 HTML 属性
      if (theme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light')
      } else {
        document.documentElement.removeAttribute('data-theme')
      }
    }
  }
})
