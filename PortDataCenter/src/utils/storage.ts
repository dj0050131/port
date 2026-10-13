const TOKEN_KEY = 'port-data-center-token'

export const storage = {
  get<T>(key: string, defaultValue?: T): T | undefined {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return defaultValue
      try {
        return JSON.parse(raw) as T
      } catch (e) {
        // 兼容历史/手工写入：可能直接存的是纯字符串（比如 token），不是 JSON
        return (raw as unknown as T) ?? defaultValue
      }
    } catch (error) {
      console.warn('[storage:get] parse error', error)
      return defaultValue
    }
  },
  set<T>(key: string, value: T) {
    localStorage.setItem(key, JSON.stringify(value))
  },
  remove(key: string) {
    localStorage.removeItem(key)
  },
  clear() {
    localStorage.clear()
  },
}

export const getToken = () => storage.get<string>(TOKEN_KEY, '')
export const setToken = (token: string) => storage.set(TOKEN_KEY, token)
export const removeToken = () => storage.remove(TOKEN_KEY)
export const clearToken = () => storage.remove(TOKEN_KEY)
