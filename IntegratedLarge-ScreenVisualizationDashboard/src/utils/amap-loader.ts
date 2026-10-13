const AMAP_KEY = import.meta.env.VITE_AMAP_KEY ?? ''
const AMAP_SECURITY = import.meta.env.VITE_AMAP_SECURITY ?? ''
const AMAP_SCRIPT_URL = import.meta.env.VITE_AMAP_SCRIPT_URL ?? ''
const AMAP_SCRIPT_ID = 'amap-sdk-script'
const AMAP_PLUGINS = ['AMap.Scale', 'AMap.ToolBar', 'AMap.ControlBar'] as const

let loaderPromise: Promise<any> | null = null

const appendScript = (): HTMLScriptElement => {
  let script = document.getElementById(AMAP_SCRIPT_ID) as HTMLScriptElement | null
  if (script) {
    return script
  }

  window._AMapSecurityConfig = {
    securityJsCode: AMAP_SECURITY,
  }

  if (!AMAP_SCRIPT_URL) {
    throw new Error('请在 .env 中配置 VITE_AMAP_SCRIPT_URL')
  }
  script = document.createElement('script')
  script.id = AMAP_SCRIPT_ID
  script.type = 'text/javascript'
  script.async = true
  script.src = `${AMAP_SCRIPT_URL}?key=${AMAP_KEY}&plugin=${AMAP_PLUGINS.join(',')}`
  document.head.appendChild(script)
  return script
}

export const loadAmap = async (): Promise<any> => {
  if (typeof window === 'undefined') {
    throw new Error('AMap can only be loaded in browser environments')
  }

  if (window.AMap) {
    return window.AMap
  }

  if (!loaderPromise) {
    loaderPromise = new Promise((resolve, reject) => {
      const script = appendScript()

      const handleLoad = () => {
        if (window.AMap) {
          resolve(window.AMap)
        } else {
          reject(new Error('AMap script loaded but AMap is undefined'))
        }
      }

      const handleError = () => {
        loaderPromise = null
        reject(new Error('Failed to load AMap script'))
      }

      script.addEventListener('load', handleLoad, { once: true })
      script.addEventListener('error', handleError, { once: true })
    })
  }

  return loaderPromise
}

