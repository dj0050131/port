<script setup lang="ts">
import {reactive, onMounted, computed, ref} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Close, Delete } from '@element-plus/icons-vue'
import { fetchInvoices, redInvoices, voidInvoices } from '@/api/invoices'
import type { BillingResultVO } from '@/types/billingVouchers'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<BillingResultVO>('billing.invoiceVoid', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const selectedRows = ref<BillingResultVO[]>([])

const searchForm = reactive({
  keyword: '',
  page: tableState.page,
  size: tableState.pageSize,
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchInvoices({
      current: searchForm.page,
      size: searchForm.size,
      keyword: searchForm.keyword?.trim() || undefined,
    })
    if (res.code === 200) {
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('获取数据失败: ' + (error as Error).message)
  } finally {
    tableState.loading = false
  }
}

const handleSearch = () => {
  searchForm.page = 1
  fetchData()
}

const handleSelectionChange = (rows: BillingResultVO[]) => {
  selectedRows.value = rows
}

const hasSelection = computed(() => selectedRows.value.length > 0)
const selectedAllRedOrVoided = computed(() =>
  selectedRows.value.length > 0 &&
  selectedRows.value.every(r => r.electronicInvoiceStatus === 'RED' || r.electronicInvoiceStatus === 'VOIDED')
)
const isTransferLocked = (row: BillingResultVO) => {
  const ts = String((row as any).transferStatus || '').toUpperCase()
  return ts === 'PROCESSING' || ts === 'COMPLETED'
}
const canRed = computed(() =>
  hasSelection.value &&
  selectedRows.value.every(r => r.electronicInvoiceStatus !== 'RED' && r.electronicInvoiceStatus !== 'VOIDED') &&
  selectedRows.value.every(r => !isTransferLocked(r))
)
const canVoid = computed(() =>
  hasSelection.value &&
  selectedRows.value.every(r => r.electronicInvoiceStatus !== 'RED' && r.electronicInvoiceStatus !== 'VOIDED') &&
  selectedRows.value.every(r => !isTransferLocked(r))
)

const handleRed = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  if (!canRed.value) {
    ElMessage.warning('已冲红/已作废的记录不能再次冲红')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入冲红原因', '发票冲红', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '原因必填',
      inputValidator: (val: string) => !!val?.trim(),
      inputErrorMessage: '请填写原因',
    })
    const ids = selectedRows.value.map((r) => r.voucherId).filter(Boolean)
    const res = await redInvoices(ids, value.trim())
    if (res.code === 200) {
      ElMessage.success('冲红操作成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '冲红操作失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('冲红操作失败: ' + (error as Error).message)
  }
}

const handleInvalid = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  if (!canVoid.value) {
    ElMessage.warning('已冲红/已作废的记录不能再次作废')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '发票作废', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '原因必填',
      inputValidator: (val: string) => !!val?.trim(),
      inputErrorMessage: '请填写原因',
    })
    const ids = selectedRows.value.map((r) => r.voucherId).filter(Boolean)
    const res = await voidInvoices(ids, value.trim())
    if (res.code === 200) {
      ElMessage.success('作废操作成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '作废操作失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('作废操作失败: ' + (error as Error).message)
  }
}

const handlePageChange = (p: number) => {
  searchForm.page = p
  tableState.page = p
  fetchData()
}
const handleSizeChange = (s: number) => {
  searchForm.size = s
  searchForm.page = 1
  tableState.page = 1
  tableState.pageSize = s
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="invoice-void-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">票据冲红/作废</h2>
        <p class="page-desc">处理引航费收发票的冲红与作废操作</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
         <el-input v-model="searchForm.keyword" placeholder="发票号/凭证编号" style="width: 220px; margin-right: 10px" clearable />
         <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
         <el-button type="danger" :icon="Close" :disabled="!canRed" @click="handleRed">冲红</el-button>
         <el-button type="warning" :icon="Delete" :disabled="!canVoid" @click="handleInvalid">作废</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="40" />
         <el-table-column prop="planCode" label="计划编号" width="140" />
         <el-table-column prop="voucherCode" label="凭证编号" width="140" />
         <el-table-column prop="certificateCode" label="签证单号" width="140" />
         <el-table-column prop="invoiceNumber" label="发票号码" width="140" />
         <el-table-column prop="agentName" label="代理公司" width="140" show-overflow-tooltip />
         <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
         <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
         <el-table-column prop="totalAmount" label="金额" width="110" align="right">
            <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
         </el-table-column>
         <el-table-column prop="transferStatus" label="移交状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="isTransferLocked(row) ? 'warning' : 'info'">
                {{ row.transferStatus || '-' }}
              </el-tag>
            </template>
         </el-table-column>
         <el-table-column prop="invoiceNotes" label="备注/原因" min-width="180" show-overflow-tooltip />
         <el-table-column prop="electronicInvoiceStatus" label="状态" width="120">
            <template #default="{ row }">
               <el-tag
                 size="small"
                 :type="row.electronicInvoiceStatus === 'RED' ? 'danger' : (row.electronicInvoiceStatus === 'VOIDED' ? 'warning' : 'success')"
               >
                 {{ row.electronicInvoiceStatus || '-' }}
               </el-tag>
            </template>
         </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="tableState.total"
          :page-size="tableState.pageSize"
          :current-page="tableState.page"
          @update:current-page="handlePageChange"
          @update:page-size="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.invoice-void-view {
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

.toolbar { margin-bottom: 12px; display: flex; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>