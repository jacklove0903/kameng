import axios from 'axios'
import type { GenerateRequest, GenerationTask, Provider } from '../types'
import { mockProviderApi, mockGenerateApi } from './mock'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// Backend returns HTTP 200 with {code, message, data} even on errors.
// Convert non-200 business codes into real errors so catch blocks fire.
api.interceptors.response.use((res) => {
  const body = res.data
  if (body && typeof body === 'object' && 'code' in body) {
    if (body.code !== 200) {
      const err: any = new Error(body.message || '请求失败')
      err.response = { data: body, status: res.status }
      throw err
    }
    // Unwrap Result wrapper: return inner data
    res.data = body.data
  }
  return res
})

// Detect if backend is available, re-check when in mock mode
let useMock = true
let lastCheck = 0
const CHECK_INTERVAL = 5000 // re-check every 5s when in mock mode

async function ensureBackend(): Promise<boolean> {
  const now = Date.now()
  if (!useMock) return true
  if (now - lastCheck < CHECK_INTERVAL) return false
  lastCheck = now
  try {
    await api.get('/providers', { timeout: 3000 })
    useMock = false
    console.log('[API] Backend connected, switching to real API')
    return true
  } catch {
    console.log('[API] Backend unavailable, using mock mode')
    return false
  }
}

// Initial check
ensureBackend()

export const providerApi = {
  list: async (): Promise<Provider[]> => {
    await ensureBackend()
    if (useMock) return mockProviderApi.list()
    const res = await api.get<Provider[]>('/providers')
    return res.data
  },
  status: async (type: string): Promise<Provider> => {
    await ensureBackend()
    if (useMock) return mockProviderApi.status(type)
    const res = await api.get<Provider>(`/providers/${type}/status`)
    return res.data
  },
}

export const generateApi = {
  submit: async (data: GenerateRequest): Promise<GenerationTask> => {
    await ensureBackend()
    if (useMock) return mockGenerateApi.submit(data)
    const res = await api.post<GenerationTask>('/generate', data)
    return res.data
  },
  getTask: async (id: number): Promise<GenerationTask | null> => {
    if (useMock) return mockGenerateApi.getTask(id)
    const res = await api.get<GenerationTask>(`/generate/${id}`)
    return res.data
  },
  download: async (id: number): Promise<string | null> => {
    if (useMock) return mockGenerateApi.download(id)
    const res = await api.get<string>(`/generate/${id}/download`)
    return res.data
  },
  recent: async (limit = 20): Promise<GenerationTask[]> => {
    await ensureBackend()
    if (useMock) return mockGenerateApi.recent(limit)
    const res = await api.get<GenerationTask[]>(`/generate/recent?limit=${limit}`)
    return res.data
  },
}
