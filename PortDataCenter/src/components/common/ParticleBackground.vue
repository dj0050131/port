<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationFrameId: number
let particles: Particle[] = []
let ctx: CanvasRenderingContext2D | null = null

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
}

const props = withDefaults(defineProps<{
  color?: string
  lineColor?: string
  count?: number
}>(), {
  color: '#00a8cc',
  lineColor: 'rgba(0, 168, 204, 0.2)',
  count: 60
})

class Particle {
  constructor(w: number, h: number) {
    this.x = Math.random() * w
    this.y = Math.random() * h
    this.vx = (Math.random() - 0.5) * 0.5
    this.vy = (Math.random() - 0.5) * 0.5
    this.size = Math.random() * 2 + 1
  }

  update(w: number, h: number) {
    this.x += this.vx
    this.y += this.vy

    if (this.x < 0 || this.x > w) this.vx *= -1
    if (this.y < 0 || this.y > h) this.vy *= -1
  }

  draw(context: CanvasRenderingContext2D) {
    context.beginPath()
    context.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    context.fillStyle = props.color
    context.fill()
  }
}

const initParticles = (width: number, height: number) => {
  particles = []
  for (let i = 0; i < props.count; i++) {
    particles.push(new Particle(width, height))
  }
}

const drawLines = (width: number, height: number) => {
  if (!ctx) return
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 150) {
        ctx.beginPath()
        ctx.strokeStyle = props.lineColor
        ctx.lineWidth = 0.5
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.stroke()
      }
    }
  }
}

const animate = () => {
  if (!canvasRef.value || !ctx) return
  const width = canvasRef.value.width
  const height = canvasRef.value.height

  ctx.clearRect(0, 0, width, height)

  particles.forEach(p => {
    p.update(width, height)
    p.draw(ctx!)
  })

  drawLines(width, height)

  animationFrameId = requestAnimationFrame(animate)
}

const handleResize = () => {
  if (!canvasRef.value) return
  canvasRef.value.width = window.innerWidth
  canvasRef.value.height = window.innerHeight
  if (particles.length === 0) {
      initParticles(window.innerWidth, window.innerHeight)
  }
}

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    handleResize()
    initParticles(window.innerWidth, window.innerHeight)
    animate()
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  cancelAnimationFrame(animationFrameId)
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-background"></canvas>
</template>

<style scoped>
.particle-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
