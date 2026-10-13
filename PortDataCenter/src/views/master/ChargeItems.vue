<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { useDataCenterStore } from '@/stores/dataCenter'
import type { ChargeItem } from '@/types/bases/chargeItems'
import type { ChargeCategory } from '@/types/bases/chargeCategories'
import { getChargeCategoryList } from '@/api/bases/chargeCategories'
import { createChargeItem, deleteChargeItem, getChargeItemsPage, updateChargeItem } from '@/api/bases/chargeItems'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<ChargeItem>('master.chargeItems', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const keyword = ref('')
const categoryId = ref<number | undefined>(undefined)
const currentPage = ref(tableState.page)
const pageSize = ref(tableState.pageSize)

const categories = ref<ChargeCategory[]>([])
const categoryMap = computed<Record<number, ChargeCategory>>(() => {
  const m: Record<number, ChargeCategory> = {}
  for (const c of categories.value) {
    if (c?.id != null) m[Number(c.id)] = c
  }
  return m
})

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const formData = ref<ChargeItem>({
  id: 0,
  itemCode: '',
  categoryId: undefined,
  itemName: '',
  itemNotes: '',
  taxMethod: 'INCLUSIVE',
  taxRate: 0,
  sortOrder: 0,
  status: 'ENABLED',
})

const taxMethodOptions = [
  { label: '含税', value: 'INCLUSIVE' },
  { label: '不含税', value: 'EXCLUSIVE' },
]
const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
]

const fetchCategories = async () => {
  try {
    const res = await getChargeCategoryList()
    if (res.code === 200) {
      categories.value = (res.data || []).slice().sort((a: any, b: any) => Number(a?.sortOrder || 0) - Number(b?.sortOrder || 0))
    }
  } catch (e) {
    // ignore
  }
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getChargeItemsPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value?.trim() || undefined,
      categoryId: categoryId.value,
    })
    if (res.code === 200) {
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleCreate = () => {
  formTitle.value = '新增收费项目'
  isEdit.value = false
  formData.value = {
    id: 0,
    itemCode: '',
    categoryId: categoryId.value,
    itemName: '',
    itemNotes: '',
    taxMethod: 'INCLUSIVE',
    taxRate: 0,
    sortOrder: 0,
    status: 'ENABLED',
  }
  dialogVisible.value = true
}

const handleEdit = (row: ChargeItem) => {
  formTitle.value = '编辑收费项目'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  formLoading.value = true
  try {
    await formRef.value?.validate?.()
    const payload: Partial<ChargeItem> = {
      itemCode: formData.value.itemCode,
      categoryId: formData.value.categoryId,
      itemName: formData.value.itemName,
      itemNotes: formData.value.itemNotes,
      taxMethod: formData.value.taxMethod,
      taxRate: formData.value.taxRate,
      sortOrder: formData.value.sortOrder,
      status: formData.value.status,
    }
    if (isEdit.value) {
      const res = await updateChargeItem(Number(formData.value.id), payload)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await createChargeItem(payload)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (row: ChargeItem) => {
  const id = Number(row?.id)
  if (!id) return
  try {
    await ElMessageBox.confirm(`确定删除收费项目「${row.itemName}」吗？`, '确认删除', { type: 'warning' })
    const res = await deleteChargeItem(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    // ignore cancel
  }
}

const handleCurrentChange = (p: number) => {
  currentPage.value = p
  tableState.page = p
  fetchData()
}
const handleSizeChange = (s: number) => {
  pageSize.value = s
  currentPage.value = 1
  tableState.pageSize = s
  tableState.page = 1
  fetchData()
}

onMounted(async () => {
  await fetchCategories()
  await fetchData()
})
</script>

<template>
  <div class="charge-items-master-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">收费项目管理</h2>
        <p class="page-desc">维护 charge_items（项目编码、税率、状态、排序等）</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>

    <div class="table-container glass-card">
      <div class="toolbar">
        <el-select v-model="categoryId" placeholder="收费分类" clearable style="width: 200px">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="项目编码/名称" style="width: 220px" clearable @keyup.enter="fetchData" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="itemCode" label="项目编码" width="140" />
        <el-table-column prop="itemName" label="项目名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="收费分类" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.categoryId && categoryMap[row.categoryId]">
              {{ categoryMap[row.categoryId].categoryName }}
            </span>
            <span v-else>{{ row.categoryId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="taxMethod" label="计税方式" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.taxMethod === 'EXCLUSIVE' ? 'warning' : 'success'">
              {{ row.taxMethod === 'EXCLUSIVE' ? '不含税' : '含税' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taxRate" label="税率" width="90" align="right" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="right" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
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
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="formTitle" width="720px">
      <el-form ref="formRef" :model="formData" label-width="110px" v-loading="formLoading">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="项目编码" prop="itemCode" :rules="[{ required: true, message: '请输入项目编码', trigger: 'blur' }]">
              <el-input v-model="formData.itemCode" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收费分类" prop="categoryId" :rules="[{ required: true, message: '请选择收费分类', trigger: 'change' }]">
              <el-select v-model="formData.categoryId" filterable style="width: 100%" placeholder="请选择收费分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目名称" prop="itemName" :rules="[{ required: true, message: '请输入项目名称', trigger: 'blur' }]">
          <el-input v-model="formData.itemName" />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计税方式">
              <el-select v-model="formData.taxMethod" style="width: 100%">
                <el-option v-for="o in taxMethodOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税率">
              <el-input-number v-model="formData.taxRate" :precision="4" :step="0.0001" :min="0" :max="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="formData.sortOrder" :precision="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="formData.itemNotes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.charge-items-master-view {
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

.page-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--tidal-text-secondary);
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
  align-items: center;
}
</style>
