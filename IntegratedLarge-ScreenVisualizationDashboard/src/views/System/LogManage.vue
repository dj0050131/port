<template>
  <TransparentCard>
    <div class="panel-header">
      <h3>日志管理</h3>
      <el-button size="small">导出</el-button>
    </div>
    <el-table :data="logs" height="420" stripe>
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="level" label="级别" width="120">
        <template #default="{ row }">
          <el-tag :type="row.level === 'ERROR' ? 'danger' : 'info'">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" />
    </el-table>
  </TransparentCard>
</template>

<script setup lang="ts">
import TransparentCard from '@/components/common/TransparentCard.vue';

const logs = [
  { time: '2025-11-11 09:12:00', level: 'INFO', content: '完成费用计算任务' },
  { time: '2025-11-11 09:20:12', level: 'ERROR', content: '调度接口超时，已自动重试' }
];
</script>

<style scoped>
.panel-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>

