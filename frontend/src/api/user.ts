import request from '@/utils/request'

export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  score: number
  role: string
  createTime: string
}

export interface UserUpdateRequest {
  nickname: string
  avatar?: string
  email?: string
}

// 获取用户信息
export function getUserInfo(id: number) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

// 修改个人信息
export function updateUserInfo(data: UserUpdateRequest) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

export interface UserQuery {
    pageNum: number
    pageSize: number
    keyword?: string // 用于搜索用户名或昵称
  }

// 分页获取用户列表
export function getUserList(params: UserQuery) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

  // 修改密码 请求体
export interface PasswordUpdateRequest {
    oldPassword: string
    newPassword: string
    confirmPassword: string
  }

  // 修改密码
  export function updatePassword(data: PasswordUpdateRequest) {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  }

  // 上传头像
  export function uploadAvatar(file: FormData) {
    return request({
      url: '/user/avatar',
      method: 'post',
      data: file,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }