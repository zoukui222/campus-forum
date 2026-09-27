// 认证相关接口
import request from '@/utils/request'

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  nickname: string
}

/** 登录 */
export function login(data: LoginForm): Promise<any> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/** 注册 */
export function register(data: RegisterForm): Promise<any> {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}
