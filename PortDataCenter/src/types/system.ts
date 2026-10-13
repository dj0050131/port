import type { ApiResponse } from './common'

export type UserRole = 'administrator' | 'dispatcher' | 'finance' | 'pilot' | 'viewer'

export interface UserProfile {
  id: string
  name: string
  role: UserRole
  department: string
  avatar?: string
  permissions: string[]
  lastLoginAt: string
  locale?: string
  theme?: 'dark' | 'light'
}

export interface LoginPayload {
  username: string
  password: string
  remember?: boolean
}

export interface LoginResult {
  token: string
  profile: UserProfile
}

export type LoginResponse = ApiResponse<LoginResult>

export interface LogoutResult {
  success: boolean
}

export type LogoutResponse = ApiResponse<LogoutResult>

export interface NavigationItem {
  name: string
  path: string
  icon: string
  children?: NavigationItem[]
  permission?: string
}

export interface SystemNotification {
  id: string
  title: string
  content: string
  level: 'info' | 'warning' | 'error'
  createdAt: string
  read: boolean
}

export interface SystemHealthMetric {
  module: string
  status: 'healthy' | 'degraded' | 'offline'
  uptime: number
  lastIncident?: string
}

export interface SystemConfig {
  theme: 'dark' | 'light'
  language: string
  notifications: boolean
  autoRefresh: boolean
  dataRetention: number
}

export type SystemNotificationResponse = ApiResponse<SystemNotification[]>
export type SystemHealthResponse = ApiResponse<SystemHealthMetric[]>
export type SystemConfigResponse = ApiResponse<SystemConfig>

export interface SystemParameter {
  id?: number
  paramGroup: string
  paramKey: string
  paramValue: string
  paramName?: string
  isSystem?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

export interface SystemParameterQuery {
  current: number
  size: number
  paramGroup?: string
  keyword?: string
}

