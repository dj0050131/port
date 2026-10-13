<script setup lang="ts">
import { computed } from 'vue'

export interface TableColumn<Row = Record<string, unknown>> {
  key: keyof Row | string
  label: string
  width?: string | number
  align?: 'left' | 'center' | 'right'
  formatter?: (row: Row, column: TableColumn<Row>, rowIndex: number) => string | number
}

const props = withDefaults(
  defineProps<{
    title?: string
    columns: TableColumn[]
    rows: Record<string, unknown>[]
    height?: number | string
    striped?: boolean
  }>(),
  {
    height: 280,
    striped: true,
  },
)

const containerHeight = computed(() => (typeof props.height === 'number' ? `${props.height}px` : props.height))

const getCellValue = (row: Record<string, unknown>, column: TableColumn, rowIndex: number) => {
  if (column.formatter) return column.formatter(row, column, rowIndex)
  return row[column.key as string] ?? '--'
}
</script>

<template>
  <div class="data-table tech-panel" :style="{ maxHeight: containerHeight, height: 'auto' }">
    <header v-if="title" class="data-table__header">
      <h3 class="data-table__title">{{ title }}</h3>
      <slot name="actions" />
    </header>
    
    <div class="data-table__content custom-scrollbar">
      <table class="tech-table">
        <thead>
          <tr>
            <th 
              v-for="column in columns" 
              :key="column.label" 
              :style="{ width: column.width || 'auto', textAlign: column.align || 'left' }"
            >
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody v-if="rows.length">
          <tr v-for="(row, rowIndex) in rows" :key="rowIndex" :class="{ striped: striped && rowIndex % 2 === 1 }">
            <td v-for="column in columns" :key="column.label" :style="{ textAlign: column.align || 'left' }">
              <span class="cell-text" :title="String(getCellValue(row, column, rowIndex) ?? '')">
                {{ getCellValue(row, column, rowIndex) }}
              </span>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td :colspan="columns.length" class="data-table__empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.data-table {
  display: flex;
  flex-direction: column;
  background: rgba(10, 22, 40, 0.75);
  backdrop-filter: blur(12px);
  border: 1px solid var(--tech-border);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

/* Corner Accents */
.data-table::before,
.data-table::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  border: 2px solid var(--tech-cyan);
  transition: all 0.3s ease;
  pointer-events: none;
}

.data-table::before {
  top: 0; left: 0;
  border-right: none; border-bottom: none;
}
.data-table::after {
  bottom: 0; right: 0;
  border-left: none; border-top: none;
}

.data-table__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 242, 255, 0.2);
}

.data-table__title {
  font-family: var(--font-num);
  font-size: 16px;
  letter-spacing: 0.12em;
  color: var(--tech-cyan);
  text-transform: uppercase;
  margin: 0;
  text-shadow: var(--tech-glow);
}

.data-table__content {
  flex: 1;
  overflow-y: auto;
}

.tech-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  table-layout: fixed;
}

.tech-table th {
  color: var(--tech-text-dim);
  font-weight: normal;
  padding: 8px 6px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 12px;
  text-transform: uppercase;
}

.tech-table td {
  padding: 8px 6px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tech-table tr.striped td {
  background: rgba(0, 242, 255, 0.03);
}

.tech-table tr:hover td {
  background: rgba(0, 242, 255, 0.1);
}

.data-table__empty {
  text-align: center;
  color: rgba(207, 232, 255, 0.7);
  padding: 36px 0;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 242, 255, 0.3);
  border-radius: 2px;
}
</style>
