<template>
  <view class="page">
    <view class="header">
      <view class="title">任务详情</view>
      <view class="sub">{{ plan.planCode || ('PLAN-' + planId) }}</view>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else class="card">
      <view class="kv"><text class="k">状态</text><text class="v">{{ statusText(plan.planStatus) }}</text></view>
      <view class="kv"><text class="k">确认</text><text class="v">{{ confirmText(confirmStatus) }}</text></view>
      <view class="kv"><text class="k">开始</text><text class="v">{{ format(plan.planStartTime) }}</text></view>
      <view class="kv"><text class="k">结束</text><text class="v">{{ format(plan.planEndTime) }}</text></view>
      <view class="kv"><text class="k">起点</text><text class="v">{{ plan.startLocationName || '-' }}</text></view>
      <view class="kv"><text class="k">终点</text><text class="v">{{ plan.endLocationName || '-' }}</text></view>
      <view class="kv"><text class="k">登轮</text><text class="v">{{ format(plan.boardingTime) }}</text></view>
      <view class="kv"><text class="k">离轮</text><text class="v">{{ format(plan.leavingTime) }}</text></view>
    </view>

    <view v-if="!loading" class="card">
      <view class="section-title">签证单</view>
      <view v-if="certLoading" class="hint">签证单加载中...</view>
      <view v-else-if="!certificate" class="hint">当前计划未生成签证单</view>
      <view v-else>
        <view class="kv"><text class="k">签证单号</text><text class="v">{{ certificate.certificateCode || ('CERT-' + certificate.id) }}</text></view>
        <view class="kv"><text class="k">提交状态</text><text class="v">{{ certificate.submitStatus || '-' }}</text></view>
        <view class="kv"><text class="k">签字时间</text><text class="v">{{ format(certificate.pilotSignTime) }}</text></view>
        <view class="kv"><text class="k">代理确认</text><text class="v">{{ format(certificate.agentConfirmTime) }}</text></view>
      </view>
    </view>

    <view v-if="!loading" class="actions">
      <button
        v-if="canConfirm(plan.planStatus, confirmStatus)"
        class="btn-secondary"
        :disabled="loading"
        @click="confirm(planId)"
      >
        计划确认
      </button>
      <button
        v-else-if="canCancelConfirm(plan.planStatus, confirmStatus)"
        class="btn-secondary btn-ghost"
        :disabled="loading"
        @click="cancelConfirm(planId)"
      >
        取消确认
      </button>
      <button
        v-if="canStart(plan.planStatus)"
        class="btn-primary"
        :disabled="loading"
        @click="start(planId)"
      >
        调度开始
      </button>
      <button
        v-else-if="canEnd(plan.planStatus)"
        class="btn-danger"
        :disabled="loading"
        @click="end(planId)"
      >
        调度结束
      </button>
    </view>

    <view v-if="!loading && certificate" class="actions">
      <button
        v-if="isPilot && certificate.submitStatus === 'UNSUBMITTED'"
        class="btn-secondary"
        :disabled="certLoading"
        @click="submitCert(certificate.id)"
      >
        提交签证单
      </button>
      <button
        v-if="isPilot && certificate.submitStatus === 'SUBMITTED' && !certificate.pilotSignTime"
        class="btn-primary"
        :disabled="certLoading"
        @click="signCert(certificate.id)"
      >
        签证单签字
      </button>
    </view>

    <view v-if="error" class="error">{{ error }}</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { cancelMyPlanConfirm, confirmMyPlan, endDispatch, getMyPlanConfirmStatus, getPlanDetail, startDispatch, getCertificateByPlanId, submitCertificate, pilotSignCertificate } from '@/api/pilot'
import { getToken, getUser } from '@/common/storage'
import { formatDateTime } from '@/common/format'
import { pickDateTime } from '@/common/datetime'
import { canCancelPlanConfirm, canConfirmPlan, canEndDispatch, canStartDispatch, confirmStatusLabel, planStatusLabel } from '@/common/plan'

const loading = ref(false)
const error = ref('')
const planId = ref('')
const plan = ref({})
const confirmStatus = ref('PENDING')
const isPilot = ref(true)

const certLoading = ref(false)
const certificate = ref(null)

const format = (v) => formatDateTime(v)
const statusText = (s) => planStatusLabel(s)
const canStart = (s) => isPilot.value && canStartDispatch(s)
const canEnd = (s) => isPilot.value && canEndDispatch(s)
const confirmText = (s) => confirmStatusLabel(s)
const canConfirm = (ps, cs) => canConfirmPlan(ps, cs)
const canCancelConfirm = (ps, cs) => canCancelPlanConfirm(ps, cs)

const fetchDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await getPlanDetail(planId.value)
    plan.value = (res && res.data) || {}
    try {
      const cs = await getMyPlanConfirmStatus(planId.value)
      confirmStatus.value = (cs && cs.data) || 'PENDING'
    } catch (e2) {
      // ignore
    }

    certLoading.value = true
    certificate.value = null
    try {
      const c = await getCertificateByPlanId(planId.value)
      certificate.value = (c && c.data) || null
    } catch (e3) {
      certificate.value = null
    } finally {
      certLoading.value = false
    }
  } catch (e) {
    error.value = e && e.message ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onLoad((query) => {
  planId.value = (query && query.id) || ''
})

onShow(() => {
  const token = getToken()
  if (!token) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  const u = getUser()
  const t = u && u.userType ? String(u.userType).toUpperCase() : ''
  isPilot.value = t === 'PILOT'
  if (planId.value) fetchDetail()
})

const confirm = async (id) => {
  try {
    uni.showLoading({ title: '确认中...' })
    await confirmMyPlan(id)
    uni.showToast({ title: '已确认', icon: 'none' })
    fetchDetail()
  } catch (e) {
    uni.showModal({
      title: '确认失败',
      content: e && e.message ? e.message : '操作失败',
      showCancel: false,
    })
  } finally {
    try {
      uni.hideLoading()
    } catch (err) {
      // ignore
    }
  }
}

const cancelConfirm = async (id) => {
  uni.showModal({
    title: '取消确认',
    editable: true,
    placeholderText: '请输入取消原因（可选）',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '取消中...' })
        await cancelMyPlanConfirm(id, res.content || 'App取消确认')
        uni.showToast({ title: '已取消', icon: 'none' })
        fetchDetail()
      } catch (e) {
        uni.showModal({
          title: '取消失败',
          content: e && e.message ? e.message : '操作失败',
          showCancel: false,
        })
      } finally {
        try {
          uni.hideLoading()
        } catch (err) {
          // ignore
        }
      }
    },
  })
}

const start = async (id) => {
  try {
    const ts = await pickDateTime(Date.now())
    uni.showLoading({ title: '开始中...' })
    await startDispatch(id, ts)
    uni.showToast({ title: '已开始', icon: 'none' })
    fetchDetail()
  } catch (e) {
    const msg = e && e.message ? String(e.message) : ''
    if (msg.indexOf('取消') >= 0 || msg.indexOf('cancel') >= 0) return
    uni.showModal({
      title: '开始失败',
      content: msg || '操作失败',
      showCancel: false,
    })
  } finally {
    try {
      uni.hideLoading()
    } catch (err) {}
  }
}

const end = async (id) => {
  try {
    const ts = await pickDateTime(Date.now())
    uni.showLoading({ title: '结束中...' })
    await endDispatch(id, ts)
    uni.showToast({ title: '已结束', icon: 'none' })
    fetchDetail()
  } catch (e) {
    const msg = e && e.message ? String(e.message) : ''
    if (msg.indexOf('取消') >= 0 || msg.indexOf('cancel') >= 0) return
    uni.showModal({
      title: '结束失败',
      content: msg || '操作失败',
      showCancel: false,
    })
  } finally {
    try {
      uni.hideLoading()
    } catch (err) {}
  }
}

const submitCert = async (certificateId) => {
  try {
    uni.showLoading({ title: '提交中...' })
    await submitCertificate(certificateId)
    uni.showToast({ title: '已提交', icon: 'none' })
    fetchDetail()
  } catch (e) {
    uni.showModal({
      title: '提交失败',
      content: e && e.message ? String(e.message) : '操作失败',
      showCancel: false,
    })
  } finally {
    try { uni.hideLoading() } catch (err) {}
  }
}

const signCert = async (certificateId) => {
  try {
    uni.showLoading({ title: '签字中...' })
    await pilotSignCertificate(certificateId)
    uni.showToast({ title: '签字成功', icon: 'none' })
    fetchDetail()
  } catch (e) {
    uni.showModal({
      title: '签字失败',
      content: e && e.message ? String(e.message) : '操作失败',
      showCancel: false,
    })
  } finally {
    try { uni.hideLoading() } catch (err) {}
  }
}
</script>

<style>
.page {
  padding: 32rpx;
}
.header .title {
  font-size: 38rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}
.header .sub {
  margin-top: 8rpx;
  color: rgba(0, 242, 255, 0.8);
}
.hint {
  margin-top: 18rpx;
  color: rgba(234, 242, 255, 0.65);
}
.card {
  margin-top: 18rpx;
  padding: 24rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.78);
  border: 1px solid rgba(0, 242, 255, 0.16);
}
.section-title {
  font-size: 28rpx;
  font-weight: 800;
  margin-bottom: 14rpx;
  color: rgba(234, 242, 255, 0.92);
}
.kv {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1px solid rgba(234, 242, 255, 0.06);
}
.kv:last-child {
  border-bottom: none;
}
.k {
  color: rgba(234, 242, 255, 0.65);
  font-size: 24rpx;
}
.v {
  color: #eaf2ff;
  font-size: 24rpx;
  text-align: right;
  width: 420rpx;
}
.error {
  margin-top: 16rpx;
  color: #ff4d6d;
  font-size: 24rpx;
}
.actions {
  margin-top: 18rpx;
  display: flex;
  gap: 14rpx;
}
.btn-secondary {
  flex: 1;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  color: rgba(234, 242, 255, 0.92);
  background: rgba(0, 242, 255, 0.16);
  border: 1px solid rgba(0, 242, 255, 0.26);
  font-weight: 800;
  letter-spacing: 2rpx;
}
.btn-ghost {
  background: transparent;
  border-color: rgba(234, 242, 255, 0.18);
  color: rgba(234, 242, 255, 0.8);
}
.btn-primary {
  flex: 1;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  color: #001018;
  background: linear-gradient(120deg, #00f2ff, #4aa6ff);
  font-weight: 800;
  letter-spacing: 4rpx;
}
.btn-danger {
  flex: 1;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  color: rgba(234, 242, 255, 0.92);
  background: rgba(255, 77, 109, 0.24);
  border: 1px solid rgba(255, 77, 109, 0.42);
  font-weight: 800;
  letter-spacing: 4rpx;
}
</style>

