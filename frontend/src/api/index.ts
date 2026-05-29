import axios from 'axios'
import type { GenerateRequest, GenerationTask, Provider } from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

export const providerApi = {
  list: () => api.get<Provider[]>('/providers'),
  status: (type: string) => api.get<Provider>(`/providers/${type}/status`),
}

export const generateApi = {
  submit: (data: GenerateRequest) =>
    api.post<GenerationTask>('/generate', data),
  getTask: (id: number) => api.get<GenerationTask>(`/generate/${id}`),
  download: (id: number) => api.get<string>(`/generate/${id}/download`),
  recent: (limit = 20) =>
    api.get<GenerationTask[]>(`/generate/recent?limit=${limit}`),
}
