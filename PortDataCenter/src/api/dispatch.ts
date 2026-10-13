import request from '@/utils/request'
import type {
    DispatchPlan,
    DispatchQueryParams,
    DispatchDetail,
    DispatchTimeUpdateParams,
    ApiResponse,
    PaginatedResult
} from '@/types/dispatch'
export const fetchDispatchList = (params: DispatchQueryParams) => {
    return request<ApiResponse<PaginatedResult<DispatchPlan>>>({
        url: '/api/pilotage/dispatch/list',
        method: 'get',
        params
    })
}
export const fetchDispatchDetail = (planId: number | string) => {
    return request<ApiResponse<DispatchDetail>>({
        url: `/api/pilotage/dispatch/detail/${planId}`,
        method: 'get'
    })
}
export const startDispatch = (planId: number | string, data: DispatchTimeUpdateParams) => {
    return request<ApiResponse<boolean>>({
        url: `/api/pilotage/dispatch/start/${planId}`,
        method: 'post',
        data
    })
}
export const completeDispatch = (planId: number | string, data: DispatchTimeUpdateParams) => {
    return request<ApiResponse<boolean>>({
        url: `/api/pilotage/dispatch/complete/${planId}`,
        method: 'post',
        data
    })
}
export const cancelDispatch = (planId: number | string, reason?: string) => {
    return request<ApiResponse<boolean>>({
        url: `/api/pilotage/dispatch/cancel/${planId}`,
        method: 'post',
        params: { reason }
    })
}
export const updateDispatchTime = (planId: number | string, data: DispatchTimeUpdateParams) => {
    return request<ApiResponse<boolean>>({
        url: `/api/pilotage/dispatch/time/${planId}`,
        method: 'put',
        data
    })
}
export const fetchDispatchStatistics = (params: any) => {
    return request<ApiResponse<any>>({
        url: '/api/pilotage/dispatch/statistics',
        method: 'get',
        params
    })
}
export const exportDispatchData = (params: DispatchQueryParams) => {
    return request<Blob>({
        url: '/api/pilotage/dispatch/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}