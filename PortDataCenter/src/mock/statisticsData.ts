export const mockBillingMonthly = [
  { 
    id: 1, 
    shipName: '海丰江苏', 
    englishName: 'SITC JIANGSU', 
    nationality: '中国香港', 
    dynamic: '进港', 
    startDate: '11/25 14:00', 
    endDate: '11/25 16:00',
    grossTonnage: '26872',
    netTonnage: '11655',
    berth: '状元岙8#',
    pilotTime: '2.0',
    pilotDistance: '15.5',
    pilot: '测试引航员A',
    agent: '外代',
    totalFee: '12500.00',
    holidayFee: '0.00',
    nightFee: '0.00',
    invoiceNo: 'INV001',
    printTime: '2025-11-26',
    arrivalTime: '',
    turnInTime: '',
    remarks: ''
  }
]

export const mockBillingYearly = [
  { 
    id: 1, 
    month: '1月', 
    lastUnpaid: '50000.00', 
    currentActual: '1200000.00', 
    taxDeducted: '60000.00', 
    currentPaid: '1100000.00', 
    monthEndUnpaid: '90000.00', 
    arrivedUnpaid: '0.00' 
  }
]

export const mockAssessmentSafety = [
  { 
    id: 1, 
    shipName: '津航', 
    date: '2025-11-25', 
    dynamicTime: '14:00', 
    sailTime: '14:30', 
    berthTime: '16:00', 
    pilot: '测试引航员A', 
    towedShip: '无', 
    dynamic: '进港', 
    waitTime: '0.5' 
  }
]

export const mockAssessmentPilotageMonthly = [
  { 
    id: 1, 
    shipName: '海丰江苏', 
    englishName: 'SITC JIANGSU', 
    nationality: '中国香港', 
    routeType: '国际',
    dynamic: '进港', 
    startTime: '2025-11-25 14:00',
    endTime: '2025-11-25 16:00',
    grossTonnage: '26872', 
    netTonnage: '11655', 
    deadWeight: '35326', 
    length: '188.80', 
    width: '32.20', 
    draft: '10.00',
    berth: '状元岙8#', 
    boardingTime: '13:30', 
    boardingPlace: '20#登轮点', 
    tug: '温拖2', 
    mainPilot: '测试引航员A', 
    subPilot: '', 
    no: 'P2025112501',
    agent: '外代', 
    pickDropTime: '',
    height: '',
    foreignFee: '',
    remarks: ''
  }
]

export const mockAssessmentPilotageSummary = [
  {
    id: 1,
    month: '11月',
    foreign: '150',
    domestic: '80',
    gt250: '10',
    gt200_250: '25',
    netTonnage: '500000',
    grossTonnage: '800000',
    deadWeight: '1200000',
    dangerous: '5',
    container: '120',
    night: '30',
    restDay: '20',
    holiday: '10',
    distance: '5000',
    time: '600',
    remarks: ''
  }
]

export const mockAssessmentLengthGt250 = [
  { 
    id: 1, 
    shipName: '津航', 
    englishName: 'JIN HANG', 
    nationality: '中国', 
    dynamic: '进港', 
    timeDate: '2025-11-25 12:00',
    grossTonnage: '51130',
    netTonnage: '31267',
    deadWeight: '93069',
    length: '229.20',
    width: '38.00',
    draft: '6.50',
    berth: '状元岙8#',
    boardingTime: '11:00',
    boardingPlace: '圆屿锚地',
    tug: '温拖2',
    mainPilot: '测试引航员A',
    subPilot: '',
    agent: '外代'
  }
]

export const mockAssessmentHoliday = [
  { 
    id: 1, 
    shipName: '海丰江苏', 
    englishName: 'SITC JIANGSU', 
    nationality: '中国香港', 
    dynamic: '进港', 
    timeDate: '2025-10-01 14:00',
    grossTonnage: '26872', 
    netTonnage: '11655', 
    deadWeight: '35326', 
    length: '188.80', 
    width: '32.20', 
    draft: '10.00',
    berth: '状元岙8#', 
    boardingTime: '13:30', 
    boardingPlace: '20#登轮点', 
    tug: '温拖2', 
    mainPilot: '测试引航员A', 
    subPilot: '', 
    agent: '外代',
    remarks: '国庆节'
  }
]

export const mockAssessmentWeekend = [
  { 
    id: 1, 
    shipName: '长航津海', 
    englishName: 'CHANG HANG JIN HAI', 
    nationality: '中国', 
    dynamic: '移泊', 
    timeDate: '2025-11-23 09:00', // Sunday
    grossTonnage: '29195', 
    netTonnage: '16349', 
    deadWeight: '35551', 
    length: '196.36', 
    width: '32.25', 
    draft: '9.00',
    berth: '七里2#', 
    boardingTime: '08:30', 
    boardingPlace: '七里锚地', 
    tug: '温拖3', 
    mainPilot: '测试引航员B', 
    subPilot: '', 
    agent: '温州港代理',
    remarks: ''
  }
]

export const mockAssessmentNight = [
  { 
    id: 1, 
    shipName: '银河', 
    englishName: 'JK GALAXY', 
    nationality: '韩国', 
    dynamic: '进港', 
    timeDate: '2025-11-25 23:00',
    grossTonnage: '11481', 
    netTonnage: '5850', 
    deadWeight: '17556', 
    length: '131.90', 
    width: '22.00', 
    draft: '10.00',
    berth: '状元岙8#', 
    boardingTime: '22:00', 
    boardingPlace: '圆屿锚地', 
    tug: '温拖5', 
    mainPilot: '葛奔豪', 
    subPilot: '', 
    pickDropMachine: '',
    sendMachine: '',
    agent: '外代',
    remarks: ''
  }
]

export const mockAssessmentSpecial = [
  { 
    id: 1, 
    shipName: '特殊轮1', 
    englishName: 'SPECIAL ONE', 
    specialOp: '拖带',
    specialDesc: '无动力拖带', 
    nationality: '巴拿马', 
    dynamic: '进港', 
    startTime: '2025-11-25 10:00', 
    endTime: '2025-11-25 14:00',
    grossTonnage: '50000',
    netTonnage: '30000',
    deadWeight: '80000',
    length: '200',
    width: '30',
    draft: '10',
    berth: '码头A',
    boardingTime: '09:00',
    boardingPlace: '锚地A',
    tug: '拖轮A',
    mainPilot: '张三',
    subPilot: '李四',
    no: 'SP001',
    agent: '外代',
    pickDropTime: '',
    height: '',
    foreignFee: '',
    remarks: ''
  }
]

export const mockAssessmentRecord = [
  { 
    id: 1, 
    desc: '最大吃水', 
    shipName: '巨轮1号', 
    englishName: 'GIANT ONE', 
    nationality: '利比里亚', 
    dynamic: '进港', 
    timeDate: '2025-11-25',
    grossTonnage: '150000',
    netTonnage: '80000',
    deadWeight: '200000',
    length: '300',
    width: '50',
    draft: '18.5',
    berth: '深水泊位',
    boardingTime: '10:00',
    boardingPlace: '外锚地',
    tug: '拖轮组',
    mainPilot: '王五',
    subPilot: '赵六',
    agent: '中外运'
  }
]

export const mockAssessmentNewWharf = [
  {
    id: 1,
    shipName: '首靠轮',
    englishName: 'FIRST CALL',
    nationality: '中国',
    dynamic: '进港',
    timeDate: '2025-11-20 10:00',
    grossTonnage: '30000',
    netTonnage: '18000',
    deadWeight: '45000',
    length: '180',
    width: '28',
    draft: '9.5',
    berth: '新开发泊位1#',
    boardingTime: '09:00',
    boardingPlace: '锚地',
    tug: '温拖6',
    mainPilot: '钱七',
    subPilot: '',
    agent: '外代'
  }
]

export const mockAssessmentPlanChange = [
  { 
    id: 1, 
    agent: '外代', 
    shipName: '海丰江苏', 
    englishName: 'SITC JIANGSU', 
    status: '取消', 
    reason: '天气原因', 
    nationality: '中国香港', 
    dynamic: '进港',
    timeDate: '2025-11-25 14:00',
    grossTonnage: '26872',
    netTonnage: '11655',
    deadWeight: '35326',
    length: '188.80',
    width: '32.20',
    draft: '10.00',
    berth: '状元岙8#',
    boardingTime: '',
    boardingPlace: '',
    tug: '',
    mainPilot: '测试引航员A',
    subPilot: '',
    no: 'PC001',
    pickDropTime: '',
    height: '',
    foreignFee: '',
    remarks: ''
  }
]

export const mockPilots = [
  { id: 1, name: '测试引航员A', code: 'P001', level: '高级引航员', status: '在职', phone: '13800000000' },
  { id: 2, name: '测试引航员B', code: 'P002', level: '高级引航员', status: '在职', phone: '13800000000' }
]

export const mockBoats = [
  { id: 1, name: '引航艇1号', type: '高速艇', status: '正常', capacity: '12' },
  { id: 2, name: '引航艇2号', type: '交通艇', status: '维修', capacity: '20' }
]

export const mockAgents = [
  { id: 1, name: '温州港代理', code: 'AG001', contact: '张三', phone: '0577-88888888' },
  { id: 2, name: '外代', code: 'AG002', contact: '李四', phone: '0577-88888889' }
]

export const mockShips = [
  { id: 1, name: '海丰江苏', enName: 'SITC JIANGSU', nationality: '中国香港', type: '集装箱', length: '188.80', grossTonnage: '26872' },
  { id: 2, name: '津航', enName: 'JIN HANG', nationality: '中国', type: '散货船', length: '229.20', grossTonnage: '51130' }
]