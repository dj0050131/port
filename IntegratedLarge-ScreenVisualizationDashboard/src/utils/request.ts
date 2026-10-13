import axios, { type AxiosError, type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/dashboard'
import { getToken } from './storage'

const DEFAULT_LOCAL_API_BASE = 'http://127.0.0.1:8099'

function resolveBaseURL() {
  const raw = import.meta.env.VITE_APP_BASE_API as string | undefined
  return raw || DEFAULT_LOCAL_API_BASE
}

const service: AxiosInstance = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 15_000,
})

service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (!config.headers) {
      config.headers = {}
    }
    config.headers['X-Request-Timestamp'] = new Date().toISOString()
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const payload = response.data
    if (payload.code !== 0 && payload.code !== 200) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(payload)
    }
    return payload
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export const request = <T>(config: AxiosRequestConfig) => {
  return service.request<ApiResponse<T>>(config)
}

export default service
