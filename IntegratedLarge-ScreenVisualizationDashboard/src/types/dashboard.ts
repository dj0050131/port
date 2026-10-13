export type ShipType = 'CARGO' | 'CONTAINER' | 'TANKER' | 'LNG' | 'CHEMICAL' | 'RO-RO' | 'GAS' | 'CAR'
export type ShipStatus = 'ARRIVED' | 'DEPARTED' | 'ANCHORED' | 'DOCKING' | 'UNDERWAY'
// ARRIVED → 已到港
// DEPARTED → 已离港
// ANCHORED → 锚泊中
// DOCKING → 靠泊中
// UNDERWAY → 航行中
export type PilotPlanStatus = 'PLANNING' | 'PLANNED' | 'PUBLISHED' | 'CONFIRMED' | 'ONGOING' | 'COMPLETED' | 'CERTIFICATED' | 'BILLED' | 'PAID' | 'CANCELLED'
export type AlertLevel = 'critical' | 'warning' | 'info' | 'normal'
export type SystemStatus = 'healthy' | 'degraded' | 'offline'
export type BerthState = 'occupied' | 'available' | 'maintenance'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface GeoCoordinate {
  lng: number
  lat: number
}

export interface ShipDynamic {
  id: string
  name: string
  imo: string
  type: ShipType
  status: ShipStatus
  speed: number
  heading: number
  destination: string
  eta: string
  etd: string
  position: GeoCoordinate
  pilotRequired: boolean
  berthId?: string
}

export interface PilotPlan {
  id: string
  shipId: string
  shipName: string
  pilotName: string
  schedule: string
  status: PilotPlanStatus
  route: GeoCoordinate[]
  remarks?: string
}

export interface WeatherTide {
  weather: {
    temperature: number
    humidity: number
    visibility: number
    windSpeed: number
    windDirection: string
    condition: string
    pressure: number
  }
  tide: {
    currentHeight: number
    nextHighTide: string
    nextLowTide: string
    surgeWarning: boolean
  }
  forecast: Array<{ time: string; temperature: number; tide: number; condition?: string; wind?: string; windLevel?: number }>
}

export interface FeeBreakdownItem {
  label: string
  value: number
}

export interface FeeStatistics {
  total: number
  currency: string
  transactions: number
  growth: number
  breakdown: FeeBreakdownItem[]
  trend: Array<{ time: string; value: number }>
  category: Array<{ name: string; value: number }>
}

export interface AlertItem {
  id: string
  level: AlertLevel
  message: string
  detail?: string
  timestamp: string
  acknowledged: boolean
  alertType?: string
}

export interface SystemStatusItem {
  moduleName: string
  status: SystemStatus
  lastUpdate: string
  uptime: number
  count : number
  total: number
}

export interface BerthStatus {
  id: string
  name: string
  position: GeoCoordinate
  status: BerthState
  shipId?: string
}

export interface PilotRouteSegment {
  from: GeoCoordinate
  to: GeoCoordinate
  shipId: string
}

export interface PortMapPayload {
  ships: ShipDynamic[]
  routes: PilotRouteSegment[]
  berths: BerthStatus[]
}

export interface TrafficMetric {
  id: string
  label: string
  value: number
  unit?: string
  trend?: number
  subLabel?: string
}

export interface TrafficSourceItem {
  rank: number
  nationality: string
  value: number
  ratio: number
}

export interface DeviceStatusItem {
  name: string
  category: string
  onlineRate: number
  alarms: number
}

export interface TrafficOverview {
  metrics: TrafficMetric[]
  sourceRanking: TrafficSourceItem[]
  deviceStatuses: DeviceStatusItem[]
  trend: Array<{ time: string; inbound: number; outbound: number; totalLoad: number }>
  typeDistribution: Array<{ name: string; value: number }>
  geoSpots: Array<{ name: string; value: [number, number, number] }>
}

export interface PilotTaskItem {
  id: string
  shipName: string
  pilot: string
  eta: string
  etd: string
  status: string
}

export interface MaintenanceLog {
  id: string
  moduleName: string
  level: AlertLevel
  message: string
  timestamp: string
}

export interface OperationsOverview {
  kpis: Array<{ label: string; value: number; unit?: string; change?: number }>
  pilotTasks: PilotTaskItem[]
  energyTrend: Array<{ time: string; value: number }>
  berthUsage: Array<{ name: string; value: number }>
}

// 系统性能指标类型
export interface SystemMetricsItem {
    timestamp: string;        // 时间戳
    cpuUsage: number;         // CPU使用率
    memoryUsage: number;      // 内存使用率
    diskUsage: number;        // 磁盘使用率
    networkIn: number;        // 网络流入
    networkOut: number;       // 网络流出
    loadAverage: number;      // 系统负载
    responseTime: number;     // 响应时间
}

// 系统性能汇总统计
export interface SystemPerformanceSummary {
    avgCpuUsage: number;      // 平均CPU使用率
    avgMemoryUsage: number;   // 平均内存使用率
    peakLoad: number;         // 峰值负载
    avgResponseTime: number;  // 平均响应时间
}

// 系统性能数据（包含指标和汇总统计）
export interface SystemPerformanceData {
    metrics: SystemMetricsItem[];        // 性能指标列表，用于趋势图
    summary: SystemPerformanceSummary;   // 性能汇总统计，用于关键指标展示
}