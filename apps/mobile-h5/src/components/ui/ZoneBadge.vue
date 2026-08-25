<template>
  <span class="zone-badge" :class="[`zone-badge--${zone}`, { 'zone-badge--large': large }]">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { zoneLabel, zoneLabelShort } from '@/utils/labels'

const props = defineProps({
  zone: { type: String, default: '' },
  short: { type: Boolean, default: false },
  large: { type: Boolean, default: false }
})

const text = computed(() => (props.short ? zoneLabelShort(props.zone) : zoneLabel(props.zone)))
</script>

<style scoped>
.zone-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}
.zone-badge--large {
  padding: 6px 16px;
  font-size: 15px;
  font-weight: 600;
}
.zone-badge--core { background: var(--color-success-bg); color: var(--color-success); }
.zone-badge--buffer { background: var(--color-warning-bg); color: var(--color-warning); }
.zone-badge--remote { background: var(--color-danger-bg); color: var(--color-danger); }
</style>
