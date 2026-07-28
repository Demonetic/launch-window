import type { LaunchFilters } from './types'

export const DEFAULT_LAUNCH_FILTERS: LaunchFilters = {
    sort: 'SOONEST',
    days: null,
    statuses: [],
    countryCodes: [],
    query: '',
    forecastAvailable: null,
    minimumViewingScore: null,
}

export function hasActiveLaunchFilters(
    filters: LaunchFilters,
): boolean {
    return (
        filters.sort !== DEFAULT_LAUNCH_FILTERS.sort ||
        filters.days !== null ||
        filters.statuses.length > 0 ||
        filters.countryCodes.length > 0 ||
        filters.query.trim().length > 0 ||
        filters.forecastAvailable !== null ||
        filters.minimumViewingScore !== null
    )
}