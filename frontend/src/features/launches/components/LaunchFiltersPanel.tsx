import {
    ChevronDown,
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
} from '../model/launchFilters'
import type {
    LaunchFilters,
    LaunchStatus,
} from '../model/types'
import '../styles/launchFilters.css'

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
    const [expanded, setExpanded] = useState(false)

    const active = hasActiveLaunchFilters(filters)
    const activeCount = countActiveFilterGroups(filters)

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

        setExpanded(false)
    }

    function clearFilters() {
        const cleared = copyFilters(
            DEFAULT_LAUNCH_FILTERS,
        )

        setDraft(cleared)
        onChange(cleared)
        setExpanded(false)
    }

    return (
        <form
            className={
                expanded
                    ? 'launch-filter-panel expanded'
                    : 'launch-filter-panel'
            }
            onSubmit={applyFilters}
        >
            <button
                className="launch-filter-toggle"
                type="button"
                aria-expanded={expanded}
                aria-controls="advanced-launch-filters"
                onClick={() =>
                    setExpanded((current) => !current)
                }
            >
                <span className="launch-filter-toggle-icon">
                    <SlidersHorizontal
                        aria-hidden="true"
                        size={18}
                    />
                </span>

                <span className="launch-filter-toggle-copy">
                    <strong>Filter launches</strong>
                    <small>
                        Sort, date, country, status and
                        viewing conditions
                    </small>
                </span>

                {active && (
                    <span className="active-filter-indicator">
                        <Filter
                            aria-hidden="true"
                            size={12}
                        />
                        {activeCount}{' '}
                        {activeCount === 1
                            ? 'filter'
                            : 'filters'}
                    </span>
                )}

                <ChevronDown
                    className="launch-filter-chevron"
                    aria-hidden="true"
                    size={19}
                />
            </button>

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

            {expanded && (
                <div
                    className="launch-filter-advanced"
                    id="advanced-launch-filters"
                >
                    <LaunchFilterControls
                        filters={draft}
                        onUpdate={updateDraft}
                    />

                    <LaunchCountryFilter
                        selectedCodes={
                            draft.countryCodes
                        }
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
                        {(active ||
                            hasActiveLaunchFilters(
                                draft,
                            )) && (
                            <button
                                className="clear-launch-filters"
                                type="button"
                                onClick={clearFilters}
                            >
                                <RotateCcw
                                    aria-hidden="true"
                                    size={15}
                                />
                                Clear
                            </button>
                        )}

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
                </div>
            )}
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

function countActiveFilterGroups(
    filters: LaunchFilters,
): number {
    let count = 0

    if (filters.query.trim()) {
        count += 1
    }

    if (filters.sort !== 'SOONEST') {
        count += 1
    }

    if (filters.days !== null) {
        count += 1
    }

    if (filters.forecastAvailable !== null) {
        count += 1
    }

    if (filters.minimumViewingScore !== null) {
        count += 1
    }

    if (filters.statuses.length > 0) {
        count += 1
    }

    if (filters.countryCodes.length > 0) {
        count += 1
    }

    return count
}