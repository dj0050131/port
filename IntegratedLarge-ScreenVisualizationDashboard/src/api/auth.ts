import { request } from '@/utils/request'

export interface LoginPayload {
  account: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  account: string
  userType: string
  status: string
  phone?: string
  email?: string
  avatar?: string
  permissions?: string[]
}

export interface UserInfoResponse {
  id: number
  account: string
  userCode: string
  userType: string
  username: string
  phone?: string
  email?: string
  currentStatus?: string
  avatar?: string
  department?: string
  position?: string
  lastLoginAt?: string
  createdAt?: string
  permissions?: string[]
}

export const login = (payload: LoginPayload) => {
  return request<LoginResponse>({
    url: '/api/auth/login',
    method: 'POST',
    data: payload,
  })
}

export const getCurrentUser = () => {
  return request<UserInfoResponse>({
    url: '/api/auth/current',
    method: 'GET',
  })
}

