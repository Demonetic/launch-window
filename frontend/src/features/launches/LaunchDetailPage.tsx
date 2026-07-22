import {
    ArrowLeft,
    Building2,
    CalendarClock,
    ExternalLink,
    MapPin,
    Rocket,
} from 'lucide-react'
import {
    useLocation,
    useNavigate,
    useParams,
} from 'react-router'
import { CalendarToggleButton } from '../calendar/CalendarToggleButton'
import { ApiClientError } from '../../lib/api'
import {
    formatDateTime,
    statusLabels,
    toClassName,
} from './launchPresentation'
import { LaunchWeatherPanel } from './LaunchWeatherPanel'
import { useLaunchDetail } from './useLaunchDetail'
import { LaunchNotesPanel } from '../notes/LaunchNotesPanel'
import './launchDetail.css'
import { LaunchImage } from './LaunchImage'

interface LaunchNavigationState {
    returnLabel?: string
    returnTo?: string
}

export function LaunchDetailPage() {
    const { launchId } = useParams()
    const location = useLocation()
    const navigate = useNavigate()

    const parsedLaunchId = Number(launchId)

    const navigationState =
        location.state as LaunchNavigationState | null

    const returnLabel =
        navigationState?.returnLabel ?? 'Upcoming launches'

    const {
        isValidLaunchId,
        launchQuery,
        weatherQuery,
    } = useLaunchDetail(
        Number.isNaN(parsedLaunchId)
            ? null
            : parsedLaunchId,
    )

    function handleBack() {
        if (navigationState?.returnTo) {
            void navigate(-1)
            return
        }

        void navigate('/')
    }

    if (!isValidLaunchId) {
        return (
            <main className="launch-detail-page">
                <DetailError
                    message="The launch ID is invalid."
                    returnLabel={returnLabel}
                    onBack={handleBack}
                />
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
                    returnLabel={returnLabel}
                    onBack={handleBack}
                />
            </main>
        )
    }

    const launch = launchQuery.data

    return (
        <main className="launch-detail-page">
            <button
                className="detail-back-link"
                type="button"
                onClick={handleBack}
            >
                <ArrowLeft aria-hidden="true" size={18} />
                {returnLabel}
            </button>

            <section className="launch-detail-hero">
                <div className="launch-detail-image">
                    <LaunchImage
                        src={launch.imageUrl}
                        fallbackSize={54}
                    />
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
                                    {formatDateTime(
                                        launch.launchTime,
                                    )}
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

                    <div className="launch-detail-actions">
                        <CalendarToggleButton
                            launchId={launch.id}
                        />

                        {launch.webcastUrl && (
                            <a
                                className="webcast-link"
                                href={launch.webcastUrl}
                                target="_blank"
                                rel="noreferrer"
                            >
                                Watch webcast
                                <ExternalLink
                                    aria-hidden="true"
                                    size={17}
                                />
                            </a>
                        )}
                    </div>
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

            <LaunchNotesPanel launchId={launch.id} />
        </main>
    )
}

interface DetailErrorProps {
    message: string
    returnLabel: string
    onBack: () => void
}

function DetailError({
                         message,
                         returnLabel,
                         onBack,
                     }: DetailErrorProps) {
    return (
        <div
            className="launch-state launch-error"
            role="alert"
        >
            <h1>Launch unavailable</h1>
            <p>{message}</p>

            <button
                className="detail-back-link"
                type="button"
                onClick={onBack}
            >
                <ArrowLeft aria-hidden="true" size={18} />
                Return to {returnLabel.toLowerCase()}
            </button>
        </div>
    )
}