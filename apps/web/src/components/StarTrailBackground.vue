<template>
  <canvas ref="canvasRef" class="star-trail-canvas" aria-hidden="true"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let cleanup = null

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  let stars = []
  let particles = []
  let frameId = 0

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    stars = Array.from({ length: 140 }, () => ({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      r: Math.random() * 1.4 + 0.4,
      a: Math.random() * 0.45 + 0.25
    }))
  }

  const spawn = (x, y, n) => {
    for (let i = 0; i < n; i++) {
      particles.push({
        x,
        y,
        vx: (Math.random() - 0.5) * 3,
        vy: (Math.random() - 0.5) * 3,
        life: 1,
        size: Math.random() * 2.2 + 0.8
      })
    }
  }

  const onMove = (e) => spawn(e.clientX, e.clientY, 2)
  const onClick = (e) => spawn(e.clientX, e.clientY, 16)

  const draw = () => {
    const g = ctx.createLinearGradient(0, 0, 0, canvas.height)
    g.addColorStop(0, '#070b14')
    g.addColorStop(0.55, '#0f1f3d')
    g.addColorStop(1, '#1e4d8c')
    ctx.fillStyle = g
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    stars.forEach((s) => {
      ctx.beginPath()
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255,255,255,${s.a})`
      ctx.fill()
    })

    particles.forEach((p) => {
      p.x += p.vx
      p.y += p.vy
      p.life -= 0.018
      if (p.life <= 0) return
      const radius = p.size * p.life
      ctx.beginPath()
      ctx.arc(p.x, p.y, radius, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(160,210,255,${p.life * 0.85})`
      ctx.fill()
    })
    particles = particles.filter((p) => p.life > 0)

    frameId = requestAnimationFrame(draw)
  }

  resize()
  window.addEventListener('resize', resize)
  canvas.addEventListener('mousemove', onMove)
  canvas.addEventListener('click', onClick)
  draw()

  cleanup = () => {
    window.removeEventListener('resize', resize)
    canvas.removeEventListener('mousemove', onMove)
    canvas.removeEventListener('click', onClick)
    cancelAnimationFrame(frameId)
  }
})

onUnmounted(() => cleanup?.())
</script>

<style scoped>
.star-trail-canvas {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: auto;
}
</style>
