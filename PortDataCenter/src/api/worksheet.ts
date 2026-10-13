import request from '@/utils/request'
import type { ApiResponse, PaginatedResult } from '@/types/common'
import type {
  WorksheetPilotRecord,
  WorksheetDriverRecord,
  WorksheetVehicleRecord,
  WorksheetTugboatRecord,
  WorksheetPilotBoatRecord,
  WorkSheetLedger,
  WorkSheetLedgerQuery,
} from '@/types/worksheet'

export interface WorksheetQueryParams {
  current: number
  size: number
  keyword?: string
  startDate?: string
  endDate?: string
}

export const fetchPilotWorksheetRecords = (params: WorksheetQueryParams) => {
  return request<ApiResponse<PaginatedResult<WorksheetPilotRecord>>>({
    url: '/api/pilotage/worksheets/records/pilot/page',
    method: 'get',
    params,
  })
}

export const exportPilotWorksheetRecords = (params: Omit<WorksheetQueryParams, 'current' | 'size'>) => {
  return request({
    url: '/api/pilotage/worksheets/records/pilot/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

export const fetchDriverWorksheetRecords = (params: WorksheetQueryParams) => {
  return request<ApiResponse<PaginatedResult<WorksheetDriverRecord>>>({
    url: '/api/pilotage/worksheets/records/driver/page',
    method: 'get',
    params,
  })
}

export const exportDriverWorksheetRecords = (params: Omit<WorksheetQueryParams, 'current' | 'size'>) => {
  return request({
    url: '/api/pilotage/worksheets/records/driver/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

export const fetchVehicleWorksheetRecords = (params: WorksheetQueryParams) => {
  return request<ApiResponse<PaginatedResult<WorksheetVehicleRecord>>>({
    url: '/api/pilotage/worksheets/records/vehicle/page',
    method: 'get',
    params,
  })
}

export const exportVehicleWorksheetRecords = (params: Omit<WorksheetQueryParams, 'current' | 'size'>) => {
  return request({
    url: '/api/pilotage/worksheets/records/vehicle/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

export const fetchTugboatWorksheetRecords = (params: WorksheetQueryParams) => {
  return request<ApiResponse<PaginatedResult<WorksheetTugboatRecord>>>({
    url: '/api/pilotage/worksheets/records/tugboat/page',
    method: 'get',
    params,
  })
}

export const exportTugboatWorksheetRecords = (params: Omit<WorksheetQueryParams, 'current' | 'size'>) => {
  return request({
    url: '/api/pilotage/worksheets/records/tugboat/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

export const fetchPilotBoatWorksheetRecords = (params: WorksheetQueryParams) => {
  return request<ApiResponse<PaginatedResult<WorksheetPilotBoatRecord>>>({
    url: '/api/pilotage/worksheets/records/pilot-boat/page',
    method: 'get',
    params,
  })
}

export const exportPilotBoatWorksheetRecords = (params: Omit<WorksheetQueryParams, 'current' | 'size'>) => {
  return request({
    url: '/api/pilotage/worksheets/records/pilot-boat/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

export const fetchWorkSheetLedgerPage = (params: WorkSheetLedgerQuery) => {
  return request<ApiResponse<PaginatedResult<WorkSheetLedger>>>({
    url: '/api/pilotage/worksheets/page',
    method: 'get',
    params,
  })
}

export const fetchWorkSheetLedgerDetail = (id: number) => {
  return request<ApiResponse<WorkSheetLedger>>({
    url: `/api/pilotage/worksheets/${id}`,
    method: 'get',
  })
}

export const createWorkSheetLedger = (data: Partial<WorkSheetLedger>) => {
  return request<ApiResponse<number>>({
    url: '/api/pilotage/worksheets',
    method: 'post',
    data,
  })
}

export const updateWorkSheetLedger = (id: number, data: Partial<WorkSheetLedger>) => {
  return request<ApiResponse<void>>({
    url: `/api/pilotage/worksheets/${id}`,
    method: 'put',
    data,
  })
}

export const deleteWorkSheetLedger = (id: number) => {
  return request<ApiResponse<void>>({
    url: `/api/pilotage/worksheets/${id}`,
    method: 'delete',
  })
}

export const syncWorkSheetLedgerFromPlans = (params?: { startDate?: string; endDate?: string }) => {
  return request<ApiResponse<any>>({
    url: '/api/pilotage/worksheets/ledger/sync',
    method: 'post',
    params,
  })
}

