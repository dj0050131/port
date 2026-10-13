<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Printer, Download } from '@element-plus/icons-vue'
import { getMonthlyReport } from '@/api/statistics'
import type { MonthlyReportDTO } from '@/types/statistics'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<MonthlyReportDTO>('statistics.billingMonthly', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  yearMonth: ''
})

const currentDate = new Date()
searchForm.yearMonth = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`

const fetchData = async () => {
  if (!searchForm.yearMonth) {
    ElMessage.warning('请选择年月')
    return
  }

  tableState.loading = true
  try {
    const res = await getMonthlyReport(searchForm.yearMonth)
    if (res.code === 200) {
      tableState.items = [res.data]
      ElMessage.success('数据获取成功')
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('获取数据失败: ' + (error as Error).message)
  } finally {
    tableState.loading = false
  }
}

const safeCsv = (val: any) => {
  const s = val === null || val === undefined ? '' : String(val)
  const escaped = s.replace(/"/g, '""')
  // 包含逗号/换行/引号时需要用引号包裹
  if (/[",\r\n]/.test(escaped)) return `"${escaped}"`
  return escaped
}

const downloadCsv = (filename: string, header: string[], rows: any[][]) => {
  const csv = [header.map(safeCsv).join(','), ...rows.map(r => r.map(safeCsv).join(','))].join('\r\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

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
  <title>${title}</title>
  <style>
    body { font-family: -apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",Arial,sans-serif; padding: 24px; }
    h1 { font-size: 18px; margin: 0 0 8px; }
    .meta { color: #666; font-size: 12px; margin-bottom: 16px; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; }
    th { background: #f6f7f9; }
    .right { text-align: right; }
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

const handleExport = () => {
  if (!tableState.items || tableState.items.length === 0) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const r = tableState.items[0] as any
  const header = [
    '年份', '月份', '计费凭证数', '船舶数', '代理公司数',
    '应收总额', '实收总额', '折扣总额', '滞纳金总额',
    '未付款单数', '部分付款单数', '已付款单数',
    '国际航线金额', '国内航线金额'
  ]
  const rows = [[
    r.year, r.month, r.billingVoucherCount, r.vesselCount, r.agentCount,
    r.totalAmount, r.actualReceivedAmount, r.discountAmount, r.lateFee,
    r.unpaidCount, r.partialPaidCount, r.fullyPaidCount,
    r.internationalRouteAmount, r.domesticRouteAmount
  ]]
  downloadCsv(`费收月报表统计_${searchForm.yearMonth}.csv`, header, rows)
  ElMessage.success('导出成功')
}

const handlePrint = () => {
  if (!tableState.items || tableState.items.length === 0) {
    ElMessage.warning('当前没有可打印的数据')
    return
  }
  const r = tableState.items[0] as any
  const body = `
    <h1>费收月报表统计</h1>
    <div class="meta">年月：${searchForm.yearMonth}　|　打印时间：${new Date().toLocaleString()}</div>
    <table>
      <thead>
        <tr>
          <th>年份</th><th>月份</th><th>计费凭证数</th><th>船舶数</th><th>代理公司数</th>
          <th class="right">应收总额</th><th class="right">实收总额</th><th class="right">折扣总额</th><th class="right">滞纳金总额</th>
          <th>未付款单数</th><th>部分付款单数</th><th>已付款单数</th>
          <th class="right">国际航线金额</th><th class="right">国内航线金额</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>${r.year ?? '-'}</td><td>${r.month ?? '-'}</td><td>${r.billingVoucherCount ?? '-'}</td><td>${r.vesselCount ?? '-'}</td><td>${r.agentCount ?? '-'}</td>
          <td class="right">${r.totalAmount ?? '-'}</td><td class="right">${r.actualReceivedAmount ?? '-'}</td><td class="right">${r.discountAmount ?? '-'}</td><td class="right">${r.lateFee ?? '-'}</td>
          <td>${r.unpaidCount ?? '-'}</td><td>${r.partialPaidCount ?? '-'}</td><td>${r.fullyPaidCount ?? '-'}</td>
          <td class="right">${r.internationalRouteAmount ?? '-'}</td><td class="right">${r.domesticRouteAmount ?? '-'}</td>
        </tr>
      </tbody>
    </table>
  `
  openPrintWindow('费收月报表统计', body)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="statistics-monthly-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">费收月报表统计</h2>
        <p class="page-desc">引航费收统计报表</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-form inline size="small">
          <el-form-item label="年月">
            <el-date-picker 
              v-model="searchForm.yearMonth" 
              type="month" 
              placeholder="选择年月" 
              format="YYYY-MM"
              value-format="YYYY-MM"
              style="width: 120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchData" :loading="tableState.loading">查询</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button :icon="Printer" @click="handlePrint">打印</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="tableState.items" v-loading="tableState.loading" stripe height="100%" size="small">
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="billingVoucherCount" label="计费凭证数" width="120" />
        <el-table-column prop="vesselCount" label="船舶数" width="100" />
        <el-table-column prop="agentCount" label="代理公司数" width="120" />
        <el-table-column prop="totalAmount" label="应收总额" width="120" />
        <el-table-column prop="actualReceivedAmount" label="实收总额" width="120" />
        <el-table-column prop="discountAmount" label="折扣总额" width="120" />
        <el-table-column prop="lateFee" label="滞纳金总额" width="120" />
        <el-table-column prop="unpaidCount" label="未付款单数" width="120" />
        <el-table-column prop="partialPaidCount" label="部分付款单数" width="120" />
        <el-table-column prop="fullyPaidCount" label="已付款单数" width="120" />
        <el-table-column prop="internationalRouteAmount" label="国际航线金额" width="120" />
        <el-table-column prop="domesticRouteAmount" label="国内航线金额" width="120" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.statistics-monthly-view {
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