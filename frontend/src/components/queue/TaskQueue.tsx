import { useAppStore } from '../../store/useAppStore'
import type { TaskStatus } from '../../types'
import {
  ChevronUp,
  ChevronDown,
  RotateCcw,
  Download,
  Trash2,
  Loader2,
} from 'lucide-react'
import clsx from 'clsx'

const statusConfig: Record<TaskStatus, { label: string; color: string }> = {
  PENDING: { label: '等待中', color: 'text-gray-500' },
  RUNNING: { label: '生成中', color: 'text-accent' },
  SUCCESS: { label: '已完成', color: 'text-success' },
  FAILED: { label: '失败', color: 'text-danger' },
}

export default function TaskQueue() {
  const { tasks, queueVisible, toggleQueue } = useAppStore()
  const runningCount = tasks.filter((t) => t.status === 'RUNNING').length
  const pendingCount = tasks.filter((t) => t.status === 'PENDING').length

  return (
    <div className="border-t border-border bg-bg-secondary">
      {/* Toggle bar */}
      <button
        onClick={toggleQueue}
        className="flex w-full items-center justify-between px-4 py-1.5 text-xs text-gray-400 hover:text-gray-200 transition-colors"
      >
        <div className="flex items-center gap-3">
          <span className="font-medium">任务队列</span>
          {runningCount > 0 && (
            <span className="flex items-center gap-1 text-accent">
              <Loader2 size={10} className="animate-spin" />
              {runningCount} 运行中
            </span>
          )}
          {pendingCount > 0 && (
            <span className="text-gray-600">{pendingCount} 排队</span>
          )}
          <span className="text-gray-600">共 {tasks.length} 条</span>
        </div>
        {queueVisible ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
      </button>

      {/* Queue list */}
      {queueVisible && (
        <div className="max-h-[180px] overflow-y-auto border-t border-border">
          {tasks.length === 0 ? (
            <div className="flex items-center justify-center py-6 text-xs text-gray-600">
              暂无任务
            </div>
          ) : (
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-border text-gray-600">
                  <th className="px-3 py-1.5 text-left font-normal">ID</th>
                  <th className="px-3 py-1.5 text-left font-normal">提供商</th>
                  <th className="px-3 py-1.5 text-left font-normal">Prompt</th>
                  <th className="px-3 py-1.5 text-left font-normal">状态</th>
                  <th className="px-3 py-1.5 text-left font-normal">时间</th>
                  <th className="px-3 py-1.5 text-right font-normal">操作</th>
                </tr>
              </thead>
              <tbody>
                {tasks.map((task) => {
                  const sc = statusConfig[task.status]
                  return (
                    <tr
                      key={task.id}
                      className="border-b border-border/50 hover:bg-bg-tertiary/50 transition-colors"
                    >
                      <td className="px-3 py-1.5 text-gray-500 font-mono">
                        #{task.id}
                      </td>
                      <td className="px-3 py-1.5 text-gray-300">
                        {task.provider}
                      </td>
                      <td className="px-3 py-1.5 text-gray-400 max-w-[200px] truncate">
                        {task.prompt}
                      </td>
                      <td className="px-3 py-1.5">
                        <span className={clsx('flex items-center gap-1', sc.color)}>
                          {task.status === 'RUNNING' && (
                            <Loader2 size={10} className="animate-spin" />
                          )}
                          {sc.label}
                        </span>
                      </td>
                      <td className="px-3 py-1.5 text-gray-600">
                        {formatTime(task.createdAt)}
                      </td>
                      <td className="px-3 py-1.5">
                        <div className="flex items-center justify-end gap-1">
                          {task.status === 'SUCCESS' && task.videoUrl && (
                            <button
                              onClick={() => window.open(task.videoUrl!, '_blank')}
                              className="rounded p-0.5 text-gray-500 hover:text-success"
                              title="下载"
                            >
                              <Download size={12} />
                            </button>
                          )}
                          {task.status === 'FAILED' && (
                            <button
                              className="rounded p-0.5 text-gray-500 hover:text-accent"
                              title="重试"
                            >
                              <RotateCcw size={12} />
                            </button>
                          )}
                          <button
                            className="rounded p-0.5 text-gray-600 hover:text-danger"
                            title="删除"
                          >
                            <Trash2 size={12} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  return date.toLocaleDateString('zh-CN')
}
