import type { ApiResponse, PaginatedResult } from './common'

export interface FeeWriteOffs {
  id?: number
  writeOffCode?: string
  agentId?: number
  writeOffAmount?: number
  writeOffType?: string
  writeOffPerson?: string
  writeOffDate?: string
  auditor?: string
  auditTime?: string
  statusCode?: string
  statusName?: string
  notes?: string
  documentAttribute?: string
  writeOffMethod?: string
  bankReferenceNumber?: string
  voucherNumber?: string
  financeConfirmer?: string
  financeConfirmTime?: string
  createdAt?: string
  updatedAt?: string
}

// API响应类型
export type FeeWriteOffsResponse = ApiResponse<PaginatedResult<FeeWriteOffs>>
export type FeeWriteOffDetailResponse = ApiResponse<FeeWriteOffs>
export type CreateFeeWriteOffResponse = ApiResponse<number>