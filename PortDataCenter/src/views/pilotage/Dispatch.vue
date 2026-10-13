<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, VideoPlay, VideoPause, Check, Close } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { DispatchPlan, DispatchQueryParams, DispatchTimeUpdateParams } from '@/types/dispatch'
import { fetchPilotList } from '@/api/bases/pilots'
import type { Pilots } from '@/types/bases/pilots'
import {
  fetchDispatchList,
  startDispatch,
  completeDispatch,
  cancelDispatch,
  updateDispatchTime,
  exportDispatchData
} from '@/api/dispatch'
import { fetchCertificateByPlanId, createCertificateAuto } from '@/api/certificate'
import { useDataCenterStore } from '@/stores/dataCenter'

const router = useRouter()
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<DispatchPlan>('pilotage.dispatch', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  pilot: '',
  dateRange: [] as string[],
  shipName: ''
})

const pilotOptions = ref<Pilots[]>([])

const pagination = reactive({
  page: tableState.page,
  size: tableState.pageSize
})

const timeDialogVisible = ref(false)
const timeDialogTitle = ref('')
const currentPlanId = ref<number | string>('')
const currentAction = ref<'start' | 'end'>('start')
const timeDialogMode = ref<'single' | 'batch'>('single')
const batchPlanIds = ref<Array<number | string>>([])
const timeFormData = reactive<DispatchTimeUpdateParams>({
  actualStartTime: '',
  actualEndTime: ''
})

const selectedRows = ref<DispatchPlan[]>([])

const fetchData = async () => {
  tableState.loading = true

  try {
    const params: DispatchQueryParams = {
      page: pagination.page,
      size: pagination.size,
      vesselName: searchForm.shipName,
      pilotName: searchForm.pilot
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const response = await fetchDispatchList(params)

    if (response.code === 200) {
      tableState.items = response.data.list.map((item: DispatchPlan) => ({
        ...item,
        shipName: item.vesselNameZh,
        englishName: item.vesselNameEn,
        dynamic: item.voyageType === 'IMPORT' ? '进港' : '出港',
        time: item.planStartTime,
        grossTonnage: item.grossTonnage,
        netTonnage: item.netTonnage,
        deadWeight: item.deadWeight,
        length: item.vesselLength,
        width: item.vesselWidth,
        draft: '6.50', // 暂时使用默认值，实际应从后端获取
        berth: item.startLocationName || '待分配',
        type: '引航',
        status: getStatusText(item.planStatus)
      }))

      tableState.total = response.data.total
      ElMessage.success('数据加载成功')
    } else {
      ElMessage.error(response.message || '数据加载失败')
    }
  } catch (error) {
    console.error('获取调度列表失败:', error)
    ElMessage.error('数据加载失败，请检查网络连接')
  } finally {
    tableState.loading = false
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  tableState.page = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  tableState.pageSize = size
  tableState.page = 1
  fetchData()
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PLANNING': '计划中',
    'PLANNED': '已计划',
    'PUBLISHED': '已发布',
    'CONFIRMED': '已确认',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CERTIFICATED': '已签证',
    'BILLED': '已计费',
    'PAID': '已付款',
    'CANCELLED': '已取消',
    'REPLANNED': '重计划'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    'PLANNING': 'info',
    'PLANNED': 'primary',
    'PUBLISHED': 'success',
    'CONFIRMED': 'success',
    'ONGOING': 'warning',
    'COMPLETED': 'primary',
    'CERTIFICATED': 'success',
    'BILLED': 'success',
    'PAID': 'success',
    'CANCELLED': 'danger',
    'REPLANNED': 'info'
  }
  return typeMap[status] || 'info'
}

const formatDateTime = (d: Date) => {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const isStartable = (row: DispatchPlan) => ['CONFIRMED', 'PUBLISHED'].includes(row.planStatus || '')
const isCompletable = (row: DispatchPlan) => (row.planStatus || '') === 'ONGOING'

const canBatchStart = computed(() => selectedRows.value.length > 0 && selectedRows.value.some(isStartable))
const canBatchComplete = computed(() => selectedRows.value.length > 0 && selectedRows.value.some(isCompletable))
const canBatchCancel = computed(() => selectedRows.value.length > 0)

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const tryFetchCertificateAfterComplete = async (planId: number | string) => {
  const pid = Number(planId)
  if (!pid) return null
  // AFTER_COMMIT 事件生成签证单可能有短暂延迟，这里做几次轻量重试
  for (let i = 0; i < 3; i++) {
    try {
      const resp: any = await fetchCertificateByPlanId(pid)
      if (resp?.code === 200 && resp.data) return resp.data
    } catch (_) {
      // ignore
    }
    await sleep(700 * (i + 1))
  }
  return null
}

const handleAfterComplete = async (planId: number | string) => {
  const cert: any = await tryFetchCertificateAfterComplete(planId)
  if (cert) {
    const code = String(cert.routeCode || '').trim()
    const name = String(cert.routeName || '').trim()
    const isAuto = !code || !name || code.toUpperCase() === 'AUTO'
    if (isAuto) {
      ElMessage.warning('签证单已生成，但航线区间信息未补齐（可能为 AUTO）。请到“签证单”中先补录航线区间，再提交/确认/计费。')
    }
    await ElMessageBox.confirm(
      '调度结束成功，签证单已生成。是否前往“签证单”页面继续处理？',
      '提示',
      { type: 'info', confirmButtonText: '前往签证单', cancelButtonText: '留在本页' }
    ).then(() => router.push('/pilotage/visa')).catch(() => {})
    return
  }

  const confirm = await ElMessageBox.confirm(
    '调度结束成功，但暂未查询到签证单（可能仍在生成中）。是否立即触发“补生成签证单”？',
    '提示',
    { type: 'warning', confirmButtonText: '立即补生成', cancelButtonText: '稍后再说' }
  ).then(() => true).catch(() => false)

  if (confirm) {
    try {
      const resp: any = await createCertificateAuto(Number(planId))
      if (resp?.code === 200) {
        ElMessage.success('已触发补生成签证单，请稍后在“签证单”页面查看')
      } else {
        ElMessage.error(resp?.message || '补生成失败')
      }
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || e?.message || '补生成失败')
    }
  }
}

const openTimeDialog = (type: 'start' | 'end', mode: 'single' | 'batch', row?: DispatchPlan) => {
  timeDialogMode.value = mode
  currentAction.value = type

  if (mode === 'single' && row) {
    currentPlanId.value = row.planId
    batchPlanIds.value = []
    timeDialogTitle.value = type === 'start'
      ? `设定"${(row as any).shipName || row.vesselNameZh || ''}"轮的开始时间`
      : `设定"${(row as any).shipName || row.vesselNameZh || ''}"轮的结束时间`
  } else {
    currentPlanId.value = ''
    batchPlanIds.value = selectedRows.value.map((r) => r.planId)
    timeDialogTitle.value = type === 'start' ? `批量设定开始时间（${batchPlanIds.value.length}条）` : `批量设定结束时间（${batchPlanIds.value.length}条）`
  }

  const now = new Date()
  const timeStr = formatDateTime(now)
  if (type === 'start') {
    timeFormData.actualStartTime = timeStr
    timeFormData.actualEndTime = ''
  } else {
    timeFormData.actualEndTime = timeStr
    timeFormData.actualStartTime = ''
  }

  timeDialogVisible.value = true
}

const handleTimeAction = async (row: DispatchPlan, type: 'start' | 'end') => {
  openTimeDialog(type, 'single', row)
}

const handleBatchTimeDialog = (type: 'start' | 'end') => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的调度计划')
    return
  }
  openTimeDialog(type, 'batch')

  currentPlanId.value = row.planId
  currentAction.value = type
  timeDialogTitle.value = type === 'start'
      ? `设定"${row.shipName}"轮的开始时间`
      : `设定"${row.shipName}"轮的结束时间`

  // 设置默认时间为当前时间（本地时间，带秒，避免后端 Date 反序列化失败/UTC 偏移）
  const now = new Date()
  const timeStr = formatDateTime(now)

  if (type === 'start') {
    timeFormData.actualStartTime = timeStr
    timeFormData.actualEndTime = ''
  } else {
    timeFormData.actualEndTime = timeStr
    timeFormData.actualStartTime = row.actualStartTime || ''
  }

  timeDialogVisible.value = true;
}

const saveTime = async () => {
  try {
    const payload: any = {}
    if (timeFormData.actualStartTime) payload.actualStartTime = timeFormData.actualStartTime
    if (timeFormData.actualEndTime) payload.actualEndTime = timeFormData.actualEndTime

    if (timeDialogMode.value === 'batch') {
      let successCount = 0
      const errorMessages: string[] = []
      for (const row of selectedRows.value) {
        try {
          if (currentAction.value === 'start' && !isStartable(row)) {
            errorMessages.push(`${row.planCode}: 状态不允许开始`)
            continue
          }
          if (currentAction.value === 'end' && !isCompletable(row)) {
            errorMessages.push(`${row.planCode}: 状态不允许结束`)
            continue
          }

          const resp = currentAction.value === 'start'
            ? await startDispatch(row.planId, payload)
            : await completeDispatch(row.planId, payload)

          if ((resp as any).code === 200) successCount++
          else errorMessages.push(`${row.planCode}: ${(resp as any).message || '操作失败'}`)
        } catch (e: any) {
          errorMessages.push(`${row.planCode}: ${e?.message || '操作失败'}`)
        }
      }

      if (successCount > 0) ElMessage.success(`成功操作 ${successCount} 个调度计划`)
      if (errorMessages.length > 0) ElMessage.error(`以下计划操作失败: ${errorMessages.join('; ')}`)

      timeDialogVisible.value = false
      await fetchData()
      selectedRows.value = []
      return
    }

    const response = currentAction.value === 'start'
      ? await startDispatch(currentPlanId.value, payload)
      : await completeDispatch(currentPlanId.value, payload)

    if ((response as any).code === 200) {
      ElMessage.success(currentAction.value === 'start' ? '调度开始成功' : '调度结束成功')
      timeDialogVisible.value = false
      await fetchData()
      if (currentAction.value === 'end') {
        await handleAfterComplete(currentPlanId.value)
      }
    } else {
      ElMessage.error((response as any).message || '操作失败')
    }
  } catch (error) {
    console.error('保存时间设置失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const handleBatchAction = async (action: 'start' | 'complete' | 'cancel') => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的调度计划')
    return
  }

  try {
    if (action === 'start') {
      handleBatchTimeDialog('start')
      return
    }
    if (action === 'complete') {
      handleBatchTimeDialog('end')
      return
    }

    let successCount = 0
    let errorMessages: string[] = []

    for (const row of selectedRows.value) {
      try {
        let response
        switch (action) {
          case 'start':
            if (!isStartable(row)) {
              errorMessages.push(`${row.planCode}: 状态不允许开始（需已确认/已发布）`)
              continue
            }
            const startTime = formatDateTime(new Date())
            response = await startDispatch(row.planId, { actualStartTime: startTime, actualEndTime: '' })
            break
          case 'complete':
            if (!isCompletable(row)) {
              errorMessages.push(`${row.planCode}: 状态不允许结束（需进行中）`)
              continue
            }
            const endTime = formatDateTime(new Date())
            response = await completeDispatch(row.planId, { actualStartTime: row.actualStartTime || '', actualEndTime: endTime })
            break
          case 'cancel':
            const reason = await ElMessageBox.prompt('请输入取消原因', '取消调度', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              inputPattern: /\S+/,
              inputErrorMessage: '取消原因不能为空'
            })
            response = await cancelDispatch(row.planId, reason.value)
            break
        }

        if (response?.code === 200) {
          successCount++
        } else {
          errorMessages.push(`${row.planCode}: ${response?.message || '操作失败'}`)
        }
      } catch (error: any) {
        errorMessages.push(`${row.planCode}: ${error?.message || '操作失败'}`)
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功操作 ${successCount} 个调度计划`)
      await fetchData() // 刷新数据
    }

    if (errorMessages.length > 0) {
      ElMessage.error(`以下计划操作失败: ${errorMessages.join('; ')}`)
    }

    selectedRows.value = []
  } catch (error) {
    console.error('批量操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const handleExport = async () => {
  try {
    const params: DispatchQueryParams = {
      page: 1,
      size: 10000,
      vesselName: searchForm.shipName,
      pilotName: searchForm.pilot
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const resp: any = await exportDispatchData(params)

    const url = window.URL.createObjectURL(resp.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `调度数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

const handleSelectionChange = (rows: DispatchPlan[]) => {
  selectedRows.value = rows
}

onMounted(() => {
  fetchPilotList()
    .then((res: any) => {
      if (res?.code === 200) {
        pilotOptions.value = res.data || []
      }
    })
    .catch(() => {
      pilotOptions.value = []
    })
  fetchData()
})
</script>

<template>
  <div class="dispatch-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">引航调度</h2>
        <p class="page-desc">引航作业调度执行与状态管理</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Search" @click="fetchData">查询</el-button>
        <el-button
            type="success"
            :icon="VideoPlay"
            @click="handleBatchAction('start')"
            :disabled="!canBatchStart"
        >
          引航开始
        </el-button>
        <el-button
            type="warning"
            :icon="VideoPause"
            @click="handleBatchAction('complete')"
            :disabled="!canBatchComplete"
        >
          引航结束
        </el-button>
        <el-button
            type="danger"
            :icon="Close"
            @click="handleBatchAction('cancel')"
            :disabled="!canBatchCancel"
        >
          计划取消
        </el-button>
        <el-button type="primary" :icon="Check" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <div class="search-bar glass-card">
      <el-form inline size="small">
        <el-form-item label="引航员">
          <el-select
              v-model="searchForm.pilot"
              placeholder="全部"
              style="width: 120px"
              clearable
          >
            <el-option label="全部" value="" />
            <el-option
                v-for="pilot in pilotOptions"
                :key="pilot.id"
                :label="pilot.pilotName"
                :value="pilot.pilotName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作业时间">
          <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="中文船名">
          <el-input
              v-model="searchForm.shipName"
              placeholder="船名"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container glass-card">
      <el-table
          :data="tableState.items"
          v-loading="tableState.loading"
          stripe
          height="100%"
          size="small"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="#" width="40" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag
                size="small"
                :type="getStatusType(row.planStatus)"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 计划编号：用于快速定位是哪一个调度计划 -->
        <el-table-column prop="planCode" label="计划编号" width="160" show-overflow-tooltip />
        <el-table-column prop="shipName" label="中文船名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="englishName" label="英文船名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nationality" label="国籍" width="80" />
        <el-table-column prop="dynamic" label="动态" width="60">
          <template #default="{ row }">
            <span :style="{ color: row.dynamic === '进港' ? '#52c41a' : '#1890ff' }">
              {{ row.dynamic }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间/日期" width="130" />
        <el-table-column prop="grossTonnage" label="总吨" width="80" />
        <el-table-column prop="netTonnage" label="净吨" width="80" />
        <el-table-column prop="deadWeight" label="载重吨" width="80" />
        <el-table-column prop="length" label="船长" width="60" />
        <el-table-column prop="width" label="船宽" width="60" />
        <el-table-column prop="draft" label="吃水" width="60" />
        <el-table-column prop="berth" label="泊位" min-width="100" show-overflow-tooltip />

        <!-- 确认进度列 -->
        <el-table-column label="确认进度" width="100">
          <template #default="{ row }">
            <el-progress
                :percentage="row.confirmationProgress || 0"
                :show-text="false"
                size="small"
                :status="row.confirmationProgress >= 100 ? 'success' : 'warning'"
            />
            <span style="font-size: 12px; color: #666;">
              {{ row.confirmedCount }}/{{ row.totalResourcesCount }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                size="small"
                @click="handleTimeAction(row, 'start')"
                :disabled="!isStartable(row)"
            >
              开始
            </el-button>
            <el-button
                link
                type="primary"
                size="small"
                @click="handleTimeAction(row, 'end')"
                :disabled="!isCompletable(row)"
            >
              结束
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="tableState.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          class="pagination"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <el-dialog
        v-model="timeDialogVisible"
        :title="timeDialogTitle"
        width="400px"
        destroy-on-close
        class="pilotage-dialog"
    >
      <div style="padding: 20px;">
        <el-form label-width="80px">
          <el-form-item v-if="currentAction === 'start'" label="开始时间:">
            <el-date-picker
                v-model="timeFormData.actualStartTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
            />
          </el-form-item>
          <el-form-item v-if="currentAction === 'end'" label="结束时间:">
            <el-date-picker
                v-model="timeFormData.actualEndTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="timeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTime">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.dispatch-view {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-bar {
  padding: 12px 16px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.table-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .pagination {
    margin-top: 12px;
    justify-content: flex-end;
  }
}

:deep(.pilotage-dialog) {
  .el-dialog__body {
    padding: 0;
  }

  .el-dialog__header {
    border-bottom: 1px solid var(--tidal-border);
    margin-right: 0;
  }
}

:deep(.el-table) {
  .el-table__cell {
    padding: 4px 0;
  }

  .cell {
    line-height: 1.2;
  }
}

@media (max-width: 1200px) {
  .search-bar {
    :deep(.el-form-item) {
      margin-bottom: 8px;
    }

    :deep(.el-form--inline) {
      display: flex;
      flex-wrap: wrap;
    }
  }
}
</style>