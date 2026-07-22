import { useEffect, useRef } from 'react'
import { Rocket } from 'lucide-react'
import { LaunchCard } from './LaunchCard'
import { useUpcomingLaunches } from './useUpcomingLaunches'
import { BestViewingSection } from './BestViewingSection'
import './launches.css'

export function UpcomingLaunchesPage() {
    const loadMoreRef = useRef<HTMLDivElement>(null)

    const {
        data,
        error,
        fetchNextPage,
        hasNextPage,
        isError,
        isFetchingNextPage,
        isPending,
    } = useUpcomingLaunches()

    const launches =
        data?.pages.flatMap((page) => page.items) ?? []

    useEffect(() => {
        const loadMoreElement = loadMoreRef.current

        if (!loadMoreElement || !hasNextPage) {
            return
        }

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry?.isIntersecting && !isFetchingNextPage) {
                    void fetchNextPage()
                }
            },
            {
                rootMargin: '300px',
            },
        )

        observer.observe(loadMoreElement)

        return () => observer.disconnect()
    }, [
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
    ])

    return (
        <main className="launches-page">
            <header className="launches-header">
                <div>
                    <p className="page-eyebrow">Mission control</p>
                    <h1>Upcoming launches</h1>
                    <p>
                        Explore upcoming missions and find the best
                        conditions for watching them.
                    </p>
                </div>
            </header>

            <BestViewingSection />

            <div className="upcoming-list-heading">
                <div>
                    <p className="page-eyebrow">
                        Full schedule
                    </p>
                    <h2>All upcoming launches</h2>
                </div>

                <p>
                    Browse every currently scheduled mission in
                    chronological order.
                </p>
            </div>

            {isPending && (
                <div className="launch-state" role="status">
                    <span className="launch-loader" />
                    <p>Loading upcoming launches...</p>
                </div>
            )}

            {isError && (
                <div className="launch-state launch-error" role="alert">
                    <h2>Launches could not be loaded</h2>
                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Please try again shortly.'}
                    </p>
                </div>
            )}

            {!isPending && !isError && launches.length === 0 && (
                <div className="launch-state">
                    <Rocket aria-hidden="true" size={34} />
                    <h2>No upcoming launches</h2>
                    <p>New missions will appear here when available.</p>
                </div>
            )}

            {launches.length > 0 && (
                <section
                    className="launch-grid"
                    aria-label="Upcoming launches"
                >
                    {launches.map((launch) => (
                        <LaunchCard
                            key={launch.id}
                            launch={launch}
                        />
                    ))}
                </section>
            )}

            <div
                className="launch-load-more"
                ref={loadMoreRef}
                aria-live="polite"
            >
                {isFetchingNextPage && (
                    <>
                        <span className="launch-loader" />
                        <span>Loading more launches...</span>
                    </>
                )}

                {!hasNextPage && launches.length > 0 && (
                    <span>You have reached the final launch.</span>
                )}
            </div>
        </main>
    )
}