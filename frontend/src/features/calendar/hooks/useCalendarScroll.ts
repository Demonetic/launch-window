import { useEffect, useRef } from 'react'

interface CalendarScrollOptions {
    fetchNextPage: () => Promise<unknown>
    fetchPreviousPage: () => Promise<unknown>
    hasNextPage: boolean
    hasPreviousPage: boolean
    isFetchingNextPage: boolean
    isFetchingPreviousPage: boolean
}

export function useCalendarScroll({
                                      fetchNextPage,
                                      fetchPreviousPage,
                                      hasNextPage,
                                      hasPreviousPage,
                                      isFetchingNextPage,
                                      isFetchingPreviousPage,
                                  }: CalendarScrollOptions) {
    const previousSentinelRef = useRef<HTMLDivElement>(null)
    const nextSentinelRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        const sentinel = previousSentinelRef.current

        if (!sentinel || !hasPreviousPage) {
            return
        }

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (
                    !entry?.isIntersecting ||
                    isFetchingPreviousPage
                ) {
                    return
                }

                const previousHeight =
                    document.documentElement.scrollHeight

                void fetchPreviousPage().then(() => {
                    requestAnimationFrame(() => {
                        const currentHeight =
                            document.documentElement.scrollHeight

                        window.scrollBy({
                            top: currentHeight - previousHeight,
                        })
                    })
                })
            },
            { rootMargin: '250px' },
        )

        observer.observe(sentinel)

        return () => observer.disconnect()
    }, [
        fetchPreviousPage,
        hasPreviousPage,
        isFetchingPreviousPage,
    ])

    useEffect(() => {
        const sentinel = nextSentinelRef.current

        if (!sentinel || !hasNextPage) {
            return
        }

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (
                    entry?.isIntersecting &&
                    !isFetchingNextPage
                ) {
                    void fetchNextPage()
                }
            },
            { rootMargin: '350px' },
        )

        observer.observe(sentinel)

        return () => observer.disconnect()
    }, [
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
    ])

    return {
        nextSentinelRef,
        previousSentinelRef,
    }
}