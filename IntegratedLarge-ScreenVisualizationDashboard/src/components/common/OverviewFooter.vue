<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    systemName?: string
    currentTime?: string | null
    lastUpdated?: string | null
  }>(),
  {
    systemName: '港口引航及计费系统 · 数据中心',
    currentTime: '—',
    lastUpdated: '—',
  },
)

const systemLabel = computed(() => props.systemName)
const displayTime = computed(() => props.currentTime?.trim() || '—')
const displayUpdated = computed(() => props.lastUpdated?.trim() || '—')
</script>

<template>
  <footer class="overview-footer">
    <span>{{ systemLabel }}</span>
    <span>系统时间：{{ displayTime }}</span>
    <span>数据更新：{{ displayUpdated }}</span>
  </footer>
</template>

<style scoped lang="scss">
.overview-footer {
  position: relative;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center; /* Center aligned footer */
  gap: 24px;
  padding: 14px 26px;
  border-top: 1px solid rgba(0, 242, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
  font-size: 12px;
  color: var(--tech-text-dim);
  z-index: 1;
  letter-spacing: 0.1em;
}

.overview-footer span {
  display: flex;
  align-items: center;
}

.overview-footer span::before {
  content: '';
  display: inline-block;
  width: 4px; height: 4px;
  background: var(--tech-cyan);
  border-radius: 50%;
  margin-right: 8px;
}

@media (max-width: 1200px) {
  .overview-footer {
    flex-direction: column;
    align-items: center;
  }
}
</style>
