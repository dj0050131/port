<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import {
  createTugboatCompany,
  deleteTugboatCompany,
  getTugboatCompaniesPage,
  updateTugboatCompany
} from '@/api/bases/tugboatCompanies'
import type { TugboatCompany } from '@/types/bases/tugboatCompanies'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<TugboatCompany>('master.tugboatCompanies', {
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
const formRef = ref<FormInstance>()

const formData = ref<TugboatCompany>({
  id: undefined,
  companyCode: '',
  companyName: '',
  contactName: '',
  contactPhone: '',
  status: 'ACTIVE',
  notes: ''
})

const rules = computed<FormRules>(() => ({
  companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const statusLabel = (s?: string) => {
  if (s === 'ACTIVE') return '启用'
  if (s === 'INACTIVE') return '停用'
  return s || '-'
}
const statusTagType = (s?: string) => {
  if (s === 'ACTIVE') return 'success'
  if (s === 'INACTIVE') return 'info'
  return 'info'
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getTugboatCompaniesPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    if (res.code === 200) {
      tableState.items = res.data.records
      tableState.total = res.data.total
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (e) {
    ElMessage.error('获取数据失败')
    console.error(e)
  } finally {
    tableState.loading = false
  }
}

const handleCreate = () => {
  formTitle.value = '新增拖轮公司'
  isEdit.value = false
  formData.value = {
    id: undefined,
    companyCode: '',
    companyName: '',
    contactName: '',
    contactPhone: '',
    status: 'ACTIVE',
    notes: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: TugboatCompany) => {
  formTitle.value = '编辑拖轮公司'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: TugboatCompany) => {
  try {
    await ElMessageBox.confirm('确认删除该拖轮公司吗？（若已被拖轮引用将无法删除）', '提示', { type: 'warning' })
    const res = await deleteTugboatCompany(row.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    formLoading.value = true
    try {
      const payload: TugboatCompany = {
        ...formData.value,
        companyCode: (formData.value.companyCode || '').trim(),
        companyName: (formData.value.companyName || '').trim()
      }
      const res = isEdit.value
        ? await updateTugboatCompany(payload.id!, payload)
        : await createTugboatCompany(payload)

      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
      }
    } catch (e) {
      console.error(e)
    } finally {
      formLoading.value = false
    }
  })
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
  <div class="tugboat-companies-view page-container">
    <div class="page-header">
      <h2 class="page-title">拖轮公司管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>

    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="公司名称/编码" style="width: 240px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="companyCode" label="公司编码" width="140" />
        <el-table-column prop="companyName" label="公司名称" min-width="200" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
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

    <el-dialog v-model="dialogVisible" :title="formTitle" width="640px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px" v-loading="formLoading">
        <el-form-item label="公司编码" prop="companyCode">
          <el-input v-model="formData.companyCode" placeholder="例如 TC0001" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="formData.companyName" placeholder="例如 温州港拖轮公司" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="formData.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.notes" type="textarea" />
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
.tugboat-companies-view {
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

