import { apiRequest } from '../../lib/api'
import type { LaunchCursor, LaunchPage } from './types'

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