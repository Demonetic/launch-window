import {
    type FormEvent,
    useState,
} from 'react'
import {
    Link,
    Navigate,
    useLocation,
    useNavigate,
} from 'react-router'
import { ApiClientError } from '../../lib/api'
import { AuthFormField } from './AuthFormField'
import { AuthLayout } from './AuthLayout'
import { useAuth } from './useAuth'

interface LoginLocationState {
    from?: unknown
    registered?: boolean
}

export function LoginPage() {
    const {
        login,
        isAuthenticated,
    } = useAuth()

    const navigate = useNavigate()
    const location = useLocation()
    const state =
        location.state as LoginLocationState | null

    const [error, setError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    if (isAuthenticated) {
        return <Navigate to="/" replace />
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()
        setError(null)
        setSubmitting(true)

        const form = new FormData(event.currentTarget)
        const identifier =
            String(form.get('identifier') ?? '').trim()
        const password = String(form.get('password') ?? '')

        if (!identifier || !password) {
            setError('Enter your username or email and password.')
            setSubmitting(false)
            return
        }

        try {
            await login({ identifier, password })
            navigate(getRedirectPath(state?.from), {
                replace: true,
            })
        } catch (caughtError) {
            setError(
                caughtError instanceof ApiClientError
                    ? caughtError.message
                    : 'Unable to connect to Launch Window.',
            )
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <AuthLayout
            title="Welcome back"
            description="Sign in to manage your launch calendar and notes."
            footer={
                <p>
                    New to Launch Window?{' '}
                    <Link to="/register">Create an account</Link>
                </p>
            }
        >
            {state?.registered && (
                <p className="auth-success" role="status">
                    Account created. You can sign in now.
                </p>
            )}

            {error && (
                <p className="auth-error" role="alert">
                    {error}
                </p>
            )}

            <form
                className="auth-form"
                onSubmit={handleSubmit}
                noValidate
            >
                <AuthFormField
                    label="Username or email"
                    name="identifier"
                    autoComplete="username"
                />

                <AuthFormField
                    label="Password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                />

                <button
                    className="auth-submit"
                    disabled={submitting}
                    type="submit"
                >
                    {submitting ? 'Signing in…' : 'Sign in'}
                </button>
            </form>
        </AuthLayout>
    )
}

function getRedirectPath(from: unknown): string {
    if (
        typeof from === 'string' &&
        from.startsWith('/') &&
        !from.startsWith('//')
    ) {
        return from
    }

    return '/'
}