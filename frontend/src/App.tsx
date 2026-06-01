import { useEffect } from 'react'
import Sidebar from './components/layout/Sidebar'
import TopBar from './components/layout/TopBar'
import Workspace from './components/workspace/Workspace'
import PromptPanel from './components/prompt/PromptPanel'
import TaskQueue from './components/queue/TaskQueue'
import { useAppStore } from './store/useAppStore'
import { generateApi, providerApi } from './api'

function App() {
  const { setProviders, setTasks, pollingIds, updateTask, removePollingId, updateSlot } =
    useAppStore()

  // Load initial data
  useEffect(() => {
    providerApi.list().then((providers) => setProviders(providers))
    generateApi.recent(50).then((tasks) => setTasks(tasks))
  }, [])

  // Poll running tasks
  useEffect(() => {
    if (pollingIds.length === 0) return
    const timer = setInterval(() => {
      pollingIds.forEach((id) => {
        generateApi.getTask(id).then((task) => {
          if (!task) return
          updateTask(id, task)
          // Also update the slot that has this task
          const { slots } = useAppStore.getState()
          const slotIndex = slots.findIndex((s) => s.task?.id === id)
          if (slotIndex !== -1) {
            updateSlot(slotIndex, { task })
          }
          if (task.status === 'SUCCESS' || task.status === 'FAILED') {
            removePollingId(id)
          }
        })
      })
    }, 2000)
    return () => clearInterval(timer)
  }, [pollingIds])

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-bg-primary">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar />
        <div className="flex flex-1 overflow-hidden">
          <div className="flex flex-1 flex-col overflow-hidden">
            <Workspace />
          </div>
          <PromptPanel />
        </div>
        <TaskQueue />
      </div>
    </div>
  )
}

export default App
