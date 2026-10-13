<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Ship } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import ParticleBackground from '@/components/common/ParticleBackground.vue'
import { useDataCenterStore } from '@/stores/dataCenter'

const router = useRouter()
const auth = useAuthStore()

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('auth.login', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const loginForm = reactive({
  account: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.account || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  uiState.loading = true
  try {
    const success = await auth.login(loginForm.account, loginForm.password)
    if (success) {
      ElMessage.success('登录成功')
      await router.push('/')
    } else {
      ElMessage.error('登录失败：用户名或密码错误')
    }
  } catch (error: any) {
    console.error(error)
    ElMessage.error('登录异常，请稍后重试')
  } finally {
    uiState.loading = false
  }
}
</script>

<template>
  <div class="login-page">
    <ParticleBackground color="#00a8cc" line-color="rgba(0, 168, 204, 0.15)" :count="80" />

    <div class="login-container">
      <div class="login-box glass-card">
        <div class="login-header">
          <div class="logo-icon">
            <el-icon size="64" color="#00a8cc"><Ship /></el-icon>
          </div>
          <h2 class="title">港口引航管理系统</h2>
          <p class="subtitle">PORT PILOTAGE SYSTEM</p>
        </div>

        <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input
              v-model="loginForm.account"
              :prefix-icon="User"
              placeholder="用户名"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="loginForm.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="uiState.loading"
              @click="handleLogin"
              class="login-button"
            >
              {{ uiState.loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <p class="tip-title">系统说明</p>
          <p class="tip-item">温州港引航作业管理系统</p>
          <p class="tip-item">保障港口船舶安全引航</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #002538 0%, #001824 100%);
}

.login-container {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.login-box {
  width: 450px;
  padding: 48px 40px;
  border-radius: 0 !important; /* Sharp corners */
  background: rgba(0, 37, 56, 0.7);
  border: 1px solid var(--tidal-accent);
  box-shadow: 0 0 40px rgba(0, 168, 204, 0.15);
  backdrop-filter: blur(12px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo-icon {
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
    filter: drop-shadow(0 0 8px rgba(0, 168, 204, 0.6));
  }

  .title {
    margin: 0 0 12px;
    font-size: 28px;
    font-weight: 600;
    color: #ffffff;
    letter-spacing: 0.12em;
    text-shadow: 0 0 10px rgba(0, 168, 204, 0.5);
  }

  .subtitle {
    margin: 0;
    font-size: 12px;
    color: var(--tidal-text-secondary);
    letter-spacing: 0.25em;
    text-transform: uppercase;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  :deep(.el-input) {
    .el-input__wrapper {
      background: rgba(0, 37, 56, 0.6);
      border: 1px solid rgba(0, 168, 204, 0.3);
      box-shadow: none;
      border-radius: 0;
      padding: 0 16px;
      height: 44px;
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--tidal-accent);
        background: rgba(0, 37, 56, 0.8);
      }

      &.is-focus {
        border-color: var(--tidal-accent);
        box-shadow: 0 0 15px rgba(0, 168, 204, 0.2);
        background: rgba(0, 37, 56, 0.9);
      }
    }

    .el-input__inner {
      color: #ffffff;
      height: 44px;

      &::placeholder {
        color: rgba(88, 124, 145, 0.6);
      }
    }

    .el-input__prefix {
      margin-right: 10px;
      color: var(--tidal-accent);
    }

    .el-input__suffix {
      color: var(--tidal-accent);
    }
  }

  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.2em;
    background: var(--tidal-primary);
    border: none;
    border-radius: 0;
    transition: all 0.3s ease;
    margin-top: 12px;

    &:hover {
      background: var(--tidal-accent);
      box-shadow: 0 0 20px rgba(0, 168, 204, 0.4);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.login-tips {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 168, 204, 0.2);
  text-align: center;

  .tip-title {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--tidal-text-secondary);
    font-weight: 500;
    letter-spacing: 0.1em;
  }

  .tip-item {
    margin: 6px 0;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    font-family: 'Consolas', 'Monaco', monospace;
    transition: color 0.3s;

    &:hover {
      color: var(--tidal-accent);
    }
  }
}

@media (max-width: 768px) {
  .login-box {
    width: 90%;
    padding: 32px 20px;
  }

  .login-header .title {
    font-size: 24px;
  }
}
</style>