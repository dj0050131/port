<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getAnchoragesList, createAnchorages, updateAnchorages, deleteAnchorages } from '@/api/bases/anchorages'
import type { Anchorages } from '@/types/bases/anchorages'
import { usePortsStore } from '@/stores/ports'
import { anchorageStatusLabel, anchorageTypeLabel } from '@/utils/anchorage-utils'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Anchorages>('master.anchorages', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const keyword = ref('')
const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)
const portsStore = usePortsStore()
const portAreas = computed(() => portsStore.portAreas)
const portAreaMap = computed<Record<number, any>>(() => {
  const m: Record<number, any> = {}
  for (const a of portAreas.value) {
    if (a?.id) m[a.id] = a
  }
  return m
})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const formData = ref<Anchorages>({
  id: undefined,
  portAreaId: undefined,
  anchorageCode: '',
  anchorageNameZh: '',
  anchorageNameEn: '',
  anchorageType: 'WAITING',
  capacity: undefined,
  maxDraft: undefined,
  minDraft: undefined,
  longitude: undefined,
  latitude: undefined,
  radius: undefined,
  tideRestriction: '',
  weatherRestriction: '',
  status: 'AVAILABLE'
})

const formRules = {
  portAreaId: [{ required: true, message: '请选择所属港区', trigger: 'change' }],
  anchorageCode: [{ required: true, message: '请输入锚地代码', trigger: 'blur' }],
  anchorageNameZh: [{ required: true, message: '请输入中文名称', trigger: 'blur' }],
  anchorageType: [{ required: true, message: '请选择锚地类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  longitude: [{ required: true, message: '请输入中心经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入中心纬度', trigger: 'blur' }]
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getAnchoragesList(params)
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
  formTitle.value = '新增锚地'
  isEdit.value = false
  formData.value = {
    id: undefined,
    portAreaId: undefined,
    anchorageCode: '',
    anchorageNameZh: '',
    anchorageNameEn: '',
    anchorageType: 'WAITING',
    capacity: undefined,
    maxDraft: undefined,
    minDraft: undefined,
    longitude: undefined,
    latitude: undefined,
    radius: undefined,
    tideRestriction: '',
    weatherRestriction: '',
    status: 'AVAILABLE'
  }
  dialogVisible.value = true
}

const handleEdit = (row: Anchorages) => {
  formTitle.value = '编辑锚地'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Anchorages) => {
  try {
    await ElMessageBox.confirm('确认删除该锚地吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteAnchorages(row.id!)
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
      res = await updateAnchorages(formData.value.id!, formData.value)
    } else {
      res = await createAnchorages(formData.value)
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
onMounted(() => {
  portsStore.fetchPortAreas(1)
})
</script>

<template>
  <div class="anchorages-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">锚地管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="锚地名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="anchorageCode" label="锚地代码" />
        <el-table-column prop="anchorageNameZh" label="中文名称" />
        <el-table-column prop="anchorageNameEn" label="英文名称" />
        <el-table-column prop="portAreaId" label="所属港区" width="140">
          <template #default="{ row }">
            {{ portAreaMap[row.portAreaId]?.areaNameZh || row.portAreaId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="锚地类型" width="120">
          <template #default="{ row }">
            {{ anchorageTypeLabel(row.anchorageType as any) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            {{ anchorageStatusLabel(row.status as any) }}
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳船舶数量" />
        <el-table-column prop="maxDraft" label="最大水深(米)" />
        <el-table-column prop="minDraft" label="最小水深(米)" />
        <el-table-column prop="longitude" label="中心经度" />
        <el-table-column prop="latitude" label="中心纬度" />
        <el-table-column prop="radius" label="半径(海里)" />
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="140px" v-loading="formLoading">
        <el-form-item label="所属港区" prop="portAreaId" required>
          <el-select v-model="formData.portAreaId" placeholder="请选择港区" filterable style="width: 100%">
            <el-option
              v-for="a in portAreas"
              :key="a.id"
              :label="`${a.areaNameZh}（${a.areaCode}）`"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="锚地代码" prop="anchorageCode">
          <el-input v-model="formData.anchorageCode" />
        </el-form-item>
        <el-form-item label="中文名称" prop="anchorageNameZh">
          <el-input v-model="formData.anchorageNameZh" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="formData.anchorageNameEn" />
        </el-form-item>
        <el-form-item label="锚地类型" prop="anchorageType">
          <el-select v-model="formData.anchorageType" style="width: 100%">
            <el-option label="检疫" value="QUARANTINE" />
            <el-option label="待泊" value="WAITING" />
            <el-option label="应急" value="EMERGENCY" />
            <el-option label="备用" value="SPARE" />
            <el-option label="引航" value="PILOTAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="占用" value="OCCUPIED" />
            <el-option label="受限" value="RESTRICTED" />
            <el-option label="维护" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="容纳船舶数量">
          <el-input-number v-model="formData.capacity" :precision="0" :step="1" />
        </el-form-item>
        <el-form-item label="最大水深(米)">
          <el-input-number v-model="formData.maxDraft" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="最小水深(米)">
          <el-input-number v-model="formData.minDraft" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="中心经度" prop="longitude">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="中心纬度" prop="latitude">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="半径(海里)">
          <el-input-number v-model="formData.radius" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="潮汐限制说明">
          <el-input v-model="formData.tideRestriction" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="气象限制说明">
          <el-input v-model="formData.weatherRestriction" type="textarea" :rows="2" />
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
.anchorages-master-view { 
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