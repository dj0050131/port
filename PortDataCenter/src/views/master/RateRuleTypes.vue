<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getRateRuleTypesList, createRateRuleTypes, updateRateRuleTypes, deleteRateRuleTypes } from '@/api/bases/rateRuleTypes'
import { getChargeItemList } from '@/api/bases/chargeItems'
import type { RateRuleTypes } from '@/types/bases/rateRuleTypes'
import type { ChargeItem } from '@/types/bases/chargeItems'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<RateRuleTypes>('master.rateRuleTypes', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const keyword = ref('')
const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)
const chargeItems = ref<ChargeItem[]>([])
const chargeItemMap = ref<Record<number, ChargeItem>>({})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formData = ref<RateRuleTypes>({
  id: undefined,
  ruleTypeCode: '',
  ruleTypeName: '',
  calculationMethod: '',
  chargeItemId: undefined,
  isSystem: 0,
  sortOrder: 0,
  description: ''
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getRateRuleTypesList(params)
    if (res.code === 200) {
      tableState.items = res.data.records
      tableState.total = res.data.total
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (err) {
    ElMessage.error('获取数据失败')
    console.error(err)
  } finally {
    tableState.loading = false
  }
}

const fetchChargeItems = async () => {
  try {
    const res = await getChargeItemList()
    if (res.code === 200) {
      chargeItems.value = res.data || []
      chargeItemMap.value = (res.data || []).reduce((acc: any, it: any) => {
        acc[it.id] = it
        return acc
      }, {})
    }
  } catch (e) {
    console.warn('获取收费项目失败', e)
  }
}

const handleCreate = () => {
  formTitle.value = '新增费率规则类型'
  isEdit.value = false
  formData.value = {
    id: undefined,
    ruleTypeCode: '',
    ruleTypeName: '',
    calculationMethod: '',
    chargeItemId: undefined,
    isSystem: 0,
    sortOrder: 0,
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: RateRuleTypes) => {
  formTitle.value = '编辑费率规则类型'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: RateRuleTypes) => {
  try {
    await ElMessageBox.confirm('确认删除该费率规则类型吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteRateRuleTypes(row.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(err)
    }
  }
}

const submitForm = async () => {
  formLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateRateRuleTypes(formData.value.id!, formData.value)
    } else {
      res = await createRateRuleTypes(formData.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(`${isEdit.value ? '更新' : '创建'}成功`)
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || `${isEdit.value ? '更新' : '创建'}失败`)
    }
  } catch (err) {
    ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`)
    console.error(err)
  } finally {
    formLoading.value = false
  }
}

onMounted(fetchData)

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  tableState.page = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  tableState.pageSize = size
  tableState.page = 1
  fetchData()
}
onMounted(fetchChargeItems)
</script>

<template>
  <div class="rate-rule-types-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">费率规则类型管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="规则类型编码/名称" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="ruleTypeCode" label="规则类型编码" />
        <el-table-column prop="ruleTypeName" label="规则类型名称" />
        <el-table-column prop="calculationMethod" label="计算方式">
          <template #default="{ row }">
            <span v-if="row.calculationMethod === 'PER_NET_TON'">按净吨</span>
            <span v-else-if="row.calculationMethod === 'PER_NAUTICAL_MILE'">按海里</span>
            <span v-else-if="row.calculationMethod === 'PER_HOUR'">按时长</span>
            <span v-else-if="row.calculationMethod === 'FIXED'">固定费用</span>
            <span v-else-if="row.calculationMethod === 'PERCENTAGE'">按百分比</span>
            <span v-else>{{ row.calculationMethod }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chargeItemId" label="关联收费项目">
          <template #default="{ row }">
            <span v-if="row.chargeItemId && chargeItemMap[row.chargeItemId]">
              {{ chargeItemMap[row.chargeItemId].itemName }}
            </span>
            <span v-else>
              {{ row.chargeItemId || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isSystem" label="是否系统内置">
          <template #default="{ row }">
            {{ row.isSystem === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" />
        <el-table-column label="操作" width="150">
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
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>
    
    <el-dialog v-model="dialogVisible" :title="formTitle" width="600px">
      <el-form :model="formData" label-width="120px" v-loading="formLoading">
        <el-form-item label="规则类型编码">
          <el-input v-model="formData.ruleTypeCode" />
        </el-form-item>
        <el-form-item label="规则类型名称">
          <el-input v-model="formData.ruleTypeName" />
        </el-form-item>
        <el-form-item label="计算方式">
          <el-select v-model="formData.calculationMethod" placeholder="请选择计算方式" style="width: 100%">
            <el-option label="按净吨" value="PER_NET_TON" />
            <el-option label="按海里" value="PER_NAUTICAL_MILE" />
            <el-option label="按时长" value="PER_HOUR" />
            <el-option label="固定费用" value="FIXED" />
            <el-option label="按百分比" value="PERCENTAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联收费项目">
          <el-select
            v-model="formData.chargeItemId"
            placeholder="请选择收费项目"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="it in chargeItems"
              :key="it.id"
              :label="it.itemName"
              :value="it.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否系统内置">
          <el-switch
            v-model="formData.isSystem"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :precision="0" :step="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="formLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.rate-rule-types-master-view { 
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
}
</style>