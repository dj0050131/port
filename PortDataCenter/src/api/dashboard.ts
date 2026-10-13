import request from '@/utils/request'
import type {
    BackendDashboardOverview,
    PilotageStatVO,
    FinancialStatVO,
    ThroughputStatVO,
    ResourceUsageStatVO,
    AlertStatVO, ConflictCheckRequest, ResourceConflict, ResourceUsageOverview, GanttChartData, ResourceTimeline
} from '@/types/dashboard'

export const fetchDashboardOverview = () => {
  return request<BackendDashboardOverview>({
    url: '/api/monitor/dashboard/overview',
    method: 'get',
  })
}

export const fetchPilotageStat = () => {
  return request<PilotageStatVO>({
    url: '/api/monitor/dashboard/pilotageStat',
    method: 'get'
  })
}

export const fetchFinancialStat = () => {
  return request<FinancialStatVO>({
    url: '/api/monitor/dashboard/financialStat',
    method: 'get'
  })
}

export const fetchThroughputStat = () => {
  return request<ThroughputStatVO>({
    url: '/api/monitor/dashboard/throughputStat',
    method: 'get'
  })
}

export const fetchResourceUsageStat = () => {
  return request<ResourceUsageStatVO>({
    url: '/api/monitor/dashboard/resourceUsageStat',
    method: 'get'
  })
}

export const fetchAlertStat = () => {
  return request<AlertStatVO>({
    url: '/api/monitor/dashboard/alertStat',
    method: 'get'
  })
}

export const fetchAlertList = () => {
  return request<any[]>({
    url: '/api/monitor/dashboard/alerts',
    method: 'get'
  })
}

export const fetchResourceTimeline = (startDate: string, endDate: string, resourceType?: string) => {
    return request<ResourceTimeline[]>({
        url: '/api/gantt/resourceTimeline',
        method: 'get',
        params: {
            startDate,
            endDate,
            resourceType
        }
    })
}

export const fetchGanttData = (startDate: string, endDate: string) => {
    return request<GanttChartData>({
        url: '/api/gantt/data',
        method: 'get',
        params: {
            startDate,
            endDate
        }
    })
}

export const updatePlanTime = (planId: number, newStartTime: string, newEndTime: string) => {
    return request<boolean>({
        url: '/api/gantt/updatePlanTime',
        method: 'put',
        params: {
            planId,
            newStartTime,
            newEndTime
        }
    })
}

export const checkConflicts = (requestBody: ConflictCheckRequest) => {
    return request<ResourceConflict[]>({
        url: '/api/gantt/checkConflicts',
        method: 'post',
        data: requestBody
    })
}

export const fetchResourceOverview = (startDate: string, endDate: string) => {
    return request<ResourceUsageOverview>({
        url: '/api/gantt/resourceOverview',
        method: 'get',
        params: {
            startDate,
            endDate
        }
    })
}