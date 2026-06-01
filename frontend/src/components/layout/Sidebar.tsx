import { useAppStore } from '../../store/useAppStore'
import {
  Home,
  FolderKanban,
  GitCompare,
  BookMarked,
  Film,
  ListTodo,
  BarChart3,
  Settings,
} from 'lucide-react'
import clsx from 'clsx'

const navItems = [
  { id: 'home', label: '首页', icon: Home },
  { id: 'projects', label: '项目管理', icon: FolderKanban },
  { id: 'compare', label: '模型对比', icon: GitCompare },
  { id: 'prompts', label: 'Prompt 库', icon: BookMarked },
  { id: 'videos', label: '视频库', icon: Film },
  { id: 'queue', label: '任务队列', icon: ListTodo },
  { id: 'cost', label: '费用看板', icon: BarChart3 },
  { id: 'settings', label: '偏好设置', icon: Settings },
]

export default function Sidebar() {
  const { activeNav, setActiveNav } = useAppStore()

  return (
    <aside className="flex w-[180px] flex-col bg-bg-secondary border-r border-border">
      {/* Nav items */}
      <nav className="flex flex-1 flex-col gap-0.5 px-2 py-3">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive = activeNav === item.id
          return (
            <button
              key={item.id}
              onClick={() => setActiveNav(item.id)}
              className={clsx(
                'flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors',
                isActive
                  ? 'bg-accent/20 text-accent'
                  : 'text-gray-400 hover:bg-bg-tertiary hover:text-gray-200'
              )}
            >
              <Icon size={16} />
              <span>{item.label}</span>
            </button>
          )
        })}
      </nav>

      {/* User avatar at bottom */}
      <div className="border-t border-border p-3">
        <div className="flex items-center gap-2.5 px-1">
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-accent/30 text-xs text-accent">
            U
          </div>
          <span className="text-xs text-gray-400">个人中心</span>
        </div>
      </div>
    </aside>
  )
}
