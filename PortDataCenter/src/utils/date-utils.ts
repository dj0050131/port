import { computed, onBeforeUnmount, ref } from 'vue'

const pad = (value: number) => value.toString().padStart(2, '0')

export const formatDate = (date: string | number | Date, withYear = true) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  return withYear ? `${year}-${month}-${day}` : `${month}-${day}`
}

export const formatTime = (date: string | number | Date, withSeconds = true) => {
  const d = new Date(date)
  const hour = pad(d.getHours())
  const minute = pad(d.getMinutes())
  const second = pad(d.getSeconds())
  return withSeconds ? `${hour}:${minute}:${second}` : `${hour}:${minute}`
}

export const formatDateTime = (date: string | number | Date) => {
  return `${formatDate(date)} ${formatTime(date)}`
}

export const fromNow = (date: string | number | Date) => {
  const diff = Date.now() - new Date(date).getTime()
  const minutes = Math.floor(diff / 60_000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

export const useNow = (interval = 1000) => {
  const now = ref(new Date())
  const timer = window.setInterval(() => {
    now.value = new Date()
  }, interval)

  onBeforeUnmount(() => {
    window.clearInterval(timer)
  })

  return computed(() => ({
    date: formatDate(now.value),
    time: formatTime(now.value),
    raw: now.value,
  }))
}

