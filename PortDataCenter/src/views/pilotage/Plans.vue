<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Edit, Delete, Position, Calendar, Plus,
  Refresh, Download, Connection
} from '@element-plus/icons-vue'
import {
  fetchPilotagePlans,
  publishPlan,
  deletePlansBatch,
  exportPlans, saveTugboatAssignments, setPlanDate
} from '@/api/pilotagePlan.ts'
import type { TugboatAssignmentDTO } from '@/api/pilotagePlan.ts'
import type { PilotagePlan } from '@/types/pilotage'
import ReplanPlanDialog from './components/ReplanPlanDialog.vue'
import TugboatAssignmentDialog from './components/TugboatAssignmentDialog.vue'
import { mockPlans } from '@/mock/pilotageData'
import { batchPublishPilotagePlans } from "@/api/pilotage.ts";
import { useDataCenterStore } from '@/stores/dataCenter'

const multipleSelection = ref<PilotagePlan[]>([])

const handleSelectionChange = (selection: PilotagePlan[]) => {
  multipleSelection.value = selection
}

const canReplanRow = (row: PilotagePlan) => {
  return ['PLANNED', 'REPLANNED', 'REPLAN_REQUIRED'].includes((row as any).planStatus)
}

const canBatchReplan = computed(() => {
  // 当前“批量重排”实际只支持单条（避免用户选中多条后再提示）
  if (multipleSelection.value.length !== 1) return false
  return canReplanRow(multipleSelection.value[0])
})

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PilotagePlan>('pilotage.plans', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  keyword: '',
  date: '',
  planStatus: '',
  voyageType: '',
  priority: ''
})

const pagination = reactive({
  current: tableState.page,
  size: tableState.pageSize
})

const replanDialogVisible = ref(false)
const replanPlanId = ref<number | null>(null)
const currentPlan = ref<Partial<PilotagePlan>>({}) // 仍用于拖轮分配弹窗

const tugboatDialogVisible = ref(false)

const dateDialogVisible = ref(false)
const planDate = ref('')

const openReplan = (row: PilotagePlan) => {
  replanPlanId.value = Number(row.id)
  replanDialogVisible.value = true
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword?.trim() || undefined,
      planStatus: searchForm.planStatus || undefined,
      voyageType: searchForm.voyageType || undefined,
      priority: searchForm.priority || undefined,
      startDate: searchForm.date || undefined
    }

    const res = await fetchPilotagePlans(params)
    if (res.code === 200 && res.data) {
      tableState.items = res.data.records
      tableState.total = res.data.total
    } else {
      // 降级到模拟数据
      tableState.items = mockPlans
      tableState.total = mockPlans.length
      ElMessage.warning('使用模拟数据进行演示')
    }
  } catch (e) {
    console.error('加载数据失败:', e)
    tableState.items = mockPlans
    tableState.total = mockPlans.length
    ElMessage.warning('使用模拟数据进行演示')
  } finally {
    tableState.loading = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  tableState.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.date = ''
  searchForm.planStatus = ''
  searchForm.voyageType = ''
  searchForm.priority = ''
  handleSearch()
}

// Plans 页面不做“新建/编辑计划”（计划由动态预报/申请生成），重排走专用弹窗

const handleBatchEdit = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  if (multipleSelection.value.length > 1) {
    ElMessage.warning('请选择单条记录进行修改')
    return
  }

  openReplan(multipleSelection.value[0])
}

const handlePublish = async (row: PilotagePlan) => {
  try {
    await ElMessageBox.confirm(
        `确认发布计划 ${row.planCode}?`,
        '提示',
        {
          type: 'info',
          confirmButtonText: '确认发布',
          cancelButtonText: '取消'
        }
    )

    if (!['PLANNED', 'REPLANNED'].includes(row.planStatus)) {
      ElMessage.warning('只有"已计划/重计划"状态的计划才能发布')
      return
    }

    await publishPlan(Number(row.id))
    ElMessage.success('发布成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('发布失败')
  }
}

const handleBatchPublish = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  const invalidPlans = multipleSelection.value.filter(
      plan => !['PLANNED', 'REPLANNED'].includes(plan.planStatus)
  )

  if (invalidPlans.length > 0) {
    ElMessage.warning(`选中的计划中有 ${invalidPlans.length} 条不是"已计划/重计划"状态，无法发布`)
    return
  }

  try {
    await ElMessageBox.confirm(
        `确认发布选中的 ${multipleSelection.value.length} 条计划?`,
        '批量发布确认',
        {
          type: 'warning',
          confirmButtonText: '确认发布',
          cancelButtonText: '取消'
        }
    )

    const planIds = multipleSelection.value.map(plan => Number(plan.id))
    await batchPublishPilotagePlans(planIds)

    ElMessage.success(`成功发布 ${planIds.length} 条计划`)
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('批量发布失败')
  }
}

const handleDelete = async (row: PilotagePlan) => {
  try {
    await ElMessageBox.confirm(
        `确认删除计划 ${row.planCode}?`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        }
    )

    // 使用批量删除接口删除单个计划
    await deletePlansBatch([Number(row.id)])
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确认删除选中的 ${multipleSelection.value.length} 条计划?`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        }
    )

    const planIds = multipleSelection.value.map(plan => Number(plan.id))
    await deletePlansBatch(planIds)

    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('批量删除失败')
  }
}

const handleAssignTugboats = (row: PilotagePlan) => {
  if (!['PLANNING', 'PUBLISHED'].includes(row.planStatus)) {
    ElMessage.warning('只有"计划中"或"已发布"状态的计划可以分配拖轮')
    return
  }

  currentPlan.value = { ...row }
  tugboatDialogVisible.value = true
}

const handleBatchAssignTugboats = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  const invalidPlans = multipleSelection.value.filter(
      plan => !['PLANNING', 'PUBLISHED'].includes(plan.planStatus)
  )

  if (invalidPlans.length > 0) {
    ElMessage.warning(`选中的计划中有 ${invalidPlans.length} 条不是"计划中"或"已发布"状态，无法分配拖轮`)
    return
  }

  currentPlan.value = {
    ...multipleSelection.value[0],
    batchMode: true,
    batchPlans: multipleSelection.value
  }
  tugboatDialogVisible.value = true
}

const handleReschedule = (row: PilotagePlan) => {
  if (!['PLANNED', 'REPLANNED', 'REPLAN_REQUIRED'].includes(row.planStatus)) {
    ElMessage.warning('只有"已计划/重计划/待重排"状态的计划可以重新安排')
    return
  }
  openReplan(row)
}

const handleBatchReschedule = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  if (multipleSelection.value.length > 1) {
    ElMessage.warning('重排(重排资源)暂仅支持单条，请选择一条计划')
    return
  }

  const row = multipleSelection.value[0]
  if (!['PLANNED', 'REPLANNED', 'REPLAN_REQUIRED'].includes(row.planStatus)) {
    ElMessage.warning('只有"已计划/重计划/待重排"状态的计划可以重排')
    return
  }
  openReplan(row)
}

const handleTugboatAssignmentConfirm = async (assignmentData: any) => {
  try {
    const assignments = assignmentData.tugboatAssignments.map((tug: any) => ({
      tugboatId: tug.tugboatId || tug.id,
      tugboatName: tug.tugboatName,
      assignmentType: tug.assignmentType,
      estimatedStartTime: tug.estimatedStartTime ?
          formatDateToBackend(new Date(tug.estimatedStartTime)) : undefined,
      estimatedEndTime: tug.estimatedEndTime ?
          formatDateToBackend(new Date(tug.estimatedEndTime)) : undefined,
      bollardPull: tug.bollardPull,
      status: tug.status
    }))

    const response = await saveTugboatAssignments(assignmentData.planId, assignments)

    if (response.data.failedCount > 0) {
      const failedItems = response.data.failedItems
          .map((item: any) => `拖轮 ${item.resourceId}: ${item.reason}`)
          .join(';')

      ElMessage.warning(`部分拖轮分配失败：${failedItems}`)
    } else {
      ElMessage.success('拖轮分配成功')
    }

    tugboatDialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    console.error('拖轮分配失败:', error)
    ElMessage.error(`拖轮分配失败: ${error.message || '未知错误'}`)
  }
}

const formatDateToBackend = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handleSetDate = () => {
  const currentDate = new Date()
  const tomorrow = new Date(currentDate)
  tomorrow.setDate(tomorrow.getDate() + 1)

  planDate.value = {
    planStartTime: currentDate,
    planEndTime: tomorrow,
    boardingTime: currentDate,
    leavingTime: currentDate
  }

  dateDialogVisible.value = true
}

const confirmSetDate = async () => {
  try {
    if (!selectedPlanId.value) {
      ElMessage.warning('请先选择计划')
      return
    }

    await setPlanDate(selectedPlanId.value, planDate.value)
    ElMessage.success('计划日期设定成功')
    dateDialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    console.error('日期设定失败:', error)
    ElMessage.error(`设定失败: ${error.message || '未知错误'}`)
  }
}

const handleSaveAll = async () => {
  if (!selectedPlanId.value) {
    ElMessage.warning('请先选择计划')
    return
  }

  try {
    const results = await savePlanSchedule(selectedPlanId.value, {
      planDate: planDate.value,
      tugboatAssignments: tugboatAssignments.value,
      pilotBoatAssignments: pilotBoatAssignments.value
    })

    let successMessage = '保存完成'
    const errors = []

    if (results.dateSuccess === false) {
      errors.push(`日期设置失败: ${results.dateError?.message}`)
    }

    if (results.tugboatResult?.failedCount > 0) {
      const failedCount = results.tugboatResult.failedCount
      errors.push(`拖轮分配部分失败: ${failedCount}项`)
    }

    if (results.pilotBoatResult?.failedCount > 0) {
      const failedCount = results.pilotBoatResult.failedCount
      errors.push(`引航艇分配部分失败: ${failedCount}项`)
    }

    if (results.tugboatError) {
      errors.push(`拖轮分配错误: ${results.tugboatError.message}`)
    }

    if (results.pilotBoatError) {
      errors.push(`引航艇分配错误: ${results.pilotBoatError.message}`)
    }

    if (errors.length > 0) {
      ElMessage.warning(`${successMessage}，但存在以下问题:\n${errors.join('\n')}`)
    } else {
      ElMessage.success('所有信息保存成功')
    }

    dateDialogVisible.value = false
    tugboatDialogVisible.value = false
    await fetchData()

  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(`保存失败: ${error.message || '未知错误'}`)
  }
}

const openTugboatAssignmentDialog = (plan: any) => {
  selectedPlanId.value = plan.id
  loadTugboatAssignments(plan.id)
  tugboatDialogVisible.value = true
}

const openDateSettingDialog = (plan: any) => {
  selectedPlanId.value = plan.id
  if (plan.planStartTime && plan.planEndTime) {
    planDate.value = {
      planStartTime: new Date(plan.planStartTime),
      planEndTime: new Date(plan.planEndTime),
      boardingTime: plan.boardingTime ? new Date(plan.boardingTime) : undefined,
      leavingTime: plan.leavingTime ? new Date(plan.leavingTime) : undefined
    }
  }
  dateDialogVisible.value = true
}

const handleExport = async () => {
  try {
    const resp = await exportPlans({
      startDate: searchForm.date || undefined,
      endDate: searchForm.date || undefined,
      format: 'excel',
    })

    const blob = new Blob([resp.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `引航计划_${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '')}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const handlePageChange = (current: number) => {
  pagination.current = current
  tableState.page = current
  fetchData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  tableState.pageSize = size
  tableState.page = 1
  fetchData()
}

const getVesselName = (row: PilotagePlan) => {
  return row.nameZh || row.vesselName || ''
}

const getPilotName = (row: PilotagePlan) => {
  return row.mainPilot || row.pilotName || ''
}

const formatOperationType = (type: string) => {
  if (!type) return ''
  const map: Record<string, string> = {
    'INBOUND': '进港',
    'OUTBOUND': '出港',
    'SHIFT': '移泊',
    'IMPORT_BERTHING': '进口靠泊',
    'EXPORT_UNBERTHING': '出口离泊',
  }
  return map[type] || type
}

const formatVoyageType = (type: string) => {
  if (!type) return ''
  const map: Record<string, string> = {
    'IMPORT': '进口',
    'EXPORT': '出口',
    'TRANSIT': '过境'
  }
  return map[type] || type
}

const formatTonnage = (tonnage: number | string) => {
  if (!tonnage) return '-'
  const num = typeof tonnage === 'string' ? parseFloat(tonnage) : tonnage
  return num.toLocaleString()
}

const formatDimension = (dimension: number | string) => {
  if (!dimension) return '-'
  const num = typeof dimension === 'string' ? parseFloat(dimension) : dimension
  return num.toFixed(2)
}

const formatTime = (time: string | Date) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

const formatShortTime = (time: string | Date) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PLANNING': 'info',      // 蓝色 - 计划中
    'PLANNED': 'primary',
    'REPLANNED': 'warning',  // 重计划
    'REPLAN_REQUIRED': 'danger', // 待重排（资源取消导致）
    'PUBLISHED': 'success',  // 绿色 - 已发布
    'CONFIRMED': 'success',
    'ONGOING': 'warning',    // 橙色 - 进行中
    'COMPLETED': '',         // 默认 - 已完成
    'CERTIFICATED': 'success',
    'BILLED': 'success',
    'PAID': 'success',
    'CANCELLED': 'danger'    // 红色 - 已取消
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PLANNING': '计划中',
    'PLANNED': '已计划',
    'REPLANNED': '重计划',
    'REPLAN_REQUIRED': '待重排',
    'PUBLISHED': '已发布',
    'CONFIRMED': '已确认',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CERTIFICATED': '已签证',
    'BILLED': '已计费',
    'PAID': '已付款',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="plans-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">引航计划</h2>
        <p class="page-desc">引航作业计划制定与管理</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Calendar" @click="handleSetDate">设定计划日期</el-button>
      </div>
    </div>

    <div class="search-bar glass-card">
      <el-form inline size="default">
        <el-form-item label="关键字">
          <el-input
              v-model="searchForm.keyword"
              placeholder="计划编号/船名/航次"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
              v-model="searchForm.date"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              clearable
          />
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select
            v-model="searchForm.planStatus"
            placeholder="全部状态"
            clearable
            :fit-input-width="false"
            popper-class="wide-select-popper"
            style="width: 160px"
          >
            <el-option label="计划中" value="PLANNING" />
            <el-option label="已计划" value="PLANNED" />
            <el-option label="重计划" value="REPLANNED" />
            <el-option label="待重排" value="REPLAN_REQUIRED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已签证" value="CERTIFICATED" />
            <el-option label="已计费" value="BILLED" />
            <el-option label="已付款" value="PAID" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="航次类型">
          <el-select
            v-model="searchForm.voyageType"
            placeholder="全部类型"
            clearable
            :fit-input-width="false"
            popper-class="wide-select-popper"
            style="width: 160px"
          >
            <el-option label="进口" value="IMPORT" />
            <el-option label="出口" value="EXPORT" />
            <el-option label="移泊" value="TRANSIT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar glass-card">
      <div class="action-buttons">
        <el-button type="success" :icon="Position" @click="handleBatchPublish"
                   :disabled="multipleSelection.length === 0">批量发布</el-button>
        <el-button type="info" :icon="Edit" @click="handleBatchReschedule"
                   :disabled="!canBatchReplan">重排</el-button>
        <el-button type="danger" :icon="Delete" @click="handleBatchDelete"
                   :disabled="multipleSelection.length === 0">删除</el-button>
        <el-button type="info" :icon="Download" @click="handleExport">导出</el-button>
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
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column type="index" label="#" width="50" align="center" fixed="left" />

        <el-table-column prop="planCode" label="计划编号" width="130" fixed="left" />

        <el-table-column label="船舶名称" width="150" fixed="left">
          <template #default="{ row }">
            <div class="vessel-info">
              <div class="vessel-name">{{ getVesselName(row) }}</div>
              <div class="vessel-en" v-if="row.nameEn">{{ row.nameEn }}</div>
            </div>
          </template>
        </el-table-column>

        <!-- 船舶基本信息 -->
        <el-table-column label="国籍" width="80">
          <template #default="{ row }">
            {{ row.country || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="总吨" width="100">
          <template #default="{ row }">
            {{ formatTonnage(row.grossTonnage) }}
          </template>
        </el-table-column>

        <el-table-column label="船长(m)" width="90">
          <template #default="{ row }">
            {{ formatDimension(row.vesselLength) }}
          </template>
        </el-table-column>

        <el-table-column label="船宽(m)" width="90">
          <template #default="{ row }">
            {{ formatDimension(row.vesselWidth) }}
          </template>
        </el-table-column>

        <!-- 计划信息 -->
        <el-table-column label="作业类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="primary">
              {{ formatOperationType(row.operationTypeFlag) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="航次类型" width="80">
          <template #default="{ row }">
            {{ formatVoyageType(row.voyageType) }}
          </template>
        </el-table-column>

        <el-table-column prop="planStatus" label="计划状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.planStatus)">
              {{ getStatusText(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 时间信息 -->
        <el-table-column prop="planStartTime" label="计划开始" width="140">
          <template #default="{ row }">
            {{ formatShortTime(row.planStartTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="planEndTime" label="计划结束" width="140">
          <template #default="{ row }">
            {{ formatShortTime(row.planEndTime) }}
          </template>
        </el-table-column>

        <!-- 位置信息 -->
        <el-table-column prop="startLocationName" label="起始位置" width="120" show-overflow-tooltip />
        <el-table-column prop="endLocationName" label="目标位置" width="120" show-overflow-tooltip />

        <!-- 人员信息 -->
        <el-table-column label="主引航员" width="100">
          <template #default="{ row }">
            {{ getPilotName(row) }}
          </template>
        </el-table-column>

        <!-- 拖轮信息 -->
        <el-table-column label="拖轮数" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.tugboatCount && row.tugboatCount > 0" size="small" type="success">
              {{ row.tugboatCount }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 优先级 -->
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="{
              'LOW': 'info',
              'MEDIUM': '',
              'HIGH': 'warning',
              'URGENT': 'danger'
            }[row.priority] || ''">
              {{ row.priority === 'LOW' ? '低' :
                row.priority === 'MEDIUM' ? '中' :
                    row.priority === 'HIGH' ? '高' :
                        row.priority === 'URGENT' ? '紧急' : row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" size="small" @click="handleReschedule(row)" :disabled="!canReplanRow(row)">重排</el-button>
              <el-button link type="success" size="small" @click="handlePublish(row)"
                         :disabled="!['PLANNED','REPLANNED'].includes(row.planStatus)">发布</el-button>
<!--              <el-button link type="warning" size="small" @click="handleAssignTugboats(row)"-->
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="tableState.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <ReplanPlanDialog
      v-model:visible="replanDialogVisible"
      :plan-id="replanPlanId"
      @success="fetchData"
    />

    <el-dialog
        v-model="tugboatDialogVisible"
        :title="`分配拖轮 - ${currentPlan.planCode || ''}`"
        width="800px"
        top="8vh"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <TugboatAssignmentDialog
          :plan-data="currentPlan"
          @submit="handleTugboatAssignmentConfirm"
          @cancel="tugboatDialogVisible = false"
      />
    </el-dialog>

    <el-dialog
        v-model="dateDialogVisible"
        title="设定实际计划日期"
        width="400px"
        center
        destroy-on-close
    >
      <div style="padding: 20px; text-align: center;">
        <el-date-picker
            v-model="planDate"
            type="date"
            placeholder="选择日期"
            size="large"
            value-format="YYYY-MM-DD"
            style="width: 100%"
        />
      </div>
      <template #footer>
        <el-button @click="dateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSetDate">生成</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped lang="scss">
.plans-view {
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

.search-bar {
  padding: 16px;

  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .el-form-item {
      margin-bottom: 0;
    }
  }
}

.action-bar {
  padding: 12px 16px;

  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.table-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  :deep(.el-table) {
    flex: 1;

    .el-table__body-wrapper {
      flex: 1;
    }

    .el-table__header-wrapper th {
      font-weight: 600;
      color: var(--tidal-text-primary);
    }
  }
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.vessel-info {
  .vessel-name {
    font-weight: 500;
    line-height: 1.4;
  }

  .vessel-en {
    font-size: 12px;
    color: var(--tidal-text-secondary);
    line-height: 1.2;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;

  .el-button {
    padding: 0 4px;
    font-size: 12px;
  }
}

:deep(.glass-card) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

:deep(.pilotage-dialog) {
  .el-dialog__body {
    padding: 0;
  }

  .el-dialog__header {
    border-bottom: 1px solid var(--tidal-border);
    margin-right: 0;
  }
}

:deep(.el-tag) {
  border-radius: 4px;
  min-width: 60px;
  text-align: center;
  font-size: 12px;
}

// 多选框样式
:deep(.el-table th.el-table__column--selection .cell),
:deep(.el-table td.el-table__column--selection .cell) {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

:deep(.el-table .el-checkbox) {
  margin: 0 auto;
}

/* 多选框样式修复 */
:deep(.el-table th.el-table__column--selection .cell),
:deep(.el-table td.el-table__column--selection .cell) {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

:deep(.el-table .el-checkbox) {
  margin: 0 auto;
}

:deep(.el-checkbox__inner) {
  border: 1px solid #006994;
  background-color: #ffffff;
}

:deep(.el-checkbox__inner::after) {
  border-color: #006994;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #006994;
  border-color: #006994;
}
</style>

<style lang="scss">
.wide-select-popper {
  min-width: 220px !important;
}
</style>