<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth.ts'
import { formatDateTime } from '@/utils/date-utils'
import { getPilotageStat, getThroughputStat } from '@/api/dashboard'

// Types
interface PlatformCard {
  id: string
  name: string
  badge: string
  description: string
  status: 'online' | 'maintenance'
  target: string
  action: 'internal' | 'external'
  metric: { label: string; value: string | number }
  dbSource: string
}

const router = useRouter()
const auth = useAuthStore()
const now = ref(new Date())
const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationFrameId: number
let timer: number
const showMobileModal = ref(false)

const dataCenterLoginUrl = (() => {
  if (typeof window !== 'undefined') {
    const { origin } = window.location
    return `${origin}/port-data-center/login`
  }
  return '/port-data-center/login'
})()

const mobileDownloadUrl = computed(() => {
  const envUrl = (import.meta as any).env?.VITE_PILOT_MOBILE_URL as string | undefined
  if (envUrl && envUrl.trim()) return envUrl.trim()
  if (typeof window !== 'undefined') {
    return `${window.location.origin}/pilotMobile/PilotMobileApp.apk`
  }
  return '/pilotMobile/PilotMobileApp.apk'
})

const mobileQrImg = computed(() => '')

const platforms = ref<PlatformCard[]>([
  {
    id: 'dispatch',
    name: '港口引航计费一体化平台',
    badge: 'CORE OPS',
    description: '集成引航调度、费用结算与资源管理的核心作业系统。',
    status: 'online',
    target: '/platform/integrated',
    action: 'internal',
    metric: { label: '今日任务', value: '--' },
    dbSource: "SELECT COUNT(*) FROM pilotage_plans WHERE DATE(plan_start_time) = CURDATE()"
  },
  {
    id: 'data',
    name: '港口大数据管理中心',
    badge: 'DATA HUB',
    description: '汇聚船舶档案、航次信息与气象潮汐的数据资产中心。',
    status: 'online',
    target: dataCenterLoginUrl,
    action: 'external',
    metric: { label: '在港船舶', value: '--' },
    dbSource: "SELECT COUNT(*) FROM voyages WHERE voyage_status = 'IMPORT'"
  },
  {
    id: 'mobile',
    name: '引航调度移动端',
    badge: 'MOBILE',
    description: '扫码下载引航员移动端 App（支持任务、确认与调度流程）。',
    status: 'online',
    target: '',
    action: 'internal',
    metric: { label: '下载方式', value: '二维码/链接' },
    dbSource: '移动端安装包建议由同域静态资源或对象存储提供'
  }
])

const greeting = computed(() => {
  const h = now.value.getHours()
  return h < 9 ? '早安' : h < 12 ? '上午好' : h < 18 ? '下午好' : '晚上好'
})

const timeDisplay = computed(() => ({
  time: formatDateTime(now.value).split(' ')[1],
  date: formatDateTime(now.value).split(' ')[0]
}))

const initParticles = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  let width = canvas.width = window.innerWidth
  let height = canvas.height = window.innerHeight

  const particles: Array<{ x: number; y: number; vx: number; vy: number; size: number }> = []
  const count = 100
  const connectionDistance = 150

  for (let i = 0; i < count; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      size: Math.random() * 2 + 1
    })
  }

  const animate = () => {
    ctx.clearRect(0, 0, width, height)

    // Draw Particles & Connections
    ctx.fillStyle = '#00f2ff'
    ctx.strokeStyle = 'rgba(0, 242, 255, 0.15)'

    for (let i = 0; i < count; i++) {
      let p = particles[i]
      p.x += p.vx
      p.y += p.vy

      // Bounce
      if (p.x < 0 || p.x > width) p.vx *= -1
      if (p.y < 0 || p.y > height) p.vy *= -1

      // Draw Point
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fill()

      for (let j = i + 1; j < count; j++) {
        let p2 = particles[j]
        let dx = p.x - p2.x
        let dy = p.y - p2.y
        let dist = Math.sqrt(dx * dx + dy * dy)

        if (dist < connectionDistance) {
          ctx.lineWidth = 1 - dist / connectionDistance
          ctx.beginPath()
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.stroke()
        }
      }
    }
    animationFrameId = requestAnimationFrame(animate)
  }

  animate()

  window.addEventListener('resize', () => {
    width = canvas.width = window.innerWidth
    height = canvas.height = window.innerHeight
  })
}

const updatePlatformMetrics = async () => {
  try {
    const [pilotageRes, throughputRes] = await Promise.all([
      getPilotageStat(),
      getThroughputStat()
    ])
    const todayPlans = pilotageRes.data?.todayPlans ?? 0
    const inPortShips = throughputRes.data?.inPortShips ?? throughputRes.data?.inboundShips ?? 0

    platforms.value = platforms.value.map((item) => {
      if (item.id === 'dispatch') {
        return { ...item, metric: { ...item.metric, value: todayPlans } }
      }
      if (item.id === 'data') {
        return { ...item, metric: { ...item.metric, value: inPortShips } }
      }
      return item
    })
  } catch (error) {
    console.error('[SystemConsole] 获取平台指标失败', error)
  }
}

const handleEnter = (p: PlatformCard) => {
  if (p.id === 'mobile') {
    showMobileModal.value = true
    return
  }
  if (p.action === 'internal') router.push(p.target)
  else if (p.target) {
    const win = window.open(p.target, '_blank')
    if (!win) {
      window.location.href = p.target
    }
  }
}

const closeMobileModal = () => {
  showMobileModal.value = false
}

const copyMobileUrl = async () => {
  try {
    await navigator.clipboard.writeText(mobileDownloadUrl.value)
  } catch (e) {
    const input = document.createElement('input')
    input.value = mobileDownloadUrl.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
  }
}

const handleLogout = () => {
  auth.logout()
  router.replace({ name: 'Login' })
}

onMounted(() => {
  auth.hydrate()
  timer = window.setInterval(() => { now.value = new Date() }, 1000)
  initParticles()
  updatePlatformMetrics()
})

onBeforeUnmount(() => {
  clearInterval(timer)
  cancelAnimationFrame(animationFrameId)
})
</script>

<template>
  <div class="sys-console">
    <canvas ref="canvasRef" class="bg-canvas"></canvas>

    <header class="sys-header">
      <div class="logo-area">
        <div class="logo-icon">
          <div class="logo-inner"></div>
        </div>
        <div class="brand-text">
          <h1>PORT INTELLIGENCE HUB</h1>
          <p>港口智能中枢系统</p>
        </div>
      </div>
      <div class="status-area">
        <div class="time-block">
          <span class="date">{{ timeDisplay.date }}</span>
          <span class="time">{{ timeDisplay.time }}</span>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <span class="power-icon">⏻</span> 退出
        </button>
      </div>
    </header>

    <main class="sys-main">
      <div class="welcome-bar">
        <h2>{{ greeting }}，<span class="user-highlight">{{ auth.displayName || 'Administrator' }}</span></h2>
        <p>系统运行正常，所有服务节点已就绪。</p>
      </div>

      <div class="cards-container">
        <div v-for="p in platforms" :key="p.id" class="tech-card" @click="handleEnter(p)">
          <div class="card-decor top-left"></div>
          <div class="card-decor bottom-right"></div>
          <div class="card-glow"></div>

          <div class="card-header">
            <span class="badge">{{ p.badge }}</span>
            <div class="status-dot" :class="p.status"></div>
          </div>

          <div class="card-body">
            <h3>{{ p.name }}</h3>
            <p>{{ p.description }}</p>
          </div>

          <div class="card-footer">
            <div class="metric">
              <label>{{ p.metric.label }}</label>
              <span class="value">{{ p.metric.value }}</span>
            </div>
            <div class="action-arrow">
              <span>ENTER</span>
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" fill="none" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showMobileModal" class="modal-mask" @click.self="closeMobileModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">引航调度移动端下载</div>
          <button class="modal-close" @click="closeMobileModal">×</button>
        </div>
        <div class="modal-body">
          <div class="qr-area" v-if="mobileQrImg">
            <img class="qr-img" :src="mobileQrImg" alt="二维码" />
            <div class="qr-tip">请使用手机扫码下载/安装</div>
          </div>
          <div class="link-area">
            <div class="link-label">下载链接</div>
            <div class="link-row">
              <a class="link" :href="mobileDownloadUrl" target="_blank" rel="noreferrer">{{ mobileDownloadUrl }}</a>
              <button class="copy-btn" @click="copyMobileUrl">复制</button>
            </div>
            <div class="link-note">如二维码无法加载（内网限制），请使用“复制”或直接点击链接下载。</div>
          </div>
        </div>
      </div>
    </div>

    <footer class="sys-footer">
      <p>© 2025 PORT INTELLIGENCE SYSTEM. ALL RIGHTS RESERVED.</p>
    </footer>
  </div>
</template>

<style scoped lang="scss">
/* Variables */
$cyan: #00f2ff;
$blue: #0066ff;
$dark: #050b14;
$glass: rgba(10, 25, 45, 0.7);

.sys-console {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at 50% 50%, #0b1e3b 0%, $dark 90%);
  color: #fff;
  font-family: 'Rajdhani', sans-serif;
  display: flex;
  flex-direction: column;
}

.bg-canvas {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
  z-index: 0;
}

/* Header */
.sys-header {
  position: relative;
  z-index: 10;
  height: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  border-bottom: 1px solid rgba($cyan, 0.15);
  background: linear-gradient(90deg, rgba($dark, 0.8), transparent 50%, rgba($dark, 0.8));
  backdrop-filter: blur(10px);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  width: 40px; height: 40px;
  border: 2px solid $cyan;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 15px rgba($cyan, 0.4);
}

.logo-inner {
  width: 20px; height: 20px;
  background: $cyan;
  border-radius: 2px;
  transform: rotate(45deg);
}

.brand-text h1 {
  font-size: 24px;
  letter-spacing: 0.1em;
  margin: 0;
  font-weight: 700;
  background: linear-gradient(to right, #fff, $cyan);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text p {
  font-size: 12px;
  color: rgba($cyan, 0.7);
  letter-spacing: 0.2em;
  margin: 0;
}

.status-area {
  display: flex;
  align-items: center;
  gap: 30px;
}

.time-block {
  text-align: right;
  line-height: 1.2;
}

.time-block .time {
  display: block;
  font-family: 'Orbitron', sans-serif;
  font-size: 24px;
  color: $cyan;
}

.time-block .date {
  font-size: 12px;
  color: #8ba4b6;
  letter-spacing: 0.1em;
}

.logout-btn {
  background: rgba(255, 50, 50, 0.1);
  border: 1px solid rgba(255, 50, 50, 0.3);
  color: #ff4d4d;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Rajdhani', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: rgba(255, 50, 50, 0.2);
    box-shadow: 0 0 10px rgba(255, 50, 50, 0.2);
  }
}

/* Main */
.sys-main {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 60px;
}

.welcome-bar {
  text-align: center;
  animation: fadeInDown 0.8s ease-out;
}

.welcome-bar h2 {
  font-size: 36px;
  font-weight: 300;
  margin-bottom: 10px;
}

.user-highlight {
  color: $cyan;
  font-weight: 600;
}

.welcome-bar p {
  color: #8ba4b6;
  font-size: 16px;
  letter-spacing: 0.05em;
}

.cards-container {
  display: flex;
  gap: 40px;
  perspective: 1000px;
  flex-wrap: wrap;
  justify-content: center;
}

/* Tech Card */
.tech-card {
  width: 480px;
  height: 300px;
  background: $glass;
  border: 1px solid rgba($cyan, 0.2);
  position: relative;
  padding: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  backdrop-filter: blur(12px);

  &:hover {
    transform: translateY(-10px) rotateX(2deg);
    border-color: rgba($cyan, 0.6);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);

    .card-glow { opacity: 1; }
    .action-arrow { color: $cyan; transform: translateX(5px); }
    .card-decor.top-left { width: 40px; height: 40px; }
    .card-decor.bottom-right { width: 40px; height: 40px; }
  }
}

.card-decor {
  position: absolute;
  width: 15px; height: 15px;
  border: 2px solid $cyan;
  transition: all 0.4s ease;
}

.card-decor.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.card-decor.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

.card-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba($cyan, 0.15), transparent 70%);
  opacity: 0;
  transition: opacity 0.4s;
  pointer-events: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: $cyan;
  border: 1px solid rgba($cyan, 0.3);
  padding: 4px 8px;
  background: rgba($cyan, 0.05);
}

.status-dot {
  width: 8px; height: 8px;
  background: #00ff9d;
  border-radius: 50%;
  box-shadow: 0 0 8px #00ff9d;
}

.card-body h3 {
  font-size: 28px;
  margin: 0 0 12px;
  font-weight: 500;
  color: #fff;
}

.card-body p {
  font-size: 14px;
  color: #8ba4b6;
  line-height: 1.5;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 20px;
}

.metric label {
  display: block;
  font-size: 12px;
  color: #8ba4b6;
  margin-bottom: 4px;
}

.metric .value {
  font-family: 'Orbitron', sans-serif;
  font-size: 32px;
  color: $cyan;
  text-shadow: 0 0 10px rgba($cyan, 0.4);
}

.action-arrow {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  letter-spacing: 0.1em;
  color: #566b7c;
  transition: all 0.3s;
}

.sys-footer {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 0.2em;
  z-index: 10;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .cards-container { flex-direction: column; gap: 20px; }
  .tech-card { width: 100%; max-width: 500px; height: 260px; }
}

/* Modal */
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal {
  width: min(720px, 96vw);
  background: rgba(10, 25, 45, 0.92);
  border: 1px solid rgba($cyan, 0.25);
  border-radius: 14px;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(16px);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid rgba($cyan, 0.15);
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #eaf2ff;
}

.modal-close {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid rgba($cyan, 0.2);
  background: rgba($cyan, 0.06);
  color: rgba($cyan, 0.95);
  cursor: pointer;
  font-size: 18px;
}

.modal-body {
  padding: 18px 18px 22px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
}

.qr-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid rgba($cyan, 0.15);
  background: rgba($cyan, 0.04);
}

.qr-img {
  width: 260px;
  height: 260px;
  border-radius: 12px;
  background: #fff;
}

.qr-tip {
  font-size: 12px;
  color: rgba(234, 242, 255, 0.75);
}

.link-area {
  padding: 14px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.link-label {
  font-size: 12px;
  color: rgba($cyan, 0.75);
  letter-spacing: 0.15em;
}

.link-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.link {
  flex: 1;
  color: rgba(234, 242, 255, 0.9);
  text-decoration: none;
  word-break: break-all;
  border: 1px solid rgba($cyan, 0.16);
  background: rgba($cyan, 0.04);
  padding: 10px 12px;
  border-radius: 10px;
}

.copy-btn {
  white-space: nowrap;
  border-radius: 10px;
  border: 1px solid rgba($cyan, 0.25);
  background: rgba($cyan, 0.1);
  color: rgba(234, 242, 255, 0.9);
  padding: 10px 12px;
  cursor: pointer;
}

.link-note {
  font-size: 12px;
  color: rgba(234, 242, 255, 0.55);
  line-height: 1.5;
}

@media (max-width: 820px) {
  .modal-body { grid-template-columns: 1fr; }
}
</style>