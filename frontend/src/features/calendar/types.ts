import type { LaunchSummary } from '../launches/types'

export interface CalendarEntry {
    id: number
    savedAt: string
    launch: LaunchSummary
}

export interface CalendarCursor {
    time: string
    id: number
}

export interface CalendarPage {
    items: CalendarEntry[]
    previousCursor: CalendarCursor | null
    nextCursor: CalendarCursor | null
    hasPrevious: boolean
    hasNext: boolean
}

export interface SavedLaunchIdsResponse {
    savedLaunchIds: number[]
}

export type CalendarPageParameter =
    | {
    direction: 'previous' | 'next'
    cursor: CalendarCursor
}
    | null