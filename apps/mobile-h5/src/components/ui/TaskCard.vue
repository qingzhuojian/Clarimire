<template>
  <div
    class="task-row"
    :class="{ 'task-row--active': task.status === 'in_progress' }"
    @click="$emit('click', task)"
  >
    <div class="task-row__main">
      <span class="task-row__title">{{ task.title || '未命名任务' }}</span>
      <span v-if="typeText" class="type-chip" :class="`type-chip--${task.taskType}`">{{ typeText }}</span>
      <span v-if="showBang" class="task-row__bang">!</span>
    </div>
    <div v-if="showActions" class="task-row__actions" @click.stop>
      <button
        v-if="task.status === 'pending' || task.status === 'assigned'"
        type="button"
        class="task-row__btn"
        @click="$emit('start', task)"
      >开始</button>
      <button
        v-else-if="task.status === 'in_progress'"
        type="button"
        class="task-row__btn task-row__btn--primary"
        @click="$emit('start', task)"
      >打卡</button>
      <button type="button" class="task-row__link" @click="$emit('click', task)">详情 ›</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { taskTypeLabel } from '@/utils/labels'

const props = defineProps({
  task: { type: Object, required: true },
  showActions: { type: Boolean, default: false }
})
defineEmits(['click', 'start'])

const typeText = computed(() => {
  if (!props.task.taskType) return ''
  const label = taskTypeLabel(props.task.taskType)
  return label === '-' ? '' : label
})

const showBang = computed(() => {
  if (props.task.status === 'completed' || props.task.status === 'cancelled') return false
  if (props.task.taskType !== 'daily') return false
  if (!props.task.dueTime) return true
  return new Date(props.task.dueTime).getTime() <= Date.now()
})
</script>

<style scoped>
.task-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  background: transparent;
}
.task-row:last-child {
  border-bottom: none;
}
.task-row--active {
  box-shadow: inset 3px 0 0 var(--color-primary);
  padding-left: 8px;
  margin-left: -8px;
}
.task-row__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.task-row__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-row__bang {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--color-danger);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.task-row__actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}
.task-row__btn {
  border: none;
  border-radius: 6px;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  background: var(--color-primary-light);
  cursor: pointer;
}
.task-row__btn--primary {
  color: #fff;
  background: var(--color-primary);
}
.task-row__link {
  border: none;
  background: transparent;
  padding: 4px 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  white-space: nowrap;
}
.task-row__link:active {
  color: var(--color-primary);
}
</style>
