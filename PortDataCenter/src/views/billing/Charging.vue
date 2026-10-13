<script setup lang="ts">
import {reactive, onMounted, ref} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Money, View } from '@element-plus/icons-vue'
import {
  startBilling,
  getBillingVoucherDetail,
  getBillingVoucherByCertificateId,
  fetchRejectedBillingVouchers,
  type BillingResultVO
} from '@/api/billingVouchers'
import {
  fetchCertificateList,
  type CertificateDetail
} from '@/api/certificate'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<CertificateDetail>('billing.charging', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const activeTab = ref('pending')
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref<BillingResultVO | null>(null)
const searchForm = reactive({
  page: tableState.page,
  size: tableState.pageSize,
  dateRange: [],
  pilot: '',
  shipName: '',
  startDate: '',
  endDate: ''
})

const fetchData = async () => {
  tableState.loading = true
  try {
    if (activeTab.value === 'returned') {
      const params = {
        current: searchForm.page,
        size: searchForm.size,
        params: {
          vesselNameZh: searchForm.shipName || undefined
        }
      }

      const response = await fetchRejectedBillingVouchers(params)

      if (response.code === 200) {
        tableState.items = response.data.records || []
        tableState.total = response.data.total || 0
      } else {
        ElMessage.error(response.message || '获取重计费签证单失败')
      }
    } else {
      if (searchForm.dateRange && searchForm.dateRange.length === 2) {
        searchForm.startDate = searchForm.dateRange[0]
        searchForm.endDate = searchForm.dateRange[1]
      } else {
        searchForm.startDate = ''
        searchForm.endDate = ''
      }

      const certificateResponse = await fetchCertificateList({
        page: searchForm.page,
        size: searchForm.size,
        startDate: searchForm.startDate || undefined,
        endDate: searchForm.endDate || undefined,
        vesselNameZh: searchForm.shipName || undefined,
        submitStatus: 'CONFIRMED', // 只获取已确认的签证单
        billingStatus: 'UNBILLED' // 待计费仅展示首次计费（不包含重计费）
      })

      if (certificateResponse.code === 200) {
        const certificates = certificateResponse.data.records || []

        tableState.items = certificates
        tableState.total = certificateResponse.data.total || 0
      } else {
        ElMessage.error(certificateResponse.message || '获取签证单列表失败')
      }
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleCalculate = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm(
      `确定要对签证单 ${row.certificateCode} 进行计费计算吗？`,
      '确认计费',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await startBilling(row.id)
    ElMessage.success('计费计算成功')
    await fetchData()
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('计费计算失败:', error)
    // 请求拦截器已统一提示错误，这里避免重复弹两次
  }
}

const handleRebilling = async (row: CertificateDetail) => {
  try {
    await ElMessageBox.confirm(
      `确定要对签证单 ${row.certificateCode} 重新进行计费计算吗？`,
      '确认重新计费',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await startBilling(row.id)
    ElMessage.success('重新计费计算成功')
    fetchData()
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('重新计费计算失败:', error)
    // 请求拦截器已统一提示错误，这里避免重复弹两次
  }
}

const handleViewDetail = async (row: CertificateDetail) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    let response

    // 优先使用最新的计费凭证ID（列表SQL已带出），没有则回退到 byCertificate 查询
    if (row.latestBillingVoucherId) {
      response = await getBillingVoucherDetail(row.latestBillingVoucherId)
    } else {
      response = await getBillingVoucherByCertificateId(row.id)
    }

    if (response) {
      if (response.code === 200) {
        currentDetail.value = response.data
      } else {
        ElMessage.error(response.message || '获取计费详情失败')
        detailDialogVisible.value = false
      }
    } else {
      ElMessage.warning('无法获取详情')
      detailDialogVisible.value = false
    }
  } catch (error) {
    console.error('获取计费详情失败:', error)
    ElMessage.error('获取计费详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const resetSearch = () => {
  searchForm.page = 1
  searchForm.dateRange = []
  searchForm.pilot = ''
  searchForm.shipName = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  fetchData()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  tableState.pageSize = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  searchForm.page = val
  tableState.page = val
  fetchData()
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
    case 'REBILLING': return 'orange'
    case 'BILLING_EXCEPTION': return 'danger'
    case 'PAID': return 'success'
    default: return 'info'
  }
}

const getBillingStatusText = (status: string) => {
  switch (status) {
    case 'UNBILLED': return '未计费'
    case 'BILLED': return '已计费'
    case 'REBILLING': return '重计费'
    case 'BILLING_EXCEPTION': return '计费异常'
    case 'PAID': return '已付款'
    default: return status
  }
}

const getAuditStatusTagType = (status: string) => {
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

const getPaymentStatusTagType = (status: string) => {
  switch (status) {
    case 'UNPAID': return 'warning'
    case 'PARTIAL_PAID': return 'primary'
    case 'PAID': return 'success'
    default: return 'info'
  }
}

const getPaymentStatusText = (status: string) => {
  switch (status) {
    case 'UNPAID': return '未支付'
    case 'PARTIAL_PAID': return '部分支付'
    case 'PAID': return '已支付'
    default: return status
  }
}

const handleTabChange = () => {
  searchForm.page = 1 // 切换标签页时重置为第一页
  fetchData()
}

const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentDetail.value = null
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="charging-container">
    <div class="charging-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">签证单计费</h2>
        <p class="page-desc">对已签署的签证单进行费用计算</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <el-tabs v-model="activeTab" class="billing-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="待计费签证单" name="pending">
           <div class="tab-toolbar">
             <el-form inline size="small">
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
                   placeholder="请输入"
                   clearable
                   @keyup.enter="fetchData"
                 />
               </el-form-item>
               <el-form-item>
                  <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                  <el-button @click="resetSearch">重置</el-button>
               </el-form-item>
             </el-form>
           </div>

          <el-table
            :data="tableState.items"
            v-loading="tableState.loading"
             stripe
             height="calc(100vh - 350px)"
             size="small"
             border
           >
              <el-table-column type="selection" width="40" />
              <el-table-column prop="certificateCode" label="签证单号" width="140" fixed="left" />
              <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
              <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
              <el-table-column prop="submitStatus" label="提交状态" width="100">
                 <template #default="{ row }">
                   <el-tag
                     size="small"
                     :type="getStatusTagType(row.submitStatus)"
                   >
                     {{ getStatusText(row.submitStatus) }}
                   </el-tag>
                 </template>
              </el-table-column>
              <el-table-column prop="billingStatus" label="计费状态" width="100">
                 <template #default="{ row }">
                   <el-tag
                     size="small"
                     :type="getBillingStatusTagType(row.billingStatus)"
                   >
                     {{ getBillingStatusText(row.billingStatus) }}
                   </el-tag>
                 </template>
              </el-table-column>
              <el-table-column prop="voyageNumber" label="航次" width="100" />
              <el-table-column prop="assignedPilotName" label="主引" width="80" />
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
              <el-table-column prop="netTonnage" label="净吨" width="90" align="right" />
              <el-table-column prop="startLocationFullName" label="起始位置" min-width="120" show-overflow-tooltip />
              <el-table-column prop="endLocationFullName" label="终止位置" min-width="120" show-overflow-tooltip />
              <el-table-column prop="inboundAgentName" label="进口代理" width="120" />
              <el-table-column prop="outboundAgentName" label="出口代理" width="120" />
              <el-table-column label="操作" width="150" fixed="right">
                 <template #default="{ row }">
                    <el-button
                      link
                      type="primary"
                      :icon="Money"
                      @click="handleCalculate(row)"
                    >
                      计费
                    </el-button>
                    <el-button
                      link
                      type="primary"
                      :icon="View"
                      @click="handleViewDetail(row)"
                      v-if="row.latestBillingVoucherId"
                    >
                      查看
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

        <el-tab-pane label="已退回签证单" name="returned">
          <div class="tab-toolbar">
             <el-form inline size="small">
               <el-form-item label="中文船名">
                 <el-input
                   v-model="searchForm.shipName"
                   placeholder="请输入"
                   clearable
                   @keyup.enter="fetchData"
                 />
               </el-form-item>
               <el-form-item>
                  <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                  <el-button @click="resetSearch">重置</el-button>
               </el-form-item>
             </el-form>
           </div>

          <el-table
            :data="tableState.items"
            v-loading="tableState.loading"
             stripe
             height="calc(100vh - 350px)"
             size="small"
             border
           >
              <el-table-column prop="certificateCode" label="签证单号" width="140" fixed="left" />
              <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
              <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
              <el-table-column prop="billingStatus" label="计费状态" width="100">
                <template #default="{ row }">
                  <el-tag
                    size="small"
                    :type="getBillingStatusTagType(row.billingStatus)"
                  >
                    {{ getBillingStatusText(row.billingStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="voyageNumber" label="航次" width="100" />
              <el-table-column prop="assignedPilotName" label="主引" width="80" />
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
              <el-table-column prop="netTonnage" label="净吨" width="90" align="right" />
              <el-table-column prop="startLocationFullName" label="起始位置" min-width="120" show-overflow-tooltip />
              <el-table-column prop="endLocationFullName" label="终止位置" min-width="120" show-overflow-tooltip />
              <el-table-column prop="inboundAgentName" label="进口代理" width="120" />
              <el-table-column prop="outboundAgentName" label="出口代理" width="120" />
              <el-table-column label="操作" width="150" fixed="right">
                 <template #default="{ row }">
                    <el-button
                      link
                      type="primary"
                      :icon="Money"
                      @click="handleRebilling(row)"
                    >
                      重新计费
                    </el-button>
                    <el-button
                      link
                      type="primary"
                      :icon="View"
                      @click="handleViewDetail(row)"
                      v-if="row.latestBillingVoucherId"
                    >
                      查看
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
  </div>

  <el-dialog
    v-model="detailDialogVisible"
    title="计费详情"
    width="1000px"
    :loading="detailLoading"
    @close="handleCloseDetail"
  >
    <div v-loading="detailLoading">
      <el-descriptions
        v-if="currentDetail"
        :column="3"
        border
        size="small"
      >
        <el-descriptions-item label="凭证编号" span="3">
          {{ currentDetail.voucherCode }}
        </el-descriptions-item>
        <el-descriptions-item label="船舶中文名">
          {{ currentDetail.vesselNameZh }}
        </el-descriptions-item>
        <el-descriptions-item label="船舶英文名">
          {{ currentDetail.vesselNameEn }}
        </el-descriptions-item>
        <el-descriptions-item label="代理">
          {{ currentDetail.agentName }}
        </el-descriptions-item>
        <el-descriptions-item label="基础费用">
          ¥{{ currentDetail.baseAmount?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="附加费用">
          ¥{{ currentDetail.surchargeAmount?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="折扣金额">
          ¥{{ currentDetail.discountAmount?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额" span="1">
          <span class="total-amount">¥{{ currentDetail.totalAmount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="计费方式">
          {{ currentDetail.billingMethod }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusTagType(currentDetail.auditStatus)">
            {{ getAuditStatusText(currentDetail.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getPaymentStatusTagType(currentDetail.paymentStatus)">
            {{ getPaymentStatusText(currentDetail.paymentStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="到期日">
          {{ currentDetail.dueDate ? new Date(currentDetail.dueDate).toLocaleDateString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" span="2">
          {{ currentDetail.createdAt ? new Date(currentDetail.createdAt).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="起始位置">
          {{ currentDetail.startLocationName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="终止位置">
          {{ currentDetail.endLocationName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="引航开始时间">
          {{ currentDetail.pilotageStartTime ? new Date(currentDetail.pilotageStartTime).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="引航结束时间">
          {{ currentDetail.pilotageEndTime ? new Date(currentDetail.pilotageEndTime).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="净吨">
          {{ currentDetail.netTonnage || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="总吨">
          {{ currentDetail.grossTonnage || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 费用明细 -->
      <div v-if="currentDetail && currentDetail.chargeDetails && currentDetail.chargeDetails.length > 0" class="charge-details">
        <h4>费用明细</h4>
        <el-table
          :data="currentDetail.chargeDetails"
          size="small"
          border
        >
          <el-table-column prop="chargeName" label="费用名称" width="150" />
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="amount" label="金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.amount?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="notes" label="备注" width="150" />
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleCloseDetail">关闭</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.charging-view {
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

.tab-toolbar {
  padding-bottom: 12px;

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

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.charge-details {
  margin-top: 20px;

  h4 {
    margin: 0 0 12px 0;
    font-weight: 600;
  }
}

.total-amount {
  font-weight: 600;
  font-size: 16px;
  color: var(--el-color-danger);
}
</style>