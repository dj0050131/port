<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import chinaJson from '@/echarts/map/json/china.json'
import type { BerthStatus, PilotRouteSegment, ShipDynamic } from '@/types/dashboard'

const props = defineProps<{
  ships: ShipDynamic[]
  berths: BerthStatus[]
  routes: PilotRouteSegment[]
  center?: [number, number]
}>()

const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

if (!echarts.getMap('china')) {
  echarts.registerMap('china', chinaJson as any)
}

const DEFAULT_CENTER: [number, number] = [120.699362, 27.993828]

const toFiniteNumber = (v: any, fallback = 0) => {
  const n = typeof v === 'number' ? v : Number(v)
  return Number.isFinite(n) ? n : fallback
}

const isValidLngLat = (lng: number, lat: number) => {
  // 粗校验：避免 (0,0)/null/NaN 把图标画到奇怪位置；也防止单位错导致的超范围
  return (
    Number.isFinite(lng) &&
    Number.isFinite(lat) &&
    lng >= 70 &&
    lng <= 140 &&
    lat >= 0 &&
    lat <= 60
  )
}

const clamp = (n: number, min: number, max: number) => Math.min(max, Math.max(min, n))

const resolveCenter = (): [number, number] => {
  if (props.center && props.center.length === 2) return props.center
  const points = [
    ...props.ships
      .map((ship) => [toFiniteNumber(ship.position?.lng, NaN), toFiniteNumber(ship.position?.lat, NaN)])
      .filter(([lng, lat]) => isValidLngLat(lng, lat)),
    ...props.berths
      .map((berth) => [toFiniteNumber(berth.position?.lng, NaN), toFiniteNumber(berth.position?.lat, NaN)])
      .filter(([lng, lat]) => isValidLngLat(lng, lat)),
  ]
  if (!points.length) return DEFAULT_CENTER
  const avgLng = points.reduce((sum, [lng]) => sum + (lng ?? 0), 0) / points.length
  const avgLat = points.reduce((sum, [, lat]) => sum + (lat ?? 0), 0) / points.length
  return [Number(avgLng.toFixed(3)), Number(avgLat.toFixed(3))]
}

const shipSeriesData = () =>
  props.ships
    .map((ship) => {
      const lng = toFiniteNumber(ship.position?.lng, NaN)
      const lat = toFiniteNumber(ship.position?.lat, NaN)
      if (!isValidLngLat(lng, lat)) return null
      const speed = clamp(toFiniteNumber((ship as any).speed, 0), 0, 40) // 速度异常时避免图标被放大
      return {
        name: ship.name,
        value: [lng, lat, speed],
        type: ship.type,
        heading: toFiniteNumber((ship as any).heading, 0),
      }
    })
    .filter(Boolean) as any

const berthSeriesData = () =>
  props.berths
    .map((berth) => {
      const lng = toFiniteNumber(berth.position?.lng, NaN)
      const lat = toFiniteNumber(berth.position?.lat, NaN)
      if (!isValidLngLat(lng, lat)) return null
      return {
        name: berth.name,
        value: [lng, lat, berth.status === 'occupied' ? 30 : 12],
        status: berth.status,
      }
    })
    .filter(Boolean) as any

const routeSeriesData = () =>
  props.routes.map((route) => ({
    coords: [
      [route.from.lng, route.from.lat],
      [route.to.lng, route.to.lat],
    ],
  }))

const buildOption = (): echarts.EChartsOption => ({
  backgroundColor: 'transparent',
  geo: {
    map: 'china',
    roam: true,
    zoom: 6,
    center: resolveCenter(),
    itemStyle: {
      areaColor: '#0a1628',
      borderColor: '#00f2ff',
      borderWidth: 1.2,
      shadowColor: '#00f2ff',
      shadowBlur: 15,
    },
    emphasis: {
      itemStyle: {
        areaColor: '#102b54',
      },
    },
  },
  series: [
    {
      name: '航线',
      type: 'lines',
      coordinateSystem: 'geo',
      zlevel: 2,
      effect: {
        show: true,
        period: 4,
        trailLength: 0.5,
        color: '#fff',
        symbolSize: 3,
      },
      lineStyle: {
        color: '#00f2ff',
        width: 1.5,
        opacity: 0.4,
        curveness: 0.2,
      },
      data: routeSeriesData(),
    },
    {
      name: '泊位',
      type: 'effectScatter',
      coordinateSystem: 'geo',
      zlevel: 3,
      rippleEffect: {
        period: 3,
        scale: 4,
        brushType: 'stroke',
      },
      symbolSize: (val: any) => Math.max(10, val[2] / 2),
      itemStyle: {
        color: (params: any) => {
          const status = params.data?.status
          if (status === 'occupied') return '#ffb300' // Orange
          if (status === 'maintenance') return '#ff3366' // Red
          return '#00f2ff' // Cyan
        },
        shadowBlur: 15,
        shadowColor: 'inherit',
      },
      data: berthSeriesData(),
    },
    {
      name: '船舶',
      type: 'scatter',
      coordinateSystem: 'geo',
      zlevel: 4,
      symbol: 'path://M512 0L128 960h768z', // Triangle ship shape
      symbolSize: (val: any) => {
        const speed = clamp(toFiniteNumber(val?.[2], 0), 0, 40)
        // 控制上限，防止后端异常数据导致三角形“爆炸式放大”
        return clamp(12 + speed * 0.5, 12, 32)
      },
      symbolRotate: (params: any) => {
        const heading = toFiniteNumber(params?.data?.heading, 0)
        return clamp(heading, 0, 360)
      },
      itemStyle: {
        color: (params: any) => {
          const type = params.data?.type
          if (type === 'tanker') return '#ffb300'
          if (type === 'container') return '#0066ff'
          if (type === 'bulk') return '#00ff9d'
          return '#ffffff'
        },
        shadowBlur: 10,
        shadowColor: 'inherit',
      },
      emphasis: {
        label: {
          show: true,
          formatter: '{b}',
          color: '#fff',
        },
      },
      data: shipSeriesData(),
    },
  ],
})

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption(buildOption())
  chart.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', renderChart)
})

watch(
  () => [props.ships, props.routes, props.berths, props.center],
  () => renderChart(),
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div class="port-geo-map">
    <div ref="chartRef" class="echart-root"></div>
  </div>
</template>

<style scoped lang="scss">
.port-geo-map {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(circle at 50% 50%, #0b1e3b 0%, #050b14 100%); /* Deep ocean */
}

.echart-root {
  width: 100%;
  height: 100%;
}

.port-geo-map::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(0, 242, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 242, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 5;
}
</style>
