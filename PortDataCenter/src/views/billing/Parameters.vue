<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Setting, Refresh, Plus, Delete, CirclePlus } from '@element-plus/icons-vue'
import { 
  getActiveStandardRates, 
  getStandardRateList,
  getStandardRateWithRulesById,
  createStandardRateWithRules,
  updateStandardRateWithRules,
  getAllRateRuleTypes,
  deleteStandardRate
} from '@/api/bases/standardRates'
import { getChargeCategoryList } from '@/api/bases/chargeCategories'
import { getPilotageRouteList } from '@/api/bases/pilotageRoutes'
import type { 
  StandardRates, 
  StandardRatesWithRulesDTO,
  RateRuleTypes,
  RateRules
} from '@/types/bases/standardRates'
import type { ChargeCategory } from '@/types/bases/chargeCategories'
import type { PilotageRoutes } from '@/types/bases/pilotageRoutes'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('billing.parameters', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const formRef = ref<FormInstance>()
const activeRates = ref<StandardRates[]>([])
const allRates = ref<StandardRates[]>([])
const showAllRates = ref(false)
const rateRuleTypes = ref<RateRuleTypes[]>([])
const chargeCategories = ref<ChargeCategory[]>([])
const categoryMap = ref<Record<number, ChargeCategory>>({})
const pilotageRoutes = ref<PilotageRoutes[]>([])
const routeMap = ref<Record<number, PilotageRoutes>>({})

const selectedRateId = ref<number | null>(null)
const selectedRate = ref<StandardRates | null>(null)
const selectedRateWithRules = ref<StandardRatesWithRulesDTO | null>(null)
const isCreating = ref(false)

const formData = reactive({
  standardRate: {
    id: 0,
    rateCode: '',
    categoryId: 0,
    typeId: '',
    effectiveDate: '',
    expiryDate: '',
    routeId: 0,
    nauticalMiles: 0,
    notes: '',
    applicableVesselMinNetTon: 0,
    applicableVesselMaxNetTon: 0,
    rateStatus: '',
    currencyUnit: '',
    createdBy: 0,
    updatedBy: 0,
    createdAt: '',
    updatedAt: '',
    ruleCount: 0,
    conditionCount: 0
  } as StandardRates,
  rateRules: [] as RateRules[],
  ruleTypeMap: {} as Record<number, RateRuleTypes>
})

const handleSave = async () => {
  try {
    if (!formData.standardRate.rateCode || String(formData.standardRate.rateCode).trim() === '') {
      ElMessage.warning('请填写费率编号')
      return
    }
    if (!formData.standardRate.effectiveDate) {
      ElMessage.warning('请选择生效日期')
      return
    }
    if (!formData.standardRate.routeId || Number(formData.standardRate.routeId) <= 0) {
      ElMessage.warning('请选择引航区间')
      return
    }

    uiState.loading = true
    
    const submitData: StandardRatesWithRulesDTO = {
      standardRate: formData.standardRate,
      rateRules: formData.rateRules,
      ruleTypeMap: formData.ruleTypeMap
    }
    
    if (formData.standardRate.id > 0) {
      await updateStandardRateWithRules(formData.standardRate.id, submitData)
      ElMessage.success('费率信息更新成功')
    } else {
      await createStandardRateWithRules(submitData)
      ElMessage.success('费率信息创建成功')
    }
    
    await fetchParameters()
  } catch (error) {
    ElMessage.error('保存失败: ' + (error as Error).message)
  } finally {
    uiState.loading = false
  }
}

const handleCreateNewRate = () => {
  isCreating.value = true
  selectedRate.value = null
  selectedRateWithRules.value = null
  selectedRateId.value = null
  formData.standardRate = {
    id: 0,
    rateCode: '',
    categoryId: 0,
    typeId: '',
    effectiveDate: '',
    expiryDate: '',
    routeId: 0,
    nauticalMiles: 0,
    notes: '',
    applicableVesselMinNetTon: 0,
    applicableVesselMaxNetTon: 0,
    // DB枚举：VALID / INVALID
    rateStatus: 'VALID',
    currencyUnit: 'CNY',
    createdBy: 0,
    updatedBy: 0,
    createdAt: '',
    updatedAt: '',
    ruleCount: 0,
    conditionCount: 0
  } as any
  formData.rateRules = []
  formData.ruleTypeMap = {}
}

const handleDeleteRate = async () => {
  if (!formData.standardRate.id || formData.standardRate.id <= 0) {
    ElMessage.warning('请先选择要删除的标准费率')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除当前标准费率吗？（将同时删除其费率规则）', '确认删除', { type: 'warning' })
    uiState.loading = true
    await deleteStandardRate(formData.standardRate.id)
    ElMessage.success('删除成功')
    await fetchParameters()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败: ' + (e?.message || ''))
    }
  } finally {
    uiState.loading = false
  }
}

const handleReset = async () => {
  try {
    uiState.loading = true
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('已重置')
  } catch (error) {
    ElMessage.error('重置失败')
  } finally {
    uiState.loading = false
  }
}

const addRateRule = () => {
  formData.rateRules.push({
    id: 0,
    standardRateId: formData.standardRate.id,
    ruleTypeId: 0,
    rateValue: 0,
    applicableVesselMinNetTon: 0,
    applicableVesselMaxNetTon: 0,
    notes: '',
    isActive: 1,
    version: 1,
    createdAt: '',
    updatedAt: ''
  })
}

const removeRateRule = (index: number) => {
  ElMessageBox.confirm('确定要删除这条费率规则吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formData.rateRules.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const mapRatesToForm = async (rates: StandardRates[]) => {
  if (rates.length > 0) {
    selectedRate.value = rates[0]
    if (rates[0]) {
      selectedRateId.value = rates[0].id
      isCreating.value = false
      
      const res = await getStandardRateWithRulesById(rates[0].id)
      if (res.code === 200) {
        selectedRateWithRules.value = res.data
        formData.standardRate = { ...res.data.standardRate }
        formData.rateRules = [...res.data.rateRules]
        formData.ruleTypeMap = { ...res.data.ruleTypeMap }
      }
    }
  }
}

const handleRateSelection = async (rateId: number | null) => {
  if (!rateId) {
    selectedRate.value = null
    selectedRateWithRules.value = null
    return
  }
  const source = showAllRates.value ? allRates.value : activeRates.value
  const rate = source.find(r => r.id === rateId) || null
  selectedRate.value = rate
  
  if (rate) {
    isCreating.value = false
    const res = await getStandardRateWithRulesById(rate.id)
    if (res.code === 200) {
      selectedRateWithRules.value = res.data
      formData.standardRate = { ...res.data.standardRate }
      formData.rateRules = [...res.data.rateRules]
      formData.ruleTypeMap = { ...res.data.ruleTypeMap }
    }
  }
}

const fetchParameters = async () => {
  uiState.loading = true
  try {
    const res = await getActiveStandardRates()
    if (res.code === 200) {
      activeRates.value = res.data
      await mapRatesToForm(res.data)
    }

    const allRes = await getStandardRateList()
    if (allRes.code === 200) {
      allRates.value = allRes.data || []
    }
    
    const ruleTypesRes = await getAllRateRuleTypes()
    if (ruleTypesRes.code === 200) {
      rateRuleTypes.value = ruleTypesRes.data
    }

    const catRes = await getChargeCategoryList()
    if (catRes.code === 200) {
      chargeCategories.value = catRes.data || []
      categoryMap.value = (catRes.data || []).reduce((acc: any, c: any) => {
        acc[c.id] = c
        return acc
      }, {})
    }

    const routeRes = await getPilotageRouteList()
    if (routeRes.code === 200) {
      pilotageRoutes.value = routeRes.data || []
      routeMap.value = (routeRes.data || []).reduce((acc: any, r: any) => {
        acc[r.id] = r
        return acc
      }, {})
    }
  } catch (error) {
    ElMessage.error('获取费率参数失败: ' + (error as Error).message)
  } finally {
    uiState.loading = false
  }
}

onMounted(() => {
  fetchParameters()
})
</script>

<template>
  <div class="billing-parameters page-container" v-loading="uiState.loading">
    <div class="page-header">
      <div>
        <h2 class="page-title">费收参数设置</h2>
        <p class="page-desc">配置引航费用计算的基本参数和费率标准</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="handleReset">恢复默认</el-button>
        <el-button type="primary" :icon="Setting" @click="handleSave" :loading="uiState.loading">
          保存配置
        </el-button>
      </div>
    </div>

    <div class="parameters-container">
      <div class="param-section glass-card">
        <div class="section-header">
          <h3 class="section-title">标准费率选择</h3>
          <p class="section-desc">选择要编辑的标准费率记录</p>
        </div>
        <div style="display:flex; gap:12px; align-items:center; margin-bottom: 12px;">
          <el-switch v-model="showAllRates" active-text="显示全部" inactive-text="仅显示生效" />
          <el-button type="primary" :icon="CirclePlus" @click="handleCreateNewRate">新增标准费率</el-button>
          <el-button type="danger" :icon="Delete" @click="handleDeleteRate" :disabled="!formData.standardRate.id || formData.standardRate.id <= 0">删除当前费率</el-button>
        </div>
        <el-select 
          v-model="selectedRateId" 
          placeholder="请选择费率记录" 
          @change="handleRateSelection"
          clearable
          filterable
          style="width: 100%; margin-bottom: 20px;"
        >
          <el-option
            v-for="rate in (showAllRates ? allRates : activeRates)"
            :key="rate.id"
            :label="`${rate.rateCode} - ${(categoryMap[rate.categoryId]?.categoryName || '未分类')} - ${(routeMap[rate.routeId]?.routeName || ('区间ID:' + rate.routeId))} - ${rate.notes}`"
            :value="rate.id"
          />
        </el-select>
      </div>

      <div class="param-section glass-card" v-if="selectedRate || isCreating">
        <div class="section-header">
          <h3 class="section-title">标准费率基本信息</h3>
          <p class="section-desc">配置标准费率的基本信息</p>
        </div>
        <el-form :model="formData.standardRate" label-width="150px" class="param-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="费率编号">
                <el-input v-model="formData.standardRate.rateCode" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="费率分类名称">
                <el-select v-model="formData.standardRate.categoryId" style="width: 100%" placeholder="请选择分类">
                  <el-option
                    v-for="c in chargeCategories"
                    :key="c.id"
                    :label="c.categoryName"
                    :value="c.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生效日期">
                <el-date-picker 
                  v-model="formData.standardRate.effectiveDate" 
                  type="date" 
                  value-format="YYYY-MM-DD" 
                  style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="失效日期">
                <el-date-picker 
                  v-model="formData.standardRate.expiryDate" 
                  type="date" 
                  value-format="YYYY-MM-DD" 
                  style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="适用最小净吨">
                <el-input-number 
                  v-model="formData.standardRate.applicableVesselMinNetTon" 
                  :min="0" 
                  style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="适用最大净吨">
                <el-input-number 
                  v-model="formData.standardRate.applicableVesselMaxNetTon" 
                  :min="0" 
                  style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input 
                  v-model="formData.standardRate.notes" 
                  type="textarea" 
                  :rows="2" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="引航区间">
                <el-select v-model="formData.standardRate.routeId" style="width: 100%" placeholder="请选择引航区间">
                  <el-option v-for="r in pilotageRoutes" :key="r.id" :label="`${r.routeName}（${r.routeCode}）`" :value="r.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="引航海里数">
                <el-input-number v-model="formData.standardRate.nauticalMiles" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 费率规则配置 -->
      <div class="param-section glass-card" v-if="selectedRate || isCreating">
        <div class="section-header">
          <h3 class="section-title">费率规则配置</h3>
          <p class="section-desc">配置具体的费率规则</p>
        </div>
        
        <div class="rules-table">
          <el-button 
            type="primary" 
            :icon="Plus" 
            @click="addRateRule"
            style="margin-bottom: 15px;">
            添加费率规则
          </el-button>
          
          <el-table :data="formData.rateRules" border>
            <el-table-column prop="ruleTypeId" label="规则类型" width="auto">
              <template #default="{ row }">
                <el-select v-model="row.ruleTypeId" placeholder="请选择规则类型">
                  <el-option
                    v-for="ruleType in rateRuleTypes"
                    :key="ruleType.id"
                    :label="ruleType.ruleTypeName"
                    :value="ruleType.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            
            <el-table-column prop="rateValue" label="费率值" width="auto">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.rateValue" 
                  :precision="4" 
                  :step="0.0001" 
                  style="width: 100%" />
              </template>
            </el-table-column>
            
            <el-table-column prop="applicableVesselMinNetTon" label="最小净吨" width="auto">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.applicableVesselMinNetTon" 
                  :min="0" 
                  style="width: 100%" />
              </template>
            </el-table-column>
            
            <el-table-column prop="applicableVesselMaxNetTon" label="最大净吨" width="auto">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.applicableVesselMaxNetTon" 
                  :min="0" 
                  style="width: 100%" />
              </template>
            </el-table-column>
            
            <el-table-column prop="notes" label="备注">
              <template #default="{ row }">
                <el-input v-model="row.notes" />
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button 
                  type="danger" 
                  :icon="Delete" 
                  circle 
                  @click="removeRateRule($index)" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.billing-parameters {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--tidal-text-main);
}

.page-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--tidal-text-secondary);
}

.parameters-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.param-section {
  padding: 24px;

  .section-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--tidal-border);

    .section-title {
      margin: 0 0 8px;
      font-size: 18px;
      font-weight: 600;
      color: var(--tidal-primary);
    }

    .section-desc {
      margin: 0;
      font-size: 13px;
      color: var(--tidal-text-secondary);
    }
  }
}

.param-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-form-item__label) {
    color: var(--tidal-text-main);
  }

  .unit {
    margin-left: 12px;
    font-size: 13px;
    color: var(--tidal-text-secondary);
  }
}

.rules-table {
  :deep(.el-table) {
    .el-input-number {
      width: 100%;
    }
  }
}
</style>