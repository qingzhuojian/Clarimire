<template>
  <Transition name="toast-fade">
    <div v-if="visible" class="app-toast" :class="`app-toast--${type}`">{{ message }}</div>
  </Transition>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { visible, message, type } = useToast()
</script>

<style scoped>
.app-toast {
  position: fixed;
  top: 72px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10000;
  max-width: calc(100% - 48px);
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 14px;
  color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  pointer-events: none;
}
.app-toast--success { background: rgba(56, 158, 13, 0.92); }
.app-toast--error { background: rgba(207, 19, 34, 0.92); }
.app-toast--info { background: rgba(37, 99, 235, 0.92); }
.toast-fade-enter-active,
.toast-fade-leave-active { transition: opacity 0.25s, transform 0.25s; }
.toast-fade-enter-from,
.toast-fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(-8px); }
</style>
