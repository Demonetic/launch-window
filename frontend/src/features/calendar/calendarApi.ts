import { apiRequest } from '../../lib/api'
import type {
    CalendarEntry,
    CalendarInvitation,
    CalendarPage,
    CalendarPageParameter,
    CreateCalendarInvitationRequest,
    SavedLaunchIdsResponse,
} from './types'

const DEFAULT_PAGE_SIZE = 20

export function getCalendarPage(
    token: string,
    pageParameter: CalendarPageParameter,
    limit = DEFAULT_PAGE_SIZE,
): Promise<CalendarPage> {
    const parameters = new URLSearchParams({
        limit: String(limit),
    })

    if (pageParameter) {
        const { cursor, direction } = pageParameter

        if (direction === 'previous') {
            parameters.set('beforeTime', cursor.time)
            parameters.set('beforeId', String(cursor.id))
        } else {
            parameters.set('afterTime', cursor.time)
            parameters.set('afterId', String(cursor.id))
        }
    }

    return apiRequest<CalendarPage>(
        `/api/calendar?${parameters.toString()}`,
        { token },
    )
}

export function getSavedLaunchIds(
    token: string,
    launchIds: number[],
): Promise<SavedLaunchIdsResponse> {
    const parameters = new URLSearchParams()

    for (const launchId of launchIds) {
        parameters.append('launchIds', String(launchId))
    }

    return apiRequest<SavedLaunchIdsResponse>(
        `/api/calendar/saved-launch-ids?${parameters.toString()}`,
        { token },
    )
}

export function saveLaunch(
    token: string,
    launchId: number,
): Promise<CalendarEntry> {
    return apiRequest<CalendarEntry>(
        `/api/calendar/${launchId}`,
        {
            method: 'PUT',
            token,
        },
    )
}

export function removeLaunch(
    token: string,
    launchId: number,
): Promise<void> {
    return apiRequest<void>(
        `/api/calendar/${launchId}`,
        {
            method: 'DELETE',
            token,
        },
    )
}

export function inviteToCalendar(
    token: string,
    launchId: number,
    request: CreateCalendarInvitationRequest,
): Promise<CalendarInvitation> {
    return apiRequest<CalendarInvitation>(
        `/api/calendar/${launchId}/invitations`,
        {
            method: 'POST',
            token,
            body: JSON.stringify(request),
        },
    )
}

export function getPendingCalendarInvitations(
    token: string,
): Promise<CalendarInvitation[]> {
    return apiRequest<CalendarInvitation[]>(
        '/api/calendar/invitations/pending',
        { token },
    )
}

export function acceptCalendarInvitation(
    token: string,
    invitationId: number,
): Promise<CalendarInvitation> {
    return apiRequest<CalendarInvitation>(
        `/api/calendar/invitations/${invitationId}/accept`,
        {
            method: 'PATCH',
            token,
        },
    )
}

export function declineCalendarInvitation(
    token: string,
    invitationId: number,
): Promise<CalendarInvitation> {
    return apiRequest<CalendarInvitation>(
        `/api/calendar/invitations/${invitationId}/decline`,
        {
            method: 'PATCH',
            token,
        },
    )
}