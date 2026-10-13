<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Edit, DocumentAdd, DocumentChecked, Stamp, View, Delete, Download, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  fetchCertificateList,
  submitCertificate,
  confirmCertificate,
  pilotSignCertificate,
  deleteCertificate,
  exportCertificates,
  type CertificateDetail,
  type CertificateQueryParams
} from '@/api/certificate'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDataCenterStore } from '@/stores/dataCenter'
import { useAuthStore } from '@/stores/auth'

import CertificateCreateForm from './components/CertificateCreateForm.vue'
import CertificateDetailView from './components/CertificateDetailView.vue'
import CertificateEditForm from './components/CertificateEditForm.vue'

const router = useRouter()
const dataCenterStore = useDataCenterStore()
const authStore = useAuthStore()

const currentUserType = () => String(authStore.user?.userType || '').toUpperCase()
const isPilotUser = () => currentUserType() === 'PILOT'
const isAgentUser = () => currentUserType() === 'AGENT'
const isAdminUser = () => currentUserType() === 'ADMIN'
const tableState = dataCenterStore.ensureTable<CertificateDetail>('pilotage.visa', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const exportLoading = ref(false)
const selectedRows = ref<CertificateDetail[]>([])
const dateRange = ref<string[]>([])
const searchForm = reactive<CertificateQueryParams>({
  page: tableState.page,
  size: tableState.pageSize,
  startDate: '',
  endDate: '',
  vesselNameZh: '',
  vesselNameEn: '',
  vesselCallSign: '',
  imoNumber: '',
  certificateCode: '',
  voyageNumber: '',
  submitStatus: '',
  billingStatus: '',
  agentId: undefined,
  pilotId: undefined
})

const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const selectedCertificateId = ref<number>(0)
const selectedPlanId = ref<number>(0)

const isRouteReady = (row: CertificateDetail) => {
  const code = String((row as any).routeCode || '').trim()
  const name = String((row as any).routeName || '').trim()
  if (!code || !name) return false
  if (code.toUpperCase() === 'AUTO') return false
  return true
}

const getAvailableActions = (row: CertificateDetail) => {
  const actions = []

  // 查看详情
  actions.push({
    name: '查看',
    icon: View,
    type: 'primary',
    action: () => viewCertificate(row.id),
    show: true
  })

  if (row.submitStatus === 'UNSUBMITTED') {
    actions.push({
      name: '编辑',
      icon: Edit,
      type: 'primary',
      action: () => editCertificate(row.id),
      show: true
    })
  }

  if (row.submitStatus === 'UNSUBMITTED' && isRouteReady(row)) {
    actions.push({
      name: '提交',
      icon: DocumentAdd,
      type: 'success',
      action: () => handleSubmit(row),
      show: true
    })
  }

  // 业务约束：必须先“引航员签字”，代理才能确认
  if (row.submitStatus === 'SUBMITTED' && !!row.pilotSignTime && (isAgentUser() || isAdminUser())) {
    actions.push({
      name: '确认',
      icon: DocumentChecked,
      type: 'success',
      action: () => handleConfirm(row),
      show: true
    })
  }

  if (row.submitStatus === 'SUBMITTED' && !row.pilotSignTime && (isPilotUser() || isAdminUser())) {
    actions.push({
      name: '签字',
      icon: Stamp,
      type: 'warning',
      action: () => handleSign(row),
      show: true
    })
  }

  if (row.submitStatus === 'UNSUBMITTED') {
    actions.push({
      name: '删除',
      icon: Delete,
      type: 'danger',
      action: () => handleDelete(row),
      show: true
    })
  }

  return actions
}

const fetchData = async () => {
  tableState.loading = true
  try {
    if (!searchForm.startDate || searchForm.startDate === '') {
      delete searchForm.startDate
    }
    if (!searchForm.endDate || searchForm.endDate === '') {
      delete searchForm.endDate
    }
    const response = await fetchCertificateList(searchForm)
    if (response.code === 200) {
      tableState.items = response.data.records || response.data.items || []
      tableState.total = response.data.total || 0
    }
  } catch (error) {
    console.error('获取签证单列表失败:', error)
    ElMessage.error('获取签证单列表失败')
  } finally {
    tableState.loading = false
  }
}
const handleDateRangeChange = (val: string[]) => {
  if (val && val.length === 2) {
    searchForm.startDate = val[0] || null
    searchForm.endDate = val[1] || null
  } else {
    delete searchForm.startDate
    delete searchForm.endDate
  }
  fetchData()
}

const getSignTagType = (row: CertificateDetail) => (row.pilotSignTime ? 'success' : 'info')
const getSignText = (row: CertificateDetail) => (row.pilotSignTime ? '已签字' : '未签字')
const formatDateTime = (dateString?: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString()
}

const resetSearch = () => {
  searchForm.page = 1
  tableState.page = 1
  searchForm.startDate = ''
  searchForm.endDate = ''
  searchForm.vesselNameZh = ''
  searchForm.vesselNameEn = ''
  searchForm.vesselCallSign = ''
  searchForm.imoNumber = ''
  searchForm.certificateCode = ''
  searchForm.voyageNumber = ''
  searchForm.submitStatus = ''
  searchForm.billingStatus = ''
  dateRange.value = []
  searchForm.agentId = undefined
  searchForm.pilotId = undefined
  fetchData()
}

const openCreateDialog = (planId?: number) => {
  selectedPlanId.value = planId || 0
  showCreateDialog.value = true
}

const viewCertificate = (certificateId: number) => {
  selectedCertificateId.value = certificateId
  showDetailDialog.value = true
}

const editCertificate = (certificateId: number) => {
  selectedCertificateId.value = certificateId
  showEditDialog.value = true
}

const handleSubmit = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm('确定要提交该签证单吗？提交后将不能修改。', '确认提交', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await submitCertificate(row.id)
    if (response.code === 200 && response.data) {
      ElMessage.success('提交成功')
      fetchData()
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('提交失败:', error)
    const msg = String(error?.response?.data?.message || error?.message || '提交失败')
    ElMessage.error(msg)
    if (msg.includes('航线区间信息未补齐')) {
      // 直接引导用户进入编辑补录航线区间
      editCertificate(row.id)
    }
  }
}

const handleConfirm = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm('确定要确认该签证单吗？', '确认签证单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await confirmCertificate(row.id)
    if (response.code === 200 && response.data) {
      ElMessage.success('确认成功')
      await fetchData()
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('确认失败:', error)
    ElMessage.error(error.message || '确认失败')
  }
}

const handleSign = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm('确定要签字确认吗？', '签字确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await pilotSignCertificate(row.id)
    if (response.code === 200 && response.data) {
      ElMessage.success('签字成功')
      fetchData()
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('签字失败:', error)
    ElMessage.error(error.message || '签字失败')
  }
}

const handleDelete = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm('确定要删除该签证单吗？删除后不可恢复。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    const response = await deleteCertificate(row.id)
    if (response.code === 200 && response.data) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('删除失败:', error)
    ElMessage.error(error.message || '删除失败')
  }
}

const handleSelectionChange = (rows: CertificateDetail[]) => {
  selectedRows.value = rows
}

const handlePageChange = (page: number) => {
  searchForm.page = page
  tableState.page = page
  fetchData()
}

const handlePageSizeChange = (size: number) => {
  searchForm.size = size
  searchForm.page = 1
  tableState.pageSize = size
  tableState.page = 1
  fetchData()
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    const response = await exportCertificates({
      ...searchForm,
      ids: selectedRows.value.length > 0 ? selectedRows.value.map(r => r.id).join(',') : undefined,
    })

    const blob = (response as any).data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `签证单_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleCreateSuccess = () => {
  showCreateDialog.value = false
  fetchData()
}

const handleCreateCancel = () => {
  showCreateDialog.value = false
}

const handleDetailClose = () => {
  showDetailDialog.value = false
}

const handleDetailEdit = (certificateId: number) => {
  showDetailDialog.value = false
  editCertificate(certificateId)
}

const handleEditSuccess = () => {
  showEditDialog.value = false
  fetchData()
}

const handleEditCancel = () => {
  showEditDialog.value = false
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'UNSUBMITTED': return 'info'
    case 'SUBMITTED': return 'warning'
    case 'CONFIRMED': return 'success'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'UNSUBMITTED': return '未提交'
    case 'SUBMITTED': return '已提交'
    case 'CONFIRMED': return '已确认'
    default: return status
  }
}

// 计费状态标签颜色
const getBillingStatusTagType = (status: string) => {
  switch (status) {
    case 'UNBILLED': return 'info'
    case 'BILLED': return 'warning'
    case 'BILLING_EXCEPTION': return 'danger'
    case 'PAID': return 'success'
    default: return 'info'
  }
}

const getBillingStatusText = (status: string) => {
  switch (status) {
    case 'UNBILLED': return '未计费'
    case 'BILLED': return '已计费'
    case 'BILLING_EXCEPTION': return '计费异常'
    case 'PAID': return '已付款'
    default: return status
  }
}

const getTradeTypeText = (type: string) => {
  switch (type) {
    case 'IMPORT': return '进口'
    case 'EXPORT': return '出口'
    case 'TRANSIT': return '中转'
    default: return type || '-'
  }
}

const getPilotageTypeText = (type: string) => {
  switch (type) {
    case 'STANDARD': return '标准引航'
    case 'EMERGENCY': return '应急引航'
    case 'SPECIAL': return '特殊引航'
    default: return type || '-'
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="visa-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">签证单管理</h2>
        <p class="page-desc">引航签证单据管理与状态跟踪</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog()">
          创建签证单
        </el-button>
        <el-button type="success" :icon="Download" @click="handleExport" :loading="exportLoading">
          导出
        </el-button>
      </div>
    </div>

    <div class="search-bar glass-card">
      <el-form :model="searchForm" inline size="small" label-width="80px">
        <el-form-item label="日期范围">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item label="中文船名">
          <el-input
              v-model="searchForm.vesselNameZh"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="英文船名">
          <el-input
              v-model="searchForm.vesselNameEn"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="船舶呼号">
          <el-input
              v-model="searchForm.vesselCallSign"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="IMO编号">
          <el-input
              v-model="searchForm.imoNumber"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="签证单号">
          <el-input
              v-model="searchForm.certificateCode"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="航次号">
          <el-input
              v-model="searchForm.voyageNumber"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
              @clear="fetchData"
          />
        </el-form-item>
        <el-form-item label="提交状态">
          <el-select
            v-model="searchForm.submitStatus"
            placeholder="请选择"
            clearable
            :fit-input-width="false"
            popper-class="wide-select-popper"
            style="width: 160px"
            @change="fetchData"
          >
            <el-option label="全部" value="" />
            <el-option label="未提交" value="UNSUBMITTED" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已确认" value="CONFIRMED" />
          </el-select>
        </el-form-item>
        <el-form-item label="计费状态">
          <el-select
            v-model="searchForm.billingStatus"
            placeholder="请选择"
            clearable
            :fit-input-width="false"
            popper-class="wide-select-popper"
            style="width: 160px"
            @change="fetchData"
          >
            <el-option label="全部" value="" />
            <el-option label="未计费" value="UNBILLED" />
            <el-option label="已计费" value="BILLED" />
            <el-option label="计费异常" value="BILLING_EXCEPTION" />
            <el-option label="已付款" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container glass-card">
      <el-table
        :data="tableState.items"
        v-loading="tableState.loading"
          stripe
          height="calc(100vh - 280px)"
          size="small"
          border
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column prop="certificateCode" label="签证单号" width="140" fixed="left" />
        <el-table-column prop="submitStatus" label="提交状态" width="100" fixed="left">
          <template #default="{ row }">
            <el-tag
                size="small"
                :type="getStatusTagType(row.submitStatus)"
            >
              {{ getStatusText(row.submitStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billingStatus" label="计费状态" width="100" fixed="left">
          <template #default="{ row }">
            <el-tag
                size="small"
                :type="getBillingStatusTagType(row.billingStatus)"
            >
              {{ getBillingStatusText(row.billingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签字状态" width="90" fixed="left">
          <template #default="{ row }">
            <el-tag size="small" :type="getSignTagType(row)">
              {{ getSignText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pilotSignTime" label="签字时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.pilotSignTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
        <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
        <el-table-column prop="vesselCallSign" label="船舶呼号" width="100" />
        <el-table-column prop="imoNumber" label="IMO编号" width="120" />
        <el-table-column prop="voyageNumber" label="航次" width="120" />
        <el-table-column prop="voyageType" label="航次类型" width="80">
          <template #default="{ row }">
            {{ getTradeTypeText(row.voyageType) }}
          </template>
        </el-table-column>
        <el-table-column prop="assignedPilotName" label="主引" width="80" />
        <el-table-column prop="inboundAgentName" label="进口代理" width="120" show-overflow-tooltip />
        <el-table-column prop="outboundAgentName" label="出口代理" width="120" show-overflow-tooltip />
        <el-table-column prop="pilotageType" label="引航类型" width="100">
          <template #default="{ row }">
            {{ getPilotageTypeText(row.pilotageType) }}
          </template>
        </el-table-column>
        <el-table-column prop="grossTonnage" label="总吨" width="90" align="right">
          <template #default="{ row }">
            {{ row.grossTonnage ? row.grossTonnage.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="netTonnage" label="净吨" width="90" align="right">
          <template #default="{ row }">
            {{ row.netTonnage ? row.netTonnage.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="forwardDraft" label="前吃水" width="80" align="right">
          <template #default="{ row }">
            {{ row.forwardDraft ? row.forwardDraft.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="aftDraft" label="后吃水" width="80" align="right">
          <template #default="{ row }">
            {{ row.aftDraft ? row.aftDraft.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPassengers" label="旅客" width="60" align="right">
          <template #default="{ row }">
            {{ row.totalPassengers || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCrew" label="船员" width="60" align="right">
          <template #default="{ row }">
            {{ row.totalCrew || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="pilotageStartTime" label="开始时间" width="140">
          <template #default="{ row }">
            {{ row.pilotageStartTime ? new Date(row.pilotageStartTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="pilotageEndTime" label="结束时间" width="140">
          <template #default="{ row }">
            {{ row.pilotageEndTime ? new Date(row.pilotageEndTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
<!--        <el-table-column prop="boardingTime" label="登船时间" width="140">-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="leavingTime" label="离船时间" width="140">-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="submittedAt" label="提交时间" width="140">-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="confirmedAt" label="确认时间" width="140">-->
<!--          </template>-->
<!--        </el-table-column>-->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                  v-for="action in getAvailableActions(row)"
                  :key="action.name"
                  :type="action.type"
                  :icon="action.icon"
                  size="small"
                  @click="action.action"
                  v-show="action.show"
              >
                {{ action.name }}
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="searchForm.page"
            v-model:page-size="searchForm.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="tableState.total"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
        v-model="showCreateDialog"
        title="创建签证单"
        width="800px"
        :close-on-click-modal="false"
        :destroy-on-close="true"
    >
      <CertificateCreateForm
          :plan-id="selectedPlanId"
          @success="handleCreateSuccess"
          @cancel="handleCreateCancel"
      />
    </el-dialog>

    <el-dialog
        v-model="showDetailDialog"
        title="签证单详情"
        width="900px"
        :close-on-click-modal="false"
        :destroy-on-close="true"
    >
      <CertificateDetailView
          v-if="showDetailDialog"
          :certificate-id="selectedCertificateId"
          @close="handleDetailClose"
          @edit="handleDetailEdit"
      />
    </el-dialog>

    <el-dialog
        v-model="showEditDialog"
        title="编辑签证单"
        width="800px"
        :close-on-click-modal="false"
        :destroy-on-close="true"
    >
      <CertificateEditForm
          v-if="showEditDialog"
          :certificate-id="selectedCertificateId"
          @success="handleEditSuccess"
          @cancel="handleEditCancel"
      />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.visa-view {
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

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.search-bar {
  padding: 16px;

  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .el-form-item {
      margin-bottom: 0;

      :deep(.el-form-item__label) {
        padding-right: 8px;
      }
    }
  }
}

.table-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .el-table {
    flex: 1;
    border-radius: 8px;
    overflow: hidden;
  }
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

<style lang="scss">
/* el-select 下拉框是 teleport 到 body 的，scoped 样式无法命中，用全局样式保证选项不被截断 */
.wide-select-popper {
  min-width: 220px !important;
}
</style>