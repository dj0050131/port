<!-- src/views/certificate/components/CertificateEditForm.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchCertificateDetail,
  updateCertificate,
  type CertificateDetail,
  type CertificateUpdateData
} from '@/api/certificate'

import { formatDateTime } from '@/utils/date-utils'
import { useDataCenterStore } from '@/stores/dataCenter'
import { getPilotageRouteList } from '@/api/bases/pilotageRoutes'
import type { PilotageRoutes } from '@/types/bases/pilotageRoutes'

const props = defineProps<{
  certificateId: number
}>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<CertificateDetail>('pilotage.certificateEdit', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const formData = reactive<CertificateUpdateData>({})
const currentCert = ref<CertificateDetail | null>(null)

const routeLoading = ref(false)
const routeOptions = ref<PilotageRoutes[]>([])

const isAutoRoute = computed(() => {
  const code = (formData.routeCode || '').trim()
  return !code || code.toUpperCase() === 'AUTO'
})

const filteredRoutes = computed(() => {
  const cert = currentCert.value
  if (!cert) return routeOptions.value
  const sType = (cert.startLocationType || '').trim()
  const eType = (cert.endLocationType || '').trim()
  const sId = cert.startLocationId
  const eId = cert.endLocationId
  return routeOptions.value.filter((r) =>
    String(r.startLocationType || '').trim() === sType &&
    String(r.endLocationType || '').trim() === eType &&
    Number(r.startLocationId) === Number(sId) &&
    Number(r.endLocationId) === Number(eId)
  )
})

const routeSelectOptions = computed(() => {
  const list = filteredRoutes.value || []
  const code = String(formData.routeCode || '').trim()
  const name = String(formData.routeName || '').trim()
  if (!code && !name) return list

  const exists = list.some((r) => String(r.routeCode || '').trim().toUpperCase() === code.toUpperCase())
  if (exists) return list

  // 兜底：如果当前签证单的航线在基础数据中找不到，也要能“看到原来的选项”
  const pseudo = {
    id: -1,
    routeCode: code || '（未填写）',
    routeName: name || '（未填写）',
  } as any
  return [pseudo, ...list]
})

const normalizeYn = (v: any, defaultValue: 'Y' | 'N' = 'N') => {
  if (v === 'Y' || v === 'N') return v
  if (v === 'YES') return 'Y'
  if (v === 'NO') return 'N'
  if (v == null || v === '') return defaultValue
  return defaultValue
}

const loadRoutes = async () => {
  try {
    routeLoading.value = true
    const resp: any = await getPilotageRouteList()
    if (resp?.code === 200) {
      routeOptions.value = resp.data || []
    } else {
      routeOptions.value = []
    }
  } catch (e) {
    routeOptions.value = []
  } finally {
    routeLoading.value = false
  }
}

const handleRouteChange = (routeId?: number) => {
  if (!routeId) return
  if (Number(routeId) <= 0) return
  const r = routeOptions.value.find((x) => Number(x.id) === Number(routeId))
  if (!r) return
  formData.routeCode = r.routeCode
  formData.routeName = r.routeName
}

const autoSelectRouteId = () => {
  const cert = currentCert.value
  if (!cert) return
  if (!routeOptions.value || routeOptions.value.length === 0) return
  if (formData.routeId && Number(formData.routeId) > 0) return

  const code = String(cert.routeCode || '').trim()
  const name = String(cert.routeName || '').trim()

  if (code && code.toUpperCase() !== 'AUTO') {
    const hit =
      (filteredRoutes.value || []).find((r) => String(r.routeCode || '').trim().toUpperCase() === code.toUpperCase()) ||
      routeOptions.value.find((r) => String(r.routeCode || '').trim().toUpperCase() === code.toUpperCase())
    if (hit && hit.id != null) {
      formData.routeId = Number(hit.id) as any
      handleRouteChange(Number(hit.id))
      return
    }
  }

  if (name) {
    const hit = (filteredRoutes.value || []).find((r) => String(r.routeName || '').trim() === name)
    if (hit && hit.id != null) {
      formData.routeId = Number(hit.id) as any
      handleRouteChange(Number(hit.id))
      return
    }
  }

  const candidates = filteredRoutes.value || []
  if (candidates.length === 1 && candidates[0]?.id != null) {
    formData.routeId = Number(candidates[0].id) as any
    handleRouteChange(Number(candidates[0].id))
  }
}

const fetchCertificateData = async () => {
  try {
    uiState.loading = true
    const response = await fetchCertificateDetail(props.certificateId)
    if (response.code === 200 && response.data) {
      const cert = response.data
      currentCert.value = cert

      // 填充表单数据
      Object.assign(formData, {
        routeId: undefined,
        routeCode: cert.routeCode,
        routeName: cert.routeName,
        forwardDraft: cert.forwardDraft,
        aftDraft: cert.aftDraft,
        totalPassengers: cert.totalPassengers,
        totalCrew: cert.totalCrew,
        pilotageStartTime: cert.pilotageStartTime,
        pilotageEndTime: cert.pilotageEndTime,
        boardingTime: cert.boardingTime,
        leavingTime: cert.leavingTime,
        pilotSignTime: cert.pilotSignTime,
        agentConfirmTime: cert.agentConfirmTime,
        tradeType: cert.tradeType,
        horsepower: cert.horsepower,
        escortFlag: normalizeYn(cert.escortFlag, 'N'),
        berthingAssistanceFlag: normalizeYn(cert.berthingAssistanceFlag, 'N'),
        berthingAssistanceCharge: cert.berthingAssistanceCharge,
        detentionFlag: normalizeYn(cert.detentionFlag, 'N'),
        detentionHours: cert.detentionHours,
        detentionPersons: cert.detentionPersons,
        withdrawalFlag: normalizeYn(cert.withdrawalFlag, 'N'),
        handoverSheetId: cert.handoverSheetId,
        specialSituationDescription: cert.specialSituationDescription,
        notes: cert.notes
      })
    }
  } catch (error) {
    console.error('获取签证单详情失败:', error)
    ElMessage.error('获取签证单详情失败')
  } finally {
    uiState.loading = false
  }
}

const rules = {
  forwardDraft: [
    { type: 'number', min: 0, max: 30, message: '前吃水应在0-30米之间', trigger: 'blur' }
  ],
  aftDraft: [
    { type: 'number', min: 0, max: 30, message: '后吃水应在0-30米之间', trigger: 'blur' }
  ],
  totalPassengers: [
    { type: 'number', min: 0, max: 10000, message: '旅客数应在0-10000之间', trigger: 'blur' }
  ],
  totalCrew: [
    { type: 'number', min: 0, max: 1000, message: '船员数应在0-1000之间', trigger: 'blur' }
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

    const formattedData = {
      ...formData,
      pilotageStartTime: formData.pilotageStartTime ? formatDateTime(new Date(formData.pilotageStartTime)) : undefined,
      pilotageEndTime: formData.pilotageEndTime ? formatDateTime(new Date(formData.pilotageEndTime)) : undefined,
      boardingTime: formData.boardingTime ? formatDateTime(new Date(formData.boardingTime)) : undefined,
      leavingTime: formData.leavingTime ? formatDateTime(new Date(formData.leavingTime)) : undefined,
      pilotSignTime: formData.pilotSignTime ? formatDateTime(new Date(formData.pilotSignTime)) : undefined,
      agentConfirmTime: formData.agentConfirmTime ? formatDateTime(new Date(formData.agentConfirmTime)) : undefined,
    }

    const response = await updateCertificate(props.certificateId, formattedData)
    if (response.code === 200 && response.data) {
      ElMessage.success('更新成功')
      emit('success')
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error: any) {
    console.error('更新失败:', error)
    ElMessage.error(error.message || '更新失败')
  } finally {
    uiState.loading = false
  }
}

const cancelForm = () => {
  emit('cancel')
}

onMounted(() => {
  loadRoutes()
  fetchCertificateData()
})

watch([currentCert, routeOptions], () => {
  autoSelectRouteId()
}, { immediate: true })
</script>

<template>
  <div class="certificate-edit-form">
    <el-form
        :model="formData"
        :rules="rules"
        label-width="120px"
        size="small"
        v-loading="uiState.loading"
    >
      <!-- 航线区间（关键字段：不补齐将无法提交/计费） -->
      <div class="form-section">
        <h4>航线区间</h4>
        <el-alert
            v-if="isAutoRoute"
            type="warning"
            :closable="false"
            show-icon
            title="当前签证单航线区间信息未补齐（可能为系统 AUTO 回退）。请先补齐航线区间，否则将无法提交/确认/计费。"
            style="margin-bottom: 12px;"
        />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择航线区间">
              <el-select
                  v-model="formData.routeId"
                  placeholder="请选择（按起点/终点过滤）"
                  filterable
                  clearable
                  :loading="routeLoading"
                  style="width: 100%"
                  @change="handleRouteChange"
              >
                <el-option
                    v-for="r in routeSelectOptions"
                    :key="r.id"
                    :label="`${Number(r.id) <= 0 ? '（当前）' : ''}${r.routeCode} - ${r.routeName}`"
                    :value="r.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="航线代码">
              <el-input v-model="formData.routeCode" placeholder="如 RTxxxx / 手工补录" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="航线名称">
              <el-input v-model="formData.routeName" placeholder="如 A→B / 手工补录" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

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
            <el-form-item label="开始引航时间">
              <el-date-picker
                  v-model="formData.pilotageStartTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束引航时间">
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
            <el-form-item label="登船时间">
              <el-date-picker
                  v-model="formData.boardingTime"
                  type="datetime"
                  placeholder="选择登船时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="离船时间">
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

        <el-form-item label="代理确认时间">
          <el-date-picker
              v-model="formData.agentConfirmTime"
              type="datetime"
              placeholder="选择确认时间"
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

        <el-form-item label="撤回标记">
          <el-select v-model="formData.withdrawalFlag" placeholder="请选择" style="width: 100%">
            <el-option label="否" value="N" />
            <el-option label="是" value="Y" />
          </el-select>
        </el-form-item>
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
        <el-button type="primary" @click="submitForm" :loading="uiState.loading">保存修改</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.certificate-edit-form {
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