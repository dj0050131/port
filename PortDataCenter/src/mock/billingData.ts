export const mockFiscalYears = [
  {
    id: 1,
    year: '2025',
    targetTonnage: '500000',
    targetRevenue: '15000000',
    estimatedRevenue: '12000000',
    status: true
  },
  {
    id: 2,
    year: '2024',
    targetTonnage: '480000',
    targetRevenue: '14000000',
    estimatedRevenue: '13500000',
    status: false
  }
]

export const mockPayers = [
  {
    id: 1,
    agentName: '测试代理A',
    payerName: '测试付款方A',
    invoiceType: '增值税专用发票',
    taxId: '91300000000000000X',
    address: '测试地址',
    phone: '00000000000',
    bank: '测试银行',
    account: '0000000000000000000'
  },
  {
    id: 2,
    agentName: '测试代理B',
    payerName: '测试付款方B',
    invoiceType: '普通发票',
    taxId: '91300000000000000Y',
    address: '测试地址',
    phone: '00000000000',
    bank: '测试银行',
    account: '0000000000000000000'
  }
]

export const mockDirectRoutes = [
  {
    id: 1,
    name: '状元岙8# - 七里2#',
    startPoint: '状元岙8#',
    endPoint: '七里2#',
    mileage: '15.5',
    remarks: '常规直航路线',
    creator: 'admin',
    createTime: '2024-01-15 10:00:00',
    editor: 'admin',
    editTime: '2024-05-20 14:30:00'
  },
  {
    id: 2,
    name: '小门液化1# - 乐清湾1#',
    startPoint: '小门液化1#',
    endPoint: '乐清湾1#',
    mileage: '22.8',
    remarks: 'LNG专用',
    creator: 'dispatcher',
    createTime: '2024-02-10 09:00:00',
    editor: 'dispatcher',
    editTime: '2024-02-10 09:00:00'
  }
]

export const mockVisaList = [
  {
    id: 1,
    status: '待计费',
    reason: '正常',
    shipName: '海丰江苏',
    englishName: 'SITC JIANGSU',
    mainPilot: '测试引航员A',
    berth: '状元岙8#',
    boardingPoint: '圆屿锚地',
    startTime: '2025-11-25 14:00',
    endTime: '2025-11-25 16:00',
    grossTonnage: '51130.00',
    netTonnage: '31267.00',
    length: '229.20',
    width: '38.00',
    draft: '6.50',
    agent: '外代',
    opType: '进港',
    voyageType: '国际航线',
    remarks: ''
  },
  {
    id: 2,
    status: '待计费',
    reason: '移泊',
    shipName: '长航津海',
    englishName: 'CHANG HANG JIN HAI',
    mainPilot: '测试引航员B',
    berth: '七里2#',
    boardingPoint: '七里锚地',
    startTime: '2025-11-25 09:00',
    endTime: '2025-11-25 11:00',
    grossTonnage: '29195.00',
    netTonnage: '16349.00',
    length: '196.36',
    width: '32.25',
    draft: '9.00',
    agent: '温州港代理',
    opType: '移泊',
    voyageType: '国内航线',
    remarks: ''
  }
]

export const mockAuditList = [
  {
    id: 1,
    shipName: '津航',
    englishName: 'JIN HANG',
    agent: '外代',
    routeType: '国际航线',
    opType: '进港',
    netTonnage: '31267.00',
    power: '15000',
    baseFee: '12500.00',
    surcharge: '3750.00',
    otherFee: '500.00',
    total: '16750.00',
    pilotDate: '2025-11-25',
    maker: 'system',
    makeDate: '2025-11-26',
    status: '待审核',
    remarks: ''
  }
]

export const mockPaymentList = [
  {
    id: 1,
    shipName: '津航',
    englishName: 'JIN HANG',
    agent: '外代',
    amount: '16750.00',
    invoiceNo: '',
    status: '待到款',
    confirmTime: '',
    confirmer: ''
  }
]

export const mockTransferList = [
  {
    id: 1,
    name: '2025年10月引航费移交',
    amount: '1,250,000.00',
    month: '2025-10',
    baseFee: '900,000.00',
    surcharge: '300,000.00',
    otherFee: '50,000.00',
    taxTotal: '75,000.00',
    vat: '75,000.00',
    cityTax: '5,250.00',
    eduTax: '2,250.00',
    localEduTax: '1,500.00',
    adjustment: '0.00',
    remarks: '',
    status: '已移交',
    transferTime: '2025-11-01 10:00:00'
  }
]

export const mockInvoiceVoidList = [
  {
    id: 1,
    invoiceNo: 'INV202510001',
    shipName: '银河',
    englishName: 'JK GALAXY',
    amount: '8500.00',
    reason: '开票信息错误',
    status: '已作废',
    operator: 'admin',
    time: '2025-10-28 14:00'
  }
]