import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const baseTextStyle = {
  color: 'var(--tidal-text-secondary)',
  fontSize: 12,
}

export const createLineOption = (xAxis: string[], series: number[], name: string): EChartsOption => ({
  backgroundColor: 'transparent',
  grid: { top: 36, right: 24, left: 48, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: xAxis,
    axisLine: { lineStyle: { color: 'var(--tidal-border)' } },
    axisLabel: baseTextStyle,
  },
  yAxis: {
    type: 'value',
    axisLabel: baseTextStyle,
    splitLine: { lineStyle: { color: 'rgba(0, 105, 148, 0.12)' } },
  },
  series: [
    {
      name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      showSymbol: false,
      lineStyle: {
        color: '#2b6f9a',
        width: 2,
      },
      itemStyle: {
        color: '#4db6e3',
      },
      areaStyle: {
        color: 'rgba(77, 182, 227, 0.18)',
      },
      data: series,
    },
  ],
})

export const createBarOption = (xAxis: string[], series: number[], name: string): EChartsOption => ({
  backgroundColor: 'transparent',
  grid: { top: 32, right: 24, left: 48, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: xAxis,
    axisLabel: baseTextStyle,
    axisLine: { lineStyle: { color: 'var(--tidal-border)' } },
  },
  yAxis: {
    type: 'value',
    axisLabel: baseTextStyle,
    splitLine: { lineStyle: { color: 'rgba(0, 105, 148, 0.12)' } },
  },
  series: [
    {
      name,
      type: 'bar',
      barWidth: '48%',
      data: series,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4db6e3' },
          { offset: 1, color: '#2b6f9a' },
        ]),
        borderRadius: [6, 6, 0, 0],
      },
    },
  ],
})

export const createPieOption = (series: Array<{ name: string; value: number }>, title?: string): EChartsOption => ({
  backgroundColor: 'transparent',
  title: title
    ? {
        text: title,
        textStyle: { color: 'var(--tidal-text-main)', fontSize: 14 },
        left: 'center',
      }
    : undefined,
  tooltip: { trigger: 'item' },
  legend: {
    orient: 'vertical',
    right: 16,
    top: 24,
    textStyle: baseTextStyle,
  },
  series: [
    {
      type: 'pie',
      radius: ['36%', '62%'],
      center: ['38%', '50%'],
      roseType: 'radius',
      itemStyle: {
        borderRadius: 8,
        borderColor: 'var(--tidal-bg-card)',
        borderWidth: 2,
      },
      label: {
        color: 'var(--tidal-text-secondary)',
      },
      data: series,
    },
  ],
})

export const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    'PENDING': '#faad14',      // Orange
    'APPROVED': '#13c2c2',     // Cyan
    'IN_PROGRESS': '#1890ff',  // Blue
    'ONGOING': '#1890ff',      // Blue (Added)
    'COMPLETED': '#52c41a',    // Green
    'CONFIRMED': '#52c41a',    // Green (Added)
    'CERTIFICATED': '#13c2c2', // Cyan (Added)
    'CANCELLED': '#d9d9d9',    // Grey
    'REJECTED': '#ff4d4f'      // Red
  }
  return colors[status] || '#1890ff'
}

const parseDate = (date: string | Date | number | undefined | null): number => {
  if (!date) return 0
  if (date instanceof Date) return date.getTime()
  if (typeof date === 'number') return date
  
  const str = String(date)
  if (str === 'undefined' || str === 'null') return 0
  
  // Handle "YYYY-MM-DD HH:mm:ss" format
  if (str.includes(' ') && !str.includes('T')) {
    return new Date(str.replace(' ', 'T')).getTime()
  }
  return new Date(str).getTime()
}

export const createGanttOption = (
  data: { resourceName: string; tasks: any[]; startDate?: string; endDate?: string }[],
  startTime?: Date,
  endTime?: Date
): EChartsOption => {
  const categories = data.map(d => d.resourceName)
  const seriesData: any[] = []
  const bgSeriesData: any[] = []

  let minTime = startTime ? startTime.getTime() : Number.MAX_VALUE
  let maxTime = endTime ? endTime.getTime() : Number.MIN_VALUE

  data.forEach((resource, index) => {
    if (resource.startDate && resource.endDate) {
      const resStart = parseDate(resource.startDate)
      const resEnd = parseDate(resource.endDate)
      
      if (resStart > 0 && resEnd > 0) {
        if (!startTime) minTime = Math.min(minTime, resStart)
        if (!endTime) maxTime = Math.max(maxTime, resEnd)

        bgSeriesData.push({
          name: resource.resourceName + ' (排班)',
          value: [index, resStart, resEnd, resEnd - resStart, 'AVAILABLE'],
          itemStyle: { color: 'rgba(255, 255, 255, 0.08)' }
        })
      }
    }

    // Tasks
    if (resource.tasks && Array.isArray(resource.tasks)) {
      resource.tasks.forEach(task => {
        const start = parseDate(task.startTime)
        const end = parseDate(task.endTime)
        
        if (start > 0 && end > 0) {
          if (!startTime) minTime = Math.min(minTime, start)
          if (!endTime) maxTime = Math.max(maxTime, end)

          seriesData.push({
            name: task.taskName || '未命名任务',
            value: [index, start, end, end - start, task.status],
            itemStyle: { color: getStatusColor(task.status) }
          })
        }
      })
    }
  })

  if (minTime === Number.MAX_VALUE || minTime === 0) {
    const now = new Date()
    minTime = new Date(now.setHours(0,0,0,0)).getTime()
  }
  if (maxTime === Number.MIN_VALUE || maxTime === 0) {
    const now = new Date()
    maxTime = new Date(now.setHours(23,59,59,999)).getTime()
  }

  if (!startTime || !endTime) {
    const range = maxTime - minTime
    if (range > 0) {
      minTime -= range * 0.02
      maxTime += range * 0.02
    }
  }

  const renderItem = (params: any, api: any) => {
    const categoryIndex = api.value(0)
    if (categoryIndex === undefined || isNaN(categoryIndex)) return undefined

    const start = api.coord([api.value(1), categoryIndex])
    const end = api.coord([api.value(2), categoryIndex])
    
    if (!start || !end || isNaN(start[0]) || isNaN(end[0])) return undefined

    const isBackground = api.value(4) === 'AVAILABLE'
    const heightRatio = isBackground ? 0.8 : 0.6
    
    const size = api.size([0, 1])
    const barHeight = (size && size.length > 1) ? size[1] * heightRatio : 20

    const rectShape = echarts.graphic.clipRectByRect(
      {
        x: start[0],
        y: start[1] - barHeight / 2,
        width: end[0] - start[0],
        height: barHeight,
      },
      {
        x: params.coordSys.x,
        y: params.coordSys.y,
        width: params.coordSys.width,
        height: params.coordSys.height,
      }
    )

    return rectShape && {
      type: 'rect',
      transition: ['shape'],
      shape: rectShape,
      style: api.style(),
    }
  }

  return {
    backgroundColor: 'transparent',
    tooltip: {
      formatter: (params: any) => {
        const v = params.value
        if (!v || v[4] === 'AVAILABLE') return ''
        const startStr = new Date(v[1]).toLocaleString()
        const endStr = new Date(v[2]).toLocaleString()
        return `
          <div style="font-weight:bold">${params.name}</div>
          <div>状态: ${v[4]}</div>
          <div>开始: ${startStr}</div>
          <div>结束: ${endStr}</div>
        `
      }
    },
    grid: {
      top: 40,
      left: 120, // 增加左侧留白给长名称
      right: 30,
      bottom: 60, // 增加底部留白给 DataZoom
      containLabel: true
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: 100,
        height: 20,
        bottom: 10,
        borderColor: 'rgba(143, 214, 255, 0.2)',
        fillerColor: 'rgba(143, 214, 255, 0.1)',
        handleStyle: { color: '#46b2ff' },
        textStyle: { color: '#c8e6ff' },
        brushSelect: false
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        zoomOnMouseWheel: 'ctrl', // 按住 Ctrl 缩放
        moveOnMouseWheel: true,   // 滚轮平移
        moveOnMouseMove: true     // 鼠标拖拽平移
      },
      {
        type: 'slider',
        show: true,
        yAxisIndex: [0],
        left: '96%', // 显示在右侧
        width: 15,
        start: 0,
        end: 100, // 默认显示全部，如果太多会自动缩放
        borderColor: 'rgba(143, 214, 255, 0.2)',
        fillerColor: 'rgba(143, 214, 255, 0.1)',
        handleStyle: { color: '#46b2ff' },
        textStyle: { color: 'transparent' } // 隐藏文字
      },
      {
        type: 'inside',
        yAxisIndex: [0],
        zoomOnMouseWheel: false,
        moveOnMouseWheel: true, // 允许滚轮上下滚动 Y 轴
        moveOnMouseMove: true
      }
    ],
    xAxis: {
      type: 'time',
      min: minTime,
      max: maxTime,
      axisLabel: {
        ...baseTextStyle,
        formatter: (value: number) => {
            const date = new Date(value);
            return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
        }
      },
      splitLine: { show: true, lineStyle: { color: 'rgba(42, 92, 170, 0.2)' } },
      axisLine: { lineStyle: { color: '#1a3a6a' } }
    },
    yAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        ...baseTextStyle,
        width: 90,
        overflow: 'truncate'
      },
      splitLine: { show: true, lineStyle: { color: 'rgba(42, 92, 170, 0.1)' } },
      axisLine: { lineStyle: { color: '#1a3a6a' } }
    },
    animation: false,
    series: [
      {
        name: 'Availability',
        type: 'custom',
        renderItem: renderItem,
        itemStyle: { borderRadius: 2 },
        encode: { x: [1, 2], y: 0 },
        data: bgSeriesData,
        silent: true,
        z: 1
      },
      {
        name: 'Tasks',
        type: 'custom',
        renderItem: renderItem,
        itemStyle: {
          opacity: 0.9,
          borderRadius: 4,
          shadowBlur: 2,
          shadowColor: 'rgba(0,0,0,0.3)'
        },
        encode: { x: [1, 2], y: 0, tooltip: [1, 2] },
        data: seriesData,
        z: 2
      }
    ]
  }
}

