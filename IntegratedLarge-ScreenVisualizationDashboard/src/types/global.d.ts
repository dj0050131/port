declare global {
  interface Window {
    AMap?: any
    _AMapSecurityConfig?: {
      securityJsCode: string
    }
  }
}

export {}

