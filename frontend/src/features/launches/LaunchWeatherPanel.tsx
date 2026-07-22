import {
    Cloud,
    CloudRain,
    CloudSun,
    Eye,
    Thermometer,
    Wind,
} from 'lucide-react'
import {
    formatDateTime,
    toClassName,
    viewingLabels,
} from './launchPresentation'
import type { LaunchWeather } from './types'

interface LaunchWeatherPanelProps {
    isError: boolean
    isPending: boolean
    weather: LaunchWeather | null | undefined
}

function formatVisibility(visibilityMeters: number | null) {
    if (visibilityMeters === null) {
        return 'Unknown'
    }

    return `${(visibilityMeters / 1000).toFixed(1)} km`
}

export function LaunchWeatherPanel({
                                       isError,
                                       isPending,
                                       weather,
                                   }: LaunchWeatherPanelProps) {
    if (isPending) {
        return (
            <section className="detail-panel weather-panel">
                <h2>Launch weather</h2>
                <p className="detail-muted">Loading forecast...</p>
            </section>
        )
    }

    if (isError) {
        return (
            <section className="detail-panel weather-panel">
                <h2>Launch weather</h2>
                <p className="detail-error">
                    The forecast could not be loaded.
                </p>
            </section>
        )
    }

    if (!weather) {
        return (
            <section className="detail-panel weather-panel">
                <h2>Launch weather</h2>
                <p className="detail-muted">
                    A forecast is not available yet.
                </p>
            </section>
        )
    }

    return (
        <section className="detail-panel weather-panel">
            <div className="detail-panel-heading">
                <div>
                    <p className="page-eyebrow">Forecast</p>
                    <h2>Launch weather</h2>
                </div>

                <div
                    className={`weather-score weather-score-${toClassName(
                        weather.viewingCondition,
                    )}`}
                >
                    <CloudSun aria-hidden="true" size={20} />
                    <span>
                        {viewingLabels[weather.viewingCondition]}
                    </span>
                    <strong>{weather.viewingScore}/100</strong>
                </div>
            </div>

            <div className="weather-metrics">
                <article>
                    <Thermometer aria-hidden="true" />
                    <span>Temperature</span>
                    <strong>{weather.temperatureC}°C</strong>
                </article>

                <article>
                    <Cloud aria-hidden="true" />
                    <span>Cloud cover</span>
                    <strong>{weather.cloudCoverPercent}%</strong>
                </article>

                <article>
                    <CloudRain aria-hidden="true" />
                    <span>Precipitation</span>
                    <strong>
                        {weather.precipitationProbabilityPercent}%
                    </strong>
                </article>

                <article>
                    <Wind aria-hidden="true" />
                    <span>Wind speed</span>
                    <strong>{weather.windSpeedKmh} km/h</strong>
                </article>

                <article>
                    <Eye aria-hidden="true" />
                    <span>Visibility</span>
                    <strong>
                        {formatVisibility(weather.visibilityMeters)}
                    </strong>
                </article>
            </div>

            <p className="weather-forecast-time">
                Forecast for {formatDateTime(weather.forecastTime)}
            </p>
        </section>
    )
}