export type UserRole = 'USER' | 'ADMIN'

export interface User {
    id: number
    username: string
    email: string
    role: UserRole
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