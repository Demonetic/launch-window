import { apiRequest } from '../../lib/api'
import type {
    UpdateAvatarRequest,
    User,
} from '../auth/types'

export interface UserStatistics {
    savedLaunches: number
    notesWritten: number
    friends: number
}

export function getCurrentUser(
    token: string,
): Promise<User> {
    return apiRequest<User>('/api/users/me', {
        token,
    })
}

export function getUserStatistics(
    token: string,
): Promise<UserStatistics> {
    return apiRequest<UserStatistics>(
        '/api/users/me/statistics',
        {
            token,
        },
    )
}

export function updateAvatar(
    token: string,
    request: UpdateAvatarRequest,
): Promise<User> {
    return apiRequest<User>('/api/users/me/avatar', {
        method: 'PATCH',
        token,
        body: JSON.stringify(request),
    })
}

export function deleteAccount(
    token: string,
    password: string,
): Promise<void> {
    return apiRequest<void>('/api/users/me', {
        method: 'DELETE',
        token,
        body: JSON.stringify({ password }),
    })
}