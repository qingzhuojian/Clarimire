import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    userRole: null
  }),
  
  actions: {
    initUser() {
      // 每次启动时强制未登录，由用户主动登录
      this.currentUser = null
      this.userRole = null
    },
    
    login(role = 'inspector', name = '', backendUser = null) {
      let user
      if (backendUser) {
        // 使用后端返回的用户信息
        user = {
          role: backendUser.role || role,
          name: backendUser.realName || name || (role === 'inspector' ? '张明' : '群众用户'),
          dept: backendUser.dept || (role === 'inspector' ? '江北生态环境分局' : '普通用户'),
          avatar: backendUser.avatar || (role === 'inspector' ? '巡' : '群'),
          phone: backendUser.phone || '',
          username: backendUser.username || name,
          id: backendUser.id,
          certified: backendUser.certified !== false
        }
      } else {
        // 使用演示模式数据
        const isPublic = role === 'public'
        user = {
          role,
          name: name || (isPublic ? '演示群众' : '张明'),
          username: name || (isPublic ? 'public1' : 'inspector1'),
          dept: isPublic ? '普通用户' : '江北生态环境分局',
          avatar: isPublic ? '群' : '巡',
          certified: true
        }
      }
      this.currentUser = user
      this.userRole = role
      try {
        uni.setStorageSync('envInspectionUser', user)
      } catch (e) {}
      return user
    },
    
    logout() {
      this.currentUser = null
      this.userRole = null
      try {
        uni.removeStorageSync('envInspectionUser')
      } catch (e) {}
    }
  }
})
