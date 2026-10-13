<script setup lang="ts">
import { computed } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import chinaMap from '../../echarts/map/json/china.json'
import ChartCard from '@/components/common/ChartCard.vue'
import DataTable, { type TableColumn } from '@/components/common/DataTable.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import {
  buildDeviceStatusOption,
  buildTrafficMapOption,
  buildTrafficTrendOption,
  buildTrafficTypeOption,
} from '@/utils/chart-utils'
import type { TrafficSourceItem } from '@/types/dashboard'

if (!echarts.getMap('china')) {
  echarts.registerMap('china', chinaMap as any)
}

const store = useDashboardStore()

const traffic = computed(() => store.trafficOverview)

const trendOption = computed(() => (traffic.value ? buildTrafficTrendOption(traffic.value) : {}))
const typeOption = computed(() => (traffic.value ? buildTrafficTypeOption(traffic.value) : {}))
const mapOption = computed(() => (traffic.value ? buildTrafficMapOption(traffic.value) : {}))
const deviceChartOption = computed(() => (traffic.value ? buildDeviceStatusOption(traffic.value) : {}))

const flowOption = computed<EChartsOption>(() => {
  if (!traffic.value || !traffic.value.sourceRanking.length) return {} as EChartsOption
  const topSources = traffic.value.sourceRanking.slice(0, 6)
  const nodes = [
    ...topSources.map((item) => ({ name: item.nationality })),
    { name: '港口枢纽' },
  ]
  const links = topSources.map((item) => ({
    source: item.nationality,
    target: '港口枢纽',
    value: item.value,
  }))
  return {
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'sankey',
        data: nodes,
        links,
        nodeWidth: 12,
        nodeGap: 16,
        layoutIterations: 0,
        itemStyle: {
          borderWidth: 0,
        },
        lineStyle: {
          color: 'source',
          curveness: 0.5,
          opacity: 0.4,
        },
        emphasis: { focus: 'adjacency' },
      },
    ],
    color: ['#33f0ff', '#5fd8ff', '#89a4ff', '#ffb347', '#35ffb4', '#ff9c6e'],
  } as EChartsOption
})

const radarOption = computed<EChartsOption>(() => {
  if (!traffic.value || !traffic.value.metrics.length) return {} as EChartsOption
  const indicators = traffic.value.metrics.slice(0, 5)
  const maxValue = Math.max(...indicators.map((item) => item.value), 1)
  return {
    radar: {
      indicator: indicators.map((item) => ({ name: item.label, max: Math.ceil(maxValue * 1.2) })),
      radius: '70%',
      splitArea: {
        areaStyle: {
          color: ['rgba(51, 240, 255, 0.05)', 'rgba(51, 240, 255, 0.1)'],
        },
      },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: indicators.map((item) => item.value),
            name: '船舶能力',
            areaStyle: { color: 'rgba(51, 240, 255, 0.35)' },
            lineStyle: { color: '#33f0ff' },
            symbol: 'circle',
            symbolSize: 4,
          },
        ],
      },
    ],
  } as EChartsOption
})

const throughputGaugeOption = computed<EChartsOption>(() => {
  if (!traffic.value || !traffic.value.trend.length) return {} as EChartsOption
  const latestLoad = traffic.value.trend[traffic.value.trend.length - 1]?.totalLoad || 0;
  return {
    series: [
      {
        type: 'gauge',
        max: 120,
        startAngle: 200,
        endAngle: -20,
        progress: {
          show: true,
          width: 10,
          itemStyle: {
            color: '#33f0ff',
          },
        },
        axisLine: {
          lineStyle: {
            width: 10,
            color: [
              [0.5, 'rgba(51, 240, 255, 0.25)'],
              [1, 'rgba(51, 240, 255, 0.55)'],
            ],
          },
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#9fb8ff' },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: '#e6fbff',
          fontSize: 24,
        },
        title: {
          offsetCenter: [0, '60%'],
          fontSize: 12,
          color: '#9fb8ff',
          text: '吞吐指数',
        },
        data: [{ value: latestLoad }],
      },
    ],
  } as EChartsOption
})

const sourceColumns: TableColumn[] = [
  { key: 'rank', label: '排名', width: '60px', align: 'center' },
  { key: 'nationality', label: '国籍/地区' },
  {
    key: 'value',
    label: '艘次',
    formatter: (row) => {
      const record = row as unknown as TrafficSourceItem
      return `${Number(record.value).toLocaleString()} 艘`
    },
  },
  {
    key: 'ratio',
    label: '占比',
    formatter: (row) => {
      const ratio = Number((row as unknown as TrafficSourceItem).ratio ?? 0)
      return `${ratio.toFixed(1)}%`
    },
  },
  {
    key: 'momentum',
    label: '动向',
    formatter: (_row, _column, index) => {
      const target = traffic.value?.sourceRanking[index]
      if (!target) return '--'
      if (target.ratio >= 25) return '加速↑'
      if (target.ratio >= 15) return '稳健→'
      return '平稳'
    },
  },
]
</script>

<template>
  <div class="traffic-screen" v-if="traffic">
    <section class="traffic-screen__metrics">
      <MetricCard
        v-for="metric in traffic.metrics"
        :key="metric.id"
        :label="metric.label"
        :value="metric.value.toLocaleString()"
        :unit="metric.unit"
        :trend="metric.trend"
        :sub-label="metric.subLabel"
      />
    </section>

    <section class="traffic-screen__columns">
      <div class="col col-main">
        <ChartCard title="船舶来源热力" :option="mapOption" :height="300" />
        <ChartCard title="航线网络" :option="flowOption" :height="240" />
      </div>
      <div class="col col-middle">
        <ChartCard title="24小时船舶进出趋势" :option="trendOption" :height="300" />
        <ChartCard title="通航效率雷达" :option="radarOption" :height="240" />
      </div>
      <div class="col col-side">
        <ChartCard title="引航负荷指数" :option="throughputGaugeOption" :height="240" />
        <DataTable title="船舶来源 TOP" :columns="sourceColumns" :rows="traffic.sourceRanking" :height="350" />
      </div>
    </section>
  </div>
  <el-empty v-else description="等待船舶数据..." />
</template>

<style scoped lang="scss">
.traffic-screen {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* Enable pointer events for interactive elements */
.traffic-screen :deep(.metric-card),
.traffic-screen :deep(.chart-card),
.traffic-screen :deep(.data-table),
.traffic-screen :deep(.glass-card) {
  pointer-events: auto;
}

.traffic-screen__metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.traffic-screen__columns {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1.2fr 1fr 0.9fr;
  gap: 18px;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 0;
}

.col-main {
  grid-row: span 1;
}

.col-middle {
  min-height: 0;
}

.col-side {
  min-height: 0;
}

@media (max-width: 1366px) {
  .traffic-screen__columns {
    grid-template-columns: 1fr;
  }
}
</style>
