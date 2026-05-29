import { create } from 'zustand'
import type {
  GenerationSlot,
  GenerationTask,
  LayoutMode,
  PromptParams,
  Provider,
  ProviderType,
} from '../types'

const DEFAULT_PROVIDERS: ProviderType[] = ['RUNWAY', 'KLING', 'PIKA', 'SVD']

interface AppState {
  // Layout
  layout: LayoutMode
  setLayout: (layout: LayoutMode) => void

  // Providers
  providers: Provider[]
  setProviders: (providers: Provider[]) => void

  // Generation slots
  slots: GenerationSlot[]
  updateSlot: (index: number, slot: Partial<GenerationSlot>) => void
  resetSlot: (index: number) => void

  // Unified prompt params
  promptParams: PromptParams
  setPromptParams: (params: Partial<PromptParams>) => void

  // Task history
  tasks: GenerationTask[]
  setTasks: (tasks: GenerationTask[]) => void
  addTask: (task: GenerationTask) => void
  updateTask: (id: number, task: Partial<GenerationTask>) => void

  // Task queue visibility
  queueVisible: boolean
  toggleQueue: () => void

  // Active sidebar nav
  activeNav: string
  setActiveNav: (nav: string) => void

  // Polling
  pollingIds: number[]
  addPollingId: (id: number) => void
  removePollingId: (id: number) => void
}

export const useAppStore = create<AppState>((set) => ({
  layout: 'grid',
  setLayout: (layout) => set({ layout }),

  providers: [],
  setProviders: (providers) => set({ providers }),

  slots: DEFAULT_PROVIDERS.map((p, i) => ({
    id: i,
    provider: p,
    task: null,
    loading: false,
  })),
  updateSlot: (index, partial) =>
    set((state) => ({
      slots: state.slots.map((s, i) =>
        i === index ? { ...s, ...partial } : s
      ),
    })),
  resetSlot: (index) =>
    set((state) => ({
      slots: state.slots.map((s, i) =>
        i === index ? { ...s, task: null, loading: false } : s
      ),
    })),

  promptParams: {
    prompt: '',
    negativePrompt: '',
    duration: 5,
    resolution: '1080p',
    aspectRatio: '16:9',
    motionStrength: 0.5,
    samplingSteps: 25,
    imageUrl: '',
    endFrameUrl: '',
  },
  setPromptParams: (params) =>
    set((state) => ({
      promptParams: { ...state.promptParams, ...params },
    })),

  tasks: [],
  setTasks: (tasks) => set({ tasks }),
  addTask: (task) =>
    set((state) => ({ tasks: [task, ...state.tasks] })),
  updateTask: (id, partial) =>
    set((state) => ({
      tasks: state.tasks.map((t) =>
        t.id === id ? { ...t, ...partial } : t
      ),
    })),

  queueVisible: true,
  toggleQueue: () => set((state) => ({ queueVisible: !state.queueVisible })),

  activeNav: 'workspace',
  setActiveNav: (activeNav) => set({ activeNav }),

  pollingIds: [],
  addPollingId: (id) =>
    set((state) => ({
      pollingIds: state.pollingIds.includes(id)
        ? state.pollingIds
        : [...state.pollingIds, id],
    })),
  removePollingId: (id) =>
    set((state) => ({
      pollingIds: state.pollingIds.filter((pid) => pid !== id),
    })),
}))
