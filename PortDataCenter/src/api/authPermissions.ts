import request from '@/utils/request'
import type { PaginatedResult, ApiResponse } from '@/types/common'
import type { AuthPermission } from '@/types/authPermissions'

export const fetchAuthPermissionsPage = (params: { current: number; size: number; keyword?: string; permGroup?: string }) => {
  return request<ApiResponse<PaginatedResult<AuthPermission>>>({
    url: '/api/system/auth-permissions/page',
    method: 'get',
    params,
  })
}

export const fetchAuthPermissionsList = (permGroup?: string) => {
  return request<ApiResponse<AuthPermission[]>>({
    url: '/api/system/auth-permissions/list',
    method: 'get',
    params: permGroup ? { permGroup } : undefined,
  })
}

export const createAuthPermission = (data: Partial<AuthPermission>) => {
  return request<ApiResponse<number>>({
    url: '/api/system/auth-permissions',
    method: 'post',
    data,
  })
}

export const updateAuthPermission = (id: number, data: Partial<AuthPermission>) => {
  return request<ApiResponse<void>>({
    url: `/api/system/auth-permissions/${id}`,
    method: 'put',
    data,
  })
}

export const deleteAuthPermission = (id: number) => {
  return request<ApiResponse<void>>({
    url: `/api/system/auth-permissions/${id}`,
    method: 'delete',
  })
}

