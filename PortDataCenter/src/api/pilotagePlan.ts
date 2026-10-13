import request from '@/utils/request'
import type { PaginatedResult } from "@/types/common.ts"
import type { PilotagePlan } from "@/types/pilotage.ts"

// 请求参数类型定义
export interface CreatePlanParams {
    [key: string]: any
}

export interface GetPlansListParams {
    page?: number
    size?: number
    planStatus?: string
    startDate?: string
    endDate?: string
    vesselName?: string
    pilotId?: number
}

export interface ResourceCheckRequest {
    resourceType: 'PILOT' | 'DRIVER' | 'VEHICLE' | 'TUGBOAT' | 'PILOT_BOAT'
    resourceId: number
    startTime: string
    endTime: string
    excludePlanId?: number
}

export interface GetPlanStatisticsParams {
    startDate?: string
    endDate?: string
    pilotId?: number
    vesselType?: string
}

export interface ExportPlansParams {
    startDate?: string
    endDate?: string
    format?: 'excel' | 'pdf'
}

export interface PlanDateDTO {
    planStartTime: string | Date
    planEndTime: string | Date
    boardingTime?: string | Date
    leavingTime?: string | Date
}

export interface TugboatAssignmentDTO {
    tugboatId: number
    assignmentType?: 'MAIN' | 'ASSIST'
    estimatedStartTime?: string | Date
    estimatedEndTime?: string | Date
}

export interface PilotBoatAssignmentDTO {
    pilotBoatId: number
    purpose?: string
    estimatedStartTime?: string | Date
    estimatedEndTime?: string | Date
}

export interface SaveAssignmentsResultDTO {
    successCount: number
    failedCount: number
    failedItems: Array<{
        index: number
        reason: string
        resourceId?: number
        resourceType?: string
    }>
}

export interface BatchPublishResultDTO {
    successCount: number
    failedCount: number
    successIds: number[]
    failedIds: number[]
}
export interface TugboatDTO {
    id: number
    tugboatName: string
    callSign: string
    bollardPull: number
    power: number
    currentLocation: string
    ownerCompany: string
    status: string
}

export const fetchPilotagePlans = (params: {
    current?: number,
    size?: number,
    orderBy?: string,
    asc?: boolean,
    keyword?: string,
    params?: Record<string, any>
} = {}) => {
    const { current, size, keyword, params: extraParams, ...rest } = params
    return request<PaginatedResult<PilotagePlan>>({
        url: '/api/pilotage/plans/page',
        method: 'get',
        params: {
            current: current || 1,
            size: size || 20,
            keyword,
            ...(extraParams || {}),
            ...rest
        },
    })
}

export const createPlan = (planData: CreatePlanParams) => {
    return request({
        url: '/api/pilotage/plans',
        method: 'post',
        data: planData
    })
}

export const createPilotagePlans = (planDataList: CreatePlanParams[]) => {
    return request({
        url: '/api/pilotage/plans/batch',
        method: 'post',
        data: planDataList
    })
}

export const getPlansList = (params?: GetPlansListParams) => {
    return request({
        url: '/api/pilotage/plans',
        method: 'get',
        params
    })
}

export const getPlanDetail = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}`,
        method: 'get'
    })
}

export const updatePlan = (planId: number, planData: CreatePlanParams) => {
    return request({
        url: `/api/pilotage/plans/${planId}`,
        method: 'put',
        data: planData
    })
}

export const replanPlan = (planId: number, planData: CreatePlanParams) => {
    return request({
        url: `/api/pilotage/plans/${planId}/replan`,
        method: 'put',
        data: planData
    })
}

export const deletePlan = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}`,
        method: 'delete'
    })
}

export const deletePlansBatch = (planIds: number[]) => {
    return request({
        url: '/api/pilotage/plans/batch',
        method: 'delete',
        data: planIds
    })
}

export const publishPlan = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/publish`,
        method: 'put'
    })
}

export const publishPlansBatch = (planIds: number[]): Promise<{ data: BatchPublishResultDTO }> => {
    return request({
        url: '/api/pilotage/plans/batch/publish',
        method: 'post',
        data: planIds
    })
}

export const cancelPlan = (planId: number, reason?: string) => {
    return request({
        url: `/api/pilotage/plans/${planId}/cancel`,
        method: 'put',
        data: { cancelReason: reason }
    })
}

export const startPlan = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/start`,
        method: 'put'
    })
}

export const completePlan = (planId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/complete`,
        method: 'put'
    })
}

export const checkResourceAvailability = (resourceCheckRequest: ResourceCheckRequest) => {
    return request({
        url: '/api/pilotage/plans/checkResourceAvailability',
        method: 'post',
        data: resourceCheckRequest
    })
}

export const batchCheckResourceAvailability = (
    resourceChecks: ResourceCheckRequest[]
) => {
    return request({
        url: '/api/pilotage/plans/batchCheckResourceAvailability',
        method: 'post',
        data: resourceChecks
    })
}

export const assignTugboatToPlan = (planId: number, tugboatData: any) => {
    return request({
        url: `/api/pilotage/plans/${planId}/assignTugboat`,
        method: 'post',
        data: tugboatData
    })
}

export const assignPilotBoatToPlan = (planId: number, pilotBoatData: any) => {
    return request({
        url: `/api/pilotage/plans/${planId}/assignPilotBoat`,
        method: 'post',
        data: pilotBoatData
    })
}

export const removeTugboatAssignment = (planId: number, assignmentId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/removeTugboat/${assignmentId}`,
        method: 'delete'
    })
}

export const removePilotBoatAssignment = (planId: number, assignmentId: number) => {
    return request({
        url: `/api/pilotage/plans/${planId}/removePilotBoat/${assignmentId}`,
        method: 'delete'
    })
}

export const getPlanStatistics = (params?: GetPlanStatisticsParams) => {
    return request({
        url: '/api/pilotage/plans/statistics',
        method: 'get',
        params
    })
}

export const exportPlans = (params?: ExportPlansParams) => {
    return request({
        url: '/api/pilotage/plans/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}
export const setPlanDate = (planId: number, dateData: PlanDateDTO) => {
    const formattedData = {
        planStartTime: dateData.planStartTime,
        planEndTime: dateData.planEndTime,
        boardingTime: dateData.boardingTime,
        leavingTime: dateData.leavingTime
    }

    return request({
        url: `/api/pilotage/plans/${planId}/date`,
        method: 'put',
        data: formattedData
    })
}
export const saveTugboatAssignments = (planId: number, assignments: TugboatAssignmentDTO[]) => {
    return request<SaveAssignmentsResultDTO>({
        url: `/api/pilotage/plans/${planId}/tugboatAssignments`,
        method: 'post',
        data: assignments  // 确保将assignments作为请求体发送
    })
}
export const savePilotBoatAssignments = (
    planId: number,
    assignments: PilotBoatAssignmentDTO[]
): Promise<{ data: SaveAssignmentsResultDTO }> => {
    return request({
        url: `/api/pilotage/plans/${planId}/pilotBoatAssignments`,
        method: 'post',
        data: assignments
    })
}
export const savePlanSchedule = async (
    planId: number,
    options: {
        planDate?: PlanDateDTO
        tugboatAssignments?: TugboatAssignmentDTO[]
        pilotBoatAssignments?: PilotBoatAssignmentDTO[]
    }
) => {
    const results: any = {}

    if (options.planDate) {
        try {
            await setPlanDate(planId, options.planDate)
            results.dateSuccess = true
        } catch (error) {
            results.dateSuccess = false
            results.dateError = error
        }
    }

    if (options.tugboatAssignments) {
        try {
            const response = await saveTugboatAssignments(planId, options.tugboatAssignments)
            results.tugboatResult = response.data
        } catch (error) {
            results.tugboatError = error
        }
    }

    if (options.pilotBoatAssignments) {
        try {
            const response = await savePilotBoatAssignments(planId, options.pilotBoatAssignments)
            results.pilotBoatResult = response.data
        } catch (error) {
            results.pilotBoatError = error
        }
    }

    return results
}

export const getAvailableTugboats = () => {
    return request<TugboatDTO[]>({
        url: '/api/planTugboatAssignments/availableTugs',
        method: 'get'
    })
}