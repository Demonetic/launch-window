import {
    DEFAULT_LAUNCH_FILTERS,
} from './launchFilters'
import type {
    LaunchFilters,
    LaunchSort,
    LaunchStatus,
} from './types'

const VALID_SORTS: LaunchSort[] = [
    'SOONEST',
    'BEST_VIEWING',
]

const VALID_STATUSES: LaunchStatus[] = [
    'GO',
    'TO_BE_DETERMINED',
    'TO_BE_CONFIRMED',
    'HOLD',
    'IN_FLIGHT',
    'SUCCESS',
    'PARTIAL_FAILURE',
    'FAILURE',
    'UNKNOWN',
]

export function parseLaunchFilterParams(
    parameters: URLSearchParams,
): LaunchFilters {
    return {
        sort: parseSort(parameters.get('sort')),
        days: parseNumber(
            parameters.get('days'),
            1,
            365,
        ),
        statuses: uniqueValues(
            parameters
                .getAll('statuses')
                .filter(isLaunchStatus),
        ),
        countryCodes: uniqueValues(
            parameters
                .getAll('countryCodes')
                .map((code) =>
                    code.trim().toUpperCase(),
                )
                .filter((code) =>
                    /^[A-Z]{3}$/.test(code),
                ),
        ),
        query:
            parameters
                .get('query')
                ?.trim()
                .slice(0, 100) ?? '',
        forecastAvailable: parseBoolean(
            parameters.get('forecastAvailable'),
        ),
        minimumViewingScore: parseNumber(
            parameters.get('minimumViewingScore'),
            0,
            100,
        ),
    }
}

export function createLaunchFilterParams(
    filters: LaunchFilters,
): URLSearchParams {
    const parameters = new URLSearchParams()

    if (filters.sort !== DEFAULT_LAUNCH_FILTERS.sort) {
        parameters.set('sort', filters.sort)
    }

    if (filters.days !== null) {
        parameters.set('days', String(filters.days))
    }

    filters.statuses.forEach((status) => {
        parameters.append('statuses', status)
    })

    filters.countryCodes.forEach((countryCode) => {
        parameters.append(
            'countryCodes',
            countryCode,
        )
    })

    const query = filters.query.trim()

    if (query) {
        parameters.set('query', query)
    }

    if (filters.forecastAvailable !== null) {
        parameters.set(
            'forecastAvailable',
            String(filters.forecastAvailable),
        )
    }

    if (filters.minimumViewingScore !== null) {
        parameters.set(
            'minimumViewingScore',
            String(filters.minimumViewingScore),
        )
    }

    return parameters
}

function parseSort(
    value: string | null,
): LaunchSort {
    return VALID_SORTS.includes(value as LaunchSort)
        ? (value as LaunchSort)
        : DEFAULT_LAUNCH_FILTERS.sort
}

function isLaunchStatus(
    value: string,
): value is LaunchStatus {
    return VALID_STATUSES.includes(
        value as LaunchStatus,
    )
}

function parseBoolean(
    value: string | null,
): boolean | null {
    if (value === 'true') {
        return true
    }

    if (value === 'false') {
        return false
    }

    return null
}

function parseNumber(
    value: string | null,
    minimum: number,
    maximum: number,
): number | null {
    if (value === null || value.trim() === '') {
        return null
    }

    const parsed = Number(value)

    return Number.isInteger(parsed) &&
    parsed >= minimum &&
    parsed <= maximum
        ? parsed
        : null
}

function uniqueValues<T>(values: T[]): T[] {
    return [...new Set(values)]
}