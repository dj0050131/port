<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { createSystemParameter, deleteSystemParameter, fetchSystemParameters, updateSystemParameter } from '@/api/system'
import type { SystemParameter } from '@/types/system'
import { fetchAuthPermissionsList } from '@/api/authPermissions'
import { fetchSystemParametersByGroup } from '@/api/system'
import type { AuthPermission } from '@/types/authPermissions'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<SystemParameter>('system.parameters', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  paramGroup: '',
  keyword: '',
  page: tableState.page,
  size: tableState.pageSize,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增参数')
const saving = ref(false)
const formRef = ref()
const formData = reactive<SystemParameter>({
  id: undefined,
  paramGroup: '',
  paramKey: '',
  paramValue: '',
  paramName: '',
  isSystem: false,
  sortOrder: 0,
})

const permsEditorVisible = ref(false)
const permsEditorSaving = ref(false)
const permsAll = ref<AuthPermission[]>([])
const userTypes = ref<Array<{ code: string; label?: string }>>([])
const permsSelection = reactive<Record<string, string[]>>({})
const editingParamId = ref<number | null>(null)

const isEditingUserTypePerms = computed(() => formData.paramGroup === 'AUTH' && formData.paramKey === 'USER_TYPE_PERMISSIONS')

const openPermsEditor = async () => {
  permsEditorVisible.value = true
  permsEditorSaving.value = false
  editingParamId.value = formData.id || null

  try {
    const res = await fetchAuthPermissionsList()
    if (res.code === 200) permsAll.value = res.data || []
  } catch (e) {
    // ignore
  }

  try {
    const res = await fetchSystemParametersByGroup('AUTH')
    if (res.code === 200) {
      const json = (res.data || []).find((x) => x.paramKey === 'USER_TYPES')?.paramValue
      if (json) {
        const arr = JSON.parse(json) as Array<{ code: string; label?: string }>
        userTypes.value = Array.isArray(arr) ? arr.filter((x) => x?.code) : []
      }
    }
  } catch (e) {
    // ignore
  }

  Object.keys(permsSelection).forEach((k) => delete permsSelection[k])
  try {
    const obj = formData.paramValue ? (JSON.parse(formData.paramValue) as Record<string, string[]>) : {}
    for (const ut of userTypes.value) {
      permsSelection[ut.code] = Array.isArray(obj?.[ut.code]) ? obj[ut.code] : []
    }
  } catch (e) {
    for (const ut of userTypes.value) permsSelection[ut.code] = []
  }
}

const savePermsEditor = async () => {
  if (!formData.id) {
    ElMessage.warning('请先保存该参数后再配置权限')
    return
  }
  permsEditorSaving.value = true
  try {
    const payloadObj: Record<string, string[]> = {}
    for (const ut of userTypes.value) {
      payloadObj[ut.code] = (permsSelection[ut.code] || []).slice()
    }
    const json = JSON.stringify(payloadObj)
    const res = await updateSystemParameter(formData.id, { ...formData, paramValue: json })
    if (res.code === 200) {
      ElMessage.success('权限配置已保存')
      formData.paramValue = json
      permsEditorVisible.value = false
      await fetchData()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    permsEditorSaving.value = false
  }
}

const rules = {
  paramGroup: [{ required: true, message: '请输入参数组', trigger: 'blur' }],
  paramKey: [{ required: true, message: '请输入参数键', trigger: 'blur' }],
  paramValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchSystemParameters({
      current: searchForm.page,
      size: searchForm.size,
      paramGroup: searchForm.paramGroup?.trim() || undefined,
      keyword: searchForm.keyword?.trim() || undefined,
    })
    if (res.code === 200) {
      const records = (res.data.records || []).slice().sort((a, b) => {
        const g1 = String(a?.paramGroup || '')
        const g2 = String(b?.paramGroup || '')
        if (g1 !== g2) return g1.localeCompare(g2, 'zh-Hans-CN')
        const s1 = Number(a?.sortOrder ?? 0)
        const s2 = Number(b?.sortOrder ?? 0)
        if (s1 !== s2) return s1 - s2
        const id1 = Number(a?.id ?? 0)
        const id2 = Number(b?.id ?? 0)
        return id1 - id2
      })
      tableState.items = records
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增参数'
  Object.assign(formData, {
    id: undefined,
    paramGroup: '',
    paramKey: '',
    paramValue: '',
    paramName: '',
    isSystem: false,
    sortOrder: 0,
  })
  dialogVisible.value = true
}

const openEdit = (row: SystemParameter) => {
  dialogTitle.value = '编辑参数'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const submit = async () => {
  const ok = await formRef.value?.validate?.().catch(() => false)
  if (!ok) return

  saving.value = true
  try {
    if (!formData.id) {
      const res = await createSystemParameter(formData)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '新增失败')
      }
    } else {
      const res = await updateSystemParameter(formData.id, formData)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        await fetchData()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: SystemParameter) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(`确定删除参数 ${row.paramGroup}.${row.paramKey} 吗？`, '确认删除', { type: 'warning' })
    const res = await deleteSystemParameter(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e === 'cancel') return
    console.error(e)
    ElMessage.error('删除失败')
  }
}

const handlePageChange = (p: number) => {
  searchForm.page = p
  fetchData()
}
const handleSizeChange = (s: number) => {
  searchForm.size = s
  searchForm.page = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="system-parameters page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">参数配置</h2>
        <p class="page-desc">维护 system_parameters（用于电子发票卖方信息、系统开关等）</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增参数</el-button>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-input v-model="searchForm.paramGroup" placeholder="参数组" style="width: 180px; margin-right: 10px" clearable />
        <el-input v-model="searchForm.keyword" placeholder="键/名称/值" style="width: 220px; margin-right: 10px" clearable />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
        <el-table-column prop="paramGroup" label="参数组" width="160" />
        <el-table-column prop="paramKey" label="参数键" width="180" />
        <el-table-column prop="paramName" label="参数名称" width="160" show-overflow-tooltip />
        <el-table-column prop="paramValue" label="参数值" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="right" />
        <el-table-column prop="isSystem" label="系统参数" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isSystem ? 'danger' : 'info'">{{ row.isSystem ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170">
          <template #default="{ row }">
            {{ row.updatedAt ? new Date(row.updatedAt).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
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
          :page-size="searchForm.size"
          :current-page="searchForm.page"
          @update:current-page="handlePageChange"
          @update:page-size="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="参数组" prop="paramGroup">
          <el-input v-model="formData.paramGroup" placeholder="例如：E_INVOICE_SELLER" />
        </el-form-item>
        <el-form-item label="参数键" prop="paramKey">
          <el-input v-model="formData.paramKey" placeholder="例如：company_name" />
        </el-form-item>
        <el-form-item label="参数名称">
          <el-input v-model="formData.paramName" placeholder="用于展示" />
        </el-form-item>
        <el-form-item label="参数值" prop="paramValue">
          <el-input v-model="formData.paramValue" type="textarea" :rows="3" />
          <div v-if="isEditingUserTypePerms" style="margin-top: 8px; width: 100%">
            <el-button type="primary" plain size="small" @click="openPermsEditor">可视化配置权限</el-button>
            <span style="margin-left: 8px; color: var(--tidal-text-secondary); font-size: 12px">
              通过“身份→权限码多选”生成 JSON，无需手写
            </span>
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="系统参数">
          <el-switch v-model="formData.isSystem" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permsEditorVisible" title="身份权限配置（可视化）" width="860px" destroy-on-close>
      <div style="margin-bottom: 10px; color: var(--tidal-text-secondary); font-size: 12px">
        左侧为身份类型，右侧选择该身份拥有的权限码（来源：系统管理→权限码信息）。
      </div>
      <el-table :data="userTypes" stripe size="small" height="420px">
        <el-table-column label="身份" width="200">
          <template #default="{ row }">
            {{ row.label || row.code }}（{{ row.code }}）
          </template>
        </el-table-column>
        <el-table-column label="权限码" min-width="600">
          <template #default="{ row }">
            <el-select
              v-model="permsSelection[row.code]"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择权限码"
              style="width: 100%"
            >
              <el-option
                v-for="p in permsAll"
                :key="p.permCode"
                :label="`${p.permName}（${p.permCode}）`"
                :value="p.permCode"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="permsEditorVisible = false">取消</el-button>
        <el-button type="primary" :loading="permsEditorSaving" @click="savePermsEditor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.system-parameters {
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

.main-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

