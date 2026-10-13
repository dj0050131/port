import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common'
import { getToken, removeToken } from './storage'
import router from '@/router'

const service = axios.create({
  baseURL: '/', //代理模式
  timeout: 150000,
  withCredentials: true,
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  },
)

service.interceptors.response.use(
  (response): any => {
    const rt = response.config?.responseType
    if (rt === 'blob' || rt === 'arraybuffer') {
      return response
    }

    // 常规场景：后端返回的数据格式是 { code, message, data, timestamp }
    const res = response.data as ApiResponse<unknown>

    if (res.code === 200) {
      return res
    }

    // 其他 code 表示业务错误
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    console.error('Response error:', error)

    const isDownloadRequest = (error.config as any)?.responseType === 'blob' || (error.config as any)?.responseType === 'arraybuffer'
    const url = (error.config?.url || '') as string

    const tryParseBlobMessage = async () => {
      const data: any = error.response?.data
      if (!data) return ''
      // responseType=blob 时，后端可能返回的是 JSON 错误体，需要解析出来展示
      if (typeof Blob !== 'undefined' && data instanceof Blob) {
        try {
          const text = await data.text()
          try {
            const json = JSON.parse(text)
            return json?.message || json?.msg || text
          } catch {
            return text
          }
        } catch {
          return ''
        }
      }
      return (data as any)?.message || ''
    }

    if (error.response?.status === 401) {
      // 下载/导出场景：即使返回 401，也不要把用户强制登出（容易误伤）
      if (isDownloadRequest || url.includes('/export')) {
        const serverMsg = await tryParseBlobMessage()
        ElMessage.error(serverMsg ? `导出失败：${serverMsg}` : '导出失败：登录已过期或无权限，请重新登录后重试')
        return Promise.reject(error)
      }

      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(error)
    }

    if (error.response?.status === 403) {
      const serverMsg = await tryParseBlobMessage()
      ElMessage.error(serverMsg ? `无权限：${serverMsg}` : '无权限访问')
      return Promise.reject(error)
    }

    const message = (error.response?.data as any)?.message || error.message || '请求失败，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export const request = <T>(config: AxiosRequestConfig) => {
  return service.request<ApiResponse<T>, ApiResponse<T>>(config)
}

export default request