import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth.ts'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: {
      title: '统一登录 · 港口引航及计费系统',
    },
  },
  {
    path: '/console',
    name: 'SystemConsole',
    component: () => import('@/views/SystemConsole.vue'),
    meta: {
      title: '系统总台 · 港口引航及计费系统',
      requiresAuth: true,
    },
  },
  {
    path: '/platform/integrated',
    component: () => import('@/layouts/LargeScreenLayout.vue'),
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: '',
        name: 'OverviewMain',
        component: () => import('@/views/PortPilotageDashboard.vue'),
        meta: {
          title: '综合主屏 · 港口引航及计费系统',
          view: 'main',
          requiresAuth: true,
        },
      },
      {
        path: 'aux',
        name: 'OverviewAux',
        component: () => import('@/views/PortPilotageDashboard.vue'),
        meta: {
          title: '辅助监控 · 港口引航及计费系统',
          view: 'aux',
          requiresAuth: true,
        },
      },
      {
        path: 'traffic',
        name: 'TrafficAnalysis',
        component: () => import('@/views/screens/TrafficAnalysis.vue'),
        meta: {
          title: '船舶分析 · 港口引航及计费系统',
          requiresAuth: true,
        },
      },
      {
        path: 'operations',
        name: 'OperationsCenter',
        component: () => import('@/views/screens/OperationsCenter.vue'),
        meta: {
          title: '调度运行 · 港口引航及计费系统',
          requiresAuth: true,
        },
      },
      {
        path: 'billing',
        name: 'BillingCenter',
        component: () => import('@/views/screens/BillingCenter.vue'),
        meta: {
          title: '计费中心 · 港口引航及计费系统',
          requiresAuth: true,
        },
      },
      {
        path: 'safety',
        name: 'SafetyCenter',
        component: () => import('@/views/screens/SafetyCenter.vue'),
        meta: {
          title: '安全监测 · 港口引航及计费系统',
          requiresAuth: true,
        },
      },
      {
        path: 'weather',
        name: 'WeatherCenter',
        component: () => import('@/views/screens/WeatherCenter.vue'),
        meta: {
          title: '气象中心 · 港口引航及计费系统',
          requiresAuth: true,
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  auth.hydrate()

  if (to.meta?.requiresAuth && !auth.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.name === 'Login' && auth.isAuthenticated) {
    const redirect = (to.query.redirect as string | undefined) ?? '/console'
    next({ path: redirect })
    return
  }

  next()
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title)
  }
})

export default router