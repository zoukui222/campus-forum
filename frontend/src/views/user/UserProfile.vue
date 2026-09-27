<template>
  <div class="user-profile-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="info-card" shadow="hover">
          <div class="profile-header">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" />
            </div>
            <div class="info-section">
              <h2 class="username">昵称: {{ userInfo.nickname || userInfo.username }}</h2>
              <p class="bio">用户名: {{ userInfo.username }} | 加入时间: {{ formatTime(userInfo.createTime) }}</p>
              <div class="tags">
                <el-tag effect="dark" size="small" type="success">{{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag>
                <el-tag effect="plain" size="small" style="margin-left: 8px">积分: {{ userInfo.score }}</el-tag>
              </div>
            </div>

            <div class="action-section">
              <template v-if="isOwner">
                <el-button type="primary" round :icon="Edit" @click="editDialogVisible = true">
                  编辑资料
                </el-button>
                <el-button type="warning" round :icon="Lock" @click="passwordDialogVisible = true">
                  修改密码
                </el-button>
              </template>

              <el-button v-else type="success" round :icon="ChatDotRound" @click="handleSendMessage">
                私信
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="post-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📚 {{ isOwner ? '我的帖子' : 'Ta 的帖子' }}</span>
            </div>
          </template>

          <div class="post-list">
            <el-empty v-if="postList.length === 0" description="暂无发布记录" />

            <div v-for="post in postList" :key="post.id" class="post-item">
              <div class="post-main" @click="router.push(`/post/${post.id}`)">
                <h3 class="title">{{ post.title }}</h3>
                <p class="summary">{{ post.content ? stripHtml(post.content).substring(0, 100) : '' }}...</p>
                <div class="meta">
                  <span>{{ formatTime(post.createTime) }}</span>
                  <span class="divider">|</span>
                  <span>{{ post.viewCount }} 阅读</span>
                  <span class="divider">|</span>
                  <span>{{ post.replyCount }} 评论</span>
                </div>
              </div>

              <div class="post-actions" v-if="isOwner">
                <el-button link type="primary" :icon="Edit" @click.stop="handleEditPost(post.id)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click.stop="handleDeletePost(post.id)">删除</el-button>
              </div>
            </div>

            <el-pagination
                v-if="total > 0"
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                @current-change="handlePageChange"
                class="pagination"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" center>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-section">
            <div class="current-avatar">
              <el-avatar :size="80" :src="editForm.avatar || defaultAvatar" />
            </div>
            <div class="upload-button">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="handleAvatarUpload"
                         :before-upload="beforeAvatarUpload">
                <el-button type="primary" :icon="Upload">上传本地图片</el-button>
              </el-upload>
            </div>
            <div class="avatar-url-input" style="margin-top: 10px">
              <el-input v-model="editForm.avatar" placeholder="也可直接输入图片链接" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px" center>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleSubmitPassword" :loading="passwordSubmitting">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { getUserInfo, updateUserInfo, updatePassword, uploadAvatar, type UserVO } from '@/api/user'
import { getPostList, deletePost, type PostVO } from '@/api/post'
import { sendMessage } from '@/api/message'
import { Edit, ChatDotRound, Delete, Lock, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import 'element-plus/theme-chalk/display.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const userId = ref<number>(0)
const userInfo = ref<Partial<UserVO>>({})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const postList = ref<PostVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const editDialogVisible = ref(false)
const updating = ref(false)
const editForm = reactive({
  nickname: '',
  avatar: '',
  email: ''
})

const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
})

const isOwner = computed(() => {
  return userStore.userInfo?.username === userId.value
})

const stripHtml = (html: string) => {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '')
}

const initData = async () => {
  userId.value = Number(route.params.id)
  if (!userId.value) return

  loading.value = true
  try {
    const userRes: any = await getUserInfo(userId.value)
    if (userRes.code === 0 || userRes.code === 200) {
      userInfo.value = userRes.data
      editForm.nickname = userRes.data.nickname || ''
      editForm.avatar = userRes.data.avatar || ''
      editForm.email = userRes.data.email || ''
    }
    await fetchPosts()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchPosts = async () => {
  const postRes: any = await getPostList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    userId: userId.value
  })
  if (postRes.code === 0 || postRes.code === 200) {
    postList.value = postRes.data.records
    total.value = postRes.data.total
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) initData()
})

const handleUpdateProfile = async () => {
  updating.value = true
  try {
    const res: any = await updateUserInfo(editForm)
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      userInfo.value.nickname = editForm.nickname
      userInfo.value.avatar = editForm.avatar
      userInfo.value.email = editForm.email
      if (isOwner.value) {
        userStore.userInfo.nickname = editForm.nickname
        userStore.userInfo.avatar = editForm.avatar
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
    }
  } finally {
    updating.value = false
  }
}

const handleSubmitPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordSubmitting.value = true
      try {
        const res: any = await updatePassword(passwordForm)
        if (res.code === 0 || res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          passwordDialogVisible.value = false
          userStore.logout()
        }
      } catch (error) {
        console.error(error)
      } finally {
        passwordSubmitting.value = false
      }
    }
  })
}

const beforeAvatarUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/bmp'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJpgOrPng) ElMessage.error('只能上传 JPG、PNG、GIF、BMP 格式的图片!')
  if (!isLt2M) ElMessage.error('上传图片大小不能超过 2MB!')
  return isJpgOrPng && isLt2M
}

const handleAvatarUpload = async (file: any) => {
  const formData = new FormData()
  formData.append('file', file.file)
  try {
    const res: any = await uploadAvatar(formData)
    if (res.code === 0 || res.code === 200) {
      editForm.avatar = res.data.url
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

const handleSendMessage = async () => {
  try {
    await sendMessage({ toUserId: userId.value, content: 'hello，可以聊一聊吗？' })
    ElMessage.success('已打招呼，正在跳转聊天...')
    router.push({ path: '/message', query: { chatUserId: userId.value } })
  } catch (e) { console.error(e) }
}

const handleDeletePost = (id: number) => {
  ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', { type: 'warning' })
      .then(async () => {
        const res: any = await deletePost(id)
        if (res.code === 0 || res.code === 200) {
          ElMessage.success('删除成功')
          fetchPosts()
        }
      })
}

const handleEditPost = (id: number) => {
  router.push(`/post/edit/${id}`)
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchPosts()
}

const formatTime = (time: string | undefined) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}

onMounted(() => {
  initData()
})
</script>

<style scoped lang="scss">
.user-profile-container {
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 20px;
}

/* 卡片统一样式 */
.info-card, .post-card {
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.profile-header {
  display: flex;
  align-items: center;
  padding: 28px 24px;
  flex-wrap: wrap;
  gap: 24px;
}

.avatar-section {
  margin-right: 20px;
}

.info-section {
  flex: 1;
  min-width: 200px;
}

.username {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.bio {
  color: #64748b;
  font-size: 15px;
  margin-bottom: 14px;
}

.action-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.action-section .el-button {
  width: 130px;
  border-radius: 20px;
}

/* 帖子列表 */
.post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 10px;
  border-bottom: 1px solid #f5f7fa;
  transition: all 0.25s ease;
}

.post-item:hover {
  background: #f8faff;
  border-radius: 12px;
  padding-left: 8px;
}

.post-main {
  flex: 1;
  cursor: pointer;
}

.title {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  transition: color 0.2s;
}

.post-main:hover .title {
  color: #5b7bff;
}

.summary {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 10px;
}

.meta {
  font-size: 13px;
  color: #94a3b8;
}

.divider {
  margin: 0 8px;
  color: #eee;
}

.post-actions {
  margin-left: 20px;
  display: flex;
  gap: 8px;
}

.pagination {
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(90deg, #5b7bff, #7094ff);
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.el-button {
  margin-left: unset !important;
}
</style>
