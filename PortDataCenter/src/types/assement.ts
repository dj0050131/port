export interface AssessmentItem {
  id?: number;
  shipName?: string;
  englishName?: string;
  nationality?: string;
  routeType?: string;
  dynamic?: string;
  timeDate?: string;
  startTime?: string;
  endTime?: string;
  grossTonnage?: number;
  netTonnage?: number;
  deadWeight?: number;
  length?: number;
  width?: number;
  draft?: number;
  berth?: string;
  boardingTime?: string;
  boardingPlace?: string;
  tug?: string;
  pilotName?: string[]; // 引航员名字，数组形式，包含主副引
  no?: string;
  agent?: string;
  pickDropTime?: string;
  height?: number;
  foreignFee?: number;
  remarks?: string;
  date?: string;
  dynamicTime?: string;
  sailTime?: string;
  berthTime?: string;
  towedShip?: string;
  waitTime?: string;
  month?: string | number;
  foreign?: number;
  domestic?: number;
  gt250?: number;
  gt200_250?: number;
  dangerous?: number;
  container?: number;
  night?: number;
  restDay?: number;
  holiday?: number;
  distance?: number;
  time?: string;
  specialOp?: string;
  specialDesc?: string;
  desc?: string;
  status?: string;
  reason?: string;
  pickDropMachine?: string;
  sendMachine?: string;
  cumulativeTotalAmount?: number;
  cumulativeActualReceivedAmount?: number;
  cumulativeArrears?: number;
  billingVoucherCount?: number;
  totalAmount?: number;
  actualReceivedAmount?: number;
  arrears?: number;
  vesselCount?: number;
  agentCount?: number;
  discountAmount?: number;
  lateFee?: number;
  unpaidCount?: number;
  partialPaidCount?: number;
  fullyPaidCount?: number;
  internationalRouteAmount?: number;
  domesticRouteAmount?: number;
}

// 船务安全科航行(月份)统计
export interface AssessmentSafetyItem extends AssessmentItem {
  shipName: string;
  date: string;
  dynamicTime: string;
  sailTime: string;
  berthTime: string;
  pilotName: string[]; // 引航员名字
  towedShip: string;
  dynamic: string;
  waitTime: string;
}

export interface AssessmentPilotageMonthlyItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  routeType: string;
  dynamic: string;
  startTime: string;
  endTime: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  no: string;
  agent: string;
  pickDropTime: string;
  height: number;
  foreignFee: number;
  remarks: string;
}

export interface AssessmentPilotageSummaryItem extends AssessmentItem {
  month: string | number;
  foreign: number;
  domestic: number;
  gt250: number;
  gt200_250: number;
  netTonnage: number;
  grossTonnage: number;
  deadWeight: number;
  dangerous: number;
  container: number;
  night: number;
  restDay: number;
  holiday: number;
  distance: number;
  time: string;
  remarks: string;
}

export interface AssessmentLengthGt250Item extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  agent: string;
  remarks: string;
}

export interface AssessmentHolidayItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  agent: string;
  remarks: string;
}

export interface AssessmentWeekendItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  agent: string;
  remarks: string;
}

export interface AssessmentNightItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  pickDropMachine: string;
  sendMachine: string;
  agent: string;
  remarks: string;
}

export interface AssessmentSpecialItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  specialOp: string;
  specialDesc: string;
  nationality: string;
  dynamic: string;
  startTime: string;
  endTime: string;
  grossTonnage: number;
  netTonnage: number;
  pilotName: string[]; // 引航员名字
  foreignFee: number;
  remarks: string;
}

export interface AssessmentRecordBreakItem extends AssessmentItem {
  desc: string;
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  length: number;
  draft: number;
  berth: string;
  pilotName: string[]; // 引航员名字
}

export interface AssessmentPlanChangeItem extends AssessmentItem {
  agent: string;
  shipName: string;
  englishName: string;
  status: string;
  reason: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  pilotName: string[]; // 引航员名字
  remarks: string;
}

export interface AssessmentNewWharfItem extends AssessmentItem {
  shipName: string;
  englishName: string;
  nationality: string;
  dynamic: string;
  timeDate: string;
  grossTonnage: number;
  netTonnage: number;
  deadWeight: number;
  length: number;
  width: number;
  draft: number;
  berth: string;
  boardingTime: string;
  boardingPlace: string;
  tug: string;
  pilotName: string[]; // 引航员名字
  agent: string;
  remarks: string;
}

// }