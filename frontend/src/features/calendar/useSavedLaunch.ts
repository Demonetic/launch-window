import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    getSavedLaunchIds,
    removeLaunch,
    saveLaunch,
} from './calendarApi'

export function useSavedLaunch(launchId: number) {
    const { isAuthenticated, token, user } = useAuth()
    const queryClient = useQueryClient()

    const savedQuery = useQuery({
        queryKey: [
            'calendar',
            'saved-launch',
            user?.id,
            launchId,
        ],
        enabled: isAuthenticated && Boolean(token),
        queryFn: async () => {
            const response = await getSavedLaunchIds(
                token!,
                [launchId],
            )

            return response.savedLaunchIds.includes(launchId)
        },
    })

    const isSaved = savedQuery.data ?? false

    const toggleMutation = useMutation({
        mutationFn: async () => {
            if (isSaved) {
                await removeLaunch(token!, launchId)
                return
            }

            await saveLaunch(token!, launchId)
        },
        onSuccess: async () => {
            queryClient.setQueryData(
                [
                    'calendar',
                    'saved-launch',
                    user?.id,
                    launchId,
                ],
                !isSaved,
            )

            await queryClient.invalidateQueries({
                queryKey: ['calendar', user?.id],
            })
        },
    })

    return {
        isAuthenticated,
        isError: savedQuery.isError || toggleMutation.isError,
        isLoading:
            savedQuery.isPending && isAuthenticated,
        isPending: toggleMutation.isPending,
        isSaved,
        toggle: toggleMutation.mutate,
    }
}