import { apiRequest } from '../../lib/api'
import type {
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    User,
} from './types'

export function login(
    request: LoginRequest,
): Promise<LoginResponse> {
    return apiRequest<LoginResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(request),
    })
}

export function register(
    request: RegisterRequest,
): Promise<User> {
    return apiRequest<User>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(request),
    })
}

export function requestPasswordReset(
    email: string,
): Promise<void> {
    return apiRequest<void>('/api/auth/password/forgot', {
        method: 'POST',
        body: JSON.stringify({ email }),
    })
}

export function resetPassword(
    token: string,
    newPassword: string,
): Promise<void> {
    return apiRequest<void>('/api/auth/password/reset', {
        method: 'POST',
        body: JSON.stringify({
            token,
            newPassword,
        }),
    })
}