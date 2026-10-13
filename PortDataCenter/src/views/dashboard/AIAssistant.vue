<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useAIChatStore } from '@/stores/aiChat'
import { useAuthStore } from '@/stores/auth'
import { streamWorkspaceChat } from '@/api/ai'
import type { AIChatChunk, ChatMessage } from '@/types/ai'
import { resolveAvatarUrl, resolvePublicAsset } from '@/utils/url'

const aiStore = useAIChatStore()
const auth = useAuthStore()
const inputText = ref('')
const mode = ref<'query' | 'chat'>('query')
const sending = ref(false)
const resetNext = ref(false)
const abortController = ref<AbortController | null>(null)

const { sessions, currentSession } = storeToRefs(aiStore)

const handleNewSession = () => {
  aiStore.createSession()
  resetNext.value = false
}

const handleSelectSession = (sessionId: string) => {
  if (sending.value) {
    abortController.value?.abort()
    sending.value = false
  }
  aiStore.setCurrentSession(sessionId)
  resetNext.value = false
}

const handleDeleteSession = (sessionId: string) => {
  aiStore.deleteSession(sessionId)
}

const handleResetSession = () => {
  const session = currentSession.value
  if (!session) return
  aiStore.clearMessages(session.id)
  resetNext.value = true
  ElMessage.success('已重置当前会话')
}

const getMessageContent = (sessionId: string, messageId: string) => {
  const session = sessions.value.find((item) => item.id === sessionId)
  return session?.messages.find((item) => item.id === messageId)?.content || ''
}

const appendAssistantText = (sessionId: string, messageId: string, chunk: AIChatChunk) => {
  if (chunk.textResponse) {
    aiStore.updateMessage(sessionId, messageId, {
      content: getMessageContent(sessionId, messageId) + chunk.textResponse,
    })
  }
  if (chunk.sources && chunk.sources.length > 0) {
    aiStore.updateMessage(sessionId, messageId, { sources: chunk.sources })
  }
}

const handleSend = async () => {
  const session = currentSession.value
  if (!session) return
  const content = inputText.value.trim()
  if (!content || sending.value) return
  sending.value = true

  const userMessage: ChatMessage = {
    id: `msg-${Date.now()}-user`,
    role: 'user',
    content,
    createdAt: new Date().toISOString(),
  }
  aiStore.appendMessage(session.id, userMessage)
  inputText.value = ''

  if (session.messages.length === 1) {
    aiStore.setSessionTitle(session.id, content.slice(0, 12))
  }

  const assistantMessageId = `msg-${Date.now()}-assistant`
  aiStore.appendMessage(session.id, {
    id: assistantMessageId,
    role: 'assistant',
    content: '',
    createdAt: new Date().toISOString(),
  })

  abortController.value?.abort()
  abortController.value = new AbortController()

  try {
    await streamWorkspaceChat(
      {
        message: content,
        mode: mode.value,
        sessionId: session.id,
        reset: resetNext.value,
      },
      {
        signal: abortController.value.signal,
        onChunk: (chunk) => {
          if (chunk.type === 'abort') {
            ElMessage.error('AI 已中止回复')
            return
          }
          if (chunk.error && chunk.error !== false) {
            ElMessage.error(typeof chunk.error === 'string' ? chunk.error : 'AI 响应异常')
            return
          }
          appendAssistantText(session.id, assistantMessageId, chunk)
        },
      },
    )
  } catch (error: any) {
    if (error?.name !== 'AbortError') {
      ElMessage.error(error?.message || 'AI 响应失败')
    }
  } finally {
    sending.value = false
    resetNext.value = false
  }
}
</script>

<template>
  <div class="ai-assistant">
    <section class="ai-assistant__sidebar glass-card">
      <header class="sidebar-header">
        <div class="title">
          <h3>AI 问答</h3>
          <p>会话记录</p>
        </div>
        <el-button type="primary" size="small" @click="handleNewSession">新建会话</el-button>
      </header>
      <el-scrollbar height="620px">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === currentSession?.id }"
          @click="handleSelectSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-meta">
            <span>{{ session.messages.length }} 条</span>
            <el-button link type="danger" @click.stop="handleDeleteSession(session.id)">删除</el-button>
          </div>
        </div>
      </el-scrollbar>
    </section>

    <section class="ai-assistant__chat glass-card">
      <header class="chat-header">
        <div class="title">
          <h3>{{ currentSession?.title || '未选择会话' }}</h3>
          <p>支持查询与对话模式</p>
        </div>
        <div class="actions">
          <el-select v-model="mode" size="small" style="width: 120px;">
            <el-option label="查询模式" value="query" />
            <el-option label="对话模式" value="chat" />
          </el-select>
          <el-button size="small" @click="handleResetSession">重置会话</el-button>
        </div>
      </header>

      <div class="chat-body">
        <el-scrollbar height="520px">
          <div v-if="!currentSession?.messages.length" class="empty-state">
            <p>开始你的 AI 问答吧</p>
            <small>会话会自动保存，支持随时切换。</small>
          </div>
          <div
            v-for="message in currentSession?.messages"
            :key="message.id"
            class="message-item"
            :class="message.role"
          >
            <div class="message-avatar">
              <el-avatar
                v-if="message.role === 'user'"
                :src="resolveAvatarUrl(auth.user?.avatar)"
                :size="32"
                class="user-avatar"
              >
                {{ auth.user?.username?.slice(0, 1) || '你' }}
              </el-avatar>
              <el-avatar v-else :size="32" :src="resolvePublicAsset('ai.png')" class="assistant-avatar">AI</el-avatar>
            </div>
            <div class="message-content">
              <p>{{ message.content || (message.role === 'assistant' ? '...' : '') }}</p>
              <div v-if="message.sources?.length" class="message-sources">
                <span>引用来源：</span>
                <ul>
                  <li v-for="(source, index) in message.sources" :key="index">
                    {{ source.title || source.text || source.name }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="请输入你的问题..."
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ai-assistant {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  padding: 20px;
}

.ai-assistant__sidebar,
.ai-assistant__chat {
  padding: 20px;
  border-radius: 18px;
}

.sidebar-header,
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sidebar-header .title p,
.chat-header .title p {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.session-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.session-item.active,
.session-item:hover {
  background: rgba(0, 168, 204, 0.2);
}

.session-title {
  font-weight: 600;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 6px;
}

.chat-body {
  margin-bottom: 16px;
}

.chat-body :deep(.el-scrollbar__wrap) {
  padding-right: 10px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: rgba(255, 255, 255, 0.7);
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
  width: 100%;
}

.message-avatar {
  width: 36px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  flex-shrink: 0;
}

.assistant-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.message-item.user {
  flex-direction: row-reverse;
  justify-content: flex-end;
}

.message-item.user .message-content {
  background: rgba(0, 168, 204, 0.18);
  margin-left: auto;
}

.message-item.assistant .message-content {
  background: rgba(255, 255, 255, 0.08);
  margin-right: auto;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
}

.message-content p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.message-sources {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.message-sources ul {
  padding-left: 18px;
  margin: 4px 0 0;
}

.chat-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: start;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
