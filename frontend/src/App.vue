<script setup lang="ts">
import { RouterView } from 'vue-router'
import AiAssistant from '@/components/AiAssistant.vue'
</script>

<template>
  <div id="app">
    <RouterView />
    <!-- 校园小助手：组件内部用 Pinia 里的 token 判断是否登录，未登录不渲染 -->
    <AiAssistant />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}
</style>
