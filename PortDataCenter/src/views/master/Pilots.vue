<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getPilotsList, createPilots, updatePilots, deletePilots } from '@/api/bases/pilots'
import type { Pilots } from '@/types/bases/pilots'
import { getUserList } from '@/api/bases/user'
import { getDriversList } from '@/api/bases/drivers'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Pilots>('master.pilots', {
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
const formRef = ref()
const formData = ref<Pilots>({
  id: undefined,
  userId: undefined,
  pilotCode: '',
  pilotName: '',
  pilotLevel: '',
  phone: '',
  email: '',
  certificateNo: '',
  emergencyContact: '',
  emergencyPhone: '',
  onDutyStatus: 'ON_DUTY',
})

const userOptions = ref<any[]>([])
const pilotLevelOptions = [
  { label: '一级', value: '一级' },
  { label: '二级', value: '二级' },
  { label: '三级', value: '三级' },
  { label: '四级', value: '四级' },
  { label: '见习', value: '见习' }
]

const dutyStatusOptions = [
  { label: '在岗', value: 'ON_DUTY' },
  { label: '休息', value: 'RESTING' },
  { label: '请假', value: 'LEAVE' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '执行中', value: 'OPERATING' },
]
const dutyStatusLabel = (v: any) => {
  const m: Record<string, string> = {
    ON_DUTY: '在岗',
    RESTING: '休息',
    LEAVE: '请假',
    ASSIGNED: '已分配',
    OPERATING: '执行中',
  }
  const key = String(v || '').trim().toUpperCase()
  return m[key] || (v ? String(v) : '-')
}
const dutyStatusTagType = (v: any) => {
  const key = String(v || '').trim().toUpperCase()
  if (key === 'ON_DUTY') return 'success'
  if (key === 'RESTING') return 'info'
  if (key === 'LEAVE') return 'warning'
  if (key === 'ASSIGNED') return 'primary'
  if (key === 'OPERATING') return 'danger'
  return 'info'
}

const userLabel = (u: any) => `${u?.username || '-'}（${u?.account || u?.id || '-'}）`

const userMap = computed<Record<number, any>>(() => {
  const m: Record<number, any> = {}
  for (const u of userOptions.value) {
    if (u?.id) m[Number(u.id)] = u
  }
  return m
})

const usedUserIdSet = ref<Set<number>>(new Set())
const userLabelWithUsed = (u: any) => {
  const base = userLabel(u)
  const uid = Number(u?.id)
  if (!uid) return base
  if (usedUserIdSet.value.has(uid)) return `${base}（已关联）`
  return base
}

const formRules = {
  userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }],
  pilotCode: [{ required: true, message: '请输入引航员工号', trigger: 'blur' }],
  pilotLevel: [{ required: true, message: '请选择引航员等级', trigger: 'change' }]
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getPilotsList(params)
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
  formTitle.value = '新增引航员'
  isEdit.value = false
  formData.value = {
    id: undefined,
    userId: undefined,
    pilotCode: '',
    pilotName: '',
    pilotLevel: '',
    phone: '',
    email: '',
    certificateNo: '',
    emergencyContact: '',
    emergencyPhone: '',
    onDutyStatus: 'ON_DUTY',
  }
  dialogVisible.value = true
}

const handleEdit = (row: Pilots) => {
  formTitle.value = '编辑引航员'
  isEdit.value = true
  formData.value = { ...row }
  handleUserChange(row.userId)
  dialogVisible.value = true
}

const handleDelete = async (row: Pilots) => {
  try {
    await ElMessageBox.confirm('确认删除该引航员吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deletePilots(row.id!)
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
      res = await updatePilots(formData.value.id!, formData.value)
    } else {
      res = await createPilots(formData.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(`${isEdit.value ? '更新' : '创建'}成功`)
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || `${isEdit.value ? '更新' : '创建'}失败`)
    }
  } catch (err) {
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
onMounted(async () => {
  try {
    const res = await getUserList()
    if (res.code === 200) {
      userOptions.value = res.data || []
    }
  } catch (e) {
    // ignore
  }
})
onMounted(async () => {
  // pilots/drivers 已关联用户集合
  try {
    const set = new Set<number>()
    const [pRes, dRes] = await Promise.all([
      getPilotsList({ page: 1, size: 1000 }),
      getDriversList({ page: 1, size: 1000 })
    ])
    if (pRes.code === 200) {
      for (const r of (pRes.data?.records || [])) {
        if ((r as any)?.userId) set.add(Number((r as any).userId))
      }
    }
    if (dRes.code === 200) {
      for (const r of (dRes.data?.records || [])) {
        if ((r as any)?.userId) set.add(Number((r as any).userId))
      }
    }
    usedUserIdSet.value = set
  } catch (e) {
    // ignore
  }
})

const handleUserChange = (userId?: number) => {
  if (!userId) return
  const u = userOptions.value.find((x: any) => Number(x.id) === Number(userId))
  if (!u) return
  // 关联用户后自动回填；用户信息统一在“用户管理”维护，这里不允许改
  formData.value.pilotName = u.username
  formData.value.phone = u.phone
  formData.value.email = u.email
}
</script>

<template>
  <div class="pilots-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">引航员管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="引航员姓名/工号" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="pilotCode" label="工号" width="100" />
        <el-table-column prop="pilotName" label="姓名" width="120" />
        <el-table-column label="关联用户" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ userMap[row.userId]?.username ? `${userMap[row.userId]?.username}（${userMap[row.userId]?.account || row.userId}）` : (row.userId || '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="pilotLevel" label="等级" width="120" />
        <el-table-column prop="certificateNo" label="证书编号" width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="150" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="在岗状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="dutyStatusTagType(row.onDutyStatus)">
              {{ dutyStatusLabel(row.onDutyStatus) }}
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
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>
    
    <el-dialog v-model="dialogVisible" :title="formTitle" width="760px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
        <el-form-item label="关联用户" prop="userId" required>
          <el-select
            v-model="formData.userId"
            placeholder="请选择关联用户（引航员必须关联用户）"
            filterable
            style="width: 100%"
            @change="handleUserChange"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="userLabelWithUsed(u)"
              :value="u.id"
              :disabled="usedUserIdSet.has(Number(u.id)) && Number(u.id) !== Number(formData.userId)"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="引航员工号" prop="pilotCode">
              <el-input v-model="formData.pilotCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="引航员等级" prop="pilotLevel">
              <el-select
                v-model="formData.pilotLevel"
                placeholder="请选择引航员等级"
                filterable
                clearable
                allow-create
                default-first-option
                style="width: 100%"
              >
                <el-option v-for="o in pilotLevelOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="在岗状态">
              <el-select v-model="formData.onDutyStatus" placeholder="请选择在岗状态" style="width: 100%">
                <el-option v-for="o in dutyStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名(来自用户)">
              <el-input v-model="formData.pilotName" disabled placeholder="请在用户管理维护" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话(来自用户)">
              <el-input v-model="formData.phone" disabled placeholder="请在用户管理维护" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱(来自用户)">
          <el-input v-model="formData.email" disabled placeholder="请在用户管理维护" />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="证书编号">
              <el-input v-model="formData.certificateNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="formData.emergencyContact" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="紧急联系电话">
          <el-input v-model="formData.emergencyPhone" />
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
.pilots-master-view { 
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