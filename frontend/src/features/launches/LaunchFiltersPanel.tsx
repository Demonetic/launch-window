import {
    Filter,
    RotateCcw,
    Search,
    SlidersHorizontal,
} from 'lucide-react'
import {
    useState,
    type FormEvent,
} from 'react'
import { LaunchCountryFilter } from './LaunchCountryFilter'
import { LaunchFilterControls } from './LaunchFilterControls'
import { LaunchStatusFilter } from './LaunchStatusFilter'
import {
    DEFAULT_LAUNCH_FILTERS,
    hasActiveLaunchFilters,
} from './launchFilters'
import type {
    LaunchFilters,
    LaunchStatus,
} from './types'
import './launchFilters.css'

interface LaunchFiltersPanelProps {
    filters: LaunchFilters
    onChange: (filters: LaunchFilters) => void
}

export function LaunchFiltersPanel({
                                       filters,
                                       onChange,
                                   }: LaunchFiltersPanelProps) {
    const [draft, setDraft] = useState<LaunchFilters>(
        () => copyFilters(filters),
    )

    const active = hasActiveLaunchFilters(filters)

    function updateDraft(
        update: Partial<LaunchFilters>,
    ) {
        setDraft((current) => ({
            ...current,
            ...update,
        }))
    }

    function toggleStatus(status: LaunchStatus) {
        setDraft((current) => {
            const selected =
                current.statuses.includes(status)

            return {
                ...current,
                statuses: selected
                    ? current.statuses.filter(
                        (value) => value !== status,
                    )
                    : [...current.statuses, status],
            }
        })
    }

    function applyFilters(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        onChange({
            ...draft,
            query: draft.query.trim(),
            statuses: [...draft.statuses],
            countryCodes: [...draft.countryCodes],
        })
    }

    function clearFilters() {
        const cleared = copyFilters(
            DEFAULT_LAUNCH_FILTERS,
        )

        setDraft(cleared)
        onChange(cleared)
    }

    return (
        <form
            className="launch-filter-panel"
            onSubmit={applyFilters}
        >
            <div className="launch-filter-heading">
                <div className="launch-filter-title">
                    <span className="launch-filter-icon">
                        <SlidersHorizontal
                            aria-hidden="true"
                            size={18}
                        />
                    </span>

                    <div>
                        <h2>Find a launch</h2>

                        <p>
                            Refine the full schedule by date,
                            status and viewing conditions.
                        </p>
                    </div>
                </div>

                {active && (
                    <span className="active-filter-indicator">
                        <Filter
                            aria-hidden="true"
                            size={13}
                        />
                        Filters active
                    </span>
                )}
            </div>

            <div className="launch-filter-search">
                <Search
                    aria-hidden="true"
                    size={18}
                />

                <input
                    type="search"
                    value={draft.query}
                    onChange={(event) =>
                        updateDraft({
                            query: event.target.value,
                        })
                    }
                    placeholder="Search mission, rocket, organization or location"
                    aria-label="Search upcoming launches"
                    maxLength={100}
                />
            </div>

            <LaunchFilterControls
                filters={draft}
                onUpdate={updateDraft}
            />

            <LaunchCountryFilter
                selectedCodes={draft.countryCodes}
                onChange={(countryCodes) =>
                    updateDraft({
                        countryCodes,
                    })
                }
            />

            <LaunchStatusFilter
                statuses={draft.statuses}
                onToggle={toggleStatus}
            />

            <div className="launch-filter-actions">
                <button
                    className="clear-launch-filters"
                    type="button"
                    onClick={clearFilters}
                    disabled={
                        !active &&
                        !hasActiveLaunchFilters(draft)
                    }
                >
                    <RotateCcw
                        aria-hidden="true"
                        size={15}
                    />
                    Clear filters
                </button>

                <button
                    className="apply-launch-filters"
                    type="submit"
                >
                    <Filter
                        aria-hidden="true"
                        size={16}
                    />
                    Apply filters
                </button>
            </div>
        </form>
    )
}

function copyFilters(
    filters: LaunchFilters,
): LaunchFilters {
    return {
        ...filters,
        statuses: [...filters.statuses],
        countryCodes: [...filters.countryCodes],
    }
}