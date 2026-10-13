import { createSSRApp } from 'vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)

  try {
    app.config.errorHandler = (err, instance, info) => {
      // eslint-disable-next-line no-console
      console.error('[APP errorHandler]', info, err)
      try {
        const msg = (err && (err.stack || err.message)) ? String(err.stack || err.message) : String(err)
        uni.showModal({
          title: '运行错误',
          content: msg.slice(0, 900),
          showCancel: false,
        })
      } catch (e) {
        // ignore
      }
    }
  } catch (e) {
    // ignore
  }

  try {
    if (typeof uni !== 'undefined' && uni.onUnhandledRejection) {
      uni.onUnhandledRejection((e) => {
        // eslint-disable-next-line no-console
        console.error('[APP unhandledRejection]', e)
        try {
          const reason = e && e.reason ? e.reason : e
          const msg = reason && (reason.stack || reason.message) ? String(reason.stack || reason.message) : String(reason)
          uni.showModal({
            title: 'Promise未捕获',
            content: msg.slice(0, 900),
            showCancel: false,
          })
        } catch (err) {
          // ignore
        }
      })
    }
  } catch (e) {
    // ignore
  }

  return { app }
}

