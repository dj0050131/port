
import type { ApiResponse, PaginationQuery, PaginatedResult } from './common'

export interface PlanConfirmationQueryParams extends PaginationQuery {
    planId?: number
    planCode?: string
    resourceType?: string
    confirmMethod?: string
    confirmationStatus?: string
    confirmedBy?: number
    startDate?: string
    endDate?: string
}

export interface PlanConfirmationDTO {
    id?: number
    planId: number
    resourceType: string
    resourceId?: number
    confirmMethod: string
    confirmedBy?: number
    confirmedAt?: string
    notes?: string
}

export interface SendConfirmationRequest {
    planId: number
    resourceId: number
    resourceType: string
}

export interface PlanConfirmationResult {
    id: number
    planId: number
    planCode: string
    planStatus?: string
    assignmentId?: number
    resourceType: string
    resourceTypeName: string
    resourceId: number
    resourceName: string
    confirmMethod: string
    confirmMethodName: string
    confirmedBy: number
    confirmedByName: string
    confirmedAt: string
    confirmationStatus: string
    confirmationStatusName: string
    notes: string
    createdAt: string
}

export interface PlanConfirmationStats {
    pilot_count: number
    driver_count: number
    pilot_boat_count: number
    tugboat_count: number
    confirmed_count: number
    total_count: number
    confirmationProgress: number
}

export interface PlanFullConfirmationStatus {
    planId: number
    planCode: string
    planStatus: string
    planStartTime: string
    confirmations: Record<string, PlanConfirmationResult[]>
    confirmationProgress: number
    [key: string]: any
}

export interface MyTaskQueryParams extends PaginationQuery {
    status?: string // PENDING, CONFIRMED, etc.
}

export interface MyTaskRecord {
    planId: number
    planCode: string
    planStatus: string
    assignmentId: number
    resourceType: string
    resourceId: number
    resourceName: string
    resourceTypeName: string
    vesselName: string
    vesselNameZh: string
    vesselNameEn: string
    assignmentStatus: string
    assignmentStatusName: string
    confirmationStatus: string
    confirmationStatusName: string
    estimatedStartTime: string
    estimatedEndTime: string
    createdAt: string
    confirmedAt?: string | null
    confirmedByName?: string | null
    notes?: string | null
    resourceDetail?: any
}

// API响应类型
export type PlanConfirmationListResponse = ApiResponse<PaginatedResult<PlanConfirmationResult>>
export type PlanConfirmationStatsResponse = ApiResponse<PlanConfirmationStats>
export type PlanFullConfirmationStatusResponse = ApiResponse<PlanFullConfirmationStatus>
export type BatchConfirmResponse = ApiResponse<boolean>
export type MyTaskListResponse = ApiResponse<PaginatedResult<MyTaskRecord>>
export type MyTaskDetailResponse = ApiResponse<MyTaskRecord>

export const ResourceType = {
    PILOT: 'PILOT',
    DRIVER: 'DRIVER',
    PILOT_BOAT: 'PILOT_BOAT',
    TUGBOAT: 'TUGBOAT',
    VEHICLE: 'VEHICLE',
    OVERALL: 'OVERALL'
} as const

export const ConfirmMethod = {
    WEB: 'WEB',
    SMS: 'SMS',
    PHONE: 'PHONE',
    MANUAL: 'MANUAL'
} as const

export const ConfirmationStatus = {
    PENDING: 'PENDING',
    CONFIRMED: 'CONFIRMED',
    REJECTED: 'REJECTED',
    CANCELLED: 'CANCELLED',
    SYSTEM_CANCELLATION: 'SYSTEM_CANCELLATION',
} as const

export type ResourceTypeEnum = typeof ResourceType[keyof typeof ResourceType]
export type ConfirmMethodEnum = typeof ConfirmMethod[keyof typeof ConfirmMethod]
export type ConfirmationStatusEnum = typeof ConfirmationStatus[keyof typeof ConfirmationStatus]