import type { ApiResponse } from './common'

export interface DashboardMetric {
  id: string
  label: string
  value: number
  unit?: string
  delta?: number
  trend?: number[]
  color?: string
}

export interface OperationSummary {
  inboundShips: number
  outboundShips: number
  pilotMissions: number
  revenue: number
  utilization: number
  alerts: number
}

export interface AlertMessage {
  id: string
  type: 'safety' | 'weather' | 'equipment' | 'finance'
  level: 'info' | 'warning' | 'critical'
  title: string
  content: string
  timestamp: string
}

export interface TrendPoint {
  time: string
  value: number
}

export interface ResourceUsageItem {
  name: string
  value: number
}

export interface DashboardData {
  metrics: DashboardMetric[]
  operations: OperationSummary
  alerts: AlertMessage[]
  throughputTrend: TrendPoint[]
  pilotageTrend: TrendPoint[]
  feeTrend: TrendPoint[]
  resourceUsage: ResourceUsageItem[]
}

// Backend Types
export interface PilotageStatVO {
  totalRequests: number
  pendingRequests: number
  approvedRequests: number
  completedPlans: number
  todayPlans: number
  activePilots: number
  trends?: TrendPoint[]
}

export interface FinancialStatVO {
  totalRevenue: number
  monthRevenue: number
  todayRevenue: number
  totalInvoices: number
  unpaidInvoices: number
  unpaidAmount: number
  overdueAmount: number
  trends?: TrendPoint[]
}

export interface ThroughputStatVO {
  inboundShips: number
  outboundShips: number
  cargoWeight: number
  updateTime: string
  trends: TrendPoint[]
}

export interface ResourceUsageStatVO {
  pilotBoats: number
  tugboats: number
  pilots: number
  vehicles: number
  updateTime: string
  trends: TrendPoint[]
}

export interface AlertStatVO {
  totalCount: number
  criticalCount: number
  warningCount: number
  infoCount: number
  recentAlerts: AlertMessage[]
  updateTime: string
  trends: TrendPoint[]
}

export interface BackendDashboardOverview {
  pilotage: PilotageStatVO
  financial: FinancialStatVO
  throughput?: ThroughputStatVO
  resourceUsage?: ResourceUsageStatVO
  alerts?: AlertStatVO
}

// 甘特图相关类型定义
export interface GanttResource {
  id: number
  name: string
  type: string
  status: string
  description?: string
  level?: string
}

export interface GanttTask {
  id: number
  name: string
  startDate: string
  endDate: string
  resourceId: number
  resourceType: string
  taskType: string
  planId: number
  description?: string
  progress: number
}

export interface GanttTimeline {
  startDate: string
  endDate: string
  timeUnit: string
  timeInterval: number
  showWeekends: boolean
  showHolidays: boolean
}

export interface GanttChartData {
  resources: GanttResource[]
  tasks: GanttTask[]
  timeline: GanttTimeline
}

export interface ResourceTimeline {
  resourceId: number
  resourceName: string
  resourceType: string
  tasks: ResourceTask[]
  startDate: string
  endDate: string
  availabilityPercentage: number
}

export interface ResourceTask {
  taskId: number
  taskName: string
  planId: number
  startTime: string
  endTime: string
  status: string
  description?: string
  taskType: string
}

export interface ConflictCheckItem {
  resourceId: number
  resourceType: string
  startTime: string
  endTime: string
}

export interface ConflictCheckRequest {
  resources: ConflictCheckItem[]
  planId?: number
}

export interface ResourceConflict {
  resourceId: number
  resourceType: string
  resourceName: string
  startTime: string
  endTime: string
  conflictingTaskId: number
  conflictingTaskName: string
  description: string
  severity: string
}

export interface ResourceUsageOverview {
  pilots: Record<string, any>
  tugboats: Record<string, any>
  pilotBoats: Record<string, any>
  drivers: Record<string, any>
  vehicles: Record<string, any>
  totalResources: number
}

export type DashboardResponse = ApiResponse<DashboardData>