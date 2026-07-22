import { CloudSun } from 'lucide-react'
import type { LaunchFilters } from './types'

interface LaunchFilterControlsProps {
    filters: LaunchFilters
    onUpdate: (
        update: Partial<LaunchFilters>,
    ) => void
}

export function LaunchFilterControls({
                                         filters,
                                         onUpdate,
                                     }: LaunchFilterControlsProps) {
    return (
        <div className="launch-filter-groups">
            <fieldset className="launch-filter-group">
                <legend>Sort by</legend>

                <div className="filter-segmented-control">
                    <button
                        className={
                            filters.sort === 'SOONEST'
                                ? 'selected'
                                : undefined
                        }
                        type="button"
                        onClick={() =>
                            onUpdate({
                                sort: 'SOONEST',
                            })
                        }
                    >
                        Soonest
                    </button>

                    <button
                        className={
                            filters.sort === 'BEST_VIEWING'
                                ? 'selected'
                                : undefined
                        }
                        type="button"
                        onClick={() =>
                            onUpdate({
                                sort: 'BEST_VIEWING',
                            })
                        }
                    >
                        Best viewing
                    </button>
                </div>
            </fieldset>

            <fieldset className="launch-filter-group">
                <legend>Time period</legend>

                <select
                    value={filters.days ?? ''}
                    onChange={(event) =>
                        onUpdate({
                            days: event.target.value
                                ? Number(event.target.value)
                                : null,
                        })
                    }
                    aria-label="Launch time period"
                >
                    <option value="">
                        All upcoming
                    </option>

                    <option value="7">
                        Next 7 days
                    </option>

                    <option value="30">
                        Next 30 days
                    </option>

                    <option value="90">
                        Next 90 days
                    </option>
                </select>
            </fieldset>

            <fieldset className="launch-filter-group">
                <legend>Forecast</legend>

                <div className="filter-segmented-control filter-weather-control">
                    <button
                        className={
                            filters.forecastAvailable === null
                                ? 'selected'
                                : undefined
                        }
                        type="button"
                        onClick={() =>
                            onUpdate({
                                forecastAvailable: null,
                            })
                        }
                    >
                        Any
                    </button>

                    <button
                        className={
                            filters.forecastAvailable === true
                                ? 'selected'
                                : undefined
                        }
                        type="button"
                        onClick={() =>
                            onUpdate({
                                forecastAvailable: true,
                            })
                        }
                    >
                        Available
                    </button>

                    <button
                        className={
                            filters.forecastAvailable === false
                                ? 'selected'
                                : undefined
                        }
                        type="button"
                        onClick={() =>
                            onUpdate({
                                forecastAvailable: false,
                                minimumViewingScore: null,
                            })
                        }
                    >
                        Missing
                    </button>
                </div>
            </fieldset>

            <fieldset className="launch-filter-group">
                <legend>
                    Minimum viewing score
                </legend>

                <div className="viewing-score-select">
                    <CloudSun
                        aria-hidden="true"
                        size={17}
                    />

                    <select
                        value={
                            filters.minimumViewingScore ?? ''
                        }
                        disabled={
                            filters.forecastAvailable === false
                        }
                        onChange={(event) => {
                            const score = event.target.value
                                ? Number(event.target.value)
                                : null

                            onUpdate({
                                minimumViewingScore: score,
                                forecastAvailable:
                                    score === null
                                        ? filters.forecastAvailable
                                        : true,
                            })
                        }}
                        aria-label="Minimum viewing score"
                    >
                        <option value="">
                            Any score
                        </option>

                        <option value="40">
                            40 or higher
                        </option>

                        <option value="60">
                            60 or higher
                        </option>

                        <option value="80">
                            80 or higher
                        </option>
                    </select>
                </div>
            </fieldset>
        </div>
    )
}