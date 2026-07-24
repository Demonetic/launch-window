import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    acceptCalendarInvitation,
    declineCalendarInvitation,
    getPendingCalendarInvitations,
} from './calendarApi'

type InvitationDecision = 'accept' | 'decline'

interface RespondToInvitationVariables {
    invitationId: number
    decision: InvitationDecision
}

export function useCalendarInvitations() {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    const invitationsQuery = useQuery({
        queryKey: [
            'calendar',
            'invitations',
            'pending',
            user?.id,
        ],
        enabled: Boolean(token),
        queryFn: () =>
            getPendingCalendarInvitations(token!),
    })

    const responseMutation = useMutation({
        mutationFn: ({
                         invitationId,
                         decision,
                     }: RespondToInvitationVariables) => {
            if (decision === 'accept') {
                return acceptCalendarInvitation(
                    token!,
                    invitationId,
                )
            }

            return declineCalendarInvitation(
                token!,
                invitationId,
            )
        },

        onSuccess: async (_, variables) => {
            await queryClient.invalidateQueries({
                queryKey: [
                    'calendar',
                    'invitations',
                    'pending',
                    user?.id,
                ],
            })

            if (variables.decision === 'accept') {
                await queryClient.invalidateQueries({
                    queryKey: ['calendar', user?.id],
                })
            }
        },
    })

    return {
        invitations: invitationsQuery.data ?? [],
        error:
            invitationsQuery.error ??
            responseMutation.error,
        isError:
            invitationsQuery.isError ||
            responseMutation.isError,
        isLoading: invitationsQuery.isPending,
        isResponding: responseMutation.isPending,
        respondingToId:
            responseMutation.variables?.invitationId ??
            null,
        respond: responseMutation.mutate,
    }
}