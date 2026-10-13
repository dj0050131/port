import request from '@/utils/request'
import type { ApiResponse, PaginatedResult } from '@/types/common'

// 签证单相关接口

export interface CertificateQueryParams {
    page?: number
    size?: number
    startDate?: string
    endDate?: string
    vesselNameZh?: string
    vesselNameEn?: string
    submitStatus?: string
    billingStatus?: string
    pilotId?: number
    certificateCode?: string
    voyageNumber?: string
    vesselCallSign?: string
    imoNumber?: string
    agentId?: number
    ids?: string
}

export interface CertificateDetail {
    id: number
    certificateCode: string
    operationId: string
    planId: number
    vesselId: number
    voyageId: number

    // 船舶信息
    vesselNameZh: string
    vesselNameEn: string
    vesselCallSign: string
    imoNumber: string
    mmsiNumber: string
    vesselType: string
    nationality: string
    vesselLength: number
    vesselWidth: number
    grossTonnage: number
    netTonnage: number
    deadweight: number
    fullLoadDraft: number
    airDraft: number
    speed: number

    // 航次信息
    voyageNumber: string
    voyageType: string
    cargoName: string
    cargoQuantity: number
    cargoUnit: string
    draftForward: number
    draftAft: number

    // 代理信息
    inboundAgentId: number
    outboundAgentId: number
    inboundAgentName: string
    outboundAgentName: string
    inboundAgentCode: string
    outboundAgentCode: string

    // 位置信息（后端字段：start/end_location_*）
    startLocationId: number
    endLocationId: number
    startLocationType: string
    endLocationType: string
    startLocationName: string
    endLocationName: string
    startLocationFullName?: string
    endLocationFullName?: string

    startBerthId?: number
    endBerthId?: number
    startBerthName?: string
    endBerthName?: string

    // 航线信息（可能为 AUTO 回退值）
    routeCode: string
    routeName: string

    // 引航信息
    pilotageType: string
    forwardDraft: number
    aftDraft: number
    horsepower: number
    tradeType: string

    // 时间信息
    pilotageStartTime: string
    pilotageEndTime: string
    boardingTime: string
    leavingTime: string
    pilotSignTime: string
    agentConfirmTime: string
    submittedAt: string
    confirmedAt: string

    // 人员信息
    totalPassengers: number
    totalCrew: number
    detentionFlag: string
    detentionHours: number
    detentionPersons: number
    assignedPilotId: number
    assignedPilotName: string
    assignedPilotCode: string
    enteredBy: string
    confirmedBy: number
    confirmedByName: string

    // 业务标志
    escortFlag: string
    berthingAssistanceFlag: string
    berthingAssistanceCharge: number
    withdrawalFlag: string

    // 状态信息
    submitStatus: string
    billingStatus: string
    statusCode: string
    statusName: string

    // 其他信息
    notes: string
    specialSituationDescription: string
    handoverSheetId: string
    inboundVoyage: string
    outboundVoyage: string

    // 系统信息
    createdBy: number
    updatedBy: number
    createdAt: string
    updatedAt: string

    // 后端列表接口会附带：该签证单最新的计费凭证ID（用于查看详情）
    latestBillingVoucherId?: number
}

export interface CertificateCreateData {
    planId: number
    // 船舶状态
    forwardDraft: number
    aftDraft: number
    totalPassengers: number
    totalCrew: number

    // 时间信息
    pilotageStartTime: string
    pilotageEndTime: string
    boardingTime: string
    leavingTime: string
    pilotSignTime: string

    // 业务标志
    escortFlag?: string
    berthingAssistanceFlag?: string
    berthingAssistanceCharge?: number
    detentionFlag?: string
    detentionHours?: number
    detentionPersons?: number

    // 其他信息
    tradeType?: string
    horsepower?: number
    notes?: string
    specialSituationDescription?: string
    handoverSheetId?: string
}

export interface CertificateUpdateData {
    // 航线区间补录：优先使用 routeId；否则可直接补录 routeCode/routeName
    routeId?: number
    routeCode?: string
    routeName?: string

    // 船舶状态
    forwardDraft?: number
    aftDraft?: number
    totalPassengers?: number
    totalCrew?: number

    // 时间信息
    pilotageStartTime?: string
    pilotageEndTime?: string
    boardingTime?: string
    leavingTime?: string
    pilotSignTime?: string
    agentConfirmTime?: string

    // 业务标志
    escortFlag?: string
    berthingAssistanceFlag?: string
    berthingAssistanceCharge?: number
    detentionFlag?: string
    detentionHours?: number
    detentionPersons?: number
    withdrawalFlag?: string

    // 其他信息
    tradeType?: string
    horsepower?: number
    notes?: string
    specialSituationDescription?: string
    handoverSheetId?: string
}

export interface CertificateSubmitData {
    notes?: string
    confirmData?: boolean
}

export interface CertificateResult {
    id: number
    certificateCode: string
    planId: number
    vesselId: number
    voyageId: number
    submitStatus: string
    billingStatus: string
    createdAt: string
    success: boolean
    message: string
}

export interface PlanCertificateInfo {
    planId: number
    planCode: string
    vesselId: number
    vesselNameZh: string
    vesselNameEn: string
    vesselCallSign: string
    imoNumber: string
    grossTonnage: number
    netTonnage: number
    voyageId: number
    voyageNumber: string
    voyageType: string
    startLocationName: string
    endLocationName: string
    startLocationType: string
    endLocationType: string
    plannedStartTime: string
    plannedEndTime: string
    assignedPilotId: number
    assignedPilotName: string
    inboundAgentId: number
    inboundAgentName: string
    outboundAgentId: number
    outboundAgentName: string
    routeCode: string
    routeName: string
}

// API 函数
export const fetchCertificateList = (params: CertificateQueryParams): Promise<ApiResponse<PaginatedResult<CertificateDetail>>> => {
    return request<PaginatedResult<CertificateDetail>>({
        url: '/api/pilotage/certificate/list',
        method: 'get',
        params
    })
}
export const fetchCertificateDetail = (certificateId: number): Promise<ApiResponse<CertificateDetail>> => {
    return request<CertificateDetail>({
        url: `/api/pilotage/certificate/detail/${certificateId}`,
        method: 'get'
    })
}
export const fetchCertificateByPlanId = (planId: number): Promise<ApiResponse<CertificateDetail>> => {
    return request<CertificateDetail>({
        url: `/api/pilotage/certificate/plan/${planId}`,
        method: 'get'
    })
}
export const fetchPlanCertificateInfo = (planId: number): Promise<ApiResponse<PlanCertificateInfo>> => {
    return request<PlanCertificateInfo>({
        url: `/api/pilotage/certificate/plan-info/${planId}`,
        method: 'get'
    })
}
export const createCertificate = (data: CertificateCreateData) => {
    return request<CertificateResult>({
        url: '/api/pilotage/certificate/create',
        method: 'post',
        data
    })
}
export const createCertificateAuto = (planId: number) => {
    return request<CertificateResult>({
        url: `/api/pilotage/certificate/create/auto/${planId}`,
        method: 'post'
    })
}
export const createBatchCertificates = (data: { planIds: number[], certificateData: CertificateCreateData }) => {
    return request<CertificateResult[]>({
        url: '/api/pilotage/certificate/create-batch',
        method: 'post',
        data
    })
}
export const updateCertificate = (certificateId: number, data: CertificateUpdateData) => {
    return request<boolean>({
        url: `/api/pilotage/certificate/update/${certificateId}`,
        method: 'put',
        data
    })
}
export const submitCertificate = (certificateId: number, data?: CertificateSubmitData) => {
    return request<boolean>({
        url: `/api/pilotage/certificate/submit/${certificateId}`,
        method: 'post',
        data
    })
}
export const submitBatchCertificates = (certificateIds: number[], data?: CertificateSubmitData) => {
    return request<boolean>({
        url: '/api/pilotage/certificate/submit-batch',
        method: 'post',
        data: {
            certificateIds,
            ...data
        }
    })
}
export const confirmCertificate = (certificateId: number) => {
    return request<boolean>({
        url: `/api/pilotage/certificate/confirm/${certificateId}`,
        method: 'post'
    })
}
export const pilotSignCertificate = (certificateId: number) => {
    return request<boolean>({
        url: `/api/pilotage/certificate/sign/${certificateId}`,
        method: 'post'
    })
}
export const deleteCertificate = (certificateId: number) => {
    return request<boolean>({
        url: `/api/pilotage/certificate/delete/${certificateId}`,
        method: 'delete'
    })
}
export const exportCertificates = (params: CertificateQueryParams) => {
    return request({
        url: '/api/pilotage/certificate/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}