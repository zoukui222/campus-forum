<template>
  <div class="forum-container">
    <el-row :gutter="20">
      <el-col :span="17" :xs="24">
        <el-card class="post-list-card" shadow="hover">

          <div class="toolbar">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
              <el-tab-pane label="全部" :name="0"></el-tab-pane>
              <el-tab-pane
                  v-for="board in boardList"
                  :key="board.id"
                  :label="board.name"
                  :name="board.id"
              />
            </el-tabs>

            <div class="search-box">
              <el-input
                  v-model="queryParams.keyword"
                  placeholder="搜索帖子..."
                  prefix-icon="Search"
                  clearable
                  @clear="handleSearch"
                  @keyup.enter="handleSearch"
                  style="width: 220px"
              />
              <el-button :icon="Search" circle @click="handleSearch" style="margin-left: 8px" />
            </div>
          </div>

          <div class="post-items" v-loading="loading">
            <el-empty v-if="postList.length === 0 && !loading" description="暂无相关帖子" />

            <div v-for="post in postList" :key="post.id" class="post-item" @click="goToDetail(post.id)">
              <h3 class="post-title">
                <el-tag size="small" class="board-tag" v-if="post.boardName">
                  {{ post.boardName }}
                </el-tag>
                {{ post.title }}
              </h3>
              <p class="post-summary">{{ stripHtml(post.content).substring(0, 120) }}...</p>

              <div class="post-meta">
                <span class="meta-item">
                  <el-avatar :size="24" :src="post.authorAvatar || defaultAvatar" />
                  <span class="author-name">{{ post.authorName || '匿名用户' }}</span>
                </span>
                <span class="meta-divider">•</span>
                <span class="meta-item">{{ formatTime(post.createTime) }}</span>

                <div class="post-stats">
                  <span class="stat-item"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                  <span class="stat-item"><el-icon><ChatLineRound /></el-icon> {{ post.replyCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination-container" v-if="total > 0">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="queryParams.pageSize"
                :current-page="queryParams.pageNum"
                @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="7" class="hidden-xs-only">
        <el-card class="sidebar-card welcome-card" shadow="hover">
          <div class="welcome-header">
            <h3>👋 欢迎来到校园论坛</h3>
            <p>发现有趣的内容，分享你的校园生活</p>
          </div>
        </el-card>

        <el-card class="sidebar-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📌 板块导航</span>
            </div>
          </template>
          <div class="board-tags">
            <el-tag
                v-for="board in boardList"
                :key="board.id"
                class="topic-tag"
                effect="light"
                @click="activeTab = board.id; handleTabChange(board.id)"
            >
              {{ board.name }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatLineRound, Search } from '@element-plus/icons-vue'
import { getPostList, type PostVO, type PostQuery } from '@/api/post'
import { getBoardList, type Board } from '@/api/board'
import 'element-plus/theme-chalk/display.css'

const router = useRouter()
const loading = ref(false)
const activeTab = ref(0)
const boardList = ref<Board[]>([])
const postList = ref<PostVO[]>([])
const total = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const queryParams = reactive<PostQuery>({
  pageNum: 1,
  pageSize: 10,
  boardId: undefined,
  keyword: ''
})

const fetchBoards = async () => {
  try {
    const res: any = await getBoardList()
    if (res.code === 0 || res.code === 200) {
      boardList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (activeTab.value === 0) {
      delete params.boardId
    } else {
      params.boardId = activeTab.value
    }
    const res: any = await getPostList(params)
    if (res.code === 0 || res.code === 200) {
      postList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchPosts()
}

const handleTabChange = (name: any) => {
  queryParams.pageNum = 1
  queryParams.boardId = name === 0 ? undefined : name
  fetchPosts()
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (id: number) => {
  router.push(`/post/${id}`)
}

const stripHtml = (html: string) => {
  if (!html) return ''
  return html.replace(/<[^>]*>?/gm, '')
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchBoards()
  fetchPosts()
})
</script>

<style scoped lang="scss">
.forum-container {
  padding: 12px 0;
}

/* 主卡片：高级圆角 + 阴影 */
.post-list-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  :deep(.el-card__body) {
    padding: 0 24px 20px;
  }
}

/* 顶部工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f5ff;

  .custom-tabs {
    flex: 1;

    :deep(.el-tabs__header) {
      margin: 0;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
      padding: 0 16px;
    }

    :deep(.el-tabs__active-bar) {
      background: linear-gradient(90deg, #5b7bff, #7094ff);
      height: 3px;
      border-radius: 3px;
    }
  }

  .search-box {
    display: flex;
    align-items: center;

    :deep(.el-input__wrapper) {
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }
  }
}

/* 帖子列表项 */
.post-item {
  padding: 22px 10px;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.25s ease;

  &:hover {
    background-color: #f8faff;
    transform: translateX(4px);
  }

  .post-title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 10px;
    line-height: 1.4;

    .board-tag {
      background: linear-gradient(90deg, #eef2ff, #f0f5ff);
      color: #5b7bff;
      border: none;
      font-weight: 500;
    }
  }

  .post-summary {
    color: #64748b;
    font-size: 14px;
    margin-bottom: 14px;
    line-height: 1.6;
  }

  .post-meta {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #94a3b8;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .author-name {
      font-weight: 500;
      color: #475569;
    }

    .meta-divider {
      margin: 0 10px;
      color: #e2e8f0;
    }

    .post-stats {
      margin-left: auto;
      display: flex;
      gap: 18px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

/* 侧边栏 */
.sidebar-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .topic-tag {
    margin-right: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.25s;

    &:hover {
      background: #eef2ff;
      color: #5b7bff;
      transform: translateY(-2px);
    }
  }
}

.welcome-card {
  background: linear-gradient(135deg, #eef5ff 0%, #f8faff 100%);

  .welcome-header {
    text-align: center;
    padding: 10px 0;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 6px;
    }

    p {
      font-size: 13px;
      color: #64748b;
      margin: 0;
    }
  }
}

.pagination-container {
  padding: 24px 0;
  display: flex;
  justify-content: center;

  :deep(.el-pagination) {
    &.is-background {
      .el-pager li.is-active {
        background: linear-gradient(90deg, #5b7bff, #7094ff);
      }
    }
  }
}
</style>
