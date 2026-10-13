<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { PilotageRoutes } from '@/types/bases/pilotageRoutes'
import {
  getPilotageRouteList,
  createPilotageRoute,
  updatePilotageRoute,
  deletePilotageRoute
} from '@/api/bases/pilotageRoutes'
import { usePortsStore } from '@/stores/ports.ts'
import { getBerthsById, createBerths } from '@/api/bases/berths'
import { getAnchoragesById, createAnchorages } from '@/api/bases/anchorages'
import type { Berths } from '@/types/bases/berths'
import type { Anchorages } from '@/types/bases/anchorages'
import { BERTH_TYPE_OPTIONS, berthTypeLabel } from '@/utils/berth-utils'
import { useDataCenterStore } from '@/stores/dataCenter'
import { fetchWharvesByPortAreaId, fetchAnchoragesByPortAreaId, fetchBerthsByWharvesId } from '@/api/bases/ports'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<PilotageRoutes>('billing.directRoute', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const portsStore = usePortsStore()
const portAreas = computed(() => portsStore.portAreas)

// 之前只有一套“港区/码头筛选”，无法添加“跨港区”的锚地→泊位区间；这里改为起点/终点各自独立筛选
const uiStartPortAreaId = ref<number | null>(null)
const uiStartWharfId = ref<number | null>(null)
const uiEndPortAreaId = ref<number | null>(null)
const uiEndWharfId = ref<number | null>(null)

const startWharves = ref<any[]>([])
const endWharves = ref<any[]>([])
const startAnchorages = ref<any[]>([])
const endAnchorages = ref<any[]>([])
const startBerths = ref<any[]>([])
const endBerths = ref<any[]>([])

// 为了“编辑时能回显名称”，把“当前选中的泊位/锚地”塞进临时选项里
const extraBerths = ref<any[]>([])
const extraAnchorages = ref<any[]>([])

const currentRoute = reactive<PilotageRoutes>({
  id: 0,
  routeCode: '',
  routeType: '',
  routeName: '',
  routeNotes: '',
  distanceNautical: 0,
  defaultDuration: 0,
  status: 'ENABLED',
  dangerLevel: '',
  startLocationType: '',
  startLocationId: 0,
  startLocationName: '',
  endLocationType: '',
  endLocationId: 0,
  endLocationName: '',
  pilotageDirection: '',
  applicableVesselMinNetTon: 0,
  applicableVesselMaxNetTon: 0,
  daylightOnly: false,
  tideDependent: false,
  createdAt: '',
  updatedAt: ''
})

const formRules = {
  routeCode: [{ required: true, message: '请输入区间编号', trigger: 'blur' }],
  routeName: [{ required: true, message: '请输入区间名称', trigger: 'blur' }],
  // status 允许为 null（DB 允许），避免历史数据为 null 时无法编辑其它字段
  startLocationType: [{ required: true, message: '请选择起始位置类型', trigger: 'change' }],
  startLocationId: [{ required: true, message: '请选择起始位置', trigger: 'change' }],
  endLocationType: [{ required: true, message: '请选择结束位置类型', trigger: 'change' }],
  endLocationId: [{ required: true, message: '请选择结束位置', trigger: 'change' }]
}

const statusLabel = (s: string) => (s === 'ENABLED' ? '启用' : s === 'DISABLED' ? '禁用' : s)
const statusTagType = (s: string) => (s === 'ENABLED' ? 'success' : s === 'DISABLED' ? 'info' : 'info')

const locationTypeOptions = [
  { value: 'BERTH', label: '泊位' },
  { value: 'ANCHORAGE', label: '锚地' }
]
const statusOptions = [
  { value: 'ENABLED', label: '启用' },
  { value: 'DISABLED', label: '禁用' }
]

const dangerLevelOptions = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' }
]

const pilotageDirectionOptions = [
  { value: 'IMPORT', label: '进港' },
  { value: 'EXPORT', label: '出港' },
  { value: 'SHIFTING', label: '移泊' },
  { value: 'OTHER', label: '其他' }
]

const routeTypeOptions = [
  { value: 'DIRECT', label: '直航' },
  { value: 'STANDARD', label: '标准' },
  { value: 'TEMP', label: '临时' }
]

const startLocationOptions = computed(() => {
  if (currentRoute.startLocationType === 'BERTH') return [...extraBerths.value, ...(startBerths.value || [])]
  if (currentRoute.startLocationType === 'ANCHORAGE') return [...extraAnchorages.value, ...(startAnchorages.value || [])]
  return []
})

const endLocationOptions = computed(() => {
  if (currentRoute.endLocationType === 'BERTH') return [...extraBerths.value, ...(endBerths.value || [])]
  if (currentRoute.endLocationType === 'ANCHORAGE') return [...extraAnchorages.value, ...(endAnchorages.value || [])]
  return []
})

const portAreaLabelById = (id?: number | null) => {
  if (!id) return ''
  const pa: any = (portAreas.value || []).find((x: any) => Number(x.id) === Number(id))
  return pa?.areaNameZh || pa?.portAreaNameZh || pa?.areaCode || String(pa?.id ?? id)
}

const startPortAreaLabel = computed(() => portAreaLabelById(uiStartPortAreaId.value))
const endPortAreaLabel = computed(() => portAreaLabelById(uiEndPortAreaId.value))

const wharfLabelByList = (list: any[], id?: number | null) => {
  if (!id) return ''
  const w: any = (list || []).find((x: any) => Number(x.id) === Number(id))
  return w?.wharfNameZh || w?.wharfNameEn || w?.wharfCode || String(w?.id ?? id)
}
const startWharfLabel = computed(() => wharfLabelByList(startWharves.value, uiStartWharfId.value))
const endWharfLabel = computed(() => wharfLabelByList(endWharves.value, uiEndWharfId.value))

const berthTypeOptions = computed(() => {
  const list: any[] = [...(startBerths.value || []), ...(endBerths.value || []), ...extraBerths.value]
  const uniq = Array.from(new Set(list.map(x => x?.berthType).filter((x: any) => !!x))).map((t: any) => String(t))
  const fromData = uniq.map((v: string) => ({ value: v, label: berthTypeLabel(v) }))
  const merged = [...BERTH_TYPE_OPTIONS, ...fromData]
  const seen = new Set<string>()
  return merged.filter(o => {
    if (!o?.value) return false
    if (seen.has(o.value)) return false
    seen.add(o.value)
    return true
  })
})

const loadWharves = async (portAreaId: number) => {
  const res: any = await fetchWharvesByPortAreaId(portAreaId)
  return (res?.data || []) as any[]
}
const loadAnchorages = async (portAreaId: number) => {
  const res: any = await fetchAnchoragesByPortAreaId(portAreaId)
  return (res?.data || []) as any[]
}
const loadBerths = async (wharfId: number) => {
  const res: any = await fetchBerthsByWharvesId(wharfId)
  return (res?.data || []) as any[]
}

watch(uiStartPortAreaId, async (val) => {
  startWharves.value = []
  startAnchorages.value = []
  startBerths.value = []
  uiStartWharfId.value = null
  if (!val) return
  const [ws, as] = await Promise.all([loadWharves(val), loadAnchorages(val)])
  startWharves.value = ws
  startAnchorages.value = as
})

watch(uiStartWharfId, async (val) => {
  startBerths.value = []
  if (!val) return
  startBerths.value = await loadBerths(val)
})

watch(uiEndPortAreaId, async (val) => {
  endWharves.value = []
  endAnchorages.value = []
  endBerths.value = []
  uiEndWharfId.value = null
  if (!val) return
  const [ws, as] = await Promise.all([loadWharves(val), loadAnchorages(val)])
  endWharves.value = ws
  endAnchorages.value = as
})

watch(uiEndWharfId, async (val) => {
  endBerths.value = []
  if (!val) return
  endBerths.value = await loadBerths(val)
})

watch(() => currentRoute.startLocationType, () => {
  currentRoute.startLocationId = 0
  currentRoute.startLocationName = ''
})

watch(() => currentRoute.endLocationType, () => {
  currentRoute.endLocationId = 0
  currentRoute.endLocationName = ''
})

const handleStartLocationChange = (id: number) => {
  const opt = startLocationOptions.value.find((x: any) => x.id === id)
  currentRoute.startLocationName = opt?.berthName || opt?.anchorageNameZh || ''
}

const handleEndLocationChange = (id: number) => {
  const opt = endLocationOptions.value.find((x: any) => x.id === id)
  currentRoute.endLocationName = opt?.berthName || opt?.anchorageNameZh || ''
}

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getPilotageRouteList()
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
  Object.assign(currentRoute, {
    id: 0,
    routeCode: '',
    routeType: 'DIRECT',
    routeName: '',
    routeNotes: '',
    distanceNautical: 0,
    defaultDuration: 0,
    status: 'ENABLED',
    dangerLevel: 'LOW',
    startLocationType: '',
    startLocationId: 0,
    startLocationName: '',
    endLocationType: '',
    endLocationId: 0,
    endLocationName: '',
    pilotageDirection: '',
    applicableVesselMinNetTon: 0,
    applicableVesselMaxNetTon: 0,
    daylightOnly: false,
    tideDependent: false,
    createdAt: '',
    updatedAt: ''
  })
  uiStartPortAreaId.value = null
  uiStartWharfId.value = null
  uiEndPortAreaId.value = null
  uiEndWharfId.value = null
  startWharves.value = []
  endWharves.value = []
  startAnchorages.value = []
  endAnchorages.value = []
  startBerths.value = []
  endBerths.value = []
  dialogTitle.value = '添加直航区间'
  dialogVisible.value = true
}

const handleEdit = (row: PilotageRoutes) => {
  Object.assign(currentRoute, row)
  // 编辑时仅用于选择的联动字段无法从后端反推，保留为空；用户可重新选择以修正 ID/名称
  uiStartPortAreaId.value = null
  uiStartWharfId.value = null
  uiEndPortAreaId.value = null
  uiEndWharfId.value = null
  startWharves.value = []
  endWharves.value = []
  startAnchorages.value = []
  endAnchorages.value = []
  startBerths.value = []
  endBerths.value = []
  extraBerths.value = []
  extraAnchorages.value = []
  prefillLocationUiForEdit(row).catch(() => {})
  dialogTitle.value = '编辑直航区间'
  dialogVisible.value = true
}

const prefillLocationUiForEdit = async (row: PilotageRoutes) => {
  const prefillOneSide = async (side: 'start' | 'end', type: string, id: number) => {
    if (!type || !id) return
    if (type === 'BERTH') {
      const res: any = await getBerthsById(id)
      if (res?.code === 200 && res.data) {
        const b: any = res.data
        extraBerths.value = extraBerths.value.some(x => x.id === b.id) ? extraBerths.value : [...extraBerths.value, b]
        const paId = b.portAreaId ? Number(b.portAreaId) : null
        const whId = b.wharfId ? Number(b.wharfId) : null
        if (side === 'start') {
          uiStartPortAreaId.value = paId
          if (paId) {
            const [ws, as] = await Promise.all([loadWharves(paId), loadAnchorages(paId)])
            startWharves.value = ws
            startAnchorages.value = as
          }
          uiStartWharfId.value = whId
          if (whId) startBerths.value = await loadBerths(whId)
        } else {
          uiEndPortAreaId.value = paId
          if (paId) {
            const [ws, as] = await Promise.all([loadWharves(paId), loadAnchorages(paId)])
            endWharves.value = ws
            endAnchorages.value = as
          }
          uiEndWharfId.value = whId
          if (whId) endBerths.value = await loadBerths(whId)
        }
      }
      return
    }
    if (type === 'ANCHORAGE') {
      const res: any = await getAnchoragesById(id)
      if (res?.code === 200 && res.data) {
        const a: any = res.data
        extraAnchorages.value = extraAnchorages.value.some(x => x.id === a.id) ? extraAnchorages.value : [...extraAnchorages.value, a]
        const paId = a.portAreaId ? Number(a.portAreaId) : null
        if (side === 'start') {
          uiStartPortAreaId.value = paId
          if (paId) {
            const [ws, as] = await Promise.all([loadWharves(paId), loadAnchorages(paId)])
            startWharves.value = ws
            startAnchorages.value = as
          }
        } else {
          uiEndPortAreaId.value = paId
          if (paId) {
            const [ws, as] = await Promise.all([loadWharves(paId), loadAnchorages(paId)])
            endWharves.value = ws
            endAnchorages.value = as
          }
        }
      }
    }
  }

  await Promise.all([
    prefillOneSide('start', String(row.startLocationType || ''), Number(row.startLocationId || 0)),
    prefillOneSide('end', String(row.endLocationType || ''), Number(row.endLocationId || 0)),
  ])
}

const addLocationDialogVisible = ref(false)
const addLocationType = ref<'BERTH' | 'ANCHORAGE'>('BERTH')
const addLocationFor = ref<'start' | 'end'>('start')
const addLocationLoading = ref(false)

const addBerthForm = reactive<Partial<Berths>>({
  berthCode: '',
  berthName: '',
  berthType: '',
  portAreaId: undefined,
  wharfId: undefined
})
const addAnchorageForm = reactive<Partial<Anchorages>>({
  anchorageCode: '',
  anchorageNameZh: '',
  anchorageNameEn: '',
  anchorageType: 'WAITING',
  portAreaId: undefined,
  longitude: undefined,
  latitude: undefined
})

const openAddLocation = (forField: 'start' | 'end', type: 'BERTH' | 'ANCHORAGE') => {
  addLocationFor.value = forField
  addLocationType.value = type
  addLocationLoading.value = false
  if (type === 'BERTH') {
    Object.assign(addBerthForm, {
      berthCode: '',
      berthName: '',
      berthType: '',
      portAreaId: (forField === 'start' ? uiStartPortAreaId.value : uiEndPortAreaId.value) || undefined,
      wharfId: (forField === 'start' ? uiStartWharfId.value : uiEndWharfId.value) || undefined
    })
  } else {
    Object.assign(addAnchorageForm, {
      anchorageCode: '',
      anchorageNameZh: '',
      anchorageNameEn: '',
      anchorageType: 'WAITING',
      portAreaId: (forField === 'start' ? uiStartPortAreaId.value : uiEndPortAreaId.value) || undefined,
      longitude: undefined,
      latitude: undefined
    })
  }
  addLocationDialogVisible.value = true
}

const submitAddLocation = async () => {
  addLocationLoading.value = true
  try {
    if (addLocationType.value === 'BERTH') {
      const paId = addLocationFor.value === 'start' ? uiStartPortAreaId.value : uiEndPortAreaId.value
      const whId = addLocationFor.value === 'start' ? uiStartWharfId.value : uiEndWharfId.value
      if (!paId) {
        ElMessage.warning('请先选择港区')
        return
      }
      if (!whId) {
        ElMessage.warning('新增泊位前请先选择码头')
        return
      }
      if (!addBerthForm.berthCode || !addBerthForm.berthName) {
        ElMessage.warning('请填写泊位代码和泊位名称')
        return
      }
      if (!addBerthForm.berthType) {
        ElMessage.warning('请选择或输入泊位类型')
        return
      }
      const pa = portAreas.value.find((x: any) => Number(x.id) === Number(paId))
      const payload: any = {
        ...addBerthForm,
        portAreaId: paId,
        wharfId: whId,
        portArea: pa?.areaNameZh || pa?.portAreaNameZh || pa?.areaCode || (addLocationFor.value === 'start' ? startPortAreaLabel.value : endPortAreaLabel.value) || ''
      }
      const res = await createBerths(payload)
      if (res.code === 200) {
        if (addLocationFor.value === 'start') {
          startBerths.value = await loadBerths(whId)
        } else {
          endBerths.value = await loadBerths(whId)
        }
        const detail = await getBerthsById(res.data as any)
        if (detail.code === 200 && detail.data) {
          extraBerths.value = extraBerths.value.some(x => x.id === detail.data.id) ? extraBerths.value : [...extraBerths.value, detail.data as any]
          if (addLocationFor.value === 'start') {
            currentRoute.startLocationType = 'BERTH'
            currentRoute.startLocationId = (detail.data as any).id
            handleStartLocationChange((detail.data as any).id)
          } else {
            currentRoute.endLocationType = 'BERTH'
            currentRoute.endLocationId = (detail.data as any).id
            handleEndLocationChange((detail.data as any).id)
          }
        }
        ElMessage.success('新增泊位成功')
        addLocationDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '新增泊位失败')
      }
    } else {
      const paId = addLocationFor.value === 'start' ? uiStartPortAreaId.value : uiEndPortAreaId.value
      if (!paId) {
        ElMessage.warning('请先选择港区')
        return
      }
      if (!addAnchorageForm.anchorageCode || !addAnchorageForm.anchorageNameZh) {
        ElMessage.warning('请填写锚地代码和中文名称')
        return
      }
      if (addAnchorageForm.longitude === undefined || addAnchorageForm.latitude === undefined) {
        ElMessage.warning('请填写锚地经纬度')
        return
      }
      const payload: any = {
        ...addAnchorageForm,
        portAreaId: paId
      }
      const res = await createAnchorages(payload)
      if (res.code === 200) {
        if (addLocationFor.value === 'start') {
          startAnchorages.value = await loadAnchorages(paId)
        } else {
          endAnchorages.value = await loadAnchorages(paId)
        }
        const detail = await getAnchoragesById(res.data as any)
        if (detail.code === 200 && detail.data) {
          extraAnchorages.value = extraAnchorages.value.some(x => x.id === detail.data.id) ? extraAnchorages.value : [...extraAnchorages.value, detail.data as any]
          if (addLocationFor.value === 'start') {
            currentRoute.startLocationType = 'ANCHORAGE'
            currentRoute.startLocationId = (detail.data as any).id
            handleStartLocationChange((detail.data as any).id)
          } else {
            currentRoute.endLocationType = 'ANCHORAGE'
            currentRoute.endLocationId = (detail.data as any).id
            handleEndLocationChange((detail.data as any).id)
          }
        }
        ElMessage.success('新增锚地成功')
        addLocationDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '新增锚地失败')
      }
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('新增位置失败')
  } finally {
    addLocationLoading.value = false
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()

    const payload: any = { ...currentRoute }
    if (!payload.pilotageDirection) {
      const s = String(payload.startLocationType || '').trim()
      const e = String(payload.endLocationType || '').trim()
      if (s === 'ANCHORAGE' && e === 'BERTH') payload.pilotageDirection = 'IMPORT'
      else if (s === 'BERTH' && e === 'ANCHORAGE') payload.pilotageDirection = 'EXPORT'
      else if (s === 'BERTH' && e === 'BERTH') payload.pilotageDirection = 'SHIFTING'
    }

    // create/update 都不需要前端传 createdAt/updatedAt
    delete payload.createdAt
    delete payload.updatedAt

    ;['dangerLevel', 'pilotageDirection', 'status', 'routeType', 'routeNotes'].forEach((k) => {
      if (payload[k] === '') delete payload[k]
    })

    ;['startLocationId', 'endLocationId'].forEach((k) => {
      if (payload[k] == null) return
      const n = Number(payload[k])
      if (!Number.isFinite(n) || n <= 0) delete payload[k]
    })
    ;['startLocationType', 'endLocationType', 'startLocationName', 'endLocationName'].forEach((k) => {
      if (payload[k] === '') delete payload[k]
    })

    let res
    if (payload.id === 0) {
      delete payload.id
      res = await createPilotageRoute(payload)
    } else {
      const { id, ...updateData } = payload
      res = await updatePilotageRoute(id, updateData)
    }
    
    if (res.code === 200) {
      ElMessage.success(currentRoute.id === 0 ? '新增成功' : '更新成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error((currentRoute.id === 0 ? '新增失败: ' : '更新失败: ') + res.message)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row: PilotageRoutes) => {
  try {
    await ElMessageBox.confirm(`确定要删除直航区间"${row.routeName}"吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await deletePilotageRoute(row.id)
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
  portsStore.fetchPorts()
  portsStore.fetchPortAreas(1)
  fetchData()
})
</script>

<template>
  <div class="direct-route-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">直航区间设置</h2>
        <p class="page-desc">管理直航区间及其计费里程</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
        <el-button type="success" :icon="Plus" @click="handleCreate">添加</el-button>
      </div>
    </div>

    <div class="table-container glass-card">
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%">
          <el-table-column type="selection" width="40" />
          <el-table-column prop="routeName" label="直航区间" min-width="200" />
          <el-table-column prop="routeCode" label="区间编号" width="140" show-overflow-tooltip />
          <el-table-column prop="routeType" label="区间类型" width="120" show-overflow-tooltip />
          <el-table-column prop="distanceNautical" label="计费里程" width="120" />
          <el-table-column prop="defaultDuration" label="默认时长(分)" width="120" />
          <el-table-column prop="routeNotes" label="备注" min-width="150" />
          <el-table-column prop="startLocationName" label="起点" width="160" show-overflow-tooltip />
          <el-table-column prop="endLocationName" label="终点" width="160" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
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
      width="500px"
      destroy-on-close
      class="billing-dialog"
    >
      <div style="padding: 20px;">
        <el-form 
          ref="formRef"
          :model="currentRoute" 
          :rules="formRules"
          label-width="120px" 
          size="default"
        >
          <div class="section-label">费收区间</div>
          <el-form-item label="区间编号:" prop="routeCode">
            <el-input v-model="currentRoute.routeCode" />
          </el-form-item>
          <el-form-item label="区间名称:" prop="routeName">
            <el-input v-model="currentRoute.routeName" />
          </el-form-item>
          <el-form-item label="区间类型:">
            <el-select
              v-model="currentRoute.routeType"
              style="width: 100%"
              filterable
              clearable
              allow-create
              default-first-option
              placeholder="请选择或输入（推荐：DIRECT）"
            >
              <el-option v-for="t in routeTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态:" prop="status">
            <el-select v-model="currentRoute.status" style="width: 100%">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="危险等级:">
            <el-select v-model="currentRoute.dangerLevel" style="width: 100%" clearable placeholder="默认低">
              <el-option v-for="d in dangerLevelOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="引航方向:">
            <el-select v-model="currentRoute.pilotageDirection" style="width: 100%" clearable placeholder="可留空，保存时可自动推断">
              <el-option v-for="d in pilotageDirectionOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>

          <div class="section-label">位置</div>
          <el-form-item label="起点港区(用于筛选):">
            <el-select v-model="uiStartPortAreaId" style="width: 100%" placeholder="请选择起点港区" clearable>
              <el-option v-for="p in portAreas" :key="p.id" :label="(p as any).areaNameZh || (p as any).portAreaNameZh || (p as any).areaCode || String((p as any).id)" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="起点码头(用于泊位):" v-if="currentRoute.startLocationType === 'BERTH'">
            <el-select v-model="uiStartWharfId" style="width: 100%" placeholder="请选择起点码头" clearable>
              <el-option v-for="w in startWharves" :key="w.id" :label="w.wharfNameZh" :value="w.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="起始位置类型:" prop="startLocationType">
            <el-select v-model="currentRoute.startLocationType" style="width: 100%">
              <el-option v-for="t in locationTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="起始位置:" prop="startLocationId">
            <div style="display:flex; gap:8px; width:100%;">
              <el-select v-model="currentRoute.startLocationId" style="flex:1" filterable @change="handleStartLocationChange">
                <el-option
                  v-for="item in startLocationOptions"
                  :key="item.id"
                  :label="item.berthName || item.anchorageNameZh"
                  :value="item.id"
                />
              </el-select>
              <el-button
                v-if="currentRoute.startLocationType === 'BERTH'"
                type="primary"
                plain
                @click="openAddLocation('start','BERTH')"
              >新增泊位</el-button>
              <el-button
                v-if="currentRoute.startLocationType === 'ANCHORAGE'"
                type="primary"
                plain
                @click="openAddLocation('start','ANCHORAGE')"
              >新增锚地</el-button>
            </div>
          </el-form-item>

          <el-form-item label="结束位置类型:" prop="endLocationType">
            <el-select v-model="currentRoute.endLocationType" style="width: 100%">
              <el-option v-for="t in locationTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="终点港区(用于筛选):">
            <el-select v-model="uiEndPortAreaId" style="width: 100%" placeholder="请选择终点港区" clearable>
              <el-option v-for="p in portAreas" :key="p.id" :label="(p as any).areaNameZh || (p as any).portAreaNameZh || (p as any).areaCode || String((p as any).id)" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="终点码头(用于泊位):" v-if="currentRoute.endLocationType === 'BERTH'">
            <el-select v-model="uiEndWharfId" style="width: 100%" placeholder="请选择终点码头" clearable>
              <el-option v-for="w in endWharves" :key="w.id" :label="w.wharfNameZh" :value="w.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="结束位置:" prop="endLocationId">
            <div style="display:flex; gap:8px; width:100%;">
              <el-select v-model="currentRoute.endLocationId" style="flex:1" filterable @change="handleEndLocationChange">
                <el-option
                  v-for="item in endLocationOptions"
                  :key="item.id"
                  :label="item.berthName || item.anchorageNameZh"
                  :value="item.id"
                />
              </el-select>
              <el-button
                v-if="currentRoute.endLocationType === 'BERTH'"
                type="primary"
                plain
                @click="openAddLocation('end','BERTH')"
              >新增泊位</el-button>
              <el-button
                v-if="currentRoute.endLocationType === 'ANCHORAGE'"
                type="primary"
                plain
                @click="openAddLocation('end','ANCHORAGE')"
              >新增锚地</el-button>
            </div>
          </el-form-item>

          <div class="section-label">区间参数</div>
          <el-form-item label="计费里程:">
            <el-input-number v-model="currentRoute.distanceNautical" :min="0" />
          </el-form-item>
          <el-form-item label="默认时长(分):">
            <el-input-number v-model="currentRoute.defaultDuration" :min="0" />
          </el-form-item>
          <el-form-item label="适用净吨下限:">
            <el-input-number v-model="currentRoute.applicableVesselMinNetTon" :min="0" :step="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="适用净吨上限:">
            <el-input-number v-model="currentRoute.applicableVesselMaxNetTon" :min="0" :step="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="仅限白天:">
            <el-switch v-model="currentRoute.daylightOnly" />
          </el-form-item>
          <el-form-item label="依赖潮汐:">
            <el-switch v-model="currentRoute.tideDependent" />
          </el-form-item>
          <div class="section-label">直航区间说明</div>
          <el-form-item label="直航区间说明:">
            <el-input v-model="currentRoute.routeNotes" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">保 存</el-button>
      </template>
    </el-dialog>

    <!-- 新增位置（泊位/锚地） -->
    <el-dialog v-model="addLocationDialogVisible" :title="addLocationType === 'BERTH' ? '新增泊位' : '新增锚地'" width="520px">
      <el-form v-if="addLocationType === 'BERTH'" :model="addBerthForm" label-width="120px" v-loading="addLocationLoading">
        <el-form-item label="泊位代码" required>
          <el-input v-model="addBerthForm.berthCode" />
        </el-form-item>
        <el-form-item label="泊位名称" required>
          <el-input v-model="addBerthForm.berthName" />
        </el-form-item>
        <el-form-item label="泊位类型">
          <el-select
            v-model="addBerthForm.berthType"
            style="width: 100%"
            filterable
            clearable
            allow-create
            default-first-option
            placeholder="请选择或输入泊位类型"
          >
            <el-option v-for="t in berthTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="港区(用于筛选)">
          <el-input :model-value="addLocationFor === 'start' ? startPortAreaLabel : endPortAreaLabel" disabled placeholder="请先在上方选择港区" />
        </el-form-item>
        <el-form-item label="码头(用于泊位)" required>
          <el-input :model-value="addLocationFor === 'start' ? startWharfLabel : endWharfLabel" disabled placeholder="请先在上方选择码头" />
        </el-form-item>
      </el-form>
      <el-form v-else :model="addAnchorageForm" label-width="120px" v-loading="addLocationLoading">
        <el-form-item label="锚地代码" required>
          <el-input v-model="addAnchorageForm.anchorageCode" />
        </el-form-item>
        <el-form-item label="中文名称" required>
          <el-input v-model="addAnchorageForm.anchorageNameZh" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="addAnchorageForm.anchorageNameEn" />
        </el-form-item>
        <el-form-item label="港区(用于筛选)" required>
          <el-input :model-value="addLocationFor === 'start' ? startPortAreaLabel : endPortAreaLabel" disabled placeholder="请先在上方选择港区" />
        </el-form-item>
        <el-form-item label="锚地类型">
          <el-select v-model="addAnchorageForm.anchorageType" style="width: 100%">
            <el-option label="检疫" value="QUARANTINE" />
            <el-option label="待泊" value="WAITING" />
            <el-option label="应急" value="EMERGENCY" />
            <el-option label="备用" value="SPARE" />
            <el-option label="引航" value="PILOTAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="经度" required>
          <el-input-number v-model="addAnchorageForm.longitude" :precision="6" :step="0.000001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度" required>
          <el-input-number v-model="addAnchorageForm.latitude" :precision="6" :step="0.000001" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addLocationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLocationLoading" @click="submitAddLocation">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.direct-route-view {
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

.section-label {
  color: var(--tidal-primary);
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
  padding-left: 10px;
  border-left: 3px solid var(--tidal-accent);
}

:deep(.billing-dialog) {
  .el-dialog__body { padding: 0; }
  .el-dialog__header { border-bottom: 1px solid var(--tidal-border); margin-right: 0; }
}
</style>