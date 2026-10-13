import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  fetchPilotageApplications,
  fetchPilotageDashboard,
  fetchPilotageTimeline,
} from '@/api/pilotage'
import type {
  // PilotDispatchOrder,
  PilotageApplication,
  PilotageDashboard,
  PilotagePlan,
  PilotageTimelineItem,
  PilotDispatchOrder,
} from '@/types/pilotage'
import type { PaginatedResult } from '@/types/common'
import {fetchPilotagePlans} from "@/api/pilotagePlan.ts";

interface ApplicationPagination {
  page: number
  pageSize: number
  total: number
}

export const usePilotageStore = defineStore('pilotage', () => {
  const dashboard = ref<PilotageDashboard | null>(null)
  const applications = ref<PilotageApplication[]>([])
  const plans = ref<PilotagePlan[]>([])
  const timeline = ref<PilotageTimelineItem[]>([])
  const pagination = ref<ApplicationPagination>({ page: 1, pageSize: 10, total: 0 })
  const loading = ref(false)

  const dispatchOrders = computed<PilotDispatchOrder[]>(() => {
    return plans.value.map(plan => ({
      id: plan.id,
      vessel: plan.shipName || 'Unknown Ship',
      status: plan.status,
      pilotName: plan.pilot || 'Unassigned',
      contact: '13800000000',
      departure: plan.time || '',
      arrival: '' // Mock arrival or calculate
    }))
  })

  const fetchDashboardData = async () => {
    try {
      const res = await fetchPilotageDashboard()
      if (res.code !== 200) throw new Error(res.message)
      dashboard.value = res.data
    } catch (error) {
      console.error('[pilotage] dashboard error', error)
      ElMessage.error('引航概览数据加载失败')
    }
  }

  const fetchApplications = async (page = pagination.value.page, filters: Record<string, any> = {}) => {
    loading.value = true
    try {
      const res = await fetchPilotageApplications({
        current: page,
        size: pagination.value.pageSize,
        ...filters,
      })
      if (res.code !== 200) throw new Error(res.message)
      const payload = res.data as PaginatedResult<PilotageApplication>
      pagination.value = {
        page: payload.current,
        pageSize: payload.size,
        total: payload.total,
      }
      applications.value = payload.records
    } catch (error) {
      console.error('[pilotage] applications error', error)
      ElMessage.error('引航申请数据加载失败')
    } finally {
      loading.value = false
    }
  }

  const fetchPlans = async () => {
    try {
      const res = await fetchPilotagePlans()
      if (res.code !== 200) throw new Error(res.message)
      plans.value = res.data
    } catch (error) {
      console.error('[pilotage] plans error', error)
      ElMessage.error('引航计划数据加载失败')
    }
  }

  const fetchTimeline = async () => {
    try {
      const res = await fetchPilotageTimeline()
      if (res.code !== 200) throw new Error(res.message)
      timeline.value = res.data
    } catch (error) {
      console.error('[pilotage] timeline error', error)
      ElMessage.error('引航时间轴加载失败')
    }
  }

  const fetchAll = async () => {
    await Promise.all([fetchDashboardData(), fetchApplications(1), fetchPlans(),  fetchTimeline()])
  }

  const statusStatistics = computed(() => {
    if (!dashboard.value) return []
    return [
      { label: '待审批', value: dashboard.value.pending, type: 'pending' },
      { label: '已排期', value: dashboard.value.scheduled, type: 'scheduled' },
      { label: '执行中', value: dashboard.value.inProgress, type: 'in-progress' },
      { label: '已完成', value: dashboard.value.completed, type: 'completed' },
    ]
  })

  return {
    dashboard,
    applications,
    plans,
    dispatchOrders,
    timeline,
    pagination,
    loading,
    statusStatistics,
    fetchAll,
    fetchApplications,
  }
})
