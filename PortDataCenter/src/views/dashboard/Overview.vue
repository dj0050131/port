<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import MetricCard from '@/components/common/MetricCard.vue'
import ChartCard from '@/components/common/ChartCard.vue'
import GanttChart from '@/components/common/GanttChart.vue'
import { createBarOption, createLineOption, createPieOption } from '@/utils/chart-utils'
import { useDashboardStore } from '@/stores/dashboard'
import { useNow } from '@/utils/date-utils'
import { fetchResourceTimeline, updatePlanTime } from '@/api/dashboard'
import type { ResourceTimeline } from '@/types/dashboard'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const dashboardStore = useDashboardStore()
const auth = useAuthStore()
const clock = useNow()
const ganttData = ref<ResourceTimeline[]>([])
const ganttLoading = ref(false)
const ganttError = ref<string | null>(null)
const ganttDate = ref(new Date())

const perms = computed(() => auth.user?.permissions || [])
const canViewGantt = computed(() => perms.value.includes('*') || perms.value.includes('DASHBOARD:GANTT:VIEW'))
const canEditGantt = computed(() => perms.value.includes('*') || perms.value.includes('DASHBOARD:GANTT:EDIT'))

const loadGanttData = async () => {
  if (!canViewGantt.value) {
    ganttData.value = []
    ganttError.value = null
    ganttLoading.value = false
    return
  }
  ganttLoading.value = true
  try {
    const now = new Date()
    const start = new Date(now.setHours(0, 0, 0, 0)).toISOString()
    const end = new Date(now.setHours(23, 59, 59, 999)).toISOString()
    
    ganttDate.value = now

    const res = await fetchResourceTimeline(start, end)

    if (res.code === 200 && res.data) {
      ganttData.value = res.data
    }
  } catch (e) {
    console.error('Failed to load gantt data', e)
    ganttError.value = '加载甘特图失败'
  } finally {
    ganttLoading.value = false
  }
}

const handleUpdateTask = async (payload: { planId: number, newStartTime: string, newEndTime: string }) => {
  if (!canEditGantt.value) {
    ElMessage.warning('当前账号无甘特图编辑权限')
    return
  }
  try {
    const res = await updatePlanTime(payload.planId, payload.newStartTime, payload.newEndTime)
    if (res.code === 200) {
      ElMessage.success('计划时间更新成功')
      await loadGanttData() // 刷新数据
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('更新请求失败')
  }
}

onMounted(() => {
  dashboardStore.fetchOverview()
  loadGanttData()
})

const throughputOption = computed(() => {
  const series = dashboardStore.throughputTrend
  if (!series.length) return null
  return createLineOption(
    series.map((item) => item.time),
    series.map((item) => Number(item.value.toFixed(2))),
    '月度吞吐量',
  )
})

const pilotageOption = computed(() => {
  const series = dashboardStore.pilotageTrend
  if (!series.length) return null
  return createBarOption(
    series.map((item) => item.time),
    series.map((item) => Number(item.value.toFixed(2))),
    '引航任务量',
  )
})

const feeOption = computed(() => {
  const series = dashboardStore.feeTrend
  if (!series.length) return null
  return createLineOption(
    series.map((item) => item.time),
    series.map((item) => Number(item.value.toFixed(2))),
    '费收趋势',
  )
})

const resourceOption = computed(() => {
  const series = dashboardStore.resourceUsage
  if (!series.length) return null
  return createPieOption(
    series.map((item) => ({
      name: item.name,
      value: Number(item.value.toFixed(2)),
    })),
    '资源利用率',
  )
})

const alertLevelLabel = (level?: string) => {
  if (level === 'critical') return '严重'
  if (level === 'warning') return '警告'
  return '提示'
}
</script>

<template>
  <div class="overview page-container">
    <section class="overview__top">
      <div class="overview__clock glass-card">
        <h2>实时监控</h2>
        <p>{{ clock.date }}</p>
        <span class="clock__time digital-number">{{ clock.time }}</span>
        <small>数据每 5 分钟自动同步</small>
      </div>
      <div class="overview__metrics">
        <MetricCard
          v-for="metric in dashboardStore.metrics"
          :key="metric.id"
          :label="metric.label"
          :value="metric.value"
          :unit="metric.unit"
          :delta="metric.delta"
          :color="metric.color"
          :loading="dashboardStore.loading"
        />
      </div>
    </section>

    <section class="overview__operations glass-card">
      <header class="section-header">
        <h3>运营概况</h3>
        <span>最后更新：{{ dashboardStore.lastUpdated }}</span>
      </header>
      <div class="operations-grid">
        <div class="operation-item">
          <span class="label">入港船舶</span>
          <span class="value neon-text">{{ dashboardStore.operations?.inboundShips || 0 }} <small>艘</small></span>
        </div>
        <div class="operation-item">
          <span class="label">出港船舶</span>
          <span class="value">{{ dashboardStore.operations?.outboundShips || 0 }} <small>艘</small></span>
        </div>
        <div class="operation-item">
          <span class="label">引航任务</span>
          <span class="value">{{ dashboardStore.operations?.pilotMissions || 0 }} <small>次</small></span>
        </div>
        <div class="operation-item">
          <span class="label">总营收</span>
          <span class="value">{{ dashboardStore.operations?.revenue || 0 }} <small>元</small></span>
        </div>
        <div class="operation-item">
          <span class="label">资源利用率</span>
          <el-progress
            :percentage="Math.round(dashboardStore.operations?.utilization || 0)"
            status="success"
            :stroke-width="10"
          />
        </div>
        <div class="operation-item">
          <span class="label">告警事件</span>
          <span class="value warning">{{ dashboardStore.operations?.alerts || 0 }} <small>件</small></span>
        </div>
      </div>
    </section>

    <section class="overview__charts">
      <ChartCard title="吞吐量趋势" :option="throughputOption" :loading="dashboardStore.loading" />
      <ChartCard title="引航任务趋势" :option="pilotageOption" :loading="dashboardStore.loading" />
      <ChartCard title="费收趋势" :option="feeOption" :loading="dashboardStore.loading" />
      <ChartCard title="资源利用率" :option="resourceOption" :loading="dashboardStore.loading" />
    </section>

    <section v-if="canViewGantt" class="overview__schedule glass-card">
      <header class="section-header">
        <h3>资源调度甘特图 (今日)</h3>
        <el-button link type="primary" size="small" @click="loadGanttData">刷新</el-button>
      </header>
      <div class="gantt-wrapper-box">
        <GanttChart 
          v-if="!ganttError"
          :resources="ganttData" 
          :date="ganttDate"
          :loading="ganttLoading"
          :editable="canEditGantt"
          @update-task="handleUpdateTask"
        />
        <div v-else class="error-message">
          {{ ganttError }}
        </div>
      </div>
    </section>

    <section class="overview__alerts glass-card">
      <header class="section-header">
        <h3>实时告警</h3>
        <span>近 6 小时</span>
      </header>
      <el-scrollbar height="220px">
        <ul class="alert-list">
          <li v-for="alert in dashboardStore.alerts" :key="alert.id" class="alert-item">
            <div class="alert-item__header">
              <span class="alert-item__title">{{ alert.title }}</span>
              <el-tag
                size="small"
                :type="alert.level === 'critical' ? 'danger' : alert.level === 'warning' ? 'warning' : 'info'"
              >
                {{ alertLevelLabel(alert.level) }}
              </el-tag>
            </div>
            <p class="alert-item__content">{{ alert.content }}</p>
            <span class="alert-item__time">{{ alert.timestamp }}</span>
          </li>
        </ul>
      </el-scrollbar>
    </section>
  </div>
</template>

<style scoped lang="scss">
.overview__top {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
}

.overview__clock {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  border: 1px solid rgba(143, 214, 255, 0.24);

  h2 {
    margin: 0;
    letter-spacing: 0.16em;
    color: var(--tidal-text-main);
  }

  p {
    margin: 0;
    color: var(--tidal-text-secondary);
  }

  .clock__time {
    font-size: 28px;
  }

  small {
    color: var(--tidal-text-secondary);
  }
}

.overview__metrics {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.overview__operations {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  letter-spacing: 0.08em;
  h3 {
    margin: 0;
    color: var(--tidal-text-main);
  }
  span {
    font-size: 12px;
    color: var(--tidal-text-secondary);
  }
}

.operations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.operation-item {
  padding: 16px;
  border-radius: 14px;
  background: var(--tidal-bg-card);
  border: 1px solid var(--tidal-border);
  display: flex;
  flex-direction: column;
  gap: 12px;

  .label {
    font-size: 12px;
    color: var(--tidal-text-secondary);
  }

  .value {
    font-size: 20px;
    font-weight: 600;
    display: flex;
    align-items: baseline;
    gap: 6px;

    small {
      font-size: 12px;
      color: var(--tidal-text-secondary);
    }
  }

  .value.warning {
    color: #faad14;
  }
}

.overview__charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.overview__schedule {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.gantt-wrapper-box {
  height: 500px;
  width: 100%;
}

.overview__alerts {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-list {
  display: grid;
  gap: 12px;
}

.alert-item {
  padding: 16px;
  border-radius: 12px;
  background: var(--tidal-bg-card);
  border: 1px solid var(--tidal-border);
  display: grid;
  gap: 8px;
}

.alert-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-item__title {
  font-size: 14px;
  font-weight: 600;
}

.alert-item__content {
  font-size: 13px;
  color: var(--tidal-text-secondary);
  margin: 0;
}

.alert-item__time {
  font-size: 12px;
  color: var(--tidal-text-secondary);
}

@media (max-width: 1280px) {
  .overview__top {
    grid-template-columns: 1fr;
  }
}
.error-message {
  padding: 20px;
  text-align: center;
  color: #f56c6c;
}

.rendering-state {
  position: relative;
}
</style>