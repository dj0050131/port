import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import { setToken, removeToken } from '@/utils/storage'
import type { UserInfo } from '@/api/auth'

export interface UserProfile extends UserInfo {
  token?: string
  permissions?: string[]
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const user = ref<UserProfile | null>(null)

  /**
   * 登录
   */
  const login = async (account: string, password: string) => {
    try {
      const res = await loginApi({ account, password })
      if (res.code === 200 && res.data) {
        const data = res.data
        token.value = data.token
        // Map LoginResponse to UserProfile
        // Since LoginVO lacks name/dept, we use username as fallback for name
        user.value = {
          id: data.userId,
          account: data.account,
          userCode: undefined,
          username: data.username,
          userType: data.userType,
          name: data.username, // Fallback
          phone: data.phone,
          email: data.email,
          avatar: data.avatar,
          currentStatus: data.currentStatus || data.status || '',
          token: data.token,
          permissions: data.permissions || [],
          // Initialize optional fields
          department: undefined,
          position: undefined,
          lastLoginAt: undefined,
          createTime: undefined,
          createdAt: undefined,
        }
        setToken(data.token)
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  /**
   * 登出
   */
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      user.value = null
      removeToken()
    }
  }

  /**
   * 恢复会话
   */
  const restoreSession = async (storedToken: string) => {
    token.value = storedToken
    try {
      const res = await getCurrentUser()
      if (res.code === 200 && res.data) {
        const data = res.data
        user.value = {
          id: data.id,
          account: data.account,
          userCode: data.userCode || undefined,
          username: data.username,
          userType: data.userType,
          name: data.username, // Fallback
          phone: data.phone,
          email: data.email,
          avatar: data.avatar,
          currentStatus: data.currentStatus,
          token: token.value,
          permissions: data.permissions || [],
          // Initialize optional fields
          department: data.department || undefined,
          position: data.position || undefined,
          lastLoginAt: data.lastLoginAt || undefined,
          createTime: data.createTime || data.createdAt || undefined,
          createdAt: data.createdAt || undefined,
        }
        return true
      }
      // Token无效，清除
      removeToken()
      token.value = ''
      return false
    } catch (error) {
      console.error('恢复会话失败:', error)
      removeToken()
      token.value = ''
      return false
    }
  }

  /**
   * 更新用户信息
   */
  const updateUser = (userData: Partial<UserProfile>) => {
    if (user.value) {
      user.value = { ...user.value, ...userData }
    }
  }

  return {
    token,
    user,
    login,
    logout,
    restoreSession,
    updateUser,
  }
})