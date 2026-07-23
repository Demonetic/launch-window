import {
    type PropsWithChildren,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from 'react'
import { queryClient } from '../../app/queryClient'
import {
    login as loginRequest,
    register as registerRequest,
} from './authApi'
import {
    clearAuthSession,
    readAuthSession,
    writeAuthSession,
} from './authStorage'
import { AuthContext } from './AuthContext'
import type {
    AuthSession,
    LoginRequest,
    RegisterRequest,
    User,
} from './types'

export function AuthProvider({
                                 children,
                             }: PropsWithChildren) {
    const [session, setSession] =
        useState<AuthSession | null>(readAuthSession)

    const logout = useCallback(() => {
        clearAuthSession()
        setSession(null)
        queryClient.clear()
    }, [])

    const updateUser = useCallback((user: User) => {
        setSession((currentSession) => {
            if (!currentSession) {
                return currentSession
            }

            const nextSession: AuthSession = {
                ...currentSession,
                user,
            }

            writeAuthSession(nextSession)

            return nextSession
        })
    }, [])

    const login = useCallback(
        async (request: LoginRequest) => {
            const response = await loginRequest(request)

            const nextSession: AuthSession = {
                token: response.accessToken,
                expiresAt:
                    Date.now() +
                    response.expiresIn * 1000,
                user: response.user,
            }

            writeAuthSession(nextSession)
            setSession(nextSession)
        },
        [],
    )

    const register = useCallback(
        (request: RegisterRequest) =>
            registerRequest(request),
        [],
    )

    useEffect(() => {
        if (!session) {
            return
        }

        const remainingTime =
            session.expiresAt - Date.now()

        const timeout = window.setTimeout(
            logout,
            Math.max(remainingTime, 0),
        )

        return () => window.clearTimeout(timeout)
    }, [logout, session])

    const value = useMemo(
        () => ({
            session,
            user: session?.user ?? null,
            token: session?.token ?? null,
            isAuthenticated: session !== null,
            login,
            register,
            updateUser,
            logout,
        }),
        [
            login,
            logout,
            register,
            session,
            updateUser,
        ],
    )

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}