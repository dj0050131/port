<script setup lang="ts">
import {ref, reactive, onMounted, computed} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh, Download } from '@element-plus/icons-vue'
import type { PilotageApplication } from '@/types/pilotage'
import ApplicationForm from './components/ApplicationForm.vue'
import GeneratePlanDialog from './components/GeneratePlanDialog.vue'
import {
  batchReviewPilotageApplications,
  batchDeletePilotageApplications,
  updatePilotageRequestStatus,
  exportPilotageApplications
} from '@/api/pilotage'
import {usePilotageStore} from "@/stores/pilotage.ts";
import { useDataCenterStore } from '@/stores/dataCenter'

const pilotageStore = usePilotageStore()
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PilotageApplication>('pilotage.forecast', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const actionLoading = ref(false)
const multipleSelection = ref<PilotageApplication[]>([])

const handleSelectionChange = (selection: PilotageApplication[]) => {
  multipleSelection.value = selection
}

const handleReturn = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  try {
    await ElMessageBox.confirm(`确认退回选中的 ${multipleSelection.value.length} 条申请?`, '确认操作', {
      type: 'warning'
    })

    const ids = multipleSelection.value.map(item => item.id)

    await updatePilotageRequestStatus(ids, '已驳回')

    ElMessage.success('退回成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const generatePlanDialogVisible = ref(false)

const handleGeneratePlan = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  // - 必须已批准
  // - 若已存在计划（planId != null 且 planStatus 不是 CANCELLED），必须去“引航计划”做重排/修改，禁止重复生成
  const invalidStatus = multipleSelection.value.filter((item: any) => {
    if (item.requestStatus !== 'APPROVED') return true
    const hasActivePlan = item.planId && item.planStatus && item.planStatus !== 'CANCELLED'
    return !!hasActivePlan
  })

  if (invalidStatus.length > 0) {
    ElMessage.warning('选中的申请中存在不符合生成计划条件的记录（需为已批准且未生成计划；已生成计划请到【引航计划】中重排/修改），无法生成计划')
    return
  }

  generatePlanDialogVisible.value = true
}
const getPlanStatusName = (status: string) => {
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
    'REPLANNED': '重计划'
  }
  return statusMap[status] || '未计划'
}

const getPlanStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PLANNING': 'warning',
    'PLANNED': 'primary',
    'PUBLISHED': 'primary',
    'CONFIRMED': 'primary',
    'ONGOING': 'primary',
    'COMPLETED': 'success',
    'CERTIFICATED': 'success',
    'BILLED': 'success',
    'PAID': 'success',
    'REPLANNED': 'warning'
  }
  return typeMap[status] || 'info'
}
const handleExport = async () => {
  try {
    const ids = (multipleSelection.value.length > 0 ? multipleSelection.value : tableData.value)
      .map((item: any) => item.id)
      .filter((v: any) => v !== null && v !== undefined)

    if (!ids || ids.length === 0) {
      ElMessage.warning('当前没有可导出的数据')
      return
    }

    const resp: any = await exportPilotageApplications(ids)
    const blob = resp.data
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `引航申请_${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '')}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const tableStateLoading = computed(() => tableState.loading)
const searchForm = reactive({
  nameZh: '', // 中文船名
  nameEn: '', // 英文船名
  planStatus: '', // 计划状态
})

const planStatusOptions = [
  { value: '', label: '全部' },
  { value: 'UNPLANNED', label: '未计划' },
  { value: 'PLANNING', label: '计划中' },
  { value: 'PLANNED', label: '已计划' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'CONFIRMED', label: '已确认' },
  { value: 'ONGOING', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CERTIFICATED', label: '已签证' },
  { value: 'BILLED', label: '已计费' },
  { value: 'PAID', label: '已付款' },
  { value: 'REPLANNED', label: '重计划' },
  { value: 'CANCELLED', label: '已取消' },
]

const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentApplication = ref<Partial<PilotageApplication>>({})
const isEditMode = ref(false)

// 使用 store 中的数据
const tableData = computed(() => pilotageStore.applications)
const pagination = computed(() => pilotageStore.pagination)
const storeLoading = computed(() => pilotageStore.loading)

const fetchData = async (page: number = pagination.value.page) => {
  tableState.loading = true
  try {
    const filters: Record<string, any> = {}
    if (searchForm.nameZh?.trim()) filters.nameZh = searchForm.nameZh.trim()
    if (searchForm.nameEn?.trim()) filters.nameEn = searchForm.nameEn.trim()
    if (searchForm.planStatus?.trim()) filters.planStatus = searchForm.planStatus.trim()
    await pilotageStore.fetchApplications(page, filters)
    if (pilotageStore.pagination) {
      tableState.total = pilotageStore.pagination.total
      tableState.page = pilotageStore.pagination.page
      tableState.pageSize = pilotageStore.pagination.pageSize
    }
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    tableState.loading = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  tableState.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.nameZh = ''
  searchForm.nameEn = ''
  searchForm.planStatus = ''
  handleSearch()
}

const handleGeneratePlanSuccess = async () => {
  // 清空勾选，避免用户误以为还在当前批次
  multipleSelection.value = []
  await fetchData(1)
}

const handleCreate = () => {
  currentApplication.value = {}
  dialogTitle.value = '新增动态预报'
  isEditMode.value = true
  dialogVisible.value = true
}

const handleEdit = (row: PilotageApplication) => {
  currentApplication.value = { ...row }
  dialogTitle.value = '修改动态预报'
  isEditMode.value = true
  dialogVisible.value = true
}

const handleBatchEdit = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  if (multipleSelection.value.length > 1) {
    ElMessage.warning('请选择单条记录进行修改')
    return
  }

  handleEdit(multipleSelection.value[0])
}

const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  const notPending = multipleSelection.value.filter(i => i.requestStatus !== 'PENDING')
  if (notPending.length > 0) {
    const names = notPending.slice(0, 5).map(i => i.nameZh || i.requestCode || i.id).join('、')
    ElMessage.warning(`仅“待审核”申请可删除。当前有 ${notPending.length} 条不可删除：${names}${notPending.length > 5 ? '…' : ''}`)
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除选中的 ${multipleSelection.value.length} 条申请?`, '确认操作', {
      type: 'warning'
    })

    const ids = multipleSelection.value.map(item => Number(item.id))
    await batchDeletePilotageApplications(ids)

    ElMessage.success('批量删除成功')
    await fetchData()
  } catch (e) {
    // request.ts 已统一弹出后端错误信息，这里避免重复提示
  }
}

const handleFormSubmit = async () => {
  try {
    ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
  }
}

const handleDelete = async (row: PilotageApplication) => {
  if (row.requestStatus !== 'PENDING') {
    ElMessage.warning('仅“待审核”申请可删除；如需作废请先退回/撤销相关流程')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除申请【${row.nameZh || row.requestCode}】？删除后不可恢复。`, '删除确认', { type: 'warning' })
    // 使用批量删除API，传入单个ID的数组
    await batchDeletePilotageApplications([Number(row.id)])
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) {
    // request.ts 已统一弹出后端错误信息，这里避免重复提示
  }
}

onMounted(() => {
  fetchData()
})

const getStatusType = (status: any) => {
  const map: Record<string, string> = {
    PENDING: 'warning',      // 橙色 - 待处理
    APPROVED: 'success',     // 绿色 - 已批准
    REJECTED: 'danger',      // 红色 - 已拒绝
    PLANNED: 'primary',      // 蓝色 - 已计划
    IN_PROGRESS: 'primary',  // 蓝色 - 进行中
    COMPLETED: 'success',    // 绿色 - 已完成
    CANCELLED: 'info'        // 灰色 - 已取消
  }
  return map[status] || 'info'
}
</script>

<template>
  <div class="applications-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">引航申请</h2>
        <p class="page-desc">管理船舶进出港引航申请单据</p>
      </div>
    </div>

    <div class="search-bar glass-card">
      <el-form inline size="default">
        <el-form-item label="中文船名">
          <el-input v-model="searchForm.nameZh" placeholder="请输入中文船名" clearable />
        </el-form-item>
        <el-form-item label="英文船名">
          <el-input v-model="searchForm.nameEn" placeholder="请输入英文船名" clearable />
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select v-model="searchForm.planStatus" placeholder="全部" style="width: 160px" clearable>
            <el-option v-for="o in planStatusOptions" :key="o.value || 'ALL'" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Plus" @click="handleCreate" :disabled="tableStateLoading || actionLoading">新增</el-button>
          <el-button type="primary" :icon="Edit" @click="handleBatchEdit"
                     :disabled="multipleSelection.length !== 1">修改</el-button>
          <el-button type="warning" @click="handleReturn" :disabled="multipleSelection.length === 0">退回</el-button>
          <el-button type="danger" :icon="Delete" @click="handleBatchDelete"
                     :disabled="multipleSelection.length === 0">删除</el-button>
          <el-button type="success" @click="handleGeneratePlan" :disabled="multipleSelection.length === 0">生成计划</el-button>
          <el-button type="info" :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container glass-card">
      <el-table
          :data="tableData"
          v-loading="tableStateLoading"
          stripe
          height="100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="#" width="40px" />

        <el-table-column prop="createdAt" label="预报时间" width="100">
          <template #default="{ row }">
            {{ row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="agentName" label="代理" width="150" show-overflow-tooltip />
        <el-table-column prop="requestStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.requestStatus)" size="small">
              {{ row.requestStatus === 'PENDING' ? '待审核' :
                row.requestStatus === 'APPROVED' ? '已批准' :
                    row.requestStatus === 'REJECTED' ? '已驳回' :
                        row.requestStatus === 'PLANNED' ? '已计划' :
                            row.requestStatus === 'IN_PROGRESS' ? '进行中' :
                                row.requestStatus === 'COMPLETED' ? '已完成' :
                                    row.requestStatus === 'CANCELLED' ? '已取消' : row.requestStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewerUsername" label="审核人" width="auto">
          <template #default="{ row }">
            {{ row.reviewerUsername || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="计划状态" width="auto">
          <template #default="{ row }">
            <el-tag size="small" :type="getPlanStatusType(row.planStatus)">
              {{ getPlanStatusName(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="nameZh" label="中文船名" min-width="120" />
        <el-table-column prop="nameEn" label="英文船名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nationality" label="国籍" width="auto" />
        <el-table-column prop="voyageType" label="航次类型" width="auto">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ row.voyageType === 'IMPORT' ? '进港' :
                row.voyageType === 'EXPORT' ? '出港' :
                    row.voyageType === 'TRANSIT' ? '移泊' : row.voyageType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eta" label="ETA" width="140">
          <template #default="{ row }">
            {{ row.eta ? new Date(row.eta).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="etd" label="ETD" width="140">
          <template #default="{ row }">
            {{ row.etd ? new Date(row.etd).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="grossTonnage" label="总吨" width="auto">
          <template #default="{ row }">
            {{ row.grossTonnage ? Number(row.grossTonnage).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="netTonnage" label="净吨" width="auto">
          <template #default="{ row }">
            {{ row.netTonnage ? Number(row.netTonnage).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deadweight" label="载重吨" width="auto">
          <template #default="{ row }">
            {{ row.deadweight ? Number(row.deadweight).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="routeType" label="航线种类" width="auto">
          <template #default="{ row }">
            <el-tag
                size="small"
                :type="row.routeType === 'DOMESTIC' ? 'success' : 'warning'"
            >
              {{ row.routeType === 'DOMESTIC' ? '国内航线' : '国际航线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vesselLength" label="船长(m)" width="90">
          <template #default="{ row }">
            {{ row.vesselLength || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="vesselWidth" label="船宽(m)" width="90">
          <template #default="{ row }">
            {{ row.vesselWidth || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="吃水(m)" width="90">
          <template #default="{ row }">
            {{ row.draftAft || row.draftForward || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="plannedEndName" label="泊位" width="120" show-overflow-tooltip />
        <el-table-column prop="plannedStartName" label="预计锚地" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="tableState.total"
          layout="total, prev, pager, next"
          class="pagination"
          @current-change="fetchData"
      />
    </div>

    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="1000px"
        top="5vh"
        :close-on-click-modal="false"
        destroy-on-close
        class="pilotage-dialog"
    >
      <ApplicationForm
          v-model="currentApplication"
          :is-edit="isEditMode"
          @submit="handleFormSubmit"
          @cancel="dialogVisible = false"
      />
    </el-dialog>

    <GeneratePlanDialog
        v-model:visible="generatePlanDialogVisible"
        :selected-ships="multipleSelection"
        @success="handleGeneratePlanSuccess"
    />
  </div>
</template>

<style scoped lang="scss">
.applications-view {
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

.search-bar {
  padding: 16px;

  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .el-form-item {
      margin-bottom: 0;

      &:last-child {
        margin-left: auto;
      }
    }
  }
}

.table-container {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pagination {
  margin-top: 12px;
  justify-content: flex-end;
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

/* 多选框样式修复 */
:deep(.el-table th.el-table__column--selection .cell),
:deep(.el-table td.el-table__column--selection .cell) {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

:deep(.el-table .el-checkbox) {
  margin: 0 auto;
}

:deep(.el-checkbox__inner) {
  border: 1px solid #006994;
  background-color: #ffffff;
}

:deep(.el-checkbox__inner::after) {
  border-color: #006994;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #006994;
  border-color: #006994;
}
</style>