import { apiRequest } from '../../lib/api'
import type {
    CreateFriendRequest,
    Friendship,
} from './types'

export function getFriends(
    token: string,
): Promise<Friendship[]> {
    return apiRequest<Friendship[]>(
        '/api/friends',
        { token },
    )
}

export function getReceivedFriendRequests(
    token: string,
): Promise<Friendship[]> {
    return apiRequest<Friendship[]>(
        '/api/friends/requests/received',
        { token },
    )
}

export function getSentFriendRequests(
    token: string,
): Promise<Friendship[]> {
    return apiRequest<Friendship[]>(
        '/api/friends/requests/sent',
        { token },
    )
}

export function sendFriendRequest(
    token: string,
    request: CreateFriendRequest,
): Promise<Friendship> {
    return apiRequest<Friendship>(
        '/api/friends/requests',
        {
            method: 'POST',
            token,
            body: JSON.stringify(request),
        },
    )
}

export function acceptFriendRequest(
    token: string,
    friendshipId: number,
): Promise<Friendship> {
    return apiRequest<Friendship>(
        `/api/friends/requests/${friendshipId}/accept`,
        {
            method: 'PATCH',
            token,
        },
    )
}

export function declineFriendRequest(
    token: string,
    friendshipId: number,
): Promise<Friendship> {
    return apiRequest<Friendship>(
        `/api/friends/requests/${friendshipId}/decline`,
        {
            method: 'PATCH',
            token,
        },
    )
}

export function removeFriendship(
    token: string,
    friendshipId: number,
): Promise<void> {
    return apiRequest<void>(
        `/api/friends/${friendshipId}`,
        {
            method: 'DELETE',
            token,
        },
    )
}