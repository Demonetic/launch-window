import { useInfiniteQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { getCalendarPage } from './calendarApi'
import type { CalendarPageParameter } from './types'

export function useCalendar() {
    const { token, user } = useAuth()

    return useInfiniteQuery({
        queryKey: ['calendar', user?.id],
        initialPageParam: null as CalendarPageParameter,
        enabled: Boolean(token),
        queryFn: ({ pageParam }) =>
            getCalendarPage(token!, pageParam),
        getPreviousPageParam: (firstPage) =>
            firstPage.hasPrevious && firstPage.previousCursor
                ? {
                    direction: 'previous' as const,
                    cursor: firstPage.previousCursor,
                }
                : undefined,
        getNextPageParam: (lastPage) =>
            lastPage.hasNext && lastPage.nextCursor
                ? {
                    direction: 'next' as const,
                    cursor: lastPage.nextCursor,
                }
                : undefined,
    })
}