import request from '@/utils/request'
import type { PaginatedResult } from '@/types/common'
import type {
  PilotageApplication,
  PilotagePlan,
  WorkSheet,
  PilotageDashboard,
  PilotageTimelineItem,
} from '@/types/pilotage'

export const fetchPilotageApplications = (params: {
  current?: number
  size?: number
  keyword?: string
  nameZh?: string
  nameEn?: string
  requestStatus?: string
  planStatus?: string
} = {}) => {
  return request<PaginatedResult<PilotageApplication>>({
    url: '/api/pilotage/requests/page',
    method: 'get',
    params: {
      current: params.current || 1,
      size: params.size || 20,
      keyword: params.keyword,
      nameZh: params.nameZh,
      nameEn: params.nameEn,
      requestStatus: params.requestStatus,
      planStatus: params.planStatus,
    },
  })
}

export const getPilotageApplication = (id: number) => {
  return request<PilotageApplication>({
    url: `/api/pilotage/requests/${id}`,
    method: 'get',
  })
}

export const createPilotageApplication = (data: any) => {
  return request<number>({
    url: '/api/pilotage/requests',
    method: 'post',
    data,
  })
}

export const updatePilotageApplication = (id: number, data: any) => {
  return request<void>({
    url: `/api/pilotage/requests/${id}`,
    method: 'patch',
    data,
  })
}

export const exportPilotageApplications = (ids?: Array<number | string>) => {
  const idStr = (ids || []).map((v) => String(v)).filter(Boolean).join(',')
  return request({
    url: '/api/pilotage/requests/export',
    method: 'get',
    params: idStr ? { ids: idStr } : undefined,
    responseType: 'blob',
  })
}

//     method: 'delete',
//   })
// }

//     method: 'post',
//   })
// }

export const fetchWorkSheets = (page: number = 1, pageSize: number = 20) => {
  return request<PaginatedResult<WorkSheet>>({
    url: '/api/pilotage/worksheets/page',
    method: 'get',
    params: { current: page, size: pageSize },
  })
}

// --- Dashboard & Timeline ---

export const fetchPilotageDashboard = () => {
  return request<PilotageDashboard>({
    url: '/api/monitor/dashboard/pilotageStat',
    method: 'get',
  })
}

export const fetchPilotageTimeline = () => {
  // Placeholder
  return request<PilotageTimelineItem[]>({
    url: '/api/monitor/timeline', // Still hypothetical, may need to implement in backend or use dummy data for now
    method: 'get',
  })
}
//         url: '/api/base/berths/listName',
//         method: 'get',
//     })
// }

export const batchReviewPilotageApplications = (ids: number[], approved: boolean, rejectReason?: string) => {
  return request({
    url: '/api/pilotage/requests/batch/review',
    method: 'post',
    data: ids,
    params: { approved, rejectReason }
  })
}

export const batchDeletePilotageApplications = (ids: number[]) => {
  return request({
    url: '/api/pilotage/requests/batch',
    method: 'delete',
    data: ids
  })
}

export const createPilotagePlan = (data: any | any[]) => {
  // 确保传递的数据始终是数组格式
  const planData = Array.isArray(data) ? data : [data];
  return request<number[]>({
    url: '/api/pilotage/plans',
    method: 'post',
    data: planData,
  })
}

export const createPilotagePlans = (planDataList: any[]) => {
    return request({
        url: '/api/pilotage/plans/batch',
        method: 'post',
        data: planDataList
    })
}

export const publishPilotagePlan = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/publish`,
        method: 'put'
    })
}

export const batchPublishPilotagePlans = (planIds: number[]) => {
    return request({
        url: '/api/pilotage/plans/batchPublish',
        method: 'put',
        data: planIds
    })
}

export const checkResourceAvailability = (resourceCheckRequest: {
    resourceType: 'PILOT' | 'DRIVER' | 'VEHICLE' | 'TUGBOAT' | 'PILOT_BOAT'
    resourceId: number
    startTime: string
    endTime: string
    excludePlanId?: number
}) => {
    return request({
        url: '/api/pilotage/plans/checkResourceAvailability',
        method: 'post',
        data: resourceCheckRequest
    })
}

export const batchCheckResourceAvailability = (resourceChecks: Array<{
    resourceType: 'PILOT' | 'DRIVER' | 'VEHICLE' | 'TUGBOAT' | 'PILOT_BOAT'
    resourceId: number
    startTime: string
    endTime: string
    excludePlanId?: number
}>) => {
    return request({
        url: '/api/pilotage/plans/batchCheckResourceAvailability',
        method: 'post',
        data: resourceChecks
    })
}
export const updatePilotageRequestStatus = (ids: number[], status: string) => {
    return request({
        url: '/api/pilotage/requests/status',
        method: 'put',
        data: { ids, status }
    })
}

