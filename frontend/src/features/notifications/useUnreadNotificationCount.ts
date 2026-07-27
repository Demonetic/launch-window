import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getUnreadNotificationCount } from './notificationApi'

export function useUnreadNotificationCount() {
    const {
        isAuthenticated,
        token,
        user,
    } = useAuth()

    const countQuery = useQuery({
        queryKey: [
            'notifications',
            user?.id,
            'unread-count',
        ],
        enabled:
            isAuthenticated &&
            Boolean(token),
        queryFn: () =>
            getUnreadNotificationCount(token!),
        refetchInterval: 10_000,
        refetchIntervalInBackground: true,
        refetchOnMount: 'always',
        refetchOnWindowFocus: 'always',
        staleTime: 5_000,
    })

    return {
        count: countQuery.data?.unreadCount ?? 0,
        isLoading: countQuery.isPending,
    }
}