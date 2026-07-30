<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useThemeStore, useUserStore } from '@/store'

onLaunch(() => {
  console.log('App Launch')
  // 初始化主题
  const themeStore = useThemeStore()
  themeStore.initTheme()

  // 检查登录状态，未登录则跳转到登录页
  const userStore = useUserStore()
  userStore.initUser()
  if (!userStore.currentUser) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
})
</script>

<style>
@import './uni.scss';
@import './styles/common.scss';

/* 全局背景设置 */
page,
uni-page-body {
  background: var(--bg-screen) !important;
}

/* 白天模式 */
html[data-theme="light"] page,
html[data-theme="light"] uni-page-body,
html[data-theme="light"] .page-home,
html[data-theme="light"] .page-mine {
  background: #f8fafc !important;
}

/* 手机屏幕容器 */
uni-page-body {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-screen) !important;
  position: relative !important;
  overflow-x: hidden !important;
  overflow-y: auto !important;
  padding: 16rpx;
  box-sizing: border-box !important;
}

page {
  width: 100% !important;
  height: 100% !important;
  background: var(--bg-screen) !important;
  position: relative !important;
  flex: 1 !important;
  display: flex !important;
  flex-direction: column !important;
  overflow-x: hidden !important;
  overflow-y: auto !important;
  -webkit-overflow-scrolling: touch !important;
  box-sizing: border-box !important;
  padding: 16rpx;
}

/* 登录页面保持贴边 */
uni-page-body:has(.page-login),
uni-page-body.page-login,
page:has(.page-login),
page.page-login {
  padding: 0 !important;
}
</style>
