<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { fetchAlertList } from '@/api/dashboard'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<any>('pilotage.alerts', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const rawAlerts = ref<any[]>([])
const searchForm = reactive({
  dateRange: [],
  type: ''
})

const alertLevelLabel = (level: string | undefined) => {
  if (!level) return '--'
  const key = level.toUpperCase()
  const map: Record<string, string> = {
    CRITICAL: '紧急',
    ERROR: '紧急',
    WARNING: '预警',
    INFO: '提示',
    NORMAL: '常规',
  }
  return map[key] || level
}

const alertTypeLabel = (type: string | undefined) => {
  if (!type) return '--'
  const map: Record<string, string> = {
    BILL_OVERDUE: '账单逾期',
    PILOTAGE_DELAYED: '计划延误',
    PILOT_OFF_DUTY: '引航员不在岗',
    DRIVER_OFF_DUTY: '驾驶员不在岗',
    VEHICLE_MAINTENANCE: '车辆维保',
    TUGBOAT_MAINTENANCE: '拖轮维保',
    BERTH_UNAVAILABLE: '泊位不可用',
  }
  return map[type] || type
}

const alertTypeOptions = computed(() => {
  const options = new Map<string, string>()
  rawAlerts.value.forEach((item) => {
    const type = item?.alertType
    if (type) {
      options.set(type, alertTypeLabel(type))
    }
  })
  return Array.from(options.entries()).map(([value, label]) => ({ value, label }))
})

const tableData = computed(() => {
  const [start, end] = searchForm.dateRange || []
  return rawAlerts.value
    .filter((item) => {
      if (searchForm.type && item.alertType !== searchForm.type) {
        return false
      }
      if (start || end) {
        const ts = item.timestamp ? new Date(item.timestamp).getTime() : 0
        if (start && ts < new Date(start).getTime()) return false
        if (end && ts > new Date(end).getTime() + 24 * 60 * 60 * 1000) return false
      }
      return true
    })
    .map((item) => ({
      ...item,
      type: alertTypeLabel(item.alertType),
      level: alertLevelLabel(item.level),
      details: item.message || '--',
      remarks: item.detail || '--',
      startDate: item.timestamp || '--',
    }))
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchAlertList()
    if (res.code === 200) {
      rawAlerts.value = res.data || []
      tableState.items = rawAlerts.value
      tableState.total = rawAlerts.value.length
    } else {
      rawAlerts.value = []
      tableState.items = []
      tableState.total = 0
      ElMessage.error(res.message || '获取预警数据失败')
    }
  } catch (error) {
    rawAlerts.value = []
    tableState.items = []
    tableState.total = 0
    ElMessage.error('获取预警数据失败')
  } finally {
    tableState.loading = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="alerts-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">预警管理</h2>
        <p class="page-desc">航行安全及作业风险预警信息管理</p>
      </div>
    </div>

    <div class="search-bar glass-card">
       <el-form inline size="small">
         <el-form-item label="起始日期">
           <el-date-picker v-model="searchForm.dateRange[0]" type="date" placeholder="25年10月25日" />
         </el-form-item>
         <el-form-item label="终止日期">
           <el-date-picker v-model="searchForm.dateRange[1]" type="date" placeholder="25年11月25日" />
         </el-form-item>
         <el-form-item label="类型">
           <el-select v-model="searchForm.type" placeholder="请选择" style="width: 160px" clearable>
             <el-option
               v-for="item in alertTypeOptions"
               :key="item.value"
               :label="item.label"
               :value="item.value"
             />
           </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
         </el-form-item>
       </el-form>
    </div>

    <div class="table-container glass-card">
       <el-table :data="tableData" v-loading="tableState.loading" stripe height="100%" size="small">
          <el-table-column type="selection" width="40" />
          <el-table-column prop="type" label="种类" width="120" />
          <el-table-column prop="level" label="等级" width="100" />
          <el-table-column prop="details" label="详情" min-width="200" show-overflow-tooltip />
          <el-table-column prop="startDate" label="有效期-始" width="140" />
          <el-table-column prop="remarks" label="备注" min-width="150" />
       </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.alerts-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
  .page-desc {
    font-size: 12px;
    color: var(--tidal-text-secondary);
    margin: 4px 0 0;
  }
}

.search-bar { padding: 12px 16px; }
.table-container { flex: 1; padding: 16px; overflow: hidden; }

:deep(.pilotage-dialog) {
  .el-dialog__body { padding: 0; }
  .el-dialog__header { border-bottom: 1px solid var(--tidal-border); margin-right: 0; }
}
</style>