
import request from '@/utils/request'
import type {
    PlanConfirmationQueryParams,
    PlanConfirmationDTO,
    PlanConfirmationListResponse,
    PlanConfirmationStatsResponse,
    PlanFullConfirmationStatusResponse,
    BatchConfirmResponse,
    SendConfirmationRequest,
    MyTaskQueryParams,
    MyTaskListResponse,
    MyTaskDetailResponse
} from '@/types/planConfirmations'
import type {ApiResponse} from "@/types/common.ts";
export const createConfirmation = (data: PlanConfirmationDTO) => {
    return request<BatchConfirmResponse>({
        url: '/api/pilotage/planConfirmations',
        method: 'post',
        data
    })
}
export const batchConfirm = (data: PlanConfirmationDTO[]) => {
    return request<BatchConfirmResponse>({
        url: '/api/pilotage/planConfirmations/batch',
        method: 'post',
        data
    })
}
export const batchCancelConfirmations = (data: PlanConfirmationDTO[], reason: string) => {
    return request<BatchConfirmResponse>({
        url: '/api/pilotage/planConfirmations/batch/cancel',
        method: 'put',
        params: { reason },
        data
    })
}
export const getConfirmationList = (params: PlanConfirmationQueryParams) => {
    return request<PlanConfirmationListResponse>({
        url: '/api/pilotage/planConfirmations',
        method: 'get',
        params
    })
}
export const getAssignmentsResourceList = (params: PlanConfirmationQueryParams) => {
    return request<ApiResponse<Map<string, object>>>({
        url: '/api/pilotage/planConfirmations/assignmentsResources',
        method: 'get',
        params
    })
}
export const getPlanConfirmationStats = (planId: number) => {
    return request<PlanConfirmationStatsResponse>({
        url: `/api/pilotage/planConfirmations/plan/${planId}/stats`,
        method: 'get'
    })
}
export const getPlanFullConfirmationStatus = (planId: number) => {
    return request<PlanFullConfirmationStatusResponse>({
        url: `/api/pilotage/planConfirmations/plan/${planId}/fullStatus`,
        method: 'get'
    })
}
export const cancelConfirmation = (confirmationId: number, reason: string) => {
    return request<BatchConfirmResponse>({
        url: `/api/pilotage/planConfirmations/${confirmationId}/cancel`,
        method: 'put',
        params: { reason }
    })
}
export const checkConfirmation = (planId: number, resourceType: string, resourceId: number) => {
    return request<ApiResponse<boolean>>({
        url: `/api/pilotage/planConfirmations/check/${planId}/${resourceType}/${resourceId}`,
        method: 'get'
    })
}
export const sendConfirmationReminder = (requestParams: SendConfirmationRequest) => {
    return request<BatchConfirmResponse>({
        url: '/api/pilotage/plans/sendConfirmationReminder',
        method: 'post',
        data: requestParams
    })
}
export const fetchMyTasks = (params: MyTaskQueryParams) => {
    return request<MyTaskListResponse>({
        url: '/api/pilotage/planConfirmations/myTasks',
        method: 'get',
        params
    })
}
export const fetchMyTaskDetail = (assignmentId: number) => {
    return request<MyTaskDetailResponse>({
        url: `/api/pilotage/planConfirmations/myTasks/${assignmentId}`,
        method: 'get'
    })
}