<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ResourceTimeline, ResourceTask } from '@/types/dashboard'

const props = defineProps<{
  resources: ResourceTimeline[]
  date: Date // 当前展示的日期，用于确定时间轴基准
  loading?: boolean
  editable?: boolean
}>()

const emit = defineEmits<{
  (e: 'update-task', payload: { planId: number, newStartTime: string, newEndTime: string }): void
}>()

const HOUR_WIDTH = 100 // 这里的宽度决定了横向滚动的长度
const TOTAL_HOURS = 24
const TOTAL_WIDTH = HOUR_WIDTH * TOTAL_HOURS
const SNAP_MINUTES = 15 // 吸附间隔 (分钟)
const SNAP_MS = SNAP_MINUTES * 60 * 1000

const timelineRef = ref<HTMLElement>()
const chartRef = ref<HTMLElement>()

const formatType = (type: string) => {
  const map: Record<string, string> = {
    'PILOT': '引航员',
    'TUGBOAT': '拖轮',
    'PILOT_BOAT': '引航艇',
    'DRIVER': '驾驶员',
    'VEHICLE': '车辆'
  }
  return map[type] || type
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const normalizeStatusClass = (raw?: string) => {
  return String(raw || '')
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, '_')
}

const dayStart = computed(() => {
  const d = new Date(props.date)
  d.setHours(0, 0, 0, 0)
  return d.getTime()
})

const dayEnd = computed(() => {
  const d = new Date(props.date)
  d.setHours(23, 59, 59, 999)
  return d.getTime()
})

const totalDuration = computed(() => dayEnd.value - dayStart.value)

const timeToPx = (timeStr: string | number) => {
  const time = new Date(timeStr).getTime()
  // 限制在当天范围内，超出部分截断或不做处理（这里简化处理，假设任务在当天）
  const relativeTime = Math.max(0, Math.min(time - dayStart.value, totalDuration.value))
  return (relativeTime / totalDuration.value) * TOTAL_WIDTH
}

const pxToMs = (px: number) => {
  return (px / TOTAL_WIDTH) * totalDuration.value
}

const getTaskStyle = (task: ResourceTask) => {
  // 如果正在拖动，使用临时位置
  if (draggingTask.value && draggingTask.value.taskId === task.taskId) {
    return {
      left: `${dragState.value.currentLeft}px`,
      width: `${dragState.value.width}px`,
      zIndex: 100
    }
  }

  const left = timeToPx(task.startTime)
  const right = timeToPx(task.endTime)
  return {
    left: `${left}px`,
    width: `${Math.max(right - left, 4)}px` // 最小宽度
  }
}

const getAvailabilityStyle = (start?: string, end?: string) => {
  if (!start || !end) return { display: 'none' }
  const left = timeToPx(start)
  const right = timeToPx(end)
  return {
    left: `${left}px`,
    width: `${Math.max(right - left, 0)}px`
  }
}

const draggingTask = ref<ResourceTask | null>(null)
const dragState = ref({
  startX: 0,
  startLeft: 0,
  currentLeft: 0,
  width: 0,
  originalStartTime: 0,
  originalEndTime: 0,
  previewStartTimeStr: '',
  previewEndTimeStr: '',
  mode: 'move' as 'move' | 'resize-left' | 'resize-right'
})

const sidebarBodyRef = ref<HTMLElement>()

const syncScroll = () => {
  if (timelineRef.value && sidebarBodyRef.value) {
    sidebarBodyRef.value.scrollTop = timelineRef.value.scrollTop
  }
}

const isShortTask = (task: ResourceTask) => {
  const start = new Date(task.startTime).getTime()
  const end = new Date(task.endTime).getTime()
  const hours = (end - start) / (1000 * 60 * 60)
  return hours * HOUR_WIDTH < 60
}

const startDrag = (e: MouseEvent, task: ResourceTask) => {
  if (props.loading) return
  if (props.editable === false) return
  // 仅允许左键拖动
  if (e.button !== 0) return

  const target = e.target as HTMLElement
  const el = target.closest('.gantt-bar') as HTMLElement
  if (!el) return

  let mode: 'move' | 'resize-left' | 'resize-right' = 'move'
  if (target.classList.contains('resize-handle')) {
    if (target.classList.contains('handle-left')) mode = 'resize-left'
    else if (target.classList.contains('handle-right')) mode = 'resize-right'
  }

  draggingTask.value = task
  const rect = el.getBoundingClientRect()
  const parentRect = timelineRef.value!.getBoundingClientRect()
  
  // 我们统一用 timeToPx 计算初始 left，避免 DOM 读取偏差

  dragState.value = {
    startX: e.clientX,
    startLeft: initialLeft,
    currentLeft: initialLeft,
    width: initialWidth,
    originalStartTime: new Date(task.startTime).getTime(),
    originalEndTime: new Date(task.endTime).getTime(),
    previewStartTimeStr: formatTime(task.startTime),
    previewEndTimeStr: formatTime(task.endTime),
    mode
  }

  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
}

const roundToSnap = (ms: number) => {
  return Math.round(ms / SNAP_MS) * SNAP_MS
}

const onDragMove = (e: MouseEvent) => {
  if (!draggingTask.value) return
  
  const deltaX = e.clientX - dragState.value.startX
  const state = dragState.value
  
  if (state.mode === 'move') {
    const rawLeft = state.startLeft + deltaX
    const rawStartMs = state.originalStartTime + pxToMs(deltaX)
    const snappedStartMs = roundToSnap(rawStartMs)
    const diffMs = snappedStartMs - state.originalStartTime
    
    let newLeft = state.startLeft + (diffMs / totalDuration.value) * TOTAL_WIDTH
    
    // Boundary checks
    newLeft = Math.max(0, Math.min(newLeft, TOTAL_WIDTH - state.width))
    
    // Update state
    state.currentLeft = newLeft
    
    // Update preview times
    state.previewStartTimeStr = formatTime(new Date(snappedStartMs).toISOString())
    state.previewEndTimeStr = formatTime(new Date(snappedEndMs).toISOString())
  } 
  else if (state.mode === 'resize-right') {
    const rawWidth = state.width + deltaX
    const currentDuration = state.originalEndTime - state.originalStartTime
    const rawDuration = currentDuration + pxToMs(deltaX)
    const snappedDuration = Math.max(SNAP_MS, roundToSnap(rawDuration)) // Min duration 1 snap unit
    
    let newWidth = (snappedDuration / totalDuration.value) * TOTAL_WIDTH
    
    // Boundary check
    newWidth = Math.min(newWidth, TOTAL_WIDTH - state.startLeft)
    state.width = newWidth
    
    // Update preview
    state.previewStartTimeStr = formatTime(new Date(state.originalStartTime).toISOString())
    state.previewEndTimeStr = formatTime(new Date(newEndMs).toISOString())
  }
  else if (state.mode === 'resize-left') {
    const rawLeft = state.startLeft + deltaX
    
    const rawStartMs = state.originalStartTime + pxToMs(deltaX)
    const snappedStartMs = roundToSnap(rawStartMs)
    const diffMs = snappedStartMs - state.originalStartTime
    
    let newLeft = state.startLeft + (diffMs / totalDuration.value) * TOTAL_WIDTH
    
    const rightEdge = state.startLeft + state.width
    const minWidthPx = (SNAP_MS / totalDuration.value) * TOTAL_WIDTH
    newLeft = Math.max(0, Math.min(newLeft, rightEdge - minWidthPx))
    
    const newWidth = rightEdge - newLeft
    
    state.currentLeft = newLeft
    state.width = newWidth
    
    // Update preview
    state.previewStartTimeStr = formatTime(new Date(snappedStartMs).toISOString())
    state.previewEndTimeStr = formatTime(new Date(state.originalEndTime).toISOString())
  }
}

const onDragEnd = async () => {
  if (!draggingTask.value) return
  
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)

  const state = dragState.value
  let newStartMs = state.originalStartTime
  let newEndMs = state.originalEndTime
  
  if (state.mode === 'move') {
    const deltaPx = state.currentLeft - state.startLeft
    if (Math.abs(deltaPx) < 1) { // Looser check due to snapping
        draggingTask.value = null
        return
    }
    const deltaMs = pxToMs(deltaPx)
    // Round again to be safe, though display was already snapped
    newStartMs = roundToSnap(state.originalStartTime + deltaMs)
    newEndMs = newStartMs + (state.originalEndTime - state.originalStartTime)
  } 
  else if (state.mode === 'resize-right') {
     const newDuration = pxToMs(state.width)
     newEndMs = roundToSnap(state.originalStartTime + newDuration)
     
     if (Math.abs(newEndMs - state.originalEndTime) < 1000) {
        draggingTask.value = null
        return
     }
  }
  else if (state.mode === 'resize-left') {
    const deltaPx = state.currentLeft - state.startLeft
    const deltaMs = pxToMs(deltaPx)
    newStartMs = roundToSnap(state.originalStartTime + deltaMs)
    
    if (Math.abs(newStartMs - state.originalStartTime) < 1000) {
        draggingTask.value = null
        return
    }
  }

  const toLocalISO = (ms: number) => {
    const d = new Date(ms)
    const offset = d.getTimezoneOffset() * 60000
    return new Date(d.getTime() - offset).toISOString().slice(0, 19)
  }

  const newStartTimeStr = toLocalISO(newStartMs)
  const newEndTimeStr = toLocalISO(newEndMs)

  const task = draggingTask.value
  
  try {
    emit('update-task', {
      planId: task.planId,
      newStartTime: newStartTimeStr,
      newEndTime: newEndTimeStr
    })
  } catch (e) {
    // 
  } finally {
    draggingTask.value = null
  }
}

// 自动滚动到当前时间
onMounted(() => {
  if (timelineRef.value) {
    const now = new Date()
    const px = timeToPx(now.getTime())
    const clientWidth = timelineRef.value.clientWidth
    timelineRef.value.scrollLeft = Math.max(0, px - clientWidth / 2)
    
    timelineRef.value.addEventListener('scroll', syncScroll)
  }
})

onUnmounted(() => {
  if (timelineRef.value) {
    timelineRef.value.removeEventListener('scroll', syncScroll)
  }
})

</script>

<template>
  <div class="gantt-wrapper glass-card">
    <div class="gantt-container" ref="chartRef">
      <div class="gantt-sidebar">
        <div class="gantt-header-cell sidebar-header">资源列表</div>
        <div class="gantt-sidebar-body" ref="sidebarBodyRef">
          <div 
            v-for="(res, index) in resources" 
            :key="res.resourceId + '-' + res.resourceType" 
            class="gantt-row-label"
            :class="{ 'row-odd': index % 2 === 1 }"
          >
            <div class="res-name" :title="res.resourceName">{{ res.resourceName }}</div>
            <div class="res-type">{{ formatType(res.resourceType) }}</div>
          </div>
        </div>
      </div>

      <div class="gantt-main" ref="timelineRef">
        <div class="gantt-header-row" :style="{ width: TOTAL_WIDTH + 'px' }">
          <div v-for="h in TOTAL_HOURS" :key="h" class="gantt-hour-tick" :style="{ width: HOUR_WIDTH + 'px' }">
            {{ (h - 1).toString().padStart(2, '0') }}:00
          </div>
        </div>

        <div class="gantt-body" :style="{ width: TOTAL_WIDTH + 'px' }">
          <div 
            v-for="(res, index) in resources" 
            :key="res.resourceId + '-' + res.resourceType" 
            class="gantt-row"
            :class="{ 'row-odd': index % 2 === 1 }"
          >
            <div class="gantt-grid-bg">
              <div v-for="h in TOTAL_HOURS" :key="h" class="grid-col" :style="{ width: HOUR_WIDTH + 'px' }"></div>
            </div>

            <div class="gantt-availability" :style="getAvailabilityStyle(res.startDate, res.endDate)"></div>

            <div
              v-for="task in res.tasks"
              :key="task.taskId"
              class="gantt-bar"
              :class="[
                normalizeStatusClass(task.status),
                { 'is-dragging': draggingTask?.taskId === task.taskId },
                { 'is-short': isShortTask(task) }
              ]"
              :style="getTaskStyle(task)"
              @mousedown.stop="startDrag($event, task)"
            >
              <div class="bar-inner">
                <div class="bar-label">{{ task.taskName }}</div>
                <div class="bar-time">{{ formatTime(task.startTime) }} - {{ formatTime(task.endTime) }}</div>
              </div>

              <div v-if="props.editable !== false" class="resize-handle handle-left"></div>
              <div v-if="props.editable !== false" class="resize-handle handle-right"></div>

              <div v-if="isShortTask(task)" class="bar-label-outside">
                {{ task.taskName }}
              </div>
              
              <div class="bar-tooltip" :class="{ 'show-always': draggingTask?.taskId === task.taskId }">
                <template v-if="draggingTask?.taskId === task.taskId">
                  <div>{{ task.taskName }}</div>
                  <div style="color: #40a9ff; font-weight: bold;">
                    {{ dragState.previewStartTimeStr }} - {{ dragState.previewEndTimeStr }}
                  </div>
                </template>
                <template v-else>
                  <div>{{ task.taskName }}</div>
                  <div>状态: {{ task.status }}</div>
                  <div>{{ task.startTime }} ~ {{ task.endTime }}</div>
                </template>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Current Time Indicator -->
        <div class="current-time-line" :style="{ left: timeToPx(new Date().getTime()) + 'px' }"></div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
$header-height: 40px;
$row-height: 50px;
$border-color: var(--tidal-border);
$primary-color: var(--tidal-primary);
$bg-odd: var(--el-bg-color-page);

.gantt-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--tidal-bg-card);
  border: 1px solid $border-color;
  overflow: hidden;
}

.gantt-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* --- Sidebar --- */
.gantt-sidebar {
  width: 160px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid $border-color;
  background: var(--el-bg-color);
  z-index: 10;
}

.sidebar-header {
  height: $header-height;
  line-height: $header-height;
  padding: 0 16px;
  font-weight: 600;
  color: var(--tidal-text-main);
  border-bottom: 1px solid $border-color;
  background: var(--el-bg-color-page);
}

.gantt-sidebar-body {
  overflow: hidden; /* Sync scroll with main if needed, but here simple */
  flex: 1; /* Ensure it takes remaining height */
}

.gantt-row-label {
  height: $row-height;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid $border-color;
  
  &.row-odd {
    background: $bg-odd;
  }

  .res-name {
    font-size: 13px;
    color: var(--tidal-text-main);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .res-type {
    font-size: 11px;
    color: var(--tidal-text-secondary);
  }
}

/* --- Main Timeline --- */
.gantt-main {
  flex: 1;
  overflow: auto; /* Allow both X and Y scroll */
  position: relative;
  
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(143, 214, 255, 0.2);
    border-radius: 4px;
  }
}

.gantt-header-row {
  display: flex;
  height: $header-height;
  border-bottom: 1px solid $border-color;
  background: var(--el-bg-color-page);
  position: sticky;
  top: 0;
  z-index: 5;
}

.gantt-hour-tick {
  flex-shrink: 0;
  border-right: 1px solid $border-color;
  padding-left: 8px;
  font-size: 12px;
  color: var(--tidal-text-secondary);
  line-height: $header-height;
}

.gantt-body {
  position: relative;
}

.gantt-row {
  height: $row-height;
  position: relative;
  border-bottom: 1px solid $border-color;
  
  &.row-odd {
    background: $bg-odd;
  }
}

.gantt-grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  pointer-events: none;
}

.grid-col {
  height: 100%;
  border-right: 1px dashed rgba(0, 105, 148, 0.12);
  flex-shrink: 0;
}

/* --- Availability --- */
.gantt-availability {
  position: absolute;
  top: 4px;
  bottom: 4px;
  background: rgba(0, 105, 148, 0.06);
  border-radius: 4px;
  pointer-events: none;
}

/* --- Task Bars --- */
.gantt-bar {
  position: absolute;
  top: 8px;
  bottom: 8px;
  border-radius: 4px;
  cursor: grab;
  color: #ffffff;
  font-size: 12px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: box-shadow 0.2s, transform 0.1s;
  overflow: hidden;
  z-index: 2;
  /* 默认色：避免未知状态与背景融为一体 */
  background: linear-gradient(90deg, rgba(0, 105, 148, 0.92), rgba(0, 168, 204, 0.92));

  &:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.4);
    z-index: 3;
    
    .bar-tooltip {
      display: block;
    }
  }

  &.is-dragging {
    cursor: grabbing;
    opacity: 0.9;
    transform: scale(1.02);
    box-shadow: 0 8px 16px rgba(0,0,0,0.5);
    z-index: 100;
    transition: none;
  }

  &.is-short {
    .bar-inner {
      display: none; /* Hide internal text for short tasks */
    }
  }

  .bar-inner {
    display: flex;
    flex-direction: column;
    width: 100%;
    overflow: hidden;
  }

  .bar-label {
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .bar-time {
    font-size: 10px;
    opacity: 0.8;
    white-space: nowrap;
  }

  /* Status Colors */
  /* PlanStatusEnum (backend) */
  &.PLANNING { background: linear-gradient(90deg, #595959, #8c8c8c); }
  &.PLANNED { background: linear-gradient(90deg, #096dd9, #40a9ff); }
  &.REPLANNED { background: linear-gradient(90deg, #531dab, #b37feb); }
  &.REPLAN_REQUIRED { background: linear-gradient(90deg, #d4380d, #fa8c16); }
  &.PUBLISHED { background: linear-gradient(90deg, #08979c, #36cfc9); }
  &.BILLED { background: linear-gradient(90deg, #2f54eb, #597ef7); }
  &.PAID { background: linear-gradient(90deg, #237804, #52c41a); }

  /* Legacy / generic workflow statuses */
  &.PENDING { background: linear-gradient(90deg, #faad14, #ffc53d); }
  &.APPROVED { background: linear-gradient(90deg, #13c2c2, #36cfc9); }
  &.IN_PROGRESS, &.ONGOING { background: linear-gradient(90deg, #1890ff, #40a9ff); }
  &.COMPLETED, &.CONFIRMED { background: linear-gradient(90deg, #52c41a, #73d13d); }
  &.CANCELLED { background: linear-gradient(90deg, #8c8c8c, #bfbfbf); }
  &.REJECTED { background: linear-gradient(90deg, #ff4d4f, #ff7875); }
  &.CERTIFICATED { background: linear-gradient(90deg, #722ed1, #9254de); }
}

.bar-label-outside {
  position: absolute;
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  margin-left: 6px;
  color: #fff;
  font-size: 12px;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(0,0,0,0.8);
  pointer-events: none;
  z-index: 5;
}

.bar-tooltip {
  display: none;
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  margin-bottom: 8px;
  z-index: 999;
  
  &.show-always {
    display: block;
    z-index: 1000;
  }
}

/* Resize Handles */
.resize-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 6px;
  cursor: col-resize;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
  
  &:hover {
    opacity: 1;
    background-color: rgba(255, 255, 255, 0.4);
  }
}

.gantt-bar:hover .resize-handle {
  opacity: 0.5;
}

.handle-left {
  left: 0;
}

.handle-right {
  right: 0;
}

/* Current Time Line */
.current-time-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #f5222d;
  pointer-events: none;
  z-index: 4;
  box-shadow: 0 0 4px rgba(245, 34, 45, 0.5);
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -4px;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 6px solid #f5222d;
  }
}
</style>