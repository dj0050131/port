import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { 
  fetchDashboardOverview,
  fetchThroughputStat,
  fetchResourceUsageStat,
  fetchAlertStat,
  fetchAlertList
} from '@/api/dashboard'
import type {
  DashboardData,
  DashboardMetric,
  TrendPoint,
  OperationSummary,
  AlertMessage,
  ResourceUsageItem,
  ThroughputStatVO,
  ResourceUsageStatVO,
  AlertStatVO
} from '@/types/dashboard'

export const useDashboardStore = defineStore('dashboard-overview', () => {
  const data = ref<DashboardData | null>(null)
  const loading = ref(false)
  const lastUpdated = ref<string>('')

  const fetchOverview = async () => {
    loading.value = true
    try {
      // Fetch all dashboard data in parallel
      const [overviewResult, throughputResult, resourceResult, alertResult, alertListResult] = await Promise.all([
        fetchDashboardOverview(),
        fetchThroughputStat(),
        fetchResourceUsageStat(),
        fetchAlertStat(),
        fetchAlertList()
      ])

      const processedData: DashboardData = {
        metrics: [],
        operations: {
          inboundShips: 0,
          outboundShips: 0,
          pilotMissions: 0,
          revenue: 0,
          utilization: 0,
          alerts: 0
        },
        alerts: [],
        throughputTrend: [],
        pilotageTrend: [],
        feeTrend: [],
        resourceUsage: []
      }

      if (overviewResult.code === 200 && overviewResult.data) {
        const { pilotage, financial } = overviewResult.data
        processedData.metrics = [
          {
            id: 'active-pilots',
            label: '活跃引航员',
            value: pilotage.activePilots,
            unit: '人',
            color: '#006994'
          },
          {
            id: 'today-revenue',
            label: '今日营收',
            value: financial.todayRevenue,
            unit: '元',
            color: '#00a8cc'
          },
          {
            id: 'pending-requests',
            label: '待审申请',
            value: pilotage.pendingRequests,
            unit: '条',
            color: '#faad14'
          },
          {
            id: 'today-plans',
            label: '今日计划',
            value: pilotage.todayPlans,
            unit: '次',
            color: '#52c41a'
          }
        ]

        processedData.operations.pilotMissions = pilotage.totalRequests
        processedData.operations.revenue = financial.totalRevenue
        processedData.pilotageTrend = pilotage.trends || []
        processedData.feeTrend = financial.trends || []
      } else {
        console.error('[dashboard] fetchDashboardOverview error', overviewResult)
      }

      if (throughputResult.code === 200 && throughputResult.data) {
        const throughputData: ThroughputStatVO = throughputResult.data
        processedData.operations.inboundShips = throughputData.inboundShips
        processedData.operations.outboundShips = throughputData.outboundShips
        processedData.throughputTrend = throughputData.trends || []
      } else {
        console.error('[dashboard] fetchThroughputStat error', throughputResult)
      }

      if (resourceResult.code === 200 && resourceResult.data) {
        const resourceData: ResourceUsageStatVO = resourceResult.data
        processedData.operations.utilization = calculateAverageUtilization(resourceData)

        // Convert resource usage data to ResourceUsageItem format
        processedData.resourceUsage = [
          { name: '引航艇', value: resourceData.pilotBoats },
          { name: '拖轮', value: resourceData.tugboats },
          { name: '引航员', value: resourceData.pilots },
          { name: '车辆', value: resourceData.vehicles }
        ]
      } else {
        console.error('[dashboard] fetchResourceUsageStat error', resourceResult)
      }

      if (alertResult.code === 200 && alertResult.data) {
        const alertData: AlertStatVO = alertResult.data
        processedData.operations.alerts = alertData.totalCount
      } else {
        console.error('[dashboard] fetchAlertStat error', alertResult)
      }

      if (alertListResult.code === 200 && alertListResult.data) {
        const list = alertListResult.data || []
        processedData.alerts = list.map((item: any, index: number) => ({
          id: item.id || `alert-${index}`,
          type: 'safety',
          level: mapAlertLevel(item.level),
          title: item.alertTypeName || mapAlertType(item.alertType || item.type || item.code),
          content: item.detail || item.message || '',
          timestamp: item.timestamp || ''
        }))
      } else {
        console.error('[dashboard] fetchAlertList error', alertListResult)
      }

      data.value = processedData
      lastUpdated.value = new Date().toLocaleString()
    } catch (error) {
      console.error('[dashboard] fetchOverview error', error)
      ElMessage.error('仪表板数据加载失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  const calculateAverageUtilization = (resourceUsage: ResourceUsageStatVO): number => {
    const resources = [resourceUsage.pilotBoats, resourceUsage.tugboats, resourceUsage.pilots, resourceUsage.vehicles]
    const sum = resources.reduce((acc, val) => acc + val, 0)
    return resources.length > 0 ? sum / resources.length : 0
  }

  const metrics = computed<DashboardMetric[]>(() => data.value?.metrics ?? [])
  const operations = computed<OperationSummary>(() => data.value?.operations ?? {
    inboundShips: 0,
    outboundShips: 0,
    pilotMissions: 0,
    revenue: 0,
    utilization: 0,
    alerts: 0
  })
  const alerts = computed<AlertMessage[]>(() => data.value?.alerts ?? [])
  const throughputTrend = computed<TrendPoint[]>(() => data.value?.throughputTrend ?? [])
  const pilotageTrend = computed<TrendPoint[]>(() => data.value?.pilotageTrend ?? [])
  const feeTrend = computed<TrendPoint[]>(() => data.value?.feeTrend ?? [])
  const resourceUsage = computed<ResourceUsageItem[]>(() => data.value?.resourceUsage ?? [])

  return {
    data,
    loading,
    lastUpdated,
    metrics,
    operations,
    alerts,
    throughputTrend,
    pilotageTrend,
    feeTrend,
    resourceUsage,
    fetchOverview,
  }
})

const mapAlertLevel = (level?: string): AlertMessage['level'] => {
  if (!level) return 'info'
  const key = level.toUpperCase()
  if (key === 'CRITICAL' || key === 'ERROR') return 'critical'
  if (key === 'WARNING') return 'warning'
  return 'info'
}

const mapAlertType = (raw?: string): string => {
  if (!raw) return '告警事件'
  const key = String(raw).toUpperCase()
  const map: Record<string, string> = {
    VEHICLE_MAINTENANCE: '车辆维护告警',
    TUGBOAT_MAINTENANCE: '拖轮维护告警',
    PILOT_BOAT_MAINTENANCE: '引航艇维护告警',
    PILOT_MAINTENANCE: '引航员维护告警',
    BILL_OVERDUE: '账单逾期告警',
    BILL_WARNING: '费用预警',
    SYSTEM_FAULT: '系统故障告警',
    WEATHER_WARNING: '气象预警',
    SECURITY_ALERT: '安全告警',
  }
  if (map[key]) return map[key]
  return String(raw).replace(/_/g, ' ')
}