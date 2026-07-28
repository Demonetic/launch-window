import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/session/useAuth'
import { getFriends } from './friendsApi'

export function useAcceptedFriends(
    enabled: boolean,
) {
    const { token, user } = useAuth()

    return useQuery({
        queryKey: [
            'friends',
            user?.id,
            'accepted',
        ],
        enabled: enabled && Boolean(token),
        queryFn: () => getFriends(token!),
        staleTime: 30_000,
    })
}