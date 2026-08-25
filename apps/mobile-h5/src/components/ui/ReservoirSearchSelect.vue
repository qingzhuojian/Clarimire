<template>
  <div class="reservoir-search" @keydown.esc="open = false">
    <input
      ref="inputEl"
      v-model="keyword"
      type="search"
      class="input"
      autocomplete="off"
      :placeholder="placeholder"
      @focus="open = true"
      @input="onType"
    />
    <ul v-if="open && filtered.length" class="reservoir-search__list">
      <li
        v-for="r in filtered"
        :key="nameOf(r)"
        class="reservoir-search__item"
        :class="{ 'reservoir-search__item--active': nameOf(r) === modelValue }"
        @mousedown.prevent="pick(r)"
      >{{ nameOf(r) }}</li>
    </ul>
    <p v-else-if="open && keyword.trim() && !filtered.length" class="reservoir-search__empty">
      无匹配水库
    </p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '输入关键字搜索水库' }
})
const emit = defineEmits(['update:modelValue', 'change'])

const keyword = ref(props.modelValue || '')
const open = ref(false)
const inputEl = ref(null)

const nameOf = (r) => (typeof r === 'string' ? r : r?.reservoirName || '')

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  const list = props.options || []
  if (!q) return list.slice(0, 40)
  return list.filter((r) => nameOf(r).toLowerCase().includes(q)).slice(0, 40)
})

watch(
  () => props.modelValue,
  (v) => {
    if (v !== keyword.value) keyword.value = v || ''
  }
)

const onType = () => {
  open.value = true
  const q = keyword.value.trim()
  const exact = (props.options || []).find((r) => nameOf(r) === q)
  if (exact) {
    emit('update:modelValue', q)
    emit('change', q)
  } else if (props.modelValue) {
    emit('update:modelValue', '')
    emit('change', '')
  }
}

const pick = (r) => {
  const name = nameOf(r)
  keyword.value = name
  open.value = false
  emit('update:modelValue', name)
  emit('change', name)
}

const onDocPointer = (e) => {
  const root = inputEl.value?.closest('.reservoir-search')
  if (root && !root.contains(e.target)) open.value = false
}

onMounted(() => document.addEventListener('pointerdown', onDocPointer))
onUnmounted(() => document.removeEventListener('pointerdown', onDocPointer))
</script>

<style scoped>
.reservoir-search {
  position: relative;
}
.reservoir-search__list {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  z-index: 20;
  margin: 0;
  padding: 6px 0;
  list-style: none;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.12);
}
.reservoir-search__item {
  padding: 10px 14px;
  font-size: 14px;
  color: var(--color-text);
  cursor: pointer;
}
.reservoir-search__item:active,
.reservoir-search__item:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}
.reservoir-search__item--active {
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-light);
}
.reservoir-search__empty {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
