import { faker } from '@faker-js/faker/locale/zh_CN'
import type {
    AlertItem,
    BerthStatus,
    DeviceStatusItem,
    FeeBreakdownItem,
    FeeStatistics,
    MaintenanceLog,
    OperationsOverview,
    PilotPlan,
    PilotRouteSegment,
    PilotTaskItem,
    PortMapPayload,
    ShipDynamic,
    SystemStatusItem,
    SystemStatus,
    TrafficMetric,
    TrafficOverview,
    TrafficSourceItem,
    WeatherTide, SystemPerformanceData, SystemMetricsItem, SystemPerformanceSummary,
} from '@/types/dashboard'
import type {
  MapPoint,
  TourismDashboardDataset,
  TourismMetric,
  VehicleRecord as TourismVehicleRecord,
} from '@/types/tourism'
import { generateGeoPoint, getPilotRoute, randomEnumValue } from './map-utils'
import { formatDateTime, getFutureTime, getPastTime } from './date-utils'

export function createSystemPerformanceData(): SystemPerformanceData {
    const metrics: SystemMetricsItem[] = Array.from({ length: 12 }).map((_, idx) => {
        const baseTime = new Date()
        baseTime.setHours(baseTime.getHours() - 11 + idx) // 从11小时前到当前时间

        return {
            timestamp: formatDateTime(baseTime),
            cpuUsage: faker.number.float({ min: 25, max: 75, fractionDigits: 1 }),      // CPU使用率 25-75%
            memoryUsage: faker.number.float({ min: 40, max: 85, fractionDigits: 1 }),   // 内存使用率 40-85%
            diskUsage: faker.number.float({ min: 30, max: 65, fractionDigits: 1 }),     // 磁盘使用率 30-65%
            networkIn: faker.number.float({ min: 5, max: 50, fractionDigits: 1 }),      // 网络流入速度 5-50 MB/s
            networkOut: faker.number.float({ min: 3, max: 30, fractionDigits: 1 }),     // 网络流出速度 3-30 MB/s
            loadAverage: faker.number.float({ min: 0.2, max: 2.5, fractionDigits: 2 }), // 系统负载 0.2-2.5
            responseTime: faker.number.int({ min: 50, max: 300 }),                     // 响应时间 50-300ms
        }
    })

    const cpuUsages = metrics.map(m => m.cpuUsage)
    const memoryUsages = metrics.map(m => m.memoryUsage)
    const loadAverages = metrics.map(m => m.loadAverage)
    const responseTimes = metrics.map(m => m.responseTime)

    const summary: SystemPerformanceSummary = {
        avgCpuUsage: Number((cpuUsages.reduce((sum, val) => sum + val, 0) / cpuUsages.length).toFixed(2)),
        avgMemoryUsage: Number((memoryUsages.reduce((sum, val) => sum + val, 0) / memoryUsages.length).toFixed(2)),
        peakLoad: Number(Math.max(...loadAverages).toFixed(2)),
        avgResponseTime: Math.round(responseTimes.reduce((sum, val) => sum + val, 0) / responseTimes.length),
    }

    return {
        metrics,
        summary,
    }
}

const shipTypes = ['cargo', 'container', 'tanker', 'passenger', 'bulk'] as const
const shipStatuses = ['arrived', 'departed', 'anchored', 'docking', 'underway'] as const
const pilotStatuses = ['scheduled', 'in-progress', 'completed', 'delayed'] as const
const alertLevels = ['critical', 'warning', 'info', 'normal'] as const
const systemModules = [
  '数据采集',
  '航线监控',
  '费收结算',
  '引航排班',
  '安防联动',
  '能源监测',
] as const

const provinceCoords: Record<string, [number, number]> = {
  江苏: [118.78, 32.04],
  山东: [117, 36.65],
  浙江: [120.19, 29.86],
  上海: [121.47, 31.23],
  福建: [119, 26],
  广东: [113.27, 23.13],
  辽宁: [123.43, 41.8],
  天津: [117.2, 39.13],
  广西: [108.33, 22.84],
  海南: [110.35, 20.02],
}

export const createShipDynamics = (count = 12): ShipDynamic[] =>
  Array.from({ length: count }).map(() => {
    const type = randomEnumValue(shipTypes)
    const status = randomEnumValue(shipStatuses)
    const position = generateGeoPoint()
    return {
      id: faker.string.uuid(),
      name: `${faker.location.city()}号`,
      imo: faker.string.numeric({ length: 8 }),
      type,
      status,
      speed: faker.number.float({ min: 0, max: 18, fractionDigits: 1 }),
      heading: faker.number.int({ min: 0, max: 360 }),
      destination: faker.location.city(),
      eta: formatDateTime(getFutureTime(faker.number.int({ min: 1, max: 8 }) * 60)),
      etd: formatDateTime(getPastTime(faker.number.int({ min: 1, max: 12 }) * 60)),
      position,
      pilotRequired: faker.datatype.boolean(),
      berthId: faker.datatype.boolean() ? `B-${faker.number.int({ min: 1, max: 12 })}` : undefined,
    }
  })

export const createPilotPlans = (ships: ShipDynamic[]): PilotPlan[] =>
  ships.slice(0, 8).map((ship) => {
    const schedule = getFutureTime(faker.number.int({ min: 0, max: 6 }) * 60)
    const route = getPilotRoute(ship.position)
    return {
      id: faker.string.uuid(),
      shipId: ship.id,
      shipName: ship.name,
      pilotName: faker.person.fullName(),
      schedule: formatDateTime(schedule),
      status: randomEnumValue(pilotStatuses),
      route,
      remarks: faker.lorem.sentence(),
    }
  })

export const createWeatherTide = (): WeatherTide => {
  const baseTemp = faker.number.int({ min: 12, max: 26 })
  const baseTide = faker.number.float({ min: 1.2, max: 4.6, fractionDigits: 2 })
  return {
    weather: {
      temperature: baseTemp,
      humidity: faker.number.int({ min: 45, max: 95 }),
      visibility: faker.number.int({ min: 2, max: 10 }),
      windSpeed: faker.number.float({ min: 1, max: 8, fractionDigits: 1 }),
      windDirection: faker.helpers.arrayElement(['东南', '正南', '西南', '东北', '西北']),
      condition: faker.helpers.arrayElement(['多云', '晴朗', '小雨', '阵雨', '阴']),
      pressure: faker.number.int({ min: 990, max: 1020 }),
    },
    tide: {
      currentHeight: baseTide,
      nextHighTide: formatDateTime(getFutureTime(90)),
      nextLowTide: formatDateTime(getFutureTime(240)),
      surgeWarning: faker.datatype.boolean(),
    },
    forecast: Array.from({ length: 24 * 7 }).map((_, idx) => ({
      time: formatDateTime(getFutureTime(idx * 60)),
      temperature: baseTemp + faker.number.int({ min: -2, max: 2 }),
      tide: baseTide + faker.number.float({ min: -0.5, max: 0.5, fractionDigits: 2 }),
      condition: faker.helpers.arrayElement(['晴', '多云', '小雨', '阴', '阵雨']),
      wind: faker.helpers.arrayElement(['东风', '西风', '南风', '北风', '东北风']),
      windLevel: faker.number.int({ min: 1, max: 4 }),
    })),
  }
}

const breakdownLabels = ['引航费', '泊位费', '拖轮费', '服务费', '能源补给'] as const

export const createFeeStatistics = (): FeeStatistics => {
  const breakdown: FeeBreakdownItem[] = breakdownLabels.map((label) => ({
    label,
    value: faker.number.int({ min: 80, max: 260 }) * 1000,
  }))
  const total = breakdown.reduce((sum, item) => sum + item.value, 0)
  return {
    total,
    currency: 'CNY',
    transactions: faker.number.int({ min: 85, max: 160 }),
    growth: faker.number.float({ min: -4, max: 12, fractionDigits: 1 }),
    breakdown,
    trend: Array.from({ length: 12 }).map((_, idx) => ({
      time: `${idx + 1}:00`,
      value: faker.number.int({ min: 20, max: 120 }) * 1000,
    })),
    category: breakdown.map((item) => ({
      name: item.label,
      value: item.value,
    })),
  }
}

export const createAlerts = (): AlertItem[] =>
  Array.from({ length: 8 }).map(() => ({
    id: faker.string.uuid(),
    level: randomEnumValue(alertLevels),
    message: faker.helpers.arrayElement([
      '靠泊时间超时，请检查船舶状态。',
      '潮位异常，建议调整引航计划。',
      '气象突变，建议开启应急预案。',
      '泊位占用率过高，请协调调度。',
      '费收系统有未完成的核销任务。',
    ]),
    detail: faker.lorem.sentence(),
    timestamp: formatDateTime(getPastTime(faker.number.int({ min: 1, max: 180 }))),
    acknowledged: faker.datatype.boolean(),
  }))

export const createSystemStatus = (): SystemStatusItem[] =>
  Array.from(systemModules).map((module) => ({
    module,
    status: faker.helpers.arrayElement<SystemStatus>(['healthy', 'degraded', 'offline']),
    lastUpdate: formatDateTime(getPastTime(faker.number.int({ min: 1, max: 30 }))),
    uptime: faker.number.float({ min: 95, max: 99.99, fractionDigits: 2 }),
  }))

export const createBerths = (): BerthStatus[] => {
  return Array.from({ length: 12 }).map((_, idx) => {
    const position = generateGeoPoint(0.012)
    const status = faker.helpers.arrayElement<BerthStatus['status']>(['occupied', 'available', 'maintenance'])
    return {
      id: `B-${idx + 1}`,
      name: `泊位 ${idx + 1}`,
      position,
      status,
      shipId: status === 'occupied' ? faker.string.uuid() : undefined,
    }
  })
}

export const createPortMapPayload = (ships: ShipDynamic[]): PortMapPayload => {
  const berths = createBerths()
  const routes: PilotRouteSegment[] = ships.slice(0, 6).map((ship) => ({
    from: generateGeoPoint(0.2),
    to: ship.position,
    shipId: ship.id,
  }))

  return {
    ships,
    berths,
    routes,
  }
}

const buildTrafficMetrics = (): TrafficMetric[] => [
  {
    id: 'today',
    label: '今日船舶',
    value: faker.number.int({ min: 12000, max: 38000 }),
    unit: '人次',
    trend: faker.number.float({ min: -3, max: 12, fractionDigits: 1 }),
    subLabel: '同比',
  },
  {
    id: 'weekly',
    label: '七日船舶',
    value: faker.number.int({ min: 68000, max: 140000 }),
    unit: '人次',
    trend: faker.number.float({ min: -5, max: 15, fractionDigits: 1 }),
    subLabel: '环比',
  },
  {
    id: 'ships',
    label: '今日靠泊',
    value: faker.number.int({ min: 60, max: 160 }),
    unit: '艘',
    trend: faker.number.float({ min: -4, max: 10, fractionDigits: 1 }),
  },
  {
    id: 'pilotage',
    label: '引航完成',
    value: faker.number.int({ min: 35, max: 90 }),
    unit: '次',
    trend: faker.number.float({ min: -2, max: 12, fractionDigits: 1 }),
  },
]

export const createTrafficOverview = (): TrafficOverview => {
  const provinces = Object.keys(provinceCoords)
  const sourceRanking: TrafficSourceItem[] = provinces.map((province, idx) => {
    const value = faker.number.int({ min: 8000, max: 38000 })
    return {
      rank: idx + 1,
      province,
      value,
      ratio: Number(faker.number.float({ min: 4, max: 28, fractionDigits: 1 })),
    }
  })

  const deviceStatuses: DeviceStatusItem[] = Array.from({ length: 6 }).map(() => ({
    name: `${faker.helpers.arrayElement(['泊位监测', '能耗感知', '拖轮调度', '堆场视频'])}-${faker.number.int({ min: 1, max: 12 })}`,
    category: faker.helpers.arrayElement(['传感器', '摄像头', '网络网关']),
    onlineRate: faker.number.float({ min: 88, max: 99.6, fractionDigits: 2 }),
    alarms: faker.number.int({ min: 0, max: 5 }),
  }))

  const trend = Array.from({ length: 24 }).map((_, idx) => ({
    time: `${String(idx).padStart(2, '0')}:00`,
    inbound: faker.number.int({ min: 200, max: 900 }),
    outbound: faker.number.int({ min: 180, max: 780 }),
  }))

  const typeDistribution = [
    { name: '散货', value: faker.number.int({ min: 18, max: 32 }) },
    { name: '集装箱', value: faker.number.int({ min: 25, max: 40 }) },
    { name: '油轮', value: faker.number.int({ min: 12, max: 24 }) },
    { name: '客运', value: faker.number.int({ min: 8, max: 16 }) },
    { name: '滚装', value: faker.number.int({ min: 6, max: 12 }) },
  ]

  const geoSpots = sourceRanking.slice(0, 7).map((item) => {
    const coord = provinceCoords[item.province] ?? [120, 30]
    return { name: item.province, value: [coord[0], coord[1], Math.round(item.value / 600)] }
  })

  return {
    metrics: buildTrafficMetrics(),
    sourceRanking,
    deviceStatuses,
    trend,
    typeDistribution,
    geoSpots,
  }
}

const taskStatuses = ['调度中', '执行中', '待命', '已完成'] as const

const buildPilotTasks = (): PilotTaskItem[] =>
  Array.from({ length: 6 }).map(() => {
    const eta = getFutureTime(faker.number.int({ min: 1, max: 6 }) * 30)
    const etd = getFutureTime(faker.number.int({ min: 7, max: 16 }) * 30)
    return {
      id: faker.string.uuid(),
      shipName: `${faker.location.city()}号`,
      pilot: faker.person.fullName(),
      eta: formatDateTime(eta),
      etd: formatDateTime(etd),
      status: faker.helpers.arrayElement(taskStatuses),
    }
  })

const buildMaintenanceLogs = (): MaintenanceLog[] =>
  Array.from({ length: 5 }).map(() => ({
    id: faker.string.uuid(),
    module: faker.helpers.arrayElement(['计费系统', '引航排班', '泊位调度', '航线监控']),
    level: faker.helpers.arrayElement(['warning', 'info', 'critical']),
    message: faker.lorem.sentence(),
    timestamp: formatDateTime(getPastTime(faker.number.int({ min: 5, max: 180 }))),
  }))

export const createOperationsOverview = (): OperationsOverview => {
  const energyTrend = Array.from({ length: 24 }).map((_, idx) => ({
    time: `${String(idx).padStart(2, '0')}:00`,
    value: faker.number.int({ min: 60, max: 180 }),
  }))

  const berthUsage = [
    { name: '空闲', value: faker.number.int({ min: 20, max: 40 }) },
    { name: '占用', value: faker.number.int({ min: 35, max: 55 }) },
    { name: '维护', value: faker.number.int({ min: 5, max: 15 }) },
  ]

  const kpis: OperationsOverview['kpis'] = [
    { label: '调度效率', value: faker.number.float({ min: 92, max: 99, fractionDigits: 2 }), unit: '%', change: faker.number.float({ min: -2, max: 4, fractionDigits: 1 }) },
    { label: '任务准点率', value: faker.number.float({ min: 90, max: 99, fractionDigits: 2 }), unit: '%', change: faker.number.float({ min: -3, max: 5, fractionDigits: 1 }) },
    { label: '能源消耗', value: faker.number.int({ min: 320, max: 520 }), unit: 'MWh', change: faker.number.float({ min: -6, max: 6, fractionDigits: 1 }) },
    { label: '拖轮利用率', value: faker.number.float({ min: 72, max: 93, fractionDigits: 2 }), unit: '%', change: faker.number.float({ min: -4, max: 4, fractionDigits: 1 }) },
  ]

  return {
    kpis,
    pilotTasks: buildPilotTasks(),
    maintenanceLogs: buildMaintenanceLogs(),
    energyTrend,
    berthUsage,
  }
}

const tourismTabs = ['综合概览', '船舶分析', '景区资源', '视频监控', '车辆管理', '信息发布']

const tourismPoints: MapPoint[] = [
  { name: '北山景区', coord: [118.78, 32.07], type: 'scenic', value: 80, detail: '游客密度：高，实时船舶 3.2k 人' },
  { name: '南湾码头', coord: [118.83, 32.02], type: 'scenic', value: 60, detail: '水上项目开放，船舶 1.6k 人' },
  { name: '东门停车场', coord: [118.9, 32.05], type: 'parking', value: 55, detail: '剩余车位 85 / 320' },
  { name: '西岔停车场', coord: [118.74, 32.01], type: 'parking', value: 45, detail: '剩余车位 52 / 280' },
  { name: '云台观景台', coord: [118.81, 31.98], type: 'scenic', value: 50, detail: '观景人数 860，排队 5min' },
  { name: '南部巡检点', coord: [118.86, 31.96], type: 'monitor', value: 35, detail: '重点巡检区域，已巡检 3 次' },
]

const buildTourismMetrics = (tab: string): TourismMetric[] =>
  ['船舶', '车辆', '营收', '投诉'].map((label, idx) => ({
    id: `${tab}-${idx}`,
    label: `今日${label}`,
    value: faker.number.int({ min: 1200, max: 28000 }),
    unit: idx === 2 ? '万元' : idx === 3 ? '件' : '人次',
    subLabel: '同比',
    trend: faker.number.float({ min: -5, max: 16, fractionDigits: 1 }),
  }))

const buildVisitorSources = (): { rank: number; region: string; value: number }[] =>
  Object.keys(provinceCoords)
    .slice(0, 6)
    .map((region, idx) => ({
      rank: idx + 1,
      region,
      value: faker.number.int({ min: 8000, max: 28000 }),
    }))
    .sort((a, b) => b.value - a.value)
    .map((item, idx) => ({ ...item, rank: idx + 1 }))

const buildVehicleTypes = () => [
  { name: '大巴', value: faker.number.int({ min: 40, max: 80 }) },
  { name: '自驾', value: faker.number.int({ min: 120, max: 260 }) },
  { name: '营运', value: faker.number.int({ min: 30, max: 80 }) },
  { name: '物流', value: faker.number.int({ min: 20, max: 70 }) },
]

const buildHourlyTraffic = (): { time: string; value: number }[] =>
  Array.from({ length: 12 }).map((_, idx) => ({
    time: `${idx * 2}:00`,
    value: faker.number.int({ min: 200, max: 1200 }),
  }))

const buildVehicleRecords = (): TourismVehicleRecord[] =>
  Array.from({ length: 7 }).map(() => ({
    plate: `苏${faker.string.alpha({ length: 1, casing: 'upper' })}${faker.string.alphanumeric({ length: 5, casing: 'upper' })}`,
    type: faker.helpers.arrayElement(['大巴', '自驾', '营运', '物流']),
    inTime: formatDateTime(getPastTime(faker.number.int({ min: 20, max: 180 }))),
    outTime: formatDateTime(getPastTime(faker.number.int({ min: 5, max: 60 }))),
    status: faker.helpers.arrayElement(['已离场', '在场']),
  }))

const buildWeatherSnapshot = () => ({
  location: '金山淇河智慧景区',
  temperature: faker.number.int({ min: 18, max: 33 }),
  condition: faker.helpers.arrayElement(['晴', '多云', '小雨']),
  humidity: faker.number.int({ min: 45, max: 90 }),
  visibility: faker.number.int({ min: 5, max: 25 }),
  windSpeed: faker.number.float({ min: 0.8, max: 6.5, fractionDigits: 1 }),
  windDirection: faker.helpers.arrayElement(['东南', '东北', '西南', '正北']),
  pressure: faker.number.int({ min: 990, max: 1022 }),
  pm25: faker.number.int({ min: 15, max: 35 }),
  pm10: faker.number.int({ min: 20, max: 60 }),
  co: faker.number.float({ min: 0.2, max: 0.8, fractionDigits: 2 }),
  o3: faker.number.float({ min: 70, max: 160, fractionDigits: 0 }),
})

const buildVideoFeeds = (): Array<{ id: string; title: string; location: string; status: 'online' | 'offline'; thumbnail: string }> =>
  Array.from({ length: 4 }).map((_, idx) => ({
    id: faker.string.uuid(),
    title: `监控画面 ${idx + 1}`,
    location: faker.location.city(),
    status: faker.datatype.boolean() ? 'online' : 'offline',
    thumbnail: '',
  }))

export const createTourismDataset = (tab: string): TourismDashboardDataset => {
  const mapPoints = tourismPoints.map((point) => ({
    ...point,
    value: point.value + faker.number.int({ min: -10, max: 10 }),
  }))

  return {
    tab,
    metrics: buildTourismMetrics(tab),
    visitorSources: buildVisitorSources(),
    vehicleTypes: buildVehicleTypes(),
    hourlyTraffic: buildHourlyTraffic(),
    vehicleRecords: buildVehicleRecords(),
    weather: buildWeatherSnapshot(),
    mapPoints,
    videoFeeds: buildVideoFeeds(),
  }
}

export const createTourismDatasets = (): TourismDashboardDataset[] => tourismTabs.map((tab) => createTourismDataset(tab))
