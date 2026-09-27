<template>
  <div class="create-post-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="header-title">{{ isEditMode ? '📝 编辑帖子' : '📝 发布新帖子' }}</span>
          <el-button text @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" v-loading="loading">
        <el-row :gutter="20">
          <el-col :span="6" :xs="24">
            <el-form-item label="选择板块" prop="boardId">
              <el-select
                  v-model="form.boardId"
                  placeholder="请选择发帖板块"
                  style="width: 100%"
                  :disabled="isEditMode"
                  class="custom-select"
              >
                <el-option v-for="board in boardList" :key="board.id" :label="board.name" :value="board.id" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="18" :xs="24">
            <el-form-item label="帖子标题" prop="title">
              <el-input
                  v-model="form.title"
                  placeholder="请输入标题"
                  maxlength="50"
                  show-word-limit
                  class="custom-input"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="内容正文" prop="content">
          <v-md-editor v-model="form.content" height="500px" placeholder="请输入正文..." />
        </el-form-item>

        <div class="form-footer">
          <el-button class="cancel-btn" @click="$router.back()">取消</el-button>
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="submitting">
            {{ isEditMode ? '保存修改' : '立即发布' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createPost, updatePost, getPostDetail } from '@/api/post'
import { getBoardList, type Board } from '@/api/board'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)
const boardList = ref<Board[]>([])

const isEditMode = computed(() => !!route.params.id)

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  boardId: undefined as number | undefined,
  content: ''
})

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 50, message: '长度5-50', trigger: 'blur' }
  ],
  boardId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
})

onMounted(async () => {
  const boardRes: any = await getBoardList()
  if (boardRes.code === 0 || boardRes.code === 200) {
    boardList.value = boardRes.data
  }

  if (isEditMode.value) {
    loading.value = true
    try {
      const postId = Number(route.params.id)
      const res: any = await getPostDetail(postId)
      if (res.code === 0 || res.code === 200) {
        form.id = res.data.id
        form.title = res.data.title
        form.boardId = res.data.boardId
        form.content = res.data.content
      }
    } finally {
      loading.value = false
    }
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEditMode.value) {
          await updatePost({
            id: form.id!,
            title: form.title,
            content: form.content
          })
          ElMessage.success('修改成功')
          router.push(`/user/${JSON.parse(localStorage.getItem('userInfo') || '{}').username || ''}`)
        } else {
          await createPost({
            title: form.title,
            content: form.content,
            boardId: form.boardId!
          })
          ElMessage.success('发布成功')
          router.push('/')
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.create-post-container {
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 20px;
}

/* 主卡片 - 统一高级样式 */
.box-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(91, 123, 255, 0.1);
  }

  /* 头部标题 */
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-size: 19px;
      font-weight: 600;
      color: #2c3e50;
      letter-spacing: 0.5px;
    }
  }
}

/* 统一输入框样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 15px;
  margin-bottom: 6px;
}

.custom-input {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    height: 46px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 3px 12px rgba(91, 123, 255, 0.1);
    }
  }
}

.custom-select {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    height: 46px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  }
}

/* 底部按钮 */
.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
  gap: 14px;
}

.cancel-btn {
  border-radius: 10px;
  height: 42px;
  padding: 0 20px;
}

.submit-btn {
  border-radius: 10px;
  height: 42px;
  padding: 0 24px;
  font-weight: 500;
  background: linear-gradient(90deg, #5b7bff, #7094ff);
  border: none;
}
</style>
