import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true } // 标记为游客可访问
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/AdminDashboard.vue'),
    meta: { 
      requiresAuth: true,
      requiresAdmin: true // ✅ 标记需要管理员权限
    }
  },
  {
    path: '/moderator',
    name: 'ModeratorDashboard',
    component: () => import('@/views/moderator/ModeratorDashboard.vue'),
    meta: { 
      requiresAuth: true,
      requiresModerator: true // ✅ 标记需要板主权限
    }
  },
  {
    path: '/',
    // 使用 Layout 组件作为父级
    component: () => import('@/layout/BasicLayout.vue'), 
    meta: { requiresAuth: true },
    children: [
      {
        path: '', // 默认子路由
        name: 'Home',
        component: () => import('@/views/forum/PostList.vue')
      },
      // --- 发布页面 ---
      {
        path: '/post/create',
        name: 'PostCreate',
        component: () => import('@/views/forum/PostCreate.vue'),
        meta: { title: '发布帖子' }
      },
      {
        path: '/post/:id',
        name: 'PostDetail',
        component: () => import('@/views/forum/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: '/message',
        name: 'MessageCenter',
        component: () => import('@/views/user/MessageCenter.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: '/user/:id',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { title: '个人主页' }
      },
      {
        path: '/post/edit/:id', 
        name: 'PostEdit',
        component: () => import('@/views/forum/PostCreate.vue'),
        meta: { title: '编辑帖子' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const isAuthenticated = !!userStore.token
  
    if (to.meta.requiresAuth && !isAuthenticated) {
      next('/login')
    } else if (to.path === '/login' && isAuthenticated) {
      next('/')
    } else if (to.meta.requiresAdmin) {
      // 管理员检查
      if (userStore.userInfo?.role === 'ADMIN') next()
      else { ElMessage.error('无权访问'); next('/') }
    } else if (to.meta.requiresModerator) {
      // 板主检查
      if (userStore.userInfo?.role === 'MODERATOR' ) {
        next()
      } else {
        ElMessage.error('您不是板主，无权访问')
        next('/')
      }
    } else {
      next()
    }
  })
export default router