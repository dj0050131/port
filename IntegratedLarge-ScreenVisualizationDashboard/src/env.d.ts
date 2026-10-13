
interface ImportMetaEnv {
  readonly VITE_AMAP_KEY: string;
  readonly VITE_AMAP_SECURITY: string;
  readonly VITE_API_BASE_URL: string;
  readonly VITE_PORT_DATA_CENTER_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
