<template>
  <div class="post-detail-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="17" :xs="24">
        <el-card class="post-card" shadow="hover" v-if="post">
          <div class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-meta">
              <el-tag size="small" class="board-tag">{{ post.boardName }}</el-tag>
              <span class="meta-text">作者: {{ post.authorName }}</span>
              <span class="meta-text">发布于: {{ formatTime(post.createTime) }}</span>
              <span class="meta-text">阅读: {{ post.viewCount }}</span>
            </div>
          </div>

          <el-divider class="custom-divider" />

          <v-md-preview :text="post.content" class="md-preview"></v-md-preview>
        </el-card>

        <el-card class="comment-card" shadow="hover">
          <div class="comment-header">
            <h3>评论 ({{ post?.replyCount || 0 }})</h3>
          </div>

          <div class="comment-input-area">
            <el-input
                v-model="mainCommentContent"
                type="textarea"
                :rows="3"
                placeholder="写下你的评论..."
                resize="none"
                class="comment-input"
            />
            <div class="input-actions">
              <el-button type="primary" @click="submitMainComment" :loading="submitting">发表评论</el-button>
            </div>
          </div>

          <div class="comment-list">
            <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发" />

            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="avatar-col">
                <el-avatar :src="comment.authorAvatar || defaultAvatar" class="clickable-avatar" @click.stop="goToUser(comment.userId)" />
              </div>

              <div class="content-col">
                <div class="user-info">
                  <span class="clickable-name" @click="goToUser(comment.userId)">{{ comment.authorName }}</span>
                  <span class="time">{{ formatTime(comment.createTime) }}</span>
                </div>

                <div class="comment-text">{{ comment.content }}</div>

                <div class="action-bar">
                  <el-button link type="primary" size="small" @click="openReplyBox(comment)">回复</el-button>
                  <el-button
                      v-if="userStore.userInfo?.username === comment.userId"
                      link type="danger" size="small"
                      @click="handleDelete(comment.id)"
                  >删除</el-button>
                </div>

                <div class="sub-comment-wrapper" v-if="comment.childCount > 0 || (comment.subComments && comment.subComments.length > 0)">
                  <div v-if="comment.showReplies" class="sub-list">
                    <div v-for="sub in comment.subComments" :key="sub.id" class="sub-item">
                      <span class="sub-user">{{ sub.authorName }}</span> :
                      <span class="sub-content">{{ sub.content }}</span>

                      <div class="sub-actions">
                        <span class="sub-time">{{ formatTime(sub.createTime) }}</span>
                        <el-button link size="small" @click="openReplyBox(comment, sub)">回复</el-button>
                        <el-button
                            v-if="userStore.userInfo?.username === sub.userId"
                            link type="danger" size="small"
                            @click="handleDelete(sub.id, comment)"
                        >删除</el-button>
                      </div>
                    </div>
                  </div>

                  <div class="expand-btn" v-if="comment.childCount > 0">
                    <el-button
                        v-if="!comment.showReplies"
                        link type="info"
                        @click="fetchSubComments(comment)"
                        :loading="comment.loadingReplies"
                    >
                      查看 {{ comment.childCount }} 条回复 <el-icon><ArrowDown /></el-icon>
                    </el-button>
                    <el-button
                        v-else
                        link type="info"
                        @click="comment.showReplies = false"
                    >
                      收起回复 <el-icon><ArrowUp /></el-icon>
                    </el-button>
                  </div>
                </div>

                <div v-if="activeReplyId === comment.id" class="inline-reply-box">
                  <el-input
                      v-model="replyContent"
                      :placeholder="replyPlaceholder"
                      size="small"
                      @keyup.enter="submitReply(comment)"
                      class="reply-input"
                  >
                    <template #append>
                      <el-button @click="submitReply(comment)" :loading="replySubmitting">发送</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="7" class="hidden-xs-only">
        <el-card shadow="hover" v-if="post" class="author-card-card">
          <template #header>作者信息</template>
          <div class="author-card">
            <el-avatar :size="60" :src="post.authorAvatar || defaultAvatar" class="clickable-avatar" @click="goToUser(post.userId)" />
            <h3 class="clickable-name" @click="goToUser(post.userId)">{{ post.authorName }}</h3>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { getPostDetail, type PostVO } from '@/api/post'
import {
  getCommentList,
  getSubCommentList,
  createComment,
  deleteComment,
  type CommentVO
} from '@/api/comment'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/theme-chalk/display.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postId = Number(route.params.id)

const loading = ref(false)
const post = ref<PostVO | null>(null)
const comments = ref<CommentVO[]>([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const mainCommentContent = ref('')
const submitting = ref(false)
const activeReplyId = ref<number | null>(null)
const replyContent = ref('')
const replyPlaceholder = ref('')
const replyTargetId = ref<number | null>(null)
const replySubmitting = ref(false)

const formatTime = (timeStr: string | undefined) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const goToUser = (userId: number) => {
  if (userId) router.push(`/user/${userId}`)
}

const initData = async () => {
  loading.value = true
  try {
    const postRes: any = await getPostDetail(postId)
    if (postRes.code === 0 || postRes.code === 200) post.value = postRes.data
    await refreshComments()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const refreshComments = async () => {
  const res: any = await getCommentList(postId)
  if (res.code === 0 || res.code === 200) {
    comments.value = res.data.map((item: CommentVO) => ({
      ...item,
      showReplies: false,
      subComments: [],
      loadingReplies: false
    }))
  }
}

const fetchSubComments = async (comment: CommentVO) => {
  comment.loadingReplies = true
  try {
    const res: any = await getSubCommentList(comment.id)
    if (res.code === 0 || res.code === 200) {
      comment.subComments = res.data
      comment.showReplies = true
    }
  } catch (error) {
    console.error(error)
  } finally {
    comment.loadingReplies = false
  }
}

const submitMainComment = async () => {
  if (!mainCommentContent.value.trim()) return ElMessage.warning('请输入内容')
  submitting.value = true
  try {
    const res: any = await createComment({
      postId: postId,
      content: mainCommentContent.value,
      parentId: 0
    })
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('评论成功')
      mainCommentContent.value = ''
      await refreshComments()
      const pRes: any = await getPostDetail(postId)
      if (pRes.data) post.value = pRes.data
    }
  } finally {
    submitting.value = false
  }
}

const openReplyBox = (rootComment: CommentVO, targetSub?: CommentVO) => {
  if (activeReplyId.value === rootComment.id && replyTargetId.value === (targetSub?.id || rootComment.id)) {
    activeReplyId.value = null
    return
  }
  activeReplyId.value = rootComment.id
  replyContent.value = ''
  if (targetSub) {
    replyTargetId.value = targetSub.id
    replyPlaceholder.value = `回复 @${targetSub.authorName}`
  } else {
    replyTargetId.value = rootComment.id
    replyPlaceholder.value = `回复 @${rootComment.authorName}`
  }
}

const submitReply = async (rootComment: CommentVO) => {
  if (!replyContent.value.trim()) return ElMessage.warning('请输入内容')
  if (!replyTargetId.value) return
  replySubmitting.value = true
  try {
    const res: any = await createComment({
      postId: postId,
      content: replyContent.value,
      parentId: replyTargetId.value
    })
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('回复成功')
      replyContent.value = ''
      activeReplyId.value = null
      await fetchSubComments(rootComment)
    }
  } finally {
    replySubmitting.value = false
  }
}

const handleDelete = (id: number, parentComment?: CommentVO) => {
  ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' }).then(async () => {
    const res: any = await deleteComment(id)
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('删除成功')
      parentComment ? fetchSubComments(parentComment) : refreshComments()
    }
  })
}

onMounted(() => {
  initData()
})
</script>

<style scoped lang="scss">
.post-detail-container {
  padding: 12px 0 40px;
}

/* 主帖子卡片 - 高级风格 */
.post-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  .post-header {
    padding: 8px 0;

    .post-title {
      font-size: 26px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 14px;
      line-height: 1.4;
    }

    .post-meta {
      color: #64748b;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 14px;
      flex-wrap: wrap;
    }

    .board-tag {
      background: linear-gradient(90deg, #eef2ff, #f0f5ff);
      color: #5b7bff;
      border: none;
      font-weight: 500;
    }
  }
}

.custom-divider {
  margin: 20px 0;
  border-color: #f0f5ff;
}

/* 预览内容样式 */
.md-preview {
  color: #334155;
  line-height: 1.7;
  font-size: 15px;
}

/* 评论卡片 */
.comment-card {
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  .comment-header {
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }

  /* 评论输入框 */
  .comment-input-area {
    margin-bottom: 28px;
    background: #f8faff;
    padding: 18px;
    border-radius: 12px;

    .comment-input {
      :deep(.el-input__wrapper) {
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      }
    }

    .input-actions {
      margin-top: 12px;
      text-align: right;

      .el-button {
        border-radius: 8px;
      }
    }
  }

  /* 评论列表 */
  .comment-item {
    display: flex;
    gap: 16px;
    padding: 20px 0;
    border-bottom: 1px solid #f5f7fa;
    transition: all 0.2s ease;

    &:hover {
      background: #fafbfc;
      border-radius: 12px;
      padding-left: 8px;
    }

    .avatar-col {
      flex-shrink: 0;
    }

    .content-col {
      flex-grow: 1;

      .user-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        align-items: center;

        .clickable-name {
          font-weight: 600;
          font-size: 15px;
          color: #334155;
          cursor: pointer;
          transition: color 0.2s;

          &:hover {
            color: #5b7bff;
          }
        }

        .time {
          color: #94a3b8;
          font-size: 13px;
        }
      }

      .comment-text {
        font-size: 15px;
        color: #334155;
        line-height: 1.6;
        margin-bottom: 12px;
      }

      .action-bar {
        margin-bottom: 12px;
      }

      /* 楼中楼 */
      .sub-comment-wrapper {
        background-color: #f7f8fa;
        padding: 14px;
        border-radius: 10px;
        margin-top: 12px;

        .sub-item {
          font-size: 14px;
          padding: 8px 0;
          line-height: 1.5;
          border-bottom: 1px dashed #eaecef;

          &:last-child {
            border-bottom: none;
          }

          .sub-user {
            font-weight: 600;
            color: #475569;
          }

          .sub-content {
            color: #334155;
            margin-left: 4px;
          }

          .sub-actions {
            margin-top: 4px;
            font-size: 12px;
            color: #94a3b8;
            display: flex;
            align-items: center;
            gap: 12px;
          }
        }

        .expand-btn {
          margin-top: 10px;
          padding-top: 10px;
          border-top: 1px dashed #e0e0e0;
        }
      }

      .inline-reply-box {
        margin-top: 12px;

        .reply-input {
          :deep(.el-input__wrapper) {
            border-radius: 8px;
          }
        }
      }
    }
  }
}

/* 作者卡片 */
.author-card-card {
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }
}

.author-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;

  h3 {
    margin-top: 12px;
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
  }
}

/* 可点击头像 */
.clickable-avatar {
  cursor: pointer;
  transition: transform 0.25s ease;

  &:hover {
    transform: scale(1.12);
  }
}
</style>
