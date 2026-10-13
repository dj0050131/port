<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { EChartsOption } from 'echarts'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import BaseChart from '@/components/chart/BaseChart.vue'

const store = useDashboardStore()

const weather = computed(() => store.weather?.weather)
const tide = computed(() => store.weather?.tide)
const forecast = computed(() => store.weather?.forecast ?? [])

const groupedForecast = computed(() => {
  const map = new Map<string, typeof forecast.value>()
  forecast.value.forEach((entry) => {
    const [dayPart] = entry.time.split(' ')
    const day = dayPart ?? entry.time
    if (!map.has(day)) map.set(day, [])
    map.get(day)!.push(entry)
  })
  return Array.from(map.entries()).map(([day, entries]) => ({ day, entries }))
})

const availableDays = computed(() => groupedForecast.value.map((group) => group.day))
const activeDay = ref('')

watch(availableDays, (days) => {
  if (!days.includes(activeDay.value)) {
    activeDay.value = days[0] ?? ''
  }
}, { immediate: true })

const getTimeLabel = (timestamp: string) => {
  const [, time] = timestamp.split(' ')
  return time ?? timestamp
}

const hourlyData = computed(() => {
  const target = groupedForecast.value.find((group) => group.day === activeDay.value)
  return target?.entries ?? []
})

const formatDayLabel = (day: string) => {
  if (!day) return '—'
  const date = new Date(day)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${date.getDate()}日 (${weekdays[date.getDay()]})`
}

const dailySummary = computed(() =>
  groupedForecast.value.map(({ day, entries }) => {
    const temps = entries.map((item) => item.temperature)
    return {
      day,
      label: formatDayLabel(day),
      min: Math.min(...temps),
      max: Math.max(...temps),
      condition: entries[0]?.condition ?? '多云',
      wind: `${entries[0]?.wind ?? '—'} <${entries[0]?.windLevel ?? 3}级`,
    }
  }),
)

const currentSnapshot = computed(() => {
  const current = hourlyData.value?.[0]
  if (!current) return null
  return {
    condition: weather.value?.condition ?? '—',
    temperature: current.temperature,
    humidity: weather.value?.humidity ?? '—',
    windDirection: weather.value?.windDirection ?? '—',
    windSpeed: weather.value?.windSpeed ?? '—',
    visibility: weather.value?.visibility ?? '—',
    pressure: weather.value?.pressure ?? '—',
    tide: current.tide,
  }
})

const radarChart = computed<EChartsOption>(() => ({
  color: ['#49daff'],
  tooltip: { trigger: 'item' as const },
  radar: {
    indicator: [
      { name: '温度', max: 40, min: -10 },
      { name: '湿度', max: 100 },
      { name: '能见度', max: 15 },
      { name: '风速', max: 20 },
      { name: '气压', max: 1050, min: 900 },
    ],
    splitLine: { lineStyle: { color: 'rgba(73, 218, 255, 0.4)' } },
    splitArea: { areaStyle: { color: ['rgba(73,218,255,0.15)', 'transparent'] } },
  },
  series: [
    {
      type: 'radar',
      data: [
        {
          value: [
            weather.value?.temperature ?? 0,
            weather.value?.humidity ?? 0,
            weather.value?.visibility ?? 0,
            weather.value?.windSpeed ?? 0,
            weather.value?.pressure ?? 0,
          ],
        },
      ],
    },
  ],
}))

const tideChart = computed<EChartsOption>(() => ({
  color: ['#38bdf8'],
  tooltip: { trigger: 'axis' as const },
  xAxis: {
    type: 'category' as const,
    data: hourlyData.value.map((item) => getTimeLabel(item.time)),
    axisLabel: { color: '#d5ecff' },
    boundaryGap: false,
  },
  yAxis: {
    type: 'value' as const,
    axisLabel: { color: '#d5ecff', formatter: '{value} m' },
    splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.08)' } },
  },
  series: [
    {
      type: 'line' as const,
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(56, 189, 248, 0.6)' },
            { offset: 1, color: 'rgba(56, 189, 248, 0)' },
          ],
        },
      },
      data: hourlyData.value.map((item) => item.tide),
    },
  ],
}))

const temperatureChart = computed<EChartsOption>(() => ({
  color: ['#f7b955'],
  tooltip: { trigger: 'axis' as const },
  xAxis: {
    type: 'category' as const,
    data: hourlyData.value.map((item) => getTimeLabel(item.time)),
    axisLabel: { color: '#ffd7a1' },
  },
  yAxis: {
    type: 'value' as const,
    axisLabel: { color: '#ffd7a1', formatter: '{value}°C' },
    splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.08)' } },
  },
  series: [
    {
      type: 'bar' as const,
      barWidth: 18,
      itemStyle: {
        borderRadius: 6,
      },
      data: hourlyData.value.map((item) => item.temperature),
    },
  ],
}))
</script>

<template>
  <div class="weather-center">
    <section class="weather-center__grid">
      <article class="card card--tide">
        <header class="card__header">潮汐曲线</header>
        <BaseChart :option="tideChart" />
      </article>

      <article class="card card--forecast-main">
        <header class="card__header">每日详情</header>
        <div class="daily-strip">
          <button
            v-for="summary in dailySummary"
            :key="summary.day"
            type="button"
            :class="['daily-card', { active: summary.day === activeDay }]"
            @click="activeDay = summary.day"
          >
            <span class="day">{{ summary.label }}</span>
            <span class="condition">{{ summary.condition }}</span>
            <span class="temp">{{ summary.max }}°/{{ summary.min }}°</span>
            <span class="wind">{{ summary.wind }}</span>
          </button>
        </div>
        <div class="detail-panel">
          <div class="status-grid">
            <div class="status-item">
              <p>天气</p>
              <strong>{{ currentSnapshot?.condition ?? '—' }}</strong>
            </div>
            <div class="status-item">
              <p>温度</p>
              <strong>{{ currentSnapshot?.temperature ?? '—' }} °C</strong>
            </div>
            <div class="status-item">
              <p>湿度</p>
              <strong>{{ currentSnapshot?.humidity ?? '—' }} %</strong>
            </div>
            <div class="status-item">
              <p>风向 / 风速</p>
              <strong>{{ currentSnapshot?.windDirection ?? '—' }} {{ currentSnapshot?.windSpeed ?? '—' }} m/s</strong>
            </div>
            <div class="status-item">
              <p>能见度</p>
              <strong>{{ currentSnapshot?.visibility ?? '—' }} km</strong>
            </div>
            <div class="status-item">
              <p>气压</p>
              <strong>{{ currentSnapshot?.pressure ?? '—' }} hPa</strong>
            </div>
          </div>
          <div class="hourly-grid">
            <div v-for="entry in hourlyData" :key="entry.time" class="hourly-entry">
              <p class="hour-time">{{ getTimeLabel(entry.time) }}</p>
              <p class="hour-temp">{{ entry.temperature }}°C</p>
              <p class="hour-wind">{{ entry.wind ?? '—' }} {{ entry.windLevel ?? '—' }}级</p>
            </div>
          </div>
        </div>
      </article>

      <article class="card card--radar">
        <header class="card__header">指标雷达</header>
        <BaseChart :option="radarChart" />
      </article>

      <article class="card card--tideinfo">
        <header class="card__header">潮汐信息</header>
        <ul class="tide-info">
          <li>当前潮位：<strong>{{ tide?.currentHeight ?? '—' }} m</strong></li>
          <li>下次高潮：<strong>{{ tide?.nextHighTide ?? '—' }}</strong></li>
          <li>下次低潮：<strong>{{ tide?.nextLowTide ?? '—' }}</strong></li>
          <li>风暴潮警报：<strong>{{ tide?.surgeWarning ? '是' : '否' }}</strong></li>
        </ul>
      </article>

      <article class="card card--forecast">
        <header class="card__header">24 小时气温</header>
        <BaseChart :option="temperatureChart" />
      </article>
    </section>
  </div>
</template>

<style scoped lang="scss">
.weather-center {
  width: 100%;
  height: 100%;
  padding: 10px 10px;
  display: flex;
  flex-direction: column;
  pointer-events: none;
  background: var(--tech-bg-deep); /* 统一背景 */
  position: relative;
  overflow: hidden;

}

.weather-center__grid {
  display: grid;
  grid-template-columns: 360px minmax(480px, 1fr) 360px;
  grid-template-rows: minmax(260px, auto) minmax(220px, auto);
  grid-template-areas:
    'tide forecast radar'
    'tideinfo forecast temp';
  gap: 12px;
  width: 100%;
  flex: 1;
  pointer-events: none;

}

.weather-center :is(.card, .daily-strip, .detail-panel, .hourly-grid, button) {
  pointer-events: auto;
}

.card--tide { grid-area: tide; }
.card--forecast-main { grid-area: forecast; }
.card--radar { grid-area: radar; }
.card--tideinfo { grid-area: tideinfo; }
.card--forecast { grid-area: temp; }

.card {
  background: rgba(10, 22, 40, 0.85); /* 统一 Tech 背景 */
  backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 242, 255, 0.3); /* 统一 Tech 边框 */
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: hidden;

  /* 装饰 */
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; width: 100%; height: 2px;
    background: linear-gradient(90deg, transparent, #00f2ff, transparent);
    box-shadow: 0 0 10px #00f2ff;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: 0; right: 0;
    width: 20px; height: 20px;
    border-bottom: 2px solid #00f2ff;
    border-right: 2px solid #00f2ff;
  }
}

.card__header {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  letter-spacing: 0.18em;
  color: #00f2ff; /* 统一 Tech 标题色 */
  margin: 0;
  text-transform: uppercase;
}

.daily-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 6px;
}

.daily-strip::-webkit-scrollbar {
  height: 4px;
}
.daily-strip::-webkit-scrollbar-thumb {
  background: rgba(0, 242, 255, 0.25);
  border-radius: 999px;
}

.daily-card {
  flex: 0 0 150px;
  border: 1px solid rgba(0, 242, 255, 0.18);
  background: rgba(10, 22, 40, 0.75);
  color: rgba(210, 227, 255, 0.85);
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  text-align: left;
  transition: all 0.3s;
}

.daily-card.active {
  background: linear-gradient(120deg, rgba(0, 242, 255, 0.18), rgba(0, 102, 255, 0.16));
  border-color: rgba(0, 242, 255, 0.35);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.status-item {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-item strong {
  font-size: 20px;
  color: var(--tech-cyan);
}

.hourly-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  max-height: 360px;
  overflow-y: auto;
  /* Webkit 内核浏览器滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(10, 22, 40, 0.6);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 242, 255, 0.4); /* 科技蓝透明色 */
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 242, 255, 0.6); /* 悬停时更亮 */
  }

  /* Firefox 滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 242, 255, 0.4) rgba(10, 22, 40, 0.6);
}

.card--radar :deep(.base-chart),
.card--tide :deep(.base-chart),
.card--forecast :deep(.base-chart) {
  height: 250px;
}

.tide-info {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tide-info strong {
  color: var(--tech-cyan);
}

@media (max-width: 1400px) {
  .weather-center__grid {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    grid-template-rows: none;
    grid-template-areas: none;
  }
  .card--tide, .card--forecast-main, .card--radar, .card--tideinfo, .card--forecast {
    grid-area: auto;
  }
}
</style>
