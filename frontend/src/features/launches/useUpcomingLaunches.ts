import { useInfiniteQuery } from '@tanstack/react-query'
import { getUpcomingLaunches } from './launchApi'
import type {
    LaunchCursor,
    LaunchFilters,
} from './types'

export function useUpcomingLaunches(
    filters: LaunchFilters,
) {
    return useInfiniteQuery({
        queryKey: [
            'launches',
            'upcoming',
            filters,
        ],
        initialPageParam: null as LaunchCursor | null,
        queryFn: ({ pageParam }) =>
            getUpcomingLaunches(filters, pageParam),
        getNextPageParam: (lastPage) =>
            lastPage.hasNext
                ? lastPage.nextCursor ?? undefined
                : undefined,
    })
}