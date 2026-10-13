<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getVesselsList, createVessels, updateVessels, deleteVessels } from '@/api/bases/vessels'
import type { Vessels } from '@/types/bases/vessels'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Vessels>('master.vessels', {
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
const formData = ref<Vessels>({
  id: undefined,
  vesselCallSign: '',
  imoNumber: '',
  mmsiNumber: '',
  vesselType: '',
  nationality: '',
  ownerZh: '',
  ownerEn: '',
  nameZh: '',
  nameEn: '',
  vesselLength: undefined,
  vesselWidth: undefined,
  fullLoadDraft: undefined,
  grossTonnage: undefined,
  netTonnage: undefined,
  deadweight: undefined
})

const formRules = {
  nameZh: [{ required: true, message: '请输入中文船名', trigger: 'blur' }],
  imoNumber: [{ required: true, message: '请输入 IMO 编号', trigger: 'blur' }],
  vesselType: [{ required: true, message: '请选择船舶类型', trigger: 'change' }]
}

const vesselTypeOptions = computed(() => {
  const defaults = [
    '集装箱船',
    '散货船',
    '油轮',
    '化工船',
    'LNG船',
    '客船',
    '滚装船',
    '拖轮',
    '工程船',
    '杂货船',
    '其他'
  ]
  const uniq = Array.from(new Set((tableState.items || []).map(x => (x as any)?.vesselType).filter(Boolean) as string[]))
  const merged = [...defaults, ...uniq]
  return Array.from(new Set(merged)).map(v => ({ label: v, value: v }))
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getVesselsList(params)
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
  formTitle.value = '新增船舶'
  isEdit.value = false
  formData.value = {
    id: undefined,
    vesselCallSign: '',
    imoNumber: '',
    mmsiNumber: '',
    vesselType: '',
    nationality: '',
    ownerZh: '',
    ownerEn: '',
    nameZh: '',
    nameEn: '',
    vesselLength: undefined,
    vesselWidth: undefined,
    fullLoadDraft: undefined,
    grossTonnage: undefined,
    netTonnage: undefined,
    deadweight: undefined
  }
  dialogVisible.value = true
}

const handleEdit = (row: Vessels) => {
  formTitle.value = '编辑船舶'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Vessels) => {
  try {
    await ElMessageBox.confirm('确认删除该船舶吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteVessels(row.id!)
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
      res = await updateVessels(formData.value.id!, formData.value)
    } else {
      res = await createVessels(formData.value)
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
  <div class="vessels-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">船舶管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="船舶名称/IMO编号" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="nameZh" label="中文船名" />
        <el-table-column prop="nameEn" label="英文船名" />
        <el-table-column prop="imoNumber" label="IMO编号" />
        <el-table-column prop="vesselType" label="船舶类型" />
        <el-table-column prop="nationality" label="国籍" />
        <el-table-column prop="grossTonnage" label="总吨" />
        <el-table-column prop="deadweight" label="载重吨" />
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
        <el-form-item label="中文船名" prop="nameZh">
          <el-input v-model="formData.nameZh" />
        </el-form-item>
        <el-form-item label="英文船名">
          <el-input v-model="formData.nameEn" />
        </el-form-item>
        <el-form-item label="船舶呼号">
          <el-input v-model="formData.vesselCallSign" />
        </el-form-item>
        <el-form-item label="IMO编号" prop="imoNumber">
          <el-input v-model="formData.imoNumber" />
        </el-form-item>
        <el-form-item label="MMSI编号">
          <el-input v-model="formData.mmsiNumber" />
        </el-form-item>
        <el-form-item label="船舶类型" prop="vesselType">
          <el-select
            v-model="formData.vesselType"
            placeholder="请选择船舶类型"
            filterable
            clearable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option v-for="o in vesselTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="国籍">
          <el-input v-model="formData.nationality" />
        </el-form-item>
        <el-form-item label="中文船东">
          <el-input v-model="formData.ownerZh" />
        </el-form-item>
        <el-form-item label="英文船东">
          <el-input v-model="formData.ownerEn" />
        </el-form-item>
        <el-form-item label="船长(米)">
          <el-input-number v-model="formData.vesselLength" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="船宽(米)">
          <el-input-number v-model="formData.vesselWidth" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="满载吃水(米)">
          <el-input-number v-model="formData.fullLoadDraft" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="总吨">
          <el-input-number v-model="formData.grossTonnage" :precision="0" :step="1000" />
        </el-form-item>
        <el-form-item label="净吨">
          <el-input-number v-model="formData.netTonnage" :precision="0" :step="1000" />
        </el-form-item>
        <el-form-item label="载重吨">
          <el-input-number v-model="formData.deadweight" :precision="0" :step="1000" />
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
.vessels-master-view { 
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