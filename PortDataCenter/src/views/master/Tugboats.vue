<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getTugboatsList, createTugboats, updateTugboats, deleteTugboats } from '@/api/bases/tugboats'
import type { Tugboats } from '@/types/bases/tugboats'
import { fetchTugboatCompaniesList } from '@/api/bases/tugboatCompanies'
import type { TugboatCompany } from '@/types/bases/tugboatCompanies'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Tugboats>('master.tugboats', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const keyword = ref('')
const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)
const companies = ref<TugboatCompany[]>([])
const companyMap = ref<Record<number, TugboatCompany>>({})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formData = ref<Tugboats>({
  id: undefined,
  tugboatCode: '',
  tugboatName: '',
  imoNumber: '',
  callSign: '',
  ownerCompany: '',
  bollardPull: undefined,
  length: undefined,
  power: undefined,
  buildYear: '',
  currentLocation: '',
  status: 'AVAILABLE',
})

const tugboatStatusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '维护', value: 'MAINTENANCE' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '作业中', value: 'OPERATING' },
  { label: '停用', value: 'INACTIVE' },
]
const tugboatStatusLabel = (v: any) => {
  const m: Record<string, string> = {
    AVAILABLE: '可用',
    MAINTENANCE: '维护',
    ASSIGNED: '已分配',
    OPERATING: '作业中',
    INACTIVE: '停用',
  }
  const key = String(v || '').trim().toUpperCase()
  return m[key] || (v ? String(v) : '-')
}
const tugboatStatusTagType = (v: any) => {
  const key = String(v || '').trim().toUpperCase()
  if (key === 'AVAILABLE') return 'success'
  if (key === 'MAINTENANCE') return 'warning'
  if (key === 'ASSIGNED') return 'primary'
  if (key === 'OPERATING') return 'danger'
  if (key === 'INACTIVE') return 'info'
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
    const res = await getTugboatsList(params)
    if (res.code === 200) {
      tableState.items = res.data.records
      tableState.items = tableState.items.map((r) => {
        const id = (r as any).ownerCompanyId as number | undefined
        const name = id ? companyMap.value[id]?.companyName : undefined
        return { ...r, ownerCompanyName: name || (r as any).ownerCompany || '' }
      })
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
  formTitle.value = '新增拖轮'
  isEdit.value = false
  formData.value = {
    id: undefined,
    tugboatCode: '',
    tugboatName: '',
    imoNumber: '',
    callSign: '',
    ownerCompanyId: undefined,
    bollardPull: undefined,
    length: undefined,
    power: undefined,
    buildYear: '',
    currentLocation: '',
    status: 'AVAILABLE',
  }
  dialogVisible.value = true
}

const handleEdit = (row: Tugboats) => {
  formTitle.value = '编辑拖轮'
  isEdit.value = true
  const by: any = (row as any).buildYear
  formData.value = { ...row, buildYear: by != null ? String(by) : '' }
  dialogVisible.value = true
}

const handleDelete = async (row: Tugboats) => {
  try {
    await ElMessageBox.confirm('确认删除该拖轮吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteTugboats(row.id!)
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
      res = await updateTugboats(formData.value.id!, formData.value)
    } else {
      res = await createTugboats(formData.value)
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
onMounted(async () => {
  try {
    const res = await fetchTugboatCompaniesList('ACTIVE')
    if (res.code === 200) {
      companies.value = res.data
      companyMap.value = Object.fromEntries((res.data || []).filter((c) => c.id != null).map((c) => [c.id as number, c]))
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div class="tugboats-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">拖轮管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="拖轮名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="tugboatCode" label="拖轮代码" />
        <el-table-column prop="tugboatName" label="拖轮名称" />
        <el-table-column prop="imoNumber" label="IMO编号" />
        <el-table-column prop="callSign" label="呼号" />
        <el-table-column prop="ownerCompanyName" label="所属公司" />
        <el-table-column prop="bollardPull" label="拖力(吨)" />
        <el-table-column prop="length" label="船长(米)" />
        <el-table-column prop="power" label="功率(kW)" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="tugboatStatusTagType(row.status)">
              {{ tugboatStatusLabel(row.status) }}
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
        <el-form-item label="拖轮代码">
          <el-input v-model="formData.tugboatCode" />
        </el-form-item>
        <el-form-item label="拖轮名称">
          <el-input v-model="formData.tugboatName" />
        </el-form-item>
        <el-form-item label="IMO编号">
          <el-input v-model="formData.imoNumber" />
        </el-form-item>
        <el-form-item label="呼号">
          <el-input v-model="formData.callSign" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="o in tugboatStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属公司" required>
          <el-select v-model="formData.ownerCompanyId" filterable clearable placeholder="请选择拖轮公司" style="width: 100%">
            <el-option v-for="c in companies" :key="c.id" :label="c.companyName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="拖力(吨)">
          <el-input-number v-model="formData.bollardPull" :precision="0" :step="10" />
        </el-form-item>
        <el-form-item label="船长(米)">
          <el-input-number v-model="formData.length" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="功率(kW)">
          <el-input-number v-model="formData.power" :precision="0" :step="100" />
        </el-form-item>
        <el-form-item label="建造年份">
          <el-date-picker
            v-model="formData.buildYear"
            type="year"
            value-format="YYYY"
            format="YYYY"
            placeholder="请选择建造年份"
            style="width: 100%"
          />
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
.tugboats-master-view { 
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