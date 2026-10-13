export interface Port {
    id: number;
    portCode: string;         // 港口代码
    portNameZh: string;       // 港口中文名称
    portNameEn: string;       // 港口英文名称
    country: string;          // 所属国家
    region: string;           // 所属区域
    longitude: number;        // 经度
    latitude: number;         // 纬度
    timezone: string;         // 时区
    status: any;              // 状态
    createdAt: string;        // 创建时间
    updatedAt: string;        // 更新时间
}

export interface PortArea {
    id: number;
    portId: number;           // 所属港口ID
    areaCode: string;         // 港区代码
    areaNameZh: string;       // 港区中文名称
    areaNameEn: string;       // 港区英文名称
    areaType: any;            // 港区类型
    managementCompany: string; // 管理公司
    contactPhone: string;     // 联系电话
    longitude: number;        // 中心经度
    latitude: number;         // 中心纬度
    boundaryCoordinates: any; // 边界坐标
    status: any;              // 状态
    createdAt: string;        // 创建时间
    updatedAt: string;        // 更新时间
}

export interface Wharves {
    id: number;
    portAreaId: number;       // 所属港区ID
    wharfCode: string;        // 码头代码
    wharfNameZh: string;      // 码头中文名称
    wharfNameEn: string;      // 码头英文名称
    wharfLength: number;      // 码头长度(米)
    wharfDepth: number;       // 码头水深(米)
    constructionDate: string; // 建成日期
    wharfType: any;           // 码头类型
    operationalStatus: any;   // 运营状态
    managementCompany: string; // 管理公司
    contactPhone: string;     // 联系电话
    longitude: number;        // 中心经度
    latitude: number;         // 中心纬度
    createdAt: string;        // 创建时间
    updatedAt: string;        // 更新时间
}

export interface Anchorages {
    id: number;
    portAreaId: number;               // 所属港区ID
    anchorageCode: string;            // 锚地代码
    anchorageNameZh: string;          // 锚地中文名称
    anchorageNameEn: string;          // 锚地英文名称
    anchorageType: any;               // 锚地类型
    capacity: number;                 // 容纳船舶数量
    maxDraft: number;                 // 最大水深(米)
    minDraft: number;                 // 最小水深(米)
    longitude: number;                // 中心经度
    latitude: number;                 // 中心纬度
    radius: number;                   // 半径(海里)
    boundaryCoordinates: any;         // 边界坐标
    tideRestriction: string;          // 潮汐限制说明
    weatherRestriction: string;       // 气象限制说明
    status: any;                      // 状态
    createdAt: string;                // 创建时间
    updatedAt: string;                // 更新时间
}

export interface Berths {
    id: number;
    berthCode: string;             // 泊位编号
    portArea: string;              // 港区名称
    berthType: string;             // 泊位类别
    berthName: string;             // 泊位名称
    wharfDirection: string;        // 码头方向
    longitude: number;             // 经度
    latitude: number;              // 纬度
    startLatitude: number;         // 起始纬度
    startLongitude: number;        // 起始经度
    endLatitude: number;           // 终止纬度
    endLongitude: number;          // 终止经度
    berthDirection: string;        // 泊位方向
    maxDraft: number;              // 最大吃水
    berthLength: number;           // 泊位长度
    berthDepth: number;            // 泊位水深
    berthStatus: any;              // 泊位状态
    contactPhone: string;          // 联系电话
    createdAt: string;             // 创建时间
    updatedAt: string;             // 更新时间
}