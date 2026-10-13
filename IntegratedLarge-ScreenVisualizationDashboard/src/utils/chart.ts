import type { EChartsOption } from 'echarts';

export const getChartBaseOption = () => ({
  tooltip: {
    trigger: 'axis' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    borderColor: 'rgba(255, 255, 255, 0.3)',
    textStyle: { color: '#fff' }
  },
  grid: {
    left: '8%',
    right: '4%',
    top: '15%',
    bottom: '10%',
    containLabel: true
  },
  xAxis: {
    axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.3)' } },
    axisLabel: { color: '#fff' }
  },
  yAxis: {
    axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.3)' } },
    axisLabel: { color: '#fff' },
    splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
  },
  series: []
}) as EChartsOption;

export const chartGradients = {
  blue: {
    type: 'linear' as const,
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color: '#38bdf8' },
      { offset: 1, color: '#1e40af' }
    ]
  },
  purple: {
    type: 'linear' as const,
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color: '#c4b5fd' },
      { offset: 1, color: '#7c3aed' }
    ]
  }
};
