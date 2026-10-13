import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, type LoginPayload } from '@/api/auth'
import { clearToken, getToken, setToken, storage } from '@/utils/storage'

interface UserProfile {
  id: number
  account: string
  name: string
  role: string
  userType: string
  lastLogin?: string
  avatar?: string
  permissions?: string[]
}

const USER_STORAGE_KEY = 'ppbs.auth.user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '' as string,
    user: null as UserProfile | null,
    hydrated: false,
    loading: false,
    error: '' as string,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    displayName: (state) => state.user?.name ?? state.user?.account ?? '访客',
  },
  actions: {
    hydrate() {
      if (this.hydrated) return

      const storedToken = getToken()
      const storedUser = storage.get<UserProfile>(USER_STORAGE_KEY, null) ?? null

      if (storedToken) this.token = storedToken
      if (storedUser) this.user = storedUser

      this.hydrated = true

      if (this.token && !this.user) {
        this.fetchProfile().catch((error) => {
          console.warn('[auth] Failed to fetch profile', error)
        })
      }
    },
    async login(payload: LoginPayload) {
      this.loading = true
      this.error = ''

      try {
        if (!payload.account || !payload.password) {
          throw new Error('请输入账号与密码')
        }

        const response = await loginApi(payload)
        const data = response.data

        const profile: UserProfile = {
          id: data.userId,
          account: data.account,
          name: data.username || data.account,
          role: data.userType || '用户',
          userType: data.userType,
          lastLogin: new Date().toISOString(),
          avatar: data.avatar,
          permissions: data.permissions ?? [],
        }

        this.user = profile
        this.token = data.token

        setToken(data.token)
        storage.set(USER_STORAGE_KEY, profile)
      } catch (error) {
        const message = error instanceof Error ? error.message : '登录失败，请稍后重试'
        this.error = message
        throw new Error(message)
      } finally {
        this.loading = false
      }
    },
    async fetchProfile() {
      if (!this.token) return
      const response = await getCurrentUser()
      const data = response.data

      const profile: UserProfile = {
        id: data.id,
        account: data.account,
        name: data.username || data.account,
        role: data.userType || '用户',
        userType: data.userType,
        lastLogin: data.lastLoginAt || undefined,
        avatar: data.avatar,
        permissions: data.permissions ?? [],
      }

      this.user = profile
      storage.set(USER_STORAGE_KEY, profile)
    },
    logout() {
      this.token = ''
      this.user = null

      clearToken()
      storage.remove(USER_STORAGE_KEY)
    },
  },
})

