import {
    useEffect,
} from 'react'
import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import {
    acceptCalendarInvitation,
    declineCalendarInvitation,
} from '../calendar/calendarApi'
import { useAuth } from '../auth/useAuth'
import {
    acceptFriendRequest,
    declineFriendRequest,
} from '../friends/friendsApi'
import {
    getNotifications,
    markAllNotificationsRead,
} from './notificationApi'
import type {
    Notification,
    NotificationDecision,
} from './types'

interface RespondVariables {
    notification: Notification
    decision: NotificationDecision
}

export function useNotifications() {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    const queryKey = [
        'notifications',
        user?.id,
        'latest',
    ]

    const notificationsQuery = useQuery({
        queryKey,
        enabled: Boolean(token),
        queryFn: () => getNotifications(token!),
        refetchInterval: 10_000,
        refetchIntervalInBackground: true,
        refetchOnMount: 'always',
        refetchOnWindowFocus: 'always',
        staleTime: 5_000,
    })

    const markAllMutation = useMutation({
        mutationFn: () =>
            markAllNotificationsRead(token!),

        onSuccess: async () => {
            queryClient.setQueryData<Notification[]>(
                queryKey,
                (current) =>
                    current?.map((notification) => ({
                        ...notification,
                        read: true,
                    })),
            )

            await queryClient.invalidateQueries({
                queryKey: [
                    'notifications',
                    user?.id,
                    'unread-count',
                ],
            })
        },
    })

    const notifications =
        notificationsQuery.data ?? []

    const hasUnreadNotifications =
        notifications.some(
            (notification) => !notification.read,
        )

    const {
        mutate: markAllRead,
        isPending: isMarkingAllRead,
    } = markAllMutation

    useEffect(() => {
        if (
            !hasUnreadNotifications ||
            isMarkingAllRead
        ) {
            return
        }

        markAllRead()
    }, [
        hasUnreadNotifications,
        isMarkingAllRead,
        markAllRead,
    ])

    const responseMutation = useMutation({
        mutationFn: async ({
                               notification,
                               decision,
                           }: RespondVariables) => {
            if (
                notification.type ===
                'FRIEND_REQUEST_RECEIVED'
            ) {
                if (notification.friendshipId === null) {
                    throw new Error(
                        'Friend request is unavailable.',
                    )
                }

                if (decision === 'accept') {
                    return acceptFriendRequest(
                        token!,
                        notification.friendshipId,
                    )
                }

                return declineFriendRequest(
                    token!,
                    notification.friendshipId,
                )
            }

            if (
                notification.type ===
                'CALENDAR_INVITATION_RECEIVED'
            ) {
                if (
                    notification.calendarInvitationId ===
                    null
                ) {
                    throw new Error(
                        'Calendar invitation is unavailable.',
                    )
                }

                if (decision === 'accept') {
                    return acceptCalendarInvitation(
                        token!,
                        notification.calendarInvitationId,
                    )
                }

                return declineCalendarInvitation(
                    token!,
                    notification.calendarInvitationId,
                )
            }

            throw new Error(
                'This message cannot be answered.',
            )
        },

        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: [
                        'notifications',
                        user?.id,
                    ],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        'friends',
                        user?.id,
                    ],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        'calendar',
                        'invitations',
                        'pending',
                        user?.id,
                    ],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        'calendar',
                        user?.id,
                    ],
                }),
            ])
        },
    })

    return {
        notifications,

        isLoading: notificationsQuery.isPending,
        isError:
            notificationsQuery.isError ||
            responseMutation.isError,

        error:
            responseMutation.error ??
            notificationsQuery.error,

        respondingToId:
            responseMutation.isPending
                ? responseMutation.variables
                ?.notification.id ?? null
                : null,

        respond: (
            notification: Notification,
            decision: NotificationDecision,
        ) =>
            responseMutation.mutateAsync({
                notification,
                decision,
            }),
    }
}