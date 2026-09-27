<template>
    <div class="chat-window">
      <div class="chat-sidebar">
        <div class="sidebar-search">
          <el-input prefix-icon="Search" placeholder="搜索联系人" v-model="searchText" />
        </div>
        
        <div class="conversation-list" v-loading="loadingList">
          <div 
            v-for="chat in filteredConversations" 
            :key="chat.userId" 
            class="conversation-item"
            :class="{ active: currentChatUser?.userId === chat.userId }"
            @click="selectChat(chat)"
          >
            <div class="avatar-wrapper">
              <el-avatar :src="chat.avatar || defaultAvatar" :size="40" />
              <span class="badge" v-if="chat.unreadCount > 0">{{ chat.unreadCount }}</span>
            </div>
            
            <div class="info-wrapper">
              <div class="top-row">
                <span class="nickname">{{ chat.nickname }}</span>
                <span class="time">{{ formatTime(chat.latestTime) }}</span>
              </div>
              <div class="latest-msg">{{ chat.latestMessage }}</div>
            </div>
          </div>
        </div>
      </div>
  
      <div class="chat-main">
        <template v-if="currentChatUser">
          <div class="chat-header">
            <span class="title">{{ currentChatUser.nickname }}</span>
          </div>
  
          <div class="message-container" ref="messageBoxRef">
            <div 
              v-for="msg in messageHistory" 
              :key="msg.id" 
              class="message-row"
              :class="{ 'row-self': msg.fromId == userStore.userInfo?.username }"
            >
              <el-avatar 
                v-if="msg.fromId != userStore.userInfo?.username" 
                :src="msg.fromAvatar || defaultAvatar" 
                class="msg-avatar" :size="36" 
              />
              
              <div class="msg-content-wrapper">
                <div class="msg-bubble">{{ msg.content }}</div>
                <div class="msg-time-tip">{{ formatShortTime(msg.createTime) }}</div>
              </div>
  
              <el-avatar 
                v-if="msg.fromId == userStore.userInfo?.id" 
                :src="userStore.userInfo?.avatar || defaultAvatar" 
                class="msg-avatar" :size="36" 
              />
            </div>
          </div>
  
          <div class="chat-input-area">
            <el-input
              v-model="inputContent"
              type="textarea"
              :rows="3"
              placeholder="按 Enter 发送..."
              resize="none"
              @keydown.enter.prevent="handleSend"
            />
            <div class="input-actions">
              <el-button type="primary" size="small" @click="handleSend" :loading="sending">发送</el-button>
            </div>
          </div>
        </template>
  
        <template v-else>
          <div class="empty-chat">
            <el-icon :size="60" color="#e0e0e0"><ChatDotRound /></el-icon>
            <p>选择一个联系人开始聊天</p>
          </div>
        </template>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
  import { Search, ChatDotRound } from '@element-plus/icons-vue'
  import { useUserStore } from '@/store/userStore'
  import { useRoute } from 'vue-router'
  import { 
    getConversationList, 
    getMessageHistory, 
    sendMessage,
    batchMarkRead, // ✅ 引入批量接口
    type ConversationVO, 
    type MessageVO 
  } from '@/api/message'
  
  const userStore = useUserStore()
  const route = useRoute()
  const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  
  const loadingList = ref(false)
  const conversationList = ref<ConversationVO[]>([])
  const searchText = ref('')
  const currentChatUser = ref<ConversationVO | null>(null)
  const messageHistory = ref<MessageVO[]>([])
  const inputContent = ref('')
  const sending = ref(false)
  const messageBoxRef = ref<HTMLElement | null>(null)
  
  let listInterval: any = null
  let chatInterval: any = null
  
  const filteredConversations = computed(() => {
    if (!searchText.value) return conversationList.value
    return conversationList.value.filter(c => c.nickname.includes(searchText.value))
  })
  
  const fetchConversations = async (isBackground = false) => {
    if (!isBackground) loadingList.value = true
    try {
      const res: any = await getConversationList()
      if (res.code === 0 || res.code === 200) {
        conversationList.value = res.data
      }
      // ✅ 新增逻辑：如果是首次加载，且路由有 chatUserId 参数，自动选中
      if (!isBackground && route.query.chatUserId && !currentChatUser.value) {
        const targetId = Number(route.query.chatUserId)
        const targetChat = conversationList.value.find(c => c.userId === targetId)
        if (targetChat) {
          selectChat(targetChat)
        }
        // 注意：如果列表中没有这个人（比如从未聊过天），
        // 理想做法是调用 getUserInfo 拿头像昵称构造一个临时会话对象 push 到列表里。
        // 这里为了简化，假设点击私信后后端已经创建了会话记录，或者你手动构造一个：
        else {
           // 这是一个简单的容错：尝试构造临时会话并选中
           // 实际上最好应该调用 getUserInfo(targetId) 来获取昵称头像
        }
      }
    } finally {
      loadingList.value = false
    }
  }
  
  const selectChat = async (chat: ConversationVO) => {
    if (currentChatUser.value?.userId === chat.userId) return
    currentChatUser.value = chat
    
    const target = conversationList.value.find(c => c.userId === chat.userId)
    if (target) target.unreadCount = 0
  
    if (chatInterval) clearInterval(chatInterval)
    
    messageHistory.value = [] 
    await fetchHistory(chat.userId, false)
    scrollToBottom()
  
    chatInterval = setInterval(() => {
      if (currentChatUser.value) {
        fetchHistory(currentChatUser.value.userId, true)
      }
    }, 2000)
  }
  
  // ✅ 修改：使用批量已读
  const fetchHistory = async (partnerId: number, isBackground = false) => {
    try {
      const res: any = await getMessageHistory(partnerId)
      if (res.code === 0 || res.code === 200) {
        const newHistory = res.data
        const shouldScroll = !isBackground || (newHistory.length > messageHistory.value.length)
        
        messageHistory.value = newHistory
        
        if (shouldScroll) scrollToBottom()
  
        // 1. 筛选出所有未读的、来自对方的消息 ID
        const unreadIds = newHistory
          .filter((m: MessageVO) => !m.isRead && m.fromId == partnerId)
          .map((m: MessageVO) => m.id)
  
        // 2. 如果有未读，调用批量接口
        if (unreadIds.length > 0) {
          // 乐观更新本地状态
          newHistory.forEach((m: MessageVO) => {
            if (unreadIds.includes(m.id)) m.isRead = true
          })
          
          // 发送请求
          await batchMarkRead(unreadIds)
        }
      }
    } catch (e) {
      console.error(e)
    }
  }
  
  const handleSend = async () => {
    const content = inputContent.value.trim()
    if (!content || !currentChatUser.value) return
    
    sending.value = true
    try {
      const res: any = await sendMessage({
        toUserId: currentChatUser.value.userId,
        content: content
      })
      if (res.code === 0 || res.code === 200) {
        await fetchHistory(currentChatUser.value.userId, true)
        inputContent.value = ''
        scrollToBottom()
      }
    } finally {
      sending.value = false
    }
  }
  
  const scrollToBottom = () => {
    nextTick(() => {
      if (messageBoxRef.value) {
        messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
      }
    })
  }
  
  const formatTime = (time: string) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    if (date.toDateString() === now.toDateString()) return time.substring(11, 16)
    return time.substring(5, 10)
  }
  
  const formatShortTime = (time: string) => {
    if (!time) return ''
    return time.replace('T', ' ').substring(5, 16)
  }
  
  onMounted(() => {
    fetchConversations(false)
    listInterval = setInterval(() => { fetchConversations(true) }, 3000)
  })
  
  onUnmounted(() => {
    if (listInterval) clearInterval(listInterval)
    if (chatInterval) clearInterval(chatInterval)
  })
  </script>
  
  <style scoped lang="scss">
  .chat-window {
    display: flex;
    height: 100%; 
    background-color: #fff;
    overflow: hidden;
  
    .chat-sidebar {
      width: 280px;
      border-right: 1px solid #f0f0f0;
      display: flex;
      flex-direction: column;
      background-color: #f7f7f7;
  
      .sidebar-search { padding: 15px; border-bottom: 1px solid #eee; }
  
      .conversation-list {
        flex: 1; overflow-y: auto;
        .conversation-item {
          display: flex; padding: 12px 15px; cursor: pointer; transition: background 0.2s; position: relative;
          &:hover { background-color: #ebebeb; }
          &.active { background-color: #e6f1fc; }
          
          .avatar-wrapper {
            position: relative; margin-right: 12px;
            .badge {
              position: absolute; top: -5px; right: -5px; background-color: #f56c6c; color: #fff;
              font-size: 10px; min-width: 16px; height: 16px; line-height: 16px; text-align: center;
              border-radius: 8px; border: 1px solid #fff; padding: 0 4px;
            }
          }
          
          .info-wrapper {
            flex: 1; overflow: hidden;
            .top-row {
              display: flex; justify-content: space-between; margin-bottom: 4px;
              .nickname { font-size: 14px; color: #333; font-weight: 500; }
              .time { font-size: 12px; color: #999; }
            }
            .latest-msg { font-size: 12px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
          }
        }
      }
    }
  
    .chat-main {
      flex: 1; display: flex; flex-direction: column; background-color: #fff;
      .chat-header { height: 60px; border-bottom: 1px solid #eee; display: flex; align-items: center; padding: 0 20px; .title { font-size: 16px; font-weight: 600; color: #333; } }
  
      .message-container {
        flex: 1; overflow-y: auto; padding: 20px; background-color: #f5f5f5;
        .message-row {
          display: flex; margin-bottom: 20px;
          &.row-self {
            flex-direction: row-reverse;
            .msg-content-wrapper { align-items: flex-end; .msg-bubble { background-color: #409eff; color: #fff; border-top-left-radius: 12px; border-top-right-radius: 2px; } }
            .msg-avatar { margin-left: 10px; margin-right: 0; }
          }
          .msg-avatar { margin-right: 10px; flex-shrink: 0; }
          .msg-content-wrapper {
            display: flex; flex-direction: column; align-items: flex-start; max-width: 70%;
            .msg-bubble { padding: 10px 14px; background-color: #fff; border-radius: 12px; border-top-left-radius: 2px; font-size: 14px; line-height: 1.5; color: #333; box-shadow: 0 1px 2px rgba(0,0,0,0.05); word-break: break-all; }
            .msg-time-tip { margin-top: 4px; font-size: 12px; color: #ccc; }
          }
        }
      }
  
      .chat-input-area {
        border-top: 1px solid #eee; padding: 10px 20px; background-color: #fff;
        :deep(.el-textarea__inner) { border: none; box-shadow: none; padding: 0; resize: none; }
        .input-actions { display: flex; justify-content: flex-end; margin-top: 5px; }
      }
  
      .empty-chat { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #999; font-size: 14px; gap: 10px; }
    }
  }
  </style>