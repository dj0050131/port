<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    label: string
    value: number | string
    unit?: string
    delta?: number
    color?: string
    loading?: boolean
  }>(),
  {
    unit: '',
    delta: undefined,
    color: '#2a5caa',
    loading: false,
  },
)

const formattedDelta = computed(() => {
  if (props.delta === undefined || props.delta === null) return null
  const sign = props.delta > 0 ? '+' : ''
  return `${sign}${props.delta.toFixed(1)}%`
})
</script>

<template>
  <div class="metric-card glass-card" :style="{ borderColor: color }">
    <el-skeleton v-if="loading" animated :rows="2" />
    <template v-else>
      <header class="metric-card__header">
        <span class="metric-card__label">{{ label }}</span>
        <span v-if="formattedDelta" class="metric-card__delta" :class="{ 'metric-card__delta--up': delta && delta > 0, 'metric-card__delta--down': delta && delta < 0 }">
          {{ formattedDelta }}
        </span>
      </header>
      <div class="metric-card__value" :style="{ color }">
        <span class="digital-number">{{ value }}</span>
        <small v-if="unit">{{ unit }}</small>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.metric-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  background: var(--tidal-bg-card);
  border-left: 4px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.metric-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--tidal-text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.metric-card__value {
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.metric-card__value small {
  font-size: 14px;
  opacity: 0.8;
  font-weight: 400;
  color: var(--tidal-text-secondary);
}

.metric-card__delta {
  font-size: 12px;
  padding: 2px 6px;
  /* No border radius */
  background: rgba(0, 0, 0, 0.05);
}

.metric-card__delta--up {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.metric-card__delta--down {
  color: #f5222d;
  background: rgba(245, 34, 45, 0.1);
}
</style>