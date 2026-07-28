export type UserRole = 'USER' | 'ADMIN'
export type AvatarKey =
    | 'ASTRONAUT'
    | 'ALIEN'
    | 'MOON_BASE_ROBOT'
    | 'ROCKET'
    | 'SATELLITE'
    | 'PLANET'
    | 'LUNAR_ROVER'
    | 'TELESCOPE'

export interface User {
    id: number
    username: string
    email: string
    role: UserRole
    avatarKey: AvatarKey
    avatarColor: string
}

export interface LoginRequest {
    identifier: string
    password: string
}

export interface LoginResponse {
    accessToken: string
    tokenType: string
    expiresIn: number
    user: User
}

export interface RegisterRequest {
    username: string
    email: string
    password: string
}

export interface AuthSession {
    token: string
    expiresAt: number
    user: User
}

export interface UpdateAvatarRequest {
    avatarKey: AvatarKey
    avatarColor: string
}