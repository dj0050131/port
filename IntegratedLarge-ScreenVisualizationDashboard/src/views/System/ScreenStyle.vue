<template>
  <TransparentCard>
    <div class="panel-header">
      <h3>大屏样式设置</h3>
      <el-button type="primary" size="small">应用主题</el-button>
    </div>
    <el-radio-group v-model="theme">
      <el-radio-button label="deep">蓝紫科技</el-radio-button>
      <el-radio-button label="light">海岸晨光</el-radio-button>
    </el-radio-group>
    <div class="preview-area">
      <p>当前主题：{{ theme }}</p>
      <div class="preview-card" />
    </div>
  </TransparentCard>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TransparentCard from '@/components/common/TransparentCard.vue';

const theme = ref('deep');
</script>

<style scoped>
.panel-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.preview-area {
  margin-top: 20px;
  border: 1px dashed rgba(255, 255, 255, 0.3);
  padding: 20px;
  border-radius: 12px;
}

.preview-card {
  margin-top: 12px;
  height: 120px;
  border-radius: 12px;
  background: linear-gradient(135deg, #38bdf8, #7c3aed);
  opacity: 0.8;
}
</style>

