import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/userStore'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  // 开发环境直连后端 8080；生产环境走同源 /api，由 Nginx 反向代理到后端。
  // 通过 .env.development / .env.production 注入，避免把本机地址打进生产包
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    // 如果有 token，添加到 headers 中
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: unknown) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 业务状态码非 200 视为失败
    if (res.code !== 200) {
      ElMessage.error(res.msg || '系统繁忙，请稍后再试')

      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
      }

      return Promise.reject(new Error(res.msg || 'Error'))
    }

    return res
  },
  (error) => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401: {
          // Token 过期或未登录
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          break
        }
        case 400:
          ElMessage.error(response.data?.message || '请求参数错误')
          break
        case 403:
          ElMessage.error('没有权限执行该操作')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error('网络错误')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

export default service
