<template>
  <div class="auth-container">
    <el-card class="auth-card" shadow="hover">
      <template #header>
        <h2 class="auth-title">注册新账号</h2>
      </template>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          label-width="0"
          size="large"
      >
        <el-form-item prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
              @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称"
              :prefix-icon="User"
              clearable
              @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="设置密码"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
          >
            立即注册
          </el-button>
        </el-form-item>

        <div class="auth-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

// 验证确认密码
const validatePass2 = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致！'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 15, message: '昵称长度在 2 到 15 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 提交注册
const handleSubmit = async () => {
  const formEl = registerFormRef.value
  if (!formEl) return

  try {
    await formEl.validate()
    loading.value = true

    const payload = {
      username: registerForm.username,
      nickname: registerForm.nickname,
      password: registerForm.password
    }

    await register(payload)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败：', error)
  } finally {
    loading.value = false
  }
}

// 自动聚焦
onMounted(() => {
  registerFormRef.value?.focus?.('username')
})
</script>

<style scoped lang="scss">
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 与登录页完全一致的渐变背景 */
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
