import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { inviteToCalendar } from './calendarApi'

export function useInviteToCalendar(launchId: number) {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (identifier: string) =>
            inviteToCalendar(token!, launchId, {
                identifier,
            }),

        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ['calendar', user?.id],
            })
        },
    })
}