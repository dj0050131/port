<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  fetchMyTasks, 
  fetchMyTaskDetail,
  createConfirmation 
} from '@/api/planConfirmations'
import type { MyTaskRecord, PlanConfirmationDTO } from '@/types/planConfirmations'
import { ConfirmMethod } from '@/types/planConfirmations'
import { Check, View, Refresh } from '@element-plus/icons-vue'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<MyTaskRecord>('dashboard.myTasks', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 10,
  loading: false,
})
const detailVisible = ref(false)
const currentTask = ref<MyTaskRecord | null>(null)
const confirming = ref(false)
const refreshing = ref(false)

const queryParams = reactive({
  page: tableState.page,
  pageSize: tableState.pageSize,
  status: 'PENDING' // 默认查询待确认任务
})

const statusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '系统取消', value: 'SYSTEM_CANCELLATION' },
  { label: '全部', value: '' }
]

const planStatusMap: Record<string, { label: string, type: string }> = {
  PUBLISHED: { label: '已发布', type: 'success' },
  CONFIRMED: { label: '已确认', type: 'success' },
  ONGOING: { label: '进行中', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'info' },
  // 提升对比度：灰色在浅色背景上不清晰
  CERTIFICATED: { label: '已签证', type: 'success' },
  BILLED: { label: '已计费', type: 'warning' },
  PAID: { label: '已付款', type: 'success' },
  REPLAN_REQUIRED: { label: '待重排', type: 'danger' },
  REPLANNED: { label: '重计划', type: 'warning' },
  PLANNED: { label: '已计划', type: 'primary' },
  PLANNING: { label: '计划中', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'info' },
}

const isPlanLocked = (planStatus?: string) => {
  return ['ONGOING', 'COMPLETED', 'CERTIFICATED', 'BILLED', 'PAID', 'CANCELLED'].includes(planStatus || '')
}

const canConfirm = (task: MyTaskRecord) => {
  // 后端确认条件：计划必须 PUBLISHED；并且任务当前为待确认
  if (task.confirmationStatus !== 'PENDING') return false
  if (task.planStatus !== 'PUBLISHED') return false
  if (isPlanLocked(task.planStatus)) return false
  return true
}

const loadData = async () => {
  tableState.loading = true
  try {
    const res = await fetchMyTasks({
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      status: queryParams.status || undefined
    })
    if (res.code === 200 && res.data) {
      tableState.items = res.data.records
      tableState.total = res.data.total
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载任务列表失败')
  } finally {
    tableState.loading = false
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    await loadData()
    ElMessage.success('已刷新')
  } finally {
    refreshing.value = false
  }
}

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.page = 1
  tableState.pageSize = val
  tableState.page = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  tableState.page = val
  loadData()
}

const handleStatusChange = () => {
  queryParams.page = 1
  tableState.page = 1
  loadData()
}

const viewDetail = async (task: MyTaskRecord) => {
  detailVisible.value = true
  currentTask.value = task
  try {
    const res = await fetchMyTaskDetail(task.assignmentId)
    if (res.code === 200 && res.data) {
      currentTask.value = res.data as any
    }
  } catch (e) {
    // 失败不阻塞展示
  }
}

const handleConfirm = async (task: MyTaskRecord) => {
  if (!canConfirm(task)) {
    if (isPlanLocked(task.planStatus)) {
      ElMessage.warning('计划已开始或已结束，不能再确认任务')
    } else if (task.planStatus !== 'PUBLISHED') {
      ElMessage.warning('计划未发布，不能确认任务')
    } else {
      ElMessage.warning('当前状态不可确认')
    }
    return
  }

  try {
    await ElMessageBox.confirm('确认接收此引航任务吗？', '确认提示', {
      confirmButtonText: '确认接收',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    confirming.value = true
    const data: PlanConfirmationDTO = {
      planId: task.planId,
      resourceType: task.resourceType,
      resourceId: task.resourceId,
      confirmMethod: ConfirmMethod.WEB,
      notes: 'Web端确认'
    }
    
    const res = await createConfirmation(data)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      if (detailVisible.value) {
        detailVisible.value = false
      }
      loadData()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('操作异常')
    }
  } finally {
    confirming.value = false
  }
}

const formatDateTime = (str: string) => {
  if (!str) return '-'
  return new Date(str).toLocaleString()
}

const safeText = (v: any) => {
  if (v === null || v === undefined || v === '') return '-'
  return String(v)
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'CONFIRMED': return 'success'
    case 'CANCELLED': return 'info'
    case 'SYSTEM_CANCELLATION': return 'info'
    default: return 'info'
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="my-tasks page-container">
    <div class="glass-card table-container">
      <div class="header-actions">
        <h3>我的任务</h3>
        <div style="display:flex; gap:10px; align-items:center;">
          <el-radio-group v-model="queryParams.status" @change="handleStatusChange">
            <el-radio-button v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" :loading="refreshing" @click="handleRefresh">刷新</el-button>
        </div>
      </div>

      <el-table 
        v-loading="tableState.loading" 
        :data="tableState.items" 
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column prop="planCode" label="计划编号" min-width="140" />
        <el-table-column label="计划状态" width="110">
          <template #default="{ row }">
            <el-tag :type="planStatusMap[row.planStatus]?.type || 'info'">
              {{ planStatusMap[row.planStatus]?.label || row.planStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="船舶名称" min-width="160">
          <template #default="{ row }">
            <div>{{ safeText(row.vesselNameZh || row.vesselNameZn) }}</div>
            <div v-if="row.vesselNameEn" class="sub-text">{{ row.vesselNameEn }}</div>
          </template>
        </el-table-column>
        <el-table-column label="计划时间" min-width="180">
          <template #default="{ row }">
            <div>起：{{ formatDateTime(row.estimatedStartTime || (row.resourceDetail?.estimatedStartTime ?? '')) }}</div>
            <div>止：{{ formatDateTime(row.estimatedEndTime || (row.resourceDetail?.estimatedEndTime ?? '')) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="任务资源" min-width="140">
          <template #default="{ row }">
            <div>{{ safeText(row.resourceTypeName) }}：{{ safeText(row.resourceName) }}</div>
            <div style="margin-top:4px;">
              <el-tag size="small" :type="row.assignmentStatusName === '已确认' ? 'success' : 'info'">
                分配状态：{{ safeText(row.assignmentStatusName) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="resourceTypeName" label="任务类型" width="100" />
        <el-table-column prop="confirmationStatusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.confirmationStatus)">
              {{ row.confirmationStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              :icon="View" 
              @click="viewDetail(row)"
            >
              详情
            </el-button>
            <el-button 
              v-if="row.confirmationStatus === 'PENDING'"
              type="success" 
              link 
              :icon="Check" 
              :disabled="!canConfirm(row)"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="tableState.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="任务详情"
      width="600px"
      append-to-body
      class="glass-dialog"
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="计划编号">{{ currentTask.planCode }}</el-descriptions-item>
          <el-descriptions-item label="计划状态">
            <el-tag :type="planStatusMap[currentTask.planStatus]?.type || 'info'">
              {{ planStatusMap[currentTask.planStatus]?.label || currentTask.planStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(currentTask.confirmationStatus)">
              {{ currentTask.confirmationStatusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="船舶名称">
            {{ safeText(currentTask.vesselNameZh || currentTask.vesselNameZn) }}<span v-if="currentTask.vesselNameEn"> / {{ currentTask.vesselNameEn }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="预计开始时间">{{ formatDateTime((currentTask.estimatedStartTime as any) || (currentTask.resourceDetail?.estimatedStartTime ?? '')) }}</el-descriptions-item>
          <el-descriptions-item label="预计结束时间">{{ formatDateTime((currentTask.estimatedEndTime as any) || (currentTask.resourceDetail?.estimatedEndTime ?? '')) }}</el-descriptions-item>
          <el-descriptions-item label="分配资源">
            {{ currentTask.resourceTypeName }} - {{ currentTask.resourceName }}
          </el-descriptions-item>
          <el-descriptions-item label="分配状态">{{ currentTask.assignmentStatusName }}</el-descriptions-item>
          
          <template v-if="currentTask.confirmationStatus === 'CONFIRMED'">
            <el-descriptions-item label="确认时间">{{ formatDateTime(currentTask.confirmedAt || '') }}</el-descriptions-item>
            <el-descriptions-item label="确认人">{{ currentTask.confirmedByName }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ currentTask.notes }}</el-descriptions-item>
          </template>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button 
            v-if="currentTask?.confirmationStatus === 'PENDING'"
            type="primary" 
            :loading="confirming"
            :disabled="!canConfirm(currentTask as any)"
            @click="handleConfirm(currentTask as any)"
          >
            确认接收
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.my-tasks {
  padding: 20px;
}

.table-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    color: #c8e6ff;
  }
}

.sub-text {
  font-size: 12px;
  /* 提高对比度：原色值在浅色背景上过淡 */
  color: var(--el-text-color-secondary);
}

.pagination-wrapper {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
