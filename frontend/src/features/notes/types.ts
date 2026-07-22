export interface LaunchNote {
    id: number
    launchId: number
    content: string
    createdAt: string
    updatedAt: string
}

export interface NoteOverview {
    id: number
    launchId: number
    launchName: string
    launchTime: string
    organizationName: string | null
    imageUrl: string | null
    content: string
    createdAt: string
    updatedAt: string
}

export interface NoteCursor {
    beforeUpdatedAt: string
    beforeId: number
}

export interface NotePage {
    items: NoteOverview[]
    nextCursor: NoteCursor | null
    hasNext: boolean
}

export interface NoteRequest {
    content: string
}