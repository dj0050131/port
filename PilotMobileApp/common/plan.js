export function planStatusLabel(status) {
  const s = (status || '').toString().toUpperCase()
  const map = {
    PLANNING: '计划中',
    PLANNED: '已计划',
    REPLAN_REQUIRED: '待重排',
    REPLANNED: '已重排',
    PUBLISHED: '已发布',
    CONFIRMED: '已确认',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    CERTIFICATED: '已签证',
    BILLED: '已计费',
    PAID: '已支付',
    CANCELLED: '已取消',
  }
  return map[s] || (status ? String(status) : '-')
}

export function canStartDispatch(status) {
  const s = (status || '').toString().toUpperCase()
  // 服务端 startPlan 允许从 CONFIRMED 开始；部分情况 PUBLISHED 也允许
  return s === 'PUBLISHED' || s === 'CONFIRMED'
}

export function canEndDispatch(status) {
  const s = (status || '').toString().toUpperCase()
  return s === 'ONGOING'
}

export function confirmStatusLabel(status) {
  const s = (status || '').toString().toUpperCase()
  const map = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
    SYSTEM_CANCELLATION: '系统取消',
  }
  return map[s] || (status ? String(status) : '待确认')
}

export function canConfirmPlan(planStatus, confirmStatus) {
  const ps = (planStatus || '').toString().toUpperCase()
  const cs = (confirmStatus || '').toString().toUpperCase()
  return (ps === 'PUBLISHED' || ps === 'CONFIRMED') && (cs === 'PENDING' || cs === 'REJECTED' || cs === '')
}

export function canCancelPlanConfirm(planStatus, confirmStatus) {
  const ps = (planStatus || '').toString().toUpperCase()
  const cs = (confirmStatus || '').toString().toUpperCase()
  return (ps === 'PUBLISHED' || ps === 'CONFIRMED') && cs === 'CONFIRMED'
}

