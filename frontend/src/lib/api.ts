export interface ApiErrorResponse {
    timestamp: string
    status: number
    code: string
    message: string
    path: string
    fieldErrors: Record<string, string>
}

export class ApiClientError extends Error {
    readonly status: number
    readonly details: ApiErrorResponse | null

    constructor(
        status: number,
        message: string,
        details: ApiErrorResponse | null,
    ) {
        super(message)
        this.name = 'ApiClientError'
        this.status = status
        this.details = details
    }
}

interface ApiRequestOptions extends RequestInit {
    token?: string
}

const apiBaseUrl = (
    import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
).replace(/\/$/, '')

export async function apiRequest<T>(
    path: string,
    options: ApiRequestOptions = {},
): Promise<T> {
    const { token, ...requestOptions } = options
    const headers = new Headers(requestOptions.headers)

    headers.set('Accept', 'application/json')

    if (requestOptions.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json')
    }

    if (token) {
        headers.set('Authorization', `Bearer ${token}`)
    }

    const response = await fetch(`${apiBaseUrl}${path}`, {
        ...requestOptions,
        headers,
    })

    if (!response.ok) {
        const details = await readError(response)

        throw new ApiClientError(
            response.status,
            details?.message ?? 'Request failed',
            details,
        )
    }

    if (response.status === 204) {
        return undefined as T
    }

    return response.json() as Promise<T>
}

async function readError(
    response: Response,
): Promise<ApiErrorResponse | null> {
    try {
        return (await response.json()) as ApiErrorResponse
    } catch {
        return null
    }
}