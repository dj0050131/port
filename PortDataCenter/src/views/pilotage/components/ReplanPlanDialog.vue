<template>
  <el-dialog
    v-model="visibleProxy"
    title="重排引航计划（重新排资源）"
    width="900px"
    top="6vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form label-width="120px" size="small" v-loading="uiState.loading">
      <el-alert
        v-if="replanInfo.visible"
        type="warning"
        :closable="false"
        style="margin-bottom: 12px"
      >
        <div>
          <div><b>该计划为【待重排】</b>：{{ replanInfo.reason }}</div>
          <div v-if="replanInfo.resourceType"><b>触发资源</b>：{{ replanInfo.resourceTypeLabel }} {{ replanInfo.resourceName || ('#' + replanInfo.resourceId) }}</div>
          <div>为避免再次分配到不可执行资源，触发资源已在下方列表中禁选。</div>
        </div>
      </el-alert>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="计划开始时间">
            <el-date-picker v-model="form.planStartTime" type="datetime" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划结束时间">
            <el-date-picker v-model="form.planEndTime" type="datetime" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-select v-model="form.priority" placeholder="选择优先级" style="width: 100%">
              <el-option label="低" value="LOW" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="高" value="HIGH" />
              <el-option label="紧急" value="URGENT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>资源</el-divider>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="引航员">
            <el-select v-model="form.assignedPilotId" filterable clearable style="width: 100%">
              <el-option
                v-for="p in pilots"
                :key="p.id"
                :label="`${p.pilotName} (${p.pilotLevel})`"
                :value="p.id"
                :disabled="(p.onDutyStatus !== 'ON_DUTY' && p.id !== form.assignedPilotId) || (replanInfo.resourceType === 'PILOT' && p.id === replanInfo.resourceId)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="驾驶员">
            <el-select v-model="form.assignedDriverId" filterable clearable style="width: 100%">
              <el-option
                v-for="d in drivers"
                :key="d.id"
                :label="d.name"
                :value="d.id"
                :disabled="(d.onDutyStatus !== 'ON_DUTY' && d.id !== form.assignedDriverId) || (replanInfo.resourceType === 'DRIVER' && d.id === replanInfo.resourceId)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="载具（车辆）">
            <el-select v-model="form.assignedVehicleId" filterable clearable style="width: 100%">
              <el-option
                v-for="v in vehicles"
                :key="v.id"
                :label="`${v.licensePlate} (${v.vehicleModel})`"
                :value="v.id"
                :disabled="(v.vehicleStatus !== 'AVAILABLE' && v.id !== form.assignedVehicleId) || (replanInfo.resourceType === 'VEHICLE' && v.id === replanInfo.resourceId)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="拖轮（可多选）">
            <el-select v-model="form.selectedTugboatIds" multiple filterable style="width: 100%">
              <el-option
                v-for="t in tugboats"
                :key="t.id"
                :label="t.tugboatName"
                :value="t.id"
                :disabled="(t.status !== 'AVAILABLE' && !form.selectedTugboatIds.includes(t.id)) || (replanInfo.resourceType === 'TUGBOAT' && t.id === replanInfo.resourceId)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="引航艇（可多选）">
            <el-select v-model="form.selectedPilotBoatIds" multiple filterable style="width: 100%">
              <el-option
                v-for="b in pilotBoats"
                :key="b.id"
                :label="b.boatName"
                :value="b.id"
                :disabled="(b.currentStatus !== 'AVAILABLE' && !form.selectedPilotBoatIds.includes(b.id)) || (replanInfo.resourceType === 'PILOT_BOAT' && b.id === replanInfo.resourceId)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>备注</el-divider>
      <el-form-item label="天气影响说明">
        <el-input v-model="form.weatherImpact" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.notes" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visibleProxy = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认重排</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getPlanDetail, replanPlan } from '@/api/pilotagePlan'
import { fetchPilotList } from '@/api/bases/pilots'
import { fetchDriverList } from '@/api/bases/drivers'
import { fetchVehicleList } from '@/api/bases/vehicles'
import { fetchTugBoatList } from '@/api/bases/tugboats'
import { fetchPilotBoatList } from '@/api/bases/pilotboats'
import { useDataCenterStore } from '@/stores/dataCenter'

const formatDateToBackend = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const props = defineProps<{
  visible: boolean
  planId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'success'): void
}>()

const visibleProxy = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v),
})

const dataCenterStore = useDataCenterStore()
const uiState = dataCenterStore.ensureTable<any>('pilotage.replanDialog', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const submitting = ref(false)

const pilots = ref<any[]>([])
const drivers = ref<any[]>([])
const vehicles = ref<any[]>([])
const tugboats = ref<any[]>([])
const pilotBoats = ref<any[]>([])

const form = reactive({
  planStartTime: null as Date | null,
  planEndTime: null as Date | null,
  priority: 'MEDIUM' as string,
  assignedPilotId: null as number | null,
  assignedDriverId: null as number | null,
  assignedVehicleId: null as number | null,
  selectedTugboatIds: [] as number[],
  selectedPilotBoatIds: [] as number[],
  weatherImpact: '',
  notes: '',
})

const replanInfo = reactive({
  visible: false,
  reason: '',
  resourceType: '' as string,
  resourceTypeLabel: '',
  resourceId: 0 as number,
  resourceName: '' as string,
})

const loadDicts = async () => {
  const [pRes, dRes, vRes, tRes, bRes] = await Promise.all([
    fetchPilotList(),
    fetchDriverList(),
    fetchVehicleList(),
    fetchTugBoatList(),
    fetchPilotBoatList(),
  ])
  pilots.value = (pRes as any).data || []
  drivers.value = (dRes as any).data || []
  vehicles.value = (vRes as any).data || []
  tugboats.value = (tRes as any).data || []
  pilotBoats.value = (bRes as any).data || []
}

const loadPlan = async () => {
  if (!props.planId) return
  uiState.loading = true
  try {
    await loadDicts()
    const res = await getPlanDetail(props.planId)
    const data = (res as any).data || {}

    replanInfo.visible = data.planStatus === 'REPLAN_REQUIRED'
    replanInfo.reason = data.replanRequiredReason || '因资源取消导致计划需重新编排'
    replanInfo.resourceType = data.replanRequiredResourceType || ''
    replanInfo.resourceId = data.replanRequiredResourceId || 0
    replanInfo.resourceName = data.replanRequiredResourceName || ''
    const typeMap: Record<string, string> = {
      PILOT: '引航员',
      DRIVER: '驾驶员',
      VEHICLE: '车辆',
      TUGBOAT: '拖轮',
      PILOT_BOAT: '引航艇',
    }
    replanInfo.resourceTypeLabel = typeMap[replanInfo.resourceType] || replanInfo.resourceType

    form.planStartTime = data.planStartTime ? new Date(data.planStartTime) : null
    form.planEndTime = data.planEndTime ? new Date(data.planEndTime) : null
    form.priority = data.priority || 'MEDIUM'
    form.assignedPilotId = data.assignedPilotId ?? null
    form.assignedDriverId = data.assignedDriverId ?? null
    form.assignedVehicleId = data.assignedVehicleId ?? null
    form.weatherImpact = data.weatherImpact || ''
    form.notes = data.notes || ''

    form.selectedTugboatIds = (data.tugboatAssignments || []).map((x: any) => x.tugboatId).filter(Boolean)
    form.selectedPilotBoatIds = (data.pilotBoatAssignments || []).map((x: any) => x.pilotBoatId).filter(Boolean)

    // 若为“待重排(资源取消触发)”，触发资源应被强制移出当前分配，避免出现“无法删除/替换”的锁死体验
    if (replanInfo.visible && replanInfo.resourceType && replanInfo.resourceId) {
      const rid = Number(replanInfo.resourceId)
      switch (replanInfo.resourceType) {
        case 'PILOT':
          if (form.assignedPilotId === rid) form.assignedPilotId = null
          break
        case 'DRIVER':
          if (form.assignedDriverId === rid) form.assignedDriverId = null
          break
        case 'VEHICLE':
          if (form.assignedVehicleId === rid) form.assignedVehicleId = null
          break
        case 'TUGBOAT':
          form.selectedTugboatIds = form.selectedTugboatIds.filter(id => id !== rid)
          break
        case 'PILOT_BOAT':
          form.selectedPilotBoatIds = form.selectedPilotBoatIds.filter(id => id !== rid)
          break
        default:
          break
      }
    }
  } finally {
    uiState.loading = false
  }
}

watch(() => visibleProxy.value, (v) => {
  if (v) loadPlan()
})

const submit = async () => {
  if (!props.planId) return
  if (!form.planStartTime || !form.planEndTime) {
    ElMessage.warning('请选择计划开始/结束时间')
    return
  }
  submitting.value = true
  try {
    const planStartTimeStr = formatDateToBackend(form.planStartTime)
    const planEndTimeStr = formatDateToBackend(form.planEndTime)

    const tugboatAssignments = form.selectedTugboatIds.map((id, idx) => ({
      tugboatId: id,
      assignmentType: idx === 0 ? 'MAIN' : 'ASSIST',
      estimatedStartTime: planStartTimeStr,
      estimatedEndTime: planEndTimeStr,
    }))
    const pilotBoatAssignments = form.selectedPilotBoatIds.map((id) => ({
      pilotBoatId: id,
      purpose: '接送引航员',
      estimatedStartTime: planStartTimeStr,
      estimatedEndTime: planEndTimeStr,
    }))

    const res = await replanPlan(props.planId, {
      planStartTime: planStartTimeStr,
      planEndTime: planEndTimeStr,
      priority: form.priority,
      assignedPilotId: form.assignedPilotId,
      assignedDriverId: form.assignedDriverId,
      assignedVehicleId: form.assignedVehicleId,
      tugboatAssignments,
      pilotBoatAssignments,
      weatherImpact: form.weatherImpact,
      notes: form.notes,
    })
    if ((res as any).code === 200) {
      ElMessage.success('重排成功')
      visibleProxy.value = false
      emit('success')
    } else {
      ElMessage.error((res as any).message || '重排失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>
