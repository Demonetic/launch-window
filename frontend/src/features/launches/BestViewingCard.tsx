import {
    CalendarClock,
    CloudSun,
} from 'lucide-react'
import { Link } from 'react-router'
import {
    formatDateTime,
    toClassName,
    viewingLabels,
} from './launchPresentation'
import type { LaunchSummary } from './types'
import { LaunchImage } from './LaunchImage'

interface BestViewingCardProps {
    launch: LaunchSummary
}

export function BestViewingCard({
                                    launch,
                                }: BestViewingCardProps) {
    return (
        <Link
            className="best-viewing-card"
            to={`/launches/${launch.id}`}
            state={{
                returnLabel: 'Upcoming launches',
                returnTo: '/',
            }}
        >
            <div className="best-viewing-image">
                <LaunchImage
                    src={launch.imageUrl}
                    fallbackSize={28}
                />
            </div>

            <div className="best-viewing-content">
                <div>
                    <small>
                        {launch.organizationName ??
                            'Unknown organization'}
                    </small>
                    <h3>{launch.name}</h3>
                </div>

                <div className="best-viewing-meta">
                    <span>
                        <CalendarClock
                            aria-hidden="true"
                            size={14}
                        />
                        {formatDateTime(launch.launchTime)}
                    </span>

                    {launch.weather && (
                        <strong
                            className={`best-viewing-score best-viewing-score-${toClassName(
                                launch.weather
                                    .viewingCondition,
                            )}`}
                        >
                            <CloudSun
                                aria-hidden="true"
                                size={15}
                            />
                            {
                                viewingLabels[
                                    launch.weather
                                        .viewingCondition
                                    ]
                            }
                            <b>
                                {launch.weather.viewingScore}
                            </b>
                        </strong>
                    )}
                </div>
            </div>
        </Link>
    )
}