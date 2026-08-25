<template>
  <div class="empty-state">
    <div class="empty-state__icon">{{ icon }}</div>
    <p class="empty-state__text">{{ text }}</p>
    <button v-if="actionText" type="button" class="btn btn-secondary btn-sm" @click="$emit('action')">
      {{ actionText }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  icon: { type: String, default: '—' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>
