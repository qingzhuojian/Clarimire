<template>
  <router-view />
  <DevPanel v-if="isDev" />
  <AppToast />
</template>

<script setup>
import DevPanel from './components/DevPanel.vue'
import AppToast from './components/ui/AppToast.vue'

const isDev = import.meta.env.DEV
</script>
