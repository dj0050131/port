<script setup lang="ts">
import { onMounted } from 'vue'
import { usePilotageStore } from '@/stores/pilotage'
import { fromNow, formatDateTime } from '@/utils/date-utils'

const pilotageStore = usePilotageStore()

onMounted(() => {
  pilotageStore.fetchAll()
})

const statusTypeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | ''> = {
  pending: 'warning',
  approved: 'primary',
  scheduled: 'primary',
  'in-progress': 'warning',
  completed: 'success',
  cancelled: 'danger',
}
</script>

<template>
  <div class="pilotage page-container">
    <section class="glass-card summary">
      <header class="section-title">
        <h2>引航任务看板</h2>
        <span>实时监控引航申请、计划与调度</span>
      </header>
      <div class="summary-grid">
        <div v-for="item in pilotageStore.statusStatistics" :key="item.type" class="summary-card">
          <span class="label">{{ item.label }}</span>
          <strong class="value neon-text">{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <section class="glass-card section">
      <header class="section-title">
        <h3>引航申请</h3>
        <span>实时审批进度</span>
      </header>
      <el-table :data="pilotageStore.applications" stripe v-loading="pilotageStore.loading" height="320">
        <el-table-column prop="id" label="申请号" width="120" />
        <el-table-column prop="shipName" label="船名" width="140" />
        <el-table-column prop="agent" label="代理公司" width="140" />
        <el-table-column prop="berth" label="泊位" width="80" />
        <el-table-column prop="requestedAt" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.requestedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="eta" label="预计靠泊" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.eta) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">
              {{ row.status.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
      <div class="table-footer">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pilotageStore.pagination.pageSize"
          :total="pilotageStore.pagination.total"
          :current-page="pilotageStore.pagination.page"
          @current-change="pilotageStore.fetchApplications"
        />
      </div>
    </section>

    <section class="panel-grid">
      <div class="glass-card">
        <header class="section-title">
          <h3>引航计划</h3>
          <span>今日执行安排</span>
        </header>
        <el-timeline>
          <el-timeline-item
            v-for="plan in pilotageStore.plans"
            :key="plan.id"
            :timestamp="formatDateTime(plan.startTime)"
            placement="top"
            :type="statusTypeMap[plan.status] === 'success' ? 'success' : 'primary'"
          >
            <div class="timeline-item">
              <h4>{{ plan.pilotName }} · {{ plan.route.join(' ➜ ') }}</h4>
              <p>关联申请：{{ plan.applicationId }} · 预计完成：{{ formatDateTime(plan.endTime) }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="glass-card">
        <header class="section-title">
          <h3>调度指令</h3>
          <span>引航艇准备状态</span>
        </header>
        <el-scrollbar height="320px">
          <ul class="dispatch-list">
            <li v-for="order in pilotageStore.dispatchOrders" :key="order.id" class="dispatch-item">
              <div class="dispatch-item__header">
                <strong>{{ order.vessel }}</strong>
                <el-tag size="small" :type="statusTypeMap[order.status]">
                  {{ order.status.toUpperCase() }}
                </el-tag>
              </div>
              <p>引航员：{{ order.pilotName }} · 联系方式：{{ order.contact }}</p>
              <p>出发：{{ formatDateTime(order.departure) }} · 抵达：{{ formatDateTime(order.arrival) }}</p>
            </li>
          </ul>
        </el-scrollbar>
      </div>
    </section>

    <section class="glass-card">
      <header class="section-title">
        <h3>任务时间轴</h3>
        <span>最新执行进展</span>
      </header>
      <el-timeline>
        <el-timeline-item
          v-for="item in pilotageStore.timeline"
          :key="item.id"
          :timestamp="fromNow(item.time)"
          placement="top"
        >
          <div class="timeline-item">
            <h4>{{ item.description }}</h4>
            <span>{{ formatDateTime(item.time) }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </section>
  </div>
</template>

<style scoped lang="scss">
.summary {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  h2,
  h3 {
    margin: 0;
    letter-spacing: 0.14em;
  }
  span {
    font-size: 12px;
    color: rgba(200, 230, 255, 0.72);
  }
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.summary-card {
  padding: 16px;
  border-radius: 14px;
  background: rgba(15, 28, 60, 0.34);
  border: 1px solid rgba(143, 214, 255, 0.18);
  display: flex;
  flex-direction: column;
  gap: 10px;

  .label {
    font-size: 12px;
    color: rgba(200, 230, 255, 0.7);
  }

  .value {
    font-size: 26px;
  }
}

.section {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.dispatch-list {
  display: grid;
  gap: 12px;
}

.dispatch-item {
  border-radius: 12px;
  border: 1px solid rgba(143, 214, 255, 0.18);
  padding: 16px;
  background: rgba(15, 28, 60, 0.32);
  display: grid;
  gap: 8px;

  p {
    margin: 0;
    font-size: 13px;
    color: rgba(200, 230, 255, 0.72);
  }
}

.dispatch-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  h4 {
    margin: 0;
    font-size: 14px;
  }
  span,
  p {
    font-size: 12px;
    color: rgba(200, 230, 255, 0.72);
    margin: 0;
  }
}
</style>
