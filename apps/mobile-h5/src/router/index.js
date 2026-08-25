import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },

  { path: '/inspector', component: () => import('../views/inspector/Home.vue') },
  { path: '/inspector/checkin', component: () => import('../views/inspector/Checkin.vue') },
  { path: '/inspector/tasks', component: () => import('../views/inspector/Tasks.vue') },
  { path: '/inspector/tasks/:id/result', component: () => import('../views/inspector/TaskResult.vue') },
  { path: '/inspector/tasks/:id/followup', component: () => import('../views/inspector/TaskFollowup.vue') },
  { path: '/inspector/tasks/:id/report', component: () => import('../views/inspector/TaskReport.vue') },
  { path: '/inspector/tasks/:id', component: () => import('../views/inspector/TaskDetail.vue') },
  { path: '/inspector/records', component: () => import('../views/inspector/Records.vue') },
  { path: '/inspector/records/:id', component: () => import('../views/inspector/RecordDetail.vue') },
  { path: '/inspector/emergency', component: () => import('../views/inspector/Emergency.vue') },
  { path: '/inspector/mine', redirect: '/inspector' },

  { path: '/public', component: () => import('../views/public/Home.vue') },
  { path: '/public/report', component: () => import('../views/public/Report.vue') },
  { path: '/public/my', redirect: '/public' },
  { path: '/public/mine', redirect: '/public' },
  { path: '/public/reports', component: () => import('../views/public/MyReports.vue') },
  { path: '/public/reports/:id', component: () => import('../views/public/ReportDetail.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login' || to.path === '/register') return next()
  if (!localStorage.getItem('token')) return next('/login')
  next()
})

export default router
