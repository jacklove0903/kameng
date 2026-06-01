import type { GenerationTask, Provider, ProviderType } from '../types'

// Mock video URLs (public domain sample videos)
const MOCK_VIDEOS = [
  'https://www.w3schools.com/html/mov_bbb.mp4',
  'https://www.w3schools.com/html/movie.mp4',
  'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4',
  'https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_1MB.mp4',
]

let mockTaskId = 1000
const mockTasks = new Map<number, GenerationTask>()

// Simulate async generation: pending → running → success after ~8 seconds
function simulateTask(task: GenerationTask) {
  const id = task.id
  // After 2s → RUNNING
  setTimeout(() => {
    const t = mockTasks.get(id)
    if (t) {
      t.status = 'RUNNING'
      mockTasks.set(id, { ...t })
    }
  }, 2000)
  // After 8s → SUCCESS
  setTimeout(() => {
    const t = mockTasks.get(id)
    if (t) {
      t.status = 'SUCCESS'
      t.videoUrl = MOCK_VIDEOS[Math.floor(Math.random() * MOCK_VIDEOS.length)]
      t.cost = +(Math.random() * 0.5 + 0.05).toFixed(4)
      mockTasks.set(id, { ...t })
    }
  }, 8000)
}

export const mockProviderApi = {
  list: (): Provider[] => [
    { type: 'RUNWAY', displayName: 'Runway', available: true },
    { type: 'KLING', displayName: 'Kling', available: true },
    { type: 'REPLICATE', displayName: 'Replicate', available: true },
    { type: 'PIKA', displayName: 'Pika', available: true },
    { type: 'SVD', displayName: 'Stable Video', available: true },
  ],
  status: (type: string): Provider => ({
    type: type as ProviderType,
    displayName: type,
    available: true,
  }),
}

export const mockGenerateApi = {
  submit: (data: {
    provider: ProviderType
    prompt: string
    negativePrompt?: string
    imageUrl?: string
    duration?: number
    resolution?: string
    aspectRatio?: string
    seed?: number
    motionStrength?: number
    samplingSteps?: number
  }): GenerationTask => {
    const task: GenerationTask = {
      id: ++mockTaskId,
      userId: null,
      provider: data.provider,
      prompt: data.prompt,
      negativePrompt: data.negativePrompt || null,
      imageUrl: data.imageUrl || null,
      duration: data.duration || 5,
      resolution: data.resolution || '1080p',
      aspectRatio: data.aspectRatio || '16:9',
      seed: data.seed || null,
      status: 'PENDING',
      videoUrl: null,
      errorMsg: null,
      cost: null,
      providerTaskId: `mock-${mockTaskId}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    mockTasks.set(task.id, task)
    simulateTask(task)
    return task
  },

  getTask: (id: number): GenerationTask | null => {
    return mockTasks.get(id) || null
  },

  download: (id: number): string | null => {
    const task = mockTasks.get(id)
    return task?.videoUrl || null
  },

  recent: (limit = 20): GenerationTask[] => {
    return Array.from(mockTasks.values())
      .sort((a, b) => b.id - a.id)
      .slice(0, limit)
  },
}
