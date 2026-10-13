<template>
  <view class="page">
    <view class="head-card">
      <view class="toolbar">
        <view class="title">我的任务</view>
        <view v-if="profile && profile.onDutyStatus" class="duty">在岗：{{ dutyText(profile.onDutyStatus) }}</view>
        <button class="mini" :disabled="loading" :loading="loading" @click="refresh">刷新</button>
      </view>

      <view class="filters">
        <view class="chip" :class="{ active: status === '' }" @click="setStatus('')">全部</view>
        <view class="chip" :class="{ active: status === 'REPLAN_REQUIRED' }" @click="setStatus('REPLAN_REQUIRED')">待重排</view>
        <view class="chip" :class="{ active: status === 'REPLANNED' }" @click="setStatus('REPLANNED')">已重排</view>
        <view class="chip" :class="{ active: status === 'PUBLISHED' }" @click="setStatus('PUBLISHED')">已发布</view>
        <view class="chip" :class="{ active: status === 'ONGOING' }" @click="setStatus('ONGOING')">进行中</view>
        <view class="chip" :class="{ active: status === 'COMPLETED' }" @click="setStatus('COMPLETED')">已完成</view>
      </view>
    </view>

    <view v-if="pilotBindError" class="callout callout-warn">
      <view class="callout-title">账号未绑定人员档案</view>
      <view class="callout-text">{{ error }}</view>
      <view class="callout-actions">
        <button class="btn-small" @click="goProfile">去个人中心</button>
        <button class="btn-small btn-ghost" @click="showBindHelp">如何处理</button>
      </view>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!list.length && !error" class="empty">
      <image class="empty-img" src="/app.png" mode="aspectFit" />
      <view class="empty-title">暂无任务</view>
      <view class="empty-sub">你可以切换状态筛选，或点击刷新获取最新任务</view>
    </view>
    <view v-else class="list">
      <view v-for="item in list" :key="item.id" class="row" @click="openDetail(item.id)">
        <view class="row-top">
          <text class="code">{{ item.planCode || ('PLAN-' + item.id) }}</text>
          <view class="top-right">
            <text class="badge">{{ statusText(item.planStatus) }}</text>
            <text class="badge badge2">{{ confirmText(item.pilotConfirmationStatus) }}</text>
          </view>
        </view>
        <view class="row-mid">
          <text class="loc">{{ item.startLocationName || '-' }}</text>
          <text class="arrow">→</text>
          <text class="loc">{{ item.endLocationName || '-' }}</text>
        </view>
        <view class="row-btm">
          <text class="time">{{ format(item.planStartTime) }}</text>
          <text class="time">-</text>
          <text class="time">{{ format(item.planEndTime) }}</text>
        </view>
        <view class="row-actions">
          <button
            v-if="canConfirm(item.planStatus, item.pilotConfirmationStatus)"
            class="act"
            size="mini"
            :disabled="loading"
            @click.stop="confirm(item.id)"
          >
            确认
          </button>
          <button
            v-else-if="canCancelConfirm(item.planStatus, item.pilotConfirmationStatus)"
            class="act act-ghost"
            size="mini"
            :disabled="loading"
            @click.stop="cancelConfirm(item.id)"
          >
            取消确认
          </button>
          <button
            v-if="canStart(item.planStatus)"
            class="act"
            size="mini"
            :disabled="loading"
            @click.stop="start(item.id)"
          >
            调度开始
          </button>
          <button
            v-else-if="canEnd(item.planStatus)"
            class="act act-danger"
            size="mini"
            :disabled="loading"
            @click.stop="end(item.id)"
          >
            调度结束
          </button>
        </view>
      </view>
    </view>

    <view v-if="error" class="error">{{ error }}</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { cancelMyPlanConfirm, confirmMyPlan, endDispatch, getMyPlans, getMeProfile, startDispatch } from '@/api/pilot'
import { getToken } from '@/common/storage'
import { formatDateTime } from '@/common/format'
import { pickDateTime } from '@/common/datetime'
import { canCancelPlanConfirm, canConfirmPlan, canEndDispatch, canStartDispatch, confirmStatusLabel, planStatusLabel } from '@/common/plan'

const loading = ref(false)
const error = ref('')
const status = ref('')
const page = ref(1)
const size = ref(20)
const list = ref([])
const profile = ref({})
const resourceType = computed(() => {
  const t = profile.value && profile.value.resourceType ? String(profile.value.resourceType) : ''
  return t ? t.toUpperCase() : ''
})

const format = (v) => formatDateTime(v)

const statusText = (s) => planStatusLabel(s)
const canStart = (s) => resourceType.value === 'PILOT' && canStartDispatch(s)
const canEnd = (s) => resourceType.value === 'PILOT' && canEndDispatch(s)
const confirmText = (s) => confirmStatusLabel(s)
const canConfirm = (ps, cs) => canConfirmPlan(ps, cs)
const canCancelConfirm = (ps, cs) => canCancelPlanConfirm(ps, cs)

const dutyText = (s) => {
  const v = (s || '').toString().toUpperCase()
  const map = {
    ON_DUTY: '在岗',
    RESTING: '休息',
    LEAVE: '请假',
    ASSIGNED: '已分配',
    OPERATING: '执行中',
  }
  return map[v] || (s ? String(s) : '-')
}

const fetchProfile = async () => {
  try {
    const res = await getMeProfile()
    profile.value = (res && res.data) || {}
  } catch (e) {
    // ignore
  }
}

const pilotBindError = computed(() => {
  const msg = error.value || ''
  return msg.indexOf('未绑定') >= 0 || msg.indexOf('pilots') >= 0 || msg.indexOf('drivers') >= 0
})

const refresh = async () => {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await getMyPlans({
      page: page.value,
      size: size.value,
      planStatus: status.value || undefined,
    })
    const data = (res && res.data) || {}
    const records = data.records || data.list || []
    list.value = records
  } catch (e) {
    error.value = e && e.message ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

const setStatus = (s) => {
  status.value = s
  refresh()
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/tasks/detail?id=${id}` })
}

const confirm = async (id) => {
  try {
    uni.showLoading({ title: '确认中...' })
    await confirmMyPlan(id)
    uni.showToast({ title: '已确认', icon: 'none' })
    refresh()
  } catch (e) {
    uni.showModal({
      title: '确认失败',
      content: e && e.message ? e.message : '操作失败',
      showCancel: false,
    })
  } finally {
    try {
      uni.hideLoading()
    } catch (err) {}
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
        refresh()
      } catch (e) {
        uni.showModal({
          title: '取消失败',
          content: e && e.message ? e.message : '操作失败',
          showCancel: false,
        })
      } finally {
        try {
          uni.hideLoading()
        } catch (err) {}
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
    refresh()
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
    refresh()
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

const goProfile = () => {
  uni.switchTab({ url: '/pages/profile/index' })
}

const showBindHelp = () => {
  uni.showModal({
    title: '绑定说明',
    content:
      '当前账号在后端 pilots（引航员）/ drivers（驾驶员）表中没有匹配记录。\n\n需要管理员在系统后台将“用户账号”与对应的“引航员/驾驶员档案”绑定（补齐 user_id 关联）。绑定完成后，任务列表即可正常显示。',
    showCancel: false,
  })
}

onShow(() => {
  const token = getToken()
  if (!token) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  fetchProfile()
  refresh()
})
</script>

<style>
.page {
  padding: 32rpx;
}
.head-card {
  padding: 22rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.78);
  border: 1px solid rgba(0, 242, 255, 0.16);
}
.toolbar {
  display: flex;
  align-items: center;
}
.title {
  flex: 1;
  text-align: left;
  font-size: 38rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}
.duty {
  margin-right: 12rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  border: 1px solid rgba(234, 242, 255, 0.16);
  background: rgba(6, 18, 31, 0.6);
  color: rgba(234, 242, 255, 0.8);
  font-size: 22rpx;
}
.mini {
  margin-left: auto;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: rgba(0, 242, 255, 0.12);
  color: rgba(234, 242, 255, 0.9);
  border: 1px solid rgba(0, 242, 255, 0.25);
}
.filters {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.chip {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  border: 1px solid rgba(0, 242, 255, 0.18);
  background: rgba(6, 18, 31, 0.6);
  color: rgba(234, 242, 255, 0.75);
  font-size: 24rpx;
}
.chip.active {
  background: rgba(0, 242, 255, 0.18);
  border-color: rgba(0, 242, 255, 0.35);
  color: #eaf2ff;
}
.hint {
  margin-top: 20rpx;
  color: rgba(234, 242, 255, 0.65);
}
.empty {
  margin-top: 26rpx;
  padding: 34rpx 26rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.62);
  border: 1px dashed rgba(0, 242, 255, 0.18);
  text-align: center;
}
.empty-img {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto;
  opacity: 0.9;
}
.empty-title {
  margin-top: 14rpx;
  font-size: 30rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}
.empty-sub {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(234, 242, 255, 0.6);
}
.callout {
  margin-top: 18rpx;
  padding: 22rpx 20rpx;
  border-radius: 20rpx;
  border: 1px solid rgba(234, 242, 255, 0.12);
  background: rgba(6, 18, 31, 0.72);
}
.callout-warn {
  border-color: rgba(255, 77, 109, 0.28);
  background: rgba(255, 77, 109, 0.08);
}
.callout-title {
  font-size: 28rpx;
  font-weight: 800;
}
.callout-text {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(234, 242, 255, 0.75);
  line-height: 1.6;
}
.callout-actions {
  margin-top: 14rpx;
  display: flex;
  gap: 14rpx;
}
.btn-small {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: rgba(0, 242, 255, 0.16);
  border: 1px solid rgba(0, 242, 255, 0.24);
  color: rgba(234, 242, 255, 0.92);
  font-weight: 700;
}
.btn-ghost {
  background: transparent;
  border-color: rgba(234, 242, 255, 0.18);
  color: rgba(234, 242, 255, 0.8);
}
.list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}
.row {
  padding: 22rpx 20rpx;
  border-radius: 20rpx;
  background: rgba(6, 18, 31, 0.78);
  border: 1px solid rgba(0, 242, 255, 0.16);
}
.row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.code {
  color: rgba(0, 242, 255, 0.85);
  font-weight: 700;
}
.badge {
  font-size: 22rpx;
  color: rgba(234, 242, 255, 0.75);
  border: 1px solid rgba(234, 242, 255, 0.12);
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
}
.badge2 {
  border-color: rgba(0, 242, 255, 0.18);
  color: rgba(0, 242, 255, 0.8);
}
.act {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: rgba(0, 242, 255, 0.16);
  border: 1px solid rgba(0, 242, 255, 0.26);
  color: rgba(234, 242, 255, 0.92);
  font-size: 24rpx;
  font-weight: 700;
  white-space: nowrap;
}
.act-danger {
  background: rgba(255, 77, 109, 0.18);
  border-color: rgba(255, 77, 109, 0.35);
}
.act-ghost {
  background: transparent;
  border-color: rgba(234, 242, 255, 0.18);
  color: rgba(234, 242, 255, 0.8);
}
.row-mid {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}
.loc {
  flex: 1;
  color: #eaf2ff;
  font-size: 26rpx;
}
.arrow {
  color: rgba(0, 242, 255, 0.9);
}
.row-btm {
  margin-top: 10rpx;
  color: rgba(234, 242, 255, 0.6);
  font-size: 22rpx;
}
.time {
  margin-right: 10rpx;
}
.row-actions {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1px solid rgba(0, 242, 255, 0.1);
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  justify-content: flex-end;
}
.error {
  margin-top: 16rpx;
  color: #ff4d6d;
  font-size: 24rpx;
}
</style>

