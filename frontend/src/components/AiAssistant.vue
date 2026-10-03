<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Loading, CircleCheck, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/userStore'

/** 一次工具调用在界面上的状态 */
interface ToolState {
  name: string
  label: string
  running: boolean
  success: boolean
}

interface ChatMessage {
  role: 'user' | 'ai'
  text: string
  tools: ToolState[]
  streaming?: boolean
  meta?: { iterations: number; promptTokens: number; completionTokens: number; durationMs: number } | null
}

const userStore = useUserStore()

const visible = ref(false)
const input = ref('')
const messages = ref<ChatMessage[]>([])
const streaming = ref(false)
const bodyRef = ref<HTMLElement | null>(null)

const samples = [
  '论坛里有哪些和考试相关的帖子？',
  '论坛一共多少帖子？哪个板块最火？',
  '谁发帖最活跃？'
]

const hasToken = computed(() => !!userStore.token)
const statusText = computed(() => (streaming.value ? '正在思考…' : 'DeepSeek · 可查帖子、板块与统计'))

function toggle() {
  visible.value = !visible.value
}

async function send(text?: string) {
  const question = (typeof text === 'string' ? text : input.value).trim()
  if (!question || streaming.value) return
  if (!userStore.token) {
    ElMessage.warning('请先登录后再使用智能助手')
    return
  }

  input.value = ''
  messages.value.push({ role: 'user', text: question, tools: [] })
  const aiMessage: ChatMessage = { role: 'ai', text: '', tools: [], streaming: true, meta: null }
  messages.value.push(aiMessage)
  streaming.value = true
  scrollToBottom()

  // 用 fetch 手动读取流：原生 EventSource 只能发 GET 且不支持自定义请求头，
  // 而本项目的鉴权 token 放在 Authorization 头里，只能自己解析 SSE 报文。
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${base}/ai/chat?message=${encodeURIComponent(question)}`

  try {
    const response = await fetch(url, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    if (!response.body) throw new Error('当前浏览器不支持流式响应')

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    for (;;) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      buffer = buffer.replace(/\r\n/g, '\n')
      let separator: number
      while ((separator = buffer.indexOf('\n\n')) >= 0) {
        const block = buffer.slice(0, separator)
        buffer = buffer.slice(separator + 2)
        handleEvent(block, aiMessage)
      }
    }
  } catch (error) {
    // 收到 done 事件之后连接关闭属于正常收尾（SSE 长连接由服务端主动 complete），
    // 浏览器有时会把这次关闭报成 network error，不能误显示成对话失败
    if (!aiMessage.meta) {
      aiMessage.text += (aiMessage.text ? '\n\n' : '') + `（请求失败：${(error as Error).message}）`
    }
  } finally {
    aiMessage.streaming = false
    streaming.value = false
    scrollToBottom()
  }
}

function handleEvent(block: string, aiMessage: ChatMessage) {
  const lines = block.split('\n')
  let name = ''
  const dataLines: string[] = []
  for (const line of lines) {
    if (line.startsWith('event:')) name = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
  }
  if (!dataLines.length) return

  let payload: any
  try {
    payload = JSON.parse(dataLines.join('\n'))
  } catch {
    return
  }

  if (name === 'tool_call') {
    aiMessage.tools.push({
      name: payload.name,
      label: runningLabel(payload.name),
      running: true,
      success: true
    })
    scrollToBottom()
  } else if (name === 'tool_result') {
    const pending = aiMessage.tools.filter((item) => item.running)
    const target = pending.length ? pending[pending.length - 1] : null
    if (target) {
      target.running = false
      target.success = !!payload.success
      target.label = `${payload.success ? '已' : '失败：'}${doneLabel(payload.name)}（${payload.durationMs}ms）`
    }
    scrollToBottom()
  } else if (name === 'content') {
    aiMessage.text += payload.text || ''
    scrollToBottom()
  } else if (name === 'error') {
    aiMessage.text += (aiMessage.text ? '\n\n' : '') + `（${payload.message || '服务异常'}）`
  } else if (name === 'done') {
    aiMessage.meta = payload
  }
}

const toolNames: Record<string, string> = {
  search_posts: '检索帖子',
  get_post_detail: '读取帖子正文',
  list_boards: '查询板块',
  get_forum_stats: '统计论坛数据',
  create_post: '发布帖子',
  top_post: '置顶操作'
}

function runningLabel(tool: string) {
  return `正在${toolNames[tool] || tool}…`
}

function doneLabel(tool: string) {
  return toolNames[tool] || tool
}

/** 模型输出是 Markdown 片段，只做最小渲染；先转义再替换，避免 v-html 注入 */
function render(text: string) {
  if (!text) return ''
  const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  return escaped
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    // 聊天气泡里不做完整 Markdown 渲染（不引第三方库、也避免 v-html 注入面扩大），
    // 只把列表项与标题做最小可读化处理；后端提示词已约束模型不要输出表格
    .replace(/^\s*[-*]\s+/gm, '• ')
    .replace(/^\s*#{1,4}\s+/gm, '')
    .replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    const el = bodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
</script>

<template>
  <div v-if="hasToken" class="ai-assistant">
    <transition name="ai-pop">
      <div v-show="visible" class="ai-panel">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>校园小助手</span>
          </div>
          <div class="ai-panel-sub">{{ statusText }}</div>
          <el-icon class="ai-panel-close" @click="visible = false"><Close /></el-icon>
        </div>

        <div ref="bodyRef" class="ai-panel-body">
          <div v-if="messages.length === 0" class="ai-empty">
            <div class="ai-empty-title">可以这样问我</div>
            <div class="ai-samples">
              <span v-for="item in samples" :key="item" class="ai-sample" @click="send(item)">{{ item }}</span>
            </div>
          </div>

          <div v-for="(message, index) in messages" :key="index" class="ai-msg" :class="`ai-msg--${message.role}`">
            <div class="ai-bubble">
              <div
                v-for="(tool, toolIndex) in message.tools"
                :key="toolIndex"
                class="ai-tool"
                :class="{ 'ai-tool--fail': !tool.running && !tool.success }"
              >
                <el-icon v-if="tool.running" class="is-loading"><Loading /></el-icon>
                <el-icon v-else-if="tool.success"><CircleCheck /></el-icon>
                <el-icon v-else><Warning /></el-icon>
                <span>{{ tool.label }}</span>
              </div>
              <div class="ai-text" v-html="render(message.text)"></div>
            </div>
            <div v-if="message.meta" class="ai-meta">
              {{ message.meta.iterations }} 次工具调用 · {{ message.meta.durationMs }}ms ·
              {{ message.meta.promptTokens + message.meta.completionTokens }} tokens
            </div>
          </div>
        </div>

        <div class="ai-panel-footer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="问帖子、板块、统计，或让我帮你发帖…"
            @keydown.enter.exact.prevent="send()"
          />
          <div class="ai-actions">
            <span class="ai-hint">Enter 发送 · Shift+Enter 换行</span>
            <el-button type="primary" size="small" :loading="streaming" @click="send()">发送</el-button>
          </div>
        </div>
      </div>
    </transition>

    <div v-show="!visible" class="ai-fab" @click="toggle">
      <el-icon><ChatDotRound /></el-icon>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3000;
}
.ai-fab {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #2b7de9);
  color: #fff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.42);
  transition: transform 0.2s;
  user-select: none;
}
.ai-fab:hover {
  transform: scale(1.06);
}
.ai-panel {
  position: absolute;
  right: 0;
  bottom: 66px;
  width: 384px;
  height: 544px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-panel-header {
  position: relative;
  padding: 14px 16px 12px;
  background: linear-gradient(135deg, #409eff, #2b7de9);
  color: #fff;
}
.ai-panel-title {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.ai-panel-sub {
  margin-top: 3px;
  font-size: 12px;
  opacity: 0.85;
}
.ai-panel-close {
  position: absolute;
  right: 14px;
  top: 16px;
  cursor: pointer;
  font-size: 16px;
}
.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: #f5f7fa;
}
.ai-empty-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}
.ai-samples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-sample {
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.18s;
}
.ai-sample:hover {
  border-color: #409eff;
  color: #409eff;
}
.ai-msg {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
}
.ai-msg--user {
  align-items: flex-end;
}
.ai-msg--ai {
  align-items: flex-start;
}
.ai-bubble {
  max-width: 92%;
  padding: 9px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.65;
  word-break: break-word;
}
.ai-msg--user .ai-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}
.ai-msg--ai .ai-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e8eaed;
  border-bottom-left-radius: 2px;
}
.ai-tool {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 6px;
  padding: 3px 8px;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 4px;
}
.ai-tool--fail {
  color: #f56c6c;
  background: #fef0f0;
}
.ai-meta {
  margin-top: 5px;
  font-size: 11px;
  color: #b0b3b8;
}
.ai-panel-footer {
  padding: 10px 12px 12px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
.ai-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.ai-hint {
  font-size: 11px;
  color: #c0c4cc;
}
.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: opacity 0.18s, transform 0.18s;
}
.ai-pop-enter,
.ai-pop-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
@media (max-width: 500px) {
  .ai-assistant {
    right: 14px;
    bottom: 14px;
  }
  .ai-panel {
    width: calc(100vw - 28px);
    height: 60vh;
  }
}
</style>
