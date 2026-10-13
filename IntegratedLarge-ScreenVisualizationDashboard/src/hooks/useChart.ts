import { onMounted, onUnmounted, ref } from 'vue';
import * as echarts from 'echarts';

export const useChart = () => {
  const chartRef = ref<HTMLDivElement | null>(null);
  const chartInstance = ref<echarts.ECharts | null>(null);

  const initChart = () => {
    if (!chartRef.value) return;
    chartInstance.value = echarts.init(chartRef.value);
    window.addEventListener('resize', resize);
  };

  const setOption = (option: echarts.EChartsOption) => {
    chartInstance.value?.setOption(option);
  };

  const resize = () => {
    chartInstance.value?.resize();
  };

  onMounted(() => {
    initChart();
  });

  onUnmounted(() => {
    window.removeEventListener('resize', resize);
    chartInstance.value?.dispose();
  });

  return {
    chartRef,
    setOption,
    chartInstance
  };
};

