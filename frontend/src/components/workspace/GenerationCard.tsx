import { useState } from 'react'
import { useAppStore } from '../../store/useAppStore'
import { generateApi } from '../../api'
import type { GenerationSlot, LayoutMode } from '../../types'
import {
  Play,
  Square,
  RotateCcw,
  Download,
  Loader2,
  AlertCircle,
  Film,
  Copy,
  Settings2,
  ChevronDown,
  ChevronUp,
} from 'lucide-react'
import clsx from 'clsx'

interface Props {
  slot: GenerationSlot
  index: number
  layout: LayoutMode
}

const providerColors: Record<string, string> = {
  RUNWAY: '#6c5ce7',
  KLING: '#00b894',
  REPLICATE: '#0d6efd',
  PIKA: '#fdcb6e',
  SVD: '#e17055',
}

const providerLabels: Record<string, string> = {
  RUNWAY: 'Runway',
  KLING: 'Kling',
  REPLICATE: 'Replicate',
  PIKA: 'Pika',
  SVD: 'Stable Video',
}

export default function GenerationCard({ slot, index, layout }: Props) {
  const { promptParams, updateSlot, addTask, addPollingId } = useAppStore()
  const [expanded, setExpanded] = useState(true)

  const isRunning = slot.loading
  const task = slot.task
  const status = task?.status
  const isGenerating = isRunning || status === 'PENDING' || status === 'RUNNING'
  const color = providerColors[slot.provider] || '#6c5ce7'

  const handleGenerate = async () => {
    if (!promptParams.prompt.trim()) return
    updateSlot(index, { loading: true })

    try {
      const newTask = await generateApi.submit({
        provider: slot.provider,
        prompt: promptParams.prompt,
        negativePrompt: promptParams.negativePrompt || undefined,
        imageUrl: promptParams.imageUrl || undefined,
        duration: promptParams.duration,
        resolution: promptParams.resolution,
        aspectRatio: promptParams.aspectRatio,
        motionStrength: promptParams.motionStrength,
        samplingSteps: promptParams.samplingSteps,
      })
      updateSlot(index, { task: newTask, loading: false })
      addTask(newTask)
      addPollingId(newTask.id)
    } catch (err: any) {
      const errorMsg = err?.response?.data?.message || err?.message || '提交失败'
      const failedTask = {
        id: Date.now(),
        provider: slot.provider,
        prompt: promptParams.prompt,
        status: 'FAILED' as const,
        errorMsg,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      updateSlot(index, { task: failedTask as any, loading: false })
      addTask(failedTask as any)
    }
  }

  const handleRetry = () => {
    updateSlot(index, { task: null })
    handleGenerate()
  }

  return (
    <div
      className={clsx(
        'flex flex-col rounded-xl border border-border bg-bg-secondary overflow-hidden transition-all',
        layout === '1+3' && index === 0 && 'row-span-2',
        layout === 'horizontal' && 'min-w-[280px] flex-1'
      )}
    >
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border px-3 py-2">
        <div className="flex items-center gap-2">
          <div
            className="h-2 w-2 rounded-full"
            style={{ backgroundColor: color }}
          />
          <span className="text-xs font-medium text-gray-200">
            {providerLabels[slot.provider]}
          </span>
          {status && (
            <span
              className={clsx('text-[10px] px-1.5 py-0.5 rounded-full', {
                'bg-gray-700 text-gray-400': status === 'PENDING',
                'bg-accent/20 text-accent animate-pulse-glow': status === 'RUNNING',
                'bg-success/20 text-success': status === 'SUCCESS',
                'bg-danger/20 text-danger': status === 'FAILED',
              })}
            >
              {status === 'PENDING' && '等待中'}
              {status === 'RUNNING' && `生成中 ${task?.providerTaskId ? '' : ''}`}
              {status === 'SUCCESS' && '已完成'}
              {status === 'FAILED' && '失败'}
            </span>
          )}
        </div>
        <div className="flex items-center gap-1">
          {status === 'SUCCESS' && task?.videoUrl && (
            <button
              onClick={() => window.open(task.videoUrl!, '_blank')}
              className="rounded p-1 text-gray-400 hover:bg-bg-tertiary hover:text-white"
              title="下载"
            >
              <Download size={14} />
            </button>
          )}
          <button
            onClick={() => setExpanded(!expanded)}
            className="rounded p-1 text-gray-400 hover:bg-bg-tertiary hover:text-white"
          >
            {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          </button>
        </div>
      </div>

      {/* Content area */}
      <div className="flex flex-1 flex-col">
        {/* Video preview / placeholder */}
        <div className="relative flex flex-1 items-center justify-center bg-bg-primary min-h-[180px]">
          {(status === 'PENDING' || status === 'RUNNING') && (
            <div className="flex flex-col items-center gap-3">
              <Loader2 size={32} className="animate-spin" style={{ color }} />
              <div className="w-3/4">
                <div className="h-1 rounded-full bg-bg-tertiary overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500 animate-pulse-glow"
                    style={{
                      width: status === 'RUNNING' ? '60%' : '20%',
                      backgroundColor: color,
                    }}
                  />
                </div>
              </div>
              <span className="text-xs text-gray-500">
                {status === 'PENDING' ? '排队中...' : '生成中...'}
              </span>
            </div>
          )}
          {status === 'SUCCESS' && task?.videoUrl && (
            <video
              src={task.videoUrl}
              className="h-full w-full object-contain"
              controls
              loop
              muted
              playsInline
            />
          )}
          {status === 'FAILED' && (
            <div className="flex flex-col items-center gap-2 text-danger">
              <AlertCircle size={28} />
              <span className="text-xs">{task?.errorMsg || '生成失败'}</span>
              <button
                onClick={handleRetry}
                className="flex items-center gap-1 rounded-md bg-danger/20 px-2 py-1 text-xs text-danger hover:bg-danger/30"
              >
                <RotateCcw size={12} /> 重试
              </button>
            </div>
          )}
          {!status && !isRunning && (
            <div className="flex flex-col items-center gap-2 text-gray-600">
              <Film size={28} />
              <span className="text-xs">等待生成</span>
            </div>
          )}
        </div>

        {/* Prompt & params info */}
        {expanded && (
          <div className="border-t border-border p-3 space-y-2">
            {/* Prompt */}
            <div>
              <div className="flex items-center justify-between mb-1">
                <span className="text-[10px] text-gray-500 uppercase tracking-wider">
                  Prompt
                </span>
                <button
                  onClick={() => navigator.clipboard.writeText(promptParams.prompt)}
                  className="text-gray-600 hover:text-gray-400"
                  title="复制"
                >
                  <Copy size={10} />
                </button>
              </div>
              <p className="text-xs text-gray-300 line-clamp-2">
                {promptParams.prompt || (
                  <span className="italic text-gray-600">
                    在右侧面板输入 Prompt...
                  </span>
                )}
              </p>
            </div>

            {/* Negative prompt */}
            {promptParams.negativePrompt && (
              <div>
                <span className="text-[10px] text-gray-500 uppercase tracking-wider">
                  Negative
                </span>
                <p className="text-xs text-gray-400 line-clamp-1">
                  {promptParams.negativePrompt}
                </p>
              </div>
            )}

            {/* Params row */}
            <div className="flex flex-wrap gap-2">
              <ParamTag label="分辨率" value={promptParams.resolution} />
              <ParamTag label="时长" value={`${promptParams.duration}s`} />
              <ParamTag label="比例" value={promptParams.aspectRatio} />
            </div>

            {/* Generate button */}
            <button
              onClick={handleGenerate}
              disabled={isGenerating || !promptParams.prompt.trim()}
              className={clsx(
                'flex w-full items-center justify-center gap-2 rounded-lg py-2 text-xs font-medium transition-all',
                isGenerating || !promptParams.prompt.trim()
                  ? 'bg-bg-tertiary text-gray-600 cursor-not-allowed'
                  : 'text-white hover:opacity-90'
              )}
              style={
                !isGenerating && promptParams.prompt.trim()
                  ? { backgroundColor: color }
                  : {}
              }
            >
              {isGenerating ? (
                <>
                  <Square size={12} /> 生成中
                </>
              ) : (
                <>
                  <Play size={12} /> 生成
                </>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function ParamTag({ label, value }: { label: string; value: string }) {
  return (
    <span className="inline-flex items-center gap-1 rounded bg-bg-tertiary px-1.5 py-0.5 text-[10px] text-gray-400">
      <span className="text-gray-600">{label}</span>
      {value}
    </span>
  )
}
