import { useAppStore } from '../../store/useAppStore'
import {
  Send,
  ImagePlus,
  SlidersHorizontal,
  RotateCcw,
  ChevronDown,
} from 'lucide-react'
import clsx from 'clsx'

export default function PromptPanel() {
  const { promptParams, setPromptParams, slots } = useAppStore()

  const handleSyncAll = () => {
    // Prompt is already shared via store, this just triggers a visual feedback
  }

  return (
    <aside className="flex w-[300px] flex-col border-l border-border bg-bg-secondary overflow-y-auto">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border px-4 py-2.5">
        <h3 className="text-xs font-semibold text-gray-300 tracking-wider uppercase">
          参数配置
        </h3>
        <button
          onClick={handleSyncAll}
          className="flex items-center gap-1 rounded-md bg-accent/20 px-2 py-1 text-[10px] text-accent hover:bg-accent/30 transition-colors"
          title="同步 Prompt 到所有窗口"
        >
          <Send size={10} />
          全部同步
        </button>
      </div>

      <div className="flex-1 p-4 space-y-4">
        {/* Prompt */}
        <div>
          <label className="mb-1.5 flex items-center justify-between">
            <span className="text-[11px] font-medium text-gray-400 uppercase tracking-wider">
              Prompt
            </span>
            <button
              onClick={() => setPromptParams({ prompt: '' })}
              className="text-gray-600 hover:text-gray-400"
              title="清空"
            >
              <RotateCcw size={10} />
            </button>
          </label>
          <textarea
            value={promptParams.prompt}
            onChange={(e) => setPromptParams({ prompt: e.target.value })}
            placeholder="描述你想生成的视频内容..."
            rows={4}
            className="w-full resize-none rounded-lg border border-border bg-bg-primary px-3 py-2 text-xs text-gray-200 placeholder-gray-600 outline-none focus:border-accent transition-colors"
          />
        </div>

        {/* Negative Prompt */}
        <div>
          <label className="mb-1.5 block text-[11px] font-medium text-gray-400 uppercase tracking-wider">
            Negative Prompt
          </label>
          <textarea
            value={promptParams.negativePrompt}
            onChange={(e) =>
              setPromptParams({ negativePrompt: e.target.value })
            }
            placeholder="不希望出现的内容..."
            rows={2}
            className="w-full resize-none rounded-lg border border-border bg-bg-primary px-3 py-2 text-xs text-gray-200 placeholder-gray-600 outline-none focus:border-accent transition-colors"
          />
        </div>

        {/* Reference images */}
        <div>
          <label className="mb-1.5 block text-[11px] font-medium text-gray-400 uppercase tracking-wider">
            参考图
          </label>
          <div className="grid grid-cols-2 gap-2">
            <ImageUploadButton
              label="首帧"
              url={promptParams.imageUrl}
              onChange={(url) => setPromptParams({ imageUrl: url })}
            />
            <ImageUploadButton
              label="尾帧"
              url={promptParams.endFrameUrl}
              onChange={(url) => setPromptParams({ endFrameUrl: url })}
            />
          </div>
        </div>

        {/* Divider */}
        <div className="flex items-center gap-2 text-[10px] text-gray-600 uppercase tracking-wider">
          <SlidersHorizontal size={10} />
          <span>高级参数</span>
          <div className="flex-1 h-px bg-border" />
        </div>

        {/* Duration */}
        <ParamSlider
          label="视频时长"
          value={promptParams.duration}
          min={1}
          max={10}
          step={1}
          unit="s"
          onChange={(v) => setPromptParams({ duration: v })}
        />

        {/* Resolution */}
        <ParamSelect
          label="分辨率"
          value={promptParams.resolution}
          options={[
            { value: '720p', label: '720p' },
            { value: '1080p', label: '1080p' },
            { value: '4K', label: '4K' },
          ]}
          onChange={(v) => setPromptParams({ resolution: v })}
        />

        {/* Aspect Ratio */}
        <ParamSelect
          label="宽高比"
          value={promptParams.aspectRatio}
          options={[
            { value: '16:9', label: '16:9 横屏' },
            { value: '9:16', label: '9:16 竖屏' },
            { value: '1:1', label: '1:1 方形' },
          ]}
          onChange={(v) => setPromptParams({ aspectRatio: v })}
        />

        {/* Motion Strength */}
        <ParamSlider
          label="运动强度"
          value={promptParams.motionStrength}
          min={0}
          max={1}
          step={0.1}
          unit=""
          onChange={(v) => setPromptParams({ motionStrength: v })}
        />

        {/* Sampling Steps */}
        <ParamSlider
          label="采样步数"
          value={promptParams.samplingSteps}
          min={10}
          max={50}
          step={1}
          unit=""
          onChange={(v) => setPromptParams({ samplingSteps: v })}
        />

        {/* Active providers info */}
        <div className="rounded-lg bg-bg-primary p-3 space-y-1.5">
          <span className="text-[10px] text-gray-600 uppercase tracking-wider">
            当前窗口
          </span>
          <div className="flex flex-wrap gap-1.5">
            {slots.map((slot) => (
              <span
                key={slot.id}
                className="rounded bg-bg-tertiary px-1.5 py-0.5 text-[10px] text-gray-400"
              >
                {slot.provider}
              </span>
            ))}
          </div>
        </div>
      </div>
    </aside>
  )
}

function ImageUploadButton({
  label,
  url,
  onChange,
}: {
  label: string
  url: string
  onChange: (url: string) => void
}) {
  return (
    <button
      onClick={() => {
        const input = prompt(`输入${label}图片 URL:`)
        if (input) onChange(input)
      }}
      className={clsx(
        'flex h-20 flex-col items-center justify-center gap-1 rounded-lg border border-dashed transition-colors',
        url
          ? 'border-accent/50 bg-accent/5'
          : 'border-border hover:border-gray-500 hover:bg-bg-tertiary'
      )}
    >
      {url ? (
        <img
          src={url}
          alt={label}
          className="h-full w-full rounded-lg object-cover"
        />
      ) : (
        <>
          <ImagePlus size={16} className="text-gray-600" />
          <span className="text-[10px] text-gray-600">{label}</span>
        </>
      )}
    </button>
  )
}

function ParamSlider({
  label,
  value,
  min,
  max,
  step,
  unit,
  onChange,
}: {
  label: string
  value: number
  min: number
  max: number
  step: number
  unit: string
  onChange: (v: number) => void
}) {
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between">
        <span className="text-[11px] text-gray-400">{label}</span>
        <span className="text-[11px] text-gray-300 font-mono">
          {value}{unit}
        </span>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full h-1 rounded-full appearance-none bg-bg-tertiary cursor-pointer accent-accent"
      />
    </div>
  )
}

function ParamSelect({
  label,
  value,
  options,
  onChange,
}: {
  label: string
  value: string
  options: { value: string; label: string }[]
  onChange: (v: string) => void
}) {
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between">
        <span className="text-[11px] text-gray-400">{label}</span>
      </div>
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full appearance-none rounded-lg border border-border bg-bg-primary px-3 py-1.5 pr-7 text-xs text-gray-200 outline-none focus:border-accent cursor-pointer"
        >
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <ChevronDown
          size={12}
          className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-600 pointer-events-none"
        />
      </div>
    </div>
  )
}
