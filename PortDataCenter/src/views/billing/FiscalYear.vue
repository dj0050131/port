<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Check } from '@element-plus/icons-vue'
import { useDataCenterStore } from '@/stores/dataCenter'
import { createFiscalYear, deleteFiscalYear, fetchFiscalYearsPage, updateFiscalYear, updateFiscalYearStatus } from '@/api/fiscalYear'
import type { FiscalYear } from '@/types/fiscalYear'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<FiscalYear>('billing.fiscalYear', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentYear = ref<FiscalYear>({ year: '' })
const selectedRows = ref<FiscalYear[]>([])

const handleSelectionChange = (rows: FiscalYear[]) => {
  selectedRows.value = rows || []
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchFiscalYearsPage({
      page: tableState.page,
      size: tableState.pageSize,
    })
    if (res.code === 200) {港口引航管理系统
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    }
  } finally {
    tableState.loading = false
  }
}

const handleCreate = () => {
  currentYear.value = {
    year: new Date().getFullYear().toString(),
    targetTonnage: '',
    targetRevenue: '',
    estimatedRevenue: '',
    status: true
  }
  dialogTitle.value = '添加会计年度'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  currentYear.value = { ...row }
  dialogTitle.value = '编辑会计年度'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    const payload: FiscalYear = {
      ...currentYear.value,
      year: (currentYear.value.year || '').trim(),
      status: !!currentYear.value.status,
    }
    if (!payload.year) {
      ElMessage.error('会计年度不能为空')
      return
    }

    if (payload.id) {
      const res = await updateFiscalYear(payload.id, payload)
      if (res.code === 200) {
        ElMessage.success('保存成功')
      }
    } else {
      const res = await createFiscalYear(payload)
      if (res.code === 200) {
        ElMessage.success('保存成功')
      }
    }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除会计年度 ${row.year}?`, '提示', {
    type: 'warning'
  }).then(() => {
    return deleteFiscalYear(row.id).then((res) => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      }
    })
  })
}

const handleToggleStatus = (row: any) => {
  updateFiscalYearStatus(row.id, !!row.status)
    .then((res) => {
      if (res.code === 200) {
        ElMessage.success(`已${row.status ? '启用' : '停用'} ${row.year} 年度`)
      }
    })
    .catch((e: any) => {
      row.status = !row.status
      ElMessage.error(e?.message || '更新状态失败')
    })
}

const getSingleSelection = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选一条记录')
    return null
  }
  if (selectedRows.value.length > 1) {
    ElMessage.warning('一次只能操作一条记录')
    return null
  }
  return selectedRows.value[0]
}

const handleEditSelected = () => {
  const row = getSingleSelection()
  if (!row) return
  handleEdit(row)
}

const handleDeleteSelected = () => {
  const row = getSingleSelection()
  if (!row) return
  handleDelete(row)
}

const handleEnableSelected = () => {
  const row = getSingleSelection()
  if (!row) return
  const next = !row.status
  updateFiscalYearStatus(row.id!, next).then((res) => {
    if (res.code === 200) {
      ElMessage.success(`已${next ? '启用' : '停用'} ${row.year} 年度`)
      fetchData()
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="fiscal-year-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">会计年度管理</h2>
        <p class="page-desc">管理引航费收的会计年度及指标设定</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
        <el-button type="success" :icon="Plus" @click="handleCreate">新建</el-button>
        <el-button type="primary" :icon="Edit" @click="handleEditSelected">编辑</el-button>
        <el-button type="warning" :icon="Check" @click="handleEnableSelected">启用/停用</el-button>
        <el-button type="danger" :icon="Delete" @click="handleDeleteSelected">删除</el-button>
      </div>
    </div>

    <div class="table-container glass-card">
       <el-table
         :data="tableState.items"
         v-loading="tableState.loading"
         stripe
         height="100%"
         @selection-change="handleSelectionChange"
       >
          <el-table-column type="selection" width="40" />
          <el-table-column prop="year" label="会计年度" width="120" />
          <el-table-column prop="actualTonnage" label="年度完成(吨)" min-width="150" />
          <el-table-column prop="actualRevenue" label="年度完成收入(元)" min-width="180" />
          <el-table-column prop="forecastRevenue" label="预估收入(元)" min-width="180" />
          <el-table-column prop="status" label="状态" width="100">
             <template #default="{ row }">
                <el-switch v-model="row.status" @change="handleToggleStatus(row)" />
             </template>
          </el-table-column>
       </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="450px"
      destroy-on-close
      class="billing-dialog"
    >
      <div style="padding: 20px;">
        <el-form label-width="120px" size="default">
          <el-form-item label="会计年度:">
            <el-input v-model="currentYear.year" />
          </el-form-item>
          <el-form-item label="年度指标(吨):">
            <el-input v-model="currentYear.targetTonnage" />
          </el-form-item>
          <el-form-item label="年度力争指标(元):">
            <el-input v-model="currentYear.targetRevenue" />
          </el-form-item>
          <el-form-item label="预估收入(元):">
            <el-input v-model="currentYear.estimatedRevenue" />
          </el-form-item>
          <el-form-item label="启用:">
            <el-checkbox v-model="currentYear.status" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleSave">保 存</el-button>
        <el-button @click="dialogVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.fiscal-year-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
  .page-desc {
    font-size: 12px;
    color: var(--tidal-text-secondary);
    margin: 4px 0 0;
  }
}

.table-container { flex: 1; padding: 16px; overflow: hidden; }

:deep(.billing-dialog) {
  .el-dialog__body { padding: 0; }
  .el-dialog__header { border-bottom: 1px solid var(--tidal-border); margin-right: 0; }
}
</style>