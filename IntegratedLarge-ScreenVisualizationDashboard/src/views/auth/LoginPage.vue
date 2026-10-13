<script setup lang="ts">
import { computed, onMounted, reactive, ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth.ts'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const redirectPath = computed(() => {
  const target = route.query.redirect as string | undefined
  return target && target !== '/login' ? target : '/console'
})

const form = reactive({ account: '', password: '' })
const isWarping = ref(false)
const errorMessage = ref('')
const isLoading = computed(() => isWarping.value || auth.loading)

const showRules = ref(false)
const camera = reactive({ x: 0, y: 0, lerpX: 0, lerpY: 0 })

const handleMouseMove = (e: MouseEvent) => {
  camera.x = (e.clientX / window.innerWidth - 0.5) * 25
  camera.y = (e.clientY / window.innerHeight - 0.5) * -18
}

const starParticles = Array.from({ length: 260 }, (_, i) => ({
  id: i,
  initialLeft: Math.random() * 100 + 'vw',
  initialTop: Math.random() * 100 + 'vh',
  z: Math.random() * -3000,
  opacity: Math.random() * 0.6 + 0.4,
  size: Math.random() * 3 + 2,
  duration: Math.random() * 4 + 4 + 's',
  delay: Math.random() * -5 + 's'
}))

const handleLogin = async () => {
  if (isLoading.value) return
  isWarping.value = true
  try {
    await auth.login(form)
    setTimeout(() => router.replace(redirectPath.value), 1600)
  } catch (error) {
    isWarping.value = false
    errorMessage.value = error instanceof Error ? error.message : '核心链路认证失败'
  }
}

const raf = () => {
  camera.lerpX += (camera.x - camera.lerpX) * 0.08
  camera.lerpY += (camera.y - camera.lerpY) * 0.08
  requestAnimationFrame(raf)
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  raf()
})
onUnmounted(() => window.removeEventListener('mousemove', handleMouseMove))
</script>

<template>
  <div class="warp-drive-root" :class="{ 'is-warping': isWarping }">
    <div class="tunnel-system">
      <div class="tunnel-wall wall-top"></div>
      <div class="tunnel-wall wall-bottom"></div>
      <div class="tunnel-wall wall-left"></div>
      <div class="tunnel-wall wall-right"></div>
      <div class="star-field">
        <div v-for="p in starParticles" :key="p.id" class="star-node"
             :style="{
               left: p.initialLeft,
               top: p.initialTop,
               width: p.size + 'px',
               height: p.size + 'px',
               transform: `translateZ(${p.z}px)`,
               opacity: p.opacity,
               animationDuration: p.duration,
               animationDelay: p.delay
             }">
        </div>
      </div>
    </div>

    <div class="hud-frame">
      <div class="hud-top">
        <div class="sys-id">PILOT_DISPATCH_v2.0</div>
        <div class="sys-title">引航调度及费收综合管理系统</div>
        <div class="sys-status"><span>数据链路: 正常</span> <span class="blink">●</span></div>
      </div>
      <div class="side-hud left">
        <div v-for="i in 8" :key="i" class="mark-line" :style="{ width: Math.random()*20+10+'px' }"></div>
        <div class="hud-label">VESSEL_TRACKING</div>
      </div>
      <div class="side-hud right">
        <div v-for="i in 8" :key="i" class="mark-line" :style="{ width: Math.random()*20+10+'px' }"></div>
        <div class="hud-label">FEES_SETTLEMENT</div>
      </div>
      <div class="hud-bottom">
        <div class="binary-code">PORT_NODE_SHANGHAI_SEC_01</div>
        <div class="coord-info">N 31.2304 / E 121.4737</div>
      </div>
    </div>

    <div class="viewport-3d" :style="{ transform: `rotateY(${camera.lerpX}deg) rotateX(${camera.lerpY}deg)` }">
      <div class="hologram-stage">
        <div class="vessel-hologram">
          <svg class="vessel-svg" viewBox="0 0 800 400">
            <defs>
              <filter id="neon"><feGaussianBlur stdDeviation="2.5" result="blur" /><feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge></filter>
            </defs>
            <g class="vessel-paths" fill="none" stroke="#00f2ff" stroke-width="1.8" filter="url(#neon)">
              <path class="hull" d="M100,200 L400,80 L700,200 L400,320 Z" />
              <path d="M100,200 Q400,230 700,200" opacity="0.3" />
              <circle cx="400" cy="200" r="60" stroke-dasharray="8, 4" />
              <path d="M400,80 V320 M100,200 H700" opacity="0.2" />
            </g>
          </svg>
          <div class="hologram-text">港口实时动态数据同步中...</div>
        </div>

        <div class="auth-panel">
          <div class="panel-header">调度指挥中心认证</div>
          <form @submit.prevent="handleLogin">
            <div class="input-field">
              <label>ACCOUNT</label>
              <input v-model="form.account" type="text" placeholder="OPERATOR ID" required />
            </div>
            <div class="input-field">
              <label>PASSWORD</label>
              <input v-model="form.password" type="password" placeholder="SECURITY KEY" required />
            </div>
            <div v-if="errorMessage" class="err-msg">{{ errorMessage }}</div>
            <button class="warp-trigger" :disabled="isLoading">
              <span class="btn-text">{{ isWarping ? '正在接入核心系统' : '建立安全链路连接' }}</span>
            </button>
          </form>

          <div class="rules-ticker-box" @click="showRules = true">
            <div class="ticker-label">系统准入守则 (点击查阅详情)</div>
            <div class="ticker-content">
              <span>● 严禁非授权调度指令下发</span>
              <span>● 所有费收结算需符合国家港口收费标准</span>
              <span>● 实时引航数据必须同步归档</span>
              <span>● 操作员需对所有人工录入数据负责</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Transition name="modal-fade">
      <div v-if="showRules" class="modal-overlay" @click.self="showRules = false">
        <div class="rules-modal">
          <div class="modal-scanline"></div>
          <div class="modal-header">
            <span class="header-icon">⚠</span>
            引航调度系统安全管理制度
            <button class="close-btn" @click="showRules = false">×</button>
          </div>
          <div class="modal-body">
            <h3>第一章：调度指挥准则</h3>
            <p>1.1 调度人员必须严格遵守“安全第一、预防为主、综合治理”的方针。</p>
            <p>1.2 所有船舶进出港指令必须经由加密链路双向验证，确保数据完整性。</p>
            <h3>第二章：费收管理规定</h3>
            <p>2.1 费收核算需依据实时吨位、引航距离及附加费率自动计算，禁止私自篡改算法参数。</p>
            <p>2.2 每一笔账单的减免操作必须上传电子审批单据并留档。</p>
            <h3>第三章：信息安全义务</h3>
            <p>3.1 账号禁止外借，登录有效期内操作员不得离开工位。</p>
            <p>3.2 系统产生的所有报表属于内部机密资料，严禁外泄或非必要导出。</p>
          </div>
          <div class="modal-footer">
            <button class="accept-btn" @click="showRules = false">确认已阅并遵守</button>
          </div>
        </div>
      </div>
    </Transition>

    <div class="white-flash"></div>
  </div>
</template>

<style scoped lang="scss">
$cyan: #00f2ff;
$deep: #000408;

.warp-drive-root {
  width: 100vw; height: 100vh; background: $deep; overflow: hidden;
  position: relative; display: flex; align-items: center; justify-content: center;
  perspective: 1200px; color: #fff; font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 隧道系统 */
.tunnel-system {
  position: absolute; inset: 0; transform-style: preserve-3d; z-index: 1;
  .tunnel-wall {
    position: absolute; opacity: 0.4;
    background:
        linear-gradient(rgba($cyan, 0.2) 1.5px, transparent 1.5px) 0 0 / 60px 60px,
        linear-gradient(90deg, rgba($cyan, 0.2) 1.5px, transparent 1.5px) 0 0 / 60px 60px;
    transition: 1.5s cubic-bezier(0.9, 0, 0.1, 1);
  }
  .wall-top { width: 100%; height: 3000px; top: 0; transform: rotateX(90deg) translateZ(-50vh); transform-origin: top; }
  .wall-bottom { width: 100%; height: 3000px; bottom: 0; transform: rotateX(-90deg) translateZ(-50vh); transform-origin: bottom; }
  .wall-left { width: 3000px; height: 100%; left: 0; transform: rotateY(90deg) translateZ(-50vw); transform-origin: left; }
  .wall-right { width: 3000px; height: 100%; right: 0; transform: rotateY(-90deg) translateZ(-50vw); transform-origin: right; }

  .star-field {
    position: absolute; inset: 0; transform-style: preserve-3d; pointer-events: none;
    .star-node {
      position: absolute; background: $cyan; border-radius: 50%;
      box-shadow: 0 0 10px 1px $cyan, 0 0 20px rgba($cyan, 0.4);
      animation: drift linear infinite; transition: 1.2s cubic-bezier(0.7, 0, 0.3, 1);
    }
  }
}

@keyframes drift {
  0% { transform: translateZ(var(--z, 0)) translateY(0) translateX(0); filter: brightness(1); }
  50% { transform: translateZ(var(--z, 0)) translateY(-20px) translateX(10px); filter: brightness(1.5); }
  100% { transform: translateZ(var(--z, 0)) translateY(0) translateX(0); filter: brightness(1); }
}

/* HUD 装饰 */
.hud-frame {
  position: absolute; inset: 0; z-index: 10; pointer-events: none; padding: 40px;
  .hud-top {
    display: flex; justify-content: space-between; border-bottom: 1px solid rgba($cyan, 0.3);
    padding-bottom: 12px; font-size: 13px; color: $cyan; letter-spacing: 2px; font-weight: bold;
  }
  .side-hud {
    position: absolute; top: 50%; transform: translateY(-50%); display: flex; flex-direction: column; gap: 15px;
    .mark-line { height: 1px; background: rgba($cyan, 0.5); }
    .hud-label { font-size: 10px; color: $cyan; transform: rotate(-90deg); margin-top: 30px; letter-spacing: 2px; }
    &.left { left: 40px; } &.right { right: 40px; align-items: flex-end; }
  }
  .hud-bottom {
    position: absolute; bottom: 40px; left: 40px; right: 40px;
    display: flex; justify-content: space-between; font-size: 11px; color: rgba($cyan, 0.5);
  }
}

/* 核心容器 */
.viewport-3d {
  position: relative; z-index: 20; width: 100%; height: 100%;
  transform-style: preserve-3d; display: flex; align-items: center; justify-content: center;
}

.hologram-stage {
  display: flex; align-items: center; gap: 60px; transform: translateZ(100px);
  padding: 0 5%;
}

.vessel-hologram {
  .vessel-svg { width: 500px; filter: drop-shadow(0 0 25px rgba($cyan, 0.4)); animation: pulse 4s infinite; }
  .hologram-text { text-align: center; color: $cyan; font-size: 14px; letter-spacing: 4px; margin-top: -20px; opacity: 0.8; }
}

/* 登录面板样式 */
.auth-panel {
  width: 420px; background: rgba(0, 15, 30, 0.85); backdrop-filter: blur(25px);
  border: 1px solid rgba($cyan, 0.4); padding: 45px; position: relative;
  box-shadow: 0 0 50px rgba(0,0,0,0.8), inset 0 0 30px rgba($cyan, 0.1);
  .panel-header { font-size: 14px; color: $cyan; margin-bottom: 35px; letter-spacing: 2px; border-left: 4px solid $cyan; padding-left: 15px; font-weight: bold; }
}

.input-field {
  margin-bottom: 25px;
  label { display: block; font-size: 11px; color: rgba($cyan, 0.7); margin-bottom: 10px; }
  input {
    width: 100%; background: rgba($cyan, 0.08); border: 1px solid rgba($cyan, 0.25);
    padding: 14px; color: #fff; outline: none; transition: 0.3s;
    &:focus { border-color: $cyan; background: rgba($cyan, 0.15); box-shadow: 0 0 15px rgba($cyan, 0.2); }
  }
}

.warp-trigger {
  width: 100%; padding: 18px; background: $cyan; border: none; font-weight: 900;
  color: #000; letter-spacing: 2px; cursor: pointer; transition: 0.4s;
  clip-path: polygon(0 0, 95% 0, 100% 100%, 5% 100%);
  &:hover { filter: brightness(1.2); transform: scale(1.02); box-shadow: 0 0 30px $cyan; }
}

/* 滚动条区域 */
.rules-ticker-box {
  margin-top: 25px; border: 1px dashed rgba($cyan, 0.3); padding: 10px;
  background: rgba($cyan, 0.03); cursor: pointer; overflow: hidden;
  transition: 0.3s;
  &:hover { background: rgba($cyan, 0.1); border-style: solid; }
  .ticker-label { font-size: 10px; color: $cyan; opacity: 0.6; margin-bottom: 5px; }
  .ticker-content {
    white-space: nowrap; font-size: 11px; color: rgba($cyan, 0.9);
    animation: ticker-move 20s linear infinite;
    span { margin-right: 40px; }
  }
}
@keyframes ticker-move { 0% { transform: translateX(100%); } 100% { transform: translateX(-140%); } }

/* 全息弹窗样式 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.8); z-index: 1000;
  display: flex; align-items: center; justify-content: center; backdrop-filter: blur(12px);
}
.rules-modal {
  width: 550px; max-height: 80vh; background: rgba(0, 20, 40, 0.95);
  border: 1px solid $cyan; box-shadow: 0 0 60px rgba($cyan, 0.3);
  position: relative; overflow: hidden; display: flex; flex-direction: column;
  .modal-scanline {
    position: absolute; top: 0; left: 0; width: 100%; height: 2px;
    background: rgba($cyan, 0.4); animation: scan 4s linear infinite; pointer-events: none;
  }
  .modal-header {
    background: rgba($cyan, 0.15); padding: 18px 25px; font-weight: bold;
    color: $cyan; border-bottom: 1px solid rgba($cyan, 0.3);
    display: flex; justify-content: space-between; align-items: center;
    .close-btn { background: none; border: none; color: $cyan; font-size: 28px; cursor: pointer; line-height: 1; }
  }
  .modal-body {
    padding: 30px; overflow-y: auto; color: rgba(255,255,255,0.85); line-height: 2;
    h3 { color: $cyan; font-size: 15px; margin: 20px 0 10px; border-bottom: 1px solid rgba($cyan, 0.1); }
    p { font-size: 13.5px; margin-bottom: 12px; }
  }
  .modal-footer {
    padding: 20px; text-align: center; border-top: 1px solid rgba($cyan, 0.1);
    .accept-btn {
      background: transparent; border: 1px solid $cyan; color: $cyan;
      padding: 10px 40px; cursor: pointer; transition: 0.3s; font-weight: bold;
      &:hover { background: $cyan; color: #000; box-shadow: 0 0 20px $cyan; }
    }
  }
}

@keyframes scan { 0% { top: 0; } 100% { top: 100%; } }

/* 进场动画 */
.modal-fade-enter-active, .modal-fade-leave-active { transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; transform: scale(0.9) translateY(20px); }

/* 登录时穿梭特效逻辑 */
.is-warping {
  .tunnel-wall { transform: scale3d(2, 2, 0.1) translateZ(800px); opacity: 1; filter: brightness(2); background-image: linear-gradient($cyan 3px, transparent 3px) 0 0 / 20px 20px; }
  .star-node { animation: none !important; height: 1000px !important; width: 3px !important; transform: translate3d(0, 0, 5000px) !important; background: #fff; box-shadow: 0 0 40px $cyan; }
  .viewport-3d { transform: translateZ(2000px) !important; opacity: 0; transition: 1.4s cubic-bezier(0.85, 0, 0.15, 1); }
  .hud-frame { opacity: 0; transition: 0.6s; }
  .white-flash { animation: flash-anim 1.6s forwards; }
}

@keyframes pulse { 0%, 100% { opacity: 0.6; transform: scale(1); } 50% { opacity: 1; transform: scale(1.04); } }
@keyframes flash-anim { 0% { background: transparent; } 80% { background: #fff; } 100% { background: #fff; } }
.blink { animation: blink-anim 1s infinite; }
@keyframes blink-anim { 50% { opacity: 0; } }
.err-msg { color: #ff4d6d; font-size: 12px; margin-bottom: 15px; }
</style>