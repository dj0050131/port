<script setup lang="ts">
import {ref, reactive, onMounted, computed} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import type { PilotageApplication } from '@/types/pilotage'
import ApplicationForm from './components/ApplicationForm.vue'
import {usePilotageStore} from "@/stores/pilotage.ts"
import { useAuthStore } from '@/stores/auth'
import { useDataCenterStore } from '@/stores/dataCenter'

const pilotageStore = usePilotageStore()
const auth = useAuthStore()
const multipleSelection = ref<PilotageApplication[]>([]) // 添加多选状态

const perms = computed(() => auth.user?.permissions || [])
const canCreate = computed(() => perms.value.includes('*') || perms.value.includes('PILOTAGE:APPLICATIONS:CREATE'))
const canReview = computed(() => perms.value.includes('*') || perms.value.includes('PILOTAGE:APPLICATIONS:REVIEW'))
const canDelete = computed(() => perms.value.includes('*') || perms.value.includes('PILOTAGE:APPLICATIONS:DELETE'))

const handleSelectionChange = (selection: PilotageApplication[]) => {
  multipleSelection.value = selection
}

import { batchReviewPilotageApplications, batchDeletePilotageApplications } from '@/api/pilotage'

const handleBatchApprove = async () => {
  if (!canReview.value) {
    ElMessage.warning('当前账号无审核权限')
    return
  }
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  const nonPendingItems = multipleSelection.value.filter(item => item.requestStatus !== 'PENDING');
  if (nonPendingItems.length > 0) {
    ElMessage.warning('只能对状态为待审核的申请进行批量操作')
    return;
  }

  try {
    await ElMessageBox.confirm(`确认通过选中的 ${multipleSelection.value.length} 条申请?`, '确认操作', {
      type: 'warning'
    })

    const ids = multipleSelection.value.map(item => item.id)
    await batchReviewPilotageApplications(ids, true)

    ElMessage.success('批量通过成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const rejectDialogVisible = ref(false)
const rejectReason = ref('')

const handleBatchReject = () => {
  if (!canReview.value) {
    ElMessage.warning('当前账号无审核权限')
    return
  }
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  const nonPendingItems = multipleSelection.value.filter(item => item.requestStatus !== 'PENDING');
  if (nonPendingItems.length > 0) {
    ElMessage.warning('只能对状态为待审核的申请进行批量操作')
    return;
  }
  
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  try {
    const ids = multipleSelection.value.map(item => item.id)
    await batchReviewPilotageApplications(ids, false, rejectReason.value)

    ElMessage.success('批量驳回成功')
    rejectDialogVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleBatchDelete = async () => {
  if (!canDelete.value) {
    ElMessage.warning('当前账号无删除权限')
    return
  }
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  const nonPendingItems = multipleSelection.value.filter(item => item.requestStatus !== 'PENDING');
  if (nonPendingItems.length > 0) {
    ElMessage.warning('只能对状态为待审核的申请进行批量操作')
    return;
  }

  try {
    await ElMessageBox.confirm(`确认删除选中的 ${multipleSelection.value.length} 条申请?`, '确认操作', {
      type: 'warning'
    })

    const ids = multipleSelection.value.map(item => item.id)
    await batchDeletePilotageApplications(ids)

    ElMessage.success('批量删除成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PilotageApplication>('pilotage.applications', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const searchForm = reactive({
  keyword: '',
  status: '',
  startDate: '',
  endDate: ''
})

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
    if (searchForm.keyword?.trim()) filters.keyword = searchForm.keyword.trim()
    if (searchForm.status?.trim()) filters.requestStatus = searchForm.status.trim()
    await pilotageStore.fetchApplications(page, filters)
    if (pilotageStore.pagination) {
      tableState.total = pilotageStore.pagination.total
      pagination.value.page = pilotageStore.pagination.page
      pagination.value.size = pilotageStore.pagination.pageSize
      tableState.page = pilotageStore.pagination.page
      tableState.pageSize = pilotageStore.pagination.pageSize
    }
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    tableState.loading = false
  }
}

const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.page = 1
  tableState.pageSize = size
  tableState.page = 1
  fetchData(1)
}

const handleSearch = () => {
  pagination.value.page = 1
  tableState.page = 1
  fetchData(1)
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

const handleCreate = () => {
  currentApplication.value = {}
  dialogTitle.value = '新增引航申请'
  isEditMode.value = false
  dialogVisible.value = true
}

const handleEdit = (row: PilotageApplication) => {
  currentApplication.value = { ...row }
  dialogTitle.value = '编辑引航申请'
  isEditMode.value = true
  dialogVisible.value = true
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
  try {
    await ElMessageBox.confirm(`确认删除申请 ${row.nameZh}?`, '警告', { type: 'warning' })
    // 使用批量删除API，传入单个ID的数组
    await batchDeletePilotageApplications([row.id])
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
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
    IN_PROGRESS: 'primary',  // 蓝色 - 进行中 (与 PLANNED 共享)
    COMPLETED: 'success',    // 绿色 - 已完成 (与 APPROVED 共享)
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
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="handleCreate">新增申请</el-button>
    </div>

    <div class="search-bar glass-card">
      <el-form inline size="default">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="船名/呼号/代理" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>

          <el-button
              type="success"
              @click="handleBatchApprove"
              :disabled="multipleSelection.length === 0"
              v-if="canReview && (searchForm.status === 'PENDING' || !searchForm.status)"
          >
            批量通过
          </el-button>
          <el-button
              type="warning"
              @click="handleBatchReject"
              :disabled="multipleSelection.length === 0"
              v-if="canReview && (searchForm.status === 'PENDING' || !searchForm.status)"
          >
            批量不通过
          </el-button>
          <el-button
              type="danger"
              @click="handleBatchDelete"
              :disabled="multipleSelection.length === 0"
              v-if="canDelete && (searchForm.status === 'PENDING' || !searchForm.status)"
          >
            批量删除
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container glass-card">
      <el-table
          :data="tableData"
          v-loading="tableState.loading"
          stripe
          height="100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="#" width="40px" />
        <el-table-column prop="requestCode" label="申请编号" width="120" />
        <el-table-column prop="createdAt" label="申请日期" width="120">
          <template #default="{ row }">
            {{ row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="nameZh" label="中文船名" min-width="120" />
        <el-table-column prop="nameEn" label="英文船名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nationality" label="国籍" width="80" />
        <el-table-column prop="grossTonnage" label="总吨" width="80">
          <template #default="{ row }">
            {{ row.grossTonnage ? Number(row.grossTonnage).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="netTonnage" label="净吨" width="80">
          <template #default="{ row }">
            {{ row.netTonnage ? Number(row.netTonnage).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deadweight" label="载重吨" width="90">
          <template #default="{ row }">
            {{ row.deadweight ? Number(row.deadweight).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="vesselLength" label="船长(m)" width="80">
          <template #default="{ row }">
            {{ row.vesselLength || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="vesselWidth" label="船宽(m)" width="80">
          <template #default="{ row }">
            {{ row.vesselWidth || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="吃水(m)" width="80">
          <template #default="{ row }">
            {{ row.draftAft || row.draftForward || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="routeType" label="航线种类" width="80">
          <template #default="{ row }">
            <el-tag
                size="small"
                :type="row.routeType === 'DOMESTIC' ? 'success' : 'warning'"
            >
              {{ row.routeType === 'DOMESTIC' ? '国内航线' : '国际航线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedEndName" label="预抵泊位" width="120" show-overflow-tooltip />
        <el-table-column prop="plannedStartName" label="预抵锚地" width="120" show-overflow-tooltip />
        <el-table-column prop="agentName" label="代理" width="150" show-overflow-tooltip />
        <el-table-column prop="voyageStatus" label="航次类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ row.voyageType === 'IMPORT' ? '进港' :
                row.voyageType === 'EXPORT' ? '出港' :
                row.voyageType === 'TRANSIT' ? '移泊' : row.voyageType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eta" label="预抵时间" width="140">
          <template #default="{ row }">
            {{ row.eta ? new Date(row.eta).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewerUsername" label="审核人" width="100">
          <template #default="{ row }">
            {{ row.reviewerUsername || '-' }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="row.requestStatus === 'PENDING'"
              link 
              type="danger" 
              :icon="Delete" 
              @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="tableState.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="handleSizeChange"
          @current-change="fetchData"
      />
    </div>

    <!-- Dialog -->
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

    <!-- Reject Reason Dialog -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回原因"
      width="500px"
      append-to-body
    >
      <el-form>
        <el-form-item label="原因">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject">确认驳回</el-button>
        </span>
      </template>
    </el-dialog>
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
  /* Ensure dialog body has no extra padding for the clean look */
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