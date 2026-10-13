<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getVehiclesList, createVehicles, updateVehicles, deleteVehicles } from '@/api/bases/vehicles'
import type { Vehicles } from '@/types/bases/vehicles'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Vehicles>('master.vehicles', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const keyword = ref('')
const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const formData = ref<Vehicles>({
  id: undefined,
  vehicleCode: '',
  licensePlate: '',
  vehicleModel: '',
  passengerCapacity: undefined,
  mainPerformance: '',
  color: '',
  currentLocation: '',
  vehicleStatus: 'AVAILABLE',
})

const formRules = {
  vehicleCode: [{ required: true, message: '请输入车辆代码', trigger: 'blur' }],
  licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }]
}

const locationOptions = computed(() => {
  const uniq = Array.from(new Set((tableState.items || []).map(x => (x as any)?.currentLocation).filter(Boolean) as string[]))
  const defaults = ['调度中心', '引航站', '车队', '港区', '码头', '锚地']
  const merged = [...defaults, ...uniq]
  return Array.from(new Set(merged)).map(v => ({ label: v, value: v }))
})

const vehicleStatusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '维护', value: 'MAINTENANCE' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '使用中', value: 'IN_USE' },
]
const vehicleStatusLabel = (v: any) => {
  const m: Record<string, string> = {
    AVAILABLE: '可用',
    MAINTENANCE: '维护',
    ASSIGNED: '已分配',
    IN_USE: '使用中',
  }
  const key = String(v || '').trim().toUpperCase()
  return m[key] || (v ? String(v) : '-')
}
const vehicleStatusTagType = (v: any) => {
  const key = String(v || '').trim().toUpperCase()
  if (key === 'AVAILABLE') return 'success'
  if (key === 'MAINTENANCE') return 'warning'
  if (key === 'ASSIGNED') return 'primary'
  if (key === 'IN_USE') return 'danger'
  return 'info'
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getVehiclesList(params)
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

const handleCreate = () => {
  formTitle.value = '新增车辆'
  isEdit.value = false
  formData.value = {
    id: undefined,
    vehicleCode: '',
    licensePlate: '',
    vehicleModel: '',
    passengerCapacity: undefined,
    mainPerformance: '',
    color: '',
    currentLocation: '',
    vehicleStatus: 'AVAILABLE',
  }
  dialogVisible.value = true
}

const handleEdit = (row: Vehicles) => {
  formTitle.value = '编辑车辆'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Vehicles) => {
  try {
    await ElMessageBox.confirm('确认删除该车辆吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteVehicles(row.id!)
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
    await formRef.value?.validate?.()
    let res
    if (isEdit.value) {
      res = await updateVehicles(formData.value.id!, formData.value)
    } else {
      res = await createVehicles(formData.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(`${isEdit.value ? '更新' : '创建'}成功`)
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || `${isEdit.value ? '更新' : '创建'}失败`)
    }
  } catch (err) {
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
</script>

<template>
  <div class="vehicles-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">车辆管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="车牌号/车辆代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="vehicleCode" label="车辆代码" />
        <el-table-column prop="licensePlate" label="车牌号" />
        <el-table-column prop="vehicleModel" label="车辆型号" />
        <el-table-column prop="passengerCapacity" label="载客数" />
        <el-table-column prop="mainPerformance" label="主要性能" />
        <el-table-column prop="color" label="颜色" />
        <el-table-column prop="currentLocation" label="当前位置" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="vehicleStatusTagType(row.vehicleStatus)">
              {{ vehicleStatusLabel(row.vehicleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
        <el-form-item label="车辆代码" prop="vehicleCode">
          <el-input v-model="formData.vehicleCode" />
        </el-form-item>
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="formData.licensePlate" />
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="formData.vehicleModel" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.vehicleStatus" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="o in vehicleStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="载客数">
          <el-input-number v-model="formData.passengerCapacity" :precision="0" :step="1" />
        </el-form-item>
        <el-form-item label="主要性能">
          <el-input v-model="formData.mainPerformance" type="textarea" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="formData.color" />
        </el-form-item>
        <el-form-item label="当前位置">
          <el-select
            v-model="formData.currentLocation"
            placeholder="请选择或输入当前位置"
            filterable
            clearable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option v-for="o in locationOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
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
.vehicles-master-view { 
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