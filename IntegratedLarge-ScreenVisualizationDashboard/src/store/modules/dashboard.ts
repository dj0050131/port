import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type {
  AlertItem,
  BerthStatus,
  FeeStatistics,
  OperationsOverview,
  PilotPlan,
  PortMapPayload,
  ShipDynamic,
  SystemStatusItem,
  TrafficOverview,
  WeatherTide,
    SystemPerformanceData,
} from '@/types/dashboard.ts'
import {
    getAlertList,
    getBerthStatus,
    getFeeStatistics,
    getOperationsOverview,
    getPilotPlans,
    getPortMapPayload,
    getShipDynamics,
    getSystemPerformanceData,
    getSystemStatus,
    getTrafficOverview,
    getWeatherAndTide,
} from '@/api/dashboard.ts'
import { useNow } from '@/utils/date-utils.ts'

export const useDashboardStore = defineStore('dashboard', () => {
  const shipDynamics = ref<ShipDynamic[]>([])
  const pilotPlans = ref<PilotPlan[]>([])
  const weather = ref<WeatherTide | null>(null)
  const feeStats = ref<FeeStatistics | null>(null)
  const alerts = ref<AlertItem[]>([])
  const systemStatus = ref<SystemStatusItem[]>([])
  const berths = ref<BerthStatus[]>([])
  const mapPayload = ref<PortMapPayload | null>(null)
  const trafficOverview = ref<TrafficOverview | null>(null)
  const operationsOverview = ref<OperationsOverview | null>(null)
  const loading = ref(false)
  const lastUpdated = ref<string>('')
  const performanceData = ref<SystemPerformanceData | null>(null)

  const now = useNow(1000)

  const fetchAll = async () => {
    if (loading.value) return
    loading.value = true
    try {
      const [
        shipRes,
        pilotRes,
        weatherRes,
        feeRes,
        alertRes,
        systemRes,
        berthRes,
        mapRes,
        trafficRes,
        operationsRes,
          performanceRes,
      ] = await Promise.all([
        getShipDynamics(),
        getPilotPlans(),
        getWeatherAndTide(),
        getFeeStatistics(),
        getAlertList(),
        getSystemStatus(),
        getBerthStatus(),
        getPortMapPayload(),
        getTrafficOverview(),
        getOperationsOverview(),
          getSystemPerformanceData(),
      ])

      shipDynamics.value = shipRes.data
      pilotPlans.value = pilotRes.data
      weather.value = weatherRes.data
      feeStats.value = feeRes.data
      alerts.value = alertRes.data
      systemStatus.value = systemRes.data
      berths.value = berthRes.data
      mapPayload.value = mapRes.data
      trafficOverview.value = trafficRes.data
      operationsOverview.value = operationsRes.data
      lastUpdated.value = mapRes.timestamp
        performanceData.value = performanceRes.data
    } catch (error) {
      console.error('[dashboard:fetchAll]', error)
      ElMessage.error('数据刷新失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  return {
    shipDynamics,
    pilotPlans,
    weather,
    feeStats,
    alerts,
    systemStatus,
    berths,
    mapPayload,
    trafficOverview,
    operationsOverview,
    loading,
    lastUpdated,
      performanceData,
      now,
    shipCount: computed(() => shipDynamics.value.length),
    activeAlerts: computed(() => alerts.value.filter((item) => item.level !== 'normal').length),
    fetchAll,
  }
})
