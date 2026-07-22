import { Fragment, useState } from 'react'
import { CalendarDays } from 'lucide-react'
import { CalendarEntryCard } from './CalendarEntryCard'
import { useCalendar } from './useCalendar'
import { useCalendarScroll } from './useCalendarScroll'
import './calendar.css'

function formatDate(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'full',
    }).format(new Date(value))
}

function dateKey(value: string) {
    const date = new Date(value)

    return [
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
    ].join('-')
}

export function CalendarPage() {
    const {
        data,
        error,
        fetchNextPage,
        fetchPreviousPage,
        hasNextPage,
        hasPreviousPage,
        isError,
        isFetchingNextPage,
        isFetchingPreviousPage,
        isPending,
    } = useCalendar()

    const entries =
        data?.pages.flatMap((page) => page.items) ?? []

    const {
        nextSentinelRef,
        previousSentinelRef,
    } = useCalendarScroll({
        fetchNextPage,
        fetchPreviousPage,
        hasNextPage,
        hasPreviousPage,
        isFetchingNextPage,
        isFetchingPreviousPage,
    })

    const [now] = useState(() => Date.now())

    return (
        <main className="calendar-page">
            <header className="calendar-header">
                <div>
                    <p className="page-eyebrow">Your schedule</p>
                    <h1>Launch calendar</h1>
                    <p>
                        Your saved launches, arranged around the
                        present day.
                    </p>
                </div>
            </header>

            {isPending && (
                <div className="calendar-state" role="status">
                    <span className="launch-loader" />
                    <p>Loading your calendar...</p>
                </div>
            )}

            {isError && (
                <div
                    className="calendar-state calendar-error"
                    role="alert"
                >
                    <h2>Calendar could not be loaded</h2>
                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Please try again shortly.'}
                    </p>
                </div>
            )}

            {!isPending && !isError && entries.length === 0 && (
                <div className="calendar-state">
                    <CalendarDays aria-hidden="true" size={36} />
                    <h2>Your calendar is empty</h2>
                    <p>
                        Save launches to keep their dates and
                        viewing conditions close at hand.
                    </p>
                </div>
            )}

            {entries.length > 0 && (
                <section
                    className="calendar-timeline"
                    aria-label="Saved launches"
                >
                    <div
                        className="calendar-scroll-state"
                        ref={previousSentinelRef}
                        aria-live="polite"
                    >
                        {isFetchingPreviousPage && (
                            <>
                                <span className="launch-loader" />
                                <span>Loading earlier launches...</span>
                            </>
                        )}

                        {!hasPreviousPage && (
                            <span>
                                No earlier saved launches
                            </span>
                        )}
                    </div>

                    {entries.map((entry, index) => {
                        const launchTime =
                            new Date(
                                entry.launch.launchTime,
                            ).getTime()

                        const previousEntry =
                            index > 0
                                ? entries[index - 1]
                                : null

                        const startsNewDate =
                            !previousEntry ||
                            dateKey(
                                previousEntry.launch.launchTime,
                            ) !==
                            dateKey(
                                entry.launch.launchTime,
                            )

                        const startsUpcoming =
                            launchTime >= now &&
                            (!previousEntry ||
                                new Date(
                                    previousEntry.launch.launchTime,
                                ).getTime() < now)

                        return (
                            <Fragment key={entry.id}>
                                {startsUpcoming && (
                                    <div className="calendar-now-marker">
                                        <span>Upcoming</span>
                                    </div>
                                )}

                                {startsNewDate && (
                                    <h2 className="calendar-date">
                                        {formatDate(
                                            entry.launch.launchTime,
                                        )}
                                    </h2>
                                )}

                                <CalendarEntryCard
                                    entry={entry}
                                    isPast={launchTime < now}
                                />
                            </Fragment>
                        )
                    })}

                    <div
                        className="calendar-scroll-state"
                        ref={nextSentinelRef}
                        aria-live="polite"
                    >
                        {isFetchingNextPage && (
                            <>
                                <span className="launch-loader" />
                                <span>Loading later launches...</span>
                            </>
                        )}

                        {!hasNextPage && (
                            <span>No later saved launches</span>
                        )}
                    </div>
                </section>
            )}
        </main>
    )
}