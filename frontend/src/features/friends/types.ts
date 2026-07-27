import type { AvatarKey } from '../auth/types'

export type FriendshipStatus =
    | 'PENDING'
    | 'ACCEPTED'
    | 'DECLINED'

export interface Friendship {
    id: number
    userId: number
    username: string
    avatarKey: AvatarKey
    avatarColor: string
    status: FriendshipStatus
    requestedByCurrentUser: boolean
    createdAt: string
    respondedAt: string | null
}

export interface CreateFriendRequest {
    identifier: string
}