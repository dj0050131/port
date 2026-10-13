import type { UnwrapRef } from "vue";

/**
 * 引航调度计划类型
 */
export interface DispatchPlan {
    id: number | string;
    planId: number | string;
    planCode: string;
    planStartTime: string;
    planEndTime: string;
    actualStartTime?: string;
    actualEndTime?: string;
    planStatus: string;
    dispatchStatus: string;

    // 船舶信息
    vesselNameZh: string;
    vesselNameEn: string;
    vesselCallSign: string;
    nationality: string;
    vesselLength: number | string;
    vesselWidth: number | string;
    grossTonnage: number | string;
    netTonnage: number | string;
    deadWeight: number | string;

    // 航次信息
    voyageNumber: string;
    voyageType: string;
    eta: string;
    etd: string;

    // 代理信息
    agentName: string;
    agentContact: string;
    agentPhone: string;

    // 引航员信息
    mainPilot: string;
    pilotPhone: string;
    pilotShortNum: string;

    // 驾驶员信息
    driverName: string;
    driverPhone: string;
    licenseNumber: string;

    // 车辆信息
    licensePlate: string;
    vehicleModel: string;
    vehicleStatus: string;

    // 资源信息
    tugboats: string;
    pilotBoats: string;

    confirmedCount: number;
    totalResourcesCount: number;
    confirmationProgress: number;

    // 位置信息
    startLocationName: string;
    endLocationName: string;
    boardingTime: string;
    leavingTime: string;

    // 附加信息
    notes: string;
    status: string;
    dynamic: string;
    draft: string;
    berth: string;
    type: string;
}

/**
 * 调度查询参数类型
 */
export interface DispatchQueryParams {
    page: number;
    size: number;
    planCode?: string;
    vesselName?: string;
    planStatus?: string;
    pilotName?: string;
    confirmationLevel?: string;
    startDate?: string;
    endDate?: string;
    [key: string]: any;
}

/**
 * 调度时间更新参数类型
 */
export interface DispatchTimeUpdateParams {
    boardingTime?: string;
    leavingTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    notes?: string;
}

/**
 * 调度详情类型
 */
export interface DispatchDetail {
    planInfo: PlanBaseInfo;
    vessel?: VesselInfo;
    voyage?: VoyageInfo;
    agent?: AgentInfo;
    pilotAssignments: PilotAssignment[];
    driverAssignments: DriverAssignment[];
    tugboatAssignments: TugboatAssignment[];
    pilotBoatAssignments: PilotBoatAssignment[];
    vehicleAssignments: VehicleAssignment[];
    confirmations: Confirmation[];
    confirmationProgress: number;
    operationLogs: OperationLog[];
}

/**
 * 计划基本信息
 */
export interface PlanBaseInfo {
    id: number | string;
    planCode: string;
    planStartTime: string;
    planEndTime: string;
    actualStartTime?: string;
    actualEndTime?: string;
    planStatus: string;
    startLocationType?: string;
    startLocationName?: string;
    endLocationType?: string;
    endLocationName?: string;
    boardingTime?: string;
    leavingTime?: string;
    weatherImpact?: string;
    notes?: string;
    delayReason?: string;
}

/**
 * 船舶信息
 */
export interface VesselInfo {
    id: number | string;
    nameZh: string;
    nameEn: string;
    vesselCallSign: string;
    vesselLength: number;
    vesselWidth: number;
    grossTonnage: number;
    netTonnage: number;
    deadweight: number;
    nationality: string;
    [key: string]: any;
}

/**
 * 航次信息
 */
export interface VoyageInfo {
    id: number | string;
    voyageNumber: string;
    voyageType: string;
    eta: string;
    etd: string;
    agentId?: number;
    [key: string]: any;
}

/**
 * 代理信息
 */
export interface AgentInfo {
    id: number | string;
    companyName: string;
    contactPerson: string;
    phone: string;
    [key: string]: any;
}

/**
 * 引航员分配
 */
export interface PilotAssignment {
    id: number | string;
    planId: number | string;
    pilotId: number | string;
    roleType: string;
    assignmentNotes?: string;
    estimatedStartTime?: string;
    estimatedEndTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    status: string;
    [key: string]: any;
}

/**
 * 驾驶员分配
 */
export interface DriverAssignment {
    id: number | string;
    planId: number | string;
    driverId: number | string;
    taskType: string;
    estimatedStartTime?: string;
    estimatedEndTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    status: string;
    [key: string]: any;
}

/**
 * 拖轮分配
 */
export interface TugboatAssignment {
    id: number | string;
    planId: number | string;
    tugboatId: number | string;
    assignmentType: string;
    estimatedStartTime?: string;
    estimatedEndTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    status: string;
    [key: string]: any;
}

/**
 * 引航艇分配
 */
export interface PilotBoatAssignment {
    id: number | string;
    planId: number | string;
    pilotBoatId: number | string;
    purpose: string;
    estimatedStartTime?: string;
    estimatedEndTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    status: string;
    [key: string]: any;
}

/**
 * 车辆分配
 */
export interface VehicleAssignment {
    id: number | string;
    planId: number | string;
    vehicleId: number | string;
    purpose: string;
    estimatedStartTime?: string;
    estimatedEndTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    status: string;
    [key: string]: any;
}

/**
 * 确认记录
 */
export interface Confirmation {
    id: number | string;
    planId: number | string;
    resourceType: string;
    resourceId: number | string;
    confirmMethod: string;
    confirmedBy?: number;
    confirmedAt: string;
    confirmationStatus: string;
    notes?: string;
    [key: string]: any;
}

/**
 * 操作日志
 */
export interface OperationLog {
    operationType: string;
    operatorId: number | string;
    operatorName: string;
    operationTime: string;
    description: string;
    beforeStatus: string;
    afterStatus: string;
}

/**
 * 分页响应类型
 */
export interface PaginatedResult<T> {
    list: UnwrapRef<T[]> | T[];
    total: number;
    page: number;
    size: number;
    pages: number;
}

/**
 * API响应类型
 */
export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
    timestamp: string;
}

/**
 * 分页查询参数
 */
export interface PaginationQuery {
    page: number;
    pageSize: number;
}

/**
 * 地理坐标
 */
export interface GeoCoordinate {
    lng: number;
    lat: number;
}

/**
 * 可为空的类型
 */
export type Nullable<T> = T | null | undefined;