import { request } from '@/common/request'

export function getDashboard() {
  return request({
    url: '/api/pilot/app/dashboard',
    method: 'GET',
  })
}

export function getMyPlans(params) {
  return request({
    url: '/api/pilot/app/myPlans',
    method: 'GET',
    data: params || {},
  })
}

export function getMeProfile() {
  return request({
    url: '/api/pilot/app/meProfile',
    method: 'GET',
  })
}

export function getMyPlanConfirmStatus(planId) {
  return request({
    url: `/api/pilot/app/plans/${planId}/confirmStatus`,
    method: 'GET',
  })
}

export function confirmMyPlan(planId) {
  return request({
    url: `/api/pilot/app/plans/${planId}/confirm`,
    method: 'POST',
  })
}

export function cancelMyPlanConfirm(planId, reason) {
  const r = encodeURIComponent(reason || 'App取消确认')
  return request({
    url: `/api/pilot/app/plans/${planId}/cancelConfirm?reason=${r}`,
    method: 'PUT',
  })
}

export function getPlanDetail(planId) {
  return request({
    url: `/api/pilotage/plans/${planId}`,
    method: 'GET',
  })
}

export function startDispatch(planId, actualStartTime) {
  const ts = actualStartTime ? Number(actualStartTime) : 0
  return request({
    url: ts > 0 ? `/api/pilotage/plans/${planId}/start?actualStartTime=${ts}` : `/api/pilotage/plans/${planId}/start`,
    method: 'PUT',
  })
}

export function endDispatch(planId, actualEndTime) {
  const ts = actualEndTime ? Number(actualEndTime) : 0
  return request({
    url: ts > 0 ? `/api/pilotage/plans/${planId}/complete?actualEndTime=${ts}` : `/api/pilotage/plans/${planId}/complete`,
    method: 'PUT',
  })
}

export function getCertificateByPlanId(planId) {
  return request({
    url: `/api/pilotage/certificate/plan/${planId}`,
    method: 'GET',
  })
}

export function submitCertificate(certificateId) {
  return request({
    url: `/api/pilotage/certificate/submit/${certificateId}`,
    method: 'POST',
    data: {},
  })
}

export function pilotSignCertificate(certificateId) {
  return request({
    url: `/api/pilotage/certificate/sign/${certificateId}`,
    method: 'POST',
    data: {},
  })
}

