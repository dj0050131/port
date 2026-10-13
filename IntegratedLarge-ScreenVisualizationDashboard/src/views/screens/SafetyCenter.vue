<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import ChartCard from '@/components/common/ChartCard.vue'
import DataTable, { type TableColumn } from '@/components/common/DataTable.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import type { AlertItem, SystemStatusItem } from '@/types/dashboard'

const store = useDashboardStore()

const alerts = computed(() => store.alerts)
const systems = computed(() => store.systemStatus)

const safetyMetrics = computed(() => {
  const totalAlerts = alerts.value.length
  const criticalAlerts = alerts.value.filter((item) => String(item.level || '').toLowerCase() === 'critical').length
  const degradedSystems = systems.value.filter((item) => String(item.status || '').toUpperCase() === 'DEGRADED').length
  return [
    { label: '告警总数', value: totalAlerts.toLocaleString('zh-CN'), unit: '条' },
    { label: '紧急告警', value: criticalAlerts.toLocaleString('zh-CN'), unit: '条' },
    { label: '降级系统', value: degradedSystems.toLocaleString('zh-CN'), unit: '套' },
  ]
})

const latestAlerts = computed(() => alerts.value.slice(0, 6))

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

const systemColumns: TableColumn[] = [
  { key: 'moduleName', label: '系统模块',
    formatter: (row) => {
      const system = row as unknown as SystemStatusItem
      const moduleMap: Record<string, string> = {
        'PILOTS': '引航员管理',
        'DRIVERS': '驾驶员管理',
        'VEHICLES': '车辆管理',
        'TUGBOATS': '拖轮管理',
        'PILOT_BOATS': '引航船管理'
      }
      return moduleMap[system.moduleName] || system.moduleName
    }
    },
  {
    key: 'status',
    label: '状态',
    width: '90px',
    formatter: (row) => {
      const system = row as unknown as SystemStatusItem
      return system.status === 'HEALTHY' ? '正常' : system.status === 'DEGRADED' ? '降级' : '离线'
    },
  },
  {
    key: 'uptime',
    label: '可用率',
    formatter: (row) => `${(row as unknown as SystemStatusItem).uptime}%`,
  },
  {
    key: 'resilience',
    label: '连通性',
    width: '90px',
    formatter: (row) => {
      const system = row as unknown as SystemStatusItem
      return system.uptime >= 99 ? '卓越' : system.uptime >= 95 ? '稳定' : '关注'
    },
  },
  {
    key: 'lastUpdate',
    label: '更新时间',
    width: '160px',
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
  { key: 'message', label: '告警内容' },
  { key: 'detail', label: '补充信息', width: '180px', formatter: (row) => (row as unknown as AlertItem).detail ?? '--' },
  { key: 'timestamp', label: '发生时间', width: '160px' },
  {
    key: 'acknowledged',
    label: '状态',
    width: '90px',
    formatter: (row) => {
      const status = (row as unknown as AlertItem).acknowledged
      if (status === true) return '已处理'
      if (status === false) return '待处理'
      return '持续中'
    },
  },
]

const levelStats = computed(() => {
  return alerts.value.reduce(
    (acc, item) => {
      const key = (String(item.level || '').toLowerCase() as AlertItem['level']) || 'info'
      acc[key] = (acc[key] ?? 0) + 1
      return acc
    },
    { critical: 0, warning: 0, info: 0, normal: 0 } as Record<AlertItem['level'], number>,
  )
})

const alertLevelOption = computed<EChartsOption>(() => {
  const stats = levelStats.value
  return {
    tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c} ({d}%)' },
    color: ['#ff5f6d', '#ffb347', '#40a9ff', '#35ffb4'],
    series: [
      {
        type: 'pie',
        radius: ['35%', '70%'],
        label: {
          color: '#d7f3ff',
          formatter: '{b}\n{c}条',
        },
        data: [
          { name: '紧急', value: stats.critical },
          { name: '预警', value: stats.warning },
          { name: '提示', value: stats.info },
          { name: '常规', value: stats.normal },
        ],
      },
    ],
  } as EChartsOption
})

const systemStatusOption = computed<EChartsOption>(() => {
  const healthy = systems.value.filter((item) => item.status === 'HEALTHY').length
  const degraded = systems.value.filter((item) => item.status === 'DEGRADED').length
  const offline = systems.value.filter((item) => item.status === 'OFFLINE').length
  return {
    grid: { left: 24, right: 12, top: 24, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['正常', '降级', '离线'],
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9fb8ff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
    },
    series: [
      {
        type: 'bar',
        data: [healthy, degraded, offline],
        barWidth: 26,
        itemStyle: {
          color: (params: { dataIndex: number }) => ['#35ffb4', '#ffb347', '#ff5f6d'][params.dataIndex],
          borderRadius: [6, 6, 0, 0],
        },
      },
    ],
  } as EChartsOption
})

const riskRadarOption = computed<EChartsOption>(() => {
  if (!alerts.value.length) return {} as EChartsOption

  const typeStats = alerts.value.reduce(
      (acc, item) => {
        const type = item.alertType || 'UNKNOWN'
        acc[type] = (acc[type] ?? 0) + 1
        return acc
      },
      {} as Record<string, number>
  )

  const typeLabels: Record<string, string> = {
    'BILL_OVERDUE': '账单逾期',
    'PILOTAGE_DELAYED': '引航延迟',
    'PILOT_HEALTH': '引航员健康',
    'VEHICLE_MAINTENANCE': '车辆维护',
    'TUGBOAT_MAINTENANCE': '拖轮维护',
    'BERTH_UNAVAILABLE': '泊位不可用',
    'UNKNOWN': '未知类型'
  }

  const types = Object.keys(typeStats).slice(0, 7)

  if (types.length === 0) {
    return {} as EChartsOption
  }

  const maxCount = Math.max(...Object.values(typeStats), 1)

  const indicator = types.map(type => ({
    name: typeLabels[type] || type, // 显示中文标签，如果没有映射则显示原始类型
    max: Math.ceil(maxCount * 1.2)
  }))

  const values = types.map(type => typeStats[type])

  return {
    tooltip: {
      trigger: 'item',
      formatter: params => {
        const dataIndex = params.dataIndex
        const type = types[dataIndex]
        const label = typeLabels[type] || type
        const value = values[dataIndex]
        return `${label}: ${value}`
      }
    },
    radar: {
      indicator,
      radius: '70%',
      splitArea: {
        areaStyle: {
          color: ['rgba(53, 240, 255, 0.05)', 'rgba(53, 240, 255, 0.12)'],
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.2)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.2)'
        }
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            name: '告警类型分布',
            areaStyle: {
              color: 'rgba(53, 240, 255, 0.25)'
            },
            lineStyle: {
              color: '#33f0ff',
              width: 2
            },
            symbol: 'circle',
            symbolSize: 6,
          },
        ],
      },
    ],
  } as EChartsOption
})

//组合表统计图  包含折线和条形 @安全监控-响应趋势
const responseTrendOption = computed<EChartsOption>(() => {
  if (!alerts.value.length) return {} as EChartsOption
  const buckets = new Map<string, { total: number; handled: number }>()
  alerts.value.forEach((item, idx) => {
    const time = item.timestamp ? new Date(item.timestamp) : null
    const label = time
      ? `${String(time.getHours()).padStart(2, '0')}:00`
      : `T${(idx % 12) + 1}`
    const bucket = buckets.get(label) ?? { total: 0, handled: 0 }
    bucket.total += 1
    if (item.acknowledged) bucket.handled += 1
    buckets.set(label, bucket)
  })
  const labels = [...buckets.keys()]
  return {
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#9fb8ff' } },
    grid: { left: 40, right: 24, top: 30, bottom: 24 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9fb8ff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
    },
    series: [
      {
        name: '告警数量',
        type: 'bar',
        data: labels.map((label) => buckets.get(label)?.total ?? 0),
        barWidth: 14,
        itemStyle: { color: 'rgba(255, 95, 109, 0.8)', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '已处置',
        type: 'line',
        smooth: true,
        data: labels.map((label) => buckets.get(label)?.handled ?? 0),
        lineStyle: { color: '#35ffb4', width: 3 },
        showSymbol: false,
      },
    ],
  } as EChartsOption
})

const systemPerformanceOption = computed<EChartsOption>(() => {
  // 使用 store 中的性能数据，如果不存在则返回空配置
  const performanceData = store.performanceData

  if (!performanceData || !performanceData.metrics || performanceData.metrics.length === 0) {
    return {} as EChartsOption
  }

  const performanceMetrics = performanceData.metrics

  const timeLabels = performanceMetrics.map(metric => {
    const date = new Date(metric.timestamp)
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  })

  const cpuData = performanceMetrics.map(metric => metric.cpuUsage)
  const memoryData = performanceMetrics.map(metric => metric.memoryUsage)
  const diskData = performanceMetrics.map(metric => metric.diskUsage)

  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const paramsArray = Array.isArray(params) ? params : [params]
        const time = paramsArray[0]?.axisValue || ''
        let result = `${time}<br/>`

        // 使用 for 循环替代 forEach，避免类型问题
        for (let i = 0; i < paramsArray.length; i++) {
          const param = paramsArray[i]
          if (param && param.seriesName && param.value !== undefined) {
            result += `${param.seriesName}: ${param.value}%<br/>`
          }
        }
        return result
      }
    },
    legend: {
      top: 0,
      data: ['CPU使用率', '内存使用率', '磁盘使用率'],
      textStyle: { color: '#9fb8ff' }
    },
    grid: { left: 40, right: 24, top: 56, bottom: 24 },
    xAxis: {
      type: 'category',
      data: timeLabels,
      axisLabel: { color: '#9fb8ff' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
    },
    yAxis: {
      type: 'value',
      name: '使用率 (%)',
      axisLabel: { color: '#9fb8ff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        data: cpuData,
        lineStyle: { color: '#ff6b6b', width: 3 },
        itemStyle: { color: '#ff6b6b' },
        areaStyle: { color: 'rgba(255, 107, 107, 0.1)' }
      },
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: memoryData,
        lineStyle: { color: '#4ecdc4', width: 3 },
        itemStyle: { color: '#4ecdc4' },
        areaStyle: { color: 'rgba(78, 205, 196, 0.1)' }
      },
      {
        name: '磁盘使用率',
        type: 'line',
        smooth: true,
        data: diskData,
        lineStyle: { color: '#45b7d1', width: 3 },
        itemStyle: { color: '#45b7d1' },
        areaStyle: { color: 'rgba(69, 183, 209, 0.1)' }
      }
    ],
  } as EChartsOption
})

</script>

<template>
  <div class="safety-screen">
    <section class="safety-screen__metrics">
      <MetricCard
        v-for="metric in safetyMetrics"
        :key="metric.label"
        :label="metric.label"
        :value="metric.value"
        :unit="metric.unit"
      />
    </section>

    <section class="safety-screen__grid">
      <div class="grid-col left-col">
        <DataTable
          title="最新告警"
          :columns="alertColumns"
          :rows="latestAlerts"
          :height="260"
        />
        <ChartCard title="系统性能趋势" :option="systemPerformanceOption" :height="340" />
      </div>
      <div class="grid-col right-col">
        <div class="chart-row">
          <ChartCard title="告警级别分布" :option="alertLevelOption" :height="180" />
          <ChartCard title="综合风险雷达" :option="riskRadarOption" :height="180" />
        </div>
        <ChartCard title="系统运行状态" :option="systemStatusOption" :height="180" />
        <DataTable
          title="系统健康"
          :columns="systemColumns"
          :rows="systems"
          :height="150"
        />
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.safety-screen {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  padding: 10px;
}

/* Enable pointer events for interactive elements */
.safety-screen :deep(.metric-card),
.safety-screen :deep(.chart-card),
.safety-screen :deep(.data-table),
.safety-screen :deep(.glass-card) {
  pointer-events: auto;
}

.safety-screen__metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.safety-screen__grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 12px;
  min-height: 0;
}

.grid-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.grid-col :deep(.chart-card),
.grid-col :deep(.data-table) {
  flex-shrink: 0;
}

@media (max-width: 1366px) {
  .safety-screen__grid {
    grid-template-columns: 1fr;
  }
}
</style>