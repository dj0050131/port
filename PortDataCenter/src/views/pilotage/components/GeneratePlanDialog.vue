<template>
  <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1200px"
      :close-on-click-modal="false"
      destroy-on-close
      class="generate-plan-dialog"
      @close="handleDialogClose"
  >
    <div class="selected-ships">
      <h3>已选择船舶 ({{ selectedShips.length }}艘)</h3>
      <el-table
          :data="selectedShips"
          border
          size="small"
          style="margin-bottom: 20px"
          max-height="200"
      >
        <el-table-column prop="nameZh" label="中文船名" width="120" />
        <el-table-column prop="voyageType" label="航次类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getVoyageTypeTag(row.voyageType)">
              {{ getVoyageTypeName(row.voyageType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eta" label="预抵时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.eta) }}
          </template>
        </el-table-column>
        <el-table-column prop="plannedStartName" label="预抵锚地" width="120" />
        <el-table-column prop="plannedEndName" label="预抵泊位" width="120" />
        <el-table-column prop="agentName" label="代理公司" width="150" />
        <el-table-column prop="priority" label="计划优先级" width="90">
          <template #default="{ row }">
            <el-tooltip
              effect="dark"
              placement="top"
              :content="`申请优先级：${getPriorityName(row.priority)}${row.planPriority ? `；已存在计划优先级：${getPriorityName(row.planPriority)}` : ''}`"
            >
              <el-tag :type="getPriorityTag(getEffectivePriority(row))" size="small">
                {{ getPriorityName(getEffectivePriority(row)) }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="plan-tabs">
      <el-tab-pane label="批量设置" name="batch">
        <div class="tab-content">
          <el-form
              ref="batchFormRef"
              :model="planForm"
              :rules="batchRules"
              label-width="110px"
              class="plan-form"
              :hide-required-asterisk="true"
          >
            <!-- 计划时间范围 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="计划开始时间" prop="planStartTime" :error="batchErrors.planStartTime">
                  <el-date-picker
                      v-model="planForm.planStartTime"
                      type="datetime"
                      placeholder="选择开始时间"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :disabled-date="disabledDate"
                      :shortcuts="dateShortcuts"
                      style="width: 100%"
                      @change="clearBatchError('planStartTime')"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计划结束时间" prop="planEndTime" :error="batchErrors.planEndTime">
                  <el-date-picker
                      v-model="planForm.planEndTime"
                      type="datetime"
                      placeholder="选择结束时间"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :disabled-date="disabledDate"
                      :shortcuts="dateShortcuts"
                      style="width: 100%"
                      @change="clearBatchError('planEndTime')"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 资源分配 -->
            <el-divider>资源分配</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="引航员" prop="assignedPilotId" :error="batchErrors.assignedPilotId">
                  <el-select
                      v-model="planForm.assignedPilotId"
                      filterable
                      placeholder="选择引航员"
                      style="width: 100%"
                      @change="onPilotChange"
                  >
                    <el-option
                        v-for="pilot in availablePilotList"
                        :key="pilot.id"
                        :label="`${pilot.pilotName} (${pilot.pilotLevel})`"
                        :value="pilot.id"
                        :disabled="pilot.onDutyStatus !== 'ON_DUTY'"
                    >
                      <span style="float: left">{{ pilot.pilotName }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ pilot.pilotLevel }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="驾驶员" prop="assignedDriverId" :error="batchErrors.assignedDriverId">
                  <el-select
                      v-model="planForm.assignedDriverId"
                      filterable
                      placeholder="选择驾驶员"
                      style="width: 100%"
                      @change="onDriverChange"
                  >
                    <el-option
                        v-for="driver in availableDriverList"
                        :key="driver.id"
                        :label="driver.name"
                        :value="driver.id"
                        :disabled="driver.onDutyStatus !== 'ON_DUTY'"
                    >
                      <span style="float: left">{{ driver.name }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ driver.level }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="载具（车辆）" prop="assignedVehicleId" :error="batchErrors.assignedVehicleId">
                  <el-select
                      v-model="planForm.assignedVehicleId"
                      filterable
                      placeholder="选择载具"
                      style="width: 100%"
                      @change="onVehicleChange"
                  >
                    <el-option
                        v-for="vehicle in availableVehicleList"
                        :key="vehicle.id"
                        :label="`${vehicle.licensePlate} (${vehicle.vehicleModel})`"
                        :value="vehicle.id"
                        :disabled="vehicle.vehicleStatus !== 'AVAILABLE'"
                    >
                      <span style="float: left">{{ vehicle.licensePlate }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ vehicle.vehicleModel }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="优先级" prop="priority">
                  <el-select
                      v-model="planForm.priority"
                      placeholder="选择优先级"
                      style="width: 100%"
                  >
                    <el-option label="低" value="LOW" />
                    <el-option label="中" value="MEDIUM" />
                    <el-option label="高" value="HIGH" />
                    <el-option label="紧急" value="URGENT" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 拖轮和引航艇分配（多选） -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="拖轮">
                  <el-select
                      v-model="planForm.selectedTugboatIds"
                      multiple
                      filterable
                      placeholder="选择拖轮"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="tugboat in availableTugboatList"
                        :key="tugboat.id"
                        :label="tugboat.tugboatName"
                        :value="tugboat.id"
                        :disabled="tugboat.status !== 'AVAILABLE'"
                    />
                  </el-select>
                  <div class="selected-info" v-if="planForm.selectedTugboatIds.length > 0">
                    已选择 {{ planForm.selectedTugboatIds.length }} 艘拖轮
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="引航艇">
                  <el-select
                      v-model="planForm.selectedPilotBoatIds"
                      multiple
                      filterable
                      placeholder="选择引航艇"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="boat in availablePilotBoatList"
                        :key="boat.id"
                        :label="boat.boatName"
                        :value="boat.id"
                        :disabled="boat.currentStatus !== 'AVAILABLE'"
                    />
                  </el-select>
                  <div class="selected-info" v-if="planForm.selectedPilotBoatIds.length > 0">
                    已选择 {{ planForm.selectedPilotBoatIds.length }} 艘引航艇
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 天气影响说明 -->
            <el-form-item label="天气影响说明" prop="weatherImpact">
              <el-input
                  v-model="planForm.weatherImpact"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入天气对引航作业的影响说明"
                  maxlength="500"
                  show-word-limit
              />
            </el-form-item>

            <!-- 备注/特殊要求 -->
            <el-form-item label="备注/特殊要求" prop="notes">
              <el-input
                  v-model="planForm.notes"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入特殊要求或备注"
                  maxlength="1000"
                  show-word-limit
              />
            </el-form-item>
          </el-form>

          <div class="batch-actions" style="text-align: center; margin-top: 20px;">
            <el-button type="primary" @click="applyBatchToAllIndividual">应用批量设置到所有船舶</el-button>
            <el-button @click="clearBatchForm">清空批量设置</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="单独设置" name="individual">
        <div class="tab-content individual-tab-content">
          <div class="individual-settings">
            <div class="batch-apply-info" v-if="hasBatchSettings">
              <el-alert type="info" :closable="false">
                <template #default>
                  <span>已应用批量设置到所有船舶。可在下方单独调整每艘船的计划。</span>
                  <el-button type="primary" link @click="applyBatchToAllIndividual">
                    重新应用批量设置
                  </el-button>
                </template>
              </el-alert>
            </div>

            <div class="individual-table-container">
              <el-table
                  :data="individualSettings"
                  border
                  size="small"
                  style="width: 100%; margin-top: 16px"
                  :row-class-name="tableRowClassName"
                  v-loading="individualLoading"
                  :max-height="individualTableHeight"
                  :height="individualTableHeight"
              >
                <el-table-column prop="nameZh" label="船名" width="120" fixed="left" />
                <el-table-column prop="voyageType" label="航次类型" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getVoyageTypeTag(row.voyageType)" size="small">
                      {{ getVoyageTypeName(row.voyageType) }}
                    </el-tag>
                  </template>
                </el-table-column>

                <!-- 计划时间 -->
                <el-table-column label="计划开始时间" width="180">
                  <template #default="{ $index }">
                    <el-date-picker
                        v-model="individualSettings[$index].planStartTime"
                        type="datetime"
                        placeholder="开始时间"
                        size="small"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                        :disabled-date="disabledDate"
                        @change="clearIndividualError($index, 'planStartTime')"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="计划结束时间" width="180">
                  <template #default="{ $index }">
                    <el-date-picker
                        v-model="individualSettings[$index].planEndTime"
                        type="datetime"
                        placeholder="结束时间"
                        size="small"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                        :disabled-date="disabledDate"
                        @change="clearIndividualError($index, 'planEndTime')"
                    />
                  </template>
                </el-table-column>

                <!-- 资源分配 -->
                <el-table-column label="引航员" width="150">
                  <template #default="{ $index }">
                    <el-select
                        v-model="individualSettings[$index].assignedPilotId"
                        size="small"
                        placeholder="选择引航员"
                        style="width: 100%"
                        @change="onIndividualPilotChange($index)"
                    >
                      <el-option
                          v-for="pilot in availablePilotList"
                          :key="pilot.id"
                          :label="`${pilot.pilotName} (${pilot.pilotLevel})`"
                          :value="pilot.id"
                          :disabled="pilot.onDutyStatus !== 'ON_DUTY'"
                      />
                    </el-select>
                    <div v-if="individualErrors[$index]?.assignedPilotId" class="field-error">
                      {{ individualErrors[$index].assignedPilotId }}
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="驾驶员" width="150">
                  <template #default="{ $index }">
                    <el-select
                        v-model="individualSettings[$index].assignedDriverId"
                        size="small"
                        placeholder="选择驾驶员"
                        style="width: 100%"
                        @change="onIndividualDriverChange($index)"
                    >
                      <el-option
                          v-for="driver in availableDriverList"
                          :key="driver.id"
                          :label="driver.name"
                          :value="driver.id"
                          :disabled="driver.onDutyStatus !== 'ON_DUTY'"
                      />
                    </el-select>
                    <div v-if="individualErrors[$index]?.assignedDriverId" class="field-error">
                      {{ individualErrors[$index].assignedDriverId }}
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="载具" width="150">
                  <template #default="{ $index }">
                    <el-select
                        v-model="individualSettings[$index].assignedVehicleId"
                        size="small"
                        placeholder="选择载具"
                        style="width: 100%"
                        @change="onIndividualVehicleChange($index)"
                    >
                      <el-option
                          v-for="vehicle in availableVehicleList"
                          :key="vehicle.id"
                          :label="`${vehicle.licensePlate} (${vehicle.vehicleModel})`"
                          :value="vehicle.id"
                          :disabled="vehicle.vehicleStatus !== 'AVAILABLE'"
                      />
                    </el-select>
                    <div v-if="individualErrors[$index]?.assignedVehicleId" class="field-error">
                      {{ individualErrors[$index].assignedVehicleId }}
                    </div>
                  </template>
                </el-table-column>

                <!-- 拖轮分配 -->
                <el-table-column label="拖轮" width="200">
                  <template #default="{ $index }">
                    <el-select
                        v-model="individualSettings[$index].assignedTugboatIds"
                        multiple
                        size="small"
                        placeholder="选择拖轮"
                        style="width: 100%"
                    >
                      <el-option
                          v-for="tugboat in availableTugboatList"
                          :key="tugboat.id"
                          :label="tugboat.tugboatName"
                          :value="tugboat.id"
                          :disabled="tugboat.status !== 'AVAILABLE'"
                      />
                    </el-select>
                  </template>
                </el-table-column>

                <!-- 引航艇分配 -->
                <el-table-column label="引航艇" width="200">
                  <template #default="{ $index }">
                    <el-select
                        v-model="individualSettings[$index].assignedPilotBoatIds"
                        multiple
                        size="small"
                        placeholder="选择引航艇"
                        style="width: 100%"
                    >
                      <el-option
                          v-for="boat in availablePilotBoatList"
                          :key="boat.id"
                          :label="boat.boatName"
                          :value="boat.id"
                          :disabled="boat.currentStatus !== 'AVAILABLE'"
                      />
                    </el-select>
                  </template>
                </el-table-column>

                <!-- 备注 -->
                <el-table-column label="备注" width="200">
                  <template #default="{ $index }">
                    <el-input
                        v-model="individualSettings[$index].notes"
                        size="small"
                        placeholder="该船特殊说明"
                        type="textarea"
                        :rows="1"
                        autosize
                    />
                  </template>
                </el-table-column>

                <!-- 操作 -->
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="{ $index }">
                    <el-button
                        link
                        type="primary"
                        size="small"
                        @click="applyBatchToIndividual($index)"
                    >
                      应用批量
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 资源冲突检查 -->
            <div class="conflict-check" style="margin-top: 20px; padding: 16px; background: #f8f9fa; border-radius: 4px;">
              <h4 style="margin-bottom: 12px;">资源冲突检查</h4>
              <div v-if="resourceConflicts.length > 0" class="conflict-list">
                <el-alert
                    v-for="(conflict, index) in resourceConflicts"
                    :key="index"
                    :title="conflict.message"
                    :type="conflict.level"
                    :closable="false"
                    style="margin-bottom: 8px"
                />
              </div>
              <div v-else class="no-conflict">
                <el-alert title="未检测到资源冲突" type="success" :closable="false" />
              </div>
              <div style="margin-top: 12px; text-align: center;">
                <el-button type="primary" size="small" @click="checkResourceConflicts">
                  重新检查冲突
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <span class="dialog-footer">
        <div class="footer-left">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="success" plain :loading="isTestFilling" @click="fillTestPlanData">数据填充测试</el-button>
          <el-button @click="handleSaveDraft" :loading="savingDraft">保存草稿</el-button>
        </div>
        <div class="footer-right">
          <el-button
              type="primary"
              @click="handleGeneratePlan()"
              :loading="generatingPlan"
          >
            生成计划 ({{ selectedShips.length }}艘)
          </el-button>
          <el-button
              type="success"
              @click="handleGenerateAndPublish"
              :loading="generatingPlan"
          >
            生成并发布 ({{ selectedShips.length }}艘)
          </el-button>
        </div>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { PilotageApplication } from '@/types/pilotage'
import {
  createPilotagePlans,
} from "@/api/pilotagePlan.ts";
import { batchPublishPilotagePlans } from '@/api/pilotage'
import { useAuthStore } from '@/stores/auth'
import { useDataCenterStore } from '@/stores/dataCenter'
import {fetchPortAreasByPortId} from "@/api/bases/ports.ts";
import {fetchPilotList} from "@/api/bases/pilots.ts";
import {fetchAnchorageListByPortAreaId} from "@/api/bases/anchorages.ts";
import {fetchBerthListByWharfId} from "@/api/bases/berths.ts";
import { getAnchoragesById } from '@/api/bases/anchorages'
import { getBerthsById } from '@/api/bases/berths'
import {fetchDriverList} from "@/api/bases/drivers.ts";
import {fetchPilotBoatList} from "@/api/bases/pilotboats.ts";
import {fetchVehicleList} from "@/api/bases/vehicles.ts";
import {fetchTugBoatList} from "@/api/bases/tugboats.ts";

const props = defineProps<{
  visible: boolean
  selectedShips: PilotageApplication[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'confirm', planData: any[]): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => `为 ${props.selectedShips.length} 艘船生成引航计划`)

const batchFormRef = ref<FormInstance>()
const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('pilotage.generatePlanDialog', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const individualLoading = ref(false)
const generatingPlan = ref(false)
const savingDraft = ref(false)
const activeTab = ref('batch')
const isTestFilling = ref(false)

const batchErrors = reactive({
  planStartTime: '',
  planEndTime: '',
  assignedPilotId: '',
  assignedDriverId: '',
  assignedVehicleId: ''
})

const individualErrors = ref<Array<{
  planStartTime?: string
  planEndTime?: string
  startLocationId?: string
  endLocationId?: string
  assignedPilotId?: string
  assignedDriverId?: string
  assignedVehicleId?: string
}>>([])

const pilotList = ref<any[]>([])
const driverList = ref<any[]>([])
const pilotBoatList = ref<any[]>([])
const tugboatList = ref<any[]>([])
const vehicleList = ref<any[]>([])
const anchorageList = ref<any[]>([])
const berthList = ref<any[]>([])

const availablePilotList = computed(() =>
    pilotList.value.filter(pilot => pilot.onDutyStatus === 'ON_DUTY')
)

const availableDriverList = computed(() =>
    driverList.value.filter(driver => driver.onDutyStatus === 'ON_DUTY')
)

const availablePilotBoatList = computed(() =>
    pilotBoatList.value.filter(boat => boat.currentStatus === 'AVAILABLE')
)

const availableTugboatList = computed(() =>
    tugboatList.value.filter(tugboat => tugboat.status === 'AVAILABLE')
)

const availableVehicleList = computed(() =>
    vehicleList.value.filter(vehicle => vehicle.vehicleStatus === 'AVAILABLE')
)

const planForm = reactive({
  planStartTime: '',
  planEndTime: '',
  assignedPilotId: null as number | null,
  assignedDriverId: null as number | null,
  assignedVehicleId: null as number | null,
  selectedTugboatIds: [] as number[],
  selectedPilotBoatIds: [] as number[],
  priority: 'MEDIUM' as string,
  weatherImpact: '',
  notes: ''
})

const resolveDefaultPriority = () => {
  const ships = props.selectedShips || []
  if (!ships.length) return 'MEDIUM'
  // 若已生成计划，则优先使用计划优先级（planPriority）；否则用申请优先级（priority）
  const values = ships
    .map((s: any) => s.planPriority || s.priority)
    .filter(Boolean)
  const uniq = Array.from(new Set(values))
  return uniq.length === 1 ? String(uniq[0]) : 'MEDIUM'
}

const getEffectivePriority = (row: any) => {
  if (activeTab.value === 'batch') return planForm.priority
  const hit = individualSettings.value.find((x) => String(x.id) === String(row.id))
  return (hit?.priority || row.planPriority || row.priority || planForm.priority) as string
}

const hasBatchSettings = computed(() => {
  return !!planForm.planStartTime || !!planForm.planEndTime ||
      planForm.assignedPilotId !== null || planForm.assignedDriverId !== null ||
      planForm.assignedVehicleId !== null || planForm.selectedTugboatIds.length > 0 ||
      planForm.selectedPilotBoatIds.length > 0
})

const batchRules: FormRules = {
  planStartTime: [{ required: true, message: '请选择计划开始时间' }],
  planEndTime: [{ required: true, message: '请选择计划结束时间' }],
  assignedPilotId: [{ required: true, message: '请选择引航员' }],
  assignedDriverId: [{ required: true, message: '请选择驾驶员' }],
  assignedVehicleId: [{ required: true, message: '请选择载具' }],
  priority: [{ required: true, message: '请选择优先级' }]
}

const individualSettings = ref<Array<{
  id: number
  voyageId: number
  vesselId: number
  nameZh: string
  voyageType: string
  planStartTime: string
  planEndTime: string
  startLocationType: string
  startLocationId: number | null
  startLocationName: string
  endLocationType: string
  endLocationId: number | null
  endLocationName: string
  assignedPilotId: number | null
  assignedDriverId: number | null
  assignedVehicleId: number | null
  assignedTugboatIds: number[]
  assignedPilotBoatIds: number[]
  weatherImpact: string
  notes: string
  priority: string
}>>([])

const resourceConflicts = ref<Array<{
  message: string
  level: 'warning' | 'error'
}>>([])

const parseDateTime = (s?: string) => {
  if (!s) return null
  const d = new Date(s.replace(' ', 'T'))
  return isNaN(d.getTime()) ? null : d
}

const isOverlap = (aStart: Date, aEnd: Date, bStart: Date, bEnd: Date) => {
  return aStart < bEnd && bStart < aEnd
}

const getPlannedLocation = (ship: any) => {
  const startId = ship.plannedStartId ?? ship.plannedAnchorageId ?? null
  const endId = ship.plannedEndId ?? ship.plannedBerthId ?? null
  const startName = ship.plannedStartName ?? ship.plannedAnchorage ?? ship.plannedAnchorageName ?? ''
  const endName = ship.plannedEndName ?? ship.plannedBerthName ?? ''
  return { startId, endId, startName, endName }
}

const inferLocationTypes = (ship: any) => {
  // 优先使用申请表自身记录的 start/end 类型（移泊等场景更准确）
  const st = String(ship?.startLocationType || '').trim()
  const et = String(ship?.endLocationType || '').trim()
  if (st && et) {
    return { startLocationType: st, endLocationType: et }
  }
  switch (ship?.voyageType) {
    case 'IMPORT':
      return { startLocationType: 'ANCHORAGE', endLocationType: 'BERTH' }
    case 'EXPORT':
      return { startLocationType: 'BERTH', endLocationType: 'ANCHORAGE' }
    case 'TRANSIT':
      return { startLocationType: 'BERTH', endLocationType: 'BERTH' }
    default:
      return { startLocationType: '', endLocationType: '' }
  }
}

const resolveOperationTypeFlag = (ship: any) => {
  switch (ship?.voyageType) {
    case 'IMPORT': return 'INBOUND'
    case 'EXPORT': return 'OUTBOUND'
    case 'TRANSIT': return 'SHIFTING'
    default: return String(ship?.voyageType || '')
  }
}

const anchorageNameCache = new Map<number, string>()
const berthNameCache = new Map<number, string>()

const primeCachesFromLists = () => {
  for (const a of (anchorageList.value || [])) {
    const id = Number((a as any)?.id)
    const name = String((a as any)?.anchorageNameZh || '').trim()
    if (id > 0 && name) anchorageNameCache.set(id, name)
  }
  for (const b of (berthList.value || [])) {
    const id = Number((b as any)?.id)
    const name = String((b as any)?.berthName || '').trim()
    if (id > 0 && name) berthNameCache.set(id, name)
  }
}

const getCachedLocationName = (type: string, id: any) => {
  const n = Number(id)
  if (!n || n <= 0) return ''
  if (type === 'ANCHORAGE') return anchorageNameCache.get(n) || ''
  if (type === 'BERTH') return berthNameCache.get(n) || ''
  return ''
}

const ensureLocationNames = async (ships: any[]) => {
  primeCachesFromLists()

  const needAnchorageIds = new Set<number>()
  const needBerthIds = new Set<number>()

  for (const ship of ships) {
    const loc = getPlannedLocation(ship)
    const types = inferLocationTypes(ship)

    const startType = String(types.startLocationType || '').trim()
    const endType = String(types.endLocationType || '').trim()

    const startId = Number(loc.startId ?? (ship as any)?.startLocationId)
    const endId = Number(loc.endId ?? (ship as any)?.endLocationId)

    const startName = String(loc.startName ?? (ship as any)?.startLocationName ?? '').trim()
    const endName = String(loc.endName ?? (ship as any)?.endLocationName ?? '').trim()

    if (startId > 0 && !startName) {
      if (startType === 'ANCHORAGE' && !anchorageNameCache.has(startId)) needAnchorageIds.add(startId)
      if (startType === 'BERTH' && !berthNameCache.has(startId)) needBerthIds.add(startId)
    }
    if (endId > 0 && !endName) {
      if (endType === 'ANCHORAGE' && !anchorageNameCache.has(endId)) needAnchorageIds.add(endId)
      if (endType === 'BERTH' && !berthNameCache.has(endId)) needBerthIds.add(endId)
    }
  }

  const anchoragePromises = Array.from(needAnchorageIds).map(async (id) => {
    try {
      const resp: any = await getAnchoragesById(id)
      const name = String(resp?.data?.anchorageNameZh || '').trim()
      if (name) anchorageNameCache.set(id, name)
    } catch {
      // ignore
    }
  })
  const berthPromises = Array.from(needBerthIds).map(async (id) => {
    try {
      const resp: any = await getBerthsById(id)
      const name = String(resp?.data?.berthName || '').trim()
      if (name) berthNameCache.set(id, name)
    } catch {
      // ignore
    }
  })

  await Promise.all([...anchoragePromises, ...berthPromises])
}

const resolveLocationName = (type: string, id: any, fallback?: any) => {
  const fb = String(fallback || '').trim()
  if (fb) return fb
  const cached = getCachedLocationName(type, id)
  if (cached) return cached
  const n = Number(id)
  if (n > 0 && (type === 'ANCHORAGE' || type === 'BERTH')) return `${type}-${n}`
  return ''
}

const computeResourceConflicts = (settings: Array<any>) => {
  const conflicts: Array<{ message: string; level: 'warning' | 'error' }> = []

  const slots = settings
    .map((s) => {
      const start = parseDateTime(s.planStartTime)
      const end = parseDateTime(s.planEndTime)
      return { s, start, end }
    })
    .filter((x) => x.start && x.end) as Array<{ s: any; start: Date; end: Date }>

  for (const { s, start, end } of slots) {
    if (start >= end) {
      conflicts.push({ message: `船舶 ${s.nameZh} 的计划开始时间晚于/等于结束时间`, level: 'error' })
    }
  }

  for (let i = 0; i < slots.length; i++) {
    for (let j = i + 1; j < slots.length; j++) {
      const a = slots[i]
      const b = slots[j]
      if (isOverlap(a.start, a.end, b.start, b.end)) {
        conflicts.push({ message: `船舶 ${a.s.nameZh} 和 ${b.s.nameZh} 的计划时间重叠`, level: 'warning' })
      }
    }
  }

  // 资源冲突（必须时间重叠才算冲突）
  type ResourceUse = { shipName: string; start: Date; end: Date }
  const addConflictIfOverlap = (label: string, resourceKey: string, resourceId: number, uses: ResourceUse[]) => {
    for (let i = 0; i < uses.length; i++) {
      for (let j = i + 1; j < uses.length; j++) {
        if (isOverlap(uses[i].start, uses[i].end, uses[j].start, uses[j].end)) {
          conflicts.push({
            message: `${label}(${resourceId}) 在 ${uses[i].shipName} 与 ${uses[j].shipName} 的时间段存在重叠占用`,
            level: 'error',
          })
        }
      }
    }
  }

  const pilotMap = new Map<number, ResourceUse[]>()
  const driverMap = new Map<number, ResourceUse[]>()
  const vehicleMap = new Map<number, ResourceUse[]>()
  const tugboatMap = new Map<number, ResourceUse[]>()
  const pilotBoatMap = new Map<number, ResourceUse[]>()

  const pushUse = (m: Map<number, ResourceUse[]>, id: number, use: ResourceUse) => {
    if (!m.has(id)) m.set(id, [])
    m.get(id)!.push(use)
  }

  for (const { s, start, end } of slots) {
    const use: ResourceUse = { shipName: s.nameZh, start, end }
    if (s.assignedPilotId) pushUse(pilotMap, s.assignedPilotId, use)
    if (s.assignedDriverId) pushUse(driverMap, s.assignedDriverId, use)
    if (s.assignedVehicleId) pushUse(vehicleMap, s.assignedVehicleId, use)
    ;(s.assignedTugboatIds || []).forEach((id: number) => pushUse(tugboatMap, id, use))
    ;(s.assignedPilotBoatIds || []).forEach((id: number) => pushUse(pilotBoatMap, id, use))
  }

  pilotMap.forEach((uses, id) => addConflictIfOverlap('引航员', 'PILOT', id, uses))
  driverMap.forEach((uses, id) => addConflictIfOverlap('驾驶员', 'DRIVER', id, uses))
  vehicleMap.forEach((uses, id) => addConflictIfOverlap('载具', 'VEHICLE', id, uses))
  tugboatMap.forEach((uses, id) => addConflictIfOverlap('拖轮', 'TUGBOAT', id, uses))
  pilotBoatMap.forEach((uses, id) => addConflictIfOverlap('引航艇', 'PILOT_BOAT', id, uses))

  return conflicts
}

const individualTableHeight = ref(400)
const windowHeight = ref(window.innerHeight)

const updateTableHeight = () => {
  const baseHeight = windowHeight.value - 450 // 减去其他元素的高度
  individualTableHeight.value = Math.max(300, Math.min(400, baseHeight))
}

onMounted(() => {
  window.addEventListener('resize', () => {
    windowHeight.value = window.innerHeight
    updateTableHeight()
  })
  updateTableHeight()
})

watch(
  () => props.visible,
  (v) => {
    if (v) {
      // 弹窗打开时：优先级默认值尽量贴近当前数据（避免总是 MEDIUM）
      planForm.priority = resolveDefaultPriority()
      initializeIndividualSettings()
    }
  },
  { immediate: true },
)

onUnmounted(() => {
  window.removeEventListener('resize', updateTableHeight)
})

const getVoyageTypeName = (type: string) => {
  const map: Record<string, string> = {
    'IMPORT': '进港',
    'EXPORT': '出港',
    'TRANSIT': '移泊'
  }
  return map[type] || type
}

const getVoyageTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'IMPORT': 'success',
    'EXPORT': 'warning',
    'TRANSIT': 'info'
  }
  return map[type] || 'info'
}

const getPriorityName = (priority: string) => {
  const map: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return map[priority] || priority
}

const getPriorityTag = (priority: string) => {
  const map: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning',
    'URGENT': 'danger'
  }
  return map[priority] || 'info'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const formatDateTimeFull = (d: Date) => {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const pickOne = <T,>(arr: T[]) => arr[Math.floor(Math.random() * arr.length)]

const fillTestPlanData = async () => {
  if (isTestFilling.value) return
  isTestFilling.value = true
  try {
    if ((!pilotList.value || pilotList.value.length === 0) && !uiState.loading) {
      await loadResources()
    }

    // 计划时间：统一为“今天”，默认 10:00-12:00（若当前已过，则顺延到今天晚些）
    const now = new Date()
    const base = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 10, 0, 0)
    let start = base
    if (now.getTime() > base.getTime()) {
      const h = Math.min(21, Math.max(9, now.getHours() + 1))
      start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), h, 0, 0)
    }
    const end = new Date(start.getTime() + 2 * 60 * 60 * 1000)
    planForm.planStartTime = formatDateTimeFull(start)
    planForm.planEndTime = formatDateTimeFull(end)

    if (availablePilotList.value.length > 0) {
      planForm.assignedPilotId = pickOne(availablePilotList.value as any[]).id
    } else if (pilotList.value.length > 0) {
      planForm.assignedPilotId = pickOne(pilotList.value as any[]).id
    }

    if (availableDriverList.value.length > 0) {
      planForm.assignedDriverId = pickOne(availableDriverList.value as any[]).id
    } else if (driverList.value.length > 0) {
      planForm.assignedDriverId = pickOne(driverList.value as any[]).id
    }

    if (availableVehicleList.value.length > 0) {
      planForm.assignedVehicleId = pickOne(availableVehicleList.value as any[]).id
    } else if (vehicleList.value.length > 0) {
      planForm.assignedVehicleId = pickOne(vehicleList.value as any[]).id
    }

    planForm.selectedTugboatIds = []
    if (availableTugboatList.value.length > 0) {
      planForm.selectedTugboatIds = [pickOne(availableTugboatList.value as any[]).id]
    }
    planForm.selectedPilotBoatIds = []
    if (availablePilotBoatList.value.length > 0) {
      planForm.selectedPilotBoatIds = [pickOne(availablePilotBoatList.value as any[]).id]
    }

    planForm.priority = resolveDefaultPriority() || 'MEDIUM'
    planForm.weatherImpact = '能见度良好，风力不大于4级，满足常规引航作业条件。'
    planForm.notes = '按计划时段到位，保持VHF守听；靠离泊按现场指挥执行。'

    applyBatchToAllIndividual()

    // 清理错误提示
    Object.keys(batchErrors).forEach(key => {
      batchErrors[key as keyof typeof batchErrors] = ''
    })
    individualErrors.value = individualSettings.value.map(() => ({}))

    ElMessage.success('已自动填充计划数据，可直接生成')
  } catch (e) {
    console.error(e)
    ElMessage.error('填充计划数据失败')
  } finally {
    isTestFilling.value = false
  }
}

const clearBatchError = (field: keyof typeof batchErrors) => {
  batchErrors[field] = ''
}

const clearIndividualError = (index: number, field: string) => {
  if (individualErrors.value[index]) {
    delete individualErrors.value[index][field as keyof typeof individualErrors.value[0]]
  }
}

const onIndividualLocationChange = (index: number, type: 'start' | 'end') => {
  const setting = individualSettings.value[index]
  const locationId = type === 'start' ? setting.startLocationId : setting.endLocationId
  const voyageType = setting.voyageType

  let locationType = ''
  let locationName = ''

  if (type === 'start') {
    if (voyageType === 'IMPORT') {
      // 进口船的起始位置是锚地
      locationType = 'ANCHORAGE'
      const anchorage = anchorageList.value.find(a => a.id === locationId)
      locationName = anchorage?.anchorageNameZh || ''
    } else if (voyageType === 'EXPORT') {
      // 出口船的起始位置是泊位
      locationType = 'BERTH'
      const berth = berthList.value.find(b => b.id === locationId)
      locationName = berth?.berthName || ''
    }
  } else {
    if (voyageType === 'IMPORT') {
      // 进口船的目的位置是泊位
      locationType = 'BERTH'
      const berth = berthList.value.find(b => b.id === locationId)
      locationName = berth?.berthName || ''
    } else if (voyageType === 'EXPORT') {
      // 出口船的目的位置是锚地
      locationType = 'ANCHORAGE'
      const anchorage = anchorageList.value.find(a => a.id === locationId)
      locationName = anchorage?.anchorageNameZh || ''
    }
  }

  if (type === 'start') {
    setting.startLocationType = locationType
    setting.startLocationName = locationName
    clearIndividualError(index, 'startLocationId')
  } else {
    setting.endLocationType = locationType
    setting.endLocationName = locationName
    clearIndividualError(index, 'endLocationId')
  }
}

const onIndividualPilotChange = (index: number) => {
  clearIndividualError(index, 'assignedPilotId')
}

const onIndividualVehicleChange = (index: number) => {
  clearIndividualError(index, 'assignedVehicleId')
}

const onIndividualDriverChange = (index: number) => {
  clearIndividualError(index, 'assignedDriverId')
}

const onPilotChange = () => {
  clearBatchError('assignedPilotId')
}

const onDriverChange = () => {
  clearBatchError('assignedDriverId')
}

const onVehicleChange = () => {
  clearBatchError('assignedVehicleId')
}

const loadResources = async () => {
  try {
    uiState.loading = true
    const currentPortId = 1 // 假设当前港口ID

    const [
      pilotsRes,
      driversRes,
      pilotBoatsRes,
      tugboatsRes,
      portAreasRes,
      vehiclesRes
    ] = await Promise.all([
      fetchPilotList(),
      fetchDriverList(),
      fetchPilotBoatList(),
      fetchTugBoatList(),
      fetchPortAreasByPortId(currentPortId),
      fetchVehicleList()
    ])

    pilotList.value = pilotsRes.data || []
    driverList.value = driversRes.data || []
    pilotBoatList.value = pilotBoatsRes.data || []
    tugboatList.value = tugboatsRes.data || []
    vehicleList.value = vehiclesRes.data || []
    if (portAreasRes.data && portAreasRes.data.length > 0) {
      const portAreaId = portAreasRes.data[0].id
      const wharfId = portAreasRes.data[0].wharvesId || 1

      const [anchoragesRes, berthsRes] = await Promise.all([
        fetchAnchorageListByPortAreaId(portAreaId),
        fetchBerthListByWharfId(wharfId)
      ])

      anchorageList.value = anchoragesRes.data || []
      berthList.value = berthsRes.data || []
    }
  } catch (error) {
    console.error('加载资源失败:', error)
    ElMessage.error('加载资源数据失败')
  } finally {
    uiState.loading = false
  }
}

const validateBatchForm = () => {
  let isValid = true

  Object.keys(batchErrors).forEach(key => {
    batchErrors[key as keyof typeof batchErrors] = ''
  })

  if (!planForm.planStartTime) {
    batchErrors.planStartTime = '请选择计划开始时间'
    isValid = false
  }
  if (!planForm.planEndTime) {
    batchErrors.planEndTime = '请选择计划结束时间'
    isValid = false
  }
  if (planForm.assignedPilotId === null) {
    batchErrors.assignedPilotId = '请选择引航员'
    isValid = false
  }
  if (planForm.assignedDriverId === null) {
    batchErrors.assignedDriverId = '请选择驾驶员'
    isValid = false
  }
  if (planForm.assignedVehicleId === null) {
    batchErrors.assignedVehicleId = '请选择载具'
    isValid = false
  }

  return isValid
}

const validateIndividualSettings = () => {
  let isValid = true

  individualErrors.value = individualSettings.value.map(() => ({}))

  individualSettings.value.forEach((ship, index) => {
    if (!ship.planStartTime) {
      individualErrors.value[index].planStartTime = '请选择计划开始时间'
      isValid = false
    }
    if (!ship.planEndTime) {
      individualErrors.value[index].planEndTime = '请选择计划结束时间'
      isValid = false
    }
    if (ship.assignedPilotId === null) {
      individualErrors.value[index].assignedPilotId = '请选择引航员'
      isValid = false
    }
    if (ship.assignedDriverId === null) {
      individualErrors.value[index].assignedDriverId = '请选择驾驶员'
      isValid = false
    }
    if (ship.assignedVehicleId === null) {
      individualErrors.value[index].assignedVehicleId = '请选择载具'
      isValid = false
    }
  })

  return isValid
}

const initializeIndividualSettings = () => {
  individualSettings.value = props.selectedShips.map(ship => {
    const defaultStartTime = new Date()
    defaultStartTime.setHours(defaultStartTime.getHours() + 1)
    const defaultEndTime = new Date(defaultStartTime)
    defaultEndTime.setHours(defaultEndTime.getHours() + 2)

    const inferredTypes = inferLocationTypes(ship)

    return {
      id: ship.id,
      voyageId: ship.voyageId,
      vesselId: ship.vesselId,
      nameZh: ship.nameZh || '',
      voyageType: ship.voyageType || '',
      planStartTime: planForm.planStartTime || defaultStartTime.toISOString().slice(0, 19).replace('T', ' '),
      planEndTime: planForm.planEndTime || defaultEndTime.toISOString().slice(0, 19).replace('T', ' '),
      startLocationType: inferredTypes.startLocationType,
      startLocationId: getPlannedLocation(ship).startId || null,
      startLocationName: getPlannedLocation(ship).startName || ship.plannedStartName || '',
      endLocationType: inferredTypes.endLocationType,
      endLocationId: getPlannedLocation(ship).endId || null,
      endLocationName: getPlannedLocation(ship).endName || ship.plannedEndName || '',
      assignedPilotId: planForm.assignedPilotId,
      assignedDriverId: planForm.assignedDriverId,
      assignedVehicleId: planForm.assignedVehicleId,
      assignedTugboatIds: [...planForm.selectedTugboatIds],
      assignedPilotBoatIds: [...planForm.selectedPilotBoatIds],
      weatherImpact: planForm.weatherImpact,
      notes: planForm.notes,
      priority: ship.planPriority || ship.priority || planForm.priority
    }
  })

  individualErrors.value = individualSettings.value.map(() => ({}))
}

const applyBatchToIndividual = (index: number) => {
  const ship = individualSettings.value[index]
  ship.planStartTime = planForm.planStartTime
  ship.planEndTime = planForm.planEndTime
  ship.assignedPilotId = planForm.assignedPilotId
  ship.assignedDriverId = planForm.assignedDriverId
  ship.assignedVehicleId = planForm.assignedVehicleId
  ship.assignedTugboatIds = [...planForm.selectedTugboatIds]
  ship.assignedPilotBoatIds = [...planForm.selectedPilotBoatIds]
  ship.weatherImpact = planForm.weatherImpact
  ship.notes = planForm.notes
  ship.priority = planForm.priority

  individualErrors.value[index] = {}

  ElMessage.success(`已将批量设置应用到 ${ship.nameZh}`)
}

const applyBatchToAllIndividual = () => {
  individualSettings.value.forEach((ship, index) => {
    ship.planStartTime = planForm.planStartTime
    ship.planEndTime = planForm.planEndTime
    ship.assignedPilotId = planForm.assignedPilotId
    ship.assignedDriverId = planForm.assignedDriverId
    ship.assignedVehicleId = planForm.assignedVehicleId
    ship.assignedTugboatIds = [...planForm.selectedTugboatIds]
    ship.assignedPilotBoatIds = [...planForm.selectedPilotBoatIds]
    ship.weatherImpact = planForm.weatherImpact
    ship.notes = planForm.notes
    ship.priority = planForm.priority

    individualErrors.value[index] = {}
  })

  ElMessage.success(`已将批量设置应用到所有 ${individualSettings.value.length} 艘船舶`)
}

const clearBatchForm = () => {
  planForm.planStartTime = ''
  planForm.planEndTime = ''
  planForm.assignedPilotId = null
  planForm.assignedDriverId = null
  planForm.assignedVehicleId = null
  planForm.selectedTugboatIds = []
  planForm.selectedPilotBoatIds = []
  planForm.weatherImpact = ''
  planForm.notes = ''
  planForm.priority = resolveDefaultPriority()

  Object.keys(batchErrors).forEach(key => {
    batchErrors[key as keyof typeof batchErrors] = ''
  })

  initializeIndividualSettings()
  ElMessage.success('已清空批量设置')
}

const checkResourceConflicts = async () => {
  try {
    individualLoading.value = true
    resourceConflicts.value = computeResourceConflicts(individualSettings.value)

    if (resourceConflicts.value.length === 0) {
    }
  } catch (error) {
    console.error('检查资源冲突失败:', error)
    ElMessage.error('检查资源冲突失败')
  } finally {
    individualLoading.value = false
  }
}

const handleGeneratePlan = async (publishAfterCreate: boolean | unknown = false) => {
  // 防御性处理：避免模板误把 $event 当作入参导致“生成计划”变成“生成并发布”
  const shouldPublish = publishAfterCreate === true
  try {
    let isValid = true
    let validationMessage = ''

    if (activeTab.value === 'batch') {
      if (!validateBatchForm()) {
        isValid = false
        validationMessage = '批量设置中存在未填写的必填项'
      } else {
        const shipsMissingLocation = props.selectedShips.filter(ship => {
          const loc = getPlannedLocation(ship)
          return !loc.startId || !loc.endId
        })

        if (shipsMissingLocation.length > 0) {
          const shipNames = shipsMissingLocation.map(s => s.nameZh).join(', ')
          validationMessage = `以下船舶缺少位置信息：${shipNames}`
          isValid = false
          activeTab.value = 'individual'
        } else {
          const shipsMissingTypes = props.selectedShips.filter(t => {
            return !String(t.startLocationType || '').trim() || !String(t.endLocationType || '').trim()
          })
          if (shipsMissingTypes.length > 0) {
            const shipNames = shipsMissingTypes.map(s => s.nameZh).join(', ')
            validationMessage = `以下船舶缺少位置类型（开始/结束位置类型）：${shipNames}`
            isValid = false
            activeTab.value = 'individual'
          }
        }
      }
    } else {
      if (!validateIndividualSettings()) {
        isValid = false
        validationMessage = '个别设置中存在未填写的必填项'
      }
    }

    if (!isValid) {
      ElMessage.warning(validationMessage)
      return
    }

    if (activeTab.value === 'batch') {
      await ensureLocationNames(props.selectedShips || [])
    } else {
      await ensureLocationNames(individualSettings.value || [])
    }

    if (activeTab.value === 'batch') {
      const previewSettings = props.selectedShips.map(ship => {
        const loc = getPlannedLocation(ship)
        return {
          nameZh: ship.nameZh,
          planStartTime: planForm.planStartTime,
          planEndTime: planForm.planEndTime,
          assignedPilotId: planForm.assignedPilotId,
          assignedDriverId: planForm.assignedDriverId,
          assignedVehicleId: planForm.assignedVehicleId,
          assignedTugboatIds: [...planForm.selectedTugboatIds],
          assignedPilotBoatIds: [...planForm.selectedPilotBoatIds],
          startLocationId: loc.startId,
          endLocationId: loc.endId,
        }
      })
      resourceConflicts.value = computeResourceConflicts(previewSettings)
    } else {
      resourceConflicts.value = computeResourceConflicts(individualSettings.value)
    }
    const hasErrorConflict = resourceConflicts.value.some(c => c.level === 'error')
    if (hasErrorConflict) {
      const confirm = await ElMessageBox.confirm(
          '检测到严重的资源冲突，是否继续生成计划？',
          '资源冲突警告',
          {
            confirmButtonText: '继续生成',
            cancelButtonText: '取消',
            type: 'warning'
          }
      )
      if (!confirm) return
    }

    generatingPlan.value = true

    let planDataList = []

    if (activeTab.value === 'batch') {
      planDataList = props.selectedShips.map(ship => {
        const operationTypeFlag = resolveOperationTypeFlag(ship)
        const types = inferLocationTypes(ship)
        const loc = getPlannedLocation(ship)
        const startLocationType = types.startLocationType
        const endLocationType = types.endLocationType
        const startLocationName = resolveLocationName(startLocationType, loc.startId, loc.startName || ship.plannedStartName)
        const endLocationName = resolveLocationName(endLocationType, loc.endId, loc.endName || ship.plannedEndName)

        const tugboatAssignments = planForm.selectedTugboatIds.map((tugboatId, index) => ({
          tugboatId,
          assignmentType: index === 0 ? 'MAIN' : 'ASSIST',
          estimatedStartTime: planForm.planStartTime,
          estimatedEndTime: planForm.planEndTime,
          status: 'ASSIGNED'
        }))

        const pilotBoatAssignments = planForm.selectedPilotBoatIds.map(pilotBoatId => ({
          pilotBoatId,
          purpose: '接送引航员',
          estimatedStartTime: planForm.planStartTime,
          estimatedEndTime: planForm.planEndTime,
          status: 'ASSIGNED'
        }))

        return {
          // 计划基本信息
          planStartTime: planForm.planStartTime,
          planEndTime: planForm.planEndTime,

          // 位置信息
          startLocationType,
          startLocationId: loc.startId,
          startLocationName,
          endLocationType,
          endLocationId: loc.endId,
          endLocationName,

          // 资源分配
          assignedPilotId: planForm.assignedPilotId,
          assignedDriverId: planForm.assignedDriverId,
          assignedVehicleId: planForm.assignedVehicleId,
          tugboatAssignments,
          pilotBoatAssignments,

          // 船舶和航次信息
          vesselId: ship.vesselId,
          voyageId: ship.voyageId,
          requestId: ship.id,

          // 作业类型
          operationTypeFlag,

          // 状态和优先级：生成后应为“待发布/已计划”（PLANNED）；发布动作由后续显式调用完成
          planStatus: 'PLANNED',
          priority: planForm.priority,

          // 时间信息
          boardingTime: planForm.planStartTime,
          leavingTime: planForm.planEndTime,

          // 其他信息
          weatherImpact: planForm.weatherImpact,
          notes: planForm.notes,

          createdBy: getCurrentUserId()
        }
      })
    } else {
      planDataList = individualSettings.value.map(ship => {
        const operationTypeFlag = resolveOperationTypeFlag(ship)
        const startLocationName = resolveLocationName(ship.startLocationType, ship.startLocationId, ship.startLocationName)
        const endLocationName = resolveLocationName(ship.endLocationType, ship.endLocationId, ship.endLocationName)

        const tugboatAssignments = ship.assignedTugboatIds.map((tugboatId, index) => ({
          tugboatId,
          assignmentType: index === 0 ? 'MAIN' : 'ASSIST',
          estimatedStartTime: ship.planStartTime,
          estimatedEndTime: ship.planEndTime,
          status: 'ASSIGNED'
        }))

        const pilotBoatAssignments = ship.assignedPilotBoatIds.map(pilotBoatId => ({
          pilotBoatId,
          purpose: '接送引航员',
          estimatedStartTime: ship.planStartTime,
          estimatedEndTime: ship.planEndTime,
          status: 'ASSIGNED'
        }))

        return {
          // 计划基本信息
          planStartTime: ship.planStartTime,
          planEndTime: ship.planEndTime,

          // 位置信息 - 使用plannedStartId和plannedEndId
          startLocationType: ship.startLocationType,
          startLocationId: ship.startLocationId,
          startLocationName,
          endLocationType: ship.endLocationType,
          endLocationId: ship.endLocationId,
          endLocationName,

          // 资源分配
          assignedPilotId: ship.assignedPilotId,
          assignedDriverId: ship.assignedDriverId,
          assignedVehicleId: ship.assignedVehicleId,
          tugboatAssignments,
          pilotBoatAssignments,

          // 船舶和航次信息
          vesselId: ship.vesselId,
          voyageId: ship.voyageId,
          requestId: ship.id,

          // 作业类型
          operationTypeFlag,

          planStatus: 'PLANNED',
          priority: ship.priority,

          // 时间信息
          boardingTime: ship.planStartTime,
          leavingTime: ship.planEndTime,

          // 其他信息
          weatherImpact: ship.weatherImpact,
          notes: ship.notes,

          createdBy: getCurrentUserId()
        }
      })
    }

    const response = await createPilotagePlans(planDataList)

    if (response.code === 200) {
      const createdIds: number[] = (response as any)?.data?.planIds || []

      // “生成并发布”：创建成功后立即批量发布（只发布成功创建的那部分）
      if (shouldPublish && createdIds.length > 0) {
        const pubResp: any = await batchPublishPilotagePlans(createdIds)
        if (pubResp?.code === 200) {
          ElMessage.success(`成功生成并发布 ${createdIds.length} 个引航计划`)
        } else {
          ElMessage.warning(pubResp?.message || '计划已生成，但发布失败，请到“引航计划”页面手动发布')
        }
      } else {
        ElMessage.success(`成功生成 ${planDataList.length} 个引航计划`)
      }

      const failedCount = Number((response as any)?.data?.failedCount || 0)
      if (failedCount > 0) {
        ElMessage.warning(`有 ${failedCount} 条计划生成失败，可在“引航计划”页面核对后重试`)
      }

      dialogVisible.value = false
      emit('success')
      resetFormState()
    } else {
      ElMessage.error(response.message || '生成计划失败')
    }
  } catch (error: any) {
    console.error('生成计划失败:', error)
    if (error.name !== 'ElMessageBox') {
      ElMessage.error(error.message || '生成计划失败')
    }
  } finally {
    generatingPlan.value = false
  }
}

const handleGenerateAndPublish = async () => {
  await handleGeneratePlan(true)
}

const resetFormState = () => {
  planForm.planStartTime = ''
  planForm.planEndTime = ''
  planForm.assignedPilotId = null
  planForm.assignedDriverId = null
  planForm.assignedVehicleId = null
  planForm.selectedTugboatIds = []
  planForm.selectedPilotBoatIds = []
  planForm.weatherImpact = ''
  planForm.notes = ''
  planForm.priority = resolveDefaultPriority()

  Object.keys(batchErrors).forEach(key => {
    batchErrors[key as keyof typeof batchErrors] = ''
  })

  individualSettings.value = []
  individualErrors.value = []
  resourceConflicts.value = []

  activeTab.value = 'batch'
}

const handleSaveDraft = async () => {
  try {
    savingDraft.value = true

    const draftData = {
      batchSettings: { ...planForm },
      individualSettings: [...individualSettings.value],
      timestamp: new Date().toISOString()
    }

    localStorage.setItem('pilotagePlanDraft', JSON.stringify(draftData))

    ElMessage.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const getCurrentUserId = () => {
  try {
    const authStore = useAuthStore()
    if (authStore.user?.id) {
      return authStore.user.id
    }

    // 如果 store 中没有，则尝试从 localStorage 获取
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      if (userInfo && userInfo.id) {
        return parseInt(userInfo.id)
      }
    }

    // 如果都没有找到，返回默认值 1
    return 1
  } catch (error) {
    console.error('获取当前用户ID失败:', error)
    // 出现异常时返回默认值
    return 1
  }
}

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 1 ? 'even-row' : 'odd-row'
}

const disabledDate = (date: Date) => {
  return date < new Date(Date.now() - 24 * 60 * 60 * 1000)
}

const dateShortcuts = [
  {
    text: '1小时后',
    value: () => {
      const date = new Date()
      date.setHours(date.getHours() + 1)
      return date
    }
  },
  {
    text: '2小时后',
    value: () => {
      const date = new Date()
      date.setHours(date.getHours() + 2)
      return date
    }
  },
  {
    text: '明天此时',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      return date
    }
  }
]

const handleDialogClose = () => {
  resetFormState()
}

watch(() => props.selectedShips, () => {
  initializeIndividualSettings()
}, { immediate: true, deep: true })

watch(dialogVisible, (visible) => {
  if (visible) {
    nextTick(async () => {
      await loadResources()
      initializeIndividualSettings()
      updateTableHeight()

      if (!planForm.planStartTime) {
        const now = new Date()
        now.setHours(now.getHours() + 1)
        planForm.planStartTime = now.toISOString().slice(0, 19).replace('T', ' ')

        const endTime = new Date(now)
        endTime.setHours(endTime.getHours() + 2)
        planForm.planEndTime = endTime.toISOString().slice(0, 19).replace('T', ' ')
      }
    })
  } else {
    resetFormState()
  }
})

watch(individualSettings, () => {
  clearTimeout((window as any).conflictCheckTimer)
  ;(window as any).conflictCheckTimer = setTimeout(() => {
    if (activeTab.value === 'individual') {
      checkResourceConflicts()
    }
  }, 1000)
}, { deep: true })
</script>

<style scoped lang="scss">
.generate-plan-dialog {
  .selected-ships {
    margin-bottom: 16px;
    background: var(--el-color-white);
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
    padding: 12px 12px 0;

    h3 {
      margin: 0 0 10px 0;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .plan-tabs {
    :deep(.el-tabs__content) {
      padding: 16px;
      max-height: 55vh;
      overflow-y: auto;
    }

    .tab-content {
      .plan-form {
        background: var(--el-color-white);
        border: 1px solid var(--el-border-color-light);
        border-radius: 8px;
        padding: 16px 16px 0;

        .el-form-item {
          margin-bottom: 16px;

          &:last-child {
            margin-bottom: 0;
          }

          :deep(.el-form-item__error) {
            display: none;
          }
        }

        .selected-info {
          margin-top: 8px;
          font-size: 12px;
          color: var(--el-color-primary);
        }
      }

      .batch-actions {
        padding-top: 16px;
        border-top: 1px solid var(--el-border-color-light);
      }

      &.individual-tab-content {
        display: flex;
        flex-direction: column;
        height: 100%;

        .individual-settings {
          flex: 1;
          display: flex;
          flex-direction: column;

          .batch-apply-info {
            margin-bottom: 16px;
          }

          .individual-table-container {
            flex: 1;
            overflow: hidden;

            :deep(.el-table) {
              height: 100%;

              .el-table__header-wrapper th {
                background-color: var(--el-fill-color-light);
                padding: 8px 0;
                font-weight: 600;
              }

              .even-row {
                background-color: var(--el-fill-color-lighter);
              }

              .odd-row {
                background-color: var(--el-color-white);
              }

              // 自定义滚动条样式
              .el-table__body-wrapper {
                scrollbar-width: thin;
                scrollbar-color: var(--el-color-primary-light-5) var(--el-fill-color-lighter);

                &::-webkit-scrollbar {
                  width: 8px;
                  height: 8px;
                }

                &::-webkit-scrollbar-track {
                  background: var(--el-fill-color-lighter);
                  border-radius: 4px;
                }

                &::-webkit-scrollbar-thumb {
                  background: var(--el-color-primary-light-5);
                  border-radius: 4px;

                  &:hover {
                    background: var(--el-color-primary);
                  }
                }

                &::-webkit-scrollbar-corner {
                  background: transparent;
                }
              }

              // 单元格内错误提示样式
              .field-error {
                color: var(--el-color-danger);
                font-size: 12px;
                margin-top: 4px;
                line-height: 1.2;
              }
            }
          }
        }
      }
    }
  }

  .conflict-check {
    border: 1px solid var(--el-border-color);
    border-radius: 4px;

    .no-conflict {
      padding: 8px 0;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-light);

    .footer-left,
    .footer-right {
      display: flex;
      gap: 12px;
    }
  }
}

/* 自定义滚动条样式 */
:deep(.el-tabs__content)::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-tabs__content)::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

:deep(.el-tabs__content)::-webkit-scrollbar-thumb {
  background: var(--el-color-primary-light-5);
  border-radius: 3px;

  &:hover {
    background: var(--el-color-primary);
  }
}

/* 表单错误样式 */
:deep(.el-form-item.is-error) {
  .el-input__wrapper,
  .el-select__wrapper,
  .el-date-editor {
    box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  }

  .el-form-item__error {
    position: static;
    padding-top: 4px;
    transform: none;
    font-size: 12px;
    line-height: 1.2;
  }
}
</style>