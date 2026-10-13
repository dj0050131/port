<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import HeaderBar from '@/components/dashboard/HeaderBar.vue'
import OverviewFooter from '@/components/common/OverviewFooter.vue'
import PortGeoMap from '@/components/port/PortGeoMap.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'
import { formatDateTime } from '@/utils/date-utils'

const store = useDashboardStore()
const route = useRoute()

const integratedBasePath = '/platform/integrated'

const navItems = [
  { label: '综合主屏', path: integratedBasePath },
  { label: '辅助监控', path: `${integratedBasePath}/aux` },
  { label: '船舶分析', path: `${integratedBasePath}/traffic` },
  { label: '调度运行', path: `${integratedBasePath}/operations` },
  { label: '计费中心', path: `${integratedBasePath}/billing` },
  { label: '安全监控', path: `${integratedBasePath}/safety` },
  { label: '气象中心', path: `${integratedBasePath}/weather` },
  { label: '系统总台', path: '/console' },
]

let timer: number | undefined
const isMapDragMode = ref(false)
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.code === 'Space') {
    isMapDragMode.value = true
    event.preventDefault()
  }
}

const handleKeyUp = (event: KeyboardEvent) => {
  if (event.code === 'Space') {
    isMapDragMode.value = false
  }
}

const weatherSummary = computed(() => store.weather?.weather?.condition ?? '--')
const footerUpdated = computed(() => (store.lastUpdated ? formatDateTime(store.lastUpdated) : '—'))
const mapShips = computed(() => store.mapPayload?.ships ?? store.shipDynamics ?? [])
const mapBerths = computed(() => store.mapPayload?.berths ?? store.berths ?? [])
const mapRoutes = computed(() => store.mapPayload?.routes ?? [])

const refresh = async () => {
  await store.fetchAll()
}

onMounted(async () => {
  await refresh()
  timer = window.setInterval(refresh, 30_000)
  window.addEventListener('keydown', handleKeyDown)
  window.addEventListener('keyup', handleKeyUp)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  window.removeEventListener('keydown', handleKeyDown)
  window.removeEventListener('keyup', handleKeyUp)
})
</script>

<template>
  <div class="screen-layout" :class="{ 'map-drag-mode': isMapDragMode }">
    <div class="screen-layout__map">
      <PortGeoMap
        :ships="mapShips"
        :berths="mapBerths"
        :routes="mapRoutes"
        :center="[120.699362, 27.993828]"
      />
    </div>
    <div class="screen-layout__overlay">
      <HeaderBar :current-time="store.now" :weather-text="weatherSummary" />
      <nav class="screen-layout__nav">
        <div class="nav-items">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: route.path === item.path }"
          >
            {{ item.label }}
          </RouterLink>
        </div>
      </nav>
    </div>
    <section class="screen-layout__body">
      <div class="screen-layout__content">
        <div class="screen-layout__viewport">
          <RouterView :key="route.fullPath" />
        </div>
      </div>
    </section>
    <OverviewFooter
      class="screen-layout__footer"
      :current-time="store.now"
      :last-updated="footerUpdated"
    />
  </div>
</template>

<style scoped lang="scss">
.screen-layout {
  position: relative;
  min-height: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 100%;
  margin: 0 auto; 
  overflow: hidden;
  background: var(--tech-bg-deep);
}

.screen-layout__map {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.screen-layout__overlay {
  position: relative;
  z-index: 2;
  padding-top: 6px;
  pointer-events: none;
}

.screen-layout__overlay > * {
  pointer-events: auto;
}

.screen-layout__nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 12px 30px;
  margin: 0 30px;
}

.nav-items {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.nav-item {
  position: relative;
  text-decoration: none;
  font-family: var(--font-main);
  font-size: 16px;
  letter-spacing: 0.1em;
  color: var(--tech-text-dim);
  padding: 10px 28px;
  border: 1px solid rgba(0, 242, 255, 0.1);
  background: rgba(0, 10, 24, 0.6);
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;
  min-width: 140px;
  text-align: center;
  clip-path: polygon(10% 0, 100% 0, 100% 100%, 0 100%, 0 30%); /* Removed bottom cut for simpler look if desired, or kept polygon but removed border-bottom effect from previous context */
}

/* Removed ::before pseudo-element that created the cyan bar */
/* .nav-item::before { ... } */

.nav-item:hover {
  color: #fff;
  background: rgba(0, 242, 255, 0.1);
  border-color: var(--tech-cyan);
}

.nav-item.active {
  color: #fff;
  background: rgba(0, 242, 255, 0.2);
  border-color: var(--tech-cyan);
  box-shadow: 0 0 15px rgba(0, 242, 255, 0.3);
}

.screen-layout__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 1px 6px 0 6px;
  z-index: 1;
  pointer-events: none; /* 允许地图拖拽，交互区域单独放开 */
}

.screen-layout__content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.screen-layout__viewport {
  flex: 1;
  min-height: 0;
  overflow: auto;
  pointer-events: auto; /* 允许滚动条与内容交互 */
  scrollbar-width: thin;
  scrollbar-color: var(--tech-cyan) rgba(6, 18, 36, 0.6);
}

.screen-layout.map-drag-mode .screen-layout__viewport {
  pointer-events: none;
}

.screen-layout.map-drag-mode .screen-layout__map {
  cursor: grab;
}

.screen-layout__viewport::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.screen-layout__viewport::-webkit-scrollbar-track {
  background: rgba(6, 18, 36, 0.6);
  border-radius: 999px;
}

.screen-layout__viewport::-webkit-scrollbar-thumb {
  background: rgba(0, 242, 255, 0.55);
  border-radius: 999px;
  border: 1px solid rgba(0, 242, 255, 0.25);
}

.screen-layout__viewport::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 242, 255, 0.75);
}

.screen-layout__footer {
  margin: 0 30px;
  z-index: 2;
}
</style>
