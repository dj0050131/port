<template>
  <view class="login">
    <view class="bg-grid"></view>
    <view class="card">
      <view class="title">引航调度</view>
      <view class="subtitle">登录后查看我的调度任务与看板</view>

      <view class="field">
        <text class="label">账号</text>
        <input v-model="form.account" class="input" placeholder="请输入账号" />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input v-model="form.password" class="input" password placeholder="请输入密码" />
      </view>

      <view v-if="error" class="error">{{ error }}</view>

      <button class="btn" :loading="loading" :disabled="loading" @click="onLogin">
        {{ loading ? '正在登录...' : '登录' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { login } from '@/api/auth'
import { setToken, setUser, getToken } from '@/common/storage'
import { getMeProfile } from '@/api/pilot'

const form = reactive({
  account: '',
  password: '',
})
const loading = ref(false)
const error = ref('')

const onLogin = async () => {
  if (loading.value) return
  error.value = ''
  if (!form.account || !form.password) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true
  try {
    const res = await login({ account: form.account, password: form.password })
    const data = (res && res.data) || {}
    setToken(data.token)
    const u = {
      userId: data.userId,
      account: data.account,
      username: data.username,
      avatar: data.avatar,
      userType: data.userType,
      phone: data.phone,
      email: data.email,
      status: data.status,
      permissions: data.permissions,
    }
    try {
      const me = await getMeProfile()
      const mp = (me && me.data) || null
      if (mp && mp.resourceType) {
        u.userType = mp.resourceType
      }
    } catch (e) {
      // ignore
    }
    setUser(u)
    uni.switchTab({ url: '/pages/home/index' })
  } catch (e) {
    error.value = e && e.message ? e.message : '登录失败'
    try {
      uni.showModal({
        title: '登录失败',
        content: error.value,
        showCancel: false,
      })
    } catch (err) {
      // ignore
    }
  } finally {
    loading.value = false
  }
}

onShow(() => {
  const token = getToken()
  if (token) {
    uni.switchTab({ url: '/pages/home/index' })
  }
})
</script>

<style>
.login {
  min-height: 100vh;
  padding: 60rpx 40rpx;
  position: relative;
  background: radial-gradient(circle at 20% 10%, #0b1e3b 0%, #050b14 70%);
}
.bg-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(0, 242, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 242, 255, 0.06) 1px, transparent 1px);
  background-size: 80rpx 80rpx;
  opacity: 0.6;
  pointer-events: none;
}
.card {
  position: relative;
  z-index: 2;
  margin: 120rpx auto 0;
  padding: 48rpx 40rpx;
  border-radius: 28rpx;
  background: rgba(6, 18, 31, 0.85);
  border: 1px solid rgba(0, 242, 255, 0.25);
  box-shadow: 0 24rpx 60rpx rgba(0, 0, 0, 0.55);
}
.title {
  font-size: 44rpx;
  font-weight: 700;
  letter-spacing: 6rpx;
  color: #eaf2ff;
}
.subtitle {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(234, 242, 255, 0.7);
}
.field {
  margin-top: 30rpx;
}
.label {
  display: block;
  font-size: 24rpx;
  color: rgba(0, 242, 255, 0.75);
  letter-spacing: 2rpx;
}
.input {
  margin-top: 14rpx;
  padding: 22rpx 24rpx;
  border-radius: 18rpx;
  background: rgba(0, 242, 255, 0.08);
  border: 1px solid rgba(0, 242, 255, 0.18);
  color: #fff;
}
.error {
  margin-top: 20rpx;
  color: #ff4d6d;
  font-size: 24rpx;
}
.btn {
  margin-top: 34rpx;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  color: #001018;
  background: linear-gradient(120deg, #00f2ff, #4aa6ff);
  font-weight: 700;
  letter-spacing: 6rpx;
}
</style>

