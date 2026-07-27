import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getUserStatistics } from './userApi'

export function useUserStatistics() {
    const { token, user } = useAuth()

    return useQuery({
        queryKey: [
            'users',
            'me',
            user?.id,
            'statistics',
        ],
        queryFn: () => getUserStatistics(token!),
        enabled: Boolean(token),
    })
}