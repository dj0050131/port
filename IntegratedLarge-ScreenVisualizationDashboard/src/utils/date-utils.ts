import { computed, onBeforeUnmount, ref } from 'vue'

export const formatDateTime = (date: Date | number | string, withSeconds = true) => {
  const target = new Date(date)
  const yyyy = target.getFullYear()
  const mm = String(target.getMonth() + 1).padStart(2, '0')
  const dd = String(target.getDate()).padStart(2, '0')
  const hh = String(target.getHours()).padStart(2, '0')
  const mi = String(target.getMinutes()).padStart(2, '0')
  const ss = String(target.getSeconds()).padStart(2, '0')
  return withSeconds ? `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}` : `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

export const getFutureTime = (minutesAhead: number) => {
  const now = Date.now()
  return new Date(now + minutesAhead * 60 * 1000)
}

export const getPastTime = (minutesAgo: number) => {
  const now = Date.now()
  return new Date(now - minutesAgo * 60 * 1000)
}

export const formatHM = (date: Date | number | string) => {
  const target = new Date(date)
  const hh = String(target.getHours()).padStart(2, '0')
  const mm = String(target.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

export const useNow = (interval = 1000) => {
  const now = ref(formatDateTime(new Date()))
  const update = () => {
    now.value = formatDateTime(new Date())
  }

  const timer = setInterval(update, interval)

  onBeforeUnmount(() => {
    clearInterval(timer)
  })

  return computed(() => now.value)
}

export const timeAgo = (date: string | number | Date) => {
  const diff = Date.now() - new Date(date).getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  return `${days} 天前`
}
