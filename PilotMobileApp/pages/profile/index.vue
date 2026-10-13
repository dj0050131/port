<template>
  <view class="page">
    <view class="card">
      <view class="hero">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view class="hero-info">
          <view class="hero-name">{{ usernameText }}</view>
          <view class="hero-sub">
          <text class="pill">{{ resourceTypeCn || userTypeCn }}</text>
            <text class="pill pill-muted">{{ statusCn }}</text>
          </view>
        </view>
      </view>

      <view class="kv"><text class="k">账号</text><text class="v">{{ accountText }}</text></view>
      <view class="kv"><text class="k">用户ID</text><text class="v">{{ userIdText }}</text></view>
      <view class="kv"><text class="k">手机号</text><text class="v">{{ phoneText }}</text></view>
      <view class="kv"><text class="k">邮箱</text><text class="v">{{ emailText }}</text></view>
      <view class="kv"><text class="k">权限数</text><text class="v">{{ permCountText }}</text></view>
    </view>

    <button class="btn-outline" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getToken, getUser, clearToken, clearUser } from '@/common/storage'
import { getBaseUrl } from '@/common/config'
import { userStatusLabel, userTypeLabel } from '@/common/user'
import { getMeProfile } from '@/api/pilot'

const user = ref(null)
const meProfile = ref(null)

const accountText = computed(() => (user.value && user.value.account ? user.value.account : '-'))
const usernameText = computed(() => (user.value && user.value.username ? user.value.username : '-'))
const userIdText = computed(() => (user.value && user.value.userId ? String(user.value.userId) : '-'))
const phoneText = computed(() => (user.value && user.value.phone ? user.value.phone : '-'))
const emailText = computed(() => (user.value && user.value.email ? user.value.email : '-'))
const permCountText = computed(() => {
  const p = user.value && user.value.permissions ? user.value.permissions : []
  return Array.isArray(p) ? String(p.length) : '-'
})
const userTypeCn = computed(() => userTypeLabel(user.value && user.value.userType))
const statusCn = computed(() => userStatusLabel(user.value && user.value.status))
const resourceTypeCn = computed(() => {
  const t = meProfile.value && meProfile.value.resourceType ? String(meProfile.value.resourceType) : ''
  // 资源类型与 userType 不一定一致，以资源档案为准
  if (!t) return ''
  return userTypeLabel(t)
})

const avatarUrl = computed(() => {
  const a = user.value && user.value.avatar ? String(user.value.avatar) : ''
  if (!a) return '/app.png'
  if (a.startsWith('http')) return a
  const base = getBaseUrl().replace(/\/+$/, '')
  const path = a.startsWith('/') ? a : `/${a}`
  if (path.startsWith('/uploads/')) {
    return `${base}/api/files/raw/${encodeURIComponent(path.slice('/uploads/'.length))}`
  }
  return `${base}${path}`
})

const logout = () => {
  clearToken()
  clearUser()
  uni.reLaunch({ url: '/pages/login/login' })
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
})
</script>

<style>
.page {
  padding: 32rpx;
}
.card {
  padding: 28rpx 24rpx;
  border-radius: 22rpx;
  background: rgba(6, 18, 31, 0.78);
  border: 1px solid rgba(0, 242, 255, 0.16);
}
.hero {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding-bottom: 22rpx;
  border-bottom: 1px solid rgba(234, 242, 255, 0.06);
  margin-bottom: 6rpx;
}
.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  background: rgba(0, 242, 255, 0.08);
  border: 1px solid rgba(0, 242, 255, 0.22);
}
.hero-info {
  flex: 1;
}
.hero-name {
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}
.hero-sub {
  margin-top: 10rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.pill {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  border: 1px solid rgba(0, 242, 255, 0.22);
  background: rgba(0, 242, 255, 0.10);
  color: rgba(234, 242, 255, 0.92);
  font-size: 22rpx;
}
.pill-muted {
  border-color: rgba(234, 242, 255, 0.14);
  background: rgba(234, 242, 255, 0.06);
  color: rgba(234, 242, 255, 0.72);
}
.kv {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
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
  width: 440rpx;
}
.btn-outline {
  margin-top: 22rpx;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  background: transparent;
  border: 1px solid rgba(0, 242, 255, 0.35);
  color: rgba(234, 242, 255, 0.9);
  font-weight: 700;
  letter-spacing: 4rpx;
}
</style>

