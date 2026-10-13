<!-- src/views/confirmation/PlanConfirmation.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Check, Refresh, CircleCheck, CircleClose, Bell } from '@element-plus/icons-vue'
import {
  getConfirmationList,
  batchConfirm,
  getPlanFullConfirmationStatus,
  cancelConfirmation,
  sendConfirmationReminder, getAssignmentsResourceList, batchCancelConfirmations
} from '@/api/planConfirmations'
import type {
  PlanConfirmationQueryParams,
  PlanConfirmationDTO,
  PlanConfirmationResult,
  ResourceType,
  ConfirmMethod,
  ConfirmationStatus
} from '@/types/planConfirmations'
import { useDataCenterStore } from '@/stores/dataCenter'

const resourceTypeMap: Record<string, string> = {
  PILOT: '引航员',
  DRIVER: '驾驶员',
  PILOT_BOAT: '引航艇',
  TUGBOAT: '拖轮',
  VEHICLE: '车辆'
}

const confirmMethodMap: Record<string, string> = {
  WEB: '网站确认',
  SMS: '短信确认',
  PHONE: '电话确认',
  MANUAL: '人工确认'
}

const statusMap: Record<string, { label: string, type: string }> = {
  PENDING: { label: '待确认', type: 'warning' },
  CONFIRMED: { label: '已确认', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' },
  SYSTEM_CANCELLATION: { label: '系统取消', type: 'info' }
}

const isPlanLocked = (planStatus?: string) => {
  return ['ONGOING', 'COMPLETED', 'CERTIFICATED', 'BILLED', 'PAID', 'CANCELLED'].includes(planStatus || '')
}

const isConfirmPhase = (planStatus?: string) => {
  return ['PUBLISHED', 'CONFIRMED'].includes(planStatus || '')
}
const activeTab = ref('PILOT')
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PlanConfirmationResult>('pilotage.confirmation', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const selectedRows = ref<PlanConfirmationResult[]>([])

const searchForm = reactive<PlanConfirmationQueryParams>({
  planCode: '',
  resourceType: 'PILOT',
  confirmMethod: '',
  confirmationStatus: '',
  confirmedBy: undefined,
  startDate: '',
  endDate: '',
  page: 1,
  pageSize: 20
})

const pagination = reactive({
  page: tableState.page,
  size: tableState.pageSize
})

// 当前计划状态
const currentPlanStatus = ref<any>(null)

const getPlanStat = (key: string) => {
  const data = currentPlanStatus.value || {}
  return Number(data[key] ?? data[key.toLowerCase()] ?? data[key.toUpperCase()] ?? 0)
}

const getResourceStat = (resourceType: string, kind: 'confirmed' | 'total') => {
  const data = currentPlanStatus.value || {}
  const lower = resourceType.toLowerCase()
  const candidates = kind === 'confirmed'
    ? [`${lower}_confirmed_count`, `${lower}_confirmed`, `${lower}ConfirmedCount`]
    : [`${lower}_total_count`, `${lower}_count`, `${lower}TotalCount`]
  for (const key of candidates) {
    if (data[key] !== undefined && data[key] !== null) return Number(data[key])
  }
  return 0
}

const resourceTypes = computed(() => [
  { label: '引航员', value: 'PILOT' },
  { label: '驾驶员', value: 'DRIVER' },
  { label: '引航艇', value: 'PILOT_BOAT' },
  { label: '拖轮', value: 'TUGBOAT' },
  { label: '车辆', value: 'VEHICLE' }
])

const confirmMethods = computed(() => [
  { label: '网站确认', value: 'WEB' },
  { label: '短信确认', value: 'SMS' },
  { label: '电话确认', value: 'PHONE' },
  { label: '人工确认', value: 'MANUAL' }
])

const confirmationStatuses = computed(() => [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '系统取消', value: 'SYSTEM_CANCELLATION' }
])

const fetchData = async () => {
  tableState.loading = true
  try {
    searchForm.resourceType = activeTab.value
    searchForm.page = pagination.page
    searchForm.pageSize = pagination.size

    const params = { ...searchForm }
    if (!params.startDate) {
      delete params.startDate
    } else {
      params.startDate = `${params.startDate} 00:00:00`
    }

    if (!params.endDate) {
      delete params.endDate
    } else {
      params.endDate = `${params.endDate} 23:59:59`
    }

    const response = await getAssignmentsResourceList(params)
    if (response.code === 200) {
      const payload: any = response.data || {}
      tableState.items = payload.list || payload.records || payload.items || []
      tableState.total = payload.total || payload.count || tableState.items.length
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取确认记录失败:', error)
    ElMessage.error('获取数据失败，请重试')
  } finally {
    tableState.loading = false
  }
}

const fetchPlanStatus = async (planId: number) => {
  if (!planId) return

  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载计划状态中...'
  })

  try {
    const response = await getPlanFullConfirmationStatus(planId)
    if (response.code === 200) {
      currentPlanStatus.value = response.data
    }
  } catch (error) {
    console.error('获取计划状态失败:', error)
  } finally {
    loadingInstance.close()
  }
}

const handleConfirm = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要确认的记录')
    return
  }

  try {
    const blocked = selectedRows.value.filter(r => !isConfirmPhase((r as any).planStatus))
    if (blocked.length > 0) {
      ElMessage.warning('所选记录中包含未发布/未确认阶段的计划，不能进行资源确认')
      return
    }
    const confirmations: PlanConfirmationDTO[] = selectedRows.value
        .filter(row => ['PENDING', 'REJECTED'].includes(row.confirmationStatus))
        .map(row => ({
          planId: row.planId,
          resourceType: row.resourceType,
          resourceId: row.resourceId,
          confirmMethod: 'WEB', // 默认为网站确认
          confirmedAt: new Date().toISOString()
        }))

    if (confirmations.length === 0) {
      ElMessage.warning('所选记录已全部确认')
      return
    }

    ElMessageBox.confirm(
        `确认所选 ${confirmations.length} 条记录?`,
        '提示',
        {
          type: 'warning',
          confirmButtonText: '确认',
          cancelButtonText: '取消'
        }
    ).then(async () => {
      const response = await batchConfirm(confirmations)
      if (response.code === 200) {
        ElMessage.success('批量确认成功')
        await fetchData()
        selectedRows.value = []
      } else {
        ElMessage.error(response.message || '确认失败')
      }
    })
  } catch (error) {
    console.error('确认操作失败:', error)
    ElMessage.error('确认操作失败，请重试')
  }
}

const handleBatchCancel = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要取消的记录')
    return
  }
  const cancellable = selectedRows.value.filter(r => !['SYSTEM_CANCELLATION', 'CANCELLED'].includes(r.confirmationStatus))
  if (cancellable.length === 0) {
    ElMessage.warning('所选记录均不可取消')
    return
  }

  try {
    const blocked = cancellable.filter(r => isPlanLocked((r as any).planStatus))
    if (blocked.length > 0) {
      ElMessage.warning('所选记录中包含已开始/已结束计划，不能取消资源确认')
      return
    }

    ElMessageBox.prompt('请输入批量取消原因', '批量取消确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因...',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) return '取消原因不能为空'
        return true
      }
    }).then(async ({ value }) => {
      const cancelData: PlanConfirmationDTO[] = cancellable.map(row => ({
        planId: row.planId,
        resourceType: row.resourceType,
        resourceId: row.resourceId,
        confirmMethod: 'WEB',
        notes: row.notes || '批量取消确认'
      }))

      const response = await batchCancelConfirmations(cancelData, value.trim())
      if (response.code === 200) {
        ElMessage.success(`批量取消成功（${cancelData.length}条）`)
        await fetchData()
        selectedRows.value = []
      } else {
        ElMessage.error(response.message || '批量取消失败')
      }
    })
  } catch (error) {
    console.error('批量取消失败:', error)
    ElMessage.error('批量取消失败，请重试')
  }
}

const handleSingleConfirm = async (row: PlanConfirmationResult) => {
  if (isPlanLocked(row.planStatus)) {
    ElMessage.warning('计划已开始或已结束，不能再确认/取消资源')
    return
  }
  if (!isConfirmPhase(row.planStatus)) {
    ElMessage.warning('计划未发布，不能确认资源')
    return
  }
  if (!['PENDING', 'REJECTED'].includes(row.confirmationStatus)) {
    ElMessage.warning('当前状态不可确认')
    return
  }
  try {
    ElMessageBox.confirm(
        `确认 ${row.resourceName} 的确认记录?`,
        '提示',
        {
          type: 'warning',
          confirmButtonText: '确认',
          cancelButtonText: '取消'
        }
    ).then(async () => {
      const confirmation: PlanConfirmationDTO = {
        planId: row.planId,
        resourceType: row.resourceType,
        resourceId: row.resourceId,
        confirmMethod: 'WEB',
        notes: '手动确认'
      }

      const response = await batchConfirm([confirmation])
      if (response.code === 200) {
        ElMessage.success('确认成功')
        await fetchData()
      } else {
        ElMessage.error(response.message || '确认失败')
      }
    })
  } catch (error) {
    console.error('确认操作失败:', error)
  }
}

const handleCancel = async (row: PlanConfirmationResult) => {
  if (isPlanLocked(row.planStatus)) {
    ElMessage.warning('计划已开始或已结束，不能再确认/取消资源')
    return
  }
  if (['SYSTEM_CANCELLATION', 'CANCELLED'].includes(row.confirmationStatus)) {
    ElMessage.warning('当前状态不可取消')
    return
  }
  try {
    ElMessageBox.prompt('请输入取消原因', '取消确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因...',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '取消原因不能为空'
        }
        return true
      }
    }).then(async ({ value }) => {
      const cancelData: PlanConfirmationDTO[] = [{
        planId: row.planId,
        resourceType: row.resourceType,
        resourceId: row.resourceId,
        confirmMethod: 'WEB', // 使用默认值
        notes: row.notes || '单个取消确认'
      }]
      
      const response = await batchCancelConfirmations(cancelData, value.trim())
      if (response.code === 200) {
        ElMessage.success('取消确认成功')
        await fetchData()
      } else {
        ElMessage.error(response.message || '取消确认失败')
      }
    })
  } catch (error) {
    console.error('取消确认失败:', error)
  }
}

const handleSendReminder = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要发送提醒的记录')
    return
  }

  try {
    const planIds = [...new Set(selectedRows.value.map(row => row.planId))]

    if (planIds.length > 1) {
      ElMessage.warning('请选择同一个计划的记录进行提醒')
      return
    }

    const firstRow = selectedRows.value[0]
    const planId = planIds[0]

    ElMessageBox.confirm(
        `向 ${firstRow.resourceName} 发送确认提醒?`,
        '发送提醒',
        {
          type: 'warning',
          confirmButtonText: '发送',
          cancelButtonText: '取消'
        }
    ).then(async () => {
      const requestParams = {
        planId: planId,
        resourceId: firstRow.resourceId,
        resourceType: firstRow.resourceType
      }
      
      const response = await sendConfirmationReminder(requestParams)
      if (response.code === 200) {
        ElMessage.success('提醒发送成功')
      } else {
        ElMessage.error(response.message || '提醒发送失败')
      }
    })
  } catch (error) {
    console.error('发送提醒失败:', error)
    ElMessage.error('发送提醒失败，请重试')
  }
}

const handleSelectionChange = (val: PlanConfirmationResult[]) => {
  selectedRows.value = val
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  searchForm.resourceType = tab
  searchForm.page = 1
  fetchData()
}

const handleSearch = () => {
  searchForm.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.planCode = ''
  searchForm.confirmMethod = ''
  searchForm.confirmationStatus = ''
  searchForm.confirmedBy = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  searchForm.page = 1
  tableState.page = 1
  fetchData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  tableState.pageSize = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  tableState.page = page
  fetchData()
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handlePlanCodeClick = (planCode: string, planId: number) => {
  fetchPlanStatus(planId)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="confirmation-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">计划确认管理</h2>
        <p class="page-desc">引航计划资源确认管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Bell" @click="handleSendReminder" :disabled="selectedRows.length === 0">
          发送提醒
        </el-button>
        <el-button type="success" :icon="Check" @click="handleConfirm" :disabled="selectedRows.length === 0">
          批量确认({{ selectedRows.length }})
        </el-button>
        <el-button type="danger" @click="handleBatchCancel" :disabled="selectedRows.length === 0">
          批量取消({{ selectedRows.length }})
        </el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <div class="main-content glass-card">
      <el-tabs v-model="activeTab" class="confirmation-tabs" @tab-change="handleTabChange">
        <el-tab-pane
            v-for="type in resourceTypes"
            :key="type.value"
            :label="type.label"
            :name="type.value"
        >
          <div class="tab-toolbar">
            <el-form inline size="small" :model="searchForm" class="search-form">
              <el-form-item label="计划编号">
                <el-input
                    v-model="searchForm.planCode"
                    placeholder="请输入计划编号"
                    style="width: auto"
                />
              </el-form-item>

              <el-form-item label="确认方式">
                <el-select
                    v-model="searchForm.confirmMethod"
                    placeholder="请选择确认方式"
                    clearable
                    style="width: 120px"
                >
                  <el-option
                      v-for="method in confirmMethods"
                      :key="method.value"
                      :label="method.label"
                      :value="method.value"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="确认状态">
                <el-select
                    v-model="searchForm.confirmationStatus"
                    placeholder="请选择确认状态"
                    clearable
                    style="width: 120px"
                >
                  <el-option
                      v-for="status in confirmationStatuses"
                      :key="status.value"
                      :label="status.label"
                      :value="status.value"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="日期范围">
                <el-date-picker
                    v-model="searchForm.startDate"
                    type="date"
                    placeholder="开始日期"
                    value-format="YYYY-MM-DD"
                    style="width: 140px"
                />
                <span style="margin: 0 8px">至</span>
                <el-date-picker
                    v-model="searchForm.endDate"
                    type="date"
                    placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                    style="width: 140px"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div v-if="currentPlanStatus" class="plan-status-card">
            <el-card shadow="never" class="status-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">计划状态: {{ currentPlanStatus.planCode }}</span>
                  <el-tag :type="currentPlanStatus.planStatus === 'CONFIRMED' ? 'success' : 'warning'">
                    {{ currentPlanStatus.planStatus }}
                  </el-tag>
                </div>
              </template>
              <div class="progress-section">
                <div class="progress-info">
                  <span>确认进度: {{ getPlanStat('confirmationProgress').toFixed(2) }}%</span>
                  <span class="progress-detail">
                    {{ getPlanStat('confirmed_count') }} / {{ getPlanStat('total_count') }}
                  </span>
                </div>
                <el-progress
                    :percentage="getPlanStat('confirmationProgress')"
                    :status="getPlanStat('confirmationProgress') === 100 ? 'success' : ''"
                />
              </div>
              <div class="resource-stats">
                <div class="stat-item" v-for="type in resourceTypes" :key="type.value">
                  <div class="stat-label">{{ type.label }}</div>
                  <div class="stat-value">
                    {{ getResourceStat(type.value, 'confirmed') }} / {{ getResourceStat(type.value, 'total') }}
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <el-table
              :data="tableState.items"
              v-loading="tableState.loading"
              stripe
              height="calc(100vh - 400px)"
              size="small"
              @selection-change="handleSelectionChange"
              class="confirmation-table"
          >
            <el-table-column type="selection" width="40" align="center" />

            <el-table-column prop="planCode" label="计划编号" width="160" fixed>
              <template #default="{ row }">
                <el-link type="primary" @click="handlePlanCodeClick(row.planCode, row.planId)">
                  {{ row.planCode }}
                </el-link>
              </template>
            </el-table-column>

            <el-table-column prop="resourceName" label="资源名称" width="120" />

            <el-table-column prop="resourceTypeName" label="资源类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.resourceTypeName }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="confirmMethodName" label="确认方式" width="100" />

            <el-table-column prop="confirmationStatusName" label="确认状态" width="100">
              <template #default="{ row }">
                <el-tag
                    :type="statusMap[row.confirmationStatus]?.type || 'info'"
                    size="small"
                >
                  {{ row.confirmationStatusName }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="confirmedByName" label="确认人" width="100" />

            <el-table-column prop="confirmedAt" label="确认时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.confirmedAt) }}
              </template>
            </el-table-column>

            <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip />

            <el-table-column prop="createdAt" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button
                      v-if="!isPlanLocked(row.planStatus) && isConfirmPhase(row.planStatus) && ['PENDING','REJECTED'].includes(row.confirmationStatus)"
                      type="primary"
                      size="small"
                      :icon="CircleCheck"
                      @click="handleSingleConfirm(row)"
                  >
                    确认
                  </el-button>

                  <el-button
                      v-if="!isPlanLocked(row.planStatus) && isConfirmPhase(row.planStatus) && row.confirmationStatus === 'CONFIRMED'"
                      type="danger"
                      size="small"
                      :icon="CircleClose"
                      @click="handleCancel(row)"
                  >
                    取消
                  </el-button>

                  <span v-if="!isPlanLocked(row.planStatus) && !isConfirmPhase(row.planStatus)">未发布</span>
                  <span v-if="row.confirmationStatus === 'CANCELLED' || row.confirmationStatus === 'SYSTEM_CANCELLATION'">不可操作</span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="tableState.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              class="pagination"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.confirmation-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);

  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
    color: var(--el-text-color-primary);
  }

  .page-desc {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 4px 0 0;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.main-content {
  flex: 1;
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.confirmation-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__header) {
    margin-bottom: 20px;
    background: var(--el-bg-color);
    padding: 0 8px;
    border-radius: 4px;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-tab-pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.tab-toolbar {
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: var(--el-box-shadow-light);

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.plan-status-card {
  margin-bottom: 20px;

  .status-card {
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;

    :deep(.el-card__header) {
      padding: 12px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-bottom: none;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          font-size: 16px;
          font-weight: 600;
        }
      }
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }
}

.progress-section {
  margin-bottom: 20px;

  .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .progress-detail {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}

.resource-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;

  .stat-item {
    text-align: center;
    padding: 12px;
    background: var(--el-bg-color-page);
    border-radius: 6px;
    border: 1px solid var(--el-border-color-light);

    .stat-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-color-primary);
    }
  }
}

.confirmation-table {
  flex: 1;
  margin-bottom: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;

  :deep(.el-table__header-wrapper) {
    background: var(--el-bg-color-page);
  }

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  .action-buttons {
    display: flex;
    gap: 8px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  background: var(--el-bg-color);
  border-radius: 6px;
}
</style>