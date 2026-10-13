export const ANCHORAGE_TYPE_LABELS: Record<string, string> = {
  QUARANTINE: '检疫',
  WAITING: '待泊',
  EMERGENCY: '应急',
  SPARE: '备用',
  PILOTAGE: '引航'
}

export const ANCHORAGE_STATUS_LABELS: Record<string, string> = {
  AVAILABLE: '可用',
  OCCUPIED: '占用',
  RESTRICTED: '受限',
  MAINTENANCE: '维护'
}

export function anchorageTypeLabel(value?: string | null): string {
  if (!value) return '-'
  const v = String(value).trim()
  return ANCHORAGE_TYPE_LABELS[v] || v
}

export function anchorageStatusLabel(value?: string | null): string {
  if (!value) return '-'
  const v = String(value).trim()
  return ANCHORAGE_STATUS_LABELS[v] || v
}

