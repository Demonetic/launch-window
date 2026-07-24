import { useInfiniteQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getNotesPage } from './notesApi'
import type { NoteCursor } from './types'

export function useNotesOverview() {
    const { token, user } = useAuth()

    return useInfiniteQuery({
        queryKey: ['notes', 'overview', user?.id],
        initialPageParam: null as NoteCursor | null,
        enabled: Boolean(token),
        queryFn: ({ pageParam }) =>
            getNotesPage(token!, pageParam),
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