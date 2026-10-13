<template>
  <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      :close-on-click-modal="false"
      destroy-on-close
      class="generate-plan-dialog"
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
            {{ row.voyageType === 'IMPORT' ? '进港' :
              row.voyageType === 'EXPORT' ? '出港' :
                  row.voyageType === 'TRANSIT' ? '移泊' : row.voyageType }}
          </template>
        </el-table-column>
        <el-table-column prop="eta" label="预抵时间" width="150">
          <template #default="{ row }">
            {{ row.eta ? new Date(row.eta).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="plannedStartName" label="预抵锚地" width="120" />
        <el-table-column prop="plannedEndName" label="预抵泊位" width="120" />
        <el-table-column prop="plannedAnchorageNameZh" label="预抵锚地" width="120" />
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
          >
            <!-- 计划时间范围 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="计划开始时间" prop="planStartTime">
                  <el-date-picker
                      v-model="planForm.planStartTime"
                      type="datetime"
                      placeholder="选择开始时间"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm"
                      style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计划结束时间" prop="planEndTime">
                  <el-date-picker
                      v-model="planForm.planEndTime"
                      type="datetime"
                      placeholder="选择结束时间"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm"
                      style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 作业地点 - 批量设置只显示航次类型相关的字段 -->
            <el-row :gutter="20" v-if="selectedShips.length > 0">
              <el-col :span="12" v-if="isImportShip">
                <el-form-item label="起始锚地" prop="startLocation">
                  <el-select
                      v-model="planForm.startLocation"
                      filterable
                      placeholder="选择起始锚地"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="anchorage in anchorageList"
                        :key="anchorage.id"
                        :label="anchorage.anchorageNameZh"
                        :value="anchorage.anchorageNameZh"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="isExportShip">
                <el-form-item label="起始泊位" prop="startLocation">
                  <el-select
                      v-model="planForm.startLocation"
                      filterable
                      placeholder="选择起始泊位"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="berth in berthList"
                        :key="berth.id"
                        :label="berth.berthName"
                        :value="berth.berthName"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="isImportShip">
                <el-form-item label="目的泊位" prop="endLocation">
                  <el-select
                      v-model="planForm.endLocation"
                      filterable
                      placeholder="选择目的泊位"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="berth in berthList"
                        :key="berth.id"
                        :label="berth.berthName"
                        :value="berth.berthName"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="isExportShip">
                <el-form-item label="目的锚地" prop="endLocation">
                  <el-select
                      v-model="planForm.endLocation"
                      filterable
                      placeholder="选择目的锚地"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="anchorage in anchorageList"
                        :key="anchorage.id"
                        :label="anchorage.anchorageNameZh"
                        :value="anchorage.anchorageNameZh"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 资源分配 - 批量设置中引航员和驾驶员必选 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="引航员" prop="assignedPilotId">
                  <el-select
                      v-model="planForm.assignedPilotId"
                      filterable
                      placeholder="选择引航员"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="pilot in availablePilotList"
                        :key="pilot.id"
                        :label="`${pilot.pilotName} (${pilot.pilotLevel})`"
                        :value="pilot.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="驾驶员" prop="assignedDriverId">
                  <el-select
                      v-model="planForm.assignedDriverId"
                      filterable
                      placeholder="选择驾驶员"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="driver in availableDriverList"
                        :key="driver.id"
                        :label="driver.name"
                        :value="driver.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 资源分配 - 批量设置中拖轮和引航艇可选 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="引航艇">
                  <el-select
                      v-model="planForm.assignedPilotBoatIds"
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
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="拖轮">
                  <el-select
                      v-model="planForm.assignedTugboatIds"
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
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 新增：车辆选择 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="载具（车辆）" prop="assignedVehicleId">
                  <el-select
                      v-model="planForm.assignedVehicleId"
                      filterable
                      placeholder="选择载具"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="vehicle in availableVehicleList"
                        :key="vehicle.id"
                        :label="`${vehicle.licensePlate} (${vehicle.vehicleModel})`"
                        :value="vehicle.id"
                    />
                  </el-select>
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
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="单独设置" name="individual">
        <div class="tab-content">
          <div class="individual-settings">
            <el-table
                :data="individualSettings"
                border
                size="small"
                style="width: 100%"
                :row-class-name="tableRowClassName"
            >
              <el-table-column prop="nameZh" label="船名" width="120" fixed="left" />
              <el-table-column prop="voyageType" label="航次类型" width="100">
                <template #default="{ row }">
                  {{ row.voyageType === 'IMPORT' ? '进港' :
                    row.voyageType === 'EXPORT' ? '出港' :
                        row.voyageType === 'TRANSIT' ? '移泊' : row.voyageType }}
                </template>
              </el-table-column>

              <!-- 航次类型相关的字段 -->
              <el-table-column label="起始位置" width="150">
                <template #default="{ row, $index }">
                  <el-select
                      v-model="individualSettings[$index].startLocation"
                      size="small"
                      :placeholder="row.voyageType === 'IMPORT' ? '选择起始锚地' : '选择起始泊位'"
                      style="width: 100%"
                  >
                    <el-option
                        v-if="row.voyageType === 'IMPORT'"
                        v-for="anchorage in anchorageList"
                        :key="anchorage.id"
                        :label="anchorage.anchorageNameZh"
                        :value="anchorage.anchorageNameZh"
                    />
                    <el-option
                        v-if="row.voyageType === 'EXPORT'"
                        v-for="berth in berthList"
                        :key="berth.id"
                        :label="berth.berthName"
                        :value="berth.berthName"
                    />
                  </el-select>
                </template>
              </el-table-column>

              <el-table-column label="目的位置" width="150">
                <template #default="{ row, $index }">
                  <el-select
                      v-model="individualSettings[$index].endLocation"
                      size="small"
                      :placeholder="row.voyageType === 'IMPORT' ? '选择目的泊位' : '选择目的锚地'"
                      style="width: 100%"
                  >
                    <el-option
                        v-if="row.voyageType === 'IMPORT'"
                        v-for="berth in berthList"
                        :key="berth.id"
                        :label="berth.berthName"
                        :value="berth.berthName"
                    />
                    <el-option
                        v-if="row.voyageType === 'EXPORT'"
                        v-for="anchorage in anchorageList"
                        :key="anchorage.id"
                        :label="anchorage.anchorageNameZh"
                        :value="anchorage.anchorageNameZh"
                    />
                  </el-select>
                </template>
              </el-table-column>

              <!-- 计划时间 -->
              <el-table-column label="计划开始时间" width="180">
                <template #default="{ row, $index }">
                  <el-date-picker
                      v-model="individualSettings[$index].planStartTime"
                      type="datetime"
                      placeholder="开始时间"
                      size="small"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%"
                      :default-value="planForm.planStartTime"
                  />
                </template>
              </el-table-column>
              <el-table-column label="计划结束时间" width="180">
                <template #default="{ row, $index }">
                  <el-date-picker
                      v-model="individualSettings[$index].planEndTime"
                      type="datetime"
                      placeholder="结束时间"
                      size="small"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%"
                      :default-value="planForm.planEndTime"
                  />
                </template>
              </el-table-column>

              <el-table-column label="登轮时间" width="180">
                <template #default="{ row, $index }">
                  <el-date-picker
                      v-model="individualSettings[$index].boardingTime"
                      type="datetime"
                      placeholder="登轮时间"
                      size="small"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%"
                  />
                </template>
              </el-table-column>

              <!-- 引航员选择 -->
              <el-table-column label="引航员" width="150">
                <template #default="{ row, $index }">
                  <el-select
                      v-model="individualSettings[$index].assignedPilotId"
                      size="small"
                      placeholder="选择引航员"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="pilot in availablePilotList"
                        :key="pilot.id"
                        :label="`${pilot.pilotName} (${pilot.pilotLevel})`"
                        :value="pilot.id"
                    />
                  </el-select>
                </template>
              </el-table-column>

              <el-table-column label="载具" width="150">
                <template #default="{ row, $index }">
                  <el-select
                      v-model="individualSettings[$index].assignedVehicleId"
                      size="small"
                      placeholder="选择载具"
                      style="width: 100%"
                  >
                    <el-option
                        v-for="vehicle in availableVehicleList"
                        :key="vehicle.id"
                        :label="`${vehicle.licensePlate} (${vehicle.vehicleModel})`"
                        :value="vehicle.id"
                    />
                  </el-select>
                </template>
              </el-table-column>

              <!-- 备注 -->
              <el-table-column label="备注" width="200">
                <template #default="{ row, $index }">
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

              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row, $index }">
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

            <div class="batch-apply-actions" style="margin-top: 16px; text-align: center;">
              <el-button
                  type="primary"
                  size="small"
                  @click="applyBatchToAllIndividual"
              >
                将批量设置应用到所有船舶
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="loading">确认生成</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { PilotageApplication } from '@/types/pilotage'
import {fetchPortAreasByPortId} from "@/api/bases/ports.ts";
import {fetchPilotList} from "@/api/bases/pilots.ts";
import {fetchAnchorageListByPortAreaId} from "@/api/bases/anchorages.ts";
import {fetchBerthListByWharfId} from "@/api/bases/berths.ts";
import {fetchPilotBoatList} from "@/api/bases/pilotboats.ts";
import {fetchDriverList} from "@/api/bases/drivers.ts";
import {fetchTugBoatList} from "@/api/bases/tugboats.ts";
import {fetchVehicleList} from "@/api/bases/vehicles.ts";
import { useDataCenterStore } from '@/stores/dataCenter'

const props = defineProps<{
  visible: boolean
  selectedShips: PilotageApplication[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'confirm', planData: any[]): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => `为 ${props.selectedShips.length} 艘船生成引航计划`)

const batchFormRef = ref<FormInstance>()
const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('pilotage.planForm', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const activeTab = ref('batch') // 默认选中批量设置标签页
const vehicleList = ref<any[]>([]);
const availableVehicleList = computed(() =>
    vehicleList.value.filter(vehicle => vehicle.vehicleStatus === 'AVAILABLE')
);
const planForm = reactive({
  planStartTime: '',
  planEndTime: '',
  assignedPilotId: null as number | null,
  assignedDriverId: null as number | null,
  assignedPilotBoatIds: [] as number[],
  assignedTugboatIds: [] as number[],
  assignedVehicleId: null as number | null,
  startLocation: '',
  endLocation: '',
  weatherImpact: ''
});

const batchRules: FormRules = {
  planStartTime: [{ required: true, message: '请选择计划开始时间', trigger: 'change' }],
  planEndTime: [{ required: true, message: '请选择计划结束时间', trigger: 'change' }],
  assignedPilotId: [{ required: true, message: '请选择引航员', trigger: 'change' }],
  assignedDriverId: [{ required: true, message: '请选择驾驶员', trigger: 'change' }]
}

const pilotList = ref<any[]>([])
const driverList = ref<any[]>([])
const pilotBoatList = ref<any[]>([])
const tugboatList = ref<any[]>([])
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

const isImportShip = computed(() =>
    props.selectedShips.some(ship => ship.voyageType === 'IMPORT')
)

const isExportShip = computed(() =>
    props.selectedShips.some(ship => ship.voyageType === 'EXPORT')
)

const individualSettings = ref<Array<{
  id: number
  nameZh: string
  voyageType: string
  planStartTime: string
  planEndTime: string
  boardingTime: string
  startLocation: string
  endLocation: string
  assignedPilotId: number | null
  assignedDriverId: number | null
  assignedPilotBoatIds: number[]
  assignedTugboatIds: number[]
  assignedVehicleId: number | null
  weatherImpact: string
  notes: string
}>>([]);

const loadResources = async () => {
  try {
    const currentPortId = 1;

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
    ]);

    pilotList.value = pilotsRes.data || [];
    driverList.value = driversRes.data || [];
    pilotBoatList.value = pilotBoatsRes.data || [];
    tugboatList.value = tugboatsRes.data || [];
    vehicleList.value = vehiclesRes.data || []; // 存储载具列表

    if (portAreasRes.data && portAreasRes.data.length > 0) {
      const portAreaId = portAreasRes.data[0].id;
      const wharfId = portAreasRes.data[0].wharvesId || 1; // 假设码头ID为1或从港区获取

      const [anchoragesRes, berthsRes] = await Promise.all([
        fetchAnchorageListByPortAreaId(portAreaId),
        fetchBerthListByWharfId(wharfId)
      ]);

      anchorageList.value = anchoragesRes.data || [];
      berthList.value = berthsRes.data || [];
    } else {
      const [anchoragesRes, berthsRes] = await Promise.all([
        fetchAnchorageListByPortAreaId(),
        fetchBerthListByWharfId()
      ]);

      anchorageList.value = anchoragesRes.data || [];
      berthList.value = berthsRes.data || [];
    }
  } catch (error) {
    console.error('加载资源失败:', error);
    // 如果出错，尝试使用无参版本
    try {
      const [anchoragesRes, berthsRes] = await Promise.all([
        fetchAnchorageListByPortAreaId(),
        fetchBerthListByWharfId()
      ]);

      anchorageList.value = anchoragesRes.data || [];
      berthList.value = berthsRes.data || [];
    } catch (fallbackError) {
      console.error('备用方案也失败:', fallbackError);
    }
  }
};

const initializeIndividualSettings = () => {
  individualSettings.value = props.selectedShips.map(ship => ({
    id: ship.id,
    nameZh: ship.nameZh || '',
    voyageType: ship.voyageType || '',
    planStartTime: planForm.planStartTime || '',
    planEndTime: planForm.planEndTime || '',
    boardingTime: '',
    startLocation: '',
    endLocation: '',
    assignedPilotId: planForm.assignedPilotId,
    assignedDriverId: planForm.assignedDriverId,
    assignedPilotBoatIds: [...planForm.assignedPilotBoatIds],
    assignedTugboatIds: [...planForm.assignedTugboatIds],
    assignedVehicleId: planForm.assignedVehicleId,
    weatherImpact: planForm.weatherImpact,
    notes: ''
  }));
};

const applyBatchToIndividual = (index: number) => {
  const ship = individualSettings.value[index];
  ship.planStartTime = planForm.planStartTime;
  ship.planEndTime = planForm.planEndTime;
  ship.assignedPilotId = planForm.assignedPilotId;
  ship.assignedDriverId = planForm.assignedDriverId;
  ship.assignedPilotBoatIds = [...planForm.assignedPilotBoatIds];
  ship.assignedTugboatIds = [...planForm.assignedTugboatIds];
  ship.assignedVehicleId = planForm.assignedVehicleId;
  ship.weatherImpact = planForm.weatherImpact;

  ElMessage.success(`已将批量设置应用到 ${ship.nameZh}`);
};

const applyBatchToAllIndividual = () => {
  individualSettings.value.forEach(ship => {
    ship.planStartTime = planForm.planStartTime;
    ship.planEndTime = planForm.planEndTime;
    ship.assignedPilotId = planForm.assignedPilotId;
    ship.assignedDriverId = planForm.assignedDriverId;
    ship.assignedPilotBoatIds = [...planForm.assignedPilotBoatIds];
    ship.assignedTugboatIds = [...planForm.assignedTugboatIds];
    ship.assignedVehicleId = planForm.assignedVehicleId;
    ship.weatherImpact = planForm.weatherImpact;
  });

  ElMessage.success(`已将批量设置应用到所有 ${individualSettings.value.length} 艘船舶`);
};

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 1 ? 'even-row' : 'odd-row'
}
const handleConfirm = async () => {
  try {
    uiState.loading = true;

    if (activeTab.value === 'batch' && batchFormRef.value) {
      await batchFormRef.value.validate();
    }

    const invalidShips = individualSettings.value.filter(
        (ship, index) => {
          if (activeTab.value === 'individual') {
            return !ship.planStartTime || !ship.planEndTime ||
                !ship.assignedPilotId || !ship.assignedDriverId ||
                !ship.assignedVehicleId || !ship.startLocation || !ship.endLocation;
          }
          return false;
        }
    );

    if (invalidShips.length > 0) {
      activeTab.value = 'individual';
      ElMessage.warning(`有 ${invalidShips.length} 艘船舶的必填信息未设置，请完善信息`);
      return;
    }

    const planDataList = props.selectedShips.map((ship, index) => {
      const individual = individualSettings.value[index];
      const isImport = ship.voyageType === 'IMPORT';

      let operationTypeFlag = '';
      if (ship.voyageType === 'IMPORT') {
        operationTypeFlag = '进口靠泊';
      } else if (ship.voyageType === 'EXPORT') {
        operationTypeFlag = '出口离泊';
      } else {
        operationTypeFlag = ship.voyageType;
      }

      const tugboatAssignments = (individual.assignedTugboatIds.length > 0
          ? individual.assignedTugboatIds
          : planForm.assignedTugboatIds).map(tugboatId => ({
        tugboatId,
        assignmentType: 'MAIN', // 默认为主拖轮
        estimatedStartTime: individual.planStartTime || planForm.planStartTime,
        estimatedEndTime: individual.planEndTime || planForm.planEndTime
      }));

      const pilotBoatAssignments = (individual.assignedPilotBoatIds.length > 0
          ? individual.assignedPilotBoatIds
          : planForm.assignedPilotBoatIds).map(pilotBoatId => ({
        pilotBoatId,
        purpose: '接送引航员', // 默认用途
        estimatedStartTime: individual.planStartTime || planForm.planStartTime,
        estimatedEndTime: individual.planEndTime || planForm.planEndTime
      }));

      const planData = {
        // 计划基本信息
        planStartTime: individual.planStartTime || planForm.planStartTime,
        planEndTime: individual.planEndTime || planForm.planEndTime,
        startLocation: individual.startLocation || planForm.startLocation,
        endLocation: individual.endLocation || planForm.endLocation,

        // 资源分配
        assignedPilotId: individual.assignedPilotId || planForm.assignedPilotId,
        assignedDriverId: individual.assignedDriverId || planForm.assignedDriverId,
        assignedVehicleId: individual.assignedVehicleId || planForm.assignedVehicleId,

        // 船舶和航次信息
        vesselId: ship.id,
        voyageId: ship.voyageId,

        // 分配信息
        tugboatAssignments,
        pilotBoatAssignments,

        // 其他信息
        operationTypeFlag,
        weatherImpact: individual.weatherImpact || planForm.weatherImpact,
        boardingTime: individual.boardingTime || individual.planStartTime,
        leavingTime: individual.planEndTime,
        notes: individual.notes
      };

      return planData;
    });

    try {
      const response = await createPilotagePlans(planDataList);
      if (response.code === 200) {
        ElMessage.success('引航计划生成成功');
        dialogVisible.value = false;
        emit('success');
      } else {
        ElMessage.error(response.message || '生成计划失败');
      }
    } catch (error) {
      console.error('API调用失败:', error);
      ElMessage.error('生成计划失败，请检查网络连接');
    }

  } catch (error) {
    console.error('生成计划失败:', error);
    ElMessage.error('生成计划失败，请检查表单信息');
  } finally {
    uiState.loading = false;
  }
};

const getCurrentUserId = () => {
  // 这里应该从store或localStorage获取当前登录用户ID
  return 1 // 示例值
}

watch(() => props.selectedShips, () => {
  initializeIndividualSettings()
  if (props.selectedShips.length > 0) {
    const firstShip = props.selectedShips[0]
    if (firstShip.voyageType === 'IMPORT') {
      planForm.startLocation = ''
      planForm.endLocation = ''
    } else if (firstShip.voyageType === 'EXPORT') {
      planForm.startLocation = ''
      planForm.endLocation = ''
    }
  }
}, { immediate: true, deep: true })

watch(dialogVisible, (visible) => {
  if (visible) {
    nextTick(async () => {
      await loadResources()

      initializeIndividualSettings()
      if (!planForm.planStartTime) {
        const now = new Date()
        now.setHours(now.getHours() + 1) // 默认1小时后开始
        planForm.planStartTime = now.toISOString().slice(0, 19).replace('T', ' ')

        const endTime = new Date(now)
        endTime.setHours(endTime.getHours() + 2) // 默认持续2小时
        planForm.planEndTime = endTime.toISOString().slice(0, 19).replace('T', ' ')
      }
    })
  } else {
    if (batchFormRef.value) {
      batchFormRef.value.resetFields()
    }
    activeTab.value = 'batch'
  }
})
</script>

<style scoped lang="scss">
.generate-plan-dialog {
  .selected-ships {
    margin-bottom: 16px;

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
      max-height: 50vh;
      overflow-y: auto;
    }

    .tab-content {
      .plan-form {
        .el-form-item {
          margin-bottom: 16px;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }

  .individual-settings {
    :deep(.el-table) {
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
    }

    .batch-apply-actions {
      padding: 12px 0;
      border-top: 1px solid var(--el-border-color);
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-light);
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
</style>