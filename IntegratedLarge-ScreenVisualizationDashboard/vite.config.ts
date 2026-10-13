import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  // base: '/',
  server:{
      port: 4060,
      host:true,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8099',
          changeOrigin: true,
          secure: false,
        },
        '/uploads': {
          target: 'http://127.0.0.1:8099',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  build: {
    rollupOptions: {
      onwarn(warning, warn) {
        if (warning.code === 'UNRESOLVED_IMPORT') return
        warn(warning)
      }
    }
  }
})