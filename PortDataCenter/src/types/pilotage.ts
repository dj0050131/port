import type { ApiResponse, PaginatedResult } from './common'

export type PilotageStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

//   id: string
//   // Basic Info
//   applicationNo?: string // Request Code
//   status: PilotageStatus
//
//   shipName: string // 中文船名
//   englishName?: string // 英文船名
//   imoNumber?: string // IMO
//   mmsi?: string // MMSI
//   nationality?: string // 国籍
//   shipType?: string // 船舶种类
//   company?: string // 船公司
//   length?: number // 船长
//   width?: number // 船宽
//   depth?: number // 型深
//   grossTonnage?: number // 总吨
//   netTonnage?: number // 净吨
//   deadWeight?: number // 载重吨
//   engineType?: string // 主机种类
//   power?: number // 功率
//   speed?: number // 航速
//   thrusterPower?: number // 侧推功率
//   waterlineHeight?: number // 水面高度
//   aisInterface?: string // AIS引航接口
//   buildDate?: string // 建造日期
//
//   wharf?: string // 码头
//   berth?: string // 泊位
//   anchorage?: string // 预抵锚地
//   foreDraft?: number // 艏吃水
//   aftDraft?: number // 艉吃水
//   cargo?: string // 装货名称
//   cargoQty?: number // 数量
//   etdEta?: string // ETD/ETA
//   lastPort?: string // 上一港
//   nextPort?: string // 下一港
//
//   ladderStatus?: string
//   ladderType?: string
//   freeboardHeight?: number
//
//   agentName?: string // 代理名称
//   invoiceType?: string // 发票类型
//   payerName?: string // 付款单位名称
//   payerAddress?: string // 付款单位地址
//   payerPhone?: string // 付款单位电话
//   taxId?: string // 纳税人识别号
//   bank?: string // 开户行
//   account?: string // 账号
//
//   remarks?: string // 备注
// }
export interface PilotageApplication {
    id: string
    // Basic Info
    requestCode?: string // Request Code
    requestStatus: PilotageStatus
    createdAt: string

    nameZh: string // 中文船名
    nameEn?: string // 英文船名
    vesselCallSign?: string // 呼号
    imoNumber?: string // IMO
    mmsiNumber?: string // MMSI
    nationality?: string // 国籍
    vesselType?: string // 船舶种类
    ownerZh?: string // 船舶所有人（中文）
    ownerEn?: string // 船舶所有人（英文）
    vesselLength?: number // 船长
    vesselWidth?: number // 船宽
    depth?: number // 型深
    grossTonnage?: number // 总吨
    netTonnage?: number // 净吨
    deadweight?: number // 载重吨
    mainEngineType?: string // 主机种类
    mainEnginePower?: number // 功率
    speed?: number // 航速
    bowThrusterPower?: number // 侧推功率
    airDraft?: number // 水面上高度
    aisPilotInterface?: string // AIS引航接口
    buildDate?: string // 建造日期
    routeType?: string // 航线种类 (International/Domestic)
    routeScope?: string // 航线范围
    fullLoadDraft?: number // 满载吃水
    maxHeight?: number // 最大高度
    vesselColor?: string // 船舶颜色
    funnelColor?: string // 烟囱颜色
    funnelMark?: string // 烟囱标志
    portOfRegistry?: string // 船籍港
    classificationSociety?: string // 船级社
    managementCompany?: string // 管理公司
    insuranceCompany?: string // 保险公司
    pniClub?: string // 保赔协会
    blacklistRecord?: string // 黑名单记录
    badStatusRecord?: string // 不良状态记录
    otherAbnormalRecord?: string // 其他非正常状态记录

    voyageNumber?: string // 航次编号
    voyageType?: string // 航次类型
    previousPortId?: string // 上一港id
    nextPortId?: string // 下一港id
    portOfDepartureId?: number // 出发港id
    portOfDestinationId?: number // 目的港id
    eta?: string // 预计到港时间
    etaAnchorage?: string // 预抵锚地时间
    ata?: string // 实际到港时间
    etd?: string // 预计离港时间
    atd?: string // 实际离港时间
    cargoName?: string // 装货名称
    cargoType?: string // 货物类型
    cargoQuantity?: number // 货物数量
    cargoUnit?: string // 货物单位
    draftForward?: number // 前吃水
    draftAft?: number // 后吃水
    voyageStatus?: string // 航次状态
    plannedBerthId?: number // 计划靠泊泊位ID
    actualBerthId?: number // 实际靠泊泊位ID
    plannedAnchorage?: string // 计划锚地
    actualAnchorage?: string // 实际锚地
    voyageRemarks?: string // 航次备注

    // 位置信息 (使用后端实际字段名)
    plannedStartId?: number // 开始位置ID
    plannedEndId?: number // 结束位置ID
    plannedStartName?: string // 开始位置名称
    plannedEndName?: string // 结束位置名称
    startLocationType?: 'BERTH' | 'ANCHORAGE' // 开始位置类型
    endLocationType?: 'BERTH' | 'ANCHORAGE' // 结束位置类型
    portAreaId?: number // 港区ID
    wharfId?: number // 码头ID

    ladderStatus?: string
    ladderType?: string
    freeboardHeight?: number
    inspectionDate?: string // 检查日期
    inspectionResult?: string // 检查结果
    inspector?: string // 检查人员
    ladderRemarks?: string // 引航梯备注

    agentId?: number // 代理公司ID
    agentName?: string // 代理名称
    payingUnitName?: string // 付款单位名称
    invoiceType?: string // 发票类型
    taxpayerId?: string // 纳税人识别号
    payingUnitAddress?: string // 付款单位地址
    payingUnitPhone?: string // 付款单位电话
    payingUnitBank?: string // 付款单位开户银行
    payingUnitBankAccount?: string // 付款单位银行账号
    billingVoucherId?: number // 关联计费凭证ID

    vesselId?: number // 船舶ID
    voyageId?: number // 航次ID
    expectedBerthId?: number // 预计作业泊位ID
    bargeNumber?: string // 驳船号
    inboundDraftForward?: number // 进口吃水前
    inboundDraftAft?: number // 进口吃水后
    totalPassengers?: number // 旅客总数
    totalCrew?: number // 船员总数
    inboundCargoName?: string // （进）载货名称
    inboundCargoTotalWeight?: number // （进）载货总吨数
    inboundGeneralCargoDetails?: string // （进）普通货物名称吨数
    inboundDangerousCargoDetails?: string // （进）危险货物名称吨数
    outboundCargoName?: string // （出）载货名称
    outboundCargoTotalWeight?: number // （出）载货总吨数
    outboundGeneralCargoDetails?: string // （出）普通货物名称吨数
    outboundDangerousCargoDetails?: string // （出）危险货物名称吨数
    requestNotes?: string // 申请表备注
    applicant?: string // 填报人
    applicationDate?: string // 填报日期
    requestSource?: string // 申请表信息来源
    visaProcessingResult?: string // 对应签证单处理结果
    reviewerId?: number // 审核人ID
    reviewedAt?: string // 审核时间
    rejectReason?: string // 驳回原因
    priority?: string // 优先级
    specialRequirements?: string // 特殊要求

    reviewerUserCode?: string // 用户代码
    reviewerAccount?: string // 用户账号
    reviewerUserType?: string // 用户类型
    reviewerUsername?: string // 用户名称
    reviewerApplyTime?: string // 申请时间
    reviewerPhone?: string // 联系电话
    reviewerEmail?: string // E-mail
    reviewerCurrentStatus?: string // 当前状态
    reviewerAgentLevelType?: string // 代理级别类型
    reviewerParentAgentId?: number // 所属代理主管ID
    reviewerAvatar?: string // 头像URL
    reviewerLastLoginAt?: string // 最后登录时间
    reviewerDepartment?: string // 部门
    reviewerPosition?: string // 职位

    updatedAt?: string // 更新时间
}

export interface PilotagePlan {
  id: string
  planCode?: string
  status: PilotageStatus

  shipName: string
  englishName?: string
  callSign?: string
  nationality?: string
  shipType?: string
  grossTonnage?: number
  netTonnage?: number
  deadWeight?: number
  length?: number
  width?: number
  draft?: number // Current Draft

  // Plan Details
  berth?: string // 靠离泊位
  boardingPoint?: string // 登离轮点
  time?: string // 时间

  // Pilotage Task
  pilot?: string // 主引
  subPilot?: string // 副引
  tugs?: string[] // 拖轮 (e.g. ["温拖1", "温拖2"])
  tugWorkTime?: string // 拖轮作业时间

  remarks?: string
}

export interface Tug {
  id: string
  name: string
  power: number
  capacity: number
}

// Dashboard & Timeline Types
export interface PilotageDashboard {
  totalRequests: number
  pendingRequests: number
  approvedRequests: number
  completedPlans: number
  todayPlans: number
  activePilots: number
}

export interface PilotageTimelineItem {
  id: number
  time: string
  description: string
  status: PilotageStatus
}

export interface PilotDispatchOrder {
  id: string | number
  vessel: string
  status: string
  pilotName: string
  contact: string
  departure: string
  arrival: string
}

export interface WorkSheet {
  id: number
  planId: number
  pilotId: number
  vesselId: number
  startTime: string
  endTime: string
  signature?: string
  remarks?: string
  createdAt: string
}

// API Response Types
export type PilotageApplicationResponse = ApiResponse<PaginatedResult<PilotageApplication>>