import {
    CloudSun,
    MapPin,
    Rocket,
} from 'lucide-react'
import { Link } from 'react-router'
import {
    toClassName,
    viewingLabels,
} from '../launches/launchPresentation'
import type { CalendarEntry } from './types'

interface CalendarEntryCardProps {
    entry: CalendarEntry
    isPast: boolean
}

function formatTime(value: string) {
    return new Intl.DateTimeFormat('en', {
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(value))
}

export function CalendarEntryCard({
                                      entry,
                                      isPast,
                                  }: CalendarEntryCardProps) {
    const { launch } = entry

    return (
        <article
            className={`calendar-entry${isPast ? ' calendar-entry-past' : ''}`}
        >
            <time
                className="calendar-entry-time"
                dateTime={launch.launchTime}
            >
                {formatTime(launch.launchTime)}
            </time>

            <span className="calendar-timeline-dot" />

            <Link
                className="calendar-entry-card"
                to={`/launches/${launch.id}`}
                state={{
                    returnLabel: 'Launch calendar',
                    returnTo: '/calendar',
                }}
            >
                <div className="calendar-entry-heading">
                    <div>
                        <p>
                            {launch.organizationName ??
                                'Unknown organization'}
                        </p>
                        <h2>{launch.name}</h2>
                    </div>

                    <Rocket aria-hidden="true" size={20} />
                </div>

                <div className="calendar-entry-details">
                    <span>
                        <Rocket aria-hidden="true" size={15} />
                        {launch.rocketName ??
                            'Rocket not announced'}
                    </span>

                    <span>
                        <MapPin aria-hidden="true" size={15} />
                        {launch.locationName ??
                            launch.padName ??
                            'Location not announced'}
                    </span>
                </div>

                {launch.weather ? (
                    <div
                        className={`calendar-entry-weather calendar-entry-weather-${toClassName(
                            launch.weather.viewingCondition,
                        )}`}
                    >
                        <CloudSun aria-hidden="true" size={16} />
                        <span>
                            {
                                viewingLabels[
                                    launch.weather
                                        .viewingCondition
                                    ]
                            }
                        </span>
                        <strong>
                            {launch.weather.viewingScore}/100
                        </strong>
                    </div>
                ) : (
                    <div className="calendar-entry-weather">
                        <CloudSun aria-hidden="true" size={16} />
                        <span>Forecast not available</span>
                    </div>
                )}
            </Link>
        </article>
    )
}