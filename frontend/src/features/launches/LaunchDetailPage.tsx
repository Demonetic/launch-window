import {
    ArrowLeft,
    Building2,
    CalendarClock,
    ExternalLink,
    MapPin,
    Rocket,
} from 'lucide-react'
import { Link, useParams } from 'react-router'
import { ApiClientError } from '../../lib/api'
import {
    formatDateTime,
    statusLabels,
    toClassName,
} from './launchPresentation'
import { LaunchWeatherPanel } from './LaunchWeatherPanel'
import { useLaunchDetail } from './useLaunchDetail'
import './launchDetail.css'

export function LaunchDetailPage() {
    const { launchId } = useParams()
    const parsedLaunchId = Number(launchId)

    const {
        isValidLaunchId,
        launchQuery,
        weatherQuery,
    } = useLaunchDetail(
        Number.isNaN(parsedLaunchId) ? null : parsedLaunchId,
    )

    if (!isValidLaunchId) {
        return (
            <main className="launch-detail-page">
                <DetailError message="The launch ID is invalid." />
            </main>
        )
    }

    if (launchQuery.isPending) {
        return (
            <main className="launch-detail-page">
                <div className="launch-state" role="status">
                    <span className="launch-loader" />
                    <p>Loading launch...</p>
                </div>
            </main>
        )
    }

    if (launchQuery.isError) {
        const isNotFound =
            launchQuery.error instanceof ApiClientError &&
            launchQuery.error.status === 404

        return (
            <main className="launch-detail-page">
                <DetailError
                    message={
                        isNotFound
                            ? 'This launch could not be found.'
                            : 'The launch could not be loaded.'
                    }
                />
            </main>
        )
    }

    const launch = launchQuery.data

    return (
        <main className="launch-detail-page">
            <Link className="detail-back-link" to="/">
                <ArrowLeft aria-hidden="true" size={18} />
                Upcoming launches
            </Link>

            <section className="launch-detail-hero">
                <div className="launch-detail-image">
                    {launch.imageUrl ? (
                        <img src={launch.imageUrl} alt="" />
                    ) : (
                        <Rocket aria-hidden="true" size={54} />
                    )}
                </div>

                <div className="launch-detail-introduction">
                    <div className="launch-detail-badges">
                        <span
                            className={`launch-status launch-status-${toClassName(
                                launch.status,
                            )}`}
                        >
                            {statusLabels[launch.status]}
                        </span>

                        {launch.missionType && (
                            <span className="mission-type">
                                {launch.missionType}
                            </span>
                        )}
                    </div>

                    <p className="page-eyebrow">
                        {launch.organizationName ??
                            'Unknown organization'}
                    </p>

                    <h1>{launch.name}</h1>

                    <div className="launch-detail-primary-data">
                        <p>
                            <CalendarClock aria-hidden="true" />
                            <span>
                                <small>Launch time</small>
                                <strong>
                                    {formatDateTime(launch.launchTime)}
                                </strong>
                            </span>
                        </p>

                        <p>
                            <Rocket aria-hidden="true" />
                            <span>
                                <small>Rocket</small>
                                <strong>
                                    {launch.rocketName ??
                                        'Not announced'}
                                </strong>
                            </span>
                        </p>
                    </div>

                    {launch.webcastUrl && (
                        <a
                            className="webcast-link"
                            href={launch.webcastUrl}
                            target="_blank"
                            rel="noreferrer"
                        >
                            Watch webcast
                            <ExternalLink aria-hidden="true" size={17} />
                        </a>
                    )}
                </div>
            </section>

            <div className="launch-detail-grid">
                <section className="detail-panel mission-panel">
                    <p className="page-eyebrow">Mission</p>
                    <h2>About this launch</h2>

                    <p className="mission-description">
                        {launch.description ??
                            'No mission description is available yet.'}
                    </p>

                    <div className="mission-facts">
                        <p>
                            <Building2 aria-hidden="true" />
                            <span>
                                <small>Organization</small>
                                <strong>
                                    {launch.organizationName ??
                                        'Not announced'}
                                </strong>
                            </span>
                        </p>

                        <p>
                            <MapPin aria-hidden="true" />
                            <span>
                                <small>Launch site</small>
                                <strong>
                                    {launch.padName ??
                                        'Pad not announced'}
                                </strong>
                                <small>
                                    {launch.locationName ??
                                        'Location not announced'}
                                </small>
                            </span>
                        </p>
                    </div>
                </section>

                <LaunchWeatherPanel
                    isError={weatherQuery.isError}
                    isPending={weatherQuery.isPending}
                    weather={weatherQuery.data}
                />
            </div>
        </main>
    )
}

function DetailError({ message }: { message: string }) {
    return (
        <div className="launch-state launch-error" role="alert">
            <h1>Launch unavailable</h1>
            <p>{message}</p>
            <Link className="detail-back-link" to="/">
                <ArrowLeft aria-hidden="true" size={18} />
                Return to upcoming launches
            </Link>
        </div>
    )
}