export function formatDateTime(input) {
  if (!input) return ''
  const d = typeof input === 'string' || typeof input === 'number' ? new Date(input) : input
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatDate(input) {
  if (!input) return ''
  const d = typeof input === 'string' || typeof input === 'number' ? new Date(input) : input
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 解析日期时间文本为毫秒时间戳 */
export function parseDateTimeText(text) {
  if (!text) return null
  const s = String(text).trim()
  if (!s) return null
  const normalized = s.replace(/\//g, '-').replace('T', ' ')
  const m = normalized.match(/^(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?$/)
  if (!m) return null
  const y = Number(m[1])
  const mo = Number(m[2])
  const d = Number(m[3])
  const hh = Number(m[4])
  const mm = Number(m[5])
  const ss = m[6] ? Number(m[6]) : 0
  const dt = new Date(y, mo - 1, d, hh, mm, ss)
  if (Number.isNaN(dt.getTime())) return null
  return dt.getTime()
}

