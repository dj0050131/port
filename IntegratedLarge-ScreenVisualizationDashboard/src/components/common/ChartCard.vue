<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, toRaw, watch } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const props = withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    option: EChartsOption
    height?: number | string
    loading?: boolean
    variant?: 'card' | 'plain'
  }>(),
  {
    height: 260,
    loading: false,
    variant: 'card',
  },
)

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null
let mutationObserver: MutationObserver | null = null
let rafId: number | null = null
let retryTimer: number | null = null
let sizeAttempts = 0

const canvasHeight = computed(() => (typeof props.height === 'number' ? `${props.height}px` : props.height))

const getOption = () => {
  const raw = toRaw(props.option)
  return raw ? { ...raw } : {}
}

const applyOption = () => {
  if (chartInstance) {
    const option = getOption()
    chartInstance.setOption(option, true)
  }
}

const clearRetryTimer = () => {
  if (retryTimer !== null) {
    clearTimeout(retryTimer)
    retryTimer = null
  }
}

const scheduleInit = (delay = 16) => {
  clearRetryTimer()
  retryTimer = window.setTimeout(() => {
    retryTimer = null
    if (rafId !== null) cancelAnimationFrame(rafId)
    rafId = requestAnimationFrame(() => {
      rafId = null
      createChart()
    })
  }, delay)
}

const createChart = () => {
  if (!chartRef.value) return
  const { clientWidth, clientHeight } = chartRef.value
  if (!clientWidth || !clientHeight) {
    sizeAttempts += 1
    const nextDelay = Math.min(200, 20 + sizeAttempts * 5)
    scheduleInit(nextDelay)
    return
  }
  sizeAttempts = 0
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  applyOption()
  chartInstance.resize()
}

const dispose = () => {
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
  clearRetryTimer()
  resizeObserver?.disconnect()
  resizeObserver = null
  mutationObserver?.disconnect()
  mutationObserver = null
  chartInstance?.dispose()
  chartInstance = null
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  } else {
    scheduleInit()
  }
}

onMounted(() => {
  nextTick(() => {
    scheduleInit()
    window.addEventListener('resize', handleResize)
    if (chartRef.value && 'ResizeObserver' in window) {
      resizeObserver = new ResizeObserver(() => handleResize())
      resizeObserver.observe(chartRef.value)
    } else if (chartRef.value) {
      mutationObserver = new MutationObserver(() => handleResize())
      mutationObserver.observe(chartRef.value, { attributes: true, childList: true, subtree: true })
    }
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  dispose()
})

watch(
  () => props.option,
  () => {
    if (chartInstance) {
      applyOption()
    } else {
      scheduleInit(40)
    }
  },
  { deep: true },
)
</script>

<template>
  <section class="chart-card" :class="[variant === 'card' ? 'glass-card' : '', 'tech-panel']" v-loading="loading">
    <header v-if="title || subtitle" class="chart-card__header">
      <div>
        <h3 class="chart-card__title">{{ title }}</h3>
        <p v-if="subtitle" class="chart-card__subtitle">{{ subtitle }}</p>
      </div>
      <slot name="actions" />
    </header>
    <div ref="chartRef" class="chart-card__canvas" :style="{ height: canvasHeight }"></div>
    <slot />
  </section>
</template>

<style scoped lang="scss">
.chart-card {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* Use tech-panel base styles if available globally, but we redefine key parts here to be safe */
  background: rgba(10, 22, 40, 0.75);
  backdrop-filter: blur(12px);
  border: 1px solid var(--tech-border);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

/* Corner Accents */
.chart-card::before,
.chart-card::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  border: 2px solid var(--tech-cyan);
  transition: all 0.3s ease;
  pointer-events: none;
}

.chart-card::before {
  top: 0; left: 0;
  border-right: none; border-bottom: none;
}
.chart-card::after {
  bottom: 0; right: 0;
  border-left: none; border-top: none;
}

.chart-card:not(.glass-card) {
  /* Variant 'plain' might skip the background, but user asked for borders on all charts in these views.
     So we might want to enforce the style.
     However, let's keep the option to opt-out if explicitly needed.
  */
  background: transparent;
  box-shadow: none;
  border: none;
  padding: 0;
}
/* Force show tech panel style if it's the default 'card' variant, which most use */
.chart-card.glass-card {
    /* Styles are already applied by .chart-card base rules above */
}

.chart-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(0, 242, 255, 0.2);
  padding-bottom: 8px;
  margin-bottom: 4px;
}

.chart-card__title {
  font-family: var(--font-num);
  font-size: 16px;
  letter-spacing: 0.12em;
  color: var(--tech-cyan);
  text-transform: uppercase;
  margin: 0;
  text-shadow: var(--tech-glow);
}

.chart-card__subtitle {
  font-size: 11px;
  color: rgba(207, 232, 255, 0.7);
  margin: 0;
}

.chart-card__canvas {
  width: 100%;
}
</style>
