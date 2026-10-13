<script setup lang="ts">
import {ref, watch, onMounted, computed, nextTick} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import { Delete, FolderChecked, FolderOpened } from '@element-plus/icons-vue'
import type { PilotageApplication } from '@/types/pilotage'
import { fetchVesselList } from "@/api/bases/vessels.ts";
import type { Vessels } from "@/types/bases/vessels.ts";
//   fetchAnchoragesByPortAreaId, fetchBerthsByWharvesId,
import {createPilotageApplication, updatePilotageApplication} from "@/api/pilotage.ts";
import {usePortsStore} from "@/stores/ports.ts";
import {useBaseStore} from "@/stores/base.ts";

const props = defineProps<{
  modelValue: Partial<PilotageApplication>
  loading?: boolean
  isEdit?: boolean
}>()

const emit = defineEmits(['update:modelValue', 'submit', 'cancel'])

// 使用 Pinia stores
const portsStore = usePortsStore()
const baseStore = useBaseStore()

const formRef = ref<FormInstance>()
const formData = ref<any>({
  vesselCallSign: '', // 呼号
  imoNumber: '', // IMO编号
  mmsiNumber: '', // MMSI编号
  nameZh: '', // 中文船名
  nameEn: '', // 英文船名
  nationality: '', // 国籍
  vesselType: '', // 船舶类型
  ownerZh: '', // 船舶所有人（中文）
  vesselLength: '', // 船舶长度
  vesselWidth: '', // 船舶宽度
  depth: '', // 型深
  grossTonnage: '', // 总吨
  netTonnage: '', // 净吨
  deadweight: '', // 载重吨
  mainEngineType: '', // 主机种类
  mainEnginePower: '', // 主机功率(kW)
  speed: '', // 船速
  bowThrusterPower: '', // 侧推功率(kW)
  airDraft: '', // 水面上高度
  aisPilotInterface: null, // AIS引航接口（YES/NO）
  buildDate: '', // 建造年月
  routeType: '', // 航线种类

  portOfDepartureId: null, // 始发港ID
  portOfDestinationId: null, // 目的港ID
  previousPortId: '', // 上一港ID年级
  nextPortId: '', // 下一港ID
  portAreaId: null, // 港区ID - 新增
  wharfId: null, // 码头ID - 新增
  // 使用后端实际字段名
  plannedStartId: null, // 开始位置ID
  plannedEndId: null, // 结束位置ID
  startLocationType: null, // 移泊开始位置类型(BERTH/ANCHORAGE)
  endLocationType: null, // 结束位置类型(BERTH/ANCHORAGE)
  eta: '', // 预计到港时间(ETA)
  etd: '', // 预计离港时间(ETD)
  etaAnchorage: '', // 预抵锚地时间 - 新增
  voyageStatus: '', // 航次状态
  voyageType: '', // 航次类型
  draftForward: '', // 前吃水
  draftAft: '', // 后吃水
  cargoName: '', // 装货名称
  cargoQuantity: '', // 货物数量
  cargoUnit: '', // 货物单位 - 新增 (TON/TEU/CBM/UNIT等)
  cargoType: '', // 货物类型 - 新增 (CONTAINER/BULK/LIQUID/DANGEROUS/GENERAL等)
  previousPort: '', // 上一港名称 - 新增 (字符串，用于自由输入)
  nextPort: '', // 下一港名称 - 新增 (字符串，用于自由输入)
  voyageRemarks: '', // 航次备注 - 新增

  ladderStatus: null, // 引航梯状态
  ladderType: null, // 引航梯类型
  freeboardHeight: null, // 干舷高度(m)
  ladderRemarks: null, // 引航梯备注

  agentId: null,
  agentName: '', // 代理名称
  payingUnitName: '', // 付款单位名称
  invoiceType: '', // 发票类型
  taxpayerId: '', // 纳税人识别号
  payingUnitAddress: '', // 付款单位地址
  payingUnitPhone: '', // 付款单位电话
  payingUnitBank: '', // 付款单位开户银行
  payingUnitBankAccount: '', // 付款单位银行账号

  requestNotes: '' // 申请表备注
})

const nationalities = ['中国', '中国香港', '巴拿马', '利比里亚', '马绍尔群岛', '新加坡', '韩国', '日本']

const shipTypeOptions = [
  { value: 'CARGO', label: '杂货船' },
  { value: 'CONTAINER', label: '集装箱船' },
  { value: 'TANKER', label: '油轮' },
  { value: 'LNG', label: 'LNG' },
  { value: 'CHEMICAL', label: '化学品船' },
  { value: 'RO-RO', label: '滚装船' },
  { value: 'GAS', label: '液化气船' },
  { value: 'CAR', label: '汽车运输船' },
]

const voyageTypes = [
  { value: 'IMPORT', label: '进港' },
  { value: 'EXPORT', label: '出港' },
  { value: 'TRANSIT', label: '移泊' }
]

const aisOptions = [
  { value: 'YES', label: '有' },
  { value: 'NO', label: '无' }
]
const ladderStatuses = ['正常', '故障']
const ladderTypes = ['组合梯', '引航梯']
const invoiceTypes = ['增值税专用发票', '普通发票']
const startLocationTypes = [
  { value: 'BERTH', label: '泊位' },
  { value: 'ANCHORAGE', label: '锚地' }
]

// 使用 Pinia store 中的数据
const ports = computed(() => portsStore.ports)// 港口列表
const portAreas = computed(() => portsStore.portAreas)// 港区列表
const wharves = computed(() => portsStore.wharves)// 码头数据
const anchorages = computed(() => portsStore.anchorages)// 锚地数据
const berths = computed(() => portsStore.berths)// 泊位数据
const agentsList = computed(() => baseStore.agents)// 代理公司列表

const vesselList = ref<Vessels[]>([])
const selectedVesselId = ref<number>()
const DRAFT_KEY = 'pilotage_application_draft'
const hasDraft = ref(false)
const isLoadingDraft = ref(false)
const isAutoFilling = ref(false) // 标记是否正在自动填充
const isTestFilling = ref(false) // “数据填充测试”按钮 loading

const handleVesselSelect = (id: number) => {
  if (!id) return
  const vessel = vesselList.value.find(v => v.id === id)
  if (vessel) {
    isAutoFilling.value = true // 开始自动填充
    
    // 自动填充船舶信息
    formData.value.vesselCallSign = vessel.vesselCallSign || ''
    formData.value.imoNumber = vessel.imoNumber || ''
    formData.value.mmsiNumber = vessel.mmsiNumber || ''
    formData.value.nameZh = vessel.nameZh || ''
    formData.value.nameEn = vessel.nameEn || ''
    formData.value.nationality = vessel.nationality || ''
    if (vessel.vesselType) {
       // 如果是代码直接使用，如果是中文尝试转换（虽然vessels接口应该返回代码）
       const foundOption = shipTypeOptions.find(opt => opt.value === vessel.vesselType || opt.label === vessel.vesselType)
       formData.value.vesselType = foundOption ? foundOption.value : vessel.vesselType
    }
    formData.value.ownerZh = vessel.ownerZh || vessel.ownerEn || ''
    formData.value.vesselLength = vessel.vesselLength || ''
    formData.value.vesselWidth = vessel.vesselWidth || ''
    formData.value.depth = vessel.depth || ''
    formData.value.grossTonnage = vessel.grossTonnage || ''
    formData.value.netTonnage = vessel.netTonnage || ''
    formData.value.deadweight = vessel.deadweight || ''
    formData.value.mainEngineType = vessel.mainEngineType || ''
    formData.value.mainEnginePower = vessel.mainEnginePower || ''
    formData.value.speed = vessel.speed || ''
    formData.value.bowThrusterPower = vessel.bowThrusterPower || ''
    formData.value.airDraft = vessel.airDraft || ''
    const aisVal = vessel.aisPilotInterface
    formData.value.aisPilotInterface =
      aisVal === '有' ? 'YES' :
      aisVal === '无' ? 'NO' :
      (aisVal || null)
    formData.value.buildDate = vessel.buildDate || ''
    
    ElMessage.success('已自动填充船舶信息')

    // 使用 setTimeout 确保 watch 执行完毕后再重置标志位
    setTimeout(() => {
      isAutoFilling.value = false
    }, 100)
  }
}

// 监听船舶关键字段变化，如果不是自动填充导致的，则清空已选船舶
watch(() => [
  formData.value.vesselCallSign,
  formData.value.imoNumber,
  formData.value.mmsiNumber,
  formData.value.nameZh,
  formData.value.nameEn,
  formData.value.nationality,
  formData.value.vesselType
], () => {
  if (!isAutoFilling.value && selectedVesselId.value) {
    selectedVesselId.value = undefined
  }
})

const checkDraft = () => {
  const draft = localStorage.getItem(DRAFT_KEY)
  hasDraft.value = !!draft
}

const saveDraft = () => {
  try {
    localStorage.setItem(DRAFT_KEY, JSON.stringify(formData.value))
    hasDraft.value = true
    ElMessage.success('草稿保存成功')
  } catch (e) {
    ElMessage.error('草稿保存失败')
  }
}

const loadDraft = () => {
  try {
    const draft = localStorage.getItem(DRAFT_KEY)
    if (draft) {
      ElMessageBox.confirm('是否加载上次保存的草稿？这将覆盖当前表单内容。', '提示', {
        confirmButtonText: '加载',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        const data = JSON.parse(draft)

        // 草稿回填会触发 voyageType/startLocationType 等 watch 立即清空位置字段并触发校验，造成“已显示但提示未填”。
        // 所以回填期间暂时禁用这些联动与校验，回填并加载依赖数据后再统一清理校验状态。
        isLoadingDraft.value = true

        const savedPortAreaId = data.portAreaId
        const savedWharfId = data.wharfId
        const savedVoyageType = data.voyageType
        const savedStartLocationType = data.startLocationType
        const savedEndLocationType = data.endLocationType
        const savedPlannedStartId = data.plannedStartId
        const savedPlannedEndId = data.plannedEndId

        Object.assign(formData.value, data)

        if (savedPortAreaId) {
          await Promise.all([
            portsStore.fetchWharves(savedPortAreaId),
            portsStore.fetchAnchorages(savedPortAreaId)
          ])
        }
        if (savedWharfId) {
          await portsStore.fetchBerths(savedWharfId)
        }

        formData.value.portAreaId = savedPortAreaId ?? formData.value.portAreaId
        formData.value.wharfId = savedWharfId ?? formData.value.wharfId
        formData.value.voyageType = savedVoyageType ?? formData.value.voyageType
        formData.value.startLocationType = savedStartLocationType ?? formData.value.startLocationType
        formData.value.endLocationType = savedEndLocationType ?? formData.value.endLocationType
        formData.value.plannedStartId = savedPlannedStartId ?? formData.value.plannedStartId
        formData.value.plannedEndId = savedPlannedEndId ?? formData.value.plannedEndId

        await nextTick()
        if (formRef.value) {
          formRef.value.clearValidate([
            'portAreaId',
            'wharfId',
            'voyageType',
            'startLocationType',
            'plannedStartId',
            'plannedEndId'
          ])
        }

        isLoadingDraft.value = false
        ElMessage.success('草稿加载成功')
      }).catch(() => {
        // cancel
      })
    }
  } catch (e) {
    console.error(e)
  }
}

const clearDraft = () => {
  ElMessageBox.confirm('确定要清空草稿吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem(DRAFT_KEY)
    hasDraft.value = false
    ElMessage.success('草稿已清空')
  })
}

watch(() => props.modelValue, (val) => {
  if (val) {
    const processedVal = { ...val };
    if (processedVal.vesselType) {
      const foundOption = shipTypeOptions.find(option => option.label === processedVal.vesselType);
      if (foundOption) {
        processedVal.vesselType = foundOption.value;
      }
    }
    if ((processedVal as any).aisPilotInterface) {
      const a = (processedVal as any).aisPilotInterface
      ;(processedVal as any).aisPilotInterface =
        a === '有' ? 'YES' :
        a === '无' ? 'NO' :
        a
    }
    // Merge existing values
    Object.assign(formData.value, processedVal)
  }
}, { immediate: true, deep: true })

watch(formData, (val) => {
  emit('update:modelValue', val)
}, { deep: true })

const rules: FormRules = {
  vesselCallSign: [{ required: true, message: '必填', trigger: 'blur' }], // 呼号
  mmsiNumber: [{ required: true, message: '必填', trigger: 'blur' }], // MMSI编号
  nameZh: [{ required: true, message: '必填', trigger: 'blur' }], // 中文船名
  nameEn: [{ required: true, message: '必填', trigger: 'blur' }], // 英文船名
  nationality: [{ required: true, message: '必填', trigger: 'change' }], // 国籍
  vesselType: [{ required: true, message: '必填', trigger: 'change' }], // 船舶类型
  vesselLength: [{ required: true, message: '必填', trigger: 'blur' }], // 船舶长度
  vesselWidth: [{ required: true, message: '必填', trigger: 'blur' }], // 船舶宽度
  depth: [{ required: true, message: '必填', trigger: 'blur' }], // 型深
  grossTonnage: [{ required: true, message: '必填', trigger: 'blur' }], // 总吨
  netTonnage: [{ required: true, message: '必填', trigger: 'blur' }], // 净吨
  deadweight: [{ required: true, message: '必填', trigger: 'blur' }], // 载重吨
  airDraft: [{ required: true, message: '必填', trigger: 'blur' }], // 水面上高度
  voyageType: [{ required: true, message: '必填', trigger: 'change' }], // 航次类型
  draftAft: [{ required: true, message: '必填', trigger: 'blur' }], // 后吃水
  // 港区/码头/位置类型（位置字段如果为空会导致后端枚举/数值反序列化异常）
  portAreaId: [{ required: true, message: '必填', trigger: 'change' }],
  wharfId: [{
    validator: (_rule: any, _value: any, callback: any) => {
      const vt = formData.value.voyageType
      const startType = formData.value.startLocationType
      const needWharf = vt === 'IMPORT' || vt === 'EXPORT' || (vt === 'TRANSIT' && startType === 'BERTH')
      if (needWharf && !formData.value.wharfId) {
        callback(new Error('必填'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  startLocationType: [{
    validator: (_rule: any, _value: any, callback: any) => {
      if (formData.value.voyageType === 'TRANSIT' && !formData.value.startLocationType) {
        callback(new Error('必填'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],

  agentName: [{ required: true, message: '必填', trigger: 'change' }], // 代理名称

  // 航次信息必填字段
  portOfDepartureId: [{ required: true, message: '必填', trigger: 'change' }], // 始发港
  portOfDestinationId: [{ required: true, message: '必填', trigger: 'change' }], // 目的港
  eta: [{ required: true, message: '必填', trigger: 'change' }], // 预计到港时间

  // 其他必填字段
  draftForward: [{ required: true, message: '必填', trigger: 'blur' }], // 艏吃水
  cargoName: [{ required: true, message: '必填', trigger: 'blur' }], // 装货名称
  cargoQuantity: [{ required: true, message: '必填', trigger: 'blur' }], // 货物数量
  cargoType: [{ required: true, message: '必填', trigger: 'change' }], // 货物类型

  // 引航梯必填字段
  ladderStatus: [{ required: true, message: '必填', trigger: 'change' }], // 引航梯状态
  ladderType: [{ required: true, message: '必填', trigger: 'change' }], // 引航梯类型
  freeboardHeight: [{ required: true, message: '必填', trigger: 'blur' }], // 干舷高度
  ladderRemarks: [{
    validator: (_rule: any, _value: any, callback: any) => {
      if (formData.value.ladderStatus === '故障' && !formData.value.ladderRemarks) {
        callback(new Error('引航梯故障时请填写备注'))
        return
      }
      callback()
    },
    trigger: 'blur'
  }],

  // 代理及发票信息必填字段
  payingUnitName: [{ required: true, message: '必填', trigger: 'blur' }], // 付款单位名称
  invoiceType: [{ required: true, message: '必填', trigger: 'change' }], // 发票类型
  taxpayerId: [{ required: true, message: '必填', trigger: 'blur' }], // 纳税人识别号
  payingUnitAddress: [{ required: true, message: '必填', trigger: 'blur' }], // 付款单位地址
  payingUnitPhone: [{ required: true, message: '必填', trigger: 'blur' }], // 付款单位电话
  payingUnitBank: [{ required: true, message: '必填', trigger: 'blur' }], // 付款开户银行
  payingUnitBankAccount: [{ required: true, message: '必填', trigger: 'blur' }], // 付款银行账号
}

const validateStartEndLocations = (rule: any, value: any, callback: any) => {
  if (!formData.value.voyageType) {
    callback(new Error('请先选择航次类型'));
    return;
  }

  if (rule.field === 'plannedStartId' && !formData.value.plannedStartId) {
    callback(new Error('请选择开始位置'));
    return;
  }

  if (rule.field === 'plannedEndId' && !formData.value.plannedEndId) {
    callback(new Error('请选择结束位置'));
    return;
  }

  if (formData.value.voyageType === 'TRANSIT' &&
      formData.value.plannedStartId &&
      formData.value.plannedEndId &&
      formData.value.plannedStartId === formData.value.plannedEndId) {
    callback(new Error('移泊时开始位置和结束位置不能相同'));
    return;
  }

  callback();
};

// 添加对开始位置和结束位置的验证规则
rules.plannedStartId = [{ validator: validateStartEndLocations, trigger: 'change' }];
rules.plannedEndId = [{ validator: validateStartEndLocations, trigger: 'change' }];

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()

    if (valid) {
      const submitData = deepEmptyStringToNull({ ...formData.value })
      submitData.aisPilotInterface = normalizeAisPilotInterface(submitData.aisPilotInterface)

      try {
        let response;
        if (props.isEdit && submitData.id) {
          response = await updatePilotageApplication(Number(submitData.id), submitData)
        } else {
          response = await createPilotageApplication(submitData)
        }

        if (response.code === 200) {
          emit('submit') // 成功后再触发submit事件
        } else {
          // 特别处理业务错误码
            ElMessage.error(response.message || (props.isEdit ? '更新失败' : '创建失败'))
        }
      } catch (error) {
        console.error((props.isEdit ? '更新' : '创建') + '引航申请失败:', error)
      }
    }
  } catch (error) {
    // 验证失败，Element Plus 的 validate 在验证失败时会抛出错误
    ElMessage.warning('请完善表单信息')
  }
}

const fetchBaseData = async () => {
  try {
    const [_, __, ___, vesselRes] = await Promise.all([
      portsStore.fetchPorts(),
      portsStore.fetchPortAreas(1), // 假设默认港口ID为1
      baseStore.fetchAgents(),
      fetchVesselList()
    ])
    
    if (vesselRes && vesselRes.data) {
      vesselList.value = vesselRes.data
    }
  } catch (e) {
  }
}

const handlePortAreaChange = async () => {
  if (formData.value.portAreaId) {
    await Promise.all([
      portsStore.fetchWharves(formData.value.portAreaId),
      portsStore.fetchAnchorages(formData.value.portAreaId)
    ])
    formData.value.wharfId = ''
    formData.value.plannedStartId = ''
    formData.value.plannedEndId = ''
    formData.value.startLocationType = ''
  } else {
    // 如果清空了港区，则清空其依赖项
    formData.value.wharfId = ''
    formData.value.plannedStartId = ''
    formData.value.plannedEndId = ''
    formData.value.startLocationType = ''
  }
}

const handleWharfChange = async () => {
  if (formData.value.wharfId) {
    await portsStore.fetchBerths(formData.value.wharfId)
    formData.value.plannedStartId = ''
    formData.value.plannedEndId = ''
  } else {
    formData.value.plannedStartId = ''
    formData.value.plannedEndId = ''
  }
}

const handlePayingUnitChange = (value: string) => {
  const selectedAgent = agentsList.value.find(agent => agent.companyName === value)
  if (selectedAgent) {
    // 自动填充相关信息
    formData.value.agentId = selectedAgent.id
    formData.value.taxpayerId = selectedAgent.defaultTaxpayerId || ''
    formData.value.payingUnitAddress = selectedAgent.defaultAddress || selectedAgent.address || ''
    formData.value.payingUnitPhone = selectedAgent.phone || ''
    formData.value.payingUnitBank = selectedAgent.defaultBankName || ''
    formData.value.payingUnitBankAccount = selectedAgent.defaultBankAccount || ''
  } else {
    formData.value.agentId = null
  }
}

const handleAgentNameChange = (value: string) => {
  const selectedAgent = agentsList.value.find(agent => agent.companyName === value)
  if (selectedAgent) {
    formData.value.agentId = selectedAgent.id
    if (!formData.value.payingUnitName) {
      formData.value.payingUnitName = value
      handlePayingUnitChange(value)
    }
  } else {
    formData.value.agentId = null
  }
}

const handleStartLocationTypeChange = () => {
  formData.value.plannedStartId = null;
  formData.value.plannedEndId = null;

  if (formData.value.startLocationType === 'BERTH') {
    formData.value.endLocationType = 'ANCHORAGE';
  } else if (formData.value.startLocationType === 'ANCHORAGE') {
    formData.value.endLocationType = 'BERTH';
  }

  setTimeout(() => {
    if (formRef.value) {
      formRef.value.validateField('plannedStartId');
      formRef.value.validateField('plannedEndId');
    }
  }, 0);
}

onMounted(() => {
  fetchBaseData()
  checkDraft()
})

const disabledDate = (date: Date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date.getTime() < today.getTime();
};

const getShipTypeLabel = (value: string) => {
  const option = shipTypeOptions.find(option => option.value === value);
  return option ? option.label : value;
};

const startPositionLabel = computed(() => {
  switch (formData.value.voyageType) {
    case 'IMPORT':
      return '开始位置(锚地)';
    case 'EXPORT':
      return '开始位置(泊位)';
    case 'TRANSIT':
      return '开始位置';
    default:
      return '开始位置';
  }
});

const endPositionLabel = computed(() => {
  switch (formData.value.voyageType) {
    case 'IMPORT':
      return '结束位置(泊位)';
    case 'EXPORT':
      return '结束位置(锚地)';
    case 'TRANSIT':
      return '结束位置';
    default:
      return '结束位置';
  }
});

const startPositionOptions = computed(() => {
  switch (formData.value.voyageType) {
    case 'IMPORT':
      return anchorages.value;
    case 'EXPORT':
      return berths.value;
    case 'TRANSIT':
      if (formData.value.startLocationType === 'BERTH') {
        return berths.value;
      } else if (formData.value.startLocationType === 'ANCHORAGE') {
        return anchorages.value;
      }
      return [];
    default:
      return [];
  }
});

const endPositionOptions = computed(() => {
  switch (formData.value.voyageType) {
    case 'IMPORT':
      return berths.value;
    case 'EXPORT':
      return anchorages.value;
    case 'TRANSIT':
      if (formData.value.startLocationType === 'BERTH') {
        return anchorages.value;
      } else if (formData.value.startLocationType === 'ANCHORAGE') {
        return berths.value;
      }
      return [];
    default:
      return [];
  }
});

const isWharfDisabled = computed(() => {
  return !formData.value.portAreaId;
});

const isStartPositionDisabled = computed(() => {
  if (!formData.value.portAreaId || !formData.value.voyageType) {
    return true;
  }

  switch (formData.value.voyageType) {
    case 'IMPORT':
      return false;
    case 'EXPORT':
      return !formData.value.wharfId;
    case 'TRANSIT':
      if (formData.value.startLocationType === 'BERTH') {
        return !formData.value.wharfId;
      }
      return false;
    default:
      return true;
  }
});

const isEndPositionDisabled = computed(() => {
  if (!formData.value.portAreaId || !formData.value.voyageType) {
    return true;
  }

  switch (formData.value.voyageType) {
    case 'IMPORT':
      return !formData.value.wharfId;
    case 'EXPORT':
      return false;
    case 'TRANSIT':
      return !formData.value.plannedStartId;
    default:
      return true;
  }
});

const isStartLocationTypeDisabled = computed(() => {
  return !formData.value.portAreaId || formData.value.voyageType !== 'TRANSIT';
});

const positionHint = computed(() => {
  switch (formData.value.voyageType) {
    case 'IMPORT':
      return '船舶将从【锚地】移动至【泊位】';
    case 'EXPORT':
      return '船舶将从【泊位】移动至【锚地】';
    case 'TRANSIT':
      if (formData.value.startLocationType === 'BERTH') {
        return '船舶将从【泊位】移动至【锚地】';
      } else if (formData.value.startLocationType === 'ANCHORAGE') {
        return '船舶将从【锚地】移动至【泊位】';
      }
      return '请选择开始位置类型';
    default:
      return '';
  }
});

watch(() => formData.value.voyageType, (newVal) => {
  if (isLoadingDraft.value) return;
  formData.value.plannedStartId = null;
  formData.value.plannedEndId = null;
  formData.value.startLocationType = null;
  formData.value.endLocationType = null; // 也要重置结束位置类型

  if (newVal !== 'TRANSIT') {
    if (newVal === 'IMPORT') {
      formData.value.startLocationType = 'ANCHORAGE';
      formData.value.endLocationType = 'BERTH';
    }
    else if (newVal === 'EXPORT') {
      formData.value.startLocationType = 'BERTH';
      formData.value.endLocationType = 'ANCHORAGE';
    }
  }

  setTimeout(() => {
    if (formRef.value) {
      formRef.value.validateField('plannedStartId');
      formRef.value.validateField('plannedEndId');
    }
  }, 0);
});

watch(() => formData.value.plannedStartId, (newVal) => {
  if (isLoadingDraft.value) return;
  if (formData.value.voyageType === 'TRANSIT' && newVal) {
  }

  setTimeout(() => {
    if (formRef.value) {
      formRef.value.validateField('plannedStartId');
      formRef.value.validateField('plannedEndId');
    }
  }, 0);
});

watch(() => formData.value.plannedEndId, (newVal) => {
  if (isLoadingDraft.value) return;
  setTimeout(() => {
    if (formRef.value) {
      formRef.value.validateField('plannedStartId');
      formRef.value.validateField('plannedEndId');
    }
  }, 0);
});

watch(() => formData.value.startLocationType, (newVal) => {
  if (isLoadingDraft.value) return;
  // 当开始位置类型变化时，自动设置结束位置类型（移泊情况）
  if (formData.value.voyageType === 'TRANSIT' && newVal) {
    if (newVal === 'BERTH') {
      formData.value.endLocationType = 'ANCHORAGE';
    } else if (newVal === 'ANCHORAGE') {
      formData.value.endLocationType = 'BERTH';
    }
  }

  formData.value.plannedStartId = null;
  formData.value.plannedEndId = null;

  setTimeout(() => {
    if (formRef.value) {
      formRef.value.validateField('plannedStartId');
      formRef.value.validateField('plannedEndId');
    }
  }, 0);
});

// --- 提交前数据清洗：避免 "" 导致后端枚举/数值反序列化异常 ---
const deepEmptyStringToNull = (input: any): any => {
  if (input === '') return null
  if (Array.isArray(input)) return input.map(deepEmptyStringToNull)
  if (input && typeof input === 'object') {
    const out: any = {}
    for (const [k, v] of Object.entries(input)) {
      out[k] = deepEmptyStringToNull(v)
    }
    return out
  }
  return input
}

const normalizeAisPilotInterface = (v: any) => {
  if (v === '有') return 'YES'
  if (v === '无') return 'NO'
  return v
}

const pad2 = (n: number) => (n < 10 ? `0${n}` : String(n))
const formatDateTime = (d: Date) => {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
}

const pickOne = <T,>(arr: T[]) => arr[Math.floor(Math.random() * arr.length)]
const randomInt = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min
const randomCallSign = () => {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const pick = () => letters[randomInt(0, letters.length - 1)]
  return `${pick()}${pick()}${pick()}${pick()}${randomInt(0, 9)}${randomInt(0, 9)}`
}

const ensureBaseLoaded = async () => {
  // fetchBaseData 走并发；这里做兜底，避免某些数据加载失败导致测试按钮无效
  if (!ports.value || ports.value.length === 0) {
    await portsStore.fetchPorts()
  }
  if (!portAreas.value || portAreas.value.length === 0) {
    await portsStore.fetchPortAreas(1)
  }
  if (!agentsList.value || agentsList.value.length === 0) {
    await baseStore.fetchAgents()
  }
  if (!vesselList.value || vesselList.value.length === 0) {
    const vesselRes: any = await fetchVesselList()
    if (vesselRes && vesselRes.data) {
      vesselList.value = vesselRes.data || []
    }
  }
}

const fillTestData = async () => {
  if (props.isEdit) {
    ElMessage.warning('仅新增场景使用“数据填充测试”')
    return
  }
  if (isTestFilling.value) return

  isTestFilling.value = true
  try {
    await ensureBaseLoaded()

    const vessels = (vesselList.value || []).slice()
    const goodVessels = vessels.filter(v =>
      v && (v as any).id &&
      (v as any).vesselCallSign &&
      (v as any).nameZh &&
      (v as any).nameEn &&
      (v as any).mmsiNumber &&
      (v as any).netTonnage &&
      (v as any).grossTonnage
    )
    const chosenVessel = (goodVessels.length > 0 ? pickOne(goodVessels) : (vessels.length > 0 ? pickOne(vessels) : null)) as any
    if (chosenVessel && chosenVessel.id) {
      selectedVesselId.value = chosenVessel.id
      handleVesselSelect(chosenVessel.id)
    } else {
      formData.value.vesselCallSign = randomCallSign()
      formData.value.mmsiNumber = `413${String(Math.floor(Math.random() * 9000000) + 1000000)}`
      formData.value.imoNumber = `9${String(Math.floor(Math.random() * 9000000) + 1000000)}`
      formData.value.nameZh = '远洋之星'
      formData.value.nameEn = 'OCEAN STAR'
      formData.value.nationality = '中国'
      formData.value.vesselType = 'CONTAINER'
      formData.value.ownerZh = '中远海运'
      formData.value.vesselLength = 299.8
      formData.value.vesselWidth = 45.6
      formData.value.depth = 24.5
      formData.value.grossTonnage = 120000
      formData.value.netTonnage = 80000
      formData.value.deadweight = 150000
      formData.value.mainEngineType = 'MAN-B&W'
      formData.value.mainEnginePower = 52000
      formData.value.speed = 18.5
      formData.value.bowThrusterPower = 2200
      formData.value.airDraft = 48.0
      formData.value.aisPilotInterface = 'YES'
      formData.value.buildDate = formatDateTime(new Date(new Date().getFullYear() - 6, 5, 1, 0, 0, 0))
      formData.value.routeType = 'DOMESTIC'
    }

    if (!formData.value.nationality) formData.value.nationality = '中国'
    if (!formData.value.vesselType) formData.value.vesselType = 'CONTAINER'
    if (!formData.value.vesselLength) formData.value.vesselLength = 289.0
    if (!formData.value.vesselWidth) formData.value.vesselWidth = 44.0
    if (!formData.value.depth) formData.value.depth = 24.0
    if (!formData.value.grossTonnage) formData.value.grossTonnage = 110000
    if (!formData.value.netTonnage) formData.value.netTonnage = 78000
    if (!formData.value.deadweight) formData.value.deadweight = 140000
    if (!formData.value.airDraft) formData.value.airDraft = 45.0
    if (!formData.value.aisPilotInterface) formData.value.aisPilotInterface = 'YES'
    if (!formData.value.mainEngineType) {
      const t = String(formData.value.vesselType || '').toUpperCase()
      formData.value.mainEngineType =
        t === 'TANKER' ? 'WinGD 6X82' :
        t === 'LNG' ? 'MAN B&W 5G70ME-C9.5-GI' :
        'MAN B&W 7S80ME-C'
    }
    if (!formData.value.mainEnginePower) {
      const t = String(formData.value.vesselType || '').toUpperCase()
      formData.value.mainEnginePower =
        t === 'TANKER' ? 42000 :
        t === 'LNG' ? 36000 :
        52000
    }
    if (!formData.value.speed) {
      const t = String(formData.value.vesselType || '').toUpperCase()
      formData.value.speed =
        t === 'TANKER' ? 14.5 :
        t === 'LNG' ? 16.5 :
        18.5
    }
    if (!formData.value.bowThrusterPower) formData.value.bowThrusterPower = 2200
    if (!formData.value.routeType) {
      const nat = String(formData.value.nationality || '')
      formData.value.routeType = nat.includes('中国') ? 'DOMESTIC' : 'INTERNATIONAL'
    }

    const pa = portAreas.value && portAreas.value.length > 0 ? pickOne(portAreas.value as any[]) : null
    if (pa && (pa as any).id) {
      formData.value.portAreaId = (pa as any).id
      await handlePortAreaChange()
    }

    formData.value.voyageType = 'IMPORT'
    await nextTick()

    // 注意：handleWharfChange 会清空 plannedStartId/plannedEndId，因此先选码头并拉取泊位，再填开始/结束位置
    if (wharves.value && wharves.value.length > 0) {
      const w = pickOne(wharves.value as any[])
      formData.value.wharfId = (w as any).id
      await handleWharfChange()
    }
    if (anchorages.value && anchorages.value.length > 0) {
      const start = pickOne(anchorages.value as any[])
      formData.value.plannedStartId = (start as any).id
    }
    if (berths.value && berths.value.length > 0) {
      const end = pickOne(berths.value as any[])
      formData.value.plannedEndId = (end as any).id
    }

    if (ports.value && ports.value.length > 0) {
      const dep = pickOne(ports.value as any[])
      let dest = dep
      if ((ports.value as any[]).length > 1) {
        const candidates = (ports.value as any[]).filter(p => p && (p as any).id !== (dep as any).id)
        if (candidates.length > 0) dest = pickOne(candidates)
      }
      formData.value.portOfDepartureId = (dep as any).id
      formData.value.portOfDestinationId = (dest as any).id
      formData.value.previousPort = (dep as any).portNameZh || (dep as any).portNameEn || ''
      formData.value.nextPort = (dest as any).portNameZh || (dest as any).portNameEn || ''
    }

    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
    let eta = new Date(now.getTime() + 30 * 60 * 1000)
    if (eta < new Date(todayStart.getTime() + 9 * 60 * 60 * 1000)) {
      eta = new Date(todayStart.getTime() + 9 * 60 * 60 * 1000)
    }
    if (eta.getDate() !== todayStart.getDate()) {
      eta = new Date(todayStart.getTime() + 21 * 60 * 60 * 1000)
    }
    let etd = new Date(eta.getTime() + 4 * 60 * 60 * 1000)
    if (etd.getDate() !== todayStart.getDate()) {
      etd = new Date(todayStart.getTime() + 23 * 60 * 60 * 1000)
    }
    const etaAnch = new Date(Math.max(todayStart.getTime() + 8 * 60 * 60 * 1000, eta.getTime() - 45 * 60 * 1000))

    formData.value.eta = formatDateTime(eta)
    formData.value.etd = formatDateTime(etd)
    formData.value.etaAnchorage = formatDateTime(etaAnch)

    const vt = String(formData.value.vesselType || '').toUpperCase()
    if (!formData.value.draftForward) formData.value.draftForward = 11.8
    if (!formData.value.draftAft) formData.value.draftAft = 12.1

    if (vt === 'CONTAINER') {
      formData.value.cargoType = 'CONTAINER'
      formData.value.cargoName = '集装箱货物'
      formData.value.cargoUnit = 'TEU'
      formData.value.cargoQuantity = String(Math.floor(Math.random() * 900 + 600)) // 600~1500
    } else if (vt === 'TANKER') {
      formData.value.cargoType = 'LIQUID'
      formData.value.cargoName = '成品油'
      formData.value.cargoUnit = 'TON'
      formData.value.cargoQuantity = String(Math.floor(Math.random() * 20000 + 8000)) // 8000~28000
    } else {
      formData.value.cargoType = 'BULK'
      formData.value.cargoName = '钢材'
      formData.value.cargoUnit = 'TON'
      formData.value.cargoQuantity = String(Math.floor(Math.random() * 15000 + 5000)) // 5000~20000
    }

    formData.value.voyageRemarks = '进港靠泊作业，注意潮汐窗口；引航梯按规定布置，保持通讯畅通。'

    formData.value.ladderStatus = '正常'
    formData.value.ladderType = pickOne(ladderTypes)
    formData.value.freeboardHeight = 2.2
    formData.value.ladderRemarks = '梯架布置符合要求，已备妥救生圈与照明设备；靠泊阶段保持VHF守听。'

    const agents = agentsList.value || []
    if (agents.length > 0) {
      const goodAgents = (agents as any[]).filter(a => a && (a as any).companyName && ((a as any).defaultTaxpayerId || (a as any).taxpayerId))
      const a = goodAgents.length > 0 ? pickOne(goodAgents) : pickOne(agents as any[])
      formData.value.agentName = (a as any).companyName
      handleAgentNameChange((a as any).companyName)
      formData.value.payingUnitName = (a as any).companyName
      handlePayingUnitChange((a as any).companyName)
    }

    formData.value.invoiceType = pickOne(invoiceTypes)

    if (!formData.value.taxpayerId) formData.value.taxpayerId = '91330300MA282XXXX1'
    if (!formData.value.payingUnitAddress) formData.value.payingUnitAddress = '温州市鹿城区瓯江路88号 港航大厦8层'
    if (!formData.value.payingUnitPhone) formData.value.payingUnitPhone = '0577-56889999'
    if (!formData.value.payingUnitBank) formData.value.payingUnitBank = '中国工商银行温州分行营业部'
    if (!formData.value.payingUnitBankAccount) formData.value.payingUnitBankAccount = '3303020104001234567'

    formData.value.requestNotes = '请按规定提前备妥引航梯与救生设备，靠泊阶段注意拖轮配合。'

    await nextTick()
    formRef.value?.clearValidate()
    ElMessage.success('已自动填充数据，可直接提交')
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e?.message || '数据填充失败')
  } finally {
    isTestFilling.value = false
  }
}

</script>

<template>
  <div class="application-form-container">
    <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="110px"
        size="small"
        class="ocean-form"
    >
      <div class="form-group glass-panel">
        <div class="group-title">船舶资料</div>
        <div class="group-content">
          <div style="margin-bottom: 15px;">
            <el-select
                v-model="selectedVesselId"
                filterable
                placeholder="选择船舶以自动填充信息"
                style="width: 100%"
                @change="handleVesselSelect"
                clearable
            >
              <el-option
                  v-for="item in vesselList"
                  :key="item.id"
                  :label="item.nameZh + ' / ' + item.nameEn + ' (' + item.vesselCallSign + ')'"
                  :value="item.id"
              />
            </el-select>
          </div>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="呼号" prop="vesselCallSign">
                <el-input v-model="formData.vesselCallSign" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="IMO" prop="imoNumber">
                <el-input v-model="formData.imoNumber" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="MMSI" prop="mmsiNumber">
                <el-input v-model="formData.mmsiNumber" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="中文船名" prop="nameZh">
                <el-input v-model="formData.nameZh" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="英文船名" prop="nameEn">
                <el-input v-model="formData.nameEn" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="国籍" prop="nationality">
                <el-select v-model="formData.nationality" filterable allow-create>
                  <el-option v-for="item in nationalities" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="船舶种类" prop="vesselType">
                <el-select v-model="formData.vesselType" filterable>
                  <el-option v-for="item in shipTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="船公司" prop="ownerZh">
                <el-input v-model="formData.ownerZh" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="船长" prop="vesselLength">
                <el-input v-model="formData.vesselLength" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="船宽" prop="vesselWidth">
                <el-input v-model="formData.vesselWidth" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="型深" prop="depth">
                <el-input v-model="formData.depth" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="总吨" prop="grossTonnage">
                <el-input v-model="formData.grossTonnage" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="净吨" prop="netTonnage">
                <el-input v-model="formData.netTonnage" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="载重吨" prop="deadweight">
                <el-input v-model="formData.deadweight" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="主机种类" prop="mainEngineType">
                <el-input v-model="formData.mainEngineType" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="功率(kW)" prop="mainEnginePower">
                <el-input v-model="formData.mainEnginePower" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="航速" prop="speed">
                <el-input v-model="formData.speed" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="侧推功率(kW)" prop="bowThrusterPower">
                <el-input v-model="formData.bowThrusterPower" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="水面上高度" prop="airDraft">
                <el-input v-model="formData.airDraft" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="AIS引航接口" prop="aisPilotInterface" label-width="100px">
                <el-select v-model="formData.aisPilotInterface" placeholder="请选择">
                  <el-option v-for="item in aisOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="建造日期" prop="buildDate">
                <el-date-picker
                    v-model="formData.buildDate"
                    type="date"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="航线种类" prop="routeType">
                <el-radio-group v-model="formData.routeType">
                  <el-radio value="INTERNATIONAL">国际航线</el-radio>
                  <el-radio value="DOMESTIC">国内航线</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <div class="form-group glass-panel">
        <div class="group-title">航次信息</div>
        <div class="group-content">
          <el-row :gutter="10">
            <el-col :span="6">
              <el-form-item label="港区" prop="portAreaId">
                <el-select
                    v-model="formData.portAreaId"
                    filterable
                    placeholder="选择港区"
                    @change="handlePortAreaChange"
                >
                  <el-option
                      v-for="item in portAreas"
                      :key="item.id"
                      :label="item.areaNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="码头" prop="wharfId">
                <el-select
                    v-model="formData.wharfId"
                    filterable
                    placeholder="选择码头"
                    :disabled="isWharfDisabled"
                    @change="handleWharfChange"
                >
                  <el-option
                      v-for="item in wharves"
                      :key="item.id"
                      :label="item.wharfNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="动态(航次类型)*" prop="voyageType">
                <el-select v-model="formData.voyageType" placeholder="选择航次类型">
                  <el-option
                      v-for="item in voyageTypes"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6" v-if="formData.voyageType === 'TRANSIT'">
              <el-form-item label="开始位置类型" prop="startLocationType">
                <el-select
                    v-model="formData.startLocationType"
                    placeholder="选择开始位置类型"
                    :disabled="isStartLocationTypeDisabled"
                    @change="handleStartLocationTypeChange"
                >
                  <el-option
                      v-for="item in startLocationTypes"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item :label="startPositionLabel + '*'" prop="plannedStartId">
                <el-select
                    v-model="formData.plannedStartId"
                    filterable
                    :placeholder="'请选择' + startPositionLabel"
                    :disabled="isStartPositionDisabled"
                >
                  <el-option
                      v-for="item in startPositionOptions"
                      :key="item.id"
                      :label="item.berthName || item.anchorageNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item :label="endPositionLabel + '*'" prop="plannedEndId">
                <el-select
                    v-model="formData.plannedEndId"
                    filterable
                    :placeholder="'请选择' + endPositionLabel"
                    :disabled="isEndPositionDisabled"
                >
                  <el-option
                      v-for="item in endPositionOptions"
                      :key="item.id"
                      :label="item.berthName || item.anchorageNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 位置说明 -->
            <el-col :span="12" v-if="positionHint">
              <div class="location-hint">{{ positionHint }}</div>
            </el-col>

            <el-col :span="6">
              <el-form-item label="始发港*" prop="portOfDepartureId">
                <el-select
                    v-model="formData.portOfDepartureId"
                    filterable
                    placeholder="选择始发港"
                >
                  <el-option
                      v-for="item in ports"
                      :key="item.id"
                      :label="item.portNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="目的港*" prop="portOfDestinationId">
                <el-select
                    v-model="formData.portOfDestinationId"
                    filterable
                    placeholder="选择目的港"
                >
                  <el-option
                      v-for="item in ports"
                      :key="item.id"
                      :label="item.portNameZh"
                      :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- ETD/ETA 时间精确选择到分钟 -->
            <el-col :span="6">
              <el-form-item label="预抵时间*" prop="eta">
                <el-date-picker
                    v-model="formData.eta"
                    type="datetime"
                    placeholder="选择预抵时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disabledDate"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="预离时间" prop="etd">
                <el-date-picker
                    v-model="formData.etd"
                    type="datetime"
                    placeholder="选择预离时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disabledDate"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="艏吃水(m)" prop="draftForward">
                <el-input
                    v-model="formData.draftForward"
                    placeholder="前吃水深度"
                    type="number"
                    step="0.1"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="艉吃水(m)*" prop="draftAft">
                <el-input
                    v-model="formData.draftAft"
                    placeholder="后吃水深度"
                    type="number"
                    step="0.1"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="预抵锚地时间" prop="etaAnchorage">
                <el-date-picker
                    v-model="formData.etaAnchorage"
                    type="datetime"
                    placeholder="选择预抵锚地时间"
                    format="YYYY-MM-DD HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disabledDate"
                />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="装货名称" prop="cargoName">
                <el-input v-model="formData.cargoName" placeholder="货物名称" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="数量" prop="cargoQuantity">
                <el-input
                    v-model="formData.cargoQuantity"
                    placeholder="货物数量"
                    type="number"
                    step="0.01"
                >
                  <template #append>
                    <el-select v-model="formData.cargoUnit" style="width: 80px">
                      <el-option label="吨" value="TON" />
                      <el-option label="TEU" value="TEU" />
                      <el-option label="立方米" value="CBM" />
                      <el-option label="辆" value="UNIT" />
                    </el-select>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="货物类型" prop="cargoType">
                <el-select v-model="formData.cargoType" placeholder="选择货物类型">
                  <el-option label="集装箱" value="CONTAINER" />
                  <el-option label="散货" value="BULK" />
                  <el-option label="液体货" value="LIQUID" />
                  <el-option label="危险品" value="DANGEROUS" />
                  <el-option label="普通货物" value="GENERAL" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="上一港" prop="previousPort">
                <el-input v-model="formData.previousPort" placeholder="上一停靠港口" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="下一港" prop="nextPort">
                <el-input v-model="formData.nextPort" placeholder="下一停靠港口" />
              </el-form-item>
            </el-col>

            <!-- 航次备注 -->
            <el-col :span="24">
              <el-form-item label="航次备注" prop="voyageRemarks">
                <el-input
                    v-model="formData.voyageRemarks"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入航次相关备注信息"
                    maxlength="500"
                    show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 引航梯 -->
      <div class="form-group glass-panel">
        <div class="group-title">引航梯</div>
        <div class="group-content">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="状态" prop="ladderStatus">
                <el-select v-model="formData.ladderStatus">
                  <el-option v-for="item in ladderStatuses" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="类型" prop="ladderType">
                <el-select v-model="formData.ladderType">
                  <el-option v-for="item in ladderTypes" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="干舷高度(m)" prop="freeboardHeight">
                <el-input v-model="formData.freeboardHeight" type="number" step="0.1" placeholder="请输入干舷高度" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注" prop="ladderRemarks">
                <el-input
                  v-model="formData.ladderRemarks"
                  type="textarea"
                  :rows="2"
                  placeholder="如引航梯故障/特殊情况请说明"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 代理及发票信息 -->
      <div class="form-group glass-panel">
        <div class="group-title">代理及发票信息</div>
        <div class="group-content">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="代理名称*" prop="agentName">
                <el-select
                    v-model="formData.agentName"
                    filterable
                    @change="handleAgentNameChange"
                >
                  <el-option
                      v-for="item in agentsList"
                      :key="item.id"
                      :label="item.companyName"
                      :value="item.companyName"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="付款单位名称" prop="payingUnitName">
                <el-select
                    v-model="formData.payingUnitName"
                    filterable
                    allow-create
                    @change="handlePayingUnitChange"
                >
                  <el-option
                      v-for="item in agentsList"
                      :key="item.id"
                      :label="item.companyName"
                      :value="item.companyName"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="发票类型" prop="invoiceType">
                <el-select v-model="formData.invoiceType">
                  <el-option v-for="item in invoiceTypes" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="纳税人识别号" prop="taxpayerId">
                <el-input v-model="formData.taxpayerId" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="付款单位地址" prop="payingUnitAddress">
                <el-input v-model="formData.payingUnitAddress" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="付款单位电话" prop="payingUnitPhone">
                <el-input v-model="formData.payingUnitPhone" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="付款开户银行" prop="payingUnitBank">
                <el-input v-model="formData.payingUnitBank" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="付款银行账号" prop="payingUnitBankAccount">
                <el-input v-model="formData.payingUnitBankAccount" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

<!--      &lt;!&ndash; 付款信息 --------------------------------------------------------暂时判断为不需要&ndash;&gt;-->
<!--      <div class="form-group glass-panel">-->
<!--        <div class="group-title">付款信息</div>-->
<!--        <div class="group-content">-->
<!--          <el-row :gutter="10">-->
<!--            <el-col :span="8">-->
<!--              <div class="info-item">单位: {{ formData.payee_unit || '暂无' }}</div>-->
<!--            </el-col>-->
<!--            <el-col :span="8">-->
<!--              <div class="info-item">账号: {{ formData.payee_bank_account || '暂无' }}</div>-->
<!--            </el-col>-->
<!--            <el-col :span="8">-->
<!--              <div class="info-item">开户行: {{ formData.payee_bank_name || '暂无' }}</div>-->
<!--            </el-col>-->
<!--          </el-row>-->
<!--        </div>-->
<!--      </div>-->

      <!-- 备注 -->
      <div class="form-group glass-panel">
        <div class="group-title">备注</div>
        <div class="group-content">
          <el-input v-model="formData.requestNotes" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </div>
      </div>
    </el-form>

    <div class="dialog-footer">
      <div style="flex: 1; display: flex; gap: 10px;">
        <el-button 
          v-if="!isEdit && hasDraft" 
          type="warning" 
          plain 
          :icon="FolderOpened" 
          @click="loadDraft"
        >
          加载草稿
        </el-button>
        <el-button 
          v-if="hasDraft" 
          type="danger" 
          text 
          :icon="Delete" 
          @click="clearDraft"
        >
          清空草稿
        </el-button>
        <el-button
          v-if="!isEdit"
          type="success"
          plain
          :loading="isTestFilling"
          @click="fillTestData"
        >
          数据填充测试
        </el-button>
      </div>
      <el-button type="info" plain :icon="FolderChecked" @click="saveDraft">保存草稿</el-button>
      <el-button @click="emit('cancel')">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">提 交</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.application-form-container {
  padding: 10px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(143, 214, 255, 0.1);
  margin-bottom: 16px;
  border-radius: 0; /* Sharp corners */

  .group-title {
    background: rgba(143, 214, 255, 0.15);
    padding: 8px 16px;
    font-size: 14px;
    font-weight: 600;
    color: var(--tidal-primary-light);
    border-bottom: 1px solid rgba(143, 214, 255, 0.1);
  }

  .group-content {
    padding: 16px;
  }
}

.info-item {
  color: var(--tidal-text-main);
  font-size: 13px;
  padding: 8px 0;
}

.location-hint {
  color: var(--tidal-primary-light);
  font-size: 12px;
  padding: 8px 0;
  height: 32px;
  display: flex;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(143, 214, 255, 0.1);
}

/* Form Overrides for Tidal Theme */
.ocean-form {
  :deep(.el-form-item__label) {
    color: var(--tidal-text-secondary);
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    background-color: rgba(0, 0, 0, 0.1);
    box-shadow: 0 0 0 1px var(--tidal-border);
    border-radius: 0;

    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px var(--tidal-primary);
    }
  }

  :deep(.el-input__inner) {
    color: var(--tidal-text-main);
  }
}
</style>