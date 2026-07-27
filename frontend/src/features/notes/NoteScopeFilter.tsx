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
    description: string
    icon: typeof BookOpen
}> = [
    {
        value: 'ALL',
        label: 'All notes',
        description: 'Your notes and shared notes',
        icon: BookOpen,
    },
    {
        value: 'MINE',
        label: 'My notes',
        description: 'Notes written by you',
        icon: StickyNote,
    },
    {
        value: 'FRIENDS',
        label: "Friends' notes",
        description: 'Notes shared through your calendar',
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
                        <span className="note-scope-icon">
                            <Icon
                                size={17}
                                aria-hidden="true"
                            />
                        </span>

                        <span className="note-scope-copy">
                            <strong>{option.label}</strong>
                            <small>
                                {option.description}
                            </small>
                        </span>
                    </button>
                )
            })}
        </div>
    )
}