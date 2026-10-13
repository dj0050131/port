import request from '@/utils/request'

export interface User {
  id: number
  username: string
  name: string
  role: string
  department: string
  email?: string
  phone?: string
  avatar?: string
  position?: string
  enabled: boolean
  lastLoginAt?: string
  createdAt: string
}

export interface UserQueryParams {
  keyword?: string
  role?: string
  enabled?: boolean
  page: number
  size: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
export const getUserList = (params: UserQueryParams) => {
  return request<PageResult<User>>({
    url: '/api/users/list',
    method: 'get',
    params
  })
}
export const getUserById = (id: number) => {
  return request<User>({
    url: `/api/users/${id}`,
    method: 'get'
  })
}
export const createUser = (data: any) => {
  return request<User>({
    url: '/api/users',
    method: 'post',
    data
  })
}
export const updateUser = (id: number, data: any) => {
  return request<User>({
    url: `/api/users/${id}`,
    method: 'put',
    data
  })
}
export const deleteUser = (id: number) => {
  return request<void>({
    url: `/api/users/${id}`,
    method: 'delete'
  })
}
export const resetPassword = (id: number) => {
  return request<void>({
    url: `/api/users/${id}/reset-password`,
    method: 'post'
  })
}
export const toggleUserStatus = (id: number, enabled: boolean) => {
  return request<void>({
    url: `/api/users/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}
export const updateAvatar = (avatar: string) => {
  return request<void>({
    url: '/api/users/avatar',
    method: 'put',
    params: { avatar }
  })
}
export const updateProfile = (data: any) => {
  return request<User>({
    url: '/api/users/profile',
    method: 'put',
    data
  })
}
export const changePassword = (oldPassword: string, newPassword: string) => {
  return request<void>({
    url: '/api/auth/change-password',
    method: 'post',
    params: { oldPassword, newPassword }
  })
}
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request<string>({
    url: '/api/files/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

