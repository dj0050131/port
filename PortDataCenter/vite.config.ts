import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'
export default defineConfig({
  // Vite 默认已提供 SPA history fallback；移除不兼容 Vite 7 的 rewrite 插件
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  base: '/port-data-center/',
  // base: '/',
  server: {
    port: 4000,
    host: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8099',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      },
      '/uploads': {
        target: 'http://127.0.0.1:8099',
        changeOrigin: true,
        secure: false,
      }
    },
  },
})