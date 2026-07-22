import type { AuthSession } from './types'

const STORAGE_KEY = 'launch-window-auth'

export function readAuthSession(): AuthSession | null {
    const storedSession = sessionStorage.getItem(STORAGE_KEY)

    if (!storedSession) {
        return null
    }

    try {
        const session = JSON.parse(storedSession) as AuthSession

        if (
            !session.token ||
            !session.user ||
            session.expiresAt <= Date.now()
        ) {
            clearAuthSession()
            return null
        }

        return session
    } catch {
        clearAuthSession()
        return null
    }
}

export function writeAuthSession(
    session: AuthSession,
): void {
    sessionStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(session),
    )
}

export function clearAuthSession(): void {
    sessionStorage.removeItem(STORAGE_KEY)
}