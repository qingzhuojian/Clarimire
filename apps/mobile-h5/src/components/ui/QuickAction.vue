<template>
  <button
    type="button"
    class="quick-action"
    :class="{ 'quick-action--primary': primary }"
    @click="$emit('click')"
  >
    <span v-if="bang" class="quick-action__bang">!</span>
    <span v-else-if="badge" class="quick-action__badge">{{ badge }}</span>
    <span class="quick-action__title">{{ title }}</span>
    <span v-if="desc" class="quick-action__desc">{{ desc }}</span>
  </button>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  primary: { type: Boolean, default: false },
  badge: { type: [String, Number], default: '' },
  bang: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>

<style scoped>
.quick-action {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  min-height: 78px;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  text-align: left;
  transition: transform 0.15s, box-shadow 0.15s;
}
.quick-action:active {
  transform: scale(0.98);
}
.quick-action:not(.quick-action--primary) {
  box-shadow: var(--shadow-card), inset 3px 0 0 var(--color-primary-light);
}
.quick-action--primary {
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-primary) 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: var(--shadow-primary);
}
.quick-action__title {
  font-size: 15px;
  font-weight: 600;
}
.quick-action__desc {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.85;
  line-height: 1.35;
}
.quick-action:not(.quick-action--primary) .quick-action__desc {
  color: var(--color-text-secondary);
  opacity: 1;
}
.quick-action__bang,
.quick-action__badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  color: #fff;
}
.quick-action__bang {
  background: var(--color-danger);
}
.quick-action__badge {
  background: var(--color-primary);
}
.quick-action--primary .quick-action__bang {
  background: #fff;
  color: var(--color-danger);
}
.quick-action--primary .quick-action__badge {
  background: rgba(255, 255, 255, 0.95);
  color: var(--color-primary);
}
</style>
