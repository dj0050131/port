<template>
  <div class="tugboat-assignment-dialog">
    <div class="ship-summary">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="船舶名称">
          {{ planData.nameZh || planData.vesselName }}
        </el-descriptions-item>
        <el-descriptions-item label="计划编号">
          {{ planData.planCode }}
        </el-descriptions-item>
        <el-descriptions-item label="作业类型">
          {{ formatOperationType(planData.operationTypeFlag) }}
        </el-descriptions-item>
        <el-descriptions-item label="作业时间">
          {{ formatShortTime(planData.planStartTime) }} - {{ formatShortTime(planData.planEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="总吨">
          {{ formatTonnage(planData.grossTonnage) }}
        </el-descriptions-item>
        <el-descriptions-item label="船长">
          {{ formatDimension(planData.vesselLength) }}m
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="requirement-calculation glass-card" style="margin: 16px 0; padding: 16px;">
      <h4 style="margin-bottom: 12px;">拖轮需求计算</h4>
      <div style="display: flex; gap: 24px; align-items: center;">
        <div>
          <span style="color: #606266;">根据船舶吨位：</span>
          <el-tag type="info" style="margin-left: 8px;">
            {{ calculateTugRequirementByTonnage(planData.grossTonnage) }} 艘
          </el-tag>
        </div>
        <div>
          <span style="color: #606266;">根据船舶长度：</span>
          <el-tag type="info" style="margin-left: 8px;">
            {{ calculateTugRequirementByLength(planData.vesselLength) }} 艘
          </el-tag>
        </div>
        <div>
          <span style="color: #606266;">推荐拖轮数：</span>
          <el-tag type="success" style="margin-left: 8px;">
            {{ calculateRecommendedTugCount() }} 艘
          </el-tag>
        </div>
        <el-button type="primary" size="small" @click="autoAssignTugs">
          智能分配
        </el-button>
      </div>
    </div>

    <div v-if="isBatchMode" class="batch-notice" style="margin-bottom: 16px;">
      <el-alert type="info" :closable="false">
        批量模式：将为 {{ planData.batchPlans?.length || 0 }} 艘船分配拖轮
      </el-alert>
    </div>

    <div class="available-tugs glass-card" style="margin-bottom: 16px; padding: 16px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
        <h4 style="margin: 0;">可用拖轮列表</h4>
        <div>
          <el-button type="primary" text @click="refreshAvailableTugs">刷新</el-button>
          <el-input
              v-model="tugSearchKey"
              placeholder="搜索拖轮"
              size="small"
              style="width: 200px; margin-left: 12px;"
              clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <div class="tug-cards">
        <div
            v-for="tug in filteredAvailableTugs"
            :key="tug.id"
            class="tug-card"
            :class="{ 'selected': isTugSelected(tug.id) }"
            @click="toggleTugSelection(tug.id)"
        >
          <div class="tug-card-header">
            <span class="tug-name">{{ tug.tugboatName }}</span>
            <el-tag v-if="tug.status === 'OPERATING'" size="small" type="warning">作业中</el-tag>
            <el-tag v-else size="small" type="success">可用</el-tag>
          </div>
          <div class="tug-card-body">
            <div class="tug-info-item">
              <span class="label">拖力：</span>
              <span class="value">{{ tug.bollardPull }} 吨</span>
            </div>
            <div class="tug-info-item">
              <span class="label">功率：</span>
              <span class="value">{{ tug.power }} kW</span>
            </div>
            <div class="tug-info-item">
              <span class="label">当前位置：</span>
              <span class="value">{{ tug.currentLocation || '未知' }}</span>
            </div>
            <div class="tug-info-item">
              <span class="label">所属公司：</span>
              <span class="value">{{ tug.ownerCompany || '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-pagination
          v-if="availableTugs.length > pageSize"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="availableTugs.length"
          layout="prev, pager, next"
          small
          style="margin-top: 16px; justify-content: center;"
      />
    </div>

    <!-- 已选拖轮 -->
    <div class="selected-tugs glass-card" style="margin-bottom: 16px; padding: 16px;">
      <h4 style="margin-bottom: 12px;">已选拖轮 ({{ selectedTugs.length }} 艘)</h4>
      <div v-if="selectedTugs.length > 0" class="selected-tugs-list">
        <el-table :data="selectedTugs" border size="small">
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="tugboatName" label="拖轮名称" width="150" />
          <el-table-column label="拖力(吨)" width="100">
            <template #default="{ row }">{{ row.bollardPull }}</template>
          </el-table-column>
          <el-table-column label="分配类型" width="120">
            <template #default="{ row }">
              <el-select v-model="row.assignmentType" size="small" @change="onAssignmentTypeChange(row)">
                <el-option label="主拖轮" value="MAIN" />
                <el-option label="备用拖轮" value="BACKUP" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="预计作业时间" width="200">
            <template #default="{ row }">
              <el-date-picker
                  v-model="row.estimatedStartTime"
                  type="datetime"
                  placeholder="开始时间"
                  size="small"
                  style="width: 100%; margin-bottom: 4px;"
                  :disabled-date="disabledDate"
                  :shortcuts="dateShortcuts"
              />
              <el-date-picker
                  v-model="row.estimatedEndTime"
                  type="datetime"
                  placeholder="结束时间"
                  size="small"
                  style="width: 100%;"
                  :disabled-date="disabledDate"
                  :shortcuts="dateShortcuts"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="removeTug($index)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-selection">
        <el-empty description="请从左侧选择拖轮" :image-size="80" />
      </div>
    </div>

    <!-- 分配规则设置 -->
    <div class="assignment-rules glass-card" style="margin-bottom: 16px; padding: 16px;">
      <h4 style="margin-bottom: 12px;">分配规则设置</h4>
      <el-form :model="assignmentRules" label-width="120px" size="small">
        <el-form-item label="分配方式">
          <el-radio-group v-model="assignmentRules.assignMethod">
            <el-radio label="AUTO">智能分配</el-radio>
            <el-radio label="MANUAL">手动分配</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主拖轮数量">
          <el-input-number
              v-model="assignmentRules.mainTugCount"
              :min="selectedTugs.length > 0 ? 1 : 0"
              :max="Math.max(selectedTugs.length, 1)"
              size="small"
              :disabled="assignmentRules.assignMethod === 'AUTO'"
          />
        </el-form-item>
        <el-form-item label="备用拖轮数量">
          <el-input-number
              v-model="assignmentRules.backupTugCount"
              :min="0"
              :max="Math.max(0, selectedTugs.length - assignmentRules.mainTugCount)"
              size="small"
              :disabled="assignmentRules.assignMethod === 'AUTO'"
          />
        </el-form-item>
        <el-form-item label="是否允许时间重叠">
          <el-switch v-model="assignmentRules.allowTimeOverlap" />
        </el-form-item>
      </el-form>
    </div>

    <div class="dialog-footer">
      <el-button @click="emit('cancel')">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确认分配
      </el-button>
      <el-button type="success" :loading="submitting" @click="handleSubmitAndPublish">
        分配并发布计划
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { PilotagePlan } from '@/types/pilotage'
import {ElMessage, ElMessageBox} from "element-plus";
import {getAvailableTugboats} from "@/api/pilotagePlan.ts";
import { useDataCenterStore } from '@/stores/dataCenter'

const props = defineProps<{
  planData: any
}>()

const emit = defineEmits<{
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}>()

const availableTugs = ref<any[]>([])
const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('pilotage.tugboatAssignment', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const tugSearchKey = ref('')
const currentPage = ref(1)
const pageSize = 8

const filteredAvailableTugs = computed(() => {
  let filtered = availableTugs.value

  if (tugSearchKey.value) {
    const keyword = tugSearchKey.value.toLowerCase()
    filtered = filtered.filter(tug =>
        tug.tugboatName.toLowerCase().includes(keyword) ||
        tug.callSign?.toLowerCase().includes(keyword) ||
        tug.ownerCompany?.toLowerCase().includes(keyword)
    )
  }

  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filtered.slice(start, end)
})

const selectedTugs = ref<any[]>([])
const submitting = ref(false)

const isBatchMode = computed(() => props.planData?.batchMode === true)

// 分配规则
const assignmentRules = reactive({
  assignMethod: 'AUTO',
  mainTugCount: 1,
  backupTugCount: 0,
  allowTimeOverlap: false,
  priorityDistribution: true
})

const disabledDate = (date: Date) => {
  return date < new Date(Date.now() - 24 * 60 * 60 * 1000)
}

const dateShortcuts = [
  {
    text: '计划开始时间',
    value: () => new Date(props.planData.planStartTime)
  },
  {
    text: '计划结束时间',
    value: () => new Date(props.planData.planEndTime)
  },
  {
    text: '1小时后',
    value: () => {
      const date = new Date()
      date.setHours(date.getHours() + 1)
      return date
    }
  }
]

const formatOperationType = (type: string) => {
  const map: Record<string, string> = {
    'INBOUND': '进港',
    'OUTBOUND': '出港',
    'SHIFT': '移泊'
  }
  return map[type] || type
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

const calculateTugRequirementByTonnage = (tonnage: number | string) => {
  if (!tonnage) return 0
  const num = typeof tonnage === 'string' ? parseFloat(tonnage) : tonnage

  if (num < 30000) return 1
  if (num < 50000) return 2
  if (num < 100000) return 3
  return 4
}

const calculateTugRequirementByLength = (length: number | string) => {
  if (!length) return 0
  const num = typeof length === 'string' ? parseFloat(length) : length

  if (num < 150) return 1
  if (num < 200) return 2
  if (num < 250) return 3
  return 4
}

const calculateRecommendedTugCount = () => {
  const byTonnage = calculateTugRequirementByTonnage(props.planData.grossTonnage)
  const byLength = calculateTugRequirementByLength(props.planData.vesselLength)
  return Math.max(byTonnage, byLength)
}

const isTugSelected = (tugId: number) => {
  return selectedTugs.value.some(tug => tug.id === tugId)
}

const toggleTugSelection = (tugId: number) => {
  const tug = availableTugs.value.find(t => t.id === tugId)
  if (!tug) return

  if (isTugSelected(tugId)) {
    selectedTugs.value = selectedTugs.value.filter(t => t.id !== tugId)
  } else {
    if (tug.status === 'OPERATING') {
      ElMessage.warning('该拖轮正在作业中')
      return
    }

    const recommendedCount = calculateRecommendedTugCount()
    if (selectedTugs.value.length >= recommendedCount + 2) {
      ElMessage.warning(`推荐拖轮数为 ${recommendedCount}，最多可添加 ${recommendedCount + 2} 艘`)
      return
    }

    selectedTugs.value.push({
      ...tug,
      assignmentType: selectedTugs.value.length === 0 ? 'MAIN' : 'BACKUP',
      estimatedStartTime: props.planData.planStartTime,
      estimatedEndTime: props.planData.planEndTime,
      status: 'ASSIGNED'
    })
  }
}

const removeTug = (index: number) => {
  selectedTugs.value.splice(index, 1)
}

const onAssignmentTypeChange = (row: any) => {
  if (row.assignmentType === 'MAIN') {
    const mainTugs = selectedTugs.value.filter(t => t.assignmentType === 'MAIN' && t.id !== row.id)
    if (mainTugs.length > 0) {
      ElMessage.warning('只能有一艘主拖轮')
      row.assignmentType = 'BACKUP'
    }
  }
}

const autoAssignTugs = async () => {
  const recommendedCount = calculateRecommendedTugCount()

  selectedTugs.value = []

  const suitableTugs = availableTugs.value
      .filter(tug => tug.status === 'AVAILABLE')
      .sort((a, b) => b.bollardPull - a.bollardPull)
      .slice(0, recommendedCount)

  if (suitableTugs.length === 0) {
    ElMessage.warning('没有找到合适的可用拖轮')
    return
  }

  suitableTugs.forEach((tug, index) => {
    selectedTugs.value.push({
      ...tug,
      assignmentType: index === 0 ? 'MAIN' : 'BACKUP',
      estimatedStartTime: props.planData.planStartTime,
      estimatedEndTime: props.planData.planEndTime,
      status: 'ASSIGNED'
    })
  })

  ElMessage.success(`智能分配了 ${selectedTugs.value.length} 艘拖轮`)
}

const loadAvailableTugs = async () => {
  try {
    uiState.loading = true

    const response = await getAvailableTugboats()

    if (response.code === 200 && response.data) {
      availableTugs.value = response.data

      const recommendedCount = calculateRecommendedTugCount()
      assignmentRules.mainTugCount = Math.min(1, recommendedCount)
      assignmentRules.backupTugCount = Math.max(0, recommendedCount - 1)
    } else {
      ElMessage.warning('未能获取到拖轮数据')
    }
  } catch (error) {
    console.error('加载拖轮数据失败:', error)
    ElMessage.error('加载拖轮数据失败')
  } finally {
    uiState.loading = false
  }
}

const refreshAvailableTugs = () => {
  loadAvailableTugs()
}

const handleSubmit = async () => {
  if (selectedTugs.value.length === 0) {
    ElMessage.warning('请至少选择一艘拖轮')
    return
  }

  const mainTugs = selectedTugs.value.filter(t => t.assignmentType === 'MAIN')
  if (mainTugs.length === 0) {
    ElMessage.warning('必须指定一艘主拖轮')
    return
  }

  submitting.value = true

  try {
    const assignmentData = {
      planId: props.planData.id,
      planCode: props.planData.planCode,
      tugboatAssignments: selectedTugs.value.map(tug => {
        const assignment: any = {
          tugboatId: tug.id,
          tugboatName: tug.tugboatName,
          assignmentType: tug.assignmentType,
          bollardPull: tug.bollardPull,
          status: tug.status
        }

        if (tug.estimatedStartTime) {
          assignment.estimatedStartTime = formatDateToBackend(new Date(tug.estimatedStartTime))
        }
        if (tug.estimatedEndTime) {
          assignment.estimatedEndTime = formatDateToBackend(new Date(tug.estimatedEndTime))
        }

        return assignment
      }),
      assignmentRules: { ...assignmentRules },
      timestamp: formatDateToBackend(new Date())
    }

    if (isBatchMode.value && props.planData.batchPlans) {
      assignmentData.batchMode = true
      assignmentData.batchPlanIds = props.planData.batchPlans.map((p: any) => p.id)
    }

    emit('submit', assignmentData)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
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

// 分配并发布
const handleSubmitAndPublish = async () => {
  if (selectedTugs.value.length === 0) {
    ElMessage.warning('请至少选择一艘拖轮')
    return
  }

  const confirm = await ElMessageBox.confirm(
      '确认分配拖轮并发布计划？计划发布后将通知所有相关人员。',
      '分配并发布确认',
      {
        type: 'warning',
        confirmButtonText: '确认发布',
        cancelButtonText: '取消'
      }
  ).catch(() => false)

  if (!confirm) return

  submitting.value = true

  try {
    const assignmentData = {
      planId: props.planData.id,
      planCode: props.planData.planCode,
      tugboatAssignments: selectedTugs.value,
      assignmentRules: { ...assignmentRules },
      publishPlan: true, // 标记为需要发布
      timestamp: new Date().toISOString()
    }

    emit('submit', assignmentData)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 添加一个 watch 来监听 selectedTugs 的变化，确保输入框的值在合法范围内
watch(selectedTugs, (newVal) => {
  // 当 selectedTugs 变化时，调整主拖轮数量
  if (newVal.length === 0) {
    assignmentRules.mainTugCount = 0
    assignmentRules.backupTugCount = 0
  } else if (assignmentRules.mainTugCount > newVal.length) {
    assignmentRules.mainTugCount = newVal.length
    assignmentRules.backupTugCount = Math.max(0, newVal.length - assignmentRules.mainTugCount)
  }
}, { deep: true })

onMounted(() => {
  loadAvailableTugs()

  if (props.planData.tugboatAssignments && props.planData.tugboatAssignments.length > 0) {
    selectedTugs.value = [...props.planData.tugboatAssignments]
  } else {
    const recommendedCount = calculateRecommendedTugCount()
    assignmentRules.mainTugCount = Math.min(1, recommendedCount)
    assignmentRules.backupTugCount = Math.max(0, recommendedCount - 1)
  }
})
</script>

<style scoped lang="scss">
.tugboat-assignment-dialog {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}

.ship-summary {
  margin-bottom: 16px;

  :deep(.el-descriptions) {
    .el-descriptions__cell {
      padding: 8px;
    }
  }
}

.tug-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.tug-card {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &.selected {
    border-color: var(--el-color-primary);
    background: rgba(var(--el-color-primary-rgb), 0.05);

    .tug-card-header .tug-name {
      color: var(--el-color-primary);
      font-weight: 600;
    }
  }

  .tug-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .tug-name {
      font-size: 14px;
      font-weight: 500;
    }
  }

  .tug-card-body {
    .tug-info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 4px;
      font-size: 12px;

      .label {
        color: var(--tidal-text-secondary);
      }

      .value {
        color: var(--tidal-text-primary);
        font-weight: 500;
      }
    }
  }
}

.empty-selection {
  padding: 40px 0;
  text-align: center;
  color: var(--tidal-text-secondary);
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-light);
}

:deep(.glass-card) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

// 自定义滚动条
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--el-color-primary-light-5);
  border-radius: 3px;

  &:hover {
    background: var(--el-color-primary);
  }
}
</style>