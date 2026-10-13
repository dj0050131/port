<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import type { AuthPermission } from '@/types/authPermissions'
import {
  fetchAuthPermissionsPage,
  createAuthPermission,
  updateAuthPermission,
  deleteAuthPermission,
} from '@/api/authPermissions'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<AuthPermission>('system.authPermissions', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const query = reactive({
  current: tableState.page,
  size: tableState.pageSize,
  keyword: '',
  permGroup: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增权限码')
const saving = ref(false)
const formRef = ref()
const form = reactive<Partial<AuthPermission>>({
  permCode: '',
  permName: '',
  permGroup: '',
  description: '',
  isSystem: false,
  sortOrder: 0,
})
const editingId = ref<number | null>(null)

const rules = {
  permCode: [{ required: true, message: '请输入权限码', trigger: 'blur' }],
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchAuthPermissionsPage({
      current: query.current,
      size: query.size,
      keyword: query.keyword?.trim() || undefined,
      permGroup: query.permGroup?.trim() || undefined,
    })
    if (res.code === 200) {
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取权限码失败')
    }
  } finally {
    tableState.loading = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增权限码'
  editingId.value = null
  Object.assign(form, {
    permCode: '',
    permName: '',
    permGroup: '',
    description: '',
    isSystem: false,
    sortOrder: 0,
  })
  dialogVisible.value = true
}

const openEdit = (row: AuthPermission) => {
  dialogTitle.value = '编辑权限码'
  editingId.value = row.id || null
  Object.assign(form, row)
  dialogVisible.value = true
}

const submit = async () => {
  const ok = await formRef.value?.validate?.().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    const payload: Partial<AuthPermission> = {
      permCode: (form.permCode || '').trim(),
      permName: (form.permName || '').trim(),
      permGroup: form.permGroup?.trim() || undefined,
      description: form.description || undefined,
      isSystem: !!form.isSystem,
      sortOrder: Number(form.sortOrder || 0),
    }
    const res = editingId.value
      ? await updateAuthPermission(editingId.value, payload)
      : await createAuthPermission(payload)
    if (res.code === 200) {
      ElMessage.success(editingId.value ? '更新成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || (editingId.value ? '更新失败' : '新增失败'))
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: AuthPermission) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(`确认删除权限码 ${row.permCode} 吗？`, '确认删除', { type: 'warning' })
    const res = await deleteAuthPermission(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="auth-permissions page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">权限码信息</h2>
        <p class="page-desc">用于查看/维护权限码，供“身份权限配置”可视化选择</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增权限码</el-button>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="权限码/名称关键字" style="width: 240px" clearable />
        <el-input v-model="query.permGroup" placeholder="权限分组(可选)" style="width: 160px" clearable />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe size="small" height="100%">
        <el-table-column prop="permCode" label="权限码" min-width="220" show-overflow-tooltip />
        <el-table-column prop="permName" label="权限名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="permGroup" label="分组" width="120" />
        <el-table-column prop="isSystem" label="系统" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isSystem ? 'warning' : 'info'">{{ row.isSystem ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="tableState.total"
          :page-size="query.size"
          :current-page="query.current"
          @update:current-page="(p:number) => { query.current=p; fetchData() }"
          @update:page-size="(s:number) => { query.size=s; query.current=1; fetchData() }"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="saving">
        <el-form-item label="权限码" prop="permCode">
          <el-input v-model="form.permCode" placeholder="如：BASE:TUGBOATS:MANAGE" />
        </el-form-item>
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="form.permName" placeholder="如：拖轮管理" />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="form.permGroup" placeholder="如：BASE/BILLING/SYSTEM" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="系统">
          <el-switch v-model="form.isSystem" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.auth-permissions { height: 100%; display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

