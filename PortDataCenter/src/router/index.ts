import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getToken } from '@/utils/storage'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 仪表板
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Overview.vue'),
        meta: { title: '工作台', icon: 'HomeFilled', group: 'dashboard' },
      },
      {
        path: 'dashboard/ai-assistant',
        name: 'AIAssistant',
        component: () => import('@/views/dashboard/AIAssistant.vue'),
        meta: { title: 'AI 问答', icon: 'ChatDotRound', group: 'dashboard' },
      },
      {
        path: 'my-tasks',
        name: 'MyTasks',
        component: () => import('@/views/dashboard/MyTasks.vue'),
        meta: { title: '我的任务', icon: 'List', group: 'myTasks', perm: 'DASHBOARD:MY_TASKS:VIEW' },
      },

      // 引航调度模块
      {
        path: 'pilotage/applications',
        name: 'PilotageApplications',
        component: () => import('@/views/pilotage/Applications.vue'),
        meta: { title: '引航申请', icon: 'DocumentAdd', group: 'pilotage', perm: 'PILOTAGE:APPLICATIONS:VIEW' },
      },
      {
        path: 'pilotage/forecast',
        name: 'PilotageForecast',
        component: () => import('@/views/pilotage/Forecast.vue'),
        meta: { title: '动态预报', icon: 'Bell', group: 'pilotage', perm: 'PILOTAGE:FORECAST:VIEW' },
      },
      {
        path: 'pilotage/plans',
        name: 'PilotagePlans',
        component: () => import('@/views/pilotage/Plans.vue'),
        meta: { title: '引航计划', icon: 'Calendar', group: 'pilotage', perm: 'PILOTAGE:PLANS:MANAGE' },
      },
      {
        path: 'pilotage/confirmation',
        name: 'PilotageConfirmation',
        component: () => import('@/views/pilotage/Confirmation.vue'),
        meta: { title: '计划确认', icon: 'CircleCheck', group: 'pilotage', perm: 'PILOTAGE:CONFIRMATION:MANAGE' },
      },
      {
        path: 'pilotage/dispatch',
        name: 'PilotageDispatch',
        component: () => import('@/views/pilotage/Dispatch.vue'),
        meta: { title: '引航调度', icon: 'Guide', group: 'pilotage', perm: 'PILOTAGE:DISPATCH:MANAGE' },
      },
      {
        path: 'pilotage/worksheet',
        name: 'PilotageWorksheet',
        component: () => import('@/views/pilotage/Worksheet.vue'),
        meta: { title: '工作表', icon: 'Tickets', group: 'pilotage', perm: 'PILOTAGE:WORKSHEET:VIEW' },
      },
      {
        path: 'pilotage/visa',
        name: 'PilotageVisa',
        component: () => import('@/views/pilotage/Visa.vue'),
        meta: { title: '签证单', icon: 'Document', group: 'pilotage', perm: 'PILOTAGE:VISA:VIEW' },
      },
      {
        path: 'pilotage/alerts',
        name: 'PilotageAlerts',
        component: () => import('@/views/pilotage/Alerts.vue'),
        meta: { title: '预警管理', icon: 'Warning', group: 'pilotage', perm: 'PILOTAGE:ALERTS:MANAGE' },
      },

      // 引航费收模块
      {
        path: 'billing/fiscal-year',
        name: 'FiscalYear',
        component: () => import('@/views/billing/FiscalYear.vue'),
        meta: { title: '会计年度管理', icon: 'Calendar', group: 'billing', perm: 'BILLING:FISCAL_YEAR:MANAGE' },
      },
      {
        path: 'billing/payer',
        name: 'PayerManagement',
        component: () => import('@/views/billing/Payer.vue'),
        meta: { title: '付款单位管理', icon: 'OfficeBuilding', group: 'billing', perm: 'BILLING:PAYER:MANAGE' },
      },
      {
        path: 'billing/parameters',
        name: 'BillingParameters',
        component: () => import('@/views/billing/Parameters.vue'),
        meta: { title: '费收参数设置', icon: 'Setting', group: 'billing', perm: 'BILLING:PARAMETERS:MANAGE' },
      },
      {
        path: 'billing/direct-route',
        name: 'DirectRoute',
        component: () => import('@/views/billing/DirectRoute.vue'),
        meta: { title: '直航区间管理', icon: 'Connection', group: 'billing', perm: 'BILLING:DIRECT_ROUTE:MANAGE' },
      },
      {
        path: 'billing/visa-list',
        name: 'BillingVisaList',
        component: () => import('@/views/billing/VisaList.vue'),
        meta: { title: '签证单', icon: 'Tickets', group: 'billing', perm: 'BILLING:VISA_LIST:VIEW' },
      },
      {
        path: 'billing/charging',
        name: 'VisaCharging',
        component: () => import('@/views/billing/Charging.vue'),
        meta: { title: '签证单计费', icon: 'Money', group: 'billing', perm: 'BILLING:CHARGING:MANAGE' },
      },
      {
        path: 'billing/audit',
        name: 'BillingAudit',
        component: () => import('@/views/billing/Audit.vue'),
        meta: { title: '计费签证审核', icon: 'CircleCheck', group: 'billing', perm: 'BILLING:AUDIT:MANAGE' },
      },
      {
        path: 'billing/confirmation',
        name: 'PaymentConfirmation',
        component: () => import('@/views/billing/PaymentConfirmation.vue'),
        meta: { title: '到款确认', icon: 'Checked', group: 'billing', perm: 'BILLING:PAYMENT_CONFIRMATION:MANAGE' },
      },
      {
        path: 'billing/transfer',
        name: 'FeeTransfer',
        component: () => import('@/views/billing/Transfer.vue'),
        meta: { title: '费用移交', icon: 'Switch', group: 'billing', perm: 'BILLING:TRANSFER:MANAGE' },
      },
      {
        path: 'billing/invoice-void',
        name: 'InvoiceVoid',
        component: () => import('@/views/billing/InvoiceVoid.vue'),
        meta: { title: '票据冲红/作废', icon: 'Delete', group: 'billing', perm: 'BILLING:INVOICE_VOID:MANAGE' },
      },
      {
        path: 'billing/payment-notice-failures',
        name: 'PaymentNoticeFailures',
        component: () => import('@/views/billing/PaymentNoticeFailures.vue'),
        meta: { title: '付款提示异常', icon: 'WarningFilled', group: 'billing', perm: 'BILLING:PAYMENT_NOTICE_FAILURES:VIEW' },
      },

      {
        path: 'statistics/billing/monthly',
        name: 'BillingMonthlyStats',
        component: () => import('@/views/statistics/BillingMonthly.vue'),
        meta: { title: '费收月报表统计', icon: 'DataLine', group: 'statistics', perm: 'STATISTICS:BILLING:VIEW' },
      },
      {
        path: 'statistics/billing/annual',
        name: 'BillingAnnualStats',
        component: () => import('@/views/statistics/BillingAnnual.vue'),
        meta: { title: '费收年报表统计', icon: 'DataLine', group: 'statistics', perm: 'STATISTICS:BILLING:VIEW' },
      },

      {
        path: 'statistics/assessment/safety-monthly',
        name: 'AssessmentSafetyMonthly',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '船务安全科航行(月份)', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/pilotage-monthly',
        name: 'AssessmentPilotageMonthly',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '船舶引航统计(月份)', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/pilotage-summary',
        name: 'AssessmentPilotageSummary',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '船舶引航统计(汇总)', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/length-gt250',
        name: 'AssessmentLengthGt250',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '船长统计(>250m)', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/holiday',
        name: 'AssessmentHoliday',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '节假日引航统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/weekend',
        name: 'AssessmentWeekend',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '双休日引航统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/night',
        name: 'AssessmentNight',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '夜班统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/meal',
        name: 'AssessmentMeal',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '餐补统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/special',
        name: 'AssessmentSpecial',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '特殊引航统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/record-break',
        name: 'AssessmentRecordBreak',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '破纪录统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/new-wharf',
        name: 'AssessmentNewWharf',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '新码头靠泊统计', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },
      {
        path: 'statistics/assessment/plan-change',
        name: 'AssessmentPlanChange',
        component: () => import('@/views/statistics/Assessment.vue'), // Placeholder
        meta: { title: '计划改变查询', icon: 'TrendCharts', group: 'assessment', perm: 'STATISTICS:ASSESSMENT:VIEW' },
      },

      {
        path: 'master/pilots',
        name: 'PilotMaster',
        component: () => import('@/views/master/Pilots.vue'),
        meta: { title: '引航员管理', icon: 'User', group: 'master', perm: 'BASE:PILOTS:MANAGE' },
      },
      // {
      //   path: 'master/car-teams',
      //   name: 'CarTeamMaster',
      // },
      {
        path: 'master/agents',
        name: 'AgentMaster',
        component: () => import('@/views/master/Agents.vue'),
        meta: { title: '代理管理', icon: 'OfficeBuilding', group: 'master', perm: 'BASE:AGENTS:MANAGE' },
      },
      // {
      //   path: 'master/tug-companies',
      //   name: 'TugCompanyMaster',
      // },
      {
        path: 'master/tug-companies',
        name: 'TugCompanyMaster',
        component: () => import('@/views/master/TugboatCompanies.vue'),
        meta: { title: '拖轮公司管理', icon: 'OfficeBuilding', group: 'master', perm: 'BASE:TUG_COMPANIES:MANAGE' },
      },
      {
        path: 'master/tugboats',
        name: 'TugboatMaster',
        component: () => import('@/views/master/Tugboats.vue'),
        meta: { title: '拖轮管理', icon: 'Van', group: 'master', perm: 'BASE:TUGBOATS:MANAGE' },
      },
      {
        path: 'master/ports',
        name: 'PortMaster',
        component: () => import('@/views/master/Ports.vue'),
        meta: { title: '港区管理', icon: 'Location', group: 'master', perm: 'BASE:PORTS:MANAGE' },
      },
      {
        path: 'master/berths',
        name: 'BerthMaster',
        component: () => import('@/views/master/Berths.vue'),
        meta: { title: '泊位管理', icon: 'LocationFilled', group: 'master', perm: 'BASE:BERTHS:MANAGE' },
      },
      {
        path: 'master/anchorages',
        name: 'AnchorageMaster',
        component: () => import('@/views/master/Anchorages.vue'),
        meta: { title: '锚地管理', icon: 'Place', group: 'master', perm: 'BASE:ANCHORAGES:MANAGE' },
      },
      // {
      //   path: 'master/boarding-points',
      //   name: 'BoardingPointMaster',
      // },
      // {
      //   path: 'master/channels',
      //   name: 'ChannelMaster',
      // },
      {
        path: 'master/drivers',
        name: 'DriverMaster',
        component: () => import('@/views/master/Drivers.vue'),
        meta: { title: '驾驶员管理', icon: 'User', group: 'master', perm: 'BASE:DRIVERS:MANAGE' },
      },
      {
        path: 'master/vehicles',
        name: 'VehicleMaster',
        component: () => import('@/views/master/Vehicles.vue'),
        meta: { title: '车辆管理', icon: 'Van', group: 'master', perm: 'BASE:VEHICLES:MANAGE' },
      },
      {
        path: 'master/pilotboats',
        name: 'PilotBoatMaster',
        component: () => import('@/views/master/PilotBoats.vue'),
        meta: { title: '引航艇管理', icon: 'Ship', group: 'master', perm: 'BASE:PILOT_BOATS:MANAGE' },
      },
      {
        path: 'master/vessels',
        name: 'VesselMaster',
        component: () => import('@/views/master/Vessels.vue'),
        meta: { title: '船舶管理', icon: 'Ship', group: 'master', perm: 'BASE:VESSELS:MANAGE' },
      },
      {
        path: 'master/wharves',
        name: 'WharfMaster',
        component: () => import('@/views/master/Wharves.vue'),
        meta: { title: '码头管理', icon: 'Location', group: 'master', perm: 'BASE:WHARVES:MANAGE' },
      },
      {
        path: 'master/rate-rule-types',
        name: 'RateRuleTypes',
        component: () => import('@/views/master/RateRuleTypes.vue'),
        meta: { title: '费率规则类型', icon: 'Coin', group: 'billing', perm: 'BASE:RATE_RULE_TYPES:MANAGE' },
      },
      {
        path: 'master/charge-items',
        name: 'ChargeItems',
        component: () => import('@/views/master/ChargeItems.vue'),
        meta: { title: '收费项目', icon: 'Coin', group: 'billing', perm: 'BASE:CHARGE_ITEMS:MANAGE' },
      },
      {
        path: 'billing/rate-conditions',
        name: 'RateConditions',
        component: () => import('@/views/billing/RateConditions.vue'),
        meta: { title: '费率条件配置', icon: 'Setting', group: 'billing', perm: 'BILLING:PARAMETERS:MANAGE' },
      },

      // 系统管理
      {
        path: 'bases/users',
        name: 'UserManagement',
        component: () => import('@/views/system/Users.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', group: 'system', perm: 'SYSTEM:USERS:MANAGE' },
      },
      {
        path: 'bases/settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/SystemParameters.vue'),
        meta: { title: '参数配置', icon: 'Setting', group: 'system', perm: 'SYSTEM:PARAMETERS:EDIT' },
      },
      {
        path: 'bases/history',
        name: 'UpdateHistory',
        component: () => import('@/views/system/UpdateHistory.vue'),
        meta: { title: '更新历史', icon: 'Timer', group: 'system', perm: 'SYSTEM:UPDATE_HISTORY:VIEW' },
      },
      {
        path: 'bases/auth-permissions',
        name: 'AuthPermissions',
        component: () => import('@/views/system/AuthPermissions.vue'),
        meta: { title: '权限码信息', icon: 'Lock', group: 'system', perm: 'SYSTEM:AUTH_PERMISSIONS:MANAGE' },
      },
      {
        path: 'bases/profile',
        name: 'UserProfile',
        component: () => import('@/views/system/Profile.vue'),
        meta: { title: '个人信息', icon: 'User', group: 'system', hidden: true }, // Hide from menu, access via avatar
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory('/port-data-center/'),
  routes,
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  const token = auth.token || getToken()
  if (to.meta.public) {
    if (token && to.path === '/login') {
      next('/dashboard')
      return
    }
    next()
    return
  }
  if (!token) {
    next({ path: '/login', replace: true })
    return
  }
  if (!auth.token && token) {
    await auth.restoreSession(token)
  }

  const requiredPerm = to.meta.perm as string | string[] | undefined
  const userPerms = auth.user?.permissions || []
  if (requiredPerm) {
    const required = Array.isArray(requiredPerm) ? requiredPerm : [requiredPerm]
    const has = userPerms.includes('*') || required.some((p) => userPerms.includes(p))
    if (!has) {
      next({ path: '/dashboard' })
      return
    }
  }

  const userRole = auth.user?.userType?.toLowerCase()
  const requiredRoles = to.meta.role as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    if (!userRole || !requiredRoles.includes(userRole)) {
      next({ path: '/dashboard' })
      return
    }
  }

  next()
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `港口引航计费数据中心 - ${to.meta.title as string}`
  }
})

export default router