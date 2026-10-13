<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const pageTitle = route.meta?.title as string || '功能页面'

const handleBack = () => {
  router.back()
}
</script>

<template>
  <div class="coming-soon-page page-container">
    <div class="coming-soon-content glass-card">
      <div class="icon-container">
        <el-icon :size="80" color="#409eff">
          <component :is="route.meta?.icon || 'Document'" />
        </el-icon>
      </div>
      <h2 class="title">{{ pageTitle }}</h2>
      <p class="message">此功能正在开发中...</p>
      <p class="hint">请参考 <code>DEVELOPMENT_GUIDE.md</code> 使用页面模板快速开发</p>
      <div class="actions">
        <el-button type="primary" :icon="ArrowLeft" @click="handleBack">返回上一页</el-button>
        <el-button @click="router.push('/dashboard')">回到首页</el-button>
      </div>
      <div class="development-info">
        <h4>开发提示：</h4>
        <ul>
          <li>页面路径：<code>{{ route.path }}</code></li>
          <li>页面组件：<code>src/views{{ route.path }}.vue</code></li>
          <li>参考示例：
            <ul>
              <li><code>src/views/system/Users.vue</code> - 列表页示例</li>
              <li><code>src/views/pilotage/Applications.vue</code> - 业务页示例</li>
              <li><code>src/views/billing/Parameters.vue</code> - 配置页示例</li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.coming-soon-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.coming-soon-content {
  padding: 60px 80px;
  text-align: center;
  max-width: 800px;

  .icon-container {
    margin-bottom: 24px;
    opacity: 0.6;
  }

  .title {
    margin: 0 0 16px;
    font-size: 32px;
    font-weight: 600;
    color: #ffffff;
  }

  .message {
    margin: 0 0 12px;
    font-size: 18px;
    color: rgba(203, 230, 255, 0.8);
  }

  .hint {
    margin: 0 0 32px;
    font-size: 14px;
    color: rgba(203, 230, 255, 0.6);

    code {
      padding: 2px 8px;
      background: rgba(64, 158, 255, 0.2);
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      color: #8fd6ff;
    }
  }

  .actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-bottom: 40px;
  }

  .development-info {
    padding: 24px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 12px;
    border: 1px solid rgba(143, 214, 255, 0.12);
    text-align: left;

    h4 {
      margin: 0 0 16px;
      font-size: 16px;
      color: #ffffff;
    }

    ul {
      margin: 0;
      padding-left: 20px;
      color: rgba(203, 230, 255, 0.7);
      font-size: 14px;
      line-height: 1.8;

      li {
        margin-bottom: 8px;
      }

      ul {
        margin-top: 8px;
      }
    }

    code {
      padding: 2px 6px;
      background: rgba(64, 158, 255, 0.15);
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      color: #8fd6ff;
    }
  }
}

@media (max-width: 768px) {
  .coming-soon-content {
    padding: 40px 30px;

    .title {
      font-size: 24px;
    }

    .message {
      font-size: 16px;
    }

    .actions {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }

    .development-info {
      font-size: 13px;
    }
  }
}
</style>

