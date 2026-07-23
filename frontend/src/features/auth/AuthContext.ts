import { createContext } from 'react'
import type {
    AuthSession,
    LoginRequest,
    RegisterRequest,
    User,
} from './types'

export interface AuthContextValue {
    session: AuthSession | null
    user: User | null
    token: string | null
    isAuthenticated: boolean
    login: (request: LoginRequest) => Promise<void>
    register: (request: RegisterRequest) => Promise<User>
    logout: () => void
    updateUser: (user: User) => void
}

export const AuthContext =
    createContext<AuthContextValue | null>(null)