<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { getAgentsList, createAgents, updateAgents, deleteAgents } from '@/api/bases/agents'
import type { Agents } from '@/types/bases/agents'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<Agents>('master.agents', {
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
const formData = ref<Agents>({
  id: undefined,
  agentCode: '',
  companyName: '',
  shortName: '',
  principal: '',
  contactPerson: '',
  phone: '',
  address: '',
  email: '',
  businessLicense: '',
  registeredCapital: undefined,
  defaultTaxpayerId: '',
  defaultBankName: '',
  defaultBankAccount: '',
  defaultAddress: ''
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    }
    const res = await getAgentsList(params)
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
  formTitle.value = '新增代理公司'
  isEdit.value = false
  formData.value = {
    id: undefined,
    agentCode: '',
    companyName: '',
    shortName: '',
    principal: '',
    contactPerson: '',
    phone: '',
    address: '',
    email: '',
    businessLicense: '',
    registeredCapital: undefined,
    defaultTaxpayerId: '',
    defaultBankName: '',
    defaultBankAccount: '',
    defaultAddress: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: Agents) => {
  formTitle.value = '编辑代理公司'
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: Agents) => {
  try {
    await ElMessageBox.confirm('确认删除该代理公司吗？', '提示', {
      type: 'warning'
    })
    
    const res = await deleteAgents(row.id!)
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
      res = await updateAgents(formData.value.id!, formData.value)
    } else {
      res = await createAgents(formData.value)
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
  <div class="agent-master-view page-container">
    <div class="page-header">
      <h2 class="page-title">代理管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
    </div>
    
    <div class="table-container glass-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="公司名称/代码" style="width: 200px" />
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
        <el-table-column prop="agentCode" label="代理代码" />
        <el-table-column prop="companyName" label="公司名称" />
        <el-table-column prop="shortName" label="简称" />
        <el-table-column prop="principal" label="负责人" />
        <el-table-column prop="contactPerson" label="联系人" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="email" label="邮箱" />
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
        <el-form-item label="代理代码">
          <el-input v-model="formData.agentCode" />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="formData.companyName" />
        </el-form-item>
        <el-form-item label="公司简称">
          <el-input v-model="formData.shortName" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="formData.principal" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="formData.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="formData.address" type="textarea" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="营业执照号">
          <el-input v-model="formData.businessLicense" />
        </el-form-item>
        <el-form-item label="注册资本">
          <el-input-number v-model="formData.registeredCapital" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="纳税人识别号">
          <el-input v-model="formData.defaultTaxpayerId" />
        </el-form-item>
        <el-form-item label="开户银行">
          <el-input v-model="formData.defaultBankName" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="formData.defaultBankAccount" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-input v-model="formData.defaultAddress" type="textarea" />
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
.agent-master-view { 
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