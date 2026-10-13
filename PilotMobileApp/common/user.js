export function userTypeLabel(userType) {
  const t = (userType || '').toString().toUpperCase()
  const map = {
    PILOT: '引航员',
    DRIVER: '驾驶员',
    DISPATCHER: '调度员',
    ADMIN: '管理员',
    OPERATOR: '操作员',
  }
  return map[t] || (userType ? String(userType) : '-')
}

export function userStatusLabel(status) {
  const s = (status || '').toString().toUpperCase()
  const map = {
    ACTIVE: '正常',
    ENABLED: '正常',
    NORMAL: '正常',
    OK: '正常',
    TRUE: '正常',
    '1': '正常',
    DISABLED: '已禁用',
    FALSE: '已禁用',
    '0': '已禁用',
    LOCKED: '已锁定',
    PENDING: '待审核',
  }
  // 如果后端已经返回中文（如“正常/禁用”），直接透传
  if (status && /[\u4e00-\u9fa5]/.test(String(status))) return String(status)
  return map[s] || (status ? String(status) : '-')
}

