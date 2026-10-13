import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { ChatMessage, ChatSession } from '@/types/ai'

const STORAGE_KEY = 'ai-chat-sessions'

const createSession = (): ChatSession => ({
  id: `session-${Date.now()}`,
  title: '新会话',
  messages: [],
  createdAt: new Date().toISOString(),
})

export const useAIChatStore = defineStore('ai-chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSession = ref<ChatSession | null>(null)

  const loadSessions = () => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const parsed = JSON.parse(raw) as ChatSession[]
        if (Array.isArray(parsed) && parsed.length) {
          sessions.value = parsed
          currentSession.value = parsed[0]
          return
        }
      }
    } catch (error) {
      // ignore invalid cache
    }
    const session = createSession()
    sessions.value = [session]
    currentSession.value = session
  }

  loadSessions()

  watch(
    sessions,
    (value) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
    },
    { deep: true },
  )

  const createSessionAction = () => {
    const session = createSession()
    sessions.value.unshift(session)
    currentSession.value = session
  }

  const setCurrentSession = (sessionId: string) => {
    currentSession.value = sessions.value.find((item) => item.id === sessionId) || null
  }

  const deleteSession = (sessionId: string) => {
    sessions.value = sessions.value.filter((item) => item.id !== sessionId)
    if (!sessions.value.length) {
      const session = createSession()
      sessions.value = [session]
      currentSession.value = session
      return
    }
    if (currentSession.value?.id === sessionId) {
      currentSession.value = sessions.value[0]
    }
  }

  const setSessionTitle = (sessionId: string, title: string) => {
    const session = sessions.value.find((item) => item.id === sessionId)
    if (session) {
      session.title = title
    }
  }

  const clearMessages = (sessionId: string) => {
    const session = sessions.value.find((item) => item.id === sessionId)
    if (session) {
      session.messages = []
    }
  }

  const appendMessage = (sessionId: string, message: ChatMessage) => {
    const session = sessions.value.find((item) => item.id === sessionId)
    if (session) {
      session.messages.push(message)
    }
  }

  const updateMessage = (sessionId: string, messageId: string, payload: Partial<ChatMessage>) => {
    const session = sessions.value.find((item) => item.id === sessionId)
    if (!session) return
    const target = session.messages.find((item) => item.id === messageId)
    if (target) {
      Object.assign(target, payload)
    }
  }

  return {
    sessions,
    currentSession,
    loadSessions,
    createSession: createSessionAction,
    setCurrentSession,
    deleteSession,
    setSessionTitle,
    clearMessages,
    appendMessage,
    updateMessage,
  }
})
