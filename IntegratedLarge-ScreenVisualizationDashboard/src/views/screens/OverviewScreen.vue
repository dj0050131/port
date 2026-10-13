<script setup lang="ts">
import ShipDynamicsPanel from '@/components/dashboard/ShipDynamicsPanel.vue'
import PilotPlanPanel from '@/components/dashboard/PilotPlanPanel.vue'
import WeatherTidePanel from '@/components/dashboard/WeatherTidePanel.vue'
import FeeStatisticPanel from '@/components/dashboard/FeeStatisticPanel.vue'
import RealtimeAlertPanel from '@/components/dashboard/RealtimeAlertPanel.vue'
import SystemStatusPanel from '@/components/dashboard/SystemStatusPanel.vue'
import PortMap from '@/components/dashboard/PortMap.vue'
import { useDashboardStore } from '@/store/modules/dashboard.ts'

const store = useDashboardStore()
</script>

<template>
  <div class="overview-grid">
    <section class="column-left">
      <ShipDynamicsPanel :ship-dynamics="store.shipDynamics" :loading="store.loading" />
      <PilotPlanPanel :pilot-plans="store.pilotPlans" />
    </section>
    <section class="column-center glass-card">
      <PortMap :payload="store.mapPayload" :loading="store.loading" :berths="store.berths" />
    </section>
    <section class="column-right">
      <WeatherTidePanel :weather="store.weather" />
      <FeeStatisticPanel :statistics="store.feeStats" />
    </section>
    <section class="column-bottom">
      <RealtimeAlertPanel :alerts="store.alerts" />
      <SystemStatusPanel :systems="store.systems" />
    </section>
  </div>
</template>

<style scoped lang="scss">
.overview-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr) 360px;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 18px;
  min-height: 0;
}

.column-left,
.column-right {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.column-center {
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.column-bottom {
  grid-column: 1 / span 3;
  display: grid;
  grid-template-columns: 2fr 1.2fr;
  gap: 18px;
}

@media (max-width: 1680px) {
  .overview-grid {
    grid-template-columns: 280px minmax(0, 1fr) 320px;
  }
}

@media (max-width: 1366px) {
  .overview-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto 1fr auto auto;
  }

  .column-bottom {
    grid-column: 1 / span 1;
    grid-template-columns: 1fr;
  }
}
</style>

