<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getPortsList, createPorts, updatePorts, deletePorts } from '@/api/bases/ports'
import type {Port} from "@/types/bases/ports.ts";
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Port>('master.ports', {
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
const formData = ref<Port>({
  createdAt: "",
  status: undefined,
  updatedAt: "",
  country: '',
  id: 0,
  latitude: 0,
  longitude: 0,
  portCode: '',
  portNameEn: '',
  portNameZh: '',
  region: '',
  timezone: ''
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getPortsList(params)
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
  formTitle.value = '新增港口'
  isEdit.value = false
  formData.value = {
    createdAt: "", status: undefined, updatedAt: "",
    id: 0,
    portCode: '',
    portNameZh: '',
    portNameEn: '',
    country: '',
    region: '',
    longitude: 0,
    latitude: 0,
    timezone: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: Port) => {
  formTitle.value = '编辑港口'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Port) => {
  try {
    await ElMessageBox.confirm('确认删除该港口吗？', '提示', {
      type: 'warning'
    })

    const res = await deletePorts(row.id!)
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
      res = await updatePorts(formData.value.id!, formData.value)
    } else {
      res = await createPorts(formData.value)
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
  <div class="ports-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">港口管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>

    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="港口名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="portCode" label="港口代码" />
        <el-table-column prop="portNameZh" label="中文名称" />
        <el-table-column prop="portNameEn" label="英文名称" />
        <el-table-column prop="country" label="国家" />
        <el-table-column prop="region" label="区域" />
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
        <el-form-item label="港口代码">
          <el-input v-model="formData.portCode" />
        </el-form-item>
        <el-form-item label="中文名称">
          <el-input v-model="formData.portNameZh" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="formData.portNameEn" />
        </el-form-item>
        <el-form-item label="国家">
          <el-input v-model="formData.country" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="formData.region" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="时区">
          <el-input v-model="formData.timezone" />
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
.ports-master-view {
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