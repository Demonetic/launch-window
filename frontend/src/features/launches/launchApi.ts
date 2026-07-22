import {
    ApiClientError,
    apiRequest,
} from '../../lib/api'
import type {
    LaunchCursor,
    LaunchDetail,
    LaunchPage,
    LaunchSummary,
    LaunchWeather,
} from './types'

const DEFAULT_PAGE_SIZE = 12

export function getUpcomingLaunches(
    cursor: LaunchCursor | null,
    limit = DEFAULT_PAGE_SIZE,
): Promise<LaunchPage> {
    const parameters = new URLSearchParams({
        limit: String(limit),
    })

    if (cursor) {
        parameters.set('afterTime', cursor.afterTime)
        parameters.set('afterId', String(cursor.afterId))
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