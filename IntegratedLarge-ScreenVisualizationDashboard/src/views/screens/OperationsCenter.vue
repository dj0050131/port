<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import ChartCard from '@/components/common/ChartCard.vue'
import DataTable, { type TableColumn } from '@/components/common/DataTable.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import {
  buildBerthUsageOption,
  buildEnergyTrendOption,
} from '@/utils/chart-utils'
import type { MaintenanceLog, PilotTaskItem } from '@/types/dashboard'

const store = useDashboardStore()

const operations = computed(() => store.operationsOverview)

const energyOption = computed(() => (operations.value ? buildEnergyTrendOption(operations.value) : {}))
const berthOption = computed(() => (operations.value ? buildBerthUsageOption(operations.value) : {}))

const resourceCouplingOption = computed<EChartsOption>(() => {
  if (!operations.value || !operations.value.energyTrend.length) return {} as EChartsOption
  const trend = operations.value.energyTrend
  const xAxis = trend.map((item) => item.time)
  const workload = trend.map((item, idx) => Math.round(item.value * (0.65 + (idx % 3) * 0.08)))
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['能耗', '调度负载'], textStyle: { color: '#9fb8ff' } },
    grid: { left: 40, right: 36, top: 56, bottom: 26 },
    xAxis: {
      type: 'category',
      data: xAxis,
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
    },
    yAxis: [
      {
        type: 'value',
        name: '能耗',
        axisLabel: { color: '#9fb8ff' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      },
      {
        type: 'value',
        name: '负载',
        axisLabel: { color: '#9fb8ff' },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '能耗',
        type: 'bar',
        data: trend.map((item) => item.value),
        barWidth: 16,
        itemStyle: {
          color: 'rgba(63, 149, 255, 0.8)',
          borderRadius: [6, 6, 0, 0],
        },
      },
      {
        name: '调度负载',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#34f0ff' },
        data: workload,
        areaStyle: {
          color: 'rgba(52, 240, 255, 0.12)',
        },
      },
    ],
  } as EChartsOption
})

const taskStatusOption = computed<EChartsOption>(() => {
  if (!operations.value || !operations.value.pilotTasks.length) return {} as EChartsOption
  const statusMap: Record<string, { name: string; value: number }> = {}
  operations.value.pilotTasks.forEach((task) => {
    const label =
        task.status === 'PLANNING'
            ? '计划中'
            : task.status === 'PLANNED'
                ? '已计划'
                : task.status === 'PUBLISHED'
                    ? '已发布'
                    : task.status === 'CONFIRMED'
                        ? '已确认'
                        : task.status === 'ONGOING'
                            ? '进行中'
                            : task.status === 'COMPLETED'
                                ? '已完成'
                                : task.status === 'CERTIFICATED'
                                    ? '已签证'
                                    : task.status === 'BILLED'
                                        ? '已计费'
                                        : task.status === 'PAID'
                                            ? '已付款'
                                            : task.status === 'CANCELLED'
                                                ? '已取消'
                                                : '未知状态'
    statusMap[label] = { name:label, value: (statusMap[label]?.value ?? 0) + 1 }
  })
  const data = Object.values(statusMap)
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [
      {
        type: 'pie',
        roseType: 'radius',
        radius: ['25%', '70%'],
        center: ['50%', '55%'],
        itemStyle: { borderWidth: 0 },
        label: { color: '#d2ecff' },
        data,
      },
    ],
    color: ['#33f0ff', '#35ffb4', '#ffdd87', '#ff9c6e', '#ff6b6b', '#a0e7e5', '#ff9f7f', '#c8f2c8', '#ffd93d', '#9b59b6', '#3498db'],
  } as EChartsOption
})

const statusLabelMap: Record<string, string> = {
  scheduled: '待命',
  'in-progress': '执行中',
  completed: '完成',
  delayed: '延误',
}

const taskColumns: TableColumn[] = [
  { key: 'shipName', label: '船舶' },
  { key: 'pilot', label: '引航员' },
  { key: 'eta', label: '进港' },
  { key: 'etd', label: '离港' },
  {
    key: 'status',
    label: '状态',
    formatter: (row) => {
      const task = row as unknown as PilotTaskItem
      return statusLabelMap[task.status] ?? task.status
    },
  },
  {
    key: 'priority',
    label: '优先级',
    width: '80px',
    align: 'center',
    formatter: (row) => {
      const task = row as unknown as PilotTaskItem
      if (task.status === 'delayed') return '高'
      if (task.status === 'in-progress') return '中'
      return '低'
    },
  },
]

const logColumns: TableColumn[] = [
  { key: 'module', label: '模块' },
  {
    key: 'level',
    label: '等级',
    formatter: (row) => (row as unknown as MaintenanceLog).level?.toUpperCase(),
  },
  {
    key: 'impact',
    label: '影响范围',
    width: '100px',
    formatter: (row) => {
      const log = row as unknown as MaintenanceLog
      return log.level === 'critical' ? '核心' : log.level === 'warning' ? '局部' : '常规'
    },
  },
  { key: 'message', label: '详情' },
  { key: 'timestamp', label: '时间' },
]
</script>

<template>
  <div class="operations-screen" v-if="operations">
    <section class="operations-screen__metrics">
      <MetricCard
        v-for="metric in operations.kpis"
        :key="metric.label"
        :label="metric.label"
        :value="metric.value"
        :unit="metric.unit"
        :trend="metric.change"
      />
    </section>

    <section class="operations-screen__columns">
      <div class="col">
        <ChartCard title="资源联动指数" :option="resourceCouplingOption" :height="200"/>
        <ChartCard title="能源消耗趋势" :option="energyOption" :height="340" />
      </div>
      <div class="col">
        <div class="top-pair">
          <ChartCard title="泊位利用率" :option="berthOption" :height="200" />
          <ChartCard title="任务状态分布" :option="taskStatusOption" :height="200" />
        </div>
        <DataTable title="引航任务看板" :columns="taskColumns" :rows="operations.pilotTasks" :height="388" />
      </div>
    </section>
  </div>
  <el-empty v-else description="等待运行数据..." />
</template>

<style scoped lang="scss">
.operations-screen {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* Enable pointer events for interactive elements */
.operations-screen :deep(.metric-card),
.operations-screen :deep(.chart-card),
.operations-screen :deep(.data-table),
.operations-screen :deep(.glass-card) {
  pointer-events: auto;
}

.operations-screen__metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.operations-screen__columns {
  flex: 1;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 12px;
  align-items: stretch;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.col :deep(.chart-card),
.col :deep(.data-table) {
  flex-shrink: 0;
}

.top-pair {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 1366px) {
  .operations-screen__columns {
    grid-template-columns: 1fr;
  }
}
</style>
