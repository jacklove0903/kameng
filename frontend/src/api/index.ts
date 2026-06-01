import axios from 'axios'
import type { GenerateRequest, GenerationTask, Provider } from '../types'
import { mockProviderApi, mockGenerateApi } from './mock'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

// Detect if backend is available
let useMock = true

async function checkBackend() {
  try {
    await api.get('/providers')
    useMock = false
    console.log('[API] Backend connected, using real API')
  } catch {
    useMock = true
    console.log('[API] Backend unavailable, using mock mode')
  }
}

// Check on load
checkBackend()

export const providerApi = {
  list: async (): Promise<Provider[]> => {
    if (useMock) return mockProviderApi.list()
    const res = await api.get<Provider[]>('/providers')
    return res.data
  },
  status: async (type: string): Promise<Provider> => {
    if (useMock) return mockProviderApi.status(type)
    const res = await api.get<Provider>(`/providers/${type}/status`)
    return res.data
  },
}

export const generateApi = {
  submit: async (data: GenerateRequest): Promise<GenerationTask> => {
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
    if (useMock) return mockGenerateApi.recent(limit)
    const res = await api.get<GenerationTask[]>(`/generate/recent?limit=${limit}`)
    return res.data
  },
}
