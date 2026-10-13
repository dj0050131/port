<template>
  <el-table-column prop="shipName" label="船名" width="120" />
  <el-table-column prop="date" label="日期" width="100" />
  <el-table-column prop="dynamicTime" label="动态时间" width="100" />
  <el-table-column prop="sailTime" label="开航时间" width="100" />
  <el-table-column prop="berthTime" label="停靠时间" width="100" />
  <el-table-column prop="pilotName" label="引航员" width="80" />
  <el-table-column prop="towedShip" label="被引船舶" width="120" />
  <el-table-column prop="dynamic" label="动态" width="80" :formatter="formatDynamic" />
  <el-table-column prop="waitTime" label="等待时间" />
</template>

<script setup lang="ts">
// 船务安全科航行(月份) 表格列组件

const formatDynamic = (row: any, column: any, cellValue: string) => {
  switch (cellValue) {
    case 'IMPORT':
      return '进港'
    case 'EXPORT':
      return '出港'
    case 'TRANSIT':
      return '移泊'
    default:
      return cellValue
  }
}
</script>