<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Promotion, View } from '@element-plus/icons-vue'
import { fetchPaymentNoticeFailed, retryPaymentNotice } from '@/api/charge'
import type { PaymentNoticeFailureVO } from '@/types/charge'
import { useDataCenterStore } from '@/stores/dataCenter'

const activeTab = ref('failed')
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PaymentNoticeFailureVO>('billing.paymentNoticeFailures', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  keyword: '',
  page: tableState.page,
  size: tableState.pageSize,
})

const detailVisible = ref(false)
const currentRow = ref<PaymentNoticeFailureVO | null>(null)

let timer: number | undefined

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchPaymentNoticeFailed({
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
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleRetry = async (row: PaymentNoticeFailureVO) => {
  try {
    const okRes = await retryPaymentNotice(row.voucher.voucherId)
    if (okRes.code === 200) {
      ElMessage.success(okRes.data ? '已重新提交付款提示' : '提交失败，请稍后再试')
      await fetchData()
    } else {
      ElMessage.error(okRes.message || '重试失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('重试失败')
  }
}

const openDetail = (row: PaymentNoticeFailureVO) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleRefresh = async () => {
  await fetchData()
}

const handlePageChange = (p: number) => {
  searchForm.page = p
  tableState.page = p
  fetchData()
}

const handleSizeChange = (s: number) => {
  searchForm.size = s
  searchForm.page = 1
  tableState.pageSize = s
  tableState.page = 1
  fetchData()
}

onMounted(async () => {
  await fetchData()
  timer = window.setInterval(() => {
    fetchData()
  }, 15_000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<template>
  <div class="payment-notice-failures page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">付款提示异常</h2>
        <p class="page-desc">单独页面展示“提交外部系统失败/未提交”的待付款订单；重试/查看详情在本页完成</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="handleRefresh">手动刷新</el-button>
    </div>

    <div class="main-content glass-card">
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="提交异常(失败/未提交)" name="failed">
          <div class="toolbar">
            <el-input
              v-model="searchForm.keyword"
              placeholder="凭证编号/发票号"
              style="width: 240px; margin-right: 10px"
              clearable
              @keyup.enter="handleRefresh"
            />
            <el-button :icon="Refresh" @click="handleRefresh">查询</el-button>
          </div>

          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="voucher.voucherCode" label="凭证编号" width="160" />
            <el-table-column prop="voucher.vesselNameZh" label="中文船名" width="120" />
            <el-table-column prop="voucher.agentName" label="代理公司" width="140" />
            <el-table-column prop="voucher.totalAmount" label="金额" width="120" align="right">
              <template #default="{ row }">¥{{ row.voucher.totalAmount?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="lastAttemptAt" label="最后提交时间" width="180">
              <template #default="{ row }">{{ row.lastAttemptAt ? new Date(row.lastAttemptAt).toLocaleString() : '未提交' }}</template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="原因" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">{{ row.errorMessage || (row.lastAttemptAt ? '-' : '未提交到外部系统') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="View" @click="openDetail(row)">查看详情</el-button>
                <el-button link type="primary" size="small" :icon="Promotion" @click="handleRetry(row)">重试</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
        :total="tableState.total"
              :page-size="searchForm.size"
              :current-page="searchForm.page"
              @update:current-page="handlePageChange"
              @update:page-size="handleSizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="detailVisible" title="提交失败详情" width="760px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="凭证编号">{{ currentRow.voucher.voucherCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签证单号">{{ currentRow.voucher.certificateCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="中文船名">{{ currentRow.voucher.vesselNameZh || '-' }}</el-descriptions-item>
        <el-descriptions-item label="代理公司">{{ currentRow.voucher.agentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentRow.voucher.totalAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ currentRow.voucher.paymentStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ currentRow.voucher.auditStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentRow.voucher.createdAt ? new Date(currentRow.voucher.createdAt).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="最后提交时间">
          {{ currentRow.lastAttemptAt ? new Date(currentRow.lastAttemptAt).toLocaleString() : '未提交' }}
        </el-descriptions-item>
        <el-descriptions-item label="原因" :span="1">{{ currentRow.errorMessage || (currentRow.lastAttemptAt ? '-' : '未提交到外部系统') }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentRow" type="primary" :icon="Promotion" @click="handleRetry(currentRow)">重试提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.payment-notice-failures {
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

.tabs {
  height: 100%;
  display: flex;
  flex-direction: column;

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

.toolbar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

