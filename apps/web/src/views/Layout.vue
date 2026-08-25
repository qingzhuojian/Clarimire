<template>
  <el-container class="layout-container">
    <el-header class="top-header">
      <div class="brand">
        <span class="brand-icon">清</span>
        <div>
          <div class="brand-title">{{ SYSTEM_BRAND }}</div>
          <div class="brand-sub">{{ SYSTEM_SUBTITLE }}</div>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        class="top-menu"
        router
        :ellipsis="false"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/map-editor">地图编辑</el-menu-item>
        <el-menu-item index="/data-management">数据管理</el-menu-item>
        <el-menu-item index="/statistics">统计分析</el-menu-item>
        <el-menu-item index="/warning">预警分析</el-menu-item>
        <el-menu-item index="/simulation">污染模拟</el-menu-item>
        <el-menu-item index="/ops">运维调度</el-menu-item>
        <el-menu-item index="/system">系统管理</el-menu-item>
      </el-menu>

      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><UserFilled /></el-icon>
            {{ realName }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main :class="['main', { 'main--map': isMapPage || isFullPage }]">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { SYSTEM_BRAND, SYSTEM_SUBTITLE } from '@/config/brand'

const router = useRouter()
const route = useRoute()

const realName = computed(() => localStorage.getItem('realName') || 'Admin')
const activeMenu = computed(() => {
  if (route.path.startsWith('/ops')) return '/ops'
  if (route.path.startsWith('/system')) return '/system'
  return route.path
})
const isMapPage = computed(() => !!route.meta.mapPage)
const isFullPage = computed(() => !!route.meta.fullPage)

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  flex-direction: column;
}

.top-header {
  height: 56px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1677ff, #0958d9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.brand-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
  line-height: 1.2;
}

.brand-sub {
  font-size: 10px;
  color: #999;
  max-width: 220px;
  line-height: 1.25;
}

.top-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  overflow-x: auto;
  min-width: 0;
}

:deep(.top-menu .el-menu-item),
:deep(.top-menu .el-sub-menu__title) {
  height: 56px;
  line-height: 56px;
  color: #444;
  border-bottom: 2px solid transparent;
}

:deep(.top-menu .el-menu-item.is-active) {
  color: #1677ff;
  border-bottom-color: #1677ff;
  background: transparent;
}

:deep(.top-menu .el-menu-item:hover),
:deep(.top-menu .el-sub-menu__title:hover) {
  background: #f5f8ff;
  color: #1677ff;
}

.header-right {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}

.main {
  background: #f0f2f5;
  padding: 16px 20px;
  overflow: auto;
  height: calc(100vh - 56px);
}

.main--map {
  padding: 0;
  background: #fff;
}
</style>
