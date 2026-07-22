import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    createLaunchNote,
    getLaunchNotes,
} from './notesApi'

export function useLaunchNotes(launchId: number) {
    const {
        isAuthenticated,
        token,
        user,
    } = useAuth()

    const queryClient = useQueryClient()

    const notesQuery = useQuery({
        queryKey: [
            'notes',
            'launch',
            user?.id,
            launchId,
        ],
        enabled: isAuthenticated && Boolean(token),
        queryFn: () =>
            getLaunchNotes(token!, launchId),
    })

    const createMutation = useMutation({
        mutationFn: (content: string) =>
            createLaunchNote(
                token!,
                launchId,
                { content },
            ),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: [
                        'notes',
                        'launch',
                        user?.id,
                        launchId,
                    ],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        'notes',
                        'overview',
                        user?.id,
                    ],
                }),
            ])
        },
    })

    return {
        isAuthenticated,
        notes: notesQuery.data ?? [],
        isError:
            notesQuery.isError ||
            createMutation.isError,
        isLoading:
            notesQuery.isPending &&
            isAuthenticated,
        isCreating: createMutation.isPending,
        error:
            createMutation.error ??
            notesQuery.error,
        createNote: createMutation.mutateAsync,
    }
}