<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Check, Printer, Delete, Refresh } from '@element-plus/icons-vue'
import { 
  fetchTransferredBillingVouchers,
  fetchUntransferredBillingVouchers,
  fetchUntransferredTaxRecords,
  transferBillingVouchers,
  transferTaxRecords,
  retryTransferWriteOffs,
  retryTransferTaxWriteOffs
} from '@/api/charge'
import { 
  fetchFeeWriteOffs, 
  getFeeWriteOffById, 
  updateFeeWriteOff, 
  deleteFeeWriteOff,
  deleteFeeWriteOffBatch
} from '@/api/writeoffs'
import { fetchAgentsList } from '@/api/bases/agents'
import request from '@/utils/request'
import type { BillingResultVO } from '@/types/billingVouchers'
import type { PageQueryParams } from '@/api/charge'
import type { TransferRequest } from '@/types/charge'
import type { FeeWriteOffs } from '@/types/writeoffs'
import type { Agents } from '@/types/bases/agents'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<FeeWriteOffs>('billing.transfer', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const detailState = dataCenterStore.ensureTable<BillingResultVO>('billing.transfer.detail', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const voucherData = ref<BillingResultVO[]>([])
const taxData = ref<BillingResultVO[]>([])
const activeBottomTab = ref('vouchers')
const selectedWriteOffRows = ref<FeeWriteOffs[]>([]) // 顶部“交接单”表格选择
const canRetrySelectedWriteOffs = computed(() => {
  if (selectedWriteOffRows.value.length === 0) return false
  return selectedWriteOffRows.value.some(r => {
    const st = String(r.statusCode || '').toUpperCase()
    return st === 'FAILED' || st === '' || st === 'PENDING'
  })
})
const selectedDetailRows = ref<BillingResultVO[]>([]) // 底部“待移交”表格选择（用于移交）
const currentWriteOff = ref<FeeWriteOffs | null>(null)

const transferDialogVisible = ref(false)
const transferForm = reactive({
  transferTarget: '',
  transferType: ''
})
const transferLoading = ref(false)

const createTransferDialogVisible = ref(false)
const createTransferForm = reactive({
  transferTarget: '',
  transferType: ''
})
const selectedUntransferredRows = ref<BillingResultVO[]>([])
const createTransferLoading = ref(false)

const editWriteOffDialogVisible = ref(false)
const editWriteOffForm = reactive<FeeWriteOffs>({
  writeOffCode: '',
  agentId: undefined,
  writeOffAmount: undefined,
  writeOffType: '',
  writeOffPerson: '',
  writeOffDate: '',
  statusCode: '',
  statusName: ''
})
const editWriteOffLoading = ref(false)

const agents = ref<Agents[]>([])
const agentMap = computed<Record<number, Agents>>(() => {
  const m: Record<number, Agents> = {}
  for (const a of agents.value) {
    if (a?.id) m[a.id] = a
  }
  return m
})
const getAgentName = (agentId: any) => {
  const id = Number(agentId)
  if (!Number.isFinite(id)) return '-'
  return agentMap.value[id]?.companyName || `#${id}`
}

const searchForm = reactive({
  page: tableState.page,
  size: tableState.pageSize
})

const fetchTransferredData = async () => {
  tableState.loading = true
  try {
    const params: PageQueryParams = {
      current: searchForm.page,
      size: searchForm.size
    }
    
    const response = await fetchFeeWriteOffs(params)
    
    if (response.code === 200) {
      tableState.items = response.data.records || []
      tableState.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    tableState.loading = false
  }
}

const fetchUntransferredData = async () => {
  detailState.loading = true
  try {
    const params: PageQueryParams = {
      current: searchForm.page,
      size: searchForm.size
    }
    
    const response = await fetchUntransferredBillingVouchers(params)
    
    if (response.code === 200) {
      voucherData.value = response.data.records || []
      detailState.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    detailState.loading = false
  }
}

const fetchUntransferredTaxRecordsData = async () => {
  detailState.loading = true
  try {
    const params: PageQueryParams = {
      current: searchForm.page,
      size: searchForm.size
    }
    
    const response = await fetchUntransferredTaxRecords(params)
    
    if (response.code === 200) {
      taxData.value = response.data.records || []
      detailState.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    detailState.loading = false
  }
}

const fetchData = async () => {
  await Promise.all([
    fetchTransferredData(),
    fetchUntransferredData(),
    fetchUntransferredTaxRecordsData()
  ])
}

const handleWriteOffSelectionChange = (rows: FeeWriteOffs[]) => {
  selectedWriteOffRows.value = rows
}

const handleWriteOffRowClick = (row: FeeWriteOffs) => {
  currentWriteOff.value = row
}

const handleUntransferredSelectionChange = (rows: BillingResultVO[]) => {
  selectedDetailRows.value = rows
}

const handleCreateTransferSelectionChange = (rows: BillingResultVO[]) => {
  selectedUntransferredRows.value = rows
}

const hasAnyDetailRows = computed(() => {
  if (activeBottomTab.value === 'taxRecords') return (taxData.value?.length || 0) > 0
  return (voucherData.value?.length || 0) > 0
})

const getTaxStatusText = (status: any) => {
  const s = String(status || '').toUpperCase()
  const map: Record<string, string> = {
    CALCULATED: '已计算',
    APPROVED: '已审核',
    TRANSFER_PENDING: '移交待处理',
    TRANSFERRED: '已移交',
  }
  return map[s] || (s || '-')
}

const getTaxStatusTagType = (status: any) => {
  const s = String(status || '').toUpperCase()
  const map: Record<string, string> = {
    CALCULATED: 'info',
    APPROVED: 'success',
    TRANSFER_PENDING: 'warning',
    TRANSFERRED: 'success',
  }
  return map[s] || 'info'
}

const getWriteOffStatusTagType = (statusCode: any) => {
  const s = String(statusCode || '')
  if (s === 'FAILED') return 'danger'
  if (s === 'PROCESSING') return 'warning'
  if (s === 'COMPLETED') return 'success'
  return 'info'
}

const getWriteOffStatusText = (statusCode: any, statusName?: any) => {
  if (statusName) return String(statusName)
  const s = String(statusCode || '').toUpperCase()
  const map: Record<string, string> = {
    PROCESSING: '处理中',
    COMPLETED: '已移交',
    FAILED: '移交失败',
    PENDING: '未处理'
  }
  return map[s] || (s || '-')
}

const suggestTransferTarget = (rows: Array<any>) => {
  const agentNames = Array.from(new Set((rows || []).map(r => String(r?.agentName || '').trim()).filter(Boolean)))
  if (agentNames.length === 1) {
    return `${agentNames[0]}（财务对接：测试联系人 13800000000）`
  }
  if (agentNames.length > 1) {
    return `港口财务中心（对接：测试联系人 13800000000）`
  }
  return `港口财务中心（对接：测试联系人 13800000000）`
}

const getTransferTypeText = (v: any) => {
  const s = String(v || '').toUpperCase()
  if (s === 'MANUAL') return '手工'
  if (s === 'AUTO') return '自动'
  if (s === 'RETRY') return '重试'
  return s || '-'
}

const safeText = (v: any) => (v === null || v === undefined ? '' : String(v))
const formatMoney = (v: any) => {
  const n = Number(v)
  if (Number.isFinite(n)) return `¥${n.toFixed(2)}`
  return safeText(v)
}
const formatDate = (v: any) => {
  if (!v) return '-'
  try {
    return new Date(v).toLocaleString()
  } catch {
    return safeText(v)
  }
}

const openPrintWindow = (title: string, htmlBody: string) => {
  const w = window.open('', '_blank')
  if (!w) {
    ElMessage.error('弹窗被浏览器拦截，请允许弹窗后重试')
    return
  }
  w.document.open()
  w.document.write(`
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <title>${title}</title>
  <style>
    body { font-family: -apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",Arial,sans-serif; padding: 24px; }
    h1 { font-size: 18px; margin: 0 0 8px; }
    .meta { color: #666; font-size: 12px; margin-bottom: 16px; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; }
    th { background: #f6f7f9; }
    .right { text-align: right; }
    .center { text-align: center; }
    .footer { margin-top: 16px; font-size: 12px; color: #666; }
    @media print { .no-print { display: none; } body { padding: 0; } }
  </style>
</head>
<body>
  <div class="no-print" style="margin-bottom:12px;">
    <button onclick="window.print()">打印</button>
  </div>
  ${htmlBody}
</body>
</html>
  `)
  w.document.close()
}

const handlePrintSummary = () => {
  const rows = selectedWriteOffRows.value.length > 0 ? selectedWriteOffRows.value : (currentWriteOff.value ? [currentWriteOff.value] : [])
  if (rows.length === 0) {
    ElMessage.warning('请先在上方选择交接单（或点击一行高亮）')
    return
  }

  const totalAmount = rows.reduce((s, r) => s + Number(r.writeOffAmount || 0), 0)
  const body = `
    <h1>费用移交汇总</h1>
    <div class="meta">打印时间：${new Date().toLocaleString()}　|　交接单数量：${rows.length}　|　合计：${formatMoney(totalAmount)}</div>
    <table>
      <thead>
        <tr>
          <th>交接单编号</th>
          <th>船代ID</th>
          <th>移交类型</th>
          <th>经办人</th>
          <th>状态</th>
          <th>移交日期</th>
          <th class="right">上交总额</th>
        </tr>
      </thead>
      <tbody>
        ${rows.map(r => `
          <tr>
            <td>${safeText(r.writeOffCode)}</td>
            <td>${safeText(r.agentId)}</td>
            <td>${safeText(getTransferTypeText(r.writeOffType))}</td>
            <td>${safeText(r.writeOffPerson)}</td>
            <td>${safeText(r.statusName || r.statusCode)}</td>
            <td>${formatDate(r.writeOffDate)}</td>
            <td class="right">${formatMoney(r.writeOffAmount)}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
    <div class="footer">备注：此汇总为系统打印预览（可用于线下盖章归档）。</div>
  `

  openPrintWindow('费用移交汇总', body)
}

// 打印明细：默认打印“底部待移交列表”（优先打印选中行，否则打印当前页全部）
const handlePrintDetail = () => {
  if (!hasAnyDetailRows.value) {
    if (selectedWriteOffRows.value.length > 0 || currentWriteOff.value) {
      ElMessage.warning('下方“待移交列表”暂无数据（仅展示未移交且符合条件的记录）。如需归档交接单，请使用“打印汇总”。')
    } else {
      ElMessage.warning('当前没有可打印的明细数据')
    }
    return
  }

  const isTax = activeBottomTab.value === 'taxRecords'
  const rows = (selectedDetailRows.value.length > 0 ? selectedDetailRows.value : (isTax ? taxData.value : voucherData.value)) || []
  const totalAmount = rows.reduce((s, r) => s + Number((isTax ? (r as any).totalTaxAmount : r.totalAmount) || 0), 0)

  const head = isTax
    ? `
        <tr>
          <th>税额记录ID</th>
          <th>计费凭证编号</th>
          <th>签证单号</th>
          <th>中文船名</th>
          <th>代理公司</th>
          <th class="right">计税金额</th>
          <th class="right">税额合计</th>
          <th class="center">税额状态</th>
        </tr>
      `
    : `
        <tr>
          <th>凭证编号</th>
          <th>签证单号</th>
          <th>中文船名</th>
          <th>代理公司</th>
          <th class="right">合计</th>
          <th class="center">审核状态</th>
          <th class="center">支付状态</th>
        </tr>
      `

  const body = `
    <h1>费用移交明细（${isTax ? '税额记录' : '计费凭证'}）</h1>
    <div class="meta">打印时间：${new Date().toLocaleString()}　|　条数：${rows.length}　|　合计：${formatMoney(totalAmount)}</div>
    <table>
      <thead>
        ${head}
      </thead>
      <tbody>
        ${rows.map(r => isTax ? `
          <tr>
            <td>${safeText((r as any).taxRecordId ?? '-')}</td>
            <td>${safeText((r as any).voucherCode ?? (r as any).voucherId ?? '-')}</td>
            <td>${safeText((r as any).certificateCode ?? '-')}</td>
            <td>${safeText((r as any).vesselNameZh ?? '-')}</td>
            <td>${safeText((r as any).agentName ?? '-')}</td>
            <td class="right">${formatMoney((r as any).taxableAmount)}</td>
            <td class="right">${formatMoney((r as any).totalTaxAmount)}</td>
            <td class="center">${safeText(getTaxStatusText((r as any).taxStatus))}</td>
          </tr>
        ` : `
          <tr>
            <td>${safeText((r as any).voucherCode ?? '-')}</td>
            <td>${safeText((r as any).certificateCode ?? '-')}</td>
            <td>${safeText((r as any).vesselNameZh ?? '-')}</td>
            <td>${safeText((r as any).agentName ?? '-')}</td>
            <td class="right">${formatMoney((r as any).totalAmount)}</td>
            <td class="center">${safeText((r as any).auditStatus ?? '-')}</td>
            <td class="center">${safeText((r as any).paymentStatus ?? '-')}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
    <div class="footer">说明：明细默认打印当前页；如需只打印部分，请先勾选要打印的行。</div>
  `

  openPrintWindow('费用移交明细', body)
}

const handleCreateTransfer = async () => {
  createTransferForm.transferTarget = suggestTransferTarget(voucherData.value as any[])
  createTransferForm.transferType = 'MANUAL'
  selectedUntransferredRows.value = []
  
  createTransferDialogVisible.value = true
}

const submitCreateTransfer = async () => {
  if (!createTransferForm.transferTarget.trim()) {
    ElMessage.error('请输入移交目标')
    return
  }
  if (!createTransferForm.transferType.trim()) {
    ElMessage.error('请输入移交类型')
    return
  }
  
  if (selectedUntransferredRows.value.length === 0) {
    ElMessage.error('请至少选择一条未移交凭证')
    return
  }
  
  createTransferLoading.value = true
  
  try {
    // 新建交接单 = 调用后端“移交计费凭证”主流程（会更新凭证移交状态，并在后端写入核销单）
    const ids = selectedUntransferredRows.value.map(row => row.voucherId!).filter(id => id)
    const data: TransferRequest = {
      transferTarget: createTransferForm.transferTarget.trim(),
      transferType: createTransferForm.transferType.trim()
    }
    const response = await transferBillingVouchers(ids, data)
    if (response.code === 200) {
      ElMessage.success('新建交接单并移交成功')
      createTransferDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(`新建交接单失败: ${response.message}`)
    }
  } catch (error: any) {
    console.error('新建交接单失败:', error)
    ElMessage.error('新建交接单失败')
  } finally {
    createTransferLoading.value = false
  }
}

const handleEditTransfer = async (row: FeeWriteOffs) => {
  try {
    const response = await getFeeWriteOffById(row.id!)
    if (response.code === 200 && response.data) {
      Object.assign(editWriteOffForm, response.data)
      if (!editWriteOffForm.writeOffType) {
        editWriteOffForm.writeOffType = 'MANUAL'
      }
      editWriteOffDialogVisible.value = true
    } else {
      ElMessage.error(`获取交接单详情失败: ${response.message}`)
    }
  } catch (error) {
    console.error('获取交接单详情失败:', error)
    ElMessage.error('获取交接单详情失败')
  }
}

const submitEditTransfer = async () => {
  if (!editWriteOffForm.id) {
    ElMessage.error('无效的交接单')
    return
  }
  
  editWriteOffLoading.value = true
  try {
    const payload: Partial<FeeWriteOffs> = {
      agentId: editWriteOffForm.agentId,
      writeOffAmount: editWriteOffForm.writeOffAmount,
      writeOffType: editWriteOffForm.writeOffType,
      writeOffPerson: editWriteOffForm.writeOffPerson,
      writeOffDate: editWriteOffForm.writeOffDate
    }
    const response = await updateFeeWriteOff(editWriteOffForm.id, payload)
    
    if (response.code === 200) {
      ElMessage.success('更新交接单成功')
      editWriteOffDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(`更新交接单失败: ${response.message}`)
    }
  } catch (error) {
    console.error('更新交接单失败:', error)
    ElMessage.error('更新交接单失败')
  } finally {
    editWriteOffLoading.value = false
  }
}

const handleDeleteTransfer = async (row: FeeWriteOffs) => {
  if (!row.id) {
    ElMessage.error('无效的交接单')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该交接单吗？此操作不可恢复。', '删除确认', {
      type: 'warning'
    })
    
    const response = await deleteFeeWriteOff(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除交接单成功')
      fetchData()
    } else {
      ElMessage.error(`删除交接单失败: ${response.message}`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除交接单失败:', error)
      ElMessage.error('删除交接单失败')
    }
  }
}

const handleDeleteSelectedTransfers = async () => {
  const ids = selectedWriteOffRows.value.map(r => r.id).filter((id): id is number => !!id)
  if (ids.length === 0) {
    ElMessage.warning('请先在上方勾选要删除的交接单')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 条交接单吗？此操作不可恢复。`, '删除确认', { type: 'warning' })
    const res = await deleteFeeWriteOffBatch(ids)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      selectedWriteOffRows.value = []
      currentWriteOff.value = null
      fetchData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (e) {
    if (e === 'cancel') return
    console.error(e)
    ElMessage.error('批量删除失败')
  }
}

const handleRetrySelectedTransfers = async () => {
  const rows = selectedWriteOffRows.value || []
  if (rows.length === 0) {
    ElMessage.warning('请先在上方勾选要重试的交接单')
    return
  }
  const idsWriteoff: number[] = []
  const idsTax: number[] = []
  for (const r of rows) {
    if (!r.id) continue
    const vn = String(r.voucherNumber || '')
    if (vn.startsWith('TAX-')) {
      idsTax.push(r.id)
    } else {
      idsWriteoff.push(r.id)
    }
  }
  if (idsWriteoff.length === 0 && idsTax.length === 0) {
    ElMessage.warning('所选交接单无法识别类型')
    return
  }
  try {
    await ElMessageBox.confirm('确定要重试移交选中的交接单吗？', '重试确认', { type: 'warning' })
    const data: TransferRequest = { transferTarget: transferForm.transferTarget || 'SYSTEM_RETRY', transferType: 'RETRY' }
    // 分类型重试
    if (idsWriteoff.length > 0) {
      const res = await retryTransferWriteOffs(idsWriteoff, data)
      if (res.code !== 200) {
        ElMessage.error(res.message || '重试移交失败')
        return
      }
    }
    if (idsTax.length > 0) {
      const res = await retryTransferTaxWriteOffs(idsTax, data)
      if (res.code !== 200) {
        ElMessage.error(res.message || '重试移交失败')
        return
      }
    }
    ElMessage.success('已提交重试，等待回调更新状态')
    fetchData()
  } catch (e) {
    if (e === 'cancel') return
    console.error(e)
    ElMessage.error('重试移交失败')
  }
}

const handleSingleTransfer = async (row: BillingResultVO) => {
  selectedDetailRows.value = [row]
  transferForm.transferTarget = suggestTransferTarget([row])
  transferForm.transferType = 'MANUAL'
  transferDialogVisible.value = true
}

const handleTransfer = async () => {
  if (selectedDetailRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  transferForm.transferTarget = suggestTransferTarget(selectedDetailRows.value as any[])
  transferForm.transferType = 'MANUAL'
  
  transferDialogVisible.value = true
}

// 底部“新增/取消计费凭证”按钮（原本无事件，导致点击无效果）
const handleAddVoucher = () => {
  handleCreateTransfer()
}
const handleCancelVoucherSelection = () => {
  selectedDetailRows.value = []
  ElMessage.success('已清空选择')
}

const submitTransfer = async () => {
  if (!transferForm.transferTarget.trim()) {
    ElMessage.error('请输入移交目标')
    return
  }
  if (!transferForm.transferType.trim()) {
    ElMessage.error('请输入移交类型')
    return
  }
  
  transferLoading.value = true
  
  try {
    if (activeBottomTab.value === 'taxRecords') {
      const ids = selectedDetailRows.value.map(row => row.taxRecordId || row.voucherId!).filter(id => id)
      const data: TransferRequest = {
        transferTarget: transferForm.transferTarget.trim(),
        transferType: transferForm.transferType.trim()
      }
      
      const response = await transferTaxRecords(ids, data)
      
      if (response.code !== 200) {
        ElMessage.error(`批量移交失败: ${response.message}`)
        return
      }
    } else {
      const ids = selectedDetailRows.value.map(row => row.voucherId!).filter(id => id)
      const data: TransferRequest = {
        transferTarget: transferForm.transferTarget.trim(),
        transferType: transferForm.transferType.trim()
      }
      
      const response = await transferBillingVouchers(ids, data)
      
      if (response.code !== 200) {
        ElMessage.error(`批量移交失败: ${response.message}`)
        return
      }
    }
    
    ElMessage.success('移交成功')
    transferDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移交失败:', error)
      ElMessage.error('移交失败')
    }
  } finally {
    transferLoading.value = false
  }
}

const getAuditStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getAuditStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已审核'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

const getPaymentStatusType = (status: string) => {
  switch (status) {
    case 'UNPAID': return 'warning'
    case 'PARTIAL_PAID': return 'primary'
    case 'FULLY_PAID': return 'success'
    default: return 'info'
  }
}

const getPaymentStatusText = (status: string) => {
  switch (status) {
    case 'UNPAID': return '未支付'
    case 'PARTIAL_PAID': return '部分支付'
    case 'FULLY_PAID': return '已支付'
    default: return status
  }
}

onMounted(() => {
  fetchAgentsList().then(res => {
    if (res.code === 200) {
      agents.value = res.data || []
    }
  }).catch(() => {})
  fetchData()
})
</script>

<template>
  <div class="transfer-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">费用移交</h2>
        <p class="page-desc">引航费用月度移交与汇总管理</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="transfer-list">
        <div class="toolbar">
           <el-button type="primary" :icon="Search" @click="fetchTransferredData">查询</el-button>
           <el-button type="success" :icon="Plus" @click="handleCreateTransfer">新建交接单</el-button>
           <el-button type="warning" :icon="Edit" :disabled="!currentWriteOff" @click="currentWriteOff && handleEditTransfer(currentWriteOff)">编辑</el-button>
           <el-button type="success" plain :icon="Check" @click="handleTransfer">移交</el-button>
           <el-button type="warning" plain :icon="Refresh" :disabled="!canRetrySelectedWriteOffs" @click="handleRetrySelectedTransfers">重试移交</el-button>
           <el-button :icon="Printer" @click="handlePrintSummary">打印汇总</el-button>
           <el-button :icon="Printer" @click="handlePrintDetail">打印明细</el-button>
           <el-button type="danger" :icon="Delete" :disabled="selectedWriteOffRows.length === 0" @click="handleDeleteSelectedTransfers">删除</el-button>
        </div>
        <el-table 
          :data="tableState.items" 
          v-loading="tableState.loading" 
          stripe 
          height="300px" 
          size="small"
          highlight-current-row
          @selection-change="handleWriteOffSelectionChange"
          @row-click="handleWriteOffRowClick"
          @row-dblclick="handleEditTransfer"
        >
           <el-table-column type="selection" width="40" />
           <el-table-column prop="writeOffCode" label="交接单名称" min-width="180" />
           <el-table-column prop="agentId" label="代理公司" min-width="160" show-overflow-tooltip>
             <template #default="{ row }">
               {{ getAgentName(row.agentId) }}
             </template>
           </el-table-column>
           <el-table-column prop="voucherNumber" label="关联凭证/税额" width="140" show-overflow-tooltip />
           <el-table-column prop="writeOffAmount" label="上交总额" width="120" :formatter="(row) => `¥${row.writeOffAmount?.toFixed(2)}`" />
           <el-table-column prop="writeOffType" label="移交类型" width="120">
             <template #default="{ row }">
               {{ getTransferTypeText(row.writeOffType) }}
             </template>
           </el-table-column>
           <el-table-column prop="writeOffPerson" label="经办人" width="100" />
           <el-table-column prop="statusName" label="状态" width="100">
              <template #default="{ row }">
                 <el-tag size="small" :type="getWriteOffStatusTagType(row.statusCode)">
                   {{ row.statusName || row.statusCode || '-' }}
                 </el-tag>
              </template>
           </el-table-column>
           <el-table-column prop="notes" label="备注/回执" min-width="200" show-overflow-tooltip />
           <el-table-column prop="auditor" label="审核人" width="100" show-overflow-tooltip />
           <el-table-column prop="auditTime" label="审核时间" width="140">
             <template #default="{ row }">
               {{ formatDate(row.auditTime) }}
             </template>
           </el-table-column>
           <el-table-column prop="financeConfirmer" label="财务确认人" width="110" show-overflow-tooltip />
           <el-table-column prop="financeConfirmTime" label="财务确认时间" width="140">
             <template #default="{ row }">
               {{ formatDate(row.financeConfirmTime) }}
             </template>
           </el-table-column>
           <el-table-column prop="bankReferenceNumber" label="银行流水号" width="150" show-overflow-tooltip />
           <el-table-column prop="writeOffDate" label="移交日期" width="140">
             <template #default="{ row }">
               {{ row.writeOffDate ? new Date(row.writeOffDate).toLocaleDateString() : '-' }}
             </template>
           </el-table-column>
           <el-table-column prop="createdAt" label="创建时间" width="140">
             <template #default="{ row }">
               {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
             </template>
           </el-table-column>
           <el-table-column label="操作" width="150">
             <template #default="{ row }">
               <el-button link type="primary" size="small" @click="handleEditTransfer(row)">编辑</el-button>
               <el-button link type="danger" size="small" @click="handleDeleteTransfer(row)">删除</el-button>
             </template>
           </el-table-column>
        </el-table>
      </div>

      <!-- Bottom: Details -->
      <div class="transfer-details">
         <el-tabs v-model="activeBottomTab" class="detail-tabs">
            <el-tab-pane label="待移交计费凭证" name="vouchers">
               <div class="sub-toolbar">
                 <el-button link type="primary" size="small" @click="handleAddVoucher">新增计费凭证</el-button>
                 <el-button link type="danger" size="small" @click="handleCancelVoucherSelection">取消计费凭证</el-button>
               </div>
               <el-table 
                 :data="voucherData" 
                 stripe 
                 height="calc(100vh - 450px)" 
                 size="small"
                 @selection-change="handleUntransferredSelectionChange"
                 border
               >
                  <el-table-column type="selection" width="40" />
                  <el-table-column prop="voucherCode" label="凭证编号" width="140" fixed="left" />
                  <el-table-column prop="planCode" label="计划编号" width="140" />
                  <el-table-column prop="certificateCode" label="签证单号" width="140" />
                  <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
                  <el-table-column prop="vesselNameEn" label="英文船名" width="140" show-overflow-tooltip />
                  <el-table-column prop="agentName" label="代理公司" width="120" />
                  <el-table-column prop="netTonnage" label="净吨" width="90" align="right" />
                  <el-table-column prop="baseAmount" label="基本费" width="100" align="right">
                    <template #default="{ row }">
                      ¥{{ row.baseAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="surchargeAmount" label="附加费" width="100" align="right">
                    <template #default="{ row }">
                      ¥{{ row.surchargeAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="discountAmount" label="折扣" width="100" align="right">
                    <template #default="{ row }">
                      ¥{{ row.discountAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="totalAmount" label="合计" width="100" align="right">
                    <template #default="{ row }">
                      ¥{{ row.totalAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="auditStatus" label="审核状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getAuditStatusType(row.auditStatus)" size="small">
                        {{ getAuditStatusText(row.auditStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="paymentStatus" label="支付状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getPaymentStatusType(row.paymentStatus)" size="small">
                        {{ getPaymentStatusText(row.paymentStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createdAt" label="创建时间" width="140">
                    <template #default="{ row }">
                      {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="handleSingleTransfer(row)">移交</el-button>
                    </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>
            
            <el-tab-pane label="待移交税额记录" name="taxRecords">
               <div class="sub-toolbar">
                 <el-button link type="primary" size="small" @click="handleAddVoucher">新增计费凭证</el-button>
                 <el-button link type="danger" size="small" @click="handleCancelVoucherSelection">取消计费凭证</el-button>
               </div>
               <el-table 
                 :data="taxData" 
                 stripe 
                 height="calc(100vh - 450px)" 
                 size="small"
                 @selection-change="handleUntransferredSelectionChange"
                 border
               >
                  <el-table-column type="selection" width="40" />
                  <el-table-column prop="taxRecordCode" label="税额记录编号" width="140" fixed="left" />
                  <el-table-column prop="voucherCode" label="凭证编号" width="140" />
                  <el-table-column prop="planCode" label="计划编号" width="140" />
                  <el-table-column prop="certificateCode" label="签证单号" width="140" />
                  <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
                  <el-table-column prop="agentName" label="代理公司" width="120" />
                  <el-table-column prop="taxableAmount" label="计税金额" width="120" align="right">
                    <template #default="{ row }">
                      ¥{{ row.taxableAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="totalTaxAmount" label="总税额" width="120" align="right">
                    <template #default="{ row }">
                      ¥{{ row.totalTaxAmount?.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="taxStatus" label="税额状态" width="100">
                    <template #default="{ row }">
                      <el-tag size="small" :type="getTaxStatusTagType(row.taxStatus)">
                        {{ getTaxStatusText(row.taxStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createdAt" label="创建时间" width="140">
                    <template #default="{ row }">
                      {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="handleSingleTransfer(row)">移交</el-button>
                    </template>
                  </el-table-column>
               </el-table>
            </el-tab-pane>
         </el-tabs>
      </div>
    </div>
    
    <el-dialog v-model="transferDialogVisible" title="移交确认" width="400px">
      <el-form :model="transferForm" label-width="80px" size="small">
        <el-form-item label="移交目标" required>
          <el-input v-model="transferForm.transferTarget" placeholder="请输入移交目标" />
        </el-form-item>
        <el-form-item label="移交类型" required>
          <el-select v-model="transferForm.transferType" style="width: 100%" placeholder="请选择">
            <el-option label="手工(MANUAL)" value="MANUAL" />
            <el-option label="自动(AUTO)" value="AUTO" />
            <el-option label="重试(RETRY)" value="RETRY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="transferLoading" @click="submitTransfer">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <el-dialog v-model="createTransferDialogVisible" title="新建交接单" width="800px">
      <el-form :model="createTransferForm" label-width="80px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="移交目标" required>
              <el-input v-model="createTransferForm.transferTarget" placeholder="请输入移交目标" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="移交类型" required>
              <el-select v-model="createTransferForm.transferType" style="width: 100%" placeholder="请选择">
                <el-option label="手工(MANUAL)" value="MANUAL" />
                <el-option label="自动(AUTO)" value="AUTO" />
                <el-option label="重试(RETRY)" value="RETRY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="选择凭证" required>
          <el-table 
            :data="voucherData" 
            height="300" 
            size="small"
            @selection-change="handleCreateTransferSelectionChange"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column prop="voucherCode" label="凭证编号" width="140" />
            <el-table-column prop="certificateCode" label="签证单号" width="140" />
            <el-table-column prop="vesselNameZh" label="中文船名" width="120" />
            <el-table-column prop="agentName" label="代理公司" width="120" />
            <el-table-column prop="totalAmount" label="合计金额" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.totalAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="140">
              <template #default="{ row }">
                {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createTransferDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createTransferLoading" @click="submitCreateTransfer">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <el-dialog v-model="editWriteOffDialogVisible" title="编辑交接单" width="500px">
      <el-form :model="editWriteOffForm" label-width="80px" size="small">
        <el-form-item label="交接单编号">
          <el-input v-model="editWriteOffForm.writeOffCode" disabled />
        </el-form-item>
        <el-form-item label="代理公司">
          <el-select
            v-model="editWriteOffForm.agentId"
            placeholder="请选择代理公司"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="a in agents"
              :key="a.id"
              :label="`${a.companyName}${a.shortName ? `(${a.shortName})` : ''}（${a.id}）`"
              :value="a.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="核销金额">
          <el-input-number v-model="editWriteOffForm.writeOffAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="核销类型">
          <el-select v-model="editWriteOffForm.writeOffType" style="width: 100%" placeholder="请选择">
            <el-option label="手工(MANUAL)" value="MANUAL" />
            <el-option label="自动(AUTO)" value="AUTO" />
            <el-option label="重试(RETRY)" value="RETRY" />
          </el-select>
        </el-form-item>
        <el-form-item label="经办人">
          <el-input v-model="editWriteOffForm.writeOffPerson" />
        </el-form-item>
        <el-form-item label="核销日期">
          <el-date-picker 
            v-model="editWriteOffForm.writeOffDate" 
            type="date" 
            value-format="YYYY-MM-DD" 
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag size="small" :type="getWriteOffStatusTagType(editWriteOffForm.statusCode)">
            {{ getWriteOffStatusText(editWriteOffForm.statusCode, editWriteOffForm.statusName) }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editWriteOffDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editWriteOffLoading" @click="submitEditTransfer">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.transfer-view {
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
  gap: 16px;
}

.transfer-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.transfer-details {
  flex: 1;
  border-top: 1px solid var(--tidal-border);
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  min-height: 0; /* Allow flex shrink */
}

.toolbar { 
  margin-bottom: 12px; 
  
  .el-button {
    margin-right: 8px;
  }
}
.sub-toolbar { margin-bottom: 8px; }

.detail-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-tabs__content) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  :deep(.el-tab-pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}
</style>