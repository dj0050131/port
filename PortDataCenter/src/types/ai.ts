export type ChatRole = 'user' | 'assistant'

export interface SourceItem {
  title?: string
  text?: string
  name?: string
}

export interface ChatMessage {
  id: string
  role: ChatRole
  content: string
  sources?: SourceItem[]
  createdAt: string
}

export interface ChatSession {
  id: string
  title: string
  messages: ChatMessage[]
  createdAt: string
}

export interface AIChatChunk {
  id?: string
  type?: string
  textResponse?: string
  error?: string | boolean | null
  sources?: SourceItem[]
  close?: boolean
}

export interface ChatRequest {
  message: string
  mode: 'query' | 'chat'
  sessionId: string
  reset?: boolean
}
