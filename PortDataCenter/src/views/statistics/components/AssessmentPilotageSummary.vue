<template>
  <el-table-column prop="month" label="月份" width="80" />
  <el-table-column prop="foreign" label="外轮" />
  <el-table-column prop="domestic" label="国轮" />
  <el-table-column prop="gt250" label=">250m" />
  <el-table-column prop="gt200_250" label="200m-250m" width="100" />
  <el-table-column prop="netTonnage" label="净吨" />
  <el-table-column prop="grossTonnage" label="总吨" />
  <el-table-column prop="deadWeight" label="载重吨" />
  <el-table-column prop="dangerous" label="危险品船" />
  <el-table-column prop="container" label="集装箱船" />
  <el-table-column prop="night" label="夜航" />
  <el-table-column prop="restDay" label="休息日" />
  <el-table-column prop="holiday" label="法定节假日" width="100" />
  <el-table-column prop="distance" label="引航距离" />
  <el-table-column prop="time" label="引航时间" />
  <el-table-column prop="remarks" label="备注" min-width="150" />
</template>

<script setup lang="ts">
</script>