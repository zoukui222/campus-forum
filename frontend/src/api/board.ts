import request from '@/utils/request'

export interface Board {
  id: number
  name: string
  description?: string
  icon?: string
  sort?: number
}

// 获取板块列表
export function getBoardList() {
  return request({
    url: '/public/board/list', 
    method: 'get'
  })
}