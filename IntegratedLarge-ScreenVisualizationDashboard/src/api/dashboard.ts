import request from '@/utils/request'
import type {
    AlertItem,
    BerthStatus,
    FeeStatistics,
    OperationsOverview,
    PilotPlan,
    PortMapPayload,
    ShipDynamic, SystemPerformanceData,
    SystemStatusItem,
    TrafficOverview,
    WeatherTide,
} from '@/types/dashboard'
import type { ApiResponse } from '@/types/dashboard'

// Real API implementations
// Note: Backend must implement these endpoints based on jiekou.txt specification

export const getShipDynamics = async (): Promise<ApiResponse<ShipDynamic[]>> => {
  return request.get<any, ApiResponse<ShipDynamic[]>>('/api/monitor/dashboard/shipDynamics') //船舶 //综合主屏
}

export const getPilotPlans = async (): Promise<ApiResponse<PilotPlan[]>> => {  //dashboard //综合主屏
  return request.get<any, ApiResponse<PilotPlan[]>>('/api/monitor/dashboard/pilotTasks')
}

export const getWeatherAndTide = async (): Promise<ApiResponse<WeatherTide>> => {
  return request.get<any, ApiResponse<WeatherTide>>('/api/public/weather/realtime')
}

export const getFeeStatistics = async (): Promise<ApiResponse<FeeStatistics>> => {
  return request.get<any, ApiResponse<FeeStatistics>>('/api/monitor/dashboard/financialStats')
}

export const getAlertList = async (): Promise<ApiResponse<AlertItem[]>> => {
  return request.get<any, ApiResponse<AlertItem[]>>('/api/monitor/dashboard/alerts')
}

export const getSystemStatus = async (): Promise<ApiResponse<SystemStatusItem[]>> => {
  return request.get<any, ApiResponse<SystemStatusItem[]>>('/api/monitor/dashboard/systemStatus')
}

export const getBerthStatus = async (): Promise<ApiResponse<BerthStatus[]>> => {
  return request.get<any, ApiResponse<BerthStatus[]>>('/api/monitor/dashboard/berthStatus')
}

export const getPortMapPayload = async (): Promise<ApiResponse<PortMapPayload>> => {
  return request.get<any, ApiResponse<PortMapPayload>>('/api/monitor/dashboard/portMap')
}

export const getTrafficOverview = async (): Promise<ApiResponse<TrafficOverview>> => {
  return request.get<any, ApiResponse<TrafficOverview>>('/api/monitor/dashboard/trafficStats')
}

export const getOperationsOverview = async (): Promise<ApiResponse<OperationsOverview>> => {        //辅助监控
  return request.get<any, ApiResponse<OperationsOverview>>('/api/monitor/dashboard/operationsStats')
}

export const getPilotageStat = async (): Promise<ApiResponse<any>> => {
  return request.get<any, ApiResponse<any>>('/api/monitor/dashboard/pilotageStat')
}

export const getThroughputStat = async (): Promise<ApiResponse<any>> => {
  return request.get<any, ApiResponse<any>>('/api/monitor/dashboard/throughputStat')
}

export const getSystemLogs = async (): Promise<ApiResponse<any[]>> => {
  return request.get<any, ApiResponse<any[]>>('/api/console/systemLogs')
}

export const getUserStats = async (): Promise<ApiResponse<any>> => {
  return request.get<any, ApiResponse<any>>('/api/console/userStats')
}

export const getSystemPerformanceData = async (): Promise<ApiResponse<any>> => {
    return request.get<any, ApiResponse<SystemPerformanceData>>('/api/monitor/dashboard/systemPerformanceData')
}