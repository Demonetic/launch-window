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
import type { LaunchNote } from './types'

export function useLaunchNotes(launchId: number) {
    const {
        isAuthenticated,
        token,
        user,
    } = useAuth()

    const queryClient = useQueryClient()

    const launchNotesQueryKey = [
        'notes',
        'launch',
        user?.id,
        launchId,
    ] as const

    const notesQuery = useQuery({
        queryKey: launchNotesQueryKey,
        enabled:
            isAuthenticated &&
            Boolean(token),
        queryFn: () =>
            getLaunchNotes(token!, launchId),
        refetchInterval: 10_000,
        refetchIntervalInBackground: true,
        refetchOnMount: 'always',
        refetchOnWindowFocus: 'always',
        staleTime: 5_000,
    })

    const createMutation = useMutation({
        mutationFn: (content: string) =>
            createLaunchNote(
                token!,
                launchId,
                { content },
            ),

        onSuccess: async (createdNote) => {
            queryClient.setQueryData<LaunchNote[]>(
                launchNotesQueryKey,
                (currentNotes = []) => [
                    createdNote,
                    ...currentNotes.filter(
                        (note) =>
                            note.id !== createdNote.id,
                    ),
                ],
            )

            await queryClient.invalidateQueries({
                queryKey: [
                    'notes',
                    'overview',
                    user?.id,
                ],
            })
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

        isCreating:
        createMutation.isPending,

        error:
            createMutation.error ??
            notesQuery.error,

        createNote:
        createMutation.mutateAsync,
    }
}