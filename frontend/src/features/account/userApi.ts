import { apiRequest } from '../../lib/api'
import type { User } from '../auth/types'

export function getCurrentUser(
    token: string,
): Promise<User> {
    return apiRequest<User>('/api/users/me', { token })
}