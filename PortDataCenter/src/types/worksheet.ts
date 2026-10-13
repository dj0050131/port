import type { PaginatedResult } from '@/types/common'

export interface WorksheetPilotRecord {
  planId: number
  planCode: string
  shipName?: string
  englishName?: string
  pilotName?: string
  time?: string
  berth?: string
  dynamic?: string
  grossTonnage?: string
  netTonnage?: string
  deadWeight?: string
  length?: string
  width?: string
  draft?: string
  nationality?: string
  callSign?: string
  imoNumber?: string
}

export type WorksheetPilotRecordPage = PaginatedResult<WorksheetPilotRecord>

export interface WorksheetDriverRecord {
  planId: number
  planCode: string
  shipName?: string
  englishName?: string
  driverName?: string
  licensePlate?: string
  time?: string
  berth?: string
  dynamic?: string
  grossTonnage?: string
  netTonnage?: string
  deadWeight?: string
  length?: string
  width?: string
  draft?: string
  nationality?: string
  callSign?: string
  imoNumber?: string
}

export interface WorksheetVehicleRecord {
  planId: number
  planCode: string
  shipName?: string
  englishName?: string
  vehicleCode?: string
  licensePlate?: string
  driverName?: string
  time?: string
  berth?: string
  dynamic?: string
}

export interface WorksheetTugboatRecord {
  planId: number
  planCode: string
  shipName?: string
  englishName?: string
  tugboatName?: string
  tugboatCallSign?: string
  bollardPull?: string
  time?: string
  berth?: string
  dynamic?: string
}

export interface WorksheetPilotBoatRecord {
  planId: number
  planCode: string
  shipName?: string
  englishName?: string
  boatName?: string
  boatCallSign?: string
  time?: string
  berth?: string
  dynamic?: string
}

export type WorksheetWorkStatus = 'NOT_STARTED' | 'ONGOING' | 'COMPLETED' | 'ABNORMAL'

export interface WorkSheetLedger {
  id: number
  worksheetCode: string
  planStartTime?: string
  planEndTime?: string
  startLocation?: string
  endLocation?: string
  assignedTugboats?: string
  assignedPilotBoats?: string
  assignedPilotId?: number
  assignedDriverId?: number
  boardingTime?: string
  leavingTime?: string
  actualStartTime?: string
  actualEndTime?: string
  operationTypeFlag?: string
  workStatus?: WorksheetWorkStatus
  completionQualityScore?: number
  specialSituationRecord?: string
  customerFeedback?: string
  workDuration?: number
  createdAt?: string
  updatedAt?: string
}

export interface WorkSheetLedgerQuery {
  current: number
  size: number
  keyword?: string
  workStatus?: WorksheetWorkStatus | ''
  startDate?: string
  endDate?: string
}

