const TOKEN_KEY = 'pilot_app_token'
const USER_KEY = 'pilot_app_user'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token || '')
}

export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getUser() {
  return uni.getStorageSync(USER_KEY) || null
}

export function setUser(user) {
  uni.setStorageSync(USER_KEY, user || null)
}

export function clearUser() {
  uni.removeStorageSync(USER_KEY)
}

