export type ProviderType = 'RUNWAY' | 'KLING' | 'REPLICATE' | 'PIKA' | 'SVD'

export interface Provider {
  type: ProviderType
  displayName: string
  available: boolean
}

export type TaskStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'

export interface GenerationTask {
  id: number
  userId: number | null
  provider: ProviderType
  prompt: string
  negativePrompt: string | null
  imageUrl: string | null
  duration: number
  resolution: string
  aspectRatio: string
  seed: number | null
  status: TaskStatus
  videoUrl: string | null
  errorMsg: string | null
  cost: number | null
  providerTaskId: string | null
  createdAt: string
  updatedAt: string
}

export interface GenerateRequest {
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
}

export interface GenerationSlot {
  id: number
  provider: ProviderType
  task: GenerationTask | null
  loading: boolean
}

export type LayoutMode = 'grid' | '1+3' | 'horizontal' | 'single'

export interface PromptParams {
  prompt: string
  negativePrompt: string
  duration: number
  resolution: string
  aspectRatio: string
  motionStrength: number
  samplingSteps: number
  imageUrl: string
  endFrameUrl: string
}
