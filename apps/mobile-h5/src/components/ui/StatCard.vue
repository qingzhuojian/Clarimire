<template>
  <div class="stat-card">
    <div class="stat-card__value">{{ value }}</div>
    <div class="stat-card__label">{{ label }}</div>
  </div>
</template>

<script setup>
defineProps({
  value: { type: [String, Number], default: 0 },
  label: { type: String, default: '' }
})
</script>

<style scoped>
.stat-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 14px 12px;
  text-align: center;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border);
  border-top: 2px solid var(--color-primary-soft);
}
.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.2;
}
.stat-card__label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
