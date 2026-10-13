import request from '@/utils/request'
import type {
  LoginPayload,
  LoginResponse,
  SystemConfig,
  UserProfile,
  SystemNotification,
  SystemHealthMetric,
  SystemParameter,
  SystemParameterQuery,
} from '@/types/system'
import type { PaginatedResult, ApiResponse } from '@/types/common'

interface JwtResponse {
  token: string
  username: string
  displayName: string
  role: string
  permissions: string[]
  department: string
  lastLoginAt: string
}

export const login = async (payload: LoginPayload): Promise<LoginResponse> => {
  const res = await request<JwtResponse>({
    url: '/auth/login',
    method: 'post',
    data: payload,
  })

  if (res.code !== 200) {
    throw new Error(res.message || '登录失败')
  }

  const profile: UserProfile = {
    id: res.data.username,
    name: res.data.displayName,
    role: res.data.role.toLowerCase() as UserProfile['role'],
    department: res.data.department,
    permissions: res.data.permissions,
    lastLoginAt: res.data.lastLoginAt,
    theme: 'dark',
  }
  return {
    code: res.code,
    message: res.message,
    timestamp: res.timestamp,
    data: {
      token: res.data.token,
      profile,
    },
  }
}

export const logout = () => {
  return request<{ success: boolean }>({
    url: '/auth/logout',
    method: 'post',
  })
}

export const fetchSystemNotifications = () => {
  return request<SystemNotification[]>({
    url: '/system/notifications',
    method: 'get',
  })
}

export const fetchSystemHealth = () => {
  return request<SystemHealthMetric[]>({
    url: '/system/health',
    method: 'get',
  })
}

export const saveSystemConfig = (config: SystemConfig) => {
  return request<SystemConfig>({
    url: '/system/config',
    method: 'post',
    data: config,
  })
}

export const fetchUserProfile = () => {
  return request<UserProfile>({
    url: '/system/profile',
    method: 'get',
  })
}

// ------------------- system_parameters -------------------

export const fetchSystemParameters = (params: SystemParameterQuery) => {
  return request<PaginatedResult<SystemParameter>>({
    url: '/api/system/parameters/page',
    method: 'get',
    params,
  })
}

export const createSystemParameter = (data: SystemParameter) => {
  return request<ApiResponse<number>>({
    url: '/api/system/parameters',
    method: 'post',
    data,
  })
}

export const updateSystemParameter = (id: number, data: Partial<SystemParameter>) => {
  return request<ApiResponse<void>>({
    url: `/api/system/parameters/${id}`,
    method: 'put',
    data,
  })
}

export const deleteSystemParameter = (id: number) => {
  return request<ApiResponse<void>>({
    url: `/api/system/parameters/${id}`,
    method: 'delete',
  })
}

export const fetchSystemParametersByGroup = (group: string) => {
  return request<SystemParameter[]>({
    url: `/api/system/parameters/group/${group}`,
    method: 'get',
  })
}
