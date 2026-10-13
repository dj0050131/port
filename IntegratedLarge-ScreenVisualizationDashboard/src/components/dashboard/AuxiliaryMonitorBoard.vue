<script setup lang="ts">
import ChartCard from '@/components/common/ChartCard.vue'
import type { AlertItem, WeatherTide } from '@/types/dashboard'
import type { EChartsOption } from 'echarts'
import {onMounted} from "vue";

const props = defineProps<{
  weeklyOption: EChartsOption
  shipTypeOption: EChartsOption
  weather: WeatherTide | null | undefined
  weatherMetrics: {
    pressure: string
    tide: string
    humidity?: string
    temperature?: string
    pm25?: string
  }
  alerts: AlertItem[]
}>()

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

const alertLevelLabel = (lvl?: string) => {
  const key = String(lvl || '').toLowerCase()
  return key === 'critical' ? '紧急' : key === 'warning' ? '预警' : key === 'info' ? '提示' : '常规'
}

onMounted(() => {
})
</script>

<template>
  <div class="aux-board">
    <div class="aux-left-panel">
      <section class="tech-panel-box aux-weekly">
        <header class="panel-title">七日引航统计</header>
        <div class="chart-wrapper chart-wrapper--weekly">
          <ChartCard variant="plain" :option="props.weeklyOption" />
        </div>
      </section>

      <div class="aux-bottom-group">
        <section v-if="props.weather" class="tech-panel-box aux-weatheraux-weather">
          <header class="panel-title">港口气象信息</header>
          <div class="weather-main">
            <div>
              <p class="weather-temp">{{ props.weather?.weather?.temperature ?? '--' }}°C</p>
              <p class="weather-condition">{{ props.weather?.weather?.condition ?? '—' }}</p>
            </div>
            <div class="weather-meta">
              <p>湿度 {{ props.weather?.weather?.humidity ?? '--' }}%</p>
              <p>能见度 {{ props.weather?.weather?.visibility ?? '--' }} km</p>
              <p>
                风速 {{ props.weather?.weather?.windSpeed ?? '--' }} m/s
                ({{ props.weather?.weather?.windDirection ?? '--' }})
              </p>
            </div>
          </div>
          <div class="weather-grid">
            <div>
              <span class="label">气压</span>
              <strong>{{ props.weatherMetrics.pressure }} hPa</strong>
            </div>
            <div>
              <span class="label">潮位</span>
              <strong>{{ props.weatherMetrics.tide }} m</strong>
            </div>
            <div>
              <span class="label">能见度</span>
              <strong>{{ props.weather?.weather?.visibility ?? '--' }} km</strong>
            </div>
          </div>
        </section>

        <section class="tech-panel-box aux-ship-type">
          <header class="panel-title">船舶类型结构</header>
          <div class="chart-wrapper chart-wrapper--small">
            <ChartCard variant="plain" :option="props.shipTypeOption" />
          </div>
        </section>
      </div>
    </div>

    <section class="tech-panel-box aux-alerts">
      <header class="panel-title">港口监控告警</header>
      <div class="monitor-list monitor-list--scroll">
        <div v-for="alert in props.alerts" :key="alert.id" class="monitor-item">
          <div class="monitor-left">
            <p class="monitor-message">{{ alertTypeLabel(alert.alertType) }}：{{ alert.message }}</p>
            <p v-if="alert.detail" class="monitor-detail">{{ alert.detail }}</p>
            <p class="monitor-time">{{ alert.timestamp }}</p>
          </div>
          <span class="monitor-level" :class="String(alert.level || '').toLowerCase()">{{ alertLevelLabel(alert.level) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.aux-board {
  flex: 1;
  display: grid;
  /* 左侧62%，右侧剩余(38%)，增加右侧宽度 */
  grid-template-columns: 62% 1fr;
  gap: 12px;
  padding: 0px;
  min-height: 0;
}

.aux-left-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.aux-weekly {
  flex: 0 0 auto;
}

.aux-bottom-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  /* 确保高度一致 */
  align-items: stretch;
}

.aux-weather,
.aux-ship-type {
  min-width: 0;
}

.aux-alerts {
  display: flex;
  flex-direction: column;
  /* 高度自动撑满，与左侧对齐 */
  height: 100%;
}

.chart-wrapper--weekly {
  height: 280px; /* 保持原有高度 */
  width: 100%;
}

.chart-wrapper--small {
  height: 200px; /* 适当调小以适配 */
  width: 100%;
}

.monitor-left {
  min-width: 0;
}

.monitor-list--scroll {
  flex: 1;
  overflow-y: auto;
  padding-right: 6px;
  min-height: 0;
}
/* Scrollbar styling for monitor list */
.monitor-list--scroll::-webkit-scrollbar {
  width: 4px;
}
.monitor-list--scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 242, 255, 0.2);
  border-radius: 2px;
}
.monitor-list--scroll::-webkit-scrollbar-track {
  background: transparent;
}

/* Tech Panel Style Reuse */
.tech-panel-box {
  height: auto;
  pointer-events: auto;
  background: rgba(10, 22, 40, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 242, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;

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

.chart-wrapper {
  /* Default fallback */
  height: 280px;
  width: 100%;
}

.panel-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  letter-spacing: 0.1em;
  margin-bottom: 18px;
  color: #00f2ff;
  position: relative;
  text-transform: uppercase;
}

.panel-weekly :deep(.chart-card),
.panel-ship-type :deep(.chart-card) {
  padding: 0;
  background: transparent;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.panel-weekly :deep(.chart-card__body),
.panel-ship-type :deep(.chart-card__body) {
  padding: 0;
  flex: 1;
  min-height: 0;
}

.weather-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: auto; /* Push grid to bottom */
}

.weather-temp {
  font-family: var(--font-num);
  font-size: 36px;
  color: var(--tech-cyan);
  margin-bottom: 4px;
  text-shadow: var(--tech-glow);
}

.weather-condition {
  font-size: 14px;
  letter-spacing: 0.12em;
  color: var(--tech-text-main);
}

.weather-meta {
  text-align: right;
  font-size: 12px;
  color: var(--tech-text-dim);
  line-height: 1.6;
}

.weather-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.weather-grid .label {
  font-size: 12px;
  color: var(--tech-text-dim);
}

.weather-grid strong {
  display: block;
  margin-top: 6px;
  font-size: 16px;
  font-family: var(--font-num);
  color: var(--tech-text-main);
}

.monitor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s;
}

.monitor-item:hover {
  background: rgba(0, 242, 255, 0.05);
  border-color: var(--tech-cyan);
}

.monitor-message {
  font-size: 14px;
  color: var(--tech-text-main);
  margin-bottom: 6px;
}

.monitor-detail {
  font-size: 12px;
  color: rgba(213, 236, 255, 0.75);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.monitor-time {
  font-size: 12px;
  color: var(--tech-text-dim);
}

.monitor-level {
  padding: 4px 14px;
  border-radius: 2px;
  font-size: 12px;
  text-transform: uppercase;
  align-self: center;
  border: 1px solid currentColor;
  white-space: nowrap;
}

.monitor-level.warning { color: #faad14; box-shadow: 0 0 5px rgba(250, 173, 20, 0.2); }
.monitor-level.critical { color: #ff3366; box-shadow: 0 0 5px rgba(255, 51, 102, 0.2); }
.monitor-level.info { color: #00f2ff; box-shadow: 0 0 5px rgba(0, 242, 255, 0.2); }

@media (max-width: 1024px) {
  .aux-board {
    grid-template-columns: 1fr;
  }
  .weather-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .aux-board {
    padding: 0;
  }
  .weather-grid {
    grid-template-columns: 1fr;
  }
}
</style>

