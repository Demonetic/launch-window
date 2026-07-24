import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    deleteLaunchNote,
    updateLaunchNote,
} from './notesApi'
import type { LaunchNote } from './types'

interface UpdateNoteVariables {
    noteId: number
    content: string
}

export function useNoteActions() {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    async function invalidateOverview() {
        await queryClient.invalidateQueries({
            queryKey: [
                'notes',
                'overview',
                user?.id,
            ],
        })
    }

    const updateMutation = useMutation({
        mutationFn: ({
                         noteId,
                         content,
                     }: UpdateNoteVariables) =>
            updateLaunchNote(token!, noteId, {
                content,
            }),

        onSuccess: async (updatedNote) => {
            queryClient.setQueriesData<LaunchNote[]>(
                {
                    queryKey: [
                        'notes',
                        'launch',
                    ],
                },
                (currentNotes) =>
                    currentNotes?.map((note) =>
                        note.id === updatedNote.id
                            ? updatedNote
                            : note,
                    ),
            )

            await invalidateOverview()
        },
    })

    const deleteMutation = useMutation({
        mutationFn: (noteId: number) =>
            deleteLaunchNote(token!, noteId),

        onSuccess: async (_, deletedNoteId) => {
            queryClient.setQueriesData<LaunchNote[]>(
                {
                    queryKey: [
                        'notes',
                        'launch',
                    ],
                },
                (currentNotes) =>
                    currentNotes?.filter(
                        (note) =>
                            note.id !== deletedNoteId,
                    ),
            )

            await invalidateOverview()
        },
    })

    return {
        deleteError: deleteMutation.error,

        deletingNoteId:
            deleteMutation.isPending
                ? deleteMutation.variables
                : null,

        updateError: updateMutation.error,

        updatingNoteId:
            updateMutation.isPending
                ? updateMutation.variables?.noteId
                : null,

        deleteNote:
        deleteMutation.mutateAsync,

        updateNote: (
            noteId: number,
            content: string,
        ) =>
            updateMutation.mutateAsync({
                noteId,
                content,
            }),
    }
}