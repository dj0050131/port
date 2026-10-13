<template>
  <div class="base-chart" ref="chartRef"></div>
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue';
import { useChart } from '@/hooks/useChart';
import type { EChartsOption } from 'echarts';

const props = defineProps<{
  option: EChartsOption;
}>();

const { chartRef, setOption } = useChart();

onMounted(() => {
  if (props.option) {
    setOption(props.option);
  }
});

watch(
  () => props.option,
  option => {
    if (option) {
      setOption(option);
    }
  },
  { deep: true }
);
</script>

<style scoped>
.base-chart {
  width: 100%;
  height: 100%;
}
</style>

