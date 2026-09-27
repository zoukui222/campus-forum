<template>
    <div class="moderator-container">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span class="title">⚖️ 板主工作台</span>
            <el-tag type="success" v-if="boardId">正在管理板块 ID: {{ boardId }}</el-tag>
          </div>
        </template>
  
        <div v-if="!boardId" class="empty-state">
          <el-empty description="您当前没有管理的板块" />
        </div>
  
        <div v-else>
           <div class="action-bar">
            <div class="search-box">
                <el-input 
                    v-model="keyword" 
                    placeholder="搜索本板块帖子" 
                    style="width: 300px" 
                    clearable
                    @clear="handleSearch"
                    @keyup.enter="handleSearch"
                >
                    <template #append>
                    <el-button :icon="Search" @click="handleSearch" />
                    </template>
                </el-input>
                </div>
             <el-alert title="您可以删除本板块内的违规帖子，请谨慎操作。" type="warning" show-icon :closable="false" />
           </div>
  
           <el-table :data="postList" style="width: 100%; margin-top: 20px" v-loading="loading" border stripe>
             <el-table-column prop="id" label="ID" width="80" />
             <el-table-column label="标题" min-width="300">
               <template #default="scope">
                 <el-link type="primary" :href="`/post/${scope.row.id}`" target="_blank">
                    {{ scope.row.title }}
                 </el-link>
               </template>
             </el-table-column>
             <el-table-column prop="authorName" label="发布者" width="120" />
             <el-table-column prop="replyCount" label="回复数" width="100" align="center" />
             <el-table-column prop="createTime" label="发布时间" width="180">
               <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
             </el-table-column>
             
             <el-table-column label="操作" width="120" fixed="right" align="center">
               <template #default="scope">
                 <el-popconfirm title="确定要删除这个帖子吗？" @confirm="handleDelete(scope.row)">
                   <template #reference>
                     <el-button type="danger" size="small" :icon="Delete" plain>删除</el-button>
                   </template>
                 </el-popconfirm>
               </template>
             </el-table-column>
           </el-table>
  
           <div class="pagination-box">
             <el-pagination 
               background 
               layout="prev, pager, next" 
               :total="total" 
               :page-size="pageSize"
               @current-change="handlePageChange"
             />
           </div>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, computed } from 'vue'
  import { Delete, Search } from '@element-plus/icons-vue'
  import { useUserStore } from '@/store/userStore'
  import { getPostList, deletePost } from '@/api/post'
  import { ElMessage } from 'element-plus'
  
  const userStore = useUserStore()
  const loading = ref(false)
  const postList = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const keyword = ref('')
  
  // 获取当前用户管理的板块ID
  const boardId = computed(() => userStore.userInfo?.boardId)
  
  const fetchMyBoardPosts = async () => {
    if (!boardId.value) return
  
    loading.value = true
    try {
      const res: any = await getPostList({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        boardId: boardId.value, // 只查询自己板块的帖子
        keyword: keyword.value
      })
      if (res.code === 0 || res.code === 200) {
        postList.value = res.data.records
        total.value = res.data.total
      }
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pageNum.value = 1
    fetchMyBoardPosts()
  }
  
  const handlePageChange = (page: number) => {
    pageNum.value = page
    fetchMyBoardPosts()
  }
  
  const handleDelete = async (row: any) => {
    try {
      // 使用通用删除接口
      const res: any = await deletePost(row.id)
      if (res.code === 0 || res.code === 200) {
        ElMessage.success('帖子已删除')
        fetchMyBoardPosts()
      }
    } catch (e) {
      console.error(e)
    }
  }
  
  const formatTime = (time: string) => {
    if (!time) return ''
    return time.replace('T', ' ').substring(0, 16)
  }
  
  onMounted(() => {
    fetchMyBoardPosts()
  })
  </script>
  
  <style scoped lang="scss">
  .moderator-container {
    max-width: 1200px;
    margin: 20px auto;
  }
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title { font-size: 18px; font-weight: bold; }
  }
  .pagination-box {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
    .action-bar {
        display: flex;
        gap: 20px;
        align-items: center;
        margin-bottom: 20px;
    }
  </style>