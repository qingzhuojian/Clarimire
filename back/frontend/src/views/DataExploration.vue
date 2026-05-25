<template>
  <div class="data-exploration">
    <div id="exploration-map" style="width: 100%; height: 100vh;"></div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

onMounted(() => {
  const map = L.map('exploration-map').setView([39.9042, 116.4074], 10);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map);
});
</script>