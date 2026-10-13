/** 将头像字段转换为可访问的图片 URL */
export function resolveAvatarUrl(avatar?: string | null): string {
  if (!avatar) return ''
  const raw = String(avatar).trim()
  if (!raw) return ''

  if (/^https?:\/\//i.test(raw) || raw.startsWith('//')) return raw

  if (raw.startsWith('/uploads/')) {
    const filename = raw.slice('/uploads/'.length)
    return filename ? `/api/files/raw/${encodeURIComponent(filename)}` : ''
  }

  if (raw.startsWith('/api/files/raw/')) return raw

  if (raw.startsWith('/')) return raw

  if (raw.startsWith('uploads/')) {
    const filename = raw.slice('uploads/'.length)
    return filename ? `/api/files/raw/${encodeURIComponent(filename)}` : ''
  }

  return `/api/files/raw/${encodeURIComponent(raw)}`
}

/** 获取 public 目录静态资源 URL */
export function resolvePublicAsset(path: string): string {
  const base = (import.meta as any).env?.BASE_URL || '/'
  const b = String(base).endsWith('/') ? String(base) : `${base}/`
  return b + String(path).replace(/^\//, '')
}

