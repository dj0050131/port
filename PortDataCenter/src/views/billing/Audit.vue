<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Check, Close } from '@element-plus/icons-vue'
import {
  fetchBillingVouchers,
  approveBillingVouchers,
} from '@/api/billingVouchers'
import type {BillingResultVO, BillingVoucherListResponse} from '@/types/billingVouchers'
import { useDataCenterStore } from '@/stores/dataCenter'
import type {ApiResponse, PaginatedResult} from "@/types/common.ts";

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<BillingResultVO>('billing.audit', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const selectedRows = ref<BillingResultVO[]>([])

const searchForm = reactive({
  current: tableState.page,
  size: tableState.pageSize,
  auditStatus: '', // 默认显示待审核
  vesselNameZh: '',
  agentName: ''
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      current: searchForm.current,
      size: searchForm.size,
      auditStatus: searchForm.auditStatus || undefined,
      vesselNameZh: searchForm.vesselNameZh || undefined,
      agentName: searchForm.agentName || undefined
    }

    const response: ApiResponse<ApiResponse<PaginatedResult<BillingResultVO>>> = await fetchBillingVouchers(params)

    if (response.code === 200) {
      tableState.items = response.data.records || []
      tableState.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleSelectionChange = (rows: BillingResultVO[]) => {
  selectedRows.value = rows
}

const handleApprove = async (row?: BillingResultVO) => {
  const ids = row ? [row.voucherId] : selectedRows.value.map(r => r.voucherId)

  if (ids.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要审核通过选中的 ${ids.length} 条计费凭证吗？`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await approveBillingVouchers({
      ids,
      approved: true
    })

    if (response.code === 200) {
      ElMessage.success('审核通过成功')
      fetchData()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

const handleReject = async (row?: BillingResultVO) => {
  const ids = row ? [row.voucherId] : selectedRows.value.map(r => r.voucherId)

  if (ids.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '审核拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入拒绝原因...'
      }
    )

    const response = await approveBillingVouchers({
      ids,
      approved: false,
      rejectionReason: value
    })

    if (response.code === 200) {
      ElMessage.success('审核拒绝成功')
      await fetchData()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

const resetSearch = () => {
  searchForm.current = 1
  tableState.page = 1
  searchForm.auditStatus = ''
  searchForm.vesselNameZh = ''
  searchForm.agentName = ''
  fetchData()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  tableState.pageSize = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  searchForm.current = val
  tableState.page = val
  fetchData()
}

const getAuditStatusType = (row: BillingResultVO) => {
  const status = row.auditStatus

  // 如果是已拒绝状态，且同一签证单下有待审核的记录，显示为已重计费（黄色）
  if (status === 'REJECTED') {
    const hasPending = tableState.items.some(item =>
      item.certificateId === row.certificateId &&
      item.auditStatus === 'PENDING'
    )
    if (hasPending) {
      return 'warning'
    }
  }

  if (status === 'REBILLING') {
    return 'warning'
  }

  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getAuditStatusText = (row: BillingResultVO) => {
  const status = row.auditStatus

  // 如果是已拒绝状态，且同一签证单下有待审核的记录，显示为已重计费
  if (status === 'REJECTED') {
    const hasPending = tableState.items.some(item =>
      item.certificateId === row.certificateId &&
      item.auditStatus === 'PENDING'
    )
    if (hasPending) {
      return '已重计费'
    }
  }

  if (status === 'REBILLING') {
    return '待审核'
  }

  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已审核'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="audit-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">计费凭证审核</h2>
        <p class="page-desc">审核已生成的引航计费凭证</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="search-bar">
        <el-form inline size="small">
          <el-form-item label="船舶中文名">
            <el-input
              v-model="searchForm.vesselNameZh"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
            />
          </el-form-item>
          <el-form-item label="代理公司">
            <el-input
              v-model="searchForm.agentName"
              placeholder="请输入"
              clearable
              @keyup.enter="fetchData"
            />
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select
              v-model="searchForm.auditStatus"
              placeholder="请选择"
              clearable
              :fit-input-width="false"
              popper-class="wide-select-popper"
              style="width: 160px"
              @change="fetchData"
            >
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Refresh" @click="fetchData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
          <el-form-item class="audit-actions">
            <el-button
              type="danger"
              :icon="Close"
              @click="handleReject()"
              :disabled="selectedRows.length === 0"
            >
              审核拒绝
            </el-button>
            <el-button
              type="success"
              :icon="Check"
              @click="handleApprove()"
              :disabled="selectedRows.length === 0"
            >
              审核通过
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="tableState.items"
        v-loading="tableState.loading"
        stripe
        height="calc(100vh - 350px)"
        size="small"
        @selection-change="handleSelectionChange"
        border
      >
         <el-table-column type="selection" width="40" />
         <el-table-column prop="voucherCode" label="凭证编号" width="140" fixed="left" />
         <el-table-column prop="certificateCode" label="签证单号" width="140" />
         <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
         <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
         <el-table-column prop="agentName" label="代理公司" width="120" />
         <el-table-column prop="netTonnage" label="净吨" width="90" align="right" />
         <el-table-column prop="baseAmount" label="基本费" width="100" align="right">
           <template #default="{ row }">
             ¥{{ row.baseAmount?.toFixed(2) }}
           </template>
         </el-table-column>
         <el-table-column prop="surchargeAmount" label="附加费" width="100" align="right">
           <template #default="{ row }">
             ¥{{ row.surchargeAmount?.toFixed(2) }}
           </template>
         </el-table-column>
         <el-table-column prop="discountAmount" label="折扣" width="100" align="right">
           <template #default="{ row }">
             ¥{{ row.discountAmount?.toFixed(2) }}
           </template>
         </el-table-column>
         <el-table-column prop="totalAmount" label="合计" width="100" align="right">
           <template #default="{ row }">
             ¥{{ row.totalAmount?.toFixed(2) }}
           </template>
         </el-table-column>
         <el-table-column prop="auditStatus" label="审核状态" width="100">
           <template #default="{ row }">
             <el-tag :type="getAuditStatusType(row)" size="small">
               {{ getAuditStatusText(row) }}
             </el-tag>
           </template>
         </el-table-column>
         <el-table-column prop="createdAt" label="创建时间" width="140">
           <template #default="{ row }">
             {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
           </template>
         </el-table-column>
         <el-table-column label="操作" width="150" fixed="right">
           <template #default="{ row }">
             <el-button size="small" type="success" @click="handleApprove(row)" :disabled="row.auditStatus !== 'PENDING' && row.auditStatus !== 'REBILLING'">通过</el-button>
             <el-button size="small" type="danger" @click="handleReject(row)" :disabled="row.auditStatus !== 'PENDING' && row.auditStatus !== 'REBILLING'">拒绝</el-button>
           </template>
         </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.current"
          v-model:page-size="searchForm.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableState.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.audit-view {
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

.main-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  margin-bottom: 16px;
  padding: 12px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid var(--el-border-color-light);

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

.audit-actions {
  margin-left: auto;
  :deep(.el-form-item__content) {
    display: flex;
    gap: 8px;
  }
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

<style lang="scss">
.wide-select-popper {
  min-width: 220px !important;
}
</style>