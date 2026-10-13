<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getPilotBoatsList, createPilotBoats, updatePilotBoats, deletePilotBoats } from '@/api/bases/pilotboats'
import type { PilotBoats } from '@/types/bases/pilotboats'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PilotBoats>('master.pilotBoats', {
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
const formData = ref<PilotBoats>({
  id: undefined,
  vesselCallSign: '',
  boatName: '',
  imoNumber: '',
  operationRange: '',
  engineModel: '',
  enginePower: '',
  maxSpeed: undefined,
  passengerCapacity: undefined,
  currentLocation: '',
  currentStatus: 'AVAILABLE',
})

const boatStatusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '维护', value: 'MAINTENANCE' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '作业中', value: 'OPERATING' },
]
const boatStatusLabel = (v: any) => {
  const m: Record<string, string> = {
    AVAILABLE: '可用',
    MAINTENANCE: '维护',
    ASSIGNED: '已分配',
    OPERATING: '作业中',
  }
  const key = String(v || '').trim().toUpperCase()
  return m[key] || (v ? String(v) : '-')
}
const boatStatusTagType = (v: any) => {
  const key = String(v || '').trim().toUpperCase()
  if (key === 'AVAILABLE') return 'success'
  if (key === 'MAINTENANCE') return 'warning'
  if (key === 'ASSIGNED') return 'primary'
  if (key === 'OPERATING') return 'danger'
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
    const res = await getPilotBoatsList(params)
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
  formTitle.value = '新增引航艇'
  isEdit.value = false
  formData.value = {
    id: undefined,
    vesselCallSign: '',
    boatName: '',
    imoNumber: '',
    operationRange: '',
    engineModel: '',
    enginePower: '',
    maxSpeed: undefined,
    passengerCapacity: undefined,
    currentLocation: '',
    currentStatus: 'AVAILABLE',
  }
  dialogVisible.value = true
}

const handleEdit = (row: PilotBoats) => {
  formTitle.value = '编辑引航艇'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: PilotBoats) => {
  try {
    await ElMessageBox.confirm('确认删除该引航艇吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deletePilotBoats(row.id!)
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
      res = await updatePilotBoats(formData.value.id!, formData.value)
    } else {
      res = await createPilotBoats(formData.value)
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
</script>

<template>
  <div class="pilotboats-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">引航艇管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="引航艇名称/呼号" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="boatName" label="艇名" />
        <el-table-column prop="vesselCallSign" label="船舶呼号" />
        <el-table-column prop="imoNumber" label="IMO编号" />
        <el-table-column prop="operationRange" label="作业范围" />
        <el-table-column prop="engineModel" label="主机型号" />
        <el-table-column prop="enginePower" label="主机功率" />
        <el-table-column prop="maxSpeed" label="最大航速" />
        <el-table-column prop="passengerCapacity" label="乘员定额" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="boatStatusTagType(row.currentStatus)">
              {{ boatStatusLabel(row.currentStatus) }}
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
      <el-form :model="formData" label-width="120px" v-loading="formLoading">
        <el-form-item label="船舶呼号">
          <el-input v-model="formData.vesselCallSign" />
        </el-form-item>
        <el-form-item label="艇名">
          <el-input v-model="formData.boatName" />
        </el-form-item>
        <el-form-item label="IMO编号">
          <el-input v-model="formData.imoNumber" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.currentStatus" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="o in boatStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业范围">
          <el-input v-model="formData.operationRange" />
        </el-form-item>
        <el-form-item label="主机型号">
          <el-input v-model="formData.engineModel" />
        </el-form-item>
        <el-form-item label="主机功率">
          <el-input v-model="formData.enginePower" />
        </el-form-item>
        <el-form-item label="最大航速">
          <el-input-number v-model="formData.maxSpeed" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="乘员定额">
          <el-input-number v-model="formData.passengerCapacity" :precision="0" :step="1" />
        </el-form-item>
        <el-form-item label="当前位置">
          <el-input v-model="formData.currentLocation" />
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
.pilotboats-master-view { 
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