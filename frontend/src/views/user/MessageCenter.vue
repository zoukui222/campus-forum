<template>
  <div class="message-center-container">
    <el-card class="message-layout-card" :body-style="{ padding: '0px', height: '100%' }">
      <div class="message-layout">

        <!-- 左侧导航 -->
        <div class="sidebar">
          <div class="sidebar-header">
            <el-icon><Promotion /></el-icon>
            消息中心
          </div>
          <ul class="nav-menu">
            <li
                :class="['nav-item', { active: activeTab === 'CHAT' }]"
                @click="changeTab('CHAT')"
            >
              我的消息
              <span class="count-badge" v-if="unreadMap.CHAT > 0">
                {{ unreadMap.CHAT > 99 ? '99+' : unreadMap.CHAT }}
              </span>
            </li>
            <li
                :class="['nav-item', { active: activeTab === 'COMMENT' }]"
                @click="changeTab('COMMENT')"
            >
              回复我的
              <span class="count-badge" v-if="unreadMap.COMMENT > 0">
                {{ unreadMap.COMMENT > 99 ? '99+' : unreadMap.COMMENT }}
              </span>
            </li>
            <li
                :class="['nav-item', { active: activeTab === 'SYSTEM' }]"
                @click="changeTab('SYSTEM')"
            >
              系统通知
              <span class="count-badge" v-if="unreadMap.SYSTEM > 0">
                {{ unreadMap.SYSTEM > 99 ? '99+' : unreadMap.SYSTEM }}
              </span>
            </li>
          </ul>
        </div>

        <!-- 聊天窗口 -->
        <div class="chat-wrapper" v-if="activeTab === 'CHAT'">
          <ChatWindow />
        </div>

        <!-- 消息列表 -->
        <div class="content-area" v-else v-loading="loading">
          <div class="content-header">
            <span class="header-title">{{ getTitle() }}</span>
            <el-button
                type="primary"
                link
                size="small"
                @click="handleReadAll"
                :loading="readingAll"
            >
              <el-icon style="margin-right: 4px"><Brush /></el-icon>
              一键已读
            </el-button>
          </div>

          <div class="message-list">
            <el-empty v-if="messageList.length === 0" description="暂无消息" />

            <div
                v-for="msg in messageList"
                :key="msg.id"
                class="message-item"
                :class="{ 'unread': !msg.isRead }"
                @click="handleMessageClick(msg)"
            >
              <template v-if="msg.type === 'COMMENT'">
                <el-avatar :size="40" :src="msg.fromAvatar || defaultAvatar" class="avatar" />
                <div class="msg-body">
                  <div class="msg-top">
                    <span class="nickname">{{ msg.fromNickname }}</span>
                    <span class="action-text">回复了你的评论</span>
                  </div>
                  <div class="msg-content">{{ parseCOMMENTMsg(msg.content).text }}</div>
                  <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </template>

              <template v-if="msg.type === 'SYSTEM'">
                <div class="system-icon">
                  <el-icon><BellFilled /></el-icon>
                </div>
                <div class="msg-body">
                  <div class="msg-top">
                    <span class="system-title">系统通知</span>
                  </div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </template>

              <div class="unread-dot" v-if="!msg.isRead"></div>
            </div>

            <div class="pagination-box" v-if="total > 0">
              <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="total"
                  :page-size="pageSize"
                  @current-change="handlePageChange"
              />
            </div>
          </div>
        </div>

      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, onUnmounted } from 'vue'
import { Promotion, BellFilled, Brush } from '@element-plus/icons-vue'
import {
  getMessageList,
  markMessageRead,
  markAllRead,
  getUnreadDetails,
  type MessageVO
} from '@/api/message'
import { useUserStore } from '@/store/userStore'
import { useRouter } from 'vue-router'
import ChatWindow from './components/ChatWindow.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const readingAll = ref(false)
const activeTab = ref('CHAT')
const messageList = ref<MessageVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

let statsTimer: any = null

const unreadMap = reactive<Record<string, number>>({
  CHAT: 0,
  COMMENT: 0,
  SYSTEM: 0
})

const parseCOMMENTMsg = (content: string) => {
  const regex = /^POST:(\d+):(.*)$/
  const match = content.match(regex)
  if (match) return { postId: match[1], text: match[2] }
  return { postId: null, text: content }
}

const changeTab = (tab: string) => {
  activeTab.value = tab
  if (tab !== 'CHAT') {
    pageNum.value = 1
    fetchMessages()
  }
}

const getTitle = () => {
  const map: Record<string, string> = {
    CHAT: '我的消息',
    COMMENT: '回复我的',
    SYSTEM: '系统通知'
  }
  return map[activeTab.value]
}

const fetchUnreadStats = async () => {
  try {
    const res: any = await getUnreadDetails()
    if (res.code === 0 || res.code === 200) {
      const data = res.data
      unreadMap.CHAT = data.chat
      unreadMap.COMMENT = data.comment
      unreadMap.SYSTEM = data.system
    }
  } catch (e) {
    console.error('获取未读数失败', e)
  }
}

const fetchMessages = async () => {
  if (activeTab.value === 'CHAT') return
  loading.value = true
  try {
    const res: any = await getMessageList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: activeTab.value
    })
    if (res.code === 0 || res.code === 200) {
      messageList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleReadAll = async () => {
  readingAll.value = true
  try {
    const res: any = await markAllRead()
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('已全部标记为已读')
      fetchMessages()
      fetchUnreadStats()
    }
  } catch (error) {
    console.error(error)
  } finally {
    readingAll.value = false
  }
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchMessages()
}

const handleMessageClick = async (msg: MessageVO) => {
  if (!msg.isRead) {
    msg.isRead = true
    if (unreadMap[msg.type] > 0) unreadMap[msg.type]--
    try { await markMessageRead(msg.id) } catch (e) { console.error(e) }
  }
  if (msg.type === 'COMMENT') {
    const { postId } = parseCOMMENTMsg(msg.content)
    if (postId) router.push(`/post/${postId}`)
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchUnreadStats()
  if (activeTab.value !== 'CHAT') fetchMessages()
  statsTimer = setInterval(fetchUnreadStats, 3000)
})

onUnmounted(() => {
  if (statsTimer) clearInterval(statsTimer)
})
</script>

<style scoped lang="scss">
.message-center-container {
  max-width: 1000px;
  margin: 24px auto;
  height: 620px;
  padding: 0 20px;
}

/* 主卡片统一风格 */
.message-layout-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: none;
}

.message-layout {
  display: flex;
  height: 100%;

  /* 左侧边栏 */
  .sidebar {
    width: 200px;
    border-right: 1px solid #f0f5ff;
    background: #fafbff;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
      gap: 8px;
      border-bottom: 1px solid #eef2ff;
    }

    .nav-menu {
      list-style: none;
      padding: 12px 0;
      margin: 0;
      flex: 1;

      .nav-item {
        padding: 16px 20px;
        cursor: pointer;
        color: #64748b;
        font-size: 14px;
        position: relative;
        transition: all 0.25s ease;
        display: flex;
        align-items: center;
        justify-content: space-between;

        &:hover {
          background: #eef2ff;
          color: #5b7bff;
        }

        &.active {
          color: #5b7bff;
          background: linear-gradient(90deg, #eef2ff, #f8faff);
          border-right: 3px solid #5b7bff;
          font-weight: 500;
        }

        .count-badge {
          background: #f43f5e;
          color: #fff;
          font-size: 12px;
          min-width: 18px;
          height: 18px;
          line-height: 18px;
          text-align: center;
          padding: 0 5px;
          border-radius: 10px;
          font-weight: bold;
          transform: scale(0.9);
        }
      }
    }
  }

  /* 聊天区域 */
  .chat-wrapper {
    flex: 1;
    height: 100%;
    overflow: hidden;

    :deep(.chat-window) {
      height: 100%;
      border: none;
      border-radius: 0;
    }
  }

  /* 右侧内容区 */
  .content-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;

    .content-header {
      height: 64px;
      line-height: 64px;
      padding: 0 24px;
      border-bottom: 1px solid #f0f5ff;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #2c3e50;
      }
    }

    .message-list {
      padding: 0 24px;
      flex: 1;
      overflow-y: auto;

      .message-item {
        padding: 20px 0;
        border-bottom: 1px solid #f5f7fa;
        display: flex;
        gap: 15px;
        position: relative;
        cursor: pointer;
        transition: background 0.25s ease;

        &:hover {
          background: #f8faff;
          border-radius: 12px;
          padding-left: 8px;
        }

        &.unread {
          background: #fef1f2;
        }

        .avatar {
          flex-shrink: 0;
        }

        .system-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: #eef2ff;
          color: #5b7bff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
        }

        .msg-body {
          flex: 1;

          .msg-top {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            gap: 10px;

            .nickname {
              font-weight: 600;
              font-size: 14px;
              color: #334155;
            }

            .action-text {
              color: #94a3b8;
              font-size: 13px;
            }

            .system-title {
              font-weight: 600;
              color: #334155;
            }
          }

          .msg-content {
            font-size: 14px;
            color: #64748b;
            line-height: 1.5;
          }

          .msg-time {
            margin-top: 8px;
            font-size: 12px;
            color: #cbd5e1;
          }
        }

        .unread-dot {
          position: absolute;
          top: 22px;
          right: 4px;
          width: 8px;
          height: 8px;
          background: #f43f5e;
          border-radius: 50%;
        }
      }
    }
  }
}

.pagination-box {
  padding: 20px 0;
  display: flex;
  justify-content: center;

  :deep(.el-pagination.is-background .el-pager li.is-active) {
    background: linear-gradient(90deg, #5b7bff, #7094ff);
  }
}
</style>
