import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页地图', mapPage: true }
      },
      {
        path: '/map',
        redirect: '/map-editor'
      },
      {
        path: '/data-management',
        name: 'DataManagement',
        component: () => import('../views/dataManagement/Index.vue'),
        meta: { title: '数据管理', fullPage: true }
      },
      {
        path: '/water-situation',
        redirect: { path: '/data-management', query: { tab: 'waterSituation' } }
      },
      {
        path: '/section-monitor',
        redirect: { path: '/data-management', query: { tab: 'sectionMonitor' } }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/Index.vue'),
        meta: { title: '统计分析', fullPage: true }
      },
      {
        path: '/statistics-analysis',
        redirect: '/statistics'
      },
      {
        path: '/simulation',
        name: 'Simulation',
        component: () => import('../views/Simulation.vue'),
        meta: { title: '污染扩散模拟', mapPage: true }
      },
      {
        path: '/warning',
        name: 'Warning',
        component: () => import('../views/warning/WarningAnalysis.vue'),
        meta: { title: '预警分析', mapPage: true }
      },
      {
        path: '/map-editor',
        name: 'MapEditor',
        component: () => import('../views/mapEditor/Index.vue'),
        meta: { title: '地图编辑', mapPage: true }
      },
      {
        path: '/ops',
        name: 'Ops',
        component: () => import('../views/ops/Index.vue'),
        meta: { title: '运维调度', fullPage: true }
      },
      {
        path: '/ops/tasks',
        redirect: { path: '/ops', query: { tab: 'tasks' } }
      },
      {
        path: '/ops/patrols',
        redirect: { path: '/ops', query: { tab: 'patrols' } }
      },
      {
        path: '/ops/issues',
        redirect: { path: '/ops', query: { tab: 'issues' } }
      },
      {
        path: '/system',
        name: 'System',
        component: () => import('../views/system/Index.vue'),
        meta: { title: '系统管理', fullPage: true }
      },
      {
        path: '/system/users',
        redirect: { path: '/system', query: { tab: 'users' } }
      },
      {
        path: '/system/checkin-policy',
        redirect: { path: '/system', query: { tab: 'checkin' } }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
