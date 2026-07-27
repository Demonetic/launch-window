import {
    BookOpen,
    StickyNote,
    Users,
} from 'lucide-react'
import type { NoteScope } from './types'
import './noteFilters.css'

interface NoteScopeFilterProps {
    value: NoteScope
    onChange: (scope: NoteScope) => void
}

const options: Array<{
    value: NoteScope
    label: string
    icon: typeof BookOpen
}> = [
    {
        value: 'ALL',
        label: 'All',
        icon: BookOpen,
    },
    {
        value: 'MINE',
        label: 'Mine',
        icon: StickyNote,
    },
    {
        value: 'FRIENDS',
        label: 'Friends',
        icon: Users,
    },
]

export function NoteScopeFilter({
                                    value,
                                    onChange,
                                }: NoteScopeFilterProps) {
    return (
        <div
            className="note-scope-filter"
            role="group"
            aria-label="Filter notes"
        >
            {options.map((option) => {
                const Icon = option.icon
                const isActive = value === option.value

                return (
                    <button
                        key={option.value}
                        type="button"
                        className={
                            isActive
                                ? 'note-scope-option active'
                                : 'note-scope-option'
                        }
                        aria-pressed={isActive}
                        onClick={() =>
                            onChange(option.value)
                        }
                    >
                        <Icon
                            size={14}
                            aria-hidden="true"
                        />
                        <span>{option.label}</span>
                    </button>
                )
            })}
        </div>
    )
}