import {
    Fragment,
    useEffect,
    useRef,
    useState,
} from 'react'
import {
    ArrowUp,
    CalendarDays,
} from 'lucide-react'
import { CalendarEntryCard } from './CalendarEntryCard'
import { CalendarInvitationsPanel } from './CalendarInvitationsPanel'
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

const CALENDAR_SCROLL_POSITION_KEY =
    'calendar-scroll-position'

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

    const [now] = useState(() => Date.now())

    const upcomingMarkerRef =
        useRef<HTMLDivElement | null>(null)

    const hasPositionedCalendar = useRef(false)

    const entries =
        data?.pages.flatMap((page) => page.items) ?? []

    const hasUpcomingLaunch = entries.some(
        (entry) =>
            new Date(
                entry.launch.launchTime,
            ).getTime() >= now,
    )

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

    useEffect(() => {
        if (
            hasPositionedCalendar.current ||
            isPending ||
            !data
        ) {
            return
        }

        const loadedEntries = data.pages.flatMap(
            (page) => page.items,
        )

        if (loadedEntries.length === 0) {
            return
        }

        const savedScrollPosition =
            sessionStorage.getItem(
                CALENDAR_SCROLL_POSITION_KEY,
            )

        if (savedScrollPosition) {
            sessionStorage.removeItem(
                CALENDAR_SCROLL_POSITION_KEY,
            )

            const parsedPosition = Number(
                savedScrollPosition,
            )

            if (Number.isFinite(parsedPosition)) {
                const frame =
                    requestAnimationFrame(() => {
                        window.scrollTo({
                            top: parsedPosition,
                            behavior: 'auto',
                        })

                        hasPositionedCalendar.current =
                            true
                    })

                return () =>
                    cancelAnimationFrame(frame)
            }
        }

        const containsUpcomingLaunch =
            loadedEntries.some(
                (entry) =>
                    new Date(
                        entry.launch.launchTime,
                    ).getTime() >= now,
            )

        if (!containsUpcomingLaunch) {
            hasPositionedCalendar.current = true
            return
        }

        const frame = requestAnimationFrame(() => {
            upcomingMarkerRef.current?.scrollIntoView({
                behavior: 'auto',
                block: 'start',
            })

            hasPositionedCalendar.current = true
        })

        return () => cancelAnimationFrame(frame)
    }, [data, isPending, now])


    function scrollToUpcoming() {
        upcomingMarkerRef.current?.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
        })
    }

    return (
        <main className="calendar-page">
            <header className="calendar-header">
                <div>
                    <p className="page-eyebrow">
                        Your schedule
                    </p>

                    <h1>Launch calendar</h1>

                    <p>
                        Your saved launches, arranged around
                        the present day.
                    </p>
                </div>
            </header>

            <CalendarInvitationsPanel />

            {isPending && (
                <div
                    className="calendar-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <p>Loading your calendar...</p>
                </div>
            )}

            {isError && (
                <div
                    className="calendar-state calendar-error"
                    role="alert"
                >
                    <h2>
                        Calendar could not be loaded
                    </h2>

                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Please try again shortly.'}
                    </p>
                </div>
            )}

            {!isPending &&
                !isError &&
                entries.length === 0 && (
                    <div className="calendar-state">
                        <CalendarDays
                            aria-hidden="true"
                            size={36}
                        />

                        <h2>Your calendar is empty</h2>

                        <p>
                            Save launches to keep their
                            dates and viewing conditions
                            close at hand.
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

                                <span>
                                    Loading earlier
                                    launches...
                                </span>
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
                                previousEntry.launch
                                    .launchTime,
                            ) !==
                            dateKey(
                                entry.launch
                                    .launchTime,
                            )

                        const startsUpcoming =
                            launchTime >= now &&
                            (!previousEntry ||
                                new Date(
                                    previousEntry.launch
                                        .launchTime,
                                ).getTime() < now)

                        return (
                            <Fragment key={entry.id}>
                                {startsUpcoming && (
                                    <div
                                        className="calendar-now-marker"
                                        ref={
                                            upcomingMarkerRef
                                        }
                                    >
                                        <span>
                                            Upcoming
                                        </span>
                                    </div>
                                )}

                                {startsNewDate && (
                                    <h2 className="calendar-date">
                                        {formatDate(
                                            entry.launch
                                                .launchTime,
                                        )}
                                    </h2>
                                )}

                                <CalendarEntryCard
                                    entry={entry}
                                    isPast={
                                        launchTime < now
                                    }
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

                                <span>
                                    Loading later
                                    launches...
                                </span>
                            </>
                        )}

                        {!hasNextPage && (
                            <span>
                                No later saved launches
                            </span>
                        )}
                    </div>
                </section>
            )}

            {hasUpcomingLaunch && (
                <button
                    type="button"
                    className="calendar-upcoming-button"
                    onClick={scrollToUpcoming}
                    aria-label="Scroll to upcoming launches"
                >
        <span className="calendar-upcoming-button-icon">
            <ArrowUp
                aria-hidden="true"
                size={17}
            />
        </span>

                    <span className="calendar-upcoming-button-copy">
            <small>Calendar</small>
            <strong>Upcoming</strong>
        </span>
                </button>
            )}
        </main>
    )
}