<template>
  <el-table-column prop="agent" label="代理" width="100" show-overflow-tooltip />
  <el-table-column prop="shipName" label="中文船名" width="120" />
  <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
  <el-table-column prop="status" label="状态" width="80" />
  <el-table-column prop="reason" label="原因" width="120" show-overflow-tooltip />
  <el-table-column prop="nationality" label="国籍" width="80" />
  <el-table-column prop="dynamic" label="动态" width="60" :formatter="formatDynamic" />
  <el-table-column prop="timeDate" label="时间/日期" width="140" />
  <el-table-column prop="grossTonnage" label="总吨" width="80" />
  <el-table-column prop="pilotName" label="引航员" width="70" />
  <el-table-column prop="remarks" label="备注" min-width="120" />
</template>

<script setup lang="ts">

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