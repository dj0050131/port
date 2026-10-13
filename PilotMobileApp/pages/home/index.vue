<template>
  <view class="page">
    <view class="header">
      <view class="title">引航看板</view>
      <view class="sub">{{ subTitle }}</view>
    </view>

    <view class="cards">
      <view class="card">
        <view class="label">全港今日任务</view>
        <view class="value">{{ todayPlansTotalText }}</view>
      </view>
      <view class="card">
        <view class="label">我的今日任务</view>
        <view class="value">{{ myTodayPlansText }}</view>
      </view>
      <view class="card">
        <view class="label">未来24小时</view>
        <view class="value">{{ myUpcoming24hPlansText }}</view>
      </view>
      <view class="card">
        <view class="label">我的全部任务</view>
        <view class="value">{{ myTotalPlansText }}</view>
      </view>
    </view>

    <view class="panel">
      <view class="panel-title">近7天引航任务趋势</view>
      <view v-if="loading" class="hint">加载中...</view>
      <view v-else-if="!trends.length" class="hint">暂无数据</view>
      <view v-else class="trend-list">
        <view v-for="t in trends" :key="t.date" class="trend-item">
          <view class="t-date">{{ t.date }}</view>
          <view class="t-bar">
            <view class="t-fill" :style="{ width: t.percent + '%' }"></view>
          </view>
          <view class="t-count">{{ t.count }}</view>
        </view>
      </view>
    </view>

    <view v-if="error" class="error">{{ error }}</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getDashboard } from '@/api/pilot'
import { getToken } from '@/common/storage'
import { getUser } from '@/common/storage'
import { userTypeLabel } from '@/common/user'
import { getMeProfile } from '@/api/pilot'

const loading = ref(false)
const error = ref('')
const dashboard = ref({})
const trends = ref([])
const user = ref(null)
const meProfile = ref(null)

const subTitle = computed(() => {
  const t = meProfile.value && meProfile.value.resourceType ? meProfile.value.resourceType : user.value && user.value.userType ? user.value.userType : ''
  const cn = userTypeLabel(t)
  return `实时同步 · ${cn}端`
})

const formatCountOrDash = (v) => {
  if (v === 0) return '0'
  if (v === null || v === undefined || v === '') return '--'
  return String(v)
}

const todayPlansTotalText = computed(() => formatCountOrDash(dashboard.value.todayPlansTotal))
const myTodayPlansText = computed(() => formatCountOrDash(dashboard.value.myTodayPlans))
const myUpcoming24hPlansText = computed(() => formatCountOrDash(dashboard.value.myUpcoming24hPlans))
const myTotalPlansText = computed(() => formatCountOrDash(dashboard.value.myTotalPlans))

const fetchData = async () => {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await getDashboard()
    dashboard.value = (res && res.data) || {}

    const trendSrc = dashboard.value.pilotageTrends7d || []
    const raw = trendSrc.map((p) => {
      const countVal =
        p && p.value !== null && p.value !== undefined
          ? p.value
          : p && p.count !== null && p.count !== undefined
            ? p.count
            : 0
      return {
        date: (p && (p.date || p.time)) || '',
        count: Number(countVal || 0),
      }
    })

    const max = raw.reduce((m, x) => Math.max(m, x.count), 0) || 1
    trends.value = raw.map((x) => ({ ...x, percent: Math.round((x.count / max) * 100) }))
  } catch (e) {
    error.value = e && e.message ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onShow(() => {
  const token = getToken()
  if (!token) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  user.value = getUser()
  getMeProfile()
    .then((res) => {
      meProfile.value = (res && res.data) || null
    })
    .catch(() => {
      meProfile.value = null
    })
  fetchData()
})
</script>

<style>
.page {
  padding: 40rpx 32rpx 60rpx;
}
.header .title {
  font-size: 40rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}
.header .sub {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(234, 242, 255, 0.65);
}
.cards {
  margin-top: 28rpx;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}
.card {
  padding: 26rpx 24rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.82);
  border: 1px solid rgba(0, 242, 255, 0.18);
}
.label {
  font-size: 24rpx;
  color: rgba(0, 242, 255, 0.75);
}
.value {
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 800;
  color: #eaf2ff;
}
.panel {
  margin-top: 26rpx;
  padding: 24rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.65);
  border: 1px solid rgba(0, 242, 255, 0.16);
}
.panel-title {
  font-size: 28rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}
.hint {
  margin-top: 16rpx;
  color: rgba(234, 242, 255, 0.6);
  font-size: 24rpx;
}
.trend-list {
  margin-top: 18rpx;
}
.trend-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 12rpx 0;
}
.t-date {
  width: 180rpx;
  font-size: 24rpx;
  color: rgba(234, 242, 255, 0.75);
}
.t-bar {
  flex: 1;
  height: 14rpx;
  background: rgba(0, 242, 255, 0.12);
  border-radius: 999rpx;
  overflow: hidden;
}
.t-fill {
  height: 100%;
  background: linear-gradient(120deg, #00f2ff, #4aa6ff);
}
.t-count {
  width: 80rpx;
  text-align: right;
  color: rgba(234, 242, 255, 0.9);
  font-weight: 700;
}
.error {
  margin-top: 16rpx;
  color: #ff4d6d;
  font-size: 24rpx;
}
</style>

