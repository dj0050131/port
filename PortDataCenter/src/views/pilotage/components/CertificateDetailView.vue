<!-- src/views/certificate/components/CertificateDetailView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCertificateDetail, type CertificateDetail } from '@/api/certificate'
import { useDataCenterStore } from '@/stores/dataCenter'

const props = defineProps<{
  certificateId: number
}>()

const emit = defineEmits<{
  close: []
  edit: [certificateId: number]
}>()

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<CertificateDetail>('pilotage.certificateDetail', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const certificate = ref<CertificateDetail | null>(null)
const isAutoRoute = (c: CertificateDetail | null) => {
  if (!c) return false
  const code = String((c as any).routeCode || '').trim()
  const name = String((c as any).routeName || '').trim()
  return !code || !name || code.toUpperCase() === 'AUTO'
}

const getStatusText = (status: any) => {
  if (!status) return '-'
  switch (status) {
    case 'UNSUBMITTED': return '未提交'
    case 'SUBMITTED': return '已提交'
    case 'CONFIRMED': return '已确认'
    default: return status
  }
}

const getBillingStatusText = (status: any) => {
  if (!status) return '-'
  switch (status) {
    case 'UNBILLED': return '未计费'
    case 'BILLED': return '已计费'
    case 'BILLING_EXCEPTION': return '计费异常'
    case 'REBILLING': return '重计费中'
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
    case 'INBOUND': return '进港引航'
    case 'OUTBOUND': return '出港引航'
    case 'SHIFTING': return '移泊引航'
    default: return type || '-'
  }
}

const getYnText = (v: any) => {
  if (v === 'Y' || v === 'YES') return '是'
  if (v === 'N' || v === 'NO') return '否'
  return '否'
}

const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString()
}

const fetchCertificateDetailData = async () => {
  try {
    uiState.loading = true
    const response = await fetchCertificateDetail(props.certificateId)
    const payload = (response as any)?.data?.data ?? (response as any)?.data
    if ((response as any)?.code === 200 && payload) {
      certificate.value = payload as CertificateDetail
    } else {
      ElMessage.error('获取签证单详情失败')
    }
  } catch (error) {
    console.error('获取签证单详情失败:', error)
    ElMessage.error('获取签证单详情失败')
  } finally {
    uiState.loading = false
  }
}

const handleEdit = () => {
  emit('edit', props.certificateId)
}

const handleClose = () => {
  emit('close')
}

onMounted(() => {
  fetchCertificateDetailData()
})
</script>

<template>
  <div class="certificate-detail-view" v-loading="uiState.loading">
    <div v-if="certificate" class="detail-container">
      <div class="section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">签证单号：</span>
            <span class="value">{{ certificate.certificateCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交状态：</span>
            <el-tag size="small">{{ getStatusText(certificate.submitStatus) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">计费状态：</span>
            <el-tag size="small">{{ getBillingStatusText(certificate.billingStatus) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">引航类型：</span>
            <span class="value">{{ getPilotageTypeText(certificate.pilotageType) }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">船舶信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">中文船名：</span>
            <span class="value">{{ certificate.vesselNameZh }}</span>
          </div>
          <div class="info-item">
            <span class="label">英文船名：</span>
            <span class="value">{{ certificate.vesselNameEn }}</span>
          </div>
          <div class="info-item">
            <span class="label">船舶呼号：</span>
            <span class="value">{{ certificate.vesselCallSign }}</span>
          </div>
          <div class="info-item">
            <span class="label">IMO编号：</span>
            <span class="value">{{ certificate.imoNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">船舶类型：</span>
            <span class="value">{{ certificate.vesselType }}</span>
          </div>
          <div class="info-item">
            <span class="label">国籍：</span>
            <span class="value">{{ certificate.nationality }}</span>
          </div>
          <div class="info-item">
            <span class="label">总吨：</span>
            <span class="value">{{ certificate.grossTonnage?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">净吨：</span>
            <span class="value">{{ certificate.netTonnage?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">载重吨：</span>
            <span class="value">{{ certificate.deadweight?.toFixed(2) || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">航次信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">航次号：</span>
            <span class="value">{{ certificate.voyageNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">航次类型：</span>
            <span class="value">{{ getTradeTypeText(certificate.voyageType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">货物名称：</span>
            <span class="value">{{ certificate.cargoName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">货物数量：</span>
            <span class="value">{{ certificate.cargoQuantity?.toFixed(2) || '-' }} {{ certificate.cargoUnit || '' }}</span>
          </div>
          <div class="info-item">
            <span class="label">前吃水(m)：</span>
            <span class="value">{{ certificate.draftForward?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">后吃水(m)：</span>
            <span class="value">{{ certificate.draftAft?.toFixed(2) || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 区间/位置 -->
      <div class="section">
        <h3 class="section-title">区间/位置</h3>
        <el-alert
            v-if="isAutoRoute(certificate)"
            type="warning"
            :closable="false"
            show-icon
            title="航线区间信息未补齐（可能为系统 AUTO 回退）。在补齐航线区间前，将无法提交/确认/计费。"
            style="margin-bottom: 12px;"
        />
        <div class="info-grid">
          <div class="info-item">
            <span class="label">起始位置：</span>
            <span class="value">{{ certificate.startLocationFullName || certificate.startLocationName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">结束位置：</span>
            <span class="value">{{ certificate.endLocationFullName || certificate.endLocationName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">航线代码：</span>
            <span class="value">{{ certificate.routeCode || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">航线名称：</span>
            <span class="value">{{ certificate.routeName || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">引航信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">主引：</span>
            <span class="value">{{ certificate.assignedPilotName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">贸易类型：</span>
            <span class="value">{{ certificate.tradeType || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">前吃水(m)：</span>
            <span class="value">{{ certificate.forwardDraft?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">后吃水(m)：</span>
            <span class="value">{{ certificate.aftDraft?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">马力：</span>
            <span class="value">{{ certificate.horsepower?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">旅客总数：</span>
            <span class="value">{{ certificate.totalPassengers || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="label">船员总数：</span>
            <span class="value">{{ certificate.totalCrew || 0 }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">时间信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">开始引航时间：</span>
            <span class="value">{{ formatDateTime(certificate.pilotageStartTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">结束引航时间：</span>
            <span class="value">{{ formatDateTime(certificate.pilotageEndTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">登船时间：</span>
            <span class="value">{{ formatDateTime(certificate.boardingTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">离船时间：</span>
            <span class="value">{{ formatDateTime(certificate.leavingTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">引航员签字时间：</span>
            <span class="value">{{ formatDateTime(certificate.pilotSignTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">代理确认时间：</span>
            <span class="value">{{ formatDateTime(certificate.agentConfirmTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatDateTime(certificate.submittedAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">确认时间：</span>
            <span class="value">{{ formatDateTime(certificate.confirmedAt) }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">业务标志</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">护航标记：</span>
            <span class="value">{{ getYnText(certificate.escortFlag) }}</span>
          </div>
          <div class="info-item">
            <span class="label">助泊标记：</span>
            <span class="value">{{ getYnText(certificate.berthingAssistanceFlag) }}</span>
          </div>
          <div v-if="certificate.berthingAssistanceFlag === 'Y' || certificate.berthingAssistanceFlag === 'YES'" class="info-item">
            <span class="label">助泊费用：</span>
            <span class="value">{{ certificate.berthingAssistanceCharge?.toFixed(2) || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">滞留标记：</span>
            <span class="value">{{ getYnText(certificate.detentionFlag) }}</span>
          </div>
          <div v-if="certificate.detentionFlag === 'Y' || certificate.detentionFlag === 'YES'" class="info-item">
            <span class="label">滞留小时数：</span>
            <span class="value">{{ certificate.detentionHours?.toFixed(2) || '-' }}</span>
          </div>
          <div v-if="certificate.detentionFlag === 'Y' || certificate.detentionFlag === 'YES'" class="info-item">
            <span class="label">滞留人数：</span>
            <span class="value">{{ certificate.detentionPersons || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">撤回标记：</span>
            <span class="value">{{ getYnText((certificate as any).withdrawalFlag) }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">其他信息</h3>
        <div class="info-grid">
          <div v-if="certificate.inboundAgentName" class="info-item">
            <span class="label">进口代理：</span>
            <span class="value">{{ certificate.inboundAgentName }}</span>
          </div>
          <div v-if="certificate.outboundAgentName" class="info-item">
            <span class="label">出口代理：</span>
            <span class="value">{{ certificate.outboundAgentName }}</span>
          </div>
          <div v-if="certificate.inboundVoyage" class="info-item">
            <span class="label">进口航次：</span>
            <span class="value">{{ certificate.inboundVoyage }}</span>
          </div>
          <div v-if="certificate.outboundVoyage" class="info-item">
            <span class="label">出口航次：</span>
            <span class="value">{{ certificate.outboundVoyage }}</span>
          </div>
          <div v-if="certificate.handoverSheetId" class="info-item">
            <span class="label">交接单编号：</span>
            <span class="value">{{ certificate.handoverSheetId }}</span>
          </div>
          <div v-if="certificate.enteredBy" class="info-item">
            <span class="label">录入人：</span>
            <span class="value">{{ certificate.enteredBy }}</span>
          </div>
        </div>

        <div v-if="certificate.specialSituationDescription" class="info-text">
          <span class="label">特殊情况说明：</span>
          <div class="value">{{ certificate.specialSituationDescription }}</div>
        </div>

        <div v-if="certificate.notes" class="info-text">
          <span class="label">备注：</span>
          <div class="value">{{ certificate.notes }}</div>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
            v-if="certificate.submitStatus === 'UNSUBMITTED'"
            type="primary"
            @click="handleEdit"
        >
          编辑
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.certificate-detail-view {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;

  .detail-container {
    .section {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px 0;
        padding-left: 8px;
        border-left: 4px solid #409eff;
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 0;
        border: 1px solid #ebeef5;
        border-radius: 6px;
        overflow: hidden;
        background: #fff;

        .info-item {
          display: flex;
          align-items: flex-start;
          min-height: 32px;
          padding: 10px 12px;
          border: 1px solid #ebeef5;
          /* 用负 margin 合并“表格线”，避免双边框 */
          margin: -1px 0 0 -1px;

          .label {
            font-weight: 500;
            color: #606266;
            min-width: 100px;
            white-space: nowrap;
          }

          .value {
            color: #303133;
            flex: 1;
            word-break: break-word;
          }
        }
      }

      .info-text {
        margin-top: 12px;

        .label {
          font-weight: 500;
          color: #606266;
          margin-bottom: 8px;
          display: block;
        }

        .value {
          color: #303133;
          background: #f5f7fa;
          padding: 12px;
          border-radius: 4px;
          line-height: 1.5;
        }
      }
    }

    .action-buttons {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding-top: 20px;
      margin-top: 20px;
      border-top: 1px solid #ebeef5;
    }
  }
}
</style>