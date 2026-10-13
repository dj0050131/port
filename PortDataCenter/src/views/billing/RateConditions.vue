<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { useDataCenterStore } from '@/stores/dataCenter'
import type { RateCondition } from '@/types/bases/rateConditions'
import type { StandardRates } from '@/types/bases/standardRates'
import { getStandardRateList } from '@/api/bases/standardRates'
import { createRateCondition, deleteRateCondition, getRateConditionsPage, updateRateCondition } from '@/api/bases/rateConditions'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<RateCondition>('billing.rateConditions', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)
const keyword = ref('')
const conditionType = ref('')
const standardRateId = ref<number | undefined>(undefined)

const standardRates = ref<StandardRates[]>([])
const standardRateMap = computed<Record<number, StandardRates>>(() => {
  const m: Record<number, StandardRates> = {}
  for (const r of standardRates.value) {
    if ((r as any)?.id) m[Number((r as any).id)] = r
  }
  return m
})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const formData = ref<RateCondition>({
  id: 0,
  standardRateId: 0,
  conditionType: '',
  conditionOperator: 'EQ',
  conditionValue: '',
  description: '',
})

const operatorOptions = [
  { label: '=', value: 'EQ' },
  { label: '!=', value: 'NE' },
  { label: '>', value: 'GT' },
  { label: '>=', value: 'GE' },
  { label: '<', value: 'LT' },
  { label: '<=', value: 'LE' },
  { label: 'IN', value: 'IN' },
  { label: 'LIKE', value: 'LIKE' },
  { label: 'BETWEEN', value: 'BETWEEN' },
]

const normalizeOperator = (op: any) => {
  const v = String(op ?? '').trim().toUpperCase()
  if (!v) return 'EQ'
  if (v === '=') return 'EQ'
  if (v === '!=') return 'NE'
  if (v === '>') return 'GT'
  if (v === '>=') return 'GE'
  if (v === '<') return 'LT'
  if (v === '<=') return 'LE'
  return v
}

const operatorLabel = (op: any) => {
  const v = normalizeOperator(op)
  const found = operatorOptions.find((x) => x.value === v)
  return found?.label || v
}

const conditionTypeOptions = [
  { label: '净吨区间（NET_TON_RANGE）', value: 'NET_TON_RANGE' },
  { label: '净吨（NET_TON）', value: 'NET_TON' },
  { label: '海里（NAUTICAL_MILES）', value: 'NAUTICAL_MILES' },
  { label: '引航小时（PILOTAGE_HOURS）', value: 'PILOTAGE_HOURS' },
  { label: '靠离泊协助标志（BERTHING_ASSISTANCE_FLAG）', value: 'BERTHING_ASSISTANCE_FLAG' },
  { label: '滞期标志（DETENTION_FLAG）', value: 'DETENTION_FLAG' },
]

const fetchStandardRates = async () => {
  try {
    const res = await getStandardRateList()
    if (res.code === 200) {
      standardRates.value = (res.data || []).slice()
    }
  } catch {
    // ignore
  }
}

const rateLabel = (r: any) => {
  if (!r) return '-'
  const code = r.rateCode ? String(r.rateCode) : `#${r.id}`
  const notes = r.notes ? String(r.notes) : ''
  return notes ? `${code} - ${notes}` : code
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getRateConditionsPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value?.trim() || undefined,
      conditionType: conditionType.value?.trim() || undefined,
      standardRateId: standardRateId.value,
    })
    if (res.code === 200) {
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleCreate = () => {
  formTitle.value = '新增费率条件'
  isEdit.value = false
  const defaultType = conditionType.value || conditionTypeOptions[0]?.value || ''
  formData.value = {
    id: 0,
    standardRateId: standardRateId.value || 0,
    conditionType: defaultType,
    conditionOperator: 'EQ',
    conditionValue: '',
    description: '',
  }
  dialogVisible.value = true
}

const handleEdit = (row: RateCondition) => {
  formTitle.value = '编辑费率条件'
  isEdit.value = true
  formData.value = { ...row, conditionOperator: normalizeOperator((row as any).conditionOperator) }
  dialogVisible.value = true
}

const submitForm = async () => {
  formLoading.value = true
  try {
    await formRef.value?.validate?.()
    const payload: any = {
      standardRateId: Number(formData.value.standardRateId),
      conditionType: String(formData.value.conditionType || '').trim(),
      conditionOperator: normalizeOperator(formData.value.conditionOperator),
      conditionValue: String(formData.value.conditionValue || '').trim(),
      description: formData.value.description,
    }
    if (!payload.standardRateId || payload.standardRateId <= 0) {
      ElMessage.warning('请选择标准费率')
      return
    }
    if (!payload.conditionType) {
      ElMessage.warning('请填写条件类型')
      return
    }
    if (!payload.conditionValue) {
      ElMessage.warning('请填写条件值')
      return
    }

    if (isEdit.value) {
      const res = await updateRateCondition(Number(formData.value.id), payload)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await createRateCondition(payload as RateCondition)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (row: RateCondition) => {
  const id = Number(row?.id)
  if (!id) return
  try {
    await ElMessageBox.confirm('确定删除该条件吗？', '确认删除', { type: 'warning' })
    const res = await deleteRateCondition(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // cancel
  }
}

const handleCurrentChange = (p: number) => {
  currentPage.value = p
  tableState.page = p
  fetchData()
}
const handleSizeChange = (s: number) => {
  pageSize.value = s
  currentPage.value = 1
  tableState.pageSize = s
  tableState.page = 1
  fetchData()
}

onMounted(async () => {
  await fetchStandardRates()
  await fetchData()
})
</script>

<template>
  <div class="rate-conditions-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">费率条件配置</h2>
        <p class="page-desc">维护 rate_conditions（用于标准费率的附加条件筛选）</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>

    <div class="table-container glass-card">
      <div class="toolbar">
        <el-select v-model="standardRateId" clearable filterable placeholder="标准费率" style="width: 280px">
          <el-option v-for="r in standardRates" :key="(r as any).id" :label="rateLabel(r)" :value="(r as any).id" />
        </el-select>
        <el-select v-model="conditionType" clearable filterable placeholder="条件类型" style="width: 260px">
          <el-option v-for="o in conditionTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-input v-model="keyword" placeholder="条件值/描述关键字" style="width: 220px" clearable @keyup.enter="fetchData" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="standardRateId" label="标准费率" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.standardRateId && standardRateMap[row.standardRateId]">
              {{ rateLabel(standardRateMap[row.standardRateId]) }}
            </span>
            <span v-else>{{ row.standardRateId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conditionType" label="条件类型" width="150" />
        <el-table-column prop="conditionOperator" label="运算符" width="90">
          <template #default="{ row }">
            {{ operatorLabel(row.conditionOperator) }}
          </template>
        </el-table-column>
        <el-table-column prop="conditionValue" label="条件值" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="tableState.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="formTitle" width="680px">
      <el-form ref="formRef" :model="formData" label-width="110px" v-loading="formLoading">
        <el-form-item label="标准费率" required>
          <el-select v-model="formData.standardRateId" filterable clearable placeholder="请选择标准费率" style="width: 100%">
            <el-option v-for="r in standardRates" :key="(r as any).id" :label="rateLabel(r)" :value="(r as any).id" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件类型" required>
          <el-select v-model="formData.conditionType" filterable placeholder="请选择条件类型" style="width: 100%">
            <el-option v-for="o in conditionTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="运算符">
          <el-select v-model="formData.conditionOperator" style="width: 100%">
            <el-option v-for="o in operatorOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件值" required>
          <el-input
            v-model="formData.conditionValue"
            type="textarea"
            :rows="2"
            placeholder="例如：CONTAINER / 30000,60000（BETWEEN）/ A,B,C（IN）"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.rate-conditions-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.page-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--tidal-text-secondary);
}

.table-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
