import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchSystemHealth, fetchSystemNotifications, saveSystemConfig } from '@/api/system'
import type { SystemConfig, SystemHealthMetric, SystemNotification } from '@/types/system'

export const useSystemStore = defineStore('system', () => {
  const notifications = ref<SystemNotification[]>([])
  const health = ref<SystemHealthMetric[]>([])
  const config = ref<SystemConfig>({
    theme: 'dark',
    language: 'zh-CN',
    notifications: true,
    autoRefresh: true,
    dataRetention: 30,
  })
  const loading = ref(false)
  const saving = ref(false)

  const fetchNotifications = async () => {
    try {
      const res = await fetchSystemNotifications()
      if (res.code !== 200) throw new Error(res.message)
      notifications.value = res.data
    } catch (error) {
      console.error('[bases] notifications error', error)
      ElMessage.error('系统通知获取失败')
    }
  }

  const fetchHealth = async () => {
    try {
      const res = await fetchSystemHealth()
      if (res.code !== 200) throw new Error(res.message)
      health.value = res.data
    } catch (error) {
      console.error('[bases] health error', error)
      ElMessage.error('系统健康状态获取失败')
    }
  }

  const fetchAll = async () => {
    loading.value = true
    await Promise.all([fetchNotifications(), fetchHealth()])
    loading.value = false
  }

  const saveConfig = async (payload: SystemConfig) => {
    saving.value = true
    try {
      const res = await saveSystemConfig(payload)
      if (res.code !== 200) throw new Error(res.message)
      config.value = res.data
      ElMessage.success('配置保存成功')
    } catch (error) {
      console.error('[bases] save config error', error)
      ElMessage.error('配置保存失败，请稍后重试')
    } finally {
      saving.value = false
    }
  }

  return {
    notifications,
    health,
    config,
    loading,
    saving,
    fetchAll,
    saveConfig,
    fetchNotifications,
  }
})

