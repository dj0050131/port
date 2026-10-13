<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { Users } from '@/types/bases/user'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  getUserById,
  searchUsers,
  updateUserStatus,
  resetUserPassword,
  disableUser,
  offlineUser
} from '@/api/bases/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchSystemParametersByGroup } from '@/api/system'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Users>('system.users', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const searchKeyword = ref('')
const selectedUserType = ref('')
const dialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const formRef = ref()
const pwdFormRef = ref()

const formData = reactive<Users>({
  id: 0,
  userCode: '',
  account: '',
  userType: '',
  username: '',
  password: '',
  applyTime: '',
  phone: '',
  email: '',
  currentStatus: '',
  agentLevelType: '',
  parentAgentId: 0,
  avatar: '',
  lastLoginAt: '',
  department: '',
  position: '',
  createdAt: '',
  updatedAt: '',
})

const pwdFormData = reactive({
  id: 0,
  username: '',
  newPassword: ''
})

// 表单规则
const formRules = {
  username: [{ required: true, message: '请输入用户名称', trigger: 'blur' }],
  account: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const pwdFormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const userTypeLabelMap = computed(() => ({
  ADMIN: '管理员',
  FINANCE: '费收员',
  DISPATCHER: '调度员',
  PILOT: '引航相关人员',
  AGENT: '代理公司',
}))
const userTypeLabel = (code?: string) => {
  if (!code) return '-'
  return userTypeLabelMap.value[code] || code
}
const userTypeOptions = ref<{ value: string; label: string }[]>([
  { value: 'ADMIN', label: userTypeLabel('ADMIN') },
  { value: 'FINANCE', label: userTypeLabel('FINANCE') },
  { value: 'DISPATCHER', label: userTypeLabel('DISPATCHER') },
  { value: 'PILOT', label: userTypeLabel('PILOT') },
  { value: 'AGENT', label: userTypeLabel('AGENT') },
])

const loadUserList = async () => {
  tableState.loading = true
  try {
    const res = await getUserList(selectedUserType.value || undefined)
    if (res.code === 200) {
      tableState.items = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    tableState.loading = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadUserList()
    return
  }
  
  tableState.loading = true
  try {
    const res = await searchUsers(searchKeyword.value)
    if (res.code === 200) {
      const list = res.data || []
      tableState.items = selectedUserType.value
        ? list.filter((item) => item.userType === selectedUserType.value)
        : list
    }
  } catch (error) {
    ElMessage.error('搜索用户失败')
  } finally {
    tableState.loading = false
  }
}

const resetSearch = () => {
  searchKeyword.value = ''
  selectedUserType.value = ''
  loadUserList()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: 0,
    userCode: '',
    account: '',
    userType: '',
    username: '',
    password: '',
    applyTime: '',
    phone: '',
    email: '',
    currentStatus: '',
    agentLevelType: '',
    parentAgentId: 0,
    avatar: '',
    lastLoginAt: '',
    department: '',
    position: '',
    createdAt: '',
    updatedAt: '',
  })
  dialogVisible.value = true
}

const handleEdit = async (row: Users) => {
  const res = await getUserById(row.id)
  if (res.code === 200) {
    Object.assign(formData, res.data)
    dialogVisible.value = true
  } else {
    ElMessage.error('获取用户信息失败')
  }
}

const handleDelete = async (row: Users) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    } else {
      ElMessage.error('删除失败: ' + res.message)
    }
  } catch (error) {
  }
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    
    let res
    if (formData.id === 0) {
      res = await createUser(formData)
    } else {
      const { id, ...updateData } = formData
      res = await updateUser(id, updateData)
    }
    
    if (res.code === 200) {
      ElMessage.success(formData.id === 0 ? '创建成功' : '更新成功')
      dialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error((formData.id === 0 ? '创建失败: ' : '更新失败: ') + res.message)
    }
  } catch (error) {
    console.error(error)
  }
}

const showResetPwdDialog = (row: Users) => {
  pwdFormData.id = row.id
  pwdFormData.username = row.username
  pwdFormData.newPassword = ''
  pwdDialogVisible.value = true
}

const resetPassword = async () => {
  try {
    await pwdFormRef.value.validate()
    
    const res = await resetUserPassword(pwdFormData.id, pwdFormData.newPassword)
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      pwdDialogVisible.value = false
    } else {
      ElMessage.error('密码重置失败: ' + res.message)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleDisable = async (row: Users) => {
  try {
    await ElMessageBox.confirm(`确定要禁用用户"${row.username}"吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await disableUser(row.id)
    if (res.code === 200) {
      ElMessage.success('用户已禁用')
      loadUserList()
    } else {
      ElMessage.error('禁用失败: ' + res.message)
    }
  } catch (error) {
  }
}

const handleOffline = async (row: Users) => {
  try {
    await ElMessageBox.confirm(`确定要将用户"${row.username}"强制下线吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await offlineUser(row.id)
    if (res.code === 200) {
      ElMessage.success('用户已下线')
      loadUserList()
    } else {
      ElMessage.error('下线失败: ' + res.message)
    }
  } catch (error) {
  }
}

const changeUserStatus = async (row: Users, status: string) => {
  try {
    const res = await updateUserStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      loadUserList()
    } else {
      ElMessage.error('状态更新失败: ' + res.message)
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => {
  loadUserList()
})
onMounted(async () => {
  // 动态身份类型：AUTH.USER_TYPES（JSON数组），用于“新增身份”的扩展
  try {
    const res = await fetchSystemParametersByGroup('AUTH')
    if (res.code !== 200) return
    const json = (res.data || []).find((x) => x.paramKey === 'USER_TYPES')?.paramValue
    if (!json) return
    const arr = JSON.parse(json) as Array<{ code: string; label?: string }>
    if (!Array.isArray(arr) || arr.length === 0) return

    const nextMap = { ...userTypeLabelMap.value }
    for (const it of arr) {
      if (it?.code) nextMap[it.code] = it.label || it.code
    }
    userTypeLabelMap.value = nextMap

    userTypeOptions.value = arr
      .filter((x) => x?.code)
      .map((x) => ({ value: x.code, label: userTypeLabel(x.code) }))
  } catch (e) {
    // ignore
  }
})
</script>

<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select
        v-model="selectedUserType"
        placeholder="选择用户类型"
        clearable
        style="width: 180px; margin-right: 10px"
        @change="loadUserList"
      >
        <el-option v-for="o in userTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-input
        v-model="searchKeyword"
        placeholder="请输入用户名或账号搜索"
        style="width: 300px; margin-right: 10px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
      <el-button icon="Refresh" @click="resetSearch">重置</el-button>
      <el-button
        type="primary"
        icon="Plus"
        style="float: right"
        @click="handleAdd"
      >
        添加用户
      </el-button>
    </div>

    <el-table
      v-loading="tableState.loading"
      :data="tableState.items"
      border
      highlight-current-row
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名称" min-width="120" />
      <el-table-column prop="account" label="用户账号" min-width="120" />
      <el-table-column label="用户类型" width="140">
        <template #default="{ row }">
          {{ userTypeLabel(row.userType) }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="150" />
      <el-table-column prop="currentStatus" label="当前状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.currentStatus === 'ACTIVE' || row.currentStatus === 'NORMAL' ? 'success' : 'info'">
            {{ row.currentStatus === 'NORMAL' ? '正常' : row.currentStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            @click="showResetPwdDialog(row)"
          >
            重置密码
          </el-button>
          <el-button
            link
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
          <el-dropdown trigger="click">
            <el-button link type="primary" size="small">
              更多<i-ep-arrow-down class="el-icon--right" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="changeUserStatus(row, 'NORMAL')" v-if="row.currentStatus !== 'NORMAL'">
                  启用
                </el-dropdown-item>
                <el-dropdown-item @click="handleDisable(row)" v-if="row.currentStatus === 'NORMAL'">
                  禁用
                </el-dropdown-item>
                <el-dropdown-item @click="handleOffline(row)" v-if="row.currentStatus === 'NORMAL'">
                  强制下线
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="formData.id === 0 ? '添加用户' : '编辑用户'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名称" prop="username">
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item label="用户账号" prop="account">
          <el-input v-model="formData.account" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="formData.userType" placeholder="请选择用户类型" style="width: 100%">
            <el-option v-for="o in userTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.id === 0" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="formData.department" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="formData.position" />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-select v-model="formData.currentStatus" placeholder="请选择用户状态" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="pwdDialogVisible"
      title="重置密码"
      width="500px"
    >
      <el-form
        ref="pwdFormRef"
        :model="pwdFormData"
        :rules="pwdFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名称">
          <el-input v-model="pwdFormData.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdFormData.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="resetPassword">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
}
</style>