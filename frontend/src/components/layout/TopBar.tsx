import { useAppStore } from '../../store/useAppStore'
import type { LayoutMode } from '../../types'
import {
  LayoutGrid,
  PanelTop,
  Rows3,
  Square,
  Bell,
  Globe,
  CreditCard,
  ChevronDown,
} from 'lucide-react'
import clsx from 'clsx'

const layouts: { mode: LayoutMode; icon: typeof LayoutGrid; label: string }[] = [
  { mode: 'grid', icon: LayoutGrid, label: '2×2' },
  { mode: '1+3', icon: PanelTop, label: '1+3' },
  { mode: 'horizontal', icon: Rows3, label: '横排' },
  { mode: 'single', icon: Square, label: '单窗口' },
]

export default function TopBar() {
  const { layout, setLayout } = useAppStore()

  return (
    <header className="flex h-11 items-center justify-between border-b border-border bg-bg-secondary px-4">
      {/* Layout switcher */}
      <div className="flex items-center gap-1 rounded-lg bg-bg-tertiary p-0.5">
        {layouts.map((l) => {
          const Icon = l.icon
          return (
            <button
              key={l.mode}
              onClick={() => setLayout(l.mode)}
              title={l.label}
              className={clsx(
                'flex h-7 items-center gap-1.5 rounded-md px-2 text-xs transition-colors',
                layout === l.mode
                  ? 'bg-accent text-white'
                  : 'text-gray-400 hover:text-gray-200'
              )}
            >
              <Icon size={14} />
              <span className="hidden sm:inline">{l.label}</span>
            </button>
          )
        })}
      </div>

      {/* Right side info */}
      <div className="flex items-center gap-4 text-xs text-gray-400">
        {/* Balance */}
        <div className="flex items-center gap-1.5">
          <CreditCard size={14} />
          <span>余额: <span className="text-white">$128.50</span></span>
        </div>
        <div className="h-4 w-px bg-border" />
        {/* Today's usage */}
        <div>
          今日消耗: <span className="text-warning">$3.20</span>
        </div>
        <div className="h-4 w-px bg-border" />
        {/* Notifications */}
        <button className="relative hover:text-white transition-colors">
          <Bell size={16} />
          <span className="absolute -right-1 -top-1 h-2 w-2 rounded-full bg-danger" />
        </button>
        {/* Language */}
        <button className="flex items-center gap-1 hover:text-white transition-colors">
          <Globe size={14} />
          <span>中文</span>
          <ChevronDown size={12} />
        </button>
      </div>
    </header>
  )
}
