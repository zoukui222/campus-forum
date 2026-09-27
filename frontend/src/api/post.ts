import request from '@/utils/request'

// 定义帖子数据的类型（根据你的接口文档）
export interface PostVO {
  id: number
  title: string
  content: string // 列表页可能只需要摘要，但接口返回了 content
  boardId: number
  boardName: string
  userId: number
  authorName: string
  authorAvatar: string
  viewCount: number
  replyCount: number
  createTime: string
}

// 定义分页响应结构
export interface PagePostVO {
  records: PostVO[]
  total: number
  size: number
  current: number
  pages: number
}

// 获取帖子列表的参数类型
export interface PostQuery {
  pageNum: number
  pageSize: number
  boardId?: number // 可选，不传则查询全部
  userId?: number
  keyword?: string
}

export interface PostUpdateRequest {
    id: number
    title: string
    content: string
  }

// 获取帖子列表
export function getPostList(params: PostQuery) {
  return request({
    url: '/post/list',
    method: 'get',
    params // axios 会自动将对象转为 ?pageNum=1&pageSize=10...
  })
}

// 定义发布帖子的参数结构
export interface PostCreateRequest {
    title: string;
    content: string;
    boardId: number;
  }
  
  // 发布帖子接口
  export function createPost(data: PostCreateRequest) {
    return request({
      url: '/post/create',
      method: 'post',
      data
    })
  }

  // 获取帖子详情
export function getPostDetail(id: number) {
    return request({
      url: `/post/${id}`, // 注意这里是 restful 风格路径
      method: 'get'
    })
  }
  // 删除帖子
  export function deletePost(id: number) {
    return request({
      url: `/post/${id}`,
      method: 'delete'
    })
  }

  // 编辑帖子
  export function updatePost(data: PostUpdateRequest) {
    return request({
      url: '/post/update',
      method: 'put',
      data
    })
  }