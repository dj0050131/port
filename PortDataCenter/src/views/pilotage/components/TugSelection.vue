<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  selectedTugs: string[]
  vesselName: string
}>()

const emit = defineEmits(['update:selectedTugs', 'close'])

const tugList = [
  { id: 1, name: '温拖11', hp: '4000', power: '2942' },
  { id: 2, name: '温拖2', hp: '3200', power: '2353' },
  { id: 3, name: '温拖3', hp: '3200', power: '2353' },
  { id: 4, name: '温拖5', hp: '4000', power: '2942' },
  { id: 5, name: '温拖6', hp: '4000', power: '2942' },
  { id: 6, name: '温拖8', hp: '5000', power: '3677' },
  { id: 7, name: '涌港拖7', hp: '3600', power: '2647' },
  { id: 8, name: '涌港拖15', hp: '4000', power: '2942' },
  { id: 9, name: '华辰1', hp: '3200', power: '2353' },
  { id: 10, name: '消拖1', hp: '4000', power: '2942' },
  { id: 11, name: '温消拖16', hp: '5200', power: '3824' },
  { id: 12, name: '温消拖17', hp: '5200', power: '3824' },
]

const selection = ref<string[]>([...props.selectedTugs])

const handleSelectionChange = (val: any[]) => {
  // Element Plus table selection handling
  // But we want to bind to specific tug names
}

const toggleSelection = (row: any) => {
  const index = selection.value.indexOf(row.name)
  if (index > -1) {
    selection.value.splice(index, 1)
  } else {
    selection.value.push(row.name)
  }
}

const isSelected = (row: any) => selection.value.includes(row.name)

const handleSave = () => {
  emit('update:selectedTugs', selection.value)
  emit('close')
}
</script>

<template>
  <div class="tug-selection">
    <div class="header-tip">请为"{{ vesselName }}"轮选择作业拖轮</div>
    
    <div class="tug-list-container">
      <el-table 
        :data="tugList" 
        style="width: 100%" 
        height="350" 
        border 
        stripe
        @row-click="toggleSelection"
      >
        <el-table-column width="50" align="center">
          <template #default="{ row }">
            <el-checkbox :model-value="isSelected(row)" @change="toggleSelection(row)" />
          </template>
        </el-table-column>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="name" label="艇名" />
        <el-table-column prop="hp" label="马力" />
        <el-table-column prop="power" label="功率" />
      </el-table>
    </div>

    <div class="dialog-footer">
      <el-button @click="$emit('close')">取 消</el-button>
      <el-button type="primary" @click="handleSave">保 存</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tug-selection {
  padding: 10px;
}

.header-tip {
  font-size: 14px;
  font-weight: 600;
  color: var(--tidal-primary);
  margin-bottom: 12px;
}

.tug-list-container {
  border: 1px solid var(--tidal-border);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>

