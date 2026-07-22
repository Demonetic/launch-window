import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import {
    deleteLaunchNote,
    updateLaunchNote,
} from './notesApi'

interface UpdateNoteVariables {
    noteId: number
    content: string
}

export function useNoteActions() {
    const { token, user } = useAuth()
    const queryClient = useQueryClient()

    async function invalidateNotes() {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: [
                    'notes',
                    'overview',
                    user?.id,
                ],
            }),
            queryClient.invalidateQueries({
                queryKey: ['notes', 'launch'],
            }),
        ])
    }

    const updateMutation = useMutation({
        mutationFn: ({
                         noteId,
                         content,
                     }: UpdateNoteVariables) =>
            updateLaunchNote(token!, noteId, {
                content,
            }),
        onSuccess: invalidateNotes,
    })

    const deleteMutation = useMutation({
        mutationFn: (noteId: number) =>
            deleteLaunchNote(token!, noteId),
        onSuccess: invalidateNotes,
    })

    return {
        deleteError: deleteMutation.error,
        deletingNoteId: deleteMutation.isPending
            ? deleteMutation.variables
            : null,
        updateError: updateMutation.error,
        updatingNoteId: updateMutation.isPending
            ? updateMutation.variables?.noteId
            : null,
        deleteNote: deleteMutation.mutateAsync,
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