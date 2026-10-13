<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import ChartCard from '@/components/common/ChartCard.vue'
import DataTable, { type TableColumn } from '@/components/common/DataTable.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import type { AlertItem, FeeBreakdownItem } from '@/types/dashboard'

const store = useDashboardStore()

const feeStats = computed(() => store.feeStats)

const feeTypeLabel = (raw: unknown) => {
  const key = String(raw ?? '').trim().toUpperCase()
  if (key === 'PILOTAGE' || key === 'PILOT') return '引航费'
  if (key === 'TUG' || key === 'TUGBOAT') return '拖轮费'
  if (key === 'VEHICLE' || key === 'CAR') return '车辆费'
  if (!key) return '其他'
  return String(raw)
}

const displayBreakdown = computed(() => {
  const list = feeStats.value?.breakdown ?? []
  return list.map((it) => ({ ...it, label: feeTypeLabel((it as any).label) }))
})

const displayCategory = computed(() => {
  const list = feeStats.value?.category ?? []
  return list.map((it) => ({ ...it, name: feeTypeLabel((it as any).name) }))
})

const feeMetrics = computed(() => {
  if (!feeStats.value) return []
  const stats = feeStats.value
  const avgTicket = stats.transactions ? Math.round(stats.total / stats.transactions) : 0
  return [
    {
      label: '营收总额',
      value: stats.total.toLocaleString('zh-CN'),
      unit: stats.currency,
      trend: stats.growth,
    },
    {
      label: '交易笔数',
      value: stats.transactions.toLocaleString('zh-CN'),
      unit: '单',
    },
    {
      label: '客单价',
      value: avgTicket.toLocaleString('zh-CN'),
      unit: stats.currency,
    },
    {
      label: '收入增幅',
      value: `${stats.growth}%`,
      trend: stats.growth,
    },
  ]
})

const billingAlerts = computed(() =>
    store.alerts
        .filter((alert) => alert.alertType === 'BILL_OVERDUE')
)

const alertTypeLabel = (t?: string) => {
  const key = String(t || '').toUpperCase()
  if (key === 'BILL_OVERDUE') return '账单逾期'
  if (key === 'PILOTAGE_DELAYED') return '引航延迟'
  if (key === 'PILOT_HEALTH') return '引航员健康'
  if (key === 'VEHICLE_MAINTENANCE') return '车辆维护'
  if (key === 'TUGBOAT_MAINTENANCE') return '拖轮维护'
  if (key === 'BERTH_UNAVAILABLE') return '泊位不可用'
  return t || '未知'
}

const breakdownColumns: TableColumn[] = [
  {
    key: 'label',
    label: '费用类型',
    formatter: (row) => feeTypeLabel((row as unknown as FeeBreakdownItem).label),
  },
  {
    key: 'value',
    label: '金额 (元)',
    formatter: (row) => (row as unknown as FeeBreakdownItem).value.toLocaleString('zh-CN'),
  },
  {
    key: 'ratio',
    label: '贡献度',
    formatter: (row) => {
      if (!feeStats.value?.total) return '--'
      const item = row as unknown as FeeBreakdownItem
      return `${((item.value / feeStats.value.total) * 100).toFixed(1)}%`
    },
  },
]

const alertColumns: TableColumn[] = [
  {
    key: 'alertType',
    label: '类型',
    width: '110px',
    formatter: (row) => alertTypeLabel((row as unknown as AlertItem).alertType),
  },
  {
    key: 'level',
    label: '等级',
    width: '80px',
    formatter: (row) => {
      const lvl = String((row as unknown as AlertItem).level || '').toLowerCase()
      return lvl === 'critical' ? '紧急' : lvl === 'warning' ? '预警' : lvl === 'info' ? '提示' : '常规'
    },
  },
  { key: 'message', label: '详情' },
  { key: 'detail', label: '补充信息', width: '180px', formatter: (row) => (row as unknown as AlertItem).detail ?? '--' },
  { key: 'timestamp', label: '时间', width: '160px' },
  {
    key: 'acknowledged',
    label: '状态',
    width: '80px',
    formatter: (row) => {
      const status = (row as unknown as AlertItem).acknowledged
      if (status === true) return '已处理'
      if (status === false) return '待处理'
      return '持续中'
    },
  },
]

const feeTrendOption = computed<EChartsOption>(() => {
  if (!feeStats.value) return {} as EChartsOption
  const stats = feeStats.value
  return {
    grid: { left: 20, right: 16, top: 26, bottom: 18, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: stats.trend.map((item) => item.time),
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#9fb8ff',
        formatter: (val: number) => `${(val / 1000).toFixed(0)}k`,
      },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
    },
    series: [
      {
        type: 'line',
        data: stats.trend.map((item) => item.value),
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#31f0ff' },
        areaStyle: {
          color: 'rgba(49, 240, 255, 0.15)',
        },
      },
    ],
  } as EChartsOption
})

const feeCategoryOption = computed<EChartsOption>(() => {
  if (!feeStats.value) return {} as EChartsOption
  const stats = { ...feeStats.value, category: displayCategory.value }
  return {
    tooltip: { trigger: 'item', formatter: '{b}<br/>占比：{d}%' },
    color: ['#33f0ff', '#35ffb4', '#ffdd87', '#ff9c6e', '#a389f8'],
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '52%'],
        label: {
          color: '#d7f3ff',
          formatter: '{b}\n{d}%',
        },
        labelLine: {
          smooth: true,
          length: 10,
          length2: 24,
          lineStyle: { color: 'rgba(255,255,255,0.45)' },
        },
        data: stats.category,
      },
    ],
  } as EChartsOption
})

const feeTreemapOption = computed<EChartsOption>(() => {
  if (!feeStats.value) return {} as EChartsOption
  return {
    tooltip: { formatter: '{b}<br/>金额：{c}元' },
    series: [
      {
        type: 'treemap',
        roam: false,
        breadcrumb: {
          show: true,
          left: 'center',
          bottom: 4,
          height: 18,
          itemStyle: {
            color: 'rgba(56, 220, 250, 0.9)',
            borderColor: '#31f0ff',
            borderWidth: 1,
            shadowBlur: 6,
            shadowColor: 'rgba(49, 240, 255, 0.45)',
          },
          textStyle: {
            color: '#e6fbff',
          },
          emphasis: {
            itemStyle: {
              color: 'rgba(22, 90, 150, 0.95)',
            },
          },
        },
        data: displayBreakdown.value.map((item) => ({
          name: feeTypeLabel(item.label),
          value: item.value,
        })),
        label: {
          show: true,
          color: '#e6fbff',
          formatter: '{b}\n{c}',
        },
        upperLabel: { show: false },
        itemStyle: {
          borderColor: 'rgba(255,255,255,0.08)',
          borderWidth: 1,
        },
      },
    ],
    color: ['#33f0ff', '#35ffb4', '#ffdd87', '#ff9c6e', '#a389f8'],
  } as EChartsOption
})

const feeFunnelOption = computed<EChartsOption>(() => {
  if (!feeStats.value) return {} as EChartsOption
  const total = feeStats.value.total
  const funnelData = [
    { name: '预估收入', value: Math.round(total * 1.08) },
    { name: '计费确认', value: total },
    { name: '已开票', value: Math.round(total * 0.9) },
    { name: '已收款', value: Math.round(total * 0.82) },
    { name: '入账完成', value: Math.round(total * 0.78) },
  ]
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 元' },
    series: [
      {
        type: 'funnel',
        left: '5%',
        right: '5%',
        top: 20,
        bottom: 10,
        label: { color: '#e6fbff' },
        data: funnelData,
        itemStyle: {
          borderColor: 'rgba(255,255,255,0.08)',
          borderWidth: 1,
        },
      },
    ],
    color: ['#33f0ff', '#35ffb4', '#ffdd87', '#ff9c6e', '#a389f8'],
  } as EChartsOption
})

const feeHeatmapOption = computed<EChartsOption>(() => {
  if (!feeStats.value || !feeStats.value.trend.length) return {} as EChartsOption
  const weekdays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  const weekCount = Math.min(Math.ceil(feeStats.value.trend.length / weekdays.length), 5)
  const rows = Array.from({ length: weekCount }, (_, idx) => `W${idx + 1}`)

  const parseTrendDate = (raw: unknown): Date | null => {
    const s = String(raw ?? '').trim()
    if (!s) return null
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return new Date(`${s}T00:00:00`)
    if (/^\d{2}-\d{2}$/.test(s)) {
      const year = new Date().getFullYear()
      return new Date(`${year}-${s}T00:00:00`)
    }
    const d = new Date(s)
    return Number.isNaN(d.getTime()) ? null : d
  }

  const weekdayMon0 = (d: Date) => (d.getDay() + 6) % 7

  const trend = feeStats.value.trend
  const startDate = parseTrendDate((trend[0] as any)?.time)
  const dayMs = 24 * 60 * 60 * 1000

  const data: Array<[number, number, number]> = []
  trend.forEach((item, idx) => {
    const d = parseTrendDate((item as any)?.time)
    const value = Number((item as any)?.value ?? 0)

    let dayIndex = idx % weekdays.length
    let weekIndex = Math.min(Math.floor(idx / weekdays.length), rows.length - 1)

    if (d && startDate) {
      dayIndex = weekdayMon0(d)
      const diffDays = Math.max(0, Math.floor((d.getTime() - startDate.getTime()) / dayMs))
      weekIndex = Math.min(Math.floor(diffDays / 7), rows.length - 1)
    }

    data.push([dayIndex, weekIndex, Number.isFinite(value) ? value : 0])
  })

  const maxVal = Math.max(...data.map((entry) => Number(entry[2]) || 0), 1)
  return {
    tooltip: {
      position: 'top',
      formatter: (params: any) => {
        const [dayIdx, rowIdx, value] = params.data
        const label = (trend[(rowIdx * 7) + dayIdx] as any)?.time
        const v = Number(value) || 0
        return `${rows[rowIdx]} ${weekdays[dayIdx]}<br/>日期：${label ?? '--'}<br/>收入：${v.toLocaleString('zh-CN')}`
      },
    },
    grid: { top: 20, bottom: 20, left: 40, right: 20 },
    xAxis: {
      type: 'category',
      data: weekdays,
      splitArea: { show: true },
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
    },
    yAxis: {
      type: 'category',
      data: rows,
      splitArea: { show: true },
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
    },
    visualMap: {
      min: 0,
      max: maxVal,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      textStyle: { color: '#9fb8ff' },
      itemWidth: 12,
      itemHeight: 90,
      handleSize: 16,
      handleStyle: {
        color: '#31f0ff',
        borderColor: '#baf4ff',
        borderWidth: 1,
        shadowBlur: 6,
        shadowColor: 'rgba(49, 240, 255, 0.45)',
      },
      indicatorStyle: {
        color: '#31f0ff',
        borderColor: '#baf4ff',
        borderWidth: 1,
      },
      inRange: {
        color: ['#08325c', '#0f6fb3', '#31f0ff', '#9cffd5'],
      },
      outOfRange: {
        color: ['rgba(8, 50, 92, 0.35)'],
      },
    },
    series: [
      {
        type: 'heatmap',
        data,
        label: { show: false },
      },
    ],
  } as EChartsOption
})
</script>

<template>
  <div class="billing-screen" v-if="feeStats">
    <section class="billing-screen__metrics">
      <MetricCard
        v-for="metric in feeMetrics"
        :key="metric.label"
        :label="metric.label"
        :value="metric.value"
        :unit="metric.unit"
        :trend="metric.trend"
      />
    </section>

    <section class="billing-screen__grid">
      <div class="grid-col">
        <ChartCard title="引航计费趋势" :option="feeTrendOption" :height="220" />
        <ChartCard title="收入热力图" :option="feeHeatmapOption" :height="200" />
        <DataTable class="glass-card" title="计费相关告警" :columns="alertColumns" :rows="billingAlerts" :height="200" style="min-height: 250px"/>
      </div>
      <div class="grid-col">
        <ChartCard title="费用结构矩阵" :option="feeTreemapOption" :height="220" />
        <ChartCard title="收费阶段漏斗" :option="feeFunnelOption" :height="200" />
        <ChartCard title="费项占比" :option="feeCategoryOption" :height="200" />
      </div>
    </section>
  </div>
  <el-empty v-else description="等待计费数据..." />
</template>

<style scoped lang="scss">
.billing-screen {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

/* Enable pointer events for interactive elements */
.billing-screen :deep(.metric-card),
.billing-screen :deep(.chart-card),
.billing-screen :deep(.data-table),
.billing-screen :deep(.glass-card) {
  pointer-events: auto;
}

.billing-screen__metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.billing-screen__grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  min-height: 0;
}

.grid-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.grid-col :deep(.chart-card),
.grid-col :deep(.data-table) {
  flex-shrink: 0;
}

@media (max-width: 1366px) {
  .billing-screen__grid {
    grid-template-columns: 1fr;
  }
}
</style>
