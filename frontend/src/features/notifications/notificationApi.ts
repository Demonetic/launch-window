import { apiRequest } from '../../lib/api'
import type {
    Notification,
    UnreadNotificationCount,
} from './types'

export function getNotifications(
    token: string,
): Promise<Notification[]> {
    return apiRequest<Notification[]>(
        '/api/notifications',
        { token },
    )
}

export function getUnreadNotificationCount(
    token: string,
): Promise<UnreadNotificationCount> {
    return apiRequest<UnreadNotificationCount>(
        '/api/notifications/unread-count',
        { token },
    )
}

export function markNotificationRead(
    token: string,
    notificationId: number,
): Promise<void> {
    return apiRequest<void>(
        `/api/notifications/${notificationId}/read`,
        {
            method: 'PATCH',
            token,
        },
    )
}

export function markAllNotificationsRead(
    token: string,
): Promise<void> {
    return apiRequest<void>(
        '/api/notifications/read-all',
        {
            method: 'PATCH',
            token,
        },
    )
}