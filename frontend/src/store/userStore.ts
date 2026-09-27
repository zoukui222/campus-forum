import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // Actions
  // 处理登录逻辑
  const handleLogin = async (loginForm) => {
    try {
      // 调用 API
      const res = await loginApi(loginForm)
      
      // 假设后端返回结构: { token: 'xyz...', user: { ... } }
      // 请根据实际后端返回调整
      const accessToken = res.data.token 
      if (!accessToken) {
        throw new Error('Token获取失败')
      }
      const user = { 
        username: res.data.user.id,
        nickname: res.data.user.nickname,
        avatar: res.data.user.avatar,
        score: res.data.user.score,
        role: res.data.user.role,
        boardId: res.data.user.managedBoardId,
      }

      console.log(user)

      // 更新状态
      token.value = accessToken
      userInfo.value = user

      // 持久化存储
      localStorage.setItem('token', accessToken)
      localStorage.setItem('userInfo', JSON.stringify(user))

      return true
    } catch (error) {
      throw error
    }
  }

  // 登出逻辑
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    handleLogin,
    logout
  }
})