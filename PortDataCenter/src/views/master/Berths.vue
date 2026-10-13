<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getBerthsList, createBerths, updateBerths, deleteBerths } from '@/api/bases/berths'
import type { Berths } from '@/types/bases/berths'
import { usePortsStore } from '@/stores/ports'
import { BERTH_TYPE_OPTIONS, berthTypeLabel } from '@/utils/berth-utils'
import { fetchWharvesAll } from '@/api/bases/ports'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Berths>('master.berths', {
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
const wharves = computed(() => portsStore.wharves)
const uiPortAreaId = ref<number | null>(null)

const portAreaMap = computed<Record<number, any>>(() => {
  const m: Record<number, any> = {}
  for (const a of portAreas.value) {
    if (a?.id) m[a.id] = a
  }
  return m
})

const wharfMap = computed<Record<number, any>>(() => {
  const m: Record<number, any> = {}
  for (const w of wharves.value) {
    if (w?.id) m[w.id] = w
  }
  return m
})

const allWharves = ref<any[]>([])
const allWharfMap = computed<Record<number, any>>(() => {
  const m: Record<number, any> = {}
  for (const w of allWharves.value) {
    if (w?.id) m[Number(w.id)] = w
  }
  return m
})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const formData = ref<Berths>({
  id: undefined,
  berthCode: '',
  portArea: '',
  berthType: '',
  berthName: '',
  wharfDirection: '',
  longitude: undefined,
  latitude: undefined,
  portAreaId: undefined,
  wharfId: undefined,
  maxDraft: undefined,
  berthLength: undefined,
  berthDepth: undefined,
  contactPhone: ''
})

const berthTypeOptions = computed(() => {
  const uniq = Array.from(new Set((tableState.items || []).map(x => (x as any)?.berthType).filter(Boolean) as string[])).map(v => String(v))
  const fromData = uniq.map(v => ({ value: v, label: berthTypeLabel(v) }))
  const merged = [...BERTH_TYPE_OPTIONS, ...fromData]
  const seen = new Set<string>()
  return merged.filter(o => {
    if (!o?.value) return false
    if (seen.has(o.value)) return false
    seen.add(o.value)
    return true
  })
})

const formRules = {
  berthCode: [{ required: true, message: '请输入泊位代码', trigger: 'blur' }],
  berthName: [{ required: true, message: '请输入泊位名称', trigger: 'blur' }],
  portAreaId: [{ required: true, message: '请选择所属港区', trigger: 'change' }],
  wharfId: [{ required: true, message: '请选择所属码头', trigger: 'change' }],
  berthType: [{ required: true, message: '请选择或输入泊位类型', trigger: 'change' }]
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getBerthsList(params)
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
  formTitle.value = '新增泊位'
  isEdit.value = false
  formData.value = {
    id: undefined,
    berthCode: '',
    portArea: '',
    berthType: '',
    berthName: '',
    wharfDirection: '',
    longitude: undefined,
    latitude: undefined,
    portAreaId: undefined,
    wharfId: undefined,
    maxDraft: undefined,
    berthLength: undefined,
    berthDepth: undefined,
    contactPhone: ''
  }
  uiPortAreaId.value = null
  dialogVisible.value = true
}

const handleEdit = (row: Berths) => {
  formTitle.value = '编辑泊位'
  isEdit.value = true
  formData.value = { ...row }
  uiPortAreaId.value = row.portAreaId || null
  dialogVisible.value = true
}

watch(uiPortAreaId, async (val) => {
  formData.value.portAreaId = val || undefined
  formData.value.wharfId = undefined
  if (val) {
    await portsStore.fetchWharves(val)
    formData.value.portArea = portAreaMap.value[val]?.areaNameZh || formData.value.portArea || ''
  }
})

const handleDelete = async (row: Berths) => {
  try {
    await ElMessageBox.confirm('确认删除该泊位吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteBerths(row.id!)
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
    if (formData.value.portAreaId) {
      formData.value.portArea =
        portAreaMap.value[Number(formData.value.portAreaId)]?.areaNameZh ||
        formData.value.portArea ||
        ''
    }

    let res
    if (isEdit.value) {
      res = await updateBerths(formData.value.id!, formData.value)
    } else {
      res = await createBerths(formData.value)
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
onMounted(async () => {
  try {
    const res = await fetchWharvesAll()
    if (res.code === 200) {
      allWharves.value = res.data || []
    }
  } catch (e) {
    // ignore
  }
})
</script>

<template>
  <div class="berths-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">泊位管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="泊位名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="berthCode" label="泊位代码" />
        <el-table-column prop="berthName" label="泊位名称" />
        <el-table-column label="所属港区" width="140">
          <template #default="{ row }">
            {{ portAreaMap[row.portAreaId]?.areaNameZh || row.portArea || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="所属码头" width="160">
          <template #default="{ row }">
            {{ allWharfMap[row.wharfId]?.wharfNameZh || wharfMap[row.wharfId]?.wharfNameZh || row.wharfId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="泊位类型" width="120">
          <template #default="{ row }">
            {{ berthTypeLabel(row.berthType) }}
          </template>
        </el-table-column>
        <el-table-column prop="wharfDirection" label="码头方向" width="120" />
        <el-table-column prop="berthLength" label="泊位长度(米)" />
        <el-table-column prop="berthDepth" label="泊位水深(米)" />
        <el-table-column prop="maxDraft" label="最大吃水(米)" />
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
        <el-form-item label="泊位代码" prop="berthCode">
          <el-input v-model="formData.berthCode" />
        </el-form-item>
        <el-form-item label="泊位名称" prop="berthName">
          <el-input v-model="formData.berthName" />
        </el-form-item>
        <el-form-item label="所属港区" prop="portAreaId" required>
          <el-select v-model="uiPortAreaId" placeholder="请选择港区" filterable style="width: 100%">
            <el-option
              v-for="a in portAreas"
              :key="a.id"
              :label="`${a.areaNameZh}（${a.areaCode}）`"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属码头" prop="wharfId" required>
          <el-select v-model="formData.wharfId" placeholder="请选择码头" filterable style="width: 100%">
            <el-option
              v-for="w in wharves"
              :key="w.id"
              :label="`${w.wharfNameZh}（${w.wharfCode}）`"
              :value="w.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="泊位类型" prop="berthType">
          <el-select
            v-model="formData.berthType"
            style="width: 100%"
            filterable
            clearable
            allow-create
            default-first-option
            placeholder="请选择或输入泊位类型"
          >
            <el-option v-for="t in berthTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="码头方向">
          <el-input v-model="formData.wharfDirection" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="最大吃水(米)">
          <el-input-number v-model="formData.maxDraft" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="泊位长度(米)">
          <el-input-number v-model="formData.berthLength" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="泊位水深(米)">
          <el-input-number v-model="formData.berthDepth" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" />
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
.berths-master-view { 
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