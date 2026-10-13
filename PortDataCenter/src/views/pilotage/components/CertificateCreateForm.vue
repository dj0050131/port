<!-- src/views/certificate/components/CertificateCreateForm.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchPlanCertificateInfo,
  createCertificate,
  type PlanCertificateInfo,
  type CertificateCreateData
} from '@/api/certificate'
import { useDataCenterStore } from '@/stores/dataCenter'

const props = defineProps<{
  planId?: number
}>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<PlanCertificateInfo>('pilotage.certificateCreate', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const formData = reactive<CertificateCreateData>({
  planId: props.planId || 0,
  forwardDraft: 0,
  aftDraft: 0,
  totalPassengers: 0,
  totalCrew: 0,
  pilotageStartTime: '',
  pilotageEndTime: '',
  boardingTime: '',
  leavingTime: '',
  pilotSignTime: new Date().toISOString(),
  // 后端标志位使用 Y/N，这里给出默认值：否=N
  escortFlag: 'N',
  berthingAssistanceFlag: 'N',
  detentionFlag: 'N',
})

const planInfo = ref<PlanCertificateInfo | null>(null)

const fetchPlanInfo = async () => {
  if (!props.planId) return

  try {
    uiState.loading = true
    const response = await fetchPlanCertificateInfo(props.planId)
    if (response.code === 200 && response.data) {
      planInfo.value = response.data
      formData.planId = props.planId

      const now = new Date()
      const planStart = new Date(planInfo.value.plannedStartTime)
      const planEnd = new Date(planInfo.value.plannedEndTime)

      // 如果计划时间是今天，使用计划时间
      if (isSameDay(planStart, now)) {
        formData.pilotageStartTime = planStart.toISOString()
        formData.pilotageEndTime = planEnd.toISOString()
        formData.boardingTime = planStart.toISOString()
        formData.leavingTime = planEnd.toISOString()
      } else {
        // 否则使用当前时间
        formData.pilotageStartTime = now.toISOString()
        formData.pilotageEndTime = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString()
        formData.boardingTime = now.toISOString()
        formData.leavingTime = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString()
      }
    }
  } catch (error) {
    console.error('获取计划信息失败:', error)
    ElMessage.error('获取计划信息失败')
  } finally {
    uiState.loading = false
  }
}

const isSameDay = (date1: Date, date2: Date) => {
  return date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
}

const rules = {
  forwardDraft: [
    { required: true, message: '请输入前吃水', trigger: 'blur' },
    { type: 'number', min: 0, max: 30, message: '前吃水应在0-30米之间', trigger: 'blur' }
  ],
  aftDraft: [
    { required: true, message: '请输入后吃水', trigger: 'blur' },
    { type: 'number', min: 0, max: 30, message: '后吃水应在0-30米之间', trigger: 'blur' }
  ],
  totalPassengers: [
    { type: 'number', min: 0, max: 10000, message: '旅客数应在0-10000之间', trigger: 'blur' }
  ],
  totalCrew: [
    { type: 'number', min: 0, max: 1000, message: '船员数应在0-1000之间', trigger: 'blur' }
  ],
  pilotageStartTime: [
    { required: true, message: '请选择开始引航时间', trigger: 'change' }
  ],
  pilotageEndTime: [
    { required: true, message: '请选择结束引航时间', trigger: 'change' }
  ],
  boardingTime: [
    { required: true, message: '请选择登船时间', trigger: 'change' }
  ],
  leavingTime: [
    { required: true, message: '请选择离船时间', trigger: 'change' }
  ]
}

const submitForm = async () => {
  try {
    uiState.loading = true

    if (formData.pilotageStartTime && formData.pilotageEndTime) {
      const startTime = new Date(formData.pilotageStartTime)
      const endTime = new Date(formData.pilotageEndTime)
      if (endTime <= startTime) {
        ElMessage.error('结束时间必须晚于开始时间')
        return
      }
    }

    const response = await createCertificate(formData)
    if (response.code === 200 && response.data) {
      ElMessage.success('创建成功')
      emit('success')
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error: any) {
    console.error('创建失败:', error)
    ElMessage.error(error.message || '创建失败')
  } finally {
    uiState.loading = false
  }
}

const cancelForm = () => {
  emit('cancel')
}

onMounted(() => {
  if (props.planId) {
    fetchPlanInfo()
  }
})
</script>

<template>
  <div class="certificate-create-form">
    <el-form
        :model="formData"
        :rules="rules"
        label-width="120px"
        size="small"
        v-loading="uiState.loading"
    >
      <div v-if="planInfo" class="plan-info-section">
        <el-divider content-position="left">计划信息</el-divider>
        <div class="plan-info-grid">
          <div class="plan-info-item">
            <span class="label">船舶名称：</span>
            <span class="value">{{ planInfo.vesselNameZh }} / {{ planInfo.vesselNameEn }}</span>
          </div>
          <div class="plan-info-item">
            <span class="label">船舶呼号：</span>
            <span class="value">{{ planInfo.vesselCallSign }}</span>
          </div>
          <div class="plan-info-item">
            <span class="label">航次号：</span>
            <span class="value">{{ planInfo.voyageNumber }}</span>
          </div>
          <div class="plan-info-item">
            <span class="label">引航员：</span>
            <span class="value">{{ planInfo.assignedPilotName }}</span>
          </div>
          <div class="plan-info-item">
            <span class="label">起止位置：</span>
            <span class="value">{{ planInfo.startLocationName }} → {{ planInfo.endLocationName }}</span>
          </div>
          <div class="plan-info-item">
            <span class="label">计划时间：</span>
            <span class="value">{{ new Date(planInfo.plannedStartTime).toLocaleString() }} - {{ new Date(planInfo.plannedEndTime).toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <el-divider content-position="left">签证信息</el-divider>

      <div class="form-section">
        <h4>船舶状态</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="前吃水(m)" prop="forwardDraft">
              <el-input-number
                  v-model="formData.forwardDraft"
                  :min="0"
                  :max="30"
                  :step="0.1"
                  placeholder="请输入前吃水"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="后吃水(m)" prop="aftDraft">
              <el-input-number
                  v-model="formData.aftDraft"
                  :min="0"
                  :max="30"
                  :step="0.1"
                  placeholder="请输入后吃水"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="马力">
              <el-input-number
                  v-model="formData.horsepower"
                  :min="0"
                  placeholder="请输入马力"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="旅客总数" prop="totalPassengers">
              <el-input-number
                  v-model="formData.totalPassengers"
                  :min="0"
                  :max="10000"
                  placeholder="旅客总数"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="船员总数" prop="totalCrew">
              <el-input-number
                  v-model="formData.totalCrew"
                  :min="0"
                  :max="1000"
                  placeholder="船员总数"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="贸易类型">
              <el-select v-model="formData.tradeType" placeholder="请选择" style="width: 100%">
                <el-option label="进口" value="IMPORT" />
                <el-option label="出口" value="EXPORT" />
                <el-option label="中转" value="TRANSIT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <h4>时间信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始引航时间" prop="pilotageStartTime">
              <el-date-picker
                  v-model="formData.pilotageStartTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束引航时间" prop="pilotageEndTime">
              <el-date-picker
                  v-model="formData.pilotageEndTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="登船时间" prop="boardingTime">
              <el-date-picker
                  v-model="formData.boardingTime"
                  type="datetime"
                  placeholder="选择登船时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="离船时间" prop="leavingTime">
              <el-date-picker
                  v-model="formData.leavingTime"
                  type="datetime"
                  placeholder="选择离船时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="引航员签字时间">
          <el-date-picker
              v-model="formData.pilotSignTime"
              type="datetime"
              placeholder="选择签字时间"
              style="width: 100%"
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <h4>业务标志</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="护航标记">
              <el-select v-model="formData.escortFlag" placeholder="请选择" style="width: 100%">
                <el-option label="否" value="N" />
                <el-option label="是" value="Y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="助泊标记">
              <el-select v-model="formData.berthingAssistanceFlag" placeholder="请选择" style="width: 100%">
                <el-option label="否" value="N" />
                <el-option label="是" value="Y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="滞留标记">
              <el-select v-model="formData.detentionFlag" placeholder="请选择" style="width: 100%">
                <el-option label="否" value="N" />
                <el-option label="是" value="Y" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div v-if="formData.detentionFlag === 'Y'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="滞留小时数">
                <el-input-number
                    v-model="formData.detentionHours"
                    :min="0"
                    :step="0.5"
                    placeholder="滞留小时数"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="滞留人数">
                <el-input-number
                    v-model="formData.detentionPersons"
                    :min="0"
                    placeholder="滞留人数"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-if="formData.berthingAssistanceFlag === 'Y'">
          <el-form-item label="助泊费用">
            <el-input-number
                v-model="formData.berthingAssistanceCharge"
                :min="0"
                placeholder="助泊费用"
                style="width: 100%"
            />
          </el-form-item>
        </div>
      </div>

      <div class="form-section">
        <h4>其他信息</h4>
        <el-form-item label="交接单编号">
          <el-input v-model="formData.handoverSheetId" placeholder="请输入交接单编号" />
        </el-form-item>

        <el-form-item label="特殊情况说明">
          <el-input
              v-model="formData.specialSituationDescription"
              type="textarea"
              :rows="3"
              placeholder="请输入特殊情况说明"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
              v-model="formData.notes"
              type="textarea"
              :rows="2"
              placeholder="请输入备注信息"
          />
        </el-form-item>
      </div>

      <div class="form-actions">
        <el-button @click="cancelForm">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="uiState.loading">创建签证单</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.certificate-create-form {
  .plan-info-section {
    background: #fff;
    border: 1px solid #ebeef5;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 20px;

    .plan-info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 0;
      border: 1px solid #ebeef5;
      border-radius: 6px;
      overflow: hidden;
      background: #fff;

      .plan-info-item {
        display: flex;
        padding: 10px 12px;
        border: 1px solid #ebeef5;
        margin: -1px 0 0 -1px;

        .label {
          font-weight: 500;
          color: #606266;
          min-width: 80px;
        }

        .value {
          color: #303133;
        }
      }
    }
  }

  .form-section {
    margin-bottom: 24px;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 14px 16px 4px;

    h4 {
      margin: 0 0 16px 0;
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
}
</style>