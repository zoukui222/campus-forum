import request from '@/utils/request'

// 消息类型枚举
export enum MessageType {
  CHAT = 'CHAT',     // 我的消息 (私信)
  COMMENT = 'COMMENT', // 回复我的
  SYSTEM = 'SYSTEM'  // 系统通知
}

export interface MessageVO {
  id: number
  fromId: number
  fromNickname: string
  fromAvatar: string
  content: string
  type: MessageType
  isRead: boolean
  createTime: string
}

export interface MessageQuery {
  pageNum: number
  pageSize: number
  type?: string // 'CHAT' | 'COMMENT' | 'SYSTEM'
}

// 1. 获取消息列表
export function getMessageList(params: MessageQuery) {
  return request({
    url: '/message/list', // baseURL 假设已配置 /api
    method: 'get',
    params
  })
}

// 2. 获取未读消息数
export function getUnreadCount() {
  return request({
    url: '/message/unread-count',
    method: 'get'
  })
}

// 3. 标记消息已读
export function markMessageRead(id: number) {
  return request({
    url: `/message/read/${id}`,
    method: 'post'
  })
}

// 新增：会话列表项类型
export interface ConversationVO {
    userId: number
    nickname: string
    avatar: string
    latestMessage: string
    latestTime: string
    unreadCount: number
  }
  
  // 新增：获取私信会话列表
  export function getConversationList() {
    return request({
      url: '/message/conversations',
      method: 'get'
    })
  }
  
  // 新增：获取与某人的聊天记录
  export function getMessageHistory(partnerId: number) {
    return request({
      url: `/message/history/${partnerId}`,
      method: 'get'
    })
  }
  
  // 新增：发送私信
  export function sendMessage(data: { toUserId: number, content: string }) {
    return request({
      url: '/message/send',
      method: 'post',
      data
    })
  }

  // 批量标记已读
export function batchMarkRead(ids: number[]) {
    return request({
      url: '/message/batch-read',
      method: 'post',
      data: ids // 直接传数组 [1, 2, 3]
    })
  }
  
  // 全部已读 (SYSTEM 和 COMMENT)
  export function markAllRead() {
    return request({
      url: '/message/read-all',
      method: 'post'
    })
  }

  // 未读数统计 VO
  export interface UnreadCountVO {
    total: number
    chat: number
    comment: number
    system: number
  }

  // 获取各类型未读消息数
  export function getUnreadDetails() {
    return request({
      url: '/message/unread-details',
      method: 'get'
    })
  }