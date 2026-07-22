import {
    CalendarClock,
    CloudSun,
    MapPin,
    Rocket,
} from 'lucide-react'
import { Link } from 'react-router'
import {
    formatDateTime,
    statusLabels,
    toClassName,
    viewingLabels,
} from './launchPresentation'
import type { LaunchSummary } from './types'
import { LaunchImage } from './LaunchImage'

interface LaunchCardProps {
    launch: LaunchSummary
}

export function LaunchCard({ launch }: LaunchCardProps) {
    return (
        <Link
            className="launch-card"
            to={`/launches/${launch.id}`}
            state={{
                returnLabel: 'Upcoming launches',
                returnTo: '/',
            }}
        >
            <div className="launch-card-image">
                <LaunchImage
                    src={launch.imageUrl}
                    fallbackSize={38}
                />

                <span
                    className={`launch-status launch-status-${toClassName(
                        launch.status,
                    )}`}
                >
                    {statusLabels[launch.status]}
                </span>
            </div>

            <div className="launch-card-content">
                <div>
                    <p className="launch-organization">
                        {launch.organizationName ??
                            'Unknown organization'}
                    </p>

                    <h2>{launch.name}</h2>
                </div>

                <div className="launch-card-details">
                    <p>
                        <CalendarClock
                            aria-hidden="true"
                            size={17}
                        />
                        {formatDateTime(launch.launchTime)}
                    </p>

                    <p>
                        <Rocket aria-hidden="true" size={17} />
                        {launch.rocketName ??
                            'Rocket not announced'}
                    </p>

                    <p>
                        <MapPin aria-hidden="true" size={17} />
                        {launch.locationName ??
                            launch.padName ??
                            'Location not announced'}
                    </p>
                </div>

                {launch.weather ? (
                    <div
                        className={`launch-weather launch-weather-${toClassName(
                            launch.weather.viewingCondition,
                        )}`}
                    >
                        <CloudSun
                            aria-hidden="true"
                            size={18}
                        />

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
                    <div className="launch-weather launch-weather-unavailable">
                        <CloudSun
                            aria-hidden="true"
                            size={18}
                        />
                        <span>Forecast not available</span>
                    </div>
                )}
            </div>
        </Link>
    )
}