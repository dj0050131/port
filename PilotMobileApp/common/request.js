import { getBaseUrl } from './config'
import { getToken } from './storage'

export function request(options) {
  const token = getToken()
  const headers = Object.assign(
    {
      'Content-Type': 'application/json',
      'X-Request-Timestamp': new Date().toISOString(),
    },
    options.header || {},
  )
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    const baseUrl = getBaseUrl()
    uni.request({
      url: options.url.startsWith('http') ? options.url : `${baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success: (res) => {
        const payload = res.data
        if (!payload || (payload.code !== 0 && payload.code !== 200)) {
          const msg = (payload && payload.message) || `请求失败(${res.statusCode})`
          reject(new Error(msg))
          return
        }
        resolve(payload)
      },
      fail: (err) => {
        const hint =
          baseUrl && baseUrl.indexOf('127.0.0.1') >= 0
            ? '（提示：真机调试不要用 127.0.0.1，请在登录页设置为电脑局域网IP）'
            : ''
        const msg = (err && err.errMsg ? err.errMsg : '网络请求失败') + hint
        reject(new Error(msg))
      },
    })
  })
}

