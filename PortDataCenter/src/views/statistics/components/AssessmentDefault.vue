<template>
  <el-table-column prop="shipName" label="中文船名" width="120" />
  <el-table-column prop="englishName" label="英文船名" width="140" show-overflow-tooltip />
  <el-table-column prop="nationality" label="国籍" width="80" />
  <el-table-column prop="routeType" label="航线类型" width="80" v-if="showRouteType" />
  <el-table-column prop="dynamic" label="动态" width="60" :formatter="formatDynamic" />

  <el-table-column v-if="showStartEndTimes" prop="startTime" label="开始时间" width="140" />
  <el-table-column v-if="showStartEndTimes" prop="endTime" label="结束时间" width="140" />
  <el-table-column v-else prop="timeDate" label="时间/日期" width="140" />

  <el-table-column prop="grossTonnage" label="总吨" width="80" />
  <el-table-column prop="netTonnage" label="净吨" width="80" />
  <el-table-column prop="deadWeight" label="载重吨" width="80" />
  <el-table-column prop="length" label="船长" width="70" />
  <el-table-column prop="width" label="船宽" width="70" />
  <el-table-column prop="draft" label="吃水" width="60" />
  <el-table-column prop="berth" label="泊位" width="100" show-overflow-tooltip />
  <el-table-column prop="boardingTime" label="登离时间" width="120" />
  <el-table-column prop="boardingPlace" label="登离地点" width="100" show-overflow-tooltip />
  <el-table-column prop="tug" label="拖轮" width="80" />
  <el-table-column prop="pilotName" label="引航员" width="70" />
  <!-- subPilot 字段已移除，因为现在使用 pilotName 数组 -->

  <el-table-column v-if="showPickDropMachine" prop="pickDropMachine" label="接送机" width="80" />
  <el-table-column v-if="showSendMachine" prop="sendMachine" label="送班机" width="80" />

  <el-table-column prop="no" label="编号" width="100" v-if="showNo" />
  <el-table-column prop="agent" label="代理" width="100" show-overflow-tooltip />
  <el-table-column prop="pickDropTime" label="接送时间" width="120" v-if="showPickDropTime" />
  <el-table-column prop="height" label="净高" width="60" v-if="showHeight" />
  <el-table-column prop="foreignFee" label="外贸费" width="80" v-if="showForeignFee" />
  <el-table-column prop="remarks" label="备注" min-width="120" show-overflow-tooltip />
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

interface Props {
  showRouteType?: boolean
  showStartEndTimes?: boolean
  showNo?: boolean
  showPickDropTime?: boolean
  showHeight?: boolean
  showForeignFee?: boolean
  showPickDropMachine?: boolean
  showSendMachine?: boolean
}

withDefaults(defineProps<Props>(), {
  showRouteType: false,
  showStartEndTimes: false,
  showNo: false,
  showPickDropTime: false,
  showHeight: false,
  showForeignFee: false,
  showPickDropMachine: false,
  showSendMachine: false
})

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