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