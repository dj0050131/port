<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { AgentInvoiceInfo } from '@/types/bases/agentInvoiceInfo'
import {
  getAgentInvoiceInfoList,
  createAgentInvoiceInfo,
  updateAgentInvoiceInfo,
  deleteAgentInvoiceInfo
} from '@/api/bases/agentInvoiceInfo'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<AgentInvoiceInfo>('billing.payer', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const currentPayer = reactive<AgentInvoiceInfo>({
  id: 0,
  agentId: 0,
  voyageId: 0,
  agentName: '',
  payingUnitName: '',
  invoiceType: '普通发票',
  taxpayerId: '',
  payingUnitAddress: '',
  payingUnitPhone: '',
  payingUnitBank: '',
  payingUnitBankAccount: '',
  billingVoucherId: 0,
  createdAt: '',
  updatedAt: ''
})

const formRules = {
  agentName: [{ required: true, message: '请选择代理公司', trigger: 'change' }],
  payingUnitName: [{ required: true, message: '请输入付款单位名称', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }]
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getAgentInvoiceInfoList()
    if (res.code === 200) {
      tableState.items = res.data || []
    } else {
      ElMessage.error('获取数据失败: ' + res.message)
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const handleCreate = () => {
  Object.assign(currentPayer, {
    id: 0,
    agentId: 0,
    voyageId: 0,
    agentName: '',
    payingUnitName: '',
    invoiceType: '普通发票',
    taxpayerId: '',
    payingUnitAddress: '',
    payingUnitPhone: '',
    payingUnitBank: '',
    payingUnitBankAccount: '',
    billingVoucherId: 0,
    createdAt: '',
    updatedAt: ''
  })
  dialogTitle.value = '新增付款单位'
  dialogVisible.value = true
}

const handleEdit = (row: AgentInvoiceInfo) => {
  Object.assign(currentPayer, row)
  dialogTitle.value = '修改付款单位'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    let res
    if (currentPayer.id === 0) {
      res = await createAgentInvoiceInfo(currentPayer)
    } else {
      const { id, ...updateData } = currentPayer
      res = await updateAgentInvoiceInfo(id, updateData)
    }
    
    if (res.code === 200) {
      ElMessage.success(currentPayer.id === 0 ? '新增成功' : '修改成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error((currentPayer.id === 0 ? '新增失败: ' : '修改失败: ') + res.message)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row: AgentInvoiceInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除付款单位"${row.payingUnitName}"吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await deleteAgentInvoiceInfo(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error('删除失败: ' + res.message)
    }
  } catch (error) {
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="payer-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">付款单位管理</h2>
        <p class="page-desc">管理引航费用的付款单位及开票信息</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
        <el-button type="success" :icon="Plus" @click="handleCreate">新增</el-button>
      </div>
    </div>

    <div class="table-container glass-card">
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
          <el-table-column type="selection" width="40" />
          <el-table-column prop="agentName" label="代理公司" width="120" />
          <el-table-column prop="payingUnitName" label="付款单位名称" min-width="180" />
          <el-table-column prop="invoiceType" label="默认发票类型" width="140" />
          <el-table-column prop="taxpayerId" label="付款单位纳税人识别号" width="180" />
          <el-table-column prop="payingUnitAddress" label="付款单位地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="payingUnitPhone" label="付款单位电话" width="140" />
          <el-table-column prop="payingUnitBank" label="付款单位开户银行" width="160" />
          <el-table-column prop="payingUnitBankAccount" label="付款单位银行账号" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
       </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      class="billing-dialog"
    >
      <div style="padding: 20px;">
        <el-form 
          ref="formRef"
          :model="currentPayer" 
          :rules="formRules"
          label-width="120px" 
          size="default"
        >
          <el-form-item label="代理公司:" prop="agentName">
            <el-select v-model="currentPayer.agentName" style="width: 100%">
               <el-option label="温州港代理" value="温州港代理" />
               <el-option label="外代" value="外代" />
            </el-select>
          </el-form-item>
          <el-form-item label="付款单位名称:" prop="payingUnitName">
            <el-input v-model="currentPayer.payingUnitName" />
          </el-form-item>
          <el-form-item label="发票类型:" prop="invoiceType">
            <el-select v-model="currentPayer.invoiceType" style="width: 100%">
               <el-option label="增值税专用发票" value="增值税专用发票" />
               <el-option label="普通发票" value="普通发票" />
            </el-select>
          </el-form-item>
          <el-form-item label="纳税人识别号:">
            <el-input v-model="currentPayer.taxpayerId" />
          </el-form-item>
          <el-form-item label="付款单位地址:">
            <el-input v-model="currentPayer.payingUnitAddress" />
          </el-form-item>
          <el-form-item label="付款单位电话:">
            <el-input v-model="currentPayer.payingUnitPhone" />
          </el-form-item>
          <el-form-item label="付款单位开户行:">
            <el-input v-model="currentPayer.payingUnitBank" />
          </el-form-item>
          <el-form-item label="付款单位银行账号:">
            <el-input v-model="currentPayer.payingUnitBankAccount" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.payer-view {
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