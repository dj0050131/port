export const BERTH_TYPE_LABELS: Record<string, string> = {
  BULK: '散货',
  CONTAINER: '集装箱',
  GENERAL: '杂货',
  RO_RO: '滚装',
  OIL: '油品',
  CHEMICAL: '化工',
  LNG: '液化气',
  PASSENGER: '客运',
  FERRY: '渡运',
  TANKER: '油轮',
  MULTIPURPOSE: '多用途',
  OTHER: '其他'
}

export const BERTH_TYPE_OPTIONS = Object.entries(BERTH_TYPE_LABELS).map(([value, label]) => ({
  value,
  label
}))

export function berthTypeLabel(value?: string | null): string {
  if (!value) return '-'
  const v = String(value).trim()
  return BERTH_TYPE_LABELS[v] || v
}

