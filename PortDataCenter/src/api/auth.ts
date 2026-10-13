import request from '@/utils/request'

export interface LoginRequest {
  account: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  account: string
  username: string
  userType: string
  status?: string
  currentStatus?: string
  phone?: string
  email?: string
  avatar?: string
  permissions?: string[]
  // Backend LoginVO does not currently return name/department,
  // so we will omit them or they will be undefined
}

export interface UserInfo {
  id: number
  account: string
  userCode?: string
  username: string
  userType: string
  phone?: string
  email?: string
  avatar?: string
  currentStatus: string
  permissions?: string[]
  createTime?: string
  createdAt?: string
  lastLoginAt?: string
  // Add name/department if/when backend adds them to UserInfoVO
  name?: string
  department?: string
  position?: string
}
export const login = (data: LoginRequest) => {
  return request<LoginResponse>({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}
export const logout = () => {
  return request<void>({
    url: '/api/auth/logout',
    method: 'post'
  })
}
export const getCurrentUser = () => {
  return request<UserInfo>({
    url: '/api/auth/current',
    method: 'get'
  })
}
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request<void>({
    url: '/api/auth/change-password',
    method: 'post',
    params: data
  })
}
export const updateProfile = (data: any) => {
  return request<UserInfo>({
    url: '/api/users/profile',
    method: 'put',
    data
  })
}