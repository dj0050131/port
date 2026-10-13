<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { Search, Download, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchPilotWorksheetRecords,
  exportPilotWorksheetRecords,
  fetchDriverWorksheetRecords,
  exportDriverWorksheetRecords,
  fetchVehicleWorksheetRecords,
  exportVehicleWorksheetRecords,
  fetchTugboatWorksheetRecords,
  exportTugboatWorksheetRecords,
  fetchPilotBoatWorksheetRecords,
  exportPilotBoatWorksheetRecords,
  fetchWorkSheetLedgerPage,
  createWorkSheetLedger,
  updateWorkSheetLedger,
  deleteWorkSheetLedger,
  syncWorkSheetLedgerFromPlans,
} from '@/api/worksheet'
import { fetchDispatchDetail } from '@/api/dispatch'
import type {
  WorksheetPilotRecord,
  WorksheetDriverRecord,
  WorksheetVehicleRecord,
  WorksheetTugboatRecord,
  WorksheetPilotBoatRecord,
  WorkSheetLedger,
  WorksheetWorkStatus,
} from '@/types/worksheet'
import { fetchPilotList } from '@/api/bases/pilots'
import { fetchDriverList } from '@/api/bases/drivers'
import type { DispatchDetail } from '@/types/dispatch'
import { useDataCenterStore } from '@/stores/dataCenter'

const activeTab = ref('pilot')
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<any>('pilotage.worksheet', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const searchForm = reactive({
  keyword: '',
  dateRange: [] as any[],
  workStatus: '' as any,
})
const pagination = reactive({
  page: tableState.page,
  size: tableState.pageSize
})

const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailRow = ref<any>(null)
const dispatchDetail = ref<DispatchDetail | null>(null)

const openDetail = async (row: any) => {
  detailRow.value = row
  dispatchDetail.value = null
  detailDrawerVisible.value = true

  if (!row?.planId) return
  try {
    detailLoading.value = true
    const res = await fetchDispatchDetail(row.planId)
    if (res.code === 200) {
      dispatchDetail.value = res.data
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } finally {
    detailLoading.value = false
  }
}

const ledgerDialogVisible = ref(false)
const ledgerDialogMode = ref<'create' | 'edit'>('create')
const ledgerFormLoading = ref(false)
const ledgerForm = reactive<Partial<WorkSheetLedger>>({
  workStatus: 'NOT_STARTED',
})
const pilots = ref<any[]>([])
const drivers = ref<any[]>([])
const pilotNameMap = ref<Record<number, string>>({})
const driverNameMap = ref<Record<number, string>>({})
const workStatusOptions: { label: string; value: WorksheetWorkStatus | '' }[] = [
  { label: '全部', value: '' },
  { label: '未开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '异常', value: 'ABNORMAL' },
]

const fetchData = async () => {
  tableState.loading = true
  try {
    const startDate = searchForm.dateRange?.[0] ? `${searchForm.dateRange[0]} 00:00:00` : undefined
    const endDate = searchForm.dateRange?.[1] ? `${searchForm.dateRange[1]} 23:59:59` : undefined
    const baseParams = {
      current: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword?.trim() || undefined,
      startDate,
      endDate,
    }

    if (activeTab.value === 'pilot') {
      const res = await fetchPilotWorksheetRecords(baseParams)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorksheetPilotRecord[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表数据失败')
      return
    }
    if (activeTab.value === 'driver') {
      const res = await fetchDriverWorksheetRecords(baseParams)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorksheetDriverRecord[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表数据失败')
      return
    }
    if (activeTab.value === 'vehicle') {
      const res = await fetchVehicleWorksheetRecords(baseParams)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorksheetVehicleRecord[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表数据失败')
      return
    }
    if (activeTab.value === 'tugboat') {
      const res = await fetchTugboatWorksheetRecords(baseParams)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorksheetTugboatRecord[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表数据失败')
      return
    }
    if (activeTab.value === 'boat') {
      const res = await fetchPilotBoatWorksheetRecords(baseParams)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorksheetPilotBoatRecord[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表数据失败')
      return
    }

    if (activeTab.value === 'ledger') {
      const res = await fetchWorkSheetLedgerPage({
        current: pagination.page,
        size: pagination.size,
        keyword: searchForm.keyword?.trim() || undefined,
        startDate,
        endDate,
        workStatus: (searchForm as any).workStatus || undefined,
      } as any)
      if (res.code === 200) {
        tableState.items = (res.data.records || []) as WorkSheetLedger[]
        tableState.total = res.data.total || 0
      } else ElMessage.error(res.message || '获取工作表台账失败')
      return
    }
  } finally {
    tableState.loading = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  tableState.page = 1
  if (activeTab.value === 'ledger') {
    // 自动同步一次，避免台账长期为空（可重复调用，后端按 WS-PLAN_CODE 幂等跳过）
    const startDate = searchForm.dateRange?.[0] ? `${searchForm.dateRange[0]} 00:00:00` : undefined
    const endDate = searchForm.dateRange?.[1] ? `${searchForm.dateRange[1]} 23:59:59` : undefined
    syncWorkSheetLedgerFromPlans({ startDate, endDate }).finally(() => fetchData())
    return
  }
  fetchData()
}

const handleExport = async () => {
  if (activeTab.value === 'ledger') {
    ElMessage.info('台账管理暂不支持导出（如需要我可以补 CSV 导出）')
    return
  }
  const startDate = searchForm.dateRange?.[0] ? `${searchForm.dateRange[0]} 00:00:00` : undefined
  const endDate = searchForm.dateRange?.[1] ? `${searchForm.dateRange[1]} 23:59:59` : undefined

  const exportParams = {
    keyword: searchForm.keyword?.trim() || undefined,
    startDate,
    endDate,
  }

  let resp: any
  let filenamePrefix = '工作表'
  if (activeTab.value === 'pilot') {
    resp = await exportPilotWorksheetRecords(exportParams)
    filenamePrefix = '工作表_引航员'
  } else if (activeTab.value === 'driver') {
    resp = await exportDriverWorksheetRecords(exportParams)
    filenamePrefix = '工作表_车队司机'
  } else if (activeTab.value === 'vehicle') {
    resp = await exportVehicleWorksheetRecords(exportParams)
    filenamePrefix = '工作表_车辆'
  } else if (activeTab.value === 'tugboat') {
    resp = await exportTugboatWorksheetRecords(exportParams)
    filenamePrefix = '工作表_拖轮'
  } else if (activeTab.value === 'boat') {
    resp = await exportPilotBoatWorksheetRecords(exportParams)
    filenamePrefix = '工作表_引航艇'
  } else {
    ElMessage.warning('当前tab不支持导出')
    return
  }

  const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${filenamePrefix}_${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '')}.csv`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

const loadLedgerDictionaries = async () => {
  try {
    const [pRes, dRes] = await Promise.all([fetchPilotList(), fetchDriverList()])
    pilots.value = (pRes as any).data || []
    drivers.value = (dRes as any).data || []
    pilotNameMap.value = Object.fromEntries((pilots.value || []).map((p: any) => [p.id, p.pilotName || p.name || String(p.id)]))
    driverNameMap.value = Object.fromEntries((drivers.value || []).map((d: any) => [d.id, d.driverName || d.name || String(d.id)]))
  } catch (e) {
    // 不阻塞主流程
  }
}

const openCreateLedger = () => {
  ledgerDialogMode.value = 'create'
  Object.assign(ledgerForm, {
    id: undefined,
    worksheetCode: undefined,
    planStartTime: undefined,
    planEndTime: undefined,
    startLocation: undefined,
    endLocation: undefined,
    assignedTugboats: undefined,
    assignedPilotBoats: undefined,
    assignedPilotId: undefined,
    assignedDriverId: undefined,
    boardingTime: undefined,
    leavingTime: undefined,
    actualStartTime: undefined,
    actualEndTime: undefined,
    operationTypeFlag: undefined,
    workStatus: 'NOT_STARTED',
    completionQualityScore: undefined,
    specialSituationRecord: undefined,
    customerFeedback: undefined,
  })
  ledgerDialogVisible.value = true
}

const openEditLedger = (row: WorkSheetLedger) => {
  ledgerDialogMode.value = 'edit'
  Object.assign(ledgerForm, row)
  ledgerDialogVisible.value = true
}

const submitLedgerForm = async () => {
  try {
    ledgerFormLoading.value = true
    if (ledgerDialogMode.value === 'create') {
      const res = await createWorkSheetLedger(ledgerForm)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        ledgerDialogVisible.value = false
        fetchData()
      } else ElMessage.error(res.message || '创建失败')
      return
    }
    if (!ledgerForm.id) {
      ElMessage.error('缺少ID')
      return
    }
    const res = await updateWorkSheetLedger(ledgerForm.id, ledgerForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      ledgerDialogVisible.value = false
      fetchData()
    } else ElMessage.error(res.message || '保存失败')
  } finally {
    ledgerFormLoading.value = false
  }
}

const removeLedger = async (row: WorkSheetLedger) => {
  await ElMessageBox.confirm(`确认删除工作表【${row.worksheetCode}】？`, '提示', { type: 'warning' })
  const res = await deleteWorkSheetLedger(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  } else ElMessage.error(res.message || '删除失败')
}

const syncLedger = async () => {
  const startDate = searchForm.dateRange?.[0] ? `${searchForm.dateRange[0]} 00:00:00` : undefined
  const endDate = searchForm.dateRange?.[1] ? `${searchForm.dateRange[1]} 23:59:59` : undefined
  const res = await syncWorkSheetLedgerFromPlans({ startDate, endDate })
  if (res.code === 200) {
    ElMessage.success(`同步完成：新增${res.data?.created ?? 0}，跳过${res.data?.skipped ?? 0}`)
    fetchData()
  } else {
    ElMessage.error(res.message || '同步失败')
  }
}

watch(
  () => [pagination.page, pagination.size],
  () => {
    tableState.page = pagination.page
    tableState.pageSize = pagination.size
    fetchData()
  }
)

onMounted(() => {
  loadLedgerDictionaries()
  fetchData()
})
</script>

<template>
  <div class="worksheet-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">工作表</h2>
        <p class="page-desc">引航作业人员及车辆船舶工作记录</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <el-tabs v-model="activeTab" class="worksheet-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="引航员" name="pilot">
           <div class="tab-toolbar">
             <el-form inline size="small">
               <el-form-item label="姓名">
                 <el-input v-model="searchForm.keyword" placeholder="请输入" />
               </el-form-item>
               <el-form-item label="作业时间">
                 <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
               </el-form-item>
               <el-form-item>
                  <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                  <el-button :icon="Download" @click="handleExport">导出</el-button>
               </el-form-item>
             </el-form>
           </div>
           
           <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
              <el-table-column type="selection" width="40" />
              <el-table-column prop="shipName" label="中文船名" width="120" />
              <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
              <el-table-column prop="pilotName" label="引航员" width="80" />
              <el-table-column prop="time" label="时间/日期" width="140" />
              <el-table-column prop="berth" label="泊位" min-width="120" show-overflow-tooltip />
              <el-table-column prop="dynamic" label="动态" width="60" />
              <el-table-column prop="grossTonnage" label="总吨" width="90" />
              <el-table-column prop="netTonnage" label="净吨" width="90" />
              <el-table-column prop="deadWeight" label="载重吨" width="90" />
              <el-table-column prop="length" label="船长" width="70" />
              <el-table-column prop="width" label="船宽" width="70" />
              <el-table-column prop="draft" label="吃水" width="70" />
              <el-table-column prop="nationality" label="国籍" width="100" />
              <el-table-column prop="callSign" label="呼号" width="100" />
              <el-table-column prop="imoNumber" label="IMO" width="100" />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
                </template>
              </el-table-column>
           </el-table>
           
           <el-pagination
             v-model:current-page="pagination.page"
             v-model:page-size="pagination.size"
             :total="tableState.total"
             layout="total, prev, pager, next"
             class="pagination"
           />
        </el-tab-pane>

        <el-tab-pane label="车队司机" name="driver">
          <div class="tab-toolbar">
            <el-form inline size="small">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="驾驶员/车牌/船名/计划号" />
              </el-form-item>
              <el-form-item label="作业时间">
                <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button :icon="Download" @click="handleExport">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="shipName" label="中文船名" width="120" />
            <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
            <el-table-column prop="driverName" label="驾驶员" width="100" />
            <el-table-column prop="licensePlate" label="车牌号" width="110" />
            <el-table-column prop="time" label="时间/日期" width="140" />
            <el-table-column prop="berth" label="泊位" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dynamic" label="动态" width="60" />
            <el-table-column prop="grossTonnage" label="总吨" width="90" />
            <el-table-column prop="netTonnage" label="净吨" width="90" />
            <el-table-column prop="deadWeight" label="载重吨" width="90" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="车辆" name="vehicle">
          <div class="tab-toolbar">
            <el-form inline size="small">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="车牌/驾驶员/船名/计划号" />
              </el-form-item>
              <el-form-item label="作业时间">
                <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button :icon="Download" @click="handleExport">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="shipName" label="中文船名" width="120" />
            <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
            <el-table-column prop="vehicleCode" label="车辆代码" width="120" />
            <el-table-column prop="licensePlate" label="车牌号" width="110" />
            <el-table-column prop="driverName" label="驾驶员" width="100" />
            <el-table-column prop="time" label="时间/日期" width="140" />
            <el-table-column prop="berth" label="泊位" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dynamic" label="动态" width="60" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="拖轮" name="tugboat">
          <div class="tab-toolbar">
            <el-form inline size="small">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="拖轮/呼号/船名/计划号" />
              </el-form-item>
              <el-form-item label="作业时间">
                <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button :icon="Download" @click="handleExport">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="shipName" label="中文船名" width="120" />
            <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
            <el-table-column prop="tugboatName" label="拖轮" width="140" show-overflow-tooltip />
            <el-table-column prop="tugboatCallSign" label="呼号" width="110" />
            <el-table-column prop="bollardPull" label="拖力(吨)" width="90" />
            <el-table-column prop="time" label="时间/日期" width="140" />
            <el-table-column prop="berth" label="泊位" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dynamic" label="动态" width="60" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="引航艇" name="boat">
          <div class="tab-toolbar">
            <el-form inline size="small">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="艇名/呼号/船名/计划号" />
              </el-form-item>
              <el-form-item label="作业时间">
                <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button :icon="Download" @click="handleExport">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="shipName" label="中文船名" width="120" />
            <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
            <el-table-column prop="boatName" label="引航艇" width="140" show-overflow-tooltip />
            <el-table-column prop="boatCallSign" label="呼号" width="110" />
            <el-table-column prop="time" label="时间/日期" width="140" />
            <el-table-column prop="berth" label="泊位" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dynamic" label="动态" width="60" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="台账管理(可增删改查)" name="ledger">
          <div class="tab-toolbar">
            <el-form inline size="small">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="编号/起止地点/拖轮/引航艇" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="(searchForm as any).workStatus" style="width: 140px" placeholder="请选择">
                  <el-option v-for="o in workStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="计划开始">
                <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button :icon="Download" @click="syncLedger">同步生成</el-button>
                <el-button :icon="Plus" type="success" @click="openCreateLedger">新建</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
            <el-table-column prop="worksheetCode" label="工作表编号" width="150" />
            <el-table-column prop="workStatus" label="状态" width="90" />
            <el-table-column prop="operationTypeFlag" label="作业类型" width="110" show-overflow-tooltip />
            <el-table-column prop="startLocation" label="起始地点" min-width="140" show-overflow-tooltip />
            <el-table-column prop="endLocation" label="结束地点" min-width="140" show-overflow-tooltip />
            <el-table-column prop="assignedPilotId" label="引航员" width="100">
              <template #default="{ row }">
                <span>{{ row.assignedPilotId ? (pilotNameMap[row.assignedPilotId] || row.assignedPilotId) : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="assignedDriverId" label="驾驶员" width="100">
              <template #default="{ row }">
                <span>{{ row.assignedDriverId ? (driverNameMap[row.assignedDriverId] || row.assignedDriverId) : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="planStartTime" label="计划开始" width="160" />
            <el-table-column prop="planEndTime" label="计划结束" width="160" />
            <el-table-column prop="actualStartTime" label="实际开始" width="160" />
            <el-table-column prop="actualEndTime" label="实际结束" width="160" />
            <el-table-column prop="workDuration" label="工时(分钟)" width="90" />
            <el-table-column prop="completionQualityScore" label="评分" width="70" />
            <el-table-column prop="assignedTugboats" label="拖轮" min-width="140" show-overflow-tooltip />
            <el-table-column prop="assignedPilotBoats" label="引航艇" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
                <el-button size="small" :icon="Edit" @click="openEditLedger(row)">编辑</el-button>
                <el-button size="small" type="danger" :icon="Delete" @click="removeLedger(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="tableState.total"
            layout="total, prev, pager, next"
            class="pagination"
          />

          <el-dialog v-model="ledgerDialogVisible" :title="ledgerDialogMode === 'create' ? '新建工作表' : '编辑工作表'" width="720px">
            <el-form label-width="110px" size="small" v-loading="ledgerFormLoading">
              <el-form-item label="工作表编号">
                <el-input v-model="ledgerForm.worksheetCode" placeholder="留空则系统自动生成" />
              </el-form-item>
              <el-form-item label="工作状态">
                <el-select v-model="ledgerForm.workStatus" placeholder="请选择" style="width: 220px">
                  <el-option label="未开始" value="NOT_STARTED" />
                  <el-option label="进行中" value="ONGOING" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="异常" value="ABNORMAL" />
                </el-select>
              </el-form-item>
              <el-form-item label="作业类型标志">
                <el-input v-model="ledgerForm.operationTypeFlag" placeholder="如 INBOUND/OUTBOUND/SHIFTING 等" />
              </el-form-item>
              <el-form-item label="起始地点">
                <el-input v-model="ledgerForm.startLocation" />
              </el-form-item>
              <el-form-item label="结束地点">
                <el-input v-model="ledgerForm.endLocation" />
              </el-form-item>
              <el-form-item label="计划开始/结束">
                <el-date-picker
                  v-model="(ledgerForm as any).planRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  @change="(v:any)=>{ ledgerForm.planStartTime=v?.[0]; ledgerForm.planEndTime=v?.[1]; }"
                />
              </el-form-item>
              <el-form-item label="实际开始/结束">
                <el-date-picker
                  v-model="(ledgerForm as any).actualRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  @change="(v:any)=>{ ledgerForm.actualStartTime=v?.[0]; ledgerForm.actualEndTime=v?.[1]; }"
                />
              </el-form-item>
              <el-form-item label="登轮/离轮">
                <el-date-picker
                  v-model="(ledgerForm as any).boardingLeavingRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  range-separator="至"
                  start-placeholder="登轮"
                  end-placeholder="离轮"
                  @change="(v:any)=>{ ledgerForm.boardingTime=v?.[0]; ledgerForm.leavingTime=v?.[1]; }"
                />
              </el-form-item>
              <el-form-item label="引航员">
                <el-select v-model="ledgerForm.assignedPilotId" filterable clearable style="width: 260px">
                  <el-option v-for="p in pilots" :key="p.id" :label="p.pilotName" :value="p.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="驾驶员">
                <el-select v-model="ledgerForm.assignedDriverId" filterable clearable style="width: 260px">
                  <el-option v-for="d in drivers" :key="d.id" :label="d.driverName" :value="d.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="拖轮(文本)">
                <el-input v-model="ledgerForm.assignedTugboats" type="textarea" :rows="2" placeholder="可填：拖轮A,拖轮B ..." />
              </el-form-item>
              <el-form-item label="引航艇(文本)">
                <el-input v-model="ledgerForm.assignedPilotBoats" type="textarea" :rows="2" placeholder="可填：引航艇A,引航艇B ..." />
              </el-form-item>
              <el-form-item label="质量评分">
                <el-input-number v-model="ledgerForm.completionQualityScore" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="特殊情况">
                <el-input v-model="ledgerForm.specialSituationRecord" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="客户反馈">
                <el-input v-model="ledgerForm.customerFeedback" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="ledgerDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="submitLedgerForm">保存</el-button>
            </template>
          </el-dialog>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-drawer v-model="detailDrawerVisible" title="工作表详情" size="720px">
      <div v-loading="detailLoading">
        <el-collapse accordion>
          <el-collapse-item title="当前行信息(快速)" name="row">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="计划ID">{{ detailRow?.planId ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="计划号">{{ detailRow?.planCode ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="船名">{{ detailRow?.shipName ?? detailRow?.shipNameZh ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="动态">{{ detailRow?.dynamic ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="时间">{{ detailRow?.time ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="泊位/地点">{{ detailRow?.berth ?? '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item v-if="dispatchDetail" title="调度/计划详情(完整)" name="dispatch">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="计划号">{{ dispatchDetail.planInfo?.planCode }}</el-descriptions-item>
              <el-descriptions-item label="计划状态">{{ dispatchDetail.planInfo?.planStatus }}</el-descriptions-item>
              <el-descriptions-item label="计划开始">{{ dispatchDetail.planInfo?.planStartTime }}</el-descriptions-item>
              <el-descriptions-item label="计划结束">{{ dispatchDetail.planInfo?.planEndTime }}</el-descriptions-item>
              <el-descriptions-item label="实际开始">{{ dispatchDetail.planInfo?.actualStartTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="实际结束">{{ dispatchDetail.planInfo?.actualEndTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="起始地点">{{ dispatchDetail.planInfo?.startLocationName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结束地点">{{ dispatchDetail.planInfo?.endLocationName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="登轮">{{ dispatchDetail.planInfo?.boardingTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="离轮">{{ dispatchDetail.planInfo?.leavingTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="天气影响">{{ dispatchDetail.planInfo?.weatherImpact || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ dispatchDetail.planInfo?.notes || '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-divider>船舶/航次/代理</el-divider>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="中文船名">{{ dispatchDetail.vessel?.nameZh || '-' }}</el-descriptions-item>
              <el-descriptions-item label="英文船名">{{ dispatchDetail.vessel?.nameEn || '-' }}</el-descriptions-item>
              <el-descriptions-item label="呼号">{{ dispatchDetail.vessel?.vesselCallSign || '-' }}</el-descriptions-item>
              <el-descriptions-item label="IMO">{{ (dispatchDetail.vessel as any)?.imoNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="航次号">{{ dispatchDetail.voyage?.voyageNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="航次类型">{{ dispatchDetail.voyage?.voyageType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="代理公司">{{ dispatchDetail.agent?.companyName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="代理电话">{{ dispatchDetail.agent?.phone || '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-divider>资源分配(支持多拖轮/多引航艇)</el-divider>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="引航员">
                <div>
                  <el-tag v-for="p in dispatchDetail.pilotAssignments" :key="String(p.id)" style="margin-right:6px;margin-bottom:6px">
                    {{ p.pilotName || p.pilotId }} / {{ p.roleType }} / {{ p.status }}
                  </el-tag>
                  <span v-if="!dispatchDetail.pilotAssignments?.length">-</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="驾驶员">
                <div>
                  <el-tag v-for="d in dispatchDetail.driverAssignments" :key="String(d.id)" style="margin-right:6px;margin-bottom:6px">
                    {{ d.driverName || d.driverId }} / {{ d.status }}
                  </el-tag>
                  <span v-if="!dispatchDetail.driverAssignments?.length">-</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="车辆">
                <div>
                  <el-tag v-for="v in dispatchDetail.vehicleAssignments" :key="String(v.id)" style="margin-right:6px;margin-bottom:6px">
                    {{ v.licensePlate || v.vehicleId }} / {{ v.status }}
                  </el-tag>
                  <span v-if="!dispatchDetail.vehicleAssignments?.length">-</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="拖轮(可多个)">
                <div>
                  <el-tag v-for="t in dispatchDetail.tugboatAssignments" :key="String(t.id)" style="margin-right:6px;margin-bottom:6px">
                    {{ t.tugboatName || t.tugboatId }} / {{ t.assignmentType }} / {{ t.status }}
                  </el-tag>
                  <span v-if="!dispatchDetail.tugboatAssignments?.length">-</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="引航艇(可多个)">
                <div>
                  <el-tag v-for="b in dispatchDetail.pilotBoatAssignments" :key="String(b.id)" style="margin-right:6px;margin-bottom:6px">
                    {{ b.pilotBoatName || b.pilotBoatId }} / {{ b.purpose }} / {{ b.status }}
                  </el-tag>
                  <span v-if="!dispatchDetail.pilotBoatAssignments?.length">-</span>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item v-if="!dispatchDetail && detailRow?.worksheetCode" title="台账详情(work_sheets)" name="ledger">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="工作表编号">{{ detailRow.worksheetCode }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ detailRow.workStatus }}</el-descriptions-item>
              <el-descriptions-item label="作业类型">{{ detailRow.operationTypeFlag || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工时(分钟)">{{ detailRow.workDuration ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="起始地点">{{ detailRow.startLocation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结束地点">{{ detailRow.endLocation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="拖轮">{{ detailRow.assignedTugboats || '-' }}</el-descriptions-item>
              <el-descriptions-item label="引航艇">{{ detailRow.assignedPilotBoats || '-' }}</el-descriptions-item>
              <el-descriptions-item label="特殊情况">{{ detailRow.specialSituationRecord || '-' }}</el-descriptions-item>
              <el-descriptions-item label="客户反馈">{{ detailRow.customerFeedback || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped lang="scss">
.worksheet-view {
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

.worksheet-tabs {
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

.tab-toolbar {
  padding-bottom: 12px;
}

.pagination { margin-top: 12px; justify-content: flex-end; }
</style>