import { useInfiniteQuery } from '@tanstack/react-query'
import { getUpcomingLaunches } from './launchApi'
import type { LaunchCursor } from './types'

export function useUpcomingLaunches() {
    return useInfiniteQuery({
        queryKey: ['launches', 'upcoming'],
        initialPageParam: null as LaunchCursor | null,
        queryFn: ({ pageParam }) =>
            getUpcomingLaunches(pageParam),
        getNextPageParam: (lastPage) =>
            lastPage.hasNext
                ? lastPage.nextCursor ?? undefined
                : undefined,
    })
}