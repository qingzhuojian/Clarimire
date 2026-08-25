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
    const parent = canvas.parentElement
    canvas.width = parent?.clientWidth || window.innerWidth
    canvas.height = parent?.clientHeight || window.innerHeight
    stars = Array.from({ length: 80 }, () => ({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      r: Math.random() * 1.2 + 0.4,
      a: Math.random() * 0.4 + 0.25
    }))
  }

  const spawn = (x, y, n) => {
    for (let i = 0; i < n; i++) {
      particles.push({
        x, y,
        vx: (Math.random() - 0.5) * 2.5,
        vy: (Math.random() - 0.5) * 2.5,
        life: 1,
        size: Math.random() * 2 + 0.6
      })
    }
  }

  const onTap = (e) => {
    const t = e.touches?.[0] || e
    spawn(t.clientX, t.clientY, 14)
  }

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
      p.life -= 0.02
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
  canvas.addEventListener('touchstart', onTap, { passive: true })
  canvas.addEventListener('click', onTap)
  draw()

  cleanup = () => {
    window.removeEventListener('resize', resize)
    canvas.removeEventListener('touchstart', onTap)
    canvas.removeEventListener('click', onTap)
    cancelAnimationFrame(frameId)
  }
})

onUnmounted(() => cleanup?.())
</script>

<style scoped>
.star-trail-canvas {
  position: absolute;
  inset: 0;
  z-index: 0;
}
</style>
