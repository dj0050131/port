<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getWharvesList, createWharves, updateWharves, deleteWharves } from '@/api/bases/wharves'
import type { Wharves } from '@/types/bases/wharves'
import { usePortsStore } from '@/stores/ports'
import { computed } from 'vue'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Wharves>('master.wharves', {
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
const formData = ref<Wharves>({
  id: undefined,
  portAreaId: undefined,
  wharfCode: '',
  wharfNameZh: '',
  wharfNameEn: '',
  wharfLength: undefined,
  wharfDepth: undefined,
  constructionDate: '',
  wharfType: 'GENERAL',
  operationalStatus: 'OPERATIONAL',
  managementCompany: '',
  contactPhone: '',
  longitude: undefined,
  latitude: undefined
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getWharvesList(params)
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
  formTitle.value = '新增码头'
  isEdit.value = false
  formData.value = {
    id: undefined,
    portAreaId: undefined,
    wharfCode: '',
    wharfNameZh: '',
    wharfNameEn: '',
    wharfLength: undefined,
    wharfDepth: undefined,
    constructionDate: '',
    wharfType: 'GENERAL',
    operationalStatus: 'OPERATIONAL',
    managementCompany: '',
    contactPhone: '',
    longitude: undefined,
    latitude: undefined
  }
  dialogVisible.value = true
}

const handleEdit = (row: Wharves) => {
  formTitle.value = '编辑码头'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Wharves) => {
  try {
    await ElMessageBox.confirm('确认删除该码头吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteWharves(row.id!)
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
    const payload: any = { ...formData.value }
    // 后端 Date 反序列化不接受空字符串：空值直接不传
    if (!payload.constructionDate || String(payload.constructionDate).trim() === '') delete payload.constructionDate
    delete payload.createdAt
    delete payload.updatedAt
    ;['wharfType', 'operationalStatus', 'wharfCode', 'wharfNameZh', 'wharfNameEn', 'managementCompany', 'contactPhone'].forEach((k) => {
      if (payload[k] === '') delete payload[k]
    })

    let res
    if (isEdit.value) {
      res = await updateWharves(payload.id!, payload)
    } else {
      res = await createWharves(payload)
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
  <div class="wharves-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">码头管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="码头名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="wharfCode" label="码头代码" />
        <el-table-column prop="wharfNameZh" label="中文名称" />
        <el-table-column prop="wharfNameEn" label="英文名称" />
        <el-table-column prop="portAreaId" label="所属港区" width="140">
          <template #default="{ row }">
            {{ portAreaMap[row.portAreaId]?.areaNameZh || row.portAreaId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="wharfLength" label="码头长度(米)" />
        <el-table-column prop="wharfDepth" label="码头水深(米)" />
        <el-table-column prop="wharfType" label="码头类型" width="120" />
        <el-table-column prop="operationalStatus" label="运营状态" width="120" />
        <el-table-column prop="managementCompany" label="管理公司" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="longitude" label="经度" />
        <el-table-column prop="latitude" label="纬度" />
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
        <el-form-item label="所属港区" required>
          <el-select v-model="formData.portAreaId" placeholder="请选择港区" filterable style="width: 100%">
            <el-option
              v-for="a in portAreas"
              :key="a.id"
              :label="`${a.areaNameZh}（${a.areaCode}）`"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="码头代码">
          <el-input v-model="formData.wharfCode" />
        </el-form-item>
        <el-form-item label="中文名称">
          <el-input v-model="formData.wharfNameZh" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="formData.wharfNameEn" />
        </el-form-item>
        <el-form-item label="建成日期">
          <el-date-picker v-model="formData.constructionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="码头类型">
          <el-select v-model="formData.wharfType" placeholder="请选择" style="width: 100%">
            <el-option label="集装箱" value="CONTAINER" />
            <el-option label="散货" value="BULK" />
            <el-option label="通用" value="GENERAL" />
            <el-option label="油品" value="OIL" />
            <el-option label="客运" value="PASSENGER" />
            <el-option label="多用途" value="MULTIPURPOSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="运营状态">
          <el-select v-model="formData.operationalStatus" placeholder="请选择" style="width: 100%">
            <el-option label="运营中" value="OPERATIONAL" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="关闭" value="CLOSED" />
            <el-option label="建设中" value="CONSTRUCTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="码头长度(米)">
          <el-input-number v-model="formData.wharfLength" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="码头水深(米)">
          <el-input-number v-model="formData.wharfDepth" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="管理公司">
          <el-input v-model="formData.managementCompany" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" />
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
.wharves-master-view { 
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