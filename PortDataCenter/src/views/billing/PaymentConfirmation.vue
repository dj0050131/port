<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, View, Download, Promotion } from '@element-plus/icons-vue'
import { 
  fetchPendingConfirmationCharges,
  sendElectronicInvoice,
  fetchElectronicInvoiceDetail,
  downloadElectronicInvoicePdf
} from '@/api/charge'
import { fetchInvoices } from '@/api/invoices'
import type { BillingResultVO } from '@/types/billingVouchers'
import type { PageQueryParams } from '@/api/charge'
import { useDataCenterStore } from '@/stores/dataCenter'

const activeTab = ref('payment')
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<BillingResultVO>('billing.paymentConfirmation', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const invoiceStatusFilter = ref<string>('') // '' 表示全部

const searchForm = reactive({
  page: tableState.page,
  size: tableState.pageSize
})

const fetchData = async () => {
  tableState.loading = true
  try {
    let response
    const params: PageQueryParams = {
      current: searchForm.page,
      size: searchForm.size
    }
    
    if (activeTab.value === 'payment') {
      response = await fetchPendingConfirmationCharges(params)
    } else {
      response = await fetchInvoices({
        current: searchForm.page,
        size: searchForm.size,
        status: invoiceStatusFilter.value || undefined,
      })
    }
    
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

const handleTabChange = () => {
  searchForm.page = 1
  tableState.page = 1
  fetchData()
}

const handleInvoiceFilterChange = () => {
  searchForm.page = 1
  tableState.page = 1
  fetchData()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  searchForm.page = 1
  tableState.pageSize = val
  tableState.page = 1
  fetchData()
}

const handleCurrentChange = (val: number) => {
  searchForm.page = val
  tableState.page = val
  fetchData()
}

const dialogVisible = ref(false)
const currentRow = ref<BillingResultVO | null>(null)
const detailLoading = ref(false)
const invoiceDetail = ref<Record<string, any> | null>(null)

const openDetail = (row: BillingResultVO) => {
  currentRow.value = row
  dialogVisible.value = true
  loadInvoiceDetail(row.voucherId!)
}

const loadInvoiceDetail = async (voucherId: number) => {
  detailLoading.value = true
  try {
    const res = await fetchElectronicInvoiceDetail(voucherId)
    if (res.code === 200) {
      invoiceDetail.value = (res.data || null) as any
    } else {
      invoiceDetail.value = null
      ElMessage.error(res.message || '获取发票详情失败')
    }
  } catch (e) {
    console.error(e)
    invoiceDetail.value = null
    ElMessage.error('获取发票详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleDownload = async (row: BillingResultVO) => {
  try {
    const resp = await downloadElectronicInvoicePdf(row.voucherId!)
    const blob = new Blob([resp.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `invoice_${row.voucherCode || row.voucherId}.pdf`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)

    ElMessage.success('电子发票PDF下载成功')
    await fetchData()
    if (currentRow.value?.voucherId === row.voucherId) {
      await loadInvoiceDetail(row.voucherId!)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('PDF下载失败')
  }
}

const handleSend = async (row: BillingResultVO) => {
  try {
    if (row.electronicInvoiceStatus === 'SENT') {
      await ElMessageBox.confirm('该电子发票已发送，是否重新发送？', '确认重发', { type: 'warning' })
    }
    const res = await sendElectronicInvoice(row.voucherId!)
    if (res.code === 200) {
      ElMessage.success(row.electronicInvoiceStatus === 'SENT' ? '已重新发送(模拟)' : '已发送(模拟)')
      await fetchData()
      if (currentRow.value?.voucherId === row.voucherId) {
        currentRow.value = res.data as any
      }
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('发送失败')
  }
}

const getAuditStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getAuditStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已审核'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

const getPaymentStatusType = (status: string) => {
  switch (status) {
    case 'UNPAID': return 'warning'
    case 'PARTIAL_PAID': return 'primary'
    case 'FULLY_PAID': return 'success'
    default: return 'info'
  }
}

const getPaymentStatusText = (status: string) => {
  switch (status) {
    case 'UNPAID': return '未支付'
    case 'PARTIAL_PAID': return '部分支付'
    case 'FULLY_PAID': return '已支付'
    default: return status
  }
}

const getEInvoiceStatusType = (status?: string) => {
  switch (status) {
    case 'NOT_GENERATED': return 'info'
    case 'GENERATING': return 'warning'
    case 'GENERATED': return 'success'
    case 'SENT': return 'success'
    case 'RED': return 'danger'
    case 'VOIDED': return 'danger'
    default: return 'info'
  }
}

const getEInvoiceStatusText = (status?: string) => {
  switch (status) {
    case 'NOT_GENERATED': return '未生成'
    case 'GENERATING': return '生成中'
    case 'GENERATED': return '已生成'
    case 'SENT': return '已发送'
    case 'RED': return '已红冲'
    case 'VOIDED': return '已作废'
    default: return status || '-'
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="payment-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">费用到款确认</h2>
        <p class="page-desc">确认引航费用的到款情况及发票管理</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <el-tabs v-model="activeTab" class="billing-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="待付款(外部系统自动回调)" name="payment">
           <div class="toolbar">
             <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
           </div>
           <el-table 
            :data="tableState.items" 
            v-loading="tableState.loading" 
             stripe 
             height="calc(100vh - 300px)" 
             size="small"
             border
           >
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
                  <el-tag :type="getAuditStatusType(row.auditStatus)" size="small">
                    {{ getAuditStatusText(row.auditStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paymentStatus" label="支付状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getPaymentStatusType(row.paymentStatus)" size="small">
                    {{ getPaymentStatusText(row.paymentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="140">
                <template #default="{ row }">
                  {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
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
               @size-change="handleSizeChange"
               @current-change="handleCurrentChange"
             />
           </div>
        </el-tab-pane>

        <el-tab-pane label="电子发票(自动生成)" name="invoice">
           <div class="toolbar">
             <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
             <el-select
               v-model="invoiceStatusFilter"
               placeholder="电子发票状态(全部)"
               clearable
               style="width: 200px; margin-left: 10px"
               @change="handleInvoiceFilterChange"
             >
               <el-option label="全部" value="" />
               <el-option label="已生成" value="GENERATED" />
               <el-option label="已发送" value="SENT" />
               <el-option label="已红冲" value="RED" />
               <el-option label="已作废" value="VOIDED" />
             </el-select>
           </div>
           <el-table 
            :data="tableState.items" 
            v-loading="tableState.loading" 
             stripe 
             height="calc(100vh - 300px)" 
             size="small"
             border
           >
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
                  <el-tag :type="getAuditStatusType(row.auditStatus)" size="small">
                    {{ getAuditStatusText(row.auditStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paymentStatus" label="支付状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getPaymentStatusType(row.paymentStatus)" size="small">
                    {{ getPaymentStatusText(row.paymentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="electronicInvoiceStatus" label="电子发票" width="110">
                <template #default="{ row }">
                  <el-tag :type="getEInvoiceStatusType(row.electronicInvoiceStatus)" size="small">
                    {{ getEInvoiceStatusText(row.electronicInvoiceStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="checkCode" label="校验码" width="160" show-overflow-tooltip />
              <el-table-column prop="electronicGeneratedAt" label="生成时间" width="160">
                <template #default="{ row }">
                  {{ row.electronicGeneratedAt ? new Date(row.electronicGeneratedAt).toLocaleString() : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="downloadCount" label="下载次数" width="90" align="right">
                <template #default="{ row }">
                  {{ row.downloadCount ?? 0 }}
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="140">
                <template #default="{ row }">
                  {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" :icon="View" @click="openDetail(row)">查看</el-button>
                  <el-button size="small" :icon="Download" @click="handleDownload(row)">下载</el-button>
                  <el-button
                    size="small"
                    type="success"
                    :icon="Promotion"
                    :disabled="row.electronicInvoiceStatus === 'VOIDED' || row.electronicInvoiceStatus === 'RED'"
                    @click="handleSend(row)"
                  >
                    {{ row.electronicInvoiceStatus === 'SENT' ? '重发' : '发送' }}
                  </el-button>
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
               @size-change="handleSizeChange"
               @current-change="handleCurrentChange"
             />
           </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="dialogVisible" title="电子发票详情" width="720px">
      <div v-loading="detailLoading">
        <el-alert
          v-if="invoiceDetail?.snapshot_info?.warning"
          type="warning"
          :closable="false"
          style="margin-bottom: 12px"
        >
          {{ invoiceDetail.snapshot_info.warning }}
        </el-alert>

        <el-alert
          v-if="invoiceDetail?.seller_info?.config_hint"
          type="error"
          :closable="false"
          style="margin-bottom: 12px"
        >
          {{ invoiceDetail.seller_info.config_hint }}
        </el-alert>

        <el-descriptions v-if="invoiceDetail?.invoice_info" :column="2" border>
          <el-descriptions-item label="计费凭证编号">{{ invoiceDetail.invoice_info.voucher_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开票时间">
            {{ invoiceDetail.invoice_info.invoice_time ? new Date(invoiceDetail.invoice_info.invoice_time).toLocaleString() : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="发票代码">{{ invoiceDetail.invoice_info.invoice_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发票号码">{{ invoiceDetail.invoice_info.invoice_number || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额合计">{{ invoiceDetail.invoice_info.total_amount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="校验码">{{ invoiceDetail.invoice_info.check_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电子发票状态">{{ getEInvoiceStatusText(invoiceDetail.invoice_info.electronic_invoice_status) }}</el-descriptions-item>
          <el-descriptions-item label="快照版本">{{ invoiceDetail.invoice_info.snapshot_version || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">船舶信息(快照)</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="中文船名">{{ invoiceDetail?.vessel_info?.name_zh || '-' }}</el-descriptions-item>
          <el-descriptions-item label="英文船名">{{ invoiceDetail?.vessel_info?.name_en || '-' }}</el-descriptions-item>
          <el-descriptions-item label="呼号">{{ invoiceDetail?.vessel_info?.call_sign || '-' }}</el-descriptions-item>
          <el-descriptions-item label="IMO">{{ invoiceDetail?.vessel_info?.imo_number || '-' }}</el-descriptions-item>
          <el-descriptions-item label="船型">{{ invoiceDetail?.vessel_info?.vessel_type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="国籍">{{ invoiceDetail?.vessel_info?.nationality || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总吨">{{ invoiceDetail?.vessel_info?.gross_tonnage ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="净吨">{{ invoiceDetail?.vessel_info?.net_tonnage ?? '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">航次信息(快照)</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="航次号">{{ invoiceDetail?.voyage_info?.voyage_number || '-' }}</el-descriptions-item>
          <el-descriptions-item label="航次类型">{{ invoiceDetail?.voyage_info?.voyage_type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="ETA">{{ invoiceDetail?.voyage_info?.eta ? new Date(invoiceDetail.voyage_info.eta).toLocaleString() : '-' }}</el-descriptions-item>
          <el-descriptions-item label="ATA">{{ invoiceDetail?.voyage_info?.ata ? new Date(invoiceDetail.voyage_info.ata).toLocaleString() : '-' }}</el-descriptions-item>
          <el-descriptions-item label="上一港">{{ invoiceDetail?.voyage_info?.previous_port || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下一港">{{ invoiceDetail?.voyage_info?.next_port || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货名">{{ invoiceDetail?.voyage_info?.cargo_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货量">{{ invoiceDetail?.voyage_info?.cargo_quantity ?? '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">代理信息(快照)</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="代理编码">{{ invoiceDetail?.agent_info?.agent_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ invoiceDetail?.agent_info?.company_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="简称">{{ invoiceDetail?.agent_info?.short_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ invoiceDetail?.agent_info?.contact_person || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ invoiceDetail?.agent_info?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纳税人识别号">{{ invoiceDetail?.agent_info?.taxpayer_id || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">引航信息(快照)</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="签证单号">{{ invoiceDetail?.pilotage_info?.certificate_code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="引航类型">{{ invoiceDetail?.pilotage_info?.pilotage_type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="起点">{{ invoiceDetail?.pilotage_info?.start_location_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="终点">{{ invoiceDetail?.pilotage_info?.end_location_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ invoiceDetail?.pilotage_info?.pilotage_start_time ? new Date(invoiceDetail.pilotage_info.pilotage_start_time).toLocaleString() : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ invoiceDetail?.pilotage_info?.pilotage_end_time ? new Date(invoiceDetail.pilotage_info.pilotage_end_time).toLocaleString() : '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">购方信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ invoiceDetail?.buyer_info?.company_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纳税人识别号">{{ invoiceDetail?.buyer_info?.taxpayer_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ invoiceDetail?.buyer_info?.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ invoiceDetail?.buyer_info?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开户行">{{ invoiceDetail?.buyer_info?.bank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ invoiceDetail?.buyer_info?.bank_account || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">销方信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ invoiceDetail?.seller_info?.company_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纳税人识别号">{{ invoiceDetail?.seller_info?.taxpayer_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ invoiceDetail?.seller_info?.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ invoiceDetail?.seller_info?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开户行">{{ invoiceDetail?.seller_info?.bank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ invoiceDetail?.seller_info?.bank_account || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="invoiceDetail?.current_info?.warning"
          type="info"
          :closable="false"
          style="margin-top: 12px"
        >
          {{ invoiceDetail.current_info.warning }}
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.payment-view {
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

.billing-tabs {
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
  
  .el-button {
    margin-right: 8px;
  }
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>