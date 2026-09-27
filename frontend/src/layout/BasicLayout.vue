<template>
  <div class="app-wrapper">
    <header class="app-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon class="logo-icon"><School /></el-icon>
          <span>校园论坛</span>
        </div>

        <div class="flex-grow"></div>
        <div class="user-actions">
          <div class="message-bell" @click="goToMessageCenter">
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="item">
              <el-icon :size="22"><Bell /></el-icon>
            </el-badge>
          </div>

          <el-button type="primary" round :icon="EditPen" @click="handlePublish">
            发布帖子
          </el-button>

          <el-dropdown class="user-dropdown" @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="username">{{ userStore.userInfo?.nickname || '同学' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item
                    v-if="userStore.userInfo?.role === 'MODERATOR'"
                    command="moderator"
                    divided
                >
                  板主工作台
                </el-dropdown-item>
                <el-dropdown-item
                    v-if="userStore.userInfo?.role === 'ADMIN'"
                    command="admin"
                    divided
                >
                  后台管理
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { School, EditPen, ArrowDown, Bell } from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/message'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 消息轮询
const unreadCount = ref(0)
let pollTimer: any = null

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const res: any = await getUnreadCount()
    if (res.code === 0 || res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('获取未读数失败', error)
  }
}

const goToMessageCenter = () => {
  router.push('/message')
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 5000) // 优化为5秒，更合理
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

// 发布帖子
const handlePublish = () => {
  router.push('/post/create')
}

// 下拉菜单
const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push(`/user/${userStore.userInfo?.username}`)
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'moderator') {
    router.push('/moderator')
  }
}
</script>

<style scoped lang="scss">
.app-wrapper {
  min-height: 100vh;
  // 全局背景与登录注册页渐变统一，整体更高级
  background: linear-gradient(135deg, #f8faff 0%, #eef5ff 100%);
}

/* 顶部导航条：更精致、现代 */
.app-header {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 24px;

    .logo {
      display: flex;
      align-items: center;
      font-size: 22px;
      font-weight: 600;
      color: #5b7bff;
      cursor: pointer;
      gap: 10px;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.03);
      }

      .logo-icon {
        font-size: 26px;
      }
    }

    .flex-grow {
      flex-grow: 1;
    }

    .user-actions {
      display: flex;
      align-items: center;
      gap: 24px;

      /* 消息铃铛 */
      .message-bell {
        cursor: pointer;
        display: flex;
        align-items: center;
        color: #64748b;
        transition: all 0.3s;

        &:hover {
          color: #5b7bff;
          transform: scale(1.08);
        }
      }

      /* 发布按钮 */
      :deep(.el-button--primary) {
        background: linear-gradient(90deg, #5b7bff, #7094ff);
        border: none;
        height: 36px;
        padding: 0 18px;
        font-weight: 500;
        box-shadow: 0 3px 10px rgba(91, 123, 255, 0.2);
      }

      /* 用户下拉 */
      .user-dropdown {
        cursor: pointer;

        .el-dropdown-link {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 4px 8px;
          border-radius: 20px;
          transition: background 0.3s;

          &:hover {
            background: rgba(91, 123, 255, 0.08);
          }
        }

        .username {
          font-weight: 500;
          color: #334155;
          font-size: 14px;
        }
      }
    }
  }
}

/* 内容主体 */
.app-main {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 20px;
}
</style>
