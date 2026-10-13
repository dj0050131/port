import type { ApiResponse, PaginatedResult } from './common'
import type { BillingResultVO } from './billingVouchers'

// 请求参数类型定义
export interface ConfirmPaymentRequest {
  receivedAmount: number
  receiptDate: string
}

export interface RecordInvoiceRequest {
  invoiceCode: string
  invoiceNumber: string
}

export interface TransferRequest {
  transferTarget: string
  transferType: string
}

export interface PaymentNoticeFailureVO {
  voucher: BillingResultVO
  lastAttemptAt: string
  errorMessage?: string
}

// API响应类型
export type PendingConfirmationResponse = ApiResponse<PaginatedResult<BillingResultVO>>
export type PendingInvoiceResponse = ApiResponse<PaginatedResult<BillingResultVO>>
export type TransferredResponse = ApiResponse<PaginatedResult<BillingResultVO>>
export type UntransferredResponse = ApiResponse<PaginatedResult<BillingResultVO>>
export type PaymentNoticeFailedResponse = ApiResponse<PaginatedResult<PaymentNoticeFailureVO>>