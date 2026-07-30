// tabBar Store - 动态 tabBar 配置管理
import { defineStore } from 'pinia'
import { tabBarStore as mockTabBarStore } from '@/utils/mockData'
import { getTabBarConfig, updateTabBarBadge as apiUpdateBadge } from '@/utils/api'

export const useTabBarStore = defineStore('tabBar', {
  state: () => ({
    // tabBar 配置列表
    tabList: [],
    // 配置是否已加载
    isLoaded: false,
    // 是否使用模拟数据
    useMockData: true,
    // API 基础地址
    apiBaseUrl: ''
  }),

  getters: {
    // 获取需要显示的 tab 列表
    visibleTabList: (state) => {
      return state.tabList.filter(item => item.isShow !== false)
    },
    
    // 根据 pagePath 获取 tab 索引
    getTabIndex: (state) => (pagePath) => {
      return state.tabList.findIndex(item => item.pagePath === pagePath)
    },

    // 获取某个 tab 的 badge
    getBadge: (state) => (id) => {
      const tab = state.tabList.find(item => item.id === id)
      return tab ? tab.badge : 0
    }
  },

  actions: {
    // 初始化 tabBar 配置
    async initTabBar() {
      if (this.isLoaded) {
        this.applyToNative()
        return
      }

      try {
        // 获取配置
        const data = this.useMockData 
          ? { list: mockTabBarStore.getConfig() }
          : await getTabBarConfig()
        
        this.tabList = data.list || []
        this.isLoaded = true
        
        // 应用到原生 tabBar
        this.applyToNative()
        
        // 更新 badge 显示
        this.updateAllBadges()
        
      } catch (error) {
        console.error('初始化 tabBar 配置失败:', error)
        // 失败时使用默认配置
        this.tabList = mockTabBarStore.getConfig()
        this.isLoaded = true
        this.applyToNative()
      }
    },

    // 应用配置到原生 tabBar
    applyToNative() {
      const visibleList = this.visibleTabList
      
      if (visibleList.length === 0) {
        return
      }

      // 设置 tabBar 项
      visibleList.forEach((item, index) => {
        uni.setTabBarItem({
          index,
          text: item.text,
          iconPath: item.iconPath,
          selectedIconPath: item.selectedIconPath,
          fail: (err) => {
            console.error(`设置 tabBar 第 ${index} 项失败:`, err)
          }
        })
      })
    },

    // 更新所有 badge 显示
    updateAllBadges() {
      const visibleList = this.visibleTabList
      
      visibleList.forEach((item, index) => {
        if (item.badge > 0) {
          uni.setTabBarBadge({
            index,
            text: String(item.badge),
            fail: (err) => {
              console.error(`设置 tabBar badge 失败:`, err)
            }
          })
        } else {
          uni.removeTabBarBadge({
            index,
            fail: () => {}
          })
        }
      })
    },

    // 更新单个 tab 的 badge
    async updateBadge(id, count) {
      const index = this.tabList.findIndex(item => item.id === id)
      if (index === -1) return

      // 更新本地状态
      this.tabList[index].badge = count
      
      // 更新原生 badge
      const visibleIndex = this.visibleTabList.findIndex(item => item.id === id)
      if (visibleIndex !== -1) {
        if (count > 0) {
          uni.setTabBarBadge({
            index: visibleIndex,
            text: String(count)
          })
        } else {
          uni.removeTabBarBadge({ index: visibleIndex })
        }
      }

      // 调用 API 更新后端
      if (!this.useMockData) {
        try {
          await apiUpdateBadge(id, count)
        } catch (error) {
          console.error('更新 badge 失败:', error)
        }
      }
    },

    // 增加 badge 数量
    async incrementBadge(id, count = 1) {
      const tab = this.tabList.find(item => item.id === id)
      if (tab) {
        await this.updateBadge(id, tab.badge + count)
      }
    },

    // 清除 badge
    async clearBadge(id) {
      await this.updateBadge(id, 0)
    },

    // 切换 tabBar 显示/隐藏
    async setTabVisible(id, isShow) {
      const index = this.tabList.findIndex(item => item.id === id)
      if (index === -1) return

      this.tabList[index].isShow = isShow
      
      // 重新应用配置
      await this.initTabBar()
    },

    // 重置配置
    async reset() {
      this.useMockData = true
      this.isLoaded = false
      await this.initTabBar()
    },

    // 从后端刷新配置
    async refreshFromServer() {
      this.useMockData = false
      this.isLoaded = false
      await this.initTabBar()
    }
  }
})

export default useTabBarStore
