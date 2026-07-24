import type { LaunchSummary } from '../launches/types'
import type { AvatarKey } from '../auth/types'

export interface CalendarEntry {
    id: number
    savedAt: string
    launch: LaunchSummary
    participants: CalendarParticipant[]
}

export interface CalendarParticipant {
    userId: number
    username: string
    avatarKey: AvatarKey
    avatarColor: string
}

export type CalendarInvitationStatus =
    | 'PENDING'
    | 'ACCEPTED'
    | 'DECLINED'

export interface CalendarInvitation {
    id: number
    launchId: number
    launchName: string
    launchTime: string
    inviterId: number
    inviterUsername: string
    inviterAvatarKey: AvatarKey
    inviterAvatarColor: string
    status: CalendarInvitationStatus
    createdAt: string
    respondedAt: string | null
}

export interface CreateCalendarInvitationRequest {
    identifier: string
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