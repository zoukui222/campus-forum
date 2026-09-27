import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/userStore'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080/api', // 根据你的后端地址设置
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 如果有 token，添加到 headers 中
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 1. 获取后端返回的数据主体
    const res = response.data

    // 2. 判断业务状态码 (假设后端约定 200 为成功，请根据实际情况调整，如 0 或 1)
    // 注意：这里 res.code 是业务逻辑的状态码，response.status 是 HTTP 状态码
    if (res.code !== 200) {
      
      // 3. 统一提示错误信息
      ElMessage.error(res.msg || '系统繁忙，请稍后再试')

      // 4. 处理特定的业务错误 (例如：Token 虽有效但被后台强制踢出，或者业务层面的 401)
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
      }

      // 5. 抛出错误，阻断后续 .then() 执行，进入 .catch()
      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 6. 业务成功，只返回数据部分 (或者根据需要返回 res)
    return res
  },
  (error) => {
    // 处理 HTTP 错误状态码
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401:
          // Token 过期或未登录
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          break
        case 400:
          ElMessage.error(response.data.message || '请求参数错误')
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