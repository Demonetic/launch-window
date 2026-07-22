import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getCurrentUser } from './userApi'

export function useCurrentUser() {
    const { token, user } = useAuth()

    return useQuery({
        queryKey: ['users', 'me', user?.id],
        queryFn: () => getCurrentUser(token!),
        enabled: Boolean(token),
    })
}