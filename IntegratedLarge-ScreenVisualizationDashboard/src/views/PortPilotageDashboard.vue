<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import AuxiliaryMonitorBoard from '@/components/dashboard/AuxiliaryMonitorBoard.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import { buildShipTypePieOption, buildWeeklyPilotOption } from '@/utils/chart-utils'
import type { ShipType } from '@/types/dashboard'

const store = useDashboardStore()
const route = useRoute()
const insightTab = ref<'ranking' | 'system'>('ranking')
const activeView = computed(() => (route.meta?.view === 'aux' ? 'aux' : 'main'))

const formatNumber = (value: number | undefined) => (value ?? 0).toLocaleString('zh-CN')

const metricCards = computed(() => {
  const totalPlans = store.pilotPlans.length
  const completed = store.pilotPlans.filter((plan) => plan.status === 'COMPLETED').length
  const executing = store.pilotPlans.filter((plan) => plan.status === 'ONGOING').length
  const ships = store.shipDynamics.length
  return [
    { label: '今日引航计划', value: totalPlans },
    { label: '已完成', value: completed },
    { label: '执行中', value: executing },
    { label: '在港船舶', value: ships },
  ]
})

const shipTypeLabel: Record<ShipType, string> = {
  CARGO: '杂货船',
  CONTAINER: '集装箱船',
  TANKER: '油轮',
  LNG: 'LNG',
  CHEMICAL: '化学品船',
  'RO-RO': '滚装船',
  GAS: '液化气船',
  CAR: '汽车运输船',
}

const getShipTypeName = (raw: string | undefined): string => {
  if (!raw) return '其他'
  const key = String(raw).trim()
  const direct = shipTypeLabel[key as ShipType]
  if (direct) return direct
  const upper = key.toUpperCase()
  const upperHit = shipTypeLabel[upper as ShipType]
  if (upperHit) return upperHit

  // 再按中文关键词兜底（后端当前返回 vessels.vessel_type，可能是中文）
  if (key.includes('集装箱')) return shipTypeLabel.CONTAINER
  if (key.includes('油')) return shipTypeLabel.TANKER
  if (upper.includes('LNG')) return shipTypeLabel.LNG
  if (key.includes('化') || key.includes('化工')) return shipTypeLabel.CHEMICAL
  if (key.includes('滚') || upper.includes('RO')) return shipTypeLabel['RO-RO']
  if (key.includes('气') || upper.includes('GAS')) return shipTypeLabel.GAS
  if (key.includes('车') || upper.includes('CAR')) return shipTypeLabel.CAR
  return shipTypeLabel.CARGO
}

const shipTypeStats = computed(() => {
  const map = new Map<string, number>()
  store.shipDynamics.forEach((ship) => {
    const name = getShipTypeName(ship.type)
    map.set(name, (map.get(name) ?? 0) + 1)
  })
  return [...map.entries()].map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

const shipTypeOption = computed(() => buildShipTypePieOption(shipTypeStats.value))

const shipRanking = computed(() =>
  shipTypeStats.value.map((item, index) => ({
    rank: index + 1,
    name: item.name,
    value: item.value,
  })),
)

const pilotRanking = computed(() => {
  const map = new Map<string, number>()
  store.pilotPlans.forEach((plan) => {
    map.set(plan.pilotName, (map.get(plan.pilotName) ?? 0) + 1)
  })
  return [...map.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 6)
    .map((item, idx) => ({ rank: idx + 1, ...item }))
})

const weeklySeries = computed(() => {
  const source = store.operationsOverview?.energyTrend ?? []
  return source.slice(0, 7).map((item, index) => ({
    label: item.time,
    value: item.value,
  }))
})

const weeklyOption = computed(() => buildWeeklyPilotOption(weeklySeries.value))

const resourceStatus = computed(() => {
  return store.systemStatus.map(item => {
    const moduleMap: Record<string, string> = {
      'PILOTS': '引航员',
      'DRIVERS': '驾驶员',
      'PILOT_BOATS': '引航艇',
      'TUGBOATS': '港口拖轮',
      'VEHICLES': '调度车辆'
    };

    //   'HEALTHY': '正常',
    //   'DEGRADED': '繁忙',
    //   'OFFLINE': '离线'
    // };

    return {
      moduleName: moduleMap[item.moduleName] || item.moduleName, // 转换模块名为中文
      lastUpdate: item.lastUpdate,
      status: String(item.status || '').toLowerCase(), // 保持英文状态用于CSS类（避免空值导致报错）
      value: `${item.count}/${item.total}`
    };
  });
})

const monitorList = computed(() => store.alerts.slice(0, 5))

const weather = computed(() => store.weather)
const weatherMetrics = computed(() => {
  const info = weather.value
  if (!info) {
    return { pressure: '--', humidity: '--', temperature: '--', tide: '--' }
  }
  return {
    pressure: info.weather?.pressure ?? '--',
    humidity: info.weather?.humidity ?? '--',
    temperature: info.weather?.temperature ?? '--',
    tide: info.tide?.currentHeight ?? '--',
  }
})

const mapShips = computed(() => store.mapPayload?.ships ?? store.shipDynamics ?? [])
const mapBerths = computed(() => store.mapPayload?.berths ?? store.berths ?? [])
const mapRoutes = computed(() => store.mapPayload?.routes ?? [])

</script>

<template>
  <div class="overview-screen">

    <section v-if="activeView === 'main'" class="overlay-stage">
      <section class="overlay-panel overlay-panel--left">
        <header class="overlay-panel__title">引航运行指标</header>
            <div class="metrics-grid">
              <div v-for="metric in metricCards" :key="metric.label" class="metric-item">
                <span class="metric-label">{{ metric.label }}</span>
                <span class="metric-value">{{ formatNumber(metric.value) }}</span>
              </div>
            </div>
          </section>

      <section class="overlay-panel overlay-panel--right">
        <header class="overlay-panel__title">资源与系统</header>
            <el-tabs v-model="insightTab" class="insight-tabs" stretch>
              <el-tab-pane label="引航排行" name="ranking">
                <div class="insight-rankings">
                  <div class="ranking-block">
                    <p class="ranking-block__title">船舶类型</p>
                    <div class="ranking-table">
                      <div v-for="item in shipRanking" :key="`ship-${item.name}`" class="ranking-row">
                        <span class="rank-tag">{{ item.rank }}</span>
                        <span class="ranking-name">{{ item.name }}</span>
                        <span class="ranking-value">{{ item.value }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="ranking-block">
                    <p class="ranking-block__title">引航员</p>
                    <div class="ranking-table">
                      <div v-for="item in pilotRanking" :key="`pilot-${item.name}`" class="ranking-row">
                        <span class="rank-tag alt">{{ item.rank }}</span>
                        <span class="ranking-name">{{ item.name }}</span>
                        <span class="ranking-value">{{ item.value }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="资源状态" name="system">
                <div class="device-list">
                  <div v-for="res in resourceStatus" :key="res.moduleName" class="device-item">
                  <div>
                      <p class="device-name">{{ res.moduleName }}</p>
                      <p class="device-update">状态: {{ res.value }}</p>
                    </div>
                    <span class="device-status" :class="res.status">{{ res.status === 'healthy' ? '正常' : '繁忙' }}</span>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </section>
    </section>

    <section v-else class="dashboard-aux glass-card">
      <AuxiliaryMonitorBoard
        :weekly-option="weeklyOption"
        :ship-type-option="shipTypeOption"
        :weather="weather"
        :weather-metrics="weatherMetrics"
        :alerts="monitorList"
      />
    </section>

  </div>
</template>

<style scoped lang="scss">
.overview-screen {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 8px 16px;
  background: transparent; /* Let body background show through */
}

/* Glass / Tech Panel Base */
.tech-panel {
  background: rgba(10, 22, 40, 0.75);
  backdrop-filter: blur(12px);
  border: 1px solid var(--tech-border);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px; /* Tech look is usually sharper */
  position: relative;
  overflow: hidden;
}

/* Corner Accents */
.tech-panel::before,
.tech-panel::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  border: 2px solid var(--tech-cyan);
  transition: all 0.3s ease;
}

.tech-panel::before {
  top: 0; left: 0;
  border-right: none; border-bottom: none;
}
.tech-panel::after {
  bottom: 0; right: 0;
  border-left: none; border-top: none;
}

/* Overlay Layout */
.overlay-stage {
  position: absolute; /* Changed from relative to absolute to sit on top of map */
  inset: 0;
  z-index: 10;
  pointer-events: none; /* Crucial: lets clicks pass through to map */
  padding-top: 10px;
}

.overlay-panel {
  @extend .tech-panel;
  position: absolute;
  top: 160px; /* Increased from 110px to completely clear header/nav */
  width: min(420px, 30vw);
  padding: 22px 26px 26px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  pointer-events: auto; /* Re-enable pointer events for panels */
  background: rgba(10, 22, 40, 0.85);
  max-height: calc(100vh - 180px); /* Adjust height calculation */
  overflow-y: auto;
}

.overlay-panel--left { left: 2%; }
.overlay-panel--right { right: 2%; }

.overlay-panel__title {
  font-family: var(--font-num); /* Tech font for titles */
  font-size: 18px;
  letter-spacing: 0.1em;
  color: var(--tech-cyan);
  text-transform: uppercase;
  border-bottom: 1px solid rgba(0, 242, 255, 0.2);
  padding-bottom: 12px;
  margin: 0;
  text-shadow: var(--tech-glow);
}

/* Metrics */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.metric-item {
  background: rgba(0, 242, 255, 0.05);
  border: 1px solid rgba(0, 242, 255, 0.1);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: transform 0.3s;
}

.metric-item:hover {
  transform: translateY(-2px);
  background: rgba(0, 242, 255, 0.1);
  border-color: var(--tech-cyan);
}

.metric-label {
  font-size: 12px;
  color: var(--tech-text-dim);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-value {
  font-family: var(--font-num);
  font-size: 24px;
  color: var(--tech-text-main);
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

/* Tabs */
.insight-tabs :deep(.el-tabs__nav) {
  width: 100%;
  border-radius: 0;
  border: 1px solid var(--tech-blue);
  background: rgba(0, 0, 0, 0.3);
}

.insight-tabs :deep(.el-tabs__item) {
  color: var(--tech-text-dim);
  font-family: var(--font-main);
}

.insight-tabs :deep(.el-tabs__item.is-active) {
  color: #fff;
  background: var(--tech-blue);
}

/* Rankings */
.insight-rankings {
  display: flex;
  gap: 16px;
  max-height: 280px;
  overflow-y: auto;
  padding-right: 6px;
}

.ranking-block { flex: 1; min-width: 0; }
.ranking-block__title {
  font-size: 13px;
  color: var(--tech-cyan);
  margin-bottom: 12px;
  border-left: 3px solid var(--tech-cyan);
  padding-left: 8px;
}

.ranking-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  margin-bottom: 8px;
  background: linear-gradient(90deg, rgba(0, 242, 255, 0.05), transparent);
  border-bottom: 1px solid rgba(0, 242, 255, 0.1);
}

.rank-tag {
  width: 24px; height: 24px;
  background: var(--tech-blue);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold;
  font-family: var(--font-num);
  font-size: 12px;
}
.rank-tag.alt { background: var(--tech-purple); }

.ranking-name { flex: 1; color: var(--tech-text-main); font-size: 14px; }
.ranking-value { color: var(--tech-cyan); font-family: var(--font-num); }

/* Device List */
.device-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-left: 2px solid transparent;
}
.device-item:hover { border-left-color: var(--tech-cyan); }

.device-name { color: #fff; font-size: 14px; }
.device-update { color: var(--tech-text-dim); font-size: 11px; }

.device-status {
  padding: 2px 8px;
  font-size: 11px;
  text-transform: uppercase;
  border: 1px solid currentColor;
}
.device-status.healthy { color: #00ff9d; box-shadow: 0 0 5px rgba(0, 255, 157, 0.2); }
.device-status.degraded { color: #ffbf00; }
.device-status.offline { color: #ff3366; }

/* Aux View Container - Make transparent to show map */
.dashboard-aux {
  flex: 1;
  min-height: 0;
  padding: 12px;
  display: flex;
  pointer-events: none; /* Allow clicks to pass through to map */
}

/* Ensure children panels capture clicks */
.dashboard-aux :deep(.panel),
.dashboard-aux :deep(.tech-panel) {
  pointer-events: auto;
}

@media (max-width: 1400px) {
  .overlay-panel {
    position: static;
    transform: none;
    width: 100%;
    margin: 12px auto;
  }
}
</style>
