import type { AvatarKey } from '../auth/model/types'
import type { CalendarInvitationStatus } from '../calendar/model/types'
import type { FriendshipStatus } from '../friends/types'

export type NotificationType =
    | 'FRIEND_REQUEST_RECEIVED'
    | 'FRIEND_REQUEST_ACCEPTED'
    | 'FRIEND_REQUEST_DECLINED'
    | 'CALENDAR_INVITATION_RECEIVED'
    | 'CALENDAR_INVITATION_ACCEPTED'
    | 'CALENDAR_INVITATION_DECLINED'

export interface Notification {
    id: number
    type: NotificationType
    read: boolean
    createdAt: string

    actorId: number
    actorUsername: string
    actorAvatarKey: AvatarKey
    actorAvatarColor: string

    friendshipId: number | null
    friendshipStatus: FriendshipStatus | null

    calendarInvitationId: number | null
    calendarInvitationStatus:
        CalendarInvitationStatus | null

    launchId: number | null
    launchName: string | null
    launchTime: string | null
}

export interface UnreadNotificationCount {
    unreadCount: number
}

export type NotificationDecision =
    | 'accept'
    | 'decline'