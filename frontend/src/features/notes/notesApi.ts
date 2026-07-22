import { apiRequest } from '../../lib/api'
import type {
    LaunchNote,
    NoteCursor,
    NotePage,
    NoteRequest,
} from './types'

const DEFAULT_PAGE_SIZE = 20

export function getNotesPage(
    token: string,
    cursor: NoteCursor | null,
    limit = DEFAULT_PAGE_SIZE,
): Promise<NotePage> {
    const parameters = new URLSearchParams({
        limit: String(limit),
    })

    if (cursor) {
        parameters.set(
            'beforeUpdatedAt',
            cursor.beforeUpdatedAt,
        )
        parameters.set(
            'beforeId',
            String(cursor.beforeId),
        )
    }

    return apiRequest<NotePage>(
        `/api/notes?${parameters.toString()}`,
        { token },
    )
}

export function getLaunchNotes(
    token: string,
    launchId: number,
): Promise<LaunchNote[]> {
    return apiRequest<LaunchNote[]>(
        `/api/launches/${launchId}/notes`,
        { token },
    )
}

export function createLaunchNote(
    token: string,
    launchId: number,
    request: NoteRequest,
): Promise<LaunchNote> {
    return apiRequest<LaunchNote>(
        `/api/launches/${launchId}/notes`,
        {
            method: 'POST',
            token,
            body: JSON.stringify(request),
        },
    )
}

export function updateLaunchNote(
    token: string,
    noteId: number,
    request: NoteRequest,
): Promise<LaunchNote> {
    return apiRequest<LaunchNote>(
        `/api/notes/${noteId}`,
        {
            method: 'PUT',
            token,
            body: JSON.stringify(request),
        },
    )
}

export function deleteLaunchNote(
    token: string,
    noteId: number,
): Promise<void> {
    return apiRequest<void>(
        `/api/notes/${noteId}`,
        {
            method: 'DELETE',
            token,
        },
    )
}