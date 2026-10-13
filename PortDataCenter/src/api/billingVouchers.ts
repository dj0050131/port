import request from '@/utils/request'
import type { ApiResponse, PaginatedResult } from '@/types/common'
import type { BillingVoucher, BillingResultVO } from '@/types/billingVouchers'
import type { CertificateDetail } from '@/api/certificate'

// 请求参数类型定义
export interface BillingVoucherQueryParams {
    current?: number
    size?: number
    certificateId?: number
    vesselNameZh?: string
    agentName?: string
    auditStatus?: string
    paymentStatus?: string
    params?: Record<string, any>
}

export interface BillingCalculateDTO {
    certificateId: number
}

export interface BillingApprovalRequest {
    ids: number[]
    approved: boolean
    rejectionReason?: string
}

// API 函数
export const fetchBillingVouchers = (params: BillingVoucherQueryParams) => {
    return request<ApiResponse<PaginatedResult<BillingResultVO>>>({
        url: '/api/billing/vouchers/page',
        method: 'get',
        params
    })
}
export const getBillingVoucherDetail = (id: number) => {
    return request<ApiResponse<BillingResultVO>>({
        url: `/api/billing/vouchers/${id}`,
        method: 'get'
    })
}
export const getBillingVoucherByCertificateId = (certificateId: number) => {
    return request<ApiResponse<BillingResultVO>>({
        url: `/api/billing/vouchers/byCertificate/${certificateId}`,
        method: 'get'
    })
}
export const startBilling = (certificateId: number) => {
    return request<ApiResponse<BillingResultVO>>({
        url: `/api/billing/vouchers/startBilling/${certificateId}`,
        method: 'post'
    })
}
export const approveBillingVouchers = (data: BillingApprovalRequest) => {
    return request<ApiResponse<void>>({
        url: `/api/billing/vouchers/approve`,
        method: 'post',
        data
    })
}
export const fetchRejectedBillingVouchers = (params: BillingVoucherQueryParams) => {
    // 后端已调整：返回的是“需要重计费的签证单列表”（CertificateDetail），不再是被退回的凭证列表
    return request<ApiResponse<PaginatedResult<CertificateDetail>>>({
        url: '/api/billing/vouchers/rejected',
        method: 'get',
        params
    })
}
export const rebillRejectedVoucher = (id: number) => {
    return request<ApiResponse<BillingResultVO>>({
        url: `/api/billing/vouchers/rebill/${id}`,
        method: 'post'
    })
}