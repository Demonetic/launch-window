import {
    ApiClientError,
    apiRequest,
} from '../../lib/api'
import type {
    LaunchCursor,
    LaunchDetail,
    LaunchFilters,
    LaunchPage,
    LaunchSummary,
    LaunchWeather,
} from './types'

const DEFAULT_PAGE_SIZE = 12

export function getUpcomingLaunches(
    filters: LaunchFilters,
    cursor: LaunchCursor | null,
    limit = DEFAULT_PAGE_SIZE,
): Promise<LaunchPage> {
    const parameters = new URLSearchParams({
        limit: String(limit),
        sort: filters.sort,
    })

    if (filters.days !== null) {
        parameters.set('days', String(filters.days))
    }

    filters.statuses.forEach((status) => {
        parameters.append('statuses', status)
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

    if (cursor) {
        parameters.set('afterTime', cursor.afterTime)
        parameters.set('afterId', String(cursor.afterId))

        if (cursor.afterViewingScore !== null) {
            parameters.set(
                'afterViewingScore',
                String(cursor.afterViewingScore),
            )
        }
    }

    return apiRequest<LaunchPage>(
        `/api/launches?${parameters.toString()}`,
    )
}

export function getLaunch(
    launchId: number,
): Promise<LaunchDetail> {
    return apiRequest<LaunchDetail>(
        `/api/launches/${launchId}`,
    )
}

export async function getLaunchWeather(
    launchId: number,
): Promise<LaunchWeather | null> {
    try {
        return await apiRequest<LaunchWeather>(
            `/api/launches/${launchId}/weather`,
        )
    } catch (error) {
        if (
            error instanceof ApiClientError &&
            error.status === 404
        ) {
            return null
        }

        throw error
    }
}

export function getBestViewingLaunches(
    days = 7,
    limit = 3,
): Promise<LaunchSummary[]> {
    const parameters = new URLSearchParams({
        days: String(days),
        limit: String(limit),
    })

    return apiRequest<LaunchSummary[]>(
        `/api/launches/best-viewing?${parameters.toString()}`,
    )
}