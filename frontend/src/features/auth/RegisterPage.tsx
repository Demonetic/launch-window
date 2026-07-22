import {
    type FormEvent,
    useState,
} from 'react'
import {
    Link,
    Navigate,
    useNavigate,
} from 'react-router'
import { ApiClientError } from '../../lib/api'
import { AuthFormField } from './AuthFormField'
import { AuthLayout } from './AuthLayout'
import {
    type RegisterFieldErrors,
    validateRegistration,
} from './authValidation'
import { useAuth } from './useAuth'

export function RegisterPage() {
    const {
        register,
        isAuthenticated,
    } = useAuth()

    const navigate = useNavigate()
    const [formError, setFormError] =
        useState<string | null>(null)

    const [fieldErrors, setFieldErrors] =
        useState<RegisterFieldErrors>({})

    const [submitting, setSubmitting] = useState(false)

    if (isAuthenticated) {
        return <Navigate to="/" replace />
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()
        setFormError(null)

        const form = new FormData(event.currentTarget)

        const values = {
            username: String(form.get('username') ?? '').trim(),
            email: String(form.get('email') ?? '').trim(),
            password: String(form.get('password') ?? ''),
            confirmPassword: String(
                form.get('confirmPassword') ?? '',
            ),
        }

        const validationErrors =
            validateRegistration(values)

        setFieldErrors(validationErrors)

        if (Object.keys(validationErrors).length > 0) {
            return
        }

        setSubmitting(true)

        try {
            await register({
                username: values.username,
                email: values.email,
                password: values.password,
            })

            navigate('/login', {
                replace: true,
                state: { registered: true },
            })
        } catch (caughtError) {
            if (caughtError instanceof ApiClientError) {
                setFormError(caughtError.message)
                setFieldErrors(caughtError.details?.fieldErrors ?? {})
            } else {
                setFormError(
                    'Unable to connect to Launch Window.',
                )
            }
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <AuthLayout
            title="Create your account"
            description="Save launches, follow viewing conditions and keep mission notes."
            footer={
                <p>
                    Already have an account?{' '}
                    <Link to="/login">Sign in</Link>
                </p>
            }
        >
            {formError && (
                <p className="auth-error" role="alert">
                    {formError}
                </p>
            )}

            <form
                className="auth-form"
                onSubmit={handleSubmit}
                noValidate
            >
                <AuthFormField
                    label="Username"
                    name="username"
                    autoComplete="username"
                    error={fieldErrors.username}
                    minLength={3}
                    maxLength={50}
                />

                <AuthFormField
                    label="Email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    error={fieldErrors.email}
                    maxLength={255}
                />

                <AuthFormField
                    label="Password"
                    name="password"
                    type="password"
                    autoComplete="new-password"
                    error={fieldErrors.password}
                    minLength={8}
                    maxLength={72}
                />

                <AuthFormField
                    label="Confirm password"
                    name="confirmPassword"
                    type="password"
                    autoComplete="new-password"
                    error={fieldErrors.confirmPassword}
                    minLength={8}
                    maxLength={72}
                />

                <button
                    className="auth-submit"
                    disabled={submitting}
                    type="submit"
                >
                    {submitting
                        ? 'Creating account…'
                        : 'Create account'}
                </button>
            </form>
        </AuthLayout>
    )
}