import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/storage'
import type { 
  ApiResponse, 
  PaginatedResult 
} from '@/types/common'
import type { 
  BillingResultVO 
} from '@/types/billingVouchers'
import type {
  ConfirmPaymentRequest,
  RecordInvoiceRequest,
  TransferRequest,
  PendingConfirmationResponse,
  PendingInvoiceResponse,
  TransferredResponse,
  UntransferredResponse,
  PaymentNoticeFailedResponse
} from '@/types/charge'

// 请求参数类型定义
export interface PageQueryParams {
  current: number
  size: number
}

const rawAxios = axios.create({
  baseURL: '/',
  timeout: 150000,
  withCredentials: true,
})
rawAxios.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// API 函数
export const fetchPendingConfirmationCharges = (params: PageQueryParams) => {
  return request<PendingConfirmationResponse>({
    url: '/api/billing/charges/pendingConfirmation',
    method: 'get',
    params
  })
}
export const fetchPaymentNoticeFailed = (params: PageQueryParams & { keyword?: string }) => {
  return request<PaymentNoticeFailedResponse>({
    url: '/api/billing/charges/paymentNoticeFailed',
    method: 'get',
    params
  })
}
export const retryPaymentNotice = (id: number) => {
  return request<ApiResponse<boolean>>({
    url: `/api/billing/charges/paymentNotice/retry/${id}`,
    method: 'post'
  })
}
export const confirmPayment = (id: number, data: ConfirmPaymentRequest) => {
  return request<ApiResponse<BillingResultVO>>({
    url: `/api/billing/charges/confirmPayment/${id}`,
    method: 'post',
    params: data
  })
}
export const fetchPendingInvoiceCharges = (params: PageQueryParams) => {
  return request<PendingInvoiceResponse>({
    url: '/api/billing/charges/pendingInvoice',
    method: 'get',
    params
  })
}
export const recordInvoice = (id: number, data: RecordInvoiceRequest) => {
  return request<ApiResponse<BillingResultVO>>({
    url: `/api/billing/charges/recordInvoice/${id}`,
    method: 'post',
    params: data
  })
}
export const transferBillingVouchers = (ids: number[], data: TransferRequest) => {
  return request<ApiResponse<BillingResultVO[]>>({
    url: `/api/billing/charges/transfer`,
    method: 'post',
    params: {
      ids: ids.join(','),
      ...data
    }
  })
}
export const transferTaxRecords = (ids: number[], data: TransferRequest) => {
  return request<ApiResponse<void>>({
    url: '/api/billing/charges/transferTaxRecords',
    method: 'post',
    params: {
      ids: ids.join(','),
      ...data
    }
  })
}
export const retryTransferWriteOffs = (writeOffIds: number[], data: TransferRequest) => {
  return request<ApiResponse<void>>({
    url: '/api/billing/charges/transferRetry/writeoff',
    method: 'post',
    params: {
      ids: writeOffIds.join(','),
      ...data
    }
  })
}
export const retryTransferTaxWriteOffs = (writeOffIds: number[], data: TransferRequest) => {
  return request<ApiResponse<void>>({
    url: '/api/billing/charges/transferRetry/taxRecords',
    method: 'post',
    params: {
      ids: writeOffIds.join(','),
      ...data
    }
  })
}
export const fetchTransferredBillingVouchers = (params: PageQueryParams) => {
  return request<TransferredResponse>({
    url: '/api/billing/charges/transferred',
    method: 'get',
    params
  })
}
export const fetchUntransferredBillingVouchers = (params: PageQueryParams) => {
  return request<UntransferredResponse>({
    url: '/api/billing/charges/untransferred',
    method: 'get',
    params
  })
}
export const fetchUntransferredTaxRecords = (params: PageQueryParams) => {
  return request<UntransferredResponse>({
    url: '/api/billing/charges/untransferredTaxRecords',
    method: 'get',
    params
  })
}
export const downloadElectronicInvoice = (id: number) => {
  return request<ApiResponse<BillingResultVO>>({
    url: `/api/billing/charges/eInvoice/download/${id}`,
    method: 'get'
  })
}
export const sendElectronicInvoice = (id: number) => {
  return request<ApiResponse<BillingResultVO>>({
    url: `/api/billing/charges/eInvoice/send/${id}`,
    method: 'post'
  })
}
export const fetchElectronicInvoiceDetail = (id: number) => {
  return request<ApiResponse<Record<string, any>>>({
    url: `/api/billing/charges/eInvoice/detail/${id}`,
    method: 'get'
  })
}
export const downloadElectronicInvoicePdf = async (id: number) => {
  const res = await rawAxios.request({
    url: `/api/billing/charges/eInvoice/pdf/${id}`,
    method: 'get',
    responseType: 'blob'
  })
  return res
}
