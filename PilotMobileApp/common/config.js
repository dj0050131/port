const BASE_URL_STORAGE_KEY = 'PORT_PILOTAGE_API_BASE_URL'

// 真机运行时请在登录页配置电脑局域网 IP
export const DEFAULT_BASE_URL = ''

function normalizeBaseUrl(url) {
  const v = (url || '').trim()
  if (!v) return ''
  return v.replace(/\/+$/, '')
}

export function isLikelyVmwareBaseUrl(url) {
  const v = normalizeBaseUrl(url)
  return /^http(s)?:\/\/(192\.168\.\d+\.\d+|10\.\d+\.\d+\.\d+|172\.(1[6-9]|2\d|3[01])\.\d+\.\d+)(:\d+)?$/i.test(v)
}

export function getBaseUrl() {
  try {
    const v = uni.getStorageSync(BASE_URL_STORAGE_KEY)
    return normalizeBaseUrl(v) || DEFAULT_BASE_URL
  } catch (e) {
    return DEFAULT_BASE_URL
  }
}

export function setBaseUrl(url) {
  try {
    uni.setStorageSync(BASE_URL_STORAGE_KEY, normalizeBaseUrl(url))
  } catch (e) {
    // ignore
  }
}

