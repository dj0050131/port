<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Printer, Download } from '@element-plus/icons-vue'
import { getAnnualReport } from '@/api/statistics'
import type { AnnualReportDTO } from '@/types/statistics'
import { useDataCenterStore } from '@/stores/dataCenter'

const dataCenterStore = useDataCenterStore()
const tableState = dataCenterStore.ensureTable<AnnualReportDTO>('statistics.billingAnnual', {
  items: [],
  total: 0,
  page: 1,
  pageSize: 20,
  loading: false,
})

const searchForm = reactive({
  year: new Date().getFullYear()
})

const fetchData = async () => {
  tableState.loading = true
  try {
    const res = await getAnnualReport(searchForm.year)
    if (res.code === 200) {
      tableState.items = res.data.monthlyStatistics || []
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
  const header = [
    '月份', '计费凭证数', '月度应收总额', '月度实收总额', '月度欠款额',
    '年度累计应收', '年度累计实收', '年度累计欠款'
  ]
  const rows = tableState.items.map((r: any) => ([
    r.month, r.billingVoucherCount, r.totalAmount, r.actualReceivedAmount, r.arrears,
    r.cumulativeTotalAmount, r.cumulativeActualReceivedAmount, r.cumulativeArrears
  ]))
  downloadCsv(`费收年报表统计_${searchForm.year}.csv`, header, rows)
  ElMessage.success('导出成功')
}

const handlePrint = () => {
  if (!tableState.items || tableState.items.length === 0) {
    ElMessage.warning('当前没有可打印的数据')
    return
  }
  const body = `
    <h1>费收年报表统计</h1>
    <div class="meta">年份：${searchForm.year}　|　打印时间：${new Date().toLocaleString()}</div>
    <table>
      <thead>
        <tr>
          <th>月份</th>
          <th>计费凭证数</th>
          <th class="right">月度应收总额</th>
          <th class="right">月度实收总额</th>
          <th class="right">月度欠款额</th>
          <th class="right">年度累计应收</th>
          <th class="right">年度累计实收</th>
          <th class="right">年度累计欠款</th>
        </tr>
      </thead>
      <tbody>
        ${tableState.items.map((r: any) => `
          <tr>
            <td>${r.month ?? '-'}</td>
            <td>${r.billingVoucherCount ?? '-'}</td>
            <td class="right">${r.totalAmount ?? '-'}</td>
            <td class="right">${r.actualReceivedAmount ?? '-'}</td>
            <td class="right">${r.arrears ?? '-'}</td>
            <td class="right">${r.cumulativeTotalAmount ?? '-'}</td>
            <td class="right">${r.cumulativeActualReceivedAmount ?? '-'}</td>
            <td class="right">${r.cumulativeArrears ?? '-'}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `
  openPrintWindow('费收年报表统计', body)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="statistics-annual-view page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">费收年报表统计</h2>
        <p class="page-desc">引航费收统计报表</p>
      </div>
    </div>

    <div class="main-content glass-card">
      <div class="toolbar">
        <el-form inline size="small">
          <el-form-item label="年份">
            <el-date-picker 
              v-model="searchForm.year" 
              type="year" 
              placeholder="选择年份" 
              format="YYYY"
              value-format="YYYY"
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
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="billingVoucherCount" label="计费凭证数" width="120" />
        <el-table-column prop="totalAmount" label="月度应收总额" width="120" />
        <el-table-column prop="actualReceivedAmount" label="月度实收总额" width="120" />
        <el-table-column prop="arrears" label="月度欠款额" width="120" />
        <el-table-column prop="cumulativeTotalAmount" label="年度累计应收" width="120" />
        <el-table-column prop="cumulativeActualReceivedAmount" label="年度累计实收" width="120" />
        <el-table-column prop="cumulativeArrears" label="年度累计欠款" width="120" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.statistics-annual-view {
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