import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getPendingCalendarInvitations } from './calendarApi'

export function usePendingCalendarInvitations() {
    const {
        isAuthenticated,
        token,
        user,
    } = useAuth()

    const invitationsQuery = useQuery({
        queryKey: [
            'calendar',
            'invitations',
            'pending',
            user?.id,
        ],
        enabled:
            isAuthenticated &&
            Boolean(token),
        queryFn: () =>
            getPendingCalendarInvitations(token!),
        refetchInterval: 10_000,
        refetchIntervalInBackground: true,
        refetchOnMount: 'always',
        refetchOnWindowFocus: 'always',
        staleTime: 5_000,
    })

    return {
        count: invitationsQuery.data?.length ?? 0,
        invitations:
            invitationsQuery.data ?? [],
        isLoading: invitationsQuery.isPending,
    }
}