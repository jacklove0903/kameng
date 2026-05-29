import { useAppStore } from '../../store/useAppStore'
import GenerationCard from './GenerationCard'
import clsx from 'clsx'

export default function Workspace() {
  const { slots, layout } = useAppStore()

  const gridClass = clsx('flex-1 gap-2 p-3 overflow-auto', {
    'grid grid-cols-2 grid-rows-2': layout === 'grid',
    'grid grid-cols-[2fr_1fr] grid-rows-2': layout === '1+3',
    'flex flex-row': layout === 'horizontal',
    'flex': layout === 'single',
  })

  const visibleSlots = layout === 'single' ? [slots[0]] : slots

  return (
    <div className={gridClass}>
      {visibleSlots.map((slot, i) => (
        <GenerationCard
          key={slot.id}
          slot={slot}
          index={i}
          layout={layout}
        />
      ))}
    </div>
  )
}
