<template>
  <div class="auth-container">
    <el-card class="auth-card" shadow="hover">
      <template #header>
        <h2 class="auth-title">校园论坛系统登录</h2>
      </template>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-width="0"
          size="large"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              @keyup.enter="handleSubmit"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleSubmit"
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
          >
            立即登录
          </el-button>
        </el-form-item>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">去注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const formEl = loginFormRef.value
  if (!formEl) return

  try {
    await formEl.validate()
    loading.value = true
    await userStore.handleLogin(loginForm)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败：', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loginFormRef.value?.focus?.('username')
})
</script>

<style scoped lang="scss">
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  padding: 0 20px;
  background: url('../../assets/imgs/login-bg.jpeg') no-repeat center center;
  /* 关键：让背景图完全铺满容器，且保持比例 */
  background-size: cover;
  /* 防止滚动时背景图抖动 */
  background-attachment: fixed;
}

.auth-card {
  width: 100%;
  max-width: 440px;
  border-radius: 20px;
  overflow: hidden;
  /* 卡片柔和阴影，更高级 */
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 28px 80px rgba(80, 110, 255, 0.15);
  }

  :deep(.el-card__header) {
    padding: 28px 0 20px;
    border-bottom: none;
  }

  .auth-title {
    text-align: center;
    margin: 0;
    font-size: 24px;
    color: #2c3e50;
    font-weight: 600;
    letter-spacing: 1px;
  }

  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    height: 48px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 12px;
    font-weight: 500;
    letter-spacing: 1px;
    margin-top: 8px;
  }

  .auth-footer {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #7f8c8d;
    margin-top: 16px;
  }
}
</style>
