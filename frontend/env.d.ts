/// <reference types="vite/client" />

// Vue 单文件组件模块声明：缺少它时，任何 import ... from '*.vue' 都会报
// TS7016 (Could not find a declaration file)
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

// 通过 .env 文件注入的变量（见 .env.development / .env.production）
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 以下第三方包未随包提供 .d.ts，显式声明以免 TS7016
declare module '@kangc/v-md-editor'
declare module '@kangc/v-md-editor/lib/preview'
declare module '@kangc/v-md-editor/lib/theme/github.js'
