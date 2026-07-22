import {
    CalendarClock,
    CloudSun,
    MapPin,
    Rocket,
} from 'lucide-react'
import { Link } from 'react-router'
import type {
    LaunchStatus,
    LaunchSummary,
    ViewingCondition,
} from './types'

interface LaunchCardProps {
    launch: LaunchSummary
}

const statusLabels: Record<LaunchStatus, string> = {
    GO: 'Go',
    TO_BE_DETERMINED: 'TBD',
    TO_BE_CONFIRMED: 'TBC',
    HOLD: 'On hold',
    IN_FLIGHT: 'In flight',
    SUCCESS: 'Success',
    PARTIAL_FAILURE: 'Partial failure',
    FAILURE: 'Failure',
    UNKNOWN: 'Unknown',
}

const viewingLabels: Record<ViewingCondition, string> = {
    EXCELLENT: 'Excellent viewing',
    GOOD: 'Good viewing',
    FAIR: 'Fair viewing',
    POOR: 'Poor viewing',
    VERY_POOR: 'Very poor viewing',
}

function formatLaunchTime(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(new Date(value))
}

function toClassName(value: string) {
    return value.toLowerCase().replaceAll('_', '-')
}

export function LaunchCard({ launch }: LaunchCardProps) {
    return (
        <Link
            className="launch-card"
            to={`/launches/${launch.id}`}
        >
            <div className="launch-card-image">
                {launch.imageUrl ? (
                    <img src={launch.imageUrl} alt="" />
                ) : (
                    <Rocket aria-hidden="true" size={38} />
                )}

                <span
                    className={`launch-status launch-status-${toClassName(launch.status)}`}
                >
                    {statusLabels[launch.status]}
                </span>
            </div>

            <div className="launch-card-content">
                <div>
                    <p className="launch-organization">
                        {launch.organizationName ?? 'Unknown organization'}
                    </p>
                    <h2>{launch.name}</h2>
                </div>

                <div className="launch-card-details">
                    <p>
                        <CalendarClock aria-hidden="true" size={17} />
                        {formatLaunchTime(launch.launchTime)}
                    </p>

                    <p>
                        <Rocket aria-hidden="true" size={17} />
                        {launch.rocketName ?? 'Rocket not announced'}
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
                        <CloudSun aria-hidden="true" size={18} />

                        <span>
                            {viewingLabels[
                                launch.weather.viewingCondition
                                ]}
                        </span>

                        <strong>
                            {launch.weather.viewingScore}/100
                        </strong>
                    </div>
                ) : (
                    <div className="launch-weather launch-weather-unavailable">
                        <CloudSun aria-hidden="true" size={18} />
                        <span>Forecast not available</span>
                    </div>
                )}
            </div>
        </Link>
    )
}