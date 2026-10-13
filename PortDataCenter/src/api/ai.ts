import { getToken } from '@/utils/storage'
import type { AIChatChunk, ChatRequest } from '@/types/ai'

interface StreamOptions {
  signal?: AbortSignal
  onChunk?: (chunk: AIChatChunk) => void
}

const parseChunk = (line: string): AIChatChunk | null => {
  const trimmed = line.trim()
  if (!trimmed.startsWith('data:')) return null
  const raw = trimmed.replace(/^data:\s*/, '').trim()
  if (!raw || raw === '[DONE]') return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    return { type: 'textResponse', textResponse: raw }
  }
}

export const streamWorkspaceChat = async (payload: ChatRequest, options: StreamOptions = {}) => {
  const token = getToken()
  const res = await fetch('/api/ai/streamChat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
    signal: options.signal,
  })

  if (!res.ok || !res.body) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `AI 请求失败 (${res.status})`)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  let shouldStop = false
  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      const chunk = parseChunk(line)
      if (chunk && options.onChunk) {
        options.onChunk(chunk)
      }
      if (chunk?.close || (chunk?.error && chunk.error !== false)) {
        shouldStop = true
        break
      }
    }
    if (shouldStop) break
  }
}
