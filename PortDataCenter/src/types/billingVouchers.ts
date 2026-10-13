import type { ApiResponse, PaginatedResult } from './common'

// 计费凭据实体
export interface BillingVoucher {
    id: number
    voucherCode: string
    certificateId: number
    vesselId: number
    agentId: number
    
    baseAmount: number
    surchargeAmount: number
    discountAmount: number
    totalAmount: number
    
    billingMethod: string
    auditStatus: string
    paymentStatus: string
    dueDate: string
    createdAt: string
    updatedAt: string
}

// 计费明细
export interface ChargeDetail {
    chargeName: string
    chargeCategory: string
    unitPrice: number
    quantity: number
    amount: number
    description: string
    notes?: string
    chargeTime?: string
}

// 计费结果VO
export interface BillingResultVO {
    voucherId: number
    voucherCode: string
    certificateId: number
    certificateCode?: string
    planId?: number
    planCode?: string
    vesselId: number
    vesselNameZh: string
    vesselNameEn: string
    agentId: number
    agentName: string
    grossTonnage?: number
    netTonnage?: number
    
    baseAmount: number
    surchargeAmount: number
    discountAmount: number
    totalAmount: number
    
    chargeDetails: ChargeDetail[]
    
    billingMethod: string
    auditStatus: string
    billingStatus?: string  // 添加计费状态字段
    paymentStatus: string
    dueDate: string
    createdAt: string
    
    // 审核详情新增字段
    startLocationName?: string
    endLocationName?: string
    pilotageStartTime?: string
    pilotageEndTime?: string

    // 纸质发票字段（兼容旧数据；电子发票时代可不再使用）
    invoiceCode?: string
    invoiceNumber?: string

    // 电子发票字段
    electronicInvoiceStatus?: string // NOT_GENERATED | GENERATING | GENERATED | SENT | RED | VOIDED
    electronicGeneratedAt?: string
    electronicGeneratedBy?: number
    electronicSentAt?: string
    electronicSentBy?: number
    electronicSentTo?: string
    checkCode?: string
    invoiceNotes?: string
    downloadCount?: number
    lastDownloadAt?: string

    // 移交信息
    transferStatus?: string
    transferTarget?: string
    transferTime?: string

    taxStatus?: string
    taxRecordId?: number
    taxRecordCode?: string
    taxableAmount?: number
    totalTaxAmount?: number
}

// API响应类型
export type BillingVoucherListResponse = ApiResponse<PaginatedResult<BillingResultVO>>
export type BillingVoucherDetailResponse = ApiResponse<BillingResultVO>