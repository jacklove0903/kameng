import { useAppStore } from '../../store/useAppStore'
import {
  LayoutDashboard,
  Wand2,
  Boxes,
  BookMarked,
  Clock,
  Wallet,
  Settings,
  HardDrive,
} from 'lucide-react'
import clsx from 'clsx'

const navItems = [
  { id: 'dashboard', label: '仪表盘', icon: LayoutDashboard },
  { id: 'workspace', label: '生成工作区', icon: Wand2 },
  { id: 'providers', label: '模型提供商', icon: Boxes },
  { id: 'prompts', label: 'Prompt 库', icon: BookMarked },
  { id: 'history', label: '历史记录', icon: Clock },
  { id: 'cost', label: '费用管理', icon: Wallet },
]

export default function Sidebar() {
  const { activeNav, setActiveNav } = useAppStore()

  return (
    <aside className="flex w-[60px] flex-col bg-bg-secondary border-r border-border">
      {/* Logo */}
      <div className="flex h-12 items-center justify-center border-b border-border">
        <Wand2 size={22} className="text-accent" />
      </div>

      {/* Nav items */}
      <nav className="flex flex-1 flex-col items-center gap-1 py-3">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive = activeNav === item.id
          return (
            <button
              key={item.id}
              onClick={() => setActiveNav(item.id)}
              title={item.label}
              className={clsx(
                'flex h-10 w-10 items-center justify-center rounded-lg transition-colors',
                isActive
                  ? 'bg-accent/20 text-accent'
                  : 'text-gray-500 hover:bg-bg-tertiary hover:text-gray-300'
              )}
            >
              <Icon size={18} />
            </button>
          )
        })}
      </nav>

      {/* Bottom section */}
      <div className="flex flex-col items-center gap-1 border-t border-border py-3">
        <button
          title="设置"
          className="flex h-10 w-10 items-center justify-center rounded-lg text-gray-500 hover:bg-bg-tertiary hover:text-gray-300 transition-colors"
        >
          <Settings size={18} />
        </button>
      </div>

      {/* User info at bottom */}
      <div className="border-t border-border p-2">
        <div className="flex flex-col items-center gap-1">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accent/30 text-xs text-accent">
            U
          </div>
          <div className="flex items-center gap-1 text-[10px] text-gray-500">
            <HardDrive size={10} />
            <span>2.1G</span>
          </div>
        </div>
      </div>
    </aside>
  )
}
