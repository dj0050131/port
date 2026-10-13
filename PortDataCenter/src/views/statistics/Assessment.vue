<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Printer } from '@element-plus/icons-vue'
import { useDataCenterStore } from '@/stores/dataCenter'
import {
  fetchAssessmentSafety,
  fetchAssessmentPilotageMonthly,
  fetchAssessmentPilotageSummary,
  fetchAssessmentLengthGt250,
  fetchAssessmentHoliday,
  fetchAssessmentWeekend,
  fetchAssessmentNight,
  fetchAssessmentSpecial,
  fetchAssessmentRecordBreak,
  fetchAssessmentNewWharf,
  fetchAssessmentPlanChange, fetchAssessmentMeal
} from '@/api/assement'

import AssessmentSafetyMonthly from './components/AssessmentSafetyMonthly.vue'
import AssessmentPilotageSummary from './components/AssessmentPilotageSummary.vue'
import AssessmentSpecial from './components/AssessmentSpecial.vue'
import AssessmentRecordBreak from './components/AssessmentRecordBreak.vue'
import AssessmentPlanChange from './components/AssessmentPlanChange.vue'
import AssessmentDefault from './components/AssessmentDefault.vue'

const route = useRoute()
const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<any>('statistics.assessment', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})
const selectedRows = ref<any[]>([])

const searchForm = reactive({
  dateRange: [],
  shipName: '',
  pilot: '',
  minLength: 250
})

const dataMap: Record<string, any> = {
  'AssessmentSafetyMonthly': { api: fetchAssessmentSafety, title: '船务安全科航行(月份)' },
  'AssessmentPilotageMonthly': { api: fetchAssessmentPilotageMonthly, title: '船舶引航统计(月份)' },
  'AssessmentPilotageSummary': { api: fetchAssessmentPilotageSummary, title: '船舶引航统计(汇总)' },
  'AssessmentLengthGt250': { api: fetchAssessmentLengthGt250, title: '船长统计(>250m)' },
  'AssessmentHoliday': { api: fetchAssessmentHoliday, title: '节假日引航统计' },
  'AssessmentWeekend': { api: fetchAssessmentWeekend, title: '双休日引航统计' },
  'AssessmentNight': { api: fetchAssessmentNight, title: '夜班统计' },
  'AssessmentMeal': { api: fetchAssessmentMeal, title: '餐补统计' },
  'AssessmentSpecial': { api: fetchAssessmentSpecial, title: '特殊引航统计' },
  'AssessmentRecordBreak': { api: fetchAssessmentRecordBreak, title: '破纪录统计' },
  'AssessmentNewWharf': { api: fetchAssessmentNewWharf, title: '新码头靠泊统计' },
  'AssessmentPlanChange': { api: fetchAssessmentPlanChange, title: '计划改变查询' }
}

const currentConfig = computed(() => dataMap[route.name as string] || { api: null, title: '' })
const currentRouteName = computed(() => route.name as string)

const fetchData = async () => {
  tableState.loading = true
  try {
    const params: any = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = (searchForm.dateRange as any[])[0]
      params.endDate = (searchForm.dateRange as any[])[1]
    }
    if (searchForm.shipName) {
      params.shipName = searchForm.shipName
    }
    if (searchForm.pilot) {
      params.pilot = searchForm.pilot
    }
    if (currentRouteName.value === 'AssessmentLengthGt250') {
      params.minLength = searchForm.minLength
    }

    const apiFunction = currentConfig.value.api
    if (apiFunction) {
      const response = await apiFunction(params)
      if (response.code === 200) {
        const processedData = response.data?.map(item => {
          if (item.pilotName && Array.isArray(item.pilotName)) {
            return {
              ...item,
              pilotName: item.pilotName.join(', ') // 将数组转换为逗号分隔的字符串
            }
          }
          return item
        }) || []
        tableState.items = processedData
      } else {
        console.error('API Error:', response.message)
        tableState.items = []
      }
    }
  } catch (error) {
    console.error('Error fetching data:', error)
    tableState.items = []
  } finally {
    tableState.loading = false
  }
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows || []
}

const escapeHtml = (s: string) =>
  s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')

const openPrintWindow = (title: string, htmlBody: string) => {
  const w = window.open('', '_blank')
  if (!w) {
    ElMessage.error('弹窗被浏览器拦截，请允许弹窗后重试')
    return
  }
  w.document.open()
  w.document.write(`
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <title>${escapeHtml(title)}</title>
  <style>
    body { font-family: -apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",Arial,sans-serif; padding: 24px; }
    h1 { font-size: 18px; margin: 0 0 8px; }
    .meta { color: #666; font-size: 12px; margin-bottom: 16px; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; vertical-align: top; }
    th { background: #f6f7f9; }
    @media print { .no-print { display: none; } body { padding: 0; } }
  </style>
</head>
<body>
  <div class="no-print" style="margin-bottom:12px;">
    <button onclick="window.print()">打印</button>
  </div>
  ${htmlBody}
</body>
</html>
  `)
  w.document.close()
}

type Col = { label: string; prop?: string; get?: (row: any) => any }

const getColumnsForRoute = (): Col[] => {
  const name = currentRouteName.value

  if (name === 'AssessmentSafetyMonthly') {
    return [
      { label: '船名', prop: 'shipName' },
      { label: '日期', prop: 'date' },
      { label: '动态时间', prop: 'dynamicTime' },
      { label: '开航时间', prop: 'sailTime' },
      { label: '停靠时间', prop: 'berthTime' },
      { label: '引航员', prop: 'pilotName' },
      { label: '被引船舶', prop: 'towedShip' },
      { label: '动态', prop: 'dynamic' },
      { label: '等待时间', prop: 'waitTime' }
    ]
  }

  if (name === 'AssessmentPilotageSummary') {
    return [
      { label: '月份', prop: 'month' },
      { label: '外轮', prop: 'foreign' },
      { label: '国轮', prop: 'domestic' },
      { label: '>250m', prop: 'gt250' },
      { label: '200m-250m', prop: 'gt200_250' },
      { label: '净吨', prop: 'netTonnage' },
      { label: '总吨', prop: 'grossTonnage' },
      { label: '载重吨', prop: 'deadWeight' },
      { label: '危险品船', prop: 'dangerous' },
      { label: '集装箱船', prop: 'container' },
      { label: '夜航', prop: 'night' },
      { label: '休息日', prop: 'restDay' },
      { label: '法定节假日', prop: 'holiday' },
      { label: '引航距离', prop: 'distance' },
      { label: '引航时间', prop: 'time' },
      { label: '备注', prop: 'remarks' }
    ]
  }

  if (name === 'AssessmentSpecial') {
    return [
      { label: '中文船名', prop: 'shipName' },
      { label: '英文船名', prop: 'englishName' },
      { label: '特殊作业', prop: 'specialOp' },
      { label: '特殊说明', prop: 'specialDesc' },
      { label: '国籍', prop: 'nationality' },
      { label: '动态', prop: 'dynamic' },
      { label: '开始时间', prop: 'startTime' },
      { label: '结束时间', prop: 'endTime' },
      { label: '总吨', prop: 'grossTonnage' },
      { label: '净吨', prop: 'netTonnage' },
      { label: '引航员', prop: 'pilotName' },
      { label: '外贸费', prop: 'foreignFee' },
      { label: '备注', prop: 'remarks' }
    ]
  }

  if (name === 'AssessmentRecordBreak') {
    return [
      { label: '说明', prop: 'desc' },
      { label: '中文船名', prop: 'shipName' },
      { label: '英文船名', prop: 'englishName' },
      { label: '国籍', prop: 'nationality' },
      { label: '动态', prop: 'dynamic' },
      { label: '时间/日期', prop: 'timeDate' },
      { label: '总吨', prop: 'grossTonnage' },
      { label: '船长', prop: 'length' },
      { label: '吃水', prop: 'draft' },
      { label: '泊位', prop: 'berth' },
      { label: '引航员', prop: 'pilotName' }
    ]
  }

  if (name === 'AssessmentPlanChange') {
    return [
      { label: '代理', prop: 'agent' },
      { label: '中文船名', prop: 'shipName' },
      { label: '英文船名', prop: 'englishName' },
      { label: '状态', prop: 'status' },
      { label: '原因', prop: 'reason' },
      { label: '国籍', prop: 'nationality' },
      { label: '动态', prop: 'dynamic' },
      { label: '时间/日期', prop: 'timeDate' },
      { label: '总吨', prop: 'grossTonnage' },
      { label: '引航员', prop: 'pilotName' },
      { label: '备注', prop: 'remarks' }
    ]
  }

  const cols: Col[] = [
    { label: '中文船名', prop: 'shipName' },
    { label: '英文船名', prop: 'englishName' },
    { label: '国籍', prop: 'nationality' }
  ]
  if ((defaultTableProps.value as any).showRouteType) cols.push({ label: '航线类型', prop: 'routeType' })
  cols.push({ label: '动态', prop: 'dynamic' })

  if ((defaultTableProps.value as any).showStartEndTimes) {
    cols.push({ label: '开始时间', prop: 'startTime' })
    cols.push({ label: '结束时间', prop: 'endTime' })
  } else {
    cols.push({ label: '时间/日期', prop: 'timeDate' })
  }

  cols.push({ label: '总吨', prop: 'grossTonnage' })
  cols.push({ label: '净吨', prop: 'netTonnage' })
  cols.push({ label: '载重吨', prop: 'deadWeight' })
  cols.push({ label: '船长', prop: 'length' })
  cols.push({ label: '船宽', prop: 'width' })
  cols.push({ label: '吃水', prop: 'draft' })
  cols.push({ label: '泊位', prop: 'berth' })
  cols.push({ label: '登离时间', prop: 'boardingTime' })
  cols.push({ label: '登离地点', prop: 'boardingPlace' })
  cols.push({ label: '拖轮', prop: 'tug' })
  cols.push({ label: '引航员', prop: 'pilotName' })

  if ((defaultTableProps.value as any).showPickDropMachine) cols.push({ label: '接送机', prop: 'pickDropMachine' })
  if ((defaultTableProps.value as any).showSendMachine) cols.push({ label: '送班机', prop: 'sendMachine' })
  if ((defaultTableProps.value as any).showNo) cols.push({ label: '编号', prop: 'no' })
  cols.push({ label: '代理', prop: 'agent' })
  if ((defaultTableProps.value as any).showPickDropTime) cols.push({ label: '接送时间', prop: 'pickDropTime' })
  if ((defaultTableProps.value as any).showHeight) cols.push({ label: '净高', prop: 'height' })
  if ((defaultTableProps.value as any).showForeignFee) cols.push({ label: '外贸费', prop: 'foreignFee' })
  cols.push({ label: '备注', prop: 'remarks' })

  return cols
}

const formatCell = (v: any) => {
  if (v === null || v === undefined || v === '') return '-'
  if (Array.isArray(v)) return v.join(', ')
  return String(v)
}

const handlePrint = () => {
  const rows = selectedRows.value.length > 0 ? selectedRows.value : tableState.items
  if (!rows || rows.length === 0) {
    ElMessage.warning('当前没有可打印的数据')
    return
  }
  const cols = getColumnsForRoute()
  const dateText = (searchForm.dateRange && (searchForm.dateRange as any[]).length === 2)
    ? `${(searchForm.dateRange as any[])[0]} ~ ${(searchForm.dateRange as any[])[1]}`
    : '全部'

  const body = `
    <h1>${escapeHtml(currentConfig.value.title || '考核统计')}</h1>
    <div class="meta">日期范围：${escapeHtml(dateText)}　|　条数：${rows.length}　|　打印时间：${new Date().toLocaleString()}</div>
    <table>
      <thead>
        <tr>
          ${cols.map(c => `<th>${escapeHtml(c.label)}</th>`).join('')}
        </tr>
      </thead>
      <tbody>
        ${rows.map(r => `
          <tr>
            ${cols.map(c => {
              const raw = c.get ? c.get(r) : (c.prop ? (r as any)[c.prop] : '')
              return `<td>${escapeHtml(formatCell(raw))}</td>`
            }).join('')}
          </tr>
        `).join('')}
      </tbody>
    </table>
  `
  openPrintWindow(currentConfig.value.title || '考核统计', body)
}

watch(() => route.name, () => {
  fetchData()
}, { immediate: true })

onMounted(() => {
  fetchData()
})

const defaultTableProps = computed(() => {
  switch (currentRouteName.value) {
    case 'AssessmentPilotageMonthly':
      return {
        showRouteType: true,
        showStartEndTimes: true,
        showNo: true,
        showPickDropTime: true,
        showHeight: true,
        showForeignFee: true
      }
    case 'AssessmentNight':
      return {
        showPickDropMachine: true,
        showSendMachine: true
      }
    default:
      return {}
  }
})
</script>

<template>
  <div class="assessment-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ currentConfig.title }}</h2>
        <p class="page-desc">引航业务各项指标考核与统计报表</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-form inline size="small">
          <el-form-item label="日期范围">
            <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" />
          </el-form-item>
          <el-form-item v-if="currentRouteName === 'AssessmentLengthGt250'" label="最小船长">
            <el-input-number v-model="searchForm.minLength" :min="0" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
            <el-button :icon="Printer" @click="handlePrint">打印</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" />

        <template v-if="currentRouteName === 'AssessmentSafetyMonthly'">
          <AssessmentSafetyMonthly />
        </template>

        <!-- Summary -->
        <template v-else-if="currentRouteName === 'AssessmentPilotageSummary'">
          <AssessmentPilotageSummary />
        </template>

        <!-- Special -->
        <template v-else-if="currentRouteName === 'AssessmentSpecial'">
          <AssessmentSpecial />
        </template>

        <!-- Record Break -->
        <template v-else-if="currentRouteName === 'AssessmentRecordBreak'">
          <AssessmentRecordBreak />
        </template>

        <!-- Plan Change -->
        <template v-else-if="currentRouteName === 'AssessmentPlanChange'">
          <AssessmentPlanChange />
        </template>

        <!-- Default / Pilotage Monthly / Length > 250 / Holiday / Weekend / Night / New Wharf -->
        <template v-else>
          <AssessmentDefault v-bind="defaultTableProps" />
        </template>

      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.assessment-view {
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

.main-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar { margin-bottom: 12px; }
</style>