<script setup lang="ts">
const props = withDefaults(
    defineProps<{
      titleCn?: string
      titleEn?: string
      currentTime?: string | null
      weatherText?: string | null
    }>(),
    {
      titleCn: '港口引航及计费系统一体化可视化大屏',
      titleEn: 'Port Pilotage & Billing Integrated Visualization',
      currentTime: '—',
      weatherText: '—',
    },
)
</script>

<template>
  <header class="dp-header">
    <div class="dp-header__row">
      <div class="dp-header__side-left">
        <p class="dp-header__title-en">{{ props.titleEn }}</p>
      </div>

      <h1 class="dp-header__title-cn">{{ props.titleCn }}</h1>

      <div class="dp-header__meta">
        <span class="meta-time">{{ props.currentTime }}</span>
        <span class="meta-weather">{{ props.weatherText }}</span>
      </div>
    </div>
  </header>
</template>

<style scoped lang="scss">
.dp-header {
  position: relative;
  width: 100%;
  box-sizing: border-box;
  padding: 10px 24px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(0, 242, 255, 0.1);
  overflow: hidden; /* 防止溢出 */
}

.dp-header__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 100%; /* 确保不超出视口 */
  margin: 0 auto;
  gap: 20px;
}

/* 左侧容器，限制宽度防止挤压中间 */
.dp-header__side-left {
  flex: 1;
  min-width: 0; /* 关键：允许 flex 项目缩小 */
}

.dp-header__title-en {
  font-family: var(--font-num, sans-serif);
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--tech-text-dim, #a1a1a1);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 文字过长显示省略号 */
}

.dp-header__title-cn {
  font-family: var(--font-main, sans-serif);
  font-size: 28px; /* 稍微放大突出重点 */
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--tech-text-main, #fff);
  text-shadow: 0 0 15px rgba(0, 242, 255, 0.6);
  margin: 0;
  white-space: nowrap; /* 保证标题不换行 */
  flex: 0 0 auto;
}

/* 右侧容器：横向排列 */
.dp-header__meta {
  flex: 1;
  display: flex;
  flex-direction: row; /* 修改为横向 */
  align-items: center;
  justify-content: flex-end;
  gap: 15px; /* 时间和天气的间距 */
  min-width: 0;
}

.meta-time {
  font-family: var(--font-num, monospace);
  letter-spacing: 0.1em;
  font-size: 18px;
  color: var(--tech-cyan, #00f2ff);
  white-space: nowrap;
}

.meta-weather {
  font-size: 14px;
  color: var(--tech-text-dim, #a1a1a1);
  white-space: nowrap;
}

/* 响应式适配：针对屏幕变窄后的处理 */
@media (max-width: 1400px) {
  .dp-header__title-cn {
    font-size: 22px;
  }
  .dp-header__title-en {
    display: none; /* 屏幕中等尺寸时隐藏英文标题，为中文腾出空间 */
  }
}

@media (max-width: 1024px) {
  .dp-header__row {
    flex-direction: column;
    gap: 8px;
  }

  .dp-header__meta {
    justify-content: center;
  }
}
</style>