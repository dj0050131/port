import request from '@/utils/request'
import type { Users } from '@/types/bases/user'
import type { PaginatedResult } from '@/types/common';

export const createUser = (data: Users) => {
  return request<number>({
    url: '/api/base/userManager',
    method: 'post',
    data
  })
}

export const updateUser = (id: number, data: Users) => {
  return request<Users>({
    url: `/api/base/userManager/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id: number) => {
  return request<void>({
    url: `/api/base/userManager/${id}`,
    method: 'delete'
  })
}

export const getUserById = (id: number) => {
  return request<Users>({
    url: `/api/base/userManager/${id}`,
    method: 'get'
  })
}

export const getUserPage = (params: { current: number; size: number }) => {
  return request<PaginatedResult<Users>>({
    url: '/api/base/userManager/page',
    method: 'get',
    params
  })
}

export const getUserList = (userType?: string, status?: string) => {
  return request<Users[]>({
    url: '/api/base/userManager/list',
    method: 'get',
    params: { userType, status }
  })
}

export const searchUsers = (keyword: string) => {
  return request<Users[]>({
    url: '/api/base/userManager/search',
    method: 'get',
    params: { keyword }
  })
}

export const updateUserStatus = (id: number, status: string) => {
  return request<void>({
    url: `/api/base/userManager/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

export const resetUserPassword = (id: number, newPassword: string) => {
  return request<void>({
    url: `/api/base/userManager/${id}/password/reset`,
    method: 'patch',
    data: { newPassword }
  })
}

export const disableUser = (id: number) => {
  return request<void>({
    url: `/api/base/userManager/${id}/disable`,
    method: 'patch'
  })
}

export const offlineUser = (id: number) => {
  return request<void>({
    url: `/api/base/userManager/${id}/offline`,
    method: 'patch'
  })
}