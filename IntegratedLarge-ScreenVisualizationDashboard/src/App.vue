<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="screen-scale-wrapper">
    <RouterView />
  </div>
</template>

<style>
/* Global Variables Fallback */
:root {
  --tech-bg-deep: #050b14;
  --tech-bg-light: #0a1628;
  --tech-cyan: #00f2ff;
  --tech-blue: #0066ff;
  --tech-purple: #7000ff;
  --tech-text-main: #e0f7ff;
  --tech-text-dim: #8ba4b6;
  --tech-border: rgba(0, 242, 255, 0.3);
  --tech-glow: 0 0 10px rgba(0, 242, 255, 0.5);
  --font-main: 'Rajdhani', sans-serif;
  --font-num: 'Orbitron', sans-serif;
}

body {
  margin: 0;
  background-color: var(--tech-bg-deep);
  color: var(--tech-text-main);
  font-family: var(--font-main);
  overflow: hidden;
}

#app {
  width: 100vw;
  height: 100vh;
}
</style>

<style scoped>
.screen-scale-wrapper {
  width: 100%;
  height: 100%;
}
</style>