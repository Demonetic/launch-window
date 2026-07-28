import { useInfiniteQuery } from '@tanstack/react-query'
import { useAuth } from '../../auth/session/useAuth'
import { getNotesPage } from '../api/notesApi'
import type { NoteCursor, NoteScope } from '../model/types'

export function useNotesOverview(scope: NoteScope) {
    const { token, user } = useAuth()

    return useInfiniteQuery({
        queryKey: [
            'notes',
            'overview',
            user?.id,
            scope,
        ],
        initialPageParam: null as NoteCursor | null,
        enabled: Boolean(token),
        queryFn: ({ pageParam }) =>
            getNotesPage(token!, pageParam, scope),
        getNextPageParam: (lastPage) =>
            lastPage.hasNext
                ? lastPage.nextCursor ?? undefined
                : undefined,
        staleTime: 5_000,
        refetchInterval: 10_000,
        refetchOnMount: 'always',
        refetchOnWindowFocus: 'always',
    })
}