<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  fetchUpdateHistoryPage,
  createUpdateHistory,
  updateUpdateHistory,
  deleteUpdateHistory,
} from '@/api/updateHistory'
import type { UpdateHistoryItem } from '@/types/updateHistory'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<UpdateHistoryItem>('system.updateHistory', {
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
  published: undefined as number | undefined,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const form = reactive<UpdateHistoryItem>({
  title: '',
  version: '',
  content: '',
  published: 0,
})
const editingId = ref<number | null>(null)

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await fetchUpdateHistoryPage({
      current: query.current,
      size: query.size,
      keyword: query.keyword?.trim() || undefined,
      published: query.published,
    })
    if (res.code === 200) {
      tableState.items = res.data.records || []
      tableState.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取更新历史失败')
    }
  } finally {
    tableState.loading = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增更新历史'
  editingId.value = null
  form.title = ''
  form.version = ''
  form.content = ''
  form.published = 0
  dialogVisible.value = true
}

const openEdit = (row: UpdateHistoryItem) => {
  dialogTitle.value = '编辑更新历史'
  editingId.value = row.id || null
  form.title = row.title || ''
  form.version = row.version || ''
  form.content = row.content || ''
  form.published = row.published ?? 0
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.title?.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  const payload: UpdateHistoryItem = {
    title: form.title.trim(),
    version: form.version?.trim() || undefined,
    content: form.content || undefined,
    published: form.published ?? 0,
  }
  if (editingId.value) {
    const res = await updateUpdateHistory(editingId.value, payload)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } else {
    const res = await createUpdateHistory(payload)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '新增失败')
    }
  }
}

const handleDelete = async (row: UpdateHistoryItem) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确认删除这条更新历史？', '提示', { type: 'warning' })
    const res = await deleteUpdateHistory(row.id)
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

const handlePageChange = (p: number) => {
  query.current = p
  fetchData()
}
const handleSizeChange = (s: number) => {
  query.size = s
  query.current = 1
  fetchData()
}

onMounted(() => fetchData())
</script>

<template>
  <div class="update-history-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">更新历史</h2>
        <p class="page-desc">记录系统版本更新与发布说明（管理员可维护）</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="标题/版本/内容关键字" style="width: 240px" clearable />
        <el-select v-model="query.published" placeholder="发布状态" style="width: 140px" clearable>
          <el-option label="已发布" :value="1" />
          <el-option label="未发布" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openCreate">新增</el-button>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe size="small" height="100%">
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="published" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.published === 1 ? 'success' : 'info'">
              {{ row.published === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="160" />
        <el-table-column prop="updatedAt" label="更新时间" width="160" />
        <el-table-column label="操作" width="150">
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
          @update:current-page="handlePageChange"
          @update:page-size="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="必填" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="form.version" placeholder="如：v1.2.0" />
        </el-form-item>
        <el-form-item label="发布">
          <el-switch v-model="form.published" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="发布说明/变更点" />
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
.update-history-view {
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
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

