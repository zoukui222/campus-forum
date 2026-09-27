// src/api/comment.ts
import request from '@/utils/request'

export interface CommentVO {
  id: number
  postId: number
  content: string
  userId: number
  authorName: string
  authorAvatar: string
  parentId: number
  childCount: number
  createTime: string
  // 前端辅助字段，不需要后端返回
  showReplies?: boolean 
  subComments?: CommentVO[]
  loadingReplies?: boolean
}

export interface CommentCreateRequest {
  postId: number
  content: string
  parentId?: number
}

// 1. 获取一级评论列表
export function getCommentList(postId: number) {
  return request({
    url: `/comment/list/${postId}`,
    method: 'get'
  })
}

// 2. 获取子评论 (楼中楼)
export function getSubCommentList(rootId: number) {
  return request({
    url: `/comment/sub-list/${rootId}`,
    method: 'get'
  })
}

// 3. 发表评论
export function createComment(data: CommentCreateRequest) {
  return request({
    url: '/comment/create',
    method: 'post',
    data
  })
}

// 4. 删除评论
export function deleteComment(id: number) {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}