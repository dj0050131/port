<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: number | string
  unit?: string
  subLabel?: string
  trend?: number
  icon?: string
}>()

const trendLabel = computed(() => {
  if (props.trend === undefined) return ''
  return `${props.trend > 0 ? '+' : ''}${props.trend}%`
})

const trendClass = computed(() => {
  if (props.trend === undefined) return ''
  if (props.trend > 0) return 'positive'
  if (props.trend < 0) return 'negative'
  return 'neutral'
})
</script>

<template>
  <div class="metric-card tech-panel">
    <div class="metric-card__meta">
      <span class="metric-card__label">{{ label }}</span>
      <span v-if="subLabel" class="metric-card__sub">{{ subLabel }}</span>
    </div>
    <div class="metric-card__value">
      <div class="metric-card__value-left">
        <span class="metric-card__number">{{ value }}</span>
        <span v-if="unit" class="metric-card__unit">{{ unit }}</span>
      </div>
      <span v-if="trend !== undefined" class="metric-card__trend-inline" :class="trendClass">
        {{ trendLabel }}
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.metric-card {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;

  /* Tech Panel Base */
  background: rgba(10, 22, 40, 0.75);
  backdrop-filter: blur(12px);
  border: 1px solid var(--tech-border);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

/* Corner Accents */
.metric-card::before,
.metric-card::after {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  border: 2px solid var(--tech-cyan);
  transition: all 0.3s ease;
  pointer-events: none;
}

.metric-card::before {
  top: 0; left: 0;
  border-right: none; border-bottom: none;
}
.metric-card::after {
  bottom: 0; right: 0;
  border-left: none; border-top: none;
}

.metric-card:hover {
  border-color: var(--tech-cyan);
  box-shadow: 0 0 25px rgba(0, 242, 255, 0.2);
}

.metric-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--tech-text-dim);
  font-size: 12px;
}

.metric-card__label {
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-card__sub {
  color: rgba(207, 232, 255, 0.6);
}

.metric-card__value {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.metric-card__value-left {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.metric-card__number {
  font-family: var(--font-num);
  font-size: 26px;
  font-weight: 600;
  color: var(--tech-text-main);
  text-shadow: 0 0 12px rgba(0, 242, 255, 0.4);
}

.metric-card__unit {
  font-size: 12px;
  color: var(--tech-text-dim);
}

.metric-card__trend-inline {
  font-size: 12px;
  letter-spacing: 0.08em;
  font-family: var(--font-num);
  white-space: nowrap;
  flex: 0 0 auto;
}

.metric-card__trend-inline.positive {
  color: #00ff9d;
}

.metric-card__trend-inline.negative {
  color: #ff3366;
}

.metric-card__trend-inline.neutral {
  color: rgba(255, 255, 255, 0.6);
}
</style>
