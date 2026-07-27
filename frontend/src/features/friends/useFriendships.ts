import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    acceptFriendRequest,
    declineFriendRequest,
    getFriends,
    getReceivedFriendRequests,
    getSentFriendRequests,
    removeFriendship,
    sendFriendRequest,
} from './friendsApi'

type FriendRequestDecision = 'accept' | 'decline'

interface RespondVariables {
    friendshipId: number
    decision: FriendRequestDecision
}

export function useFriendships() {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    const queryKey = ['friends', user?.id]

    const friendsQuery = useQuery({
        queryKey: [...queryKey, 'accepted'],
        enabled: Boolean(token),
        queryFn: () => getFriends(token!),
    })

    const receivedQuery = useQuery({
        queryKey: [...queryKey, 'received'],
        enabled: Boolean(token),
        queryFn: () =>
            getReceivedFriendRequests(token!),
        refetchInterval: 15_000,
    })

    const sentQuery = useQuery({
        queryKey: [...queryKey, 'sent'],
        enabled: Boolean(token),
        queryFn: () =>
            getSentFriendRequests(token!),
    })

    async function invalidateFriendships() {
        await queryClient.invalidateQueries({
            queryKey,
        })
    }

    const sendMutation = useMutation({
        mutationFn: (identifier: string) =>
            sendFriendRequest(token!, {
                identifier,
            }),
        onSuccess: invalidateFriendships,
    })

    const respondMutation = useMutation({
        mutationFn: ({
                         friendshipId,
                         decision,
                     }: RespondVariables) => {
            if (decision === 'accept') {
                return acceptFriendRequest(
                    token!,
                    friendshipId,
                )
            }

            return declineFriendRequest(
                token!,
                friendshipId,
            )
        },
        onSuccess: invalidateFriendships,
    })

    const removeMutation = useMutation({
        mutationFn: (friendshipId: number) =>
            removeFriendship(
                token!,
                friendshipId,
            ),
        onSuccess: invalidateFriendships,
    })

    return {
        friends: friendsQuery.data ?? [],
        receivedRequests:
            receivedQuery.data ?? [],
        sentRequests: sentQuery.data ?? [],

        isPending:
            friendsQuery.isPending ||
            receivedQuery.isPending ||
            sentQuery.isPending,

        queryError:
            friendsQuery.error ??
            receivedQuery.error ??
            sentQuery.error,

        sendError: sendMutation.error,
        responseError: respondMutation.error,
        removeError: removeMutation.error,

        isSending: sendMutation.isPending,

        respondingToId:
            respondMutation.isPending
                ? respondMutation.variables
                ?.friendshipId ?? null
                : null,

        removingId:
            removeMutation.isPending
                ? removeMutation.variables ?? null
                : null,

        sendRequest: sendMutation.mutateAsync,

        respond: (
            friendshipId: number,
            decision: FriendRequestDecision,
        ) =>
            respondMutation.mutateAsync({
                friendshipId,
                decision,
            }),

        remove: removeMutation.mutateAsync,

        resetSendError: sendMutation.reset,
    }
}