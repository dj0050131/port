import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import type {
  FeeStatistics,
  OperationsOverview,
  TrafficOverview,
  WeatherTide,
} from '@/types/dashboard'
import type { HourlyTrafficPoint, MapPoint, VehicleTypeShare, VisitorSourceItem } from '@/types/tourism'

const colors = {
  bg: 'transparent',
  tooltipBg: 'rgba(5, 11, 20, 0.95)',
  tooltipBorder: 'rgba(0, 242, 255, 0.4)',
  text: '#e0f7ff',
  textDim: '#8ba4b6',
  axisLine: 'rgba(0, 242, 255, 0.15)',
  splitLine: 'rgba(0, 242, 255, 0.08)',
  cyan: '#00f2ff',
  blue: '#0066ff',
  purple: '#7000ff',
  orange: '#ffb300',
  green: '#00ff9d',
}

const commonTooltip: EChartsOption['tooltip'] = {
  trigger: 'axis',
  backgroundColor: colors.tooltipBg,
  borderColor: colors.tooltipBorder,
  textStyle: { color: colors.text },
  padding: [10, 14],
  axisPointer: {
    type: 'line',
    lineStyle: {
      color: colors.cyan,
      type: 'dashed',
    },
  },
}

const commonAxis = {
  axisLine: { lineStyle: { color: colors.axisLine } },
  axisLabel: { color: colors.textDim, fontFamily: 'Orbitron' },
  splitLine: { lineStyle: { color: colors.splitLine } },
}

export const buildFeeTrendOption = (statistics: FeeStatistics): EChartsOption => ({
  grid: { left: 50, right: 20, top: 40, bottom: 30 },
  tooltip: commonTooltip,
  xAxis: {
    type: 'category',
    data: statistics.trend.map((item) => item.time),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: {
    type: 'value',
    ...commonAxis,
  },
  series: [
    {
      type: 'line',
      data: statistics.trend.map((item) => item.value),
      smooth: true,
      showSymbol: false,
      lineStyle: {
        color: colors.cyan,
        width: 3,
        shadowColor: colors.cyan,
        shadowBlur: 10,
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 242, 255, 0.4)' },
          { offset: 1, color: 'rgba(0, 242, 255, 0.0)' },
        ]),
      },
    },
  ],
})

export const buildFeeCategoryOption = (statistics: FeeStatistics): EChartsOption => ({
  tooltip: { ...commonTooltip, trigger: 'item' },
  legend: {
    bottom: 0,
    textStyle: { color: colors.textDim },
    itemWidth: 10, itemHeight: 10,
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      roseType: 'area',
      itemStyle: {
        borderRadius: 4,
        borderColor: '#050b14',
        borderWidth: 2,
      },
      label: { color: colors.text },
      data: statistics.category.map((item, index) => ({
        ...item,
        name: (() => {
          const key = String(item.name ?? '').trim().toUpperCase()
          if (key === 'PILOTAGE' || key === 'PILOT') return '引航费'
          if (key === 'TUG' || key === 'TUGBOAT') return '拖轮费'
          if (key === 'VEHICLE' || key === 'CAR') return '车辆费'
          return item.name
        })(),
        itemStyle: {
          color: index % 2 === 0 ? colors.cyan : colors.blue,
          shadowBlur: 10,
          shadowColor: index % 2 === 0 ? colors.cyan : colors.blue,
        }
      })),
    },
  ],
})

export const buildTideForecastOption = (weather: WeatherTide): EChartsOption => ({
  grid: { left: 40, right: 40, top: 40, bottom: 36 },
  legend: {
    top: 0,
    textStyle: { color: colors.textDim },
    data: ['潮位 (m)', '温度 (°C)'],
  },
  tooltip: commonTooltip,
  xAxis: {
    type: 'category',
    data: weather.forecast.map((item) => item.time),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: [
    {
      type: 'value',
      name: '潮位(m)',
      position: 'left',
      ...commonAxis,
    },
    {
      type: 'value',
      name: '温度(°C)',
      position: 'right',
      ...commonAxis,
      splitLine: { show: false },
    },
  ],
  series: [
    {
      name: '潮位 (m)',
      type: 'bar',
      data: weather.forecast.map((item) => item.tide),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors.blue },
          { offset: 1, color: 'rgba(0, 102, 255, 0.1)' },
        ]),
        borderRadius: [2, 2, 0, 0],
      },
      barWidth: 12,
    },
    {
      name: '温度 (°C)',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: weather.forecast.map((item) => item.temperature),
      lineStyle: { color: colors.orange, width: 3 },
      itemStyle: { color: colors.orange },
      showSymbol: false,
    },
  ],
})

export const buildTrafficTrendOption = (traffic: TrafficOverview): EChartsOption => ({
  grid: { left: 40, right: 20, top: 40, bottom: 30 },
  tooltip: commonTooltip,
  legend: {
    top: 20,
    textStyle: { color: colors.textDim },
    data: ['进口', '出口']
  },
  xAxis: {
    type: 'category',
    data: traffic.trend.map((item) => item.time),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: {
    type: 'value',
    ...commonAxis,
  },
  series: [
    {
      name: '进口', // Inbound
      type: 'line',
      smooth: true,
      data: traffic.trend.map((item) => item.inbound),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 242, 255, 0.4)' },
          { offset: 1, color: 'rgba(0, 242, 255, 0.0)' },
        ]),
      },
      lineStyle: { color: colors.cyan },
      showSymbol: false,
    },
    {
      name: '出口', // Outbound
      type: 'line',
      smooth: true,
      data: traffic.trend.map((item) => item.outbound),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(112, 0, 255, 0.4)' },
          { offset: 1, color: 'rgba(112, 0, 255, 0.0)' },
        ]),
      },
      lineStyle: { color: colors.purple },
      showSymbol: false,
    },
  ],
})

export const buildTrafficTypeOption = (traffic: TrafficOverview): EChartsOption => ({
  tooltip: { ...commonTooltip, trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      label: { show: true, color: colors.text },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#050b14',
      },
      data: traffic.typeDistribution.map((item, idx) => ({
        ...item,
        itemStyle: {
          color: [colors.cyan, colors.blue, colors.purple, colors.orange][idx % 4]
        }
      })),
    },
  ],
})

// Renamed from buildDeviceStatusOption to Resource Status
// Data source: pilots, pilot_boats, tugboats
export const buildDeviceStatusOption = (traffic: TrafficOverview): EChartsOption => {
    const deviceStatuses = traffic.deviceStatuses || [];

    return {
        title: {
            text: '资源可用率', // Resource Availability
            textStyle: { color: colors.textDim, fontSize: 12 }
        },
        grid: { left: 80, right: 20, top: 30, bottom: 20 },
        xAxis: {
            type: 'value',
            max: 100,
            ...commonAxis,
        },
        yAxis: {
            type: 'category',
            data: deviceStatuses.map((item) => item.name),
            ...commonAxis,
            splitLine: { show: false },
        },
        series: [
            {
                type: 'bar',
                data: deviceStatuses.map((item) => item.onlineRate),
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
                        { offset: 0, color: colors.cyan },
                        { offset: 1, color: colors.blue },
                    ]),
                    borderRadius: [0, 4, 4, 0],
                },
                barWidth: 10,
                label: { show: true, position: 'right', formatter: '{c}%', color: colors.text }
            },
        ],
    };
}

export const buildTrafficMapOption = (traffic: TrafficOverview): EChartsOption => ({
  visualMap: {
    min: 0,
    max: 80,
    calculable: true,
    left: 'left',
    text: ['高', '低'],
    textStyle: { color: colors.textDim },
    inRange: {
      color: [colors.blue, colors.cyan],
    },
  },
  tooltip: {
    ...commonTooltip,
    trigger: 'item',
    formatter: (params: any) => {
      const name = params?.name ?? ''
      const val = params?.value
      const count = Array.isArray(val) && val.length > 2 ? val[2] : undefined
      return `${name}<br/>艘次：${count ?? '--'}`
    },
  },
  geo: {
    map: 'china',
    roam: true,
    zoom: 1.1,
    label: { show: false },
    itemStyle: {
      areaColor: '#0a1628',
      borderColor: colors.cyan,
      borderWidth: 1,
      shadowColor: colors.cyan,
      shadowBlur: 10,
    },
    emphasis: {
      label: { show: false },
      itemStyle: { areaColor: '#102b54' },
    },
  },
  series: [
    {
      name: '船舶来源',
      type: 'effectScatter',
      coordinateSystem: 'geo',
      data: traffic.geoSpots,
      symbolSize: (val) => Math.max(8, Number(val[2]) * 0.8),
      showEffectOn: 'render',
      rippleEffect: {
        brushType: 'stroke',
        scale: 3,
      },
      itemStyle: {
        color: colors.cyan,
        shadowBlur: 10,
        shadowColor: colors.cyan,
      },
      zlevel: 1,
    },
  ],
})

export const buildEnergyTrendOption = (operations: OperationsOverview): EChartsOption => ({
  grid: { left: 40, right: 20, top: 40, bottom: 30 },
  tooltip: commonTooltip,
  xAxis: {
    type: 'category',
    data: operations.energyTrend.map((item) => item.time),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: {
    type: 'value',
    ...commonAxis,
  },
  series: [
    {
      name: '营收',
      type: 'bar',
      data: operations.energyTrend.map((item) => item.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors.purple },
          { offset: 1, color: colors.blue },
        ]),
        borderRadius: [4, 4, 0, 0],
      },
      barWidth: 12,
    },
  ],
})

export const buildBerthUsageOption = (operations: OperationsOverview): EChartsOption => ({
  title: {
    text: '泊位利用率',
    left: 'center',
    top: 'center',
    textStyle: { color: colors.textDim, fontSize: 12 }
  },
  tooltip: { ...commonTooltip, trigger: 'item' },
  series: [
    {
      name: 'Berth Status',
      type: 'pie',
      radius: ['40%', '70%'],
      label: { color: colors.text },
      data: operations.berthUsage.map((item, idx) => ({
        ...item,
        itemStyle: {
           color: [colors.blue, colors.cyan, colors.purple][idx % 3]
        }
      })),
    },
  ],
})

export const buildShipTypePieOption = (data: Array<{ name: string; value: number }>): EChartsOption => ({
  tooltip: { ...commonTooltip, trigger: 'item' },
  legend: {
    bottom: 0,  // 从0改为10，增加与图表的间距
    textStyle: { color: colors.textDim },
    itemWidth: 10,
    itemHeight: 10,
    padding: [5, 10, 5, 10],  // 添加内边距
    itemGap: 10,  // 设置图例项之间的间距
  },  grid: { bottom: '40%' },  // 为图例留出空间
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      roseType: 'area',
      label: { color: colors.text },
      itemStyle: {
        borderColor: '#050b14',
        borderWidth: 2,
      },
      data: data.map((item, idx) => ({
        ...item,
        itemStyle: {
          color: [colors.cyan, colors.blue, colors.purple, colors.orange, colors.green][idx % 5]
        }
      })),
    },
  ],
})

export const buildWeeklyPilotOption = (data: Array<{ label: string; value: number }>): EChartsOption => ({
  grid: { left: 40, right: 20, top: 30, bottom: 20 },
  tooltip: commonTooltip,
  xAxis: {
    type: 'category',
    data: data.map((item) => item.label),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: {
    type: 'value',
    ...commonAxis,
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: data.map((item) => item.value),
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: colors.cyan, width: 3, shadowColor: colors.cyan, shadowBlur: 10 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 242, 255, 0.5)' },
          { offset: 1, color: 'rgba(0, 242, 255, 0.05)' },
        ]),
      },
      itemStyle: { color: '#fff', borderColor: colors.cyan, borderWidth: 2 },
    },
  ],
})

export const buildVisitorSourceOption = (sources: VisitorSourceItem[]): EChartsOption => ({
  title: {
    text: '船舶国籍分布', // Vessel Nationality
    left: 10,
    textStyle: { color: colors.textDim, fontSize: 12 }
  },
  grid: { left: 80, right: 20, top: 30, bottom: 20 },
  xAxis: {
    type: 'value',
    ...commonAxis,
  },
  yAxis: {
    type: 'category',
    data: sources.map((item) => `${item.region}`),
    ...commonAxis,
    splitLine: { show: false },
  },
  series: [
    {
      type: 'bar',
      data: sources.map((item) => item.value),
      barWidth: 10,
      itemStyle: {
        borderRadius: 5,
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: colors.cyan },
          { offset: 1, color: colors.blue },
        ]),
      },
      label: {
        show: true,
        position: 'right',
        color: colors.text,
      },
    },
  ],
})

export const buildVehicleTypeShareOption = (types: VehicleTypeShare[]): EChartsOption => ({
  tooltip: { ...commonTooltip, trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: colors.textDim }, itemWidth: 10, itemHeight: 10 },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      label: { color: colors.text },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#050b14',
      },
      data: types.map((item, idx) => ({
        ...item,
        itemStyle: {
          color: [colors.cyan, colors.blue, colors.purple][idx % 3]
        }
      })),
    },
  ],
})

export const buildHourlyTrafficOption = (data: HourlyTrafficPoint[]): EChartsOption => ({
  grid: { left: 40, right: 16, top: 30, bottom: 26 },
  tooltip: commonTooltip,
  xAxis: {
    type: 'category',
    data: data.map((item) => item.time),
    ...commonAxis,
    splitLine: { show: false },
  },
  yAxis: {
    type: 'value',
    ...commonAxis,
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: data.map((item) => item.value),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 242, 255, 0.6)' },
          { offset: 1, color: 'rgba(0, 242, 255, 0.0)' },
        ]),
      },
      lineStyle: { color: colors.cyan },
      symbol: 'none',
    },
  ],
})

export const buildTourismMapOption = (points: MapPoint[]): EChartsOption => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: colors.tooltipBg,
    borderColor: colors.tooltipBorder,
    textStyle: { color: colors.text },
    formatter: (params: any) => {
      if (params.data?.detail) {
        return `<strong>${params.name}</strong><br/>${params.data.detail}`
      }
      return params.name
    },
  },
  geo: {
    map: 'china',
    roam: true,
    zoom: 1.2,
    label: { show: false },
    itemStyle: {
      areaColor: '#0a1628',
      borderColor: colors.cyan,
      borderWidth: 1,
      shadowColor: colors.cyan,
      shadowBlur: 20,
    },
    emphasis: {
      label: { show: false },
      itemStyle: { areaColor: '#102b54' },
    },
  },
  series: [
    {
      type: 'effectScatter',
      coordinateSystem: 'geo',
      zlevel: 2,
      rippleEffect: {
        brushType: 'stroke',
        scale: 3.2,
      },
      label: {
        formatter: '{b}',
        position: 'right',
        show: true,
        color: colors.text,
      },
      itemStyle: {
        color: (params: any) => {
          const type = params.data?.type
          if (type === 'parking') return '#28ffe8'
          if (type === 'monitor') return '#ffda75'
          return '#33f0ff'
        },
        shadowBlur: 20,
        shadowColor: 'rgba(51, 240, 255, 0.6)',
      },
      symbolSize: (val: any) => Math.max(8, (val[2] as number) / 2),
      data: points.map((point) => ({
        name: point.name,
        value: [...point.coord, point.value],
        type: point.type,
        detail: point.detail,
      })),
    },
  ],
})