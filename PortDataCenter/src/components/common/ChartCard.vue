<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, computed } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const props = withDefaults(
    defineProps<{
      title: string
      option: EChartsOption | null
      height?: number
      loading?: boolean
      description?: string
      showNoData?: boolean
    }>(),
    {
      height: 260,
      loading: false,
      description: '',
      showNoData: true,     // 默认显示无数据提示
    },
)

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
const renderComplete = ref(false) // 添加渲染完成标志

const hasValidData = computed(() => {
  if (!props.option) return false
  return checkHasValidData(props.option)
})

// 使用防抖函数避免频繁调用
const debouncedRenderChart = (() => {
  let timeoutId: number | null = null
  return (func: () => void, delay: number) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    timeoutId = window.setTimeout(func, delay)
  }
})()

const renderChart = () => {
  if (!chartRef.value || !props.option) return // 添加调试信息

  if (!hasValidData.value && props.showNoData && renderComplete.value) {
    // 如果没有有效数据且允许显示空状态，并且已经完成渲染，则清空图表
    if (chart) {
      chart.clear()
    }
    return
  }

  if (!chart) {
    chart = echarts.init(chartRef.value) // 调试信息
  }

  try {
    chart.setOption(props.option, true)
    
    setTimeout(() => {
      if (chart) {
        chart.resize()
      renderComplete.value = true

        // 如果高度仍为0，尝试强制重新resize
        if (chart.getHeight() === 0 && chartRef.value) {
           if (chartRef.value.parentElement) {
             const parentHeight = chartRef.value.parentElement.clientHeight
             if (parentHeight === 0 && props.height) {
               // 如果父容器高度也为0，尝试强制设置父容器样式
               chartRef.value.parentElement.style.height = `${props.height}px`
             }
           }
           chart.resize()
        }
      }
    }, 100)
  } catch (error) {
    console.error('Error rendering chart:', error) // 错误信息
  }
}

const checkHasValidData = (option: EChartsOption | null): boolean => {
  if (!option) return false

  if (option.series && Array.isArray(option.series)) {
    for (const series of option.series) {
      if (series.data && Array.isArray(series.data) && series.data.length > 0) {
        // 对于甘特图，数据可能是二维数组 [start, end]，所以只要长度大于0就认为有数据
        if (series.data.some(item => Array.isArray(item) ? item.length > 0 : item !== null && item !== undefined)) {
          return true
        }
      }
    }
    if (option.xAxis || option.yAxis) {
      return true
    }
    return false
  }

  return true // 如果没有定义series，但仍提供了其他配置，也认为是有效配置
}

const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})

watch(
    () => props.option,
    () => {
      if (!props.option) {
        if (chart) {
          chart.clear()
        }
        renderComplete.value = false
        return
      }
      renderComplete.value = false
      debouncedRenderChart(renderChart, 300)
    },
    { deep: true },
)
</script>

<template>
  <section class="chart-card glass-card">
    <header class="chart-card__header">
      <div>
        <h3 class="chart-card__title">{{ title }}</h3>
        <p v-if="description" class="chart-card__desc">{{ description }}</p>
      </div>
      <el-icon v-if="$slots.icon" class="chart-card__icon">
        <slot name="icon" />
      </el-icon>
    </header>
    <div class="chart-card__body" :style="{ height: `${height}px` }">
      <el-skeleton v-if="loading" animated :rows="6" />
      <div
          v-else
          ref="chartRef"
          class="chart-card__canvas"
          :class="{ 'empty-canvas': !hasValidData && showNoData && renderComplete }"
          :style="{ height: '100%', width: '100%' }"
      >
        <el-empty
            v-if="!hasValidData && showNoData && renderComplete"
            description="暂无数据"
            :style="{ height: `${height - 40}px`, display: 'flex', alignItems: 'center', justifyContent: 'center' }"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.chart-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--tidal-bg-card);
  /* glass-card in index.scss handles border and shadow */
}

.chart-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--tidal-border);
  padding-bottom: 12px;
}

.chart-card__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--tidal-text-main);
  margin: 0;
  border-left: 4px solid var(--tidal-primary);
  padding-left: 12px;
}

.chart-card__desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--tidal-text-secondary);
  padding-left: 16px; /* Align with title text */
}

.chart-card__icon {
  font-size: 20px;
  color: var(--tidal-text-secondary);
}

.chart-card__body {
  flex: 1;
  position: relative;
}

.chart-card__canvas {
  width: 100%;
  height: 100%;
  min-height: 200px; /* 添加最小高度 */

  &.empty-canvas {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>