import {
    ArrowLeft,
    CheckCircle2,
    KeyRound,
} from 'lucide-react'
import {
    type FormEvent,
    useState,
} from 'react'
import {
    Link,
    useSearchParams,
} from 'react-router'
import { ApiClientError } from '../../../lib/api'
import { AuthFormField } from '../components/AuthFormField'
import { AuthLayout } from '../components/AuthLayout'
import { resetPassword } from '../api/authApi'
import {
    type ResetPasswordFieldErrors,
    validateResetPassword,
} from '../model/passwordResetValidation'
import '../styles/passwordReset.css'

export function ResetPasswordPage() {
    const [searchParams] = useSearchParams()
    const token = searchParams.get('token')?.trim() ?? ''

    const [fieldErrors, setFieldErrors] =
        useState<ResetPasswordFieldErrors>({})
    const [requestError, setRequestError] =
        useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [completed, setCompleted] = useState(false)

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const form = new FormData(event.currentTarget)
        const newPassword = String(
            form.get('newPassword') ?? '',
        )
        const confirmPassword = String(
            form.get('confirmPassword') ?? '',
        )

        const errors = validateResetPassword(
            newPassword,
            confirmPassword,
        )

        setFieldErrors(errors)
        setRequestError(null)

        if (Object.keys(errors).length > 0) {
            return
        }

        setSubmitting(true)

        try {
            await resetPassword(token, newPassword)
            setCompleted(true)
        } catch (caughtError) {
            setRequestError(
                caughtError instanceof ApiClientError
                    ? caughtError.message
                    : 'Unable to connect to Launch Window.',
            )
        } finally {
            setSubmitting(false)
        }
    }

    if (!token) {
        return (
            <AuthLayout
                title="Reset link unavailable"
                description="This password reset link does not contain a valid token."
                footer={
                    <p>
                        <Link to="/forgot-password">
                            Request a new reset link
                        </Link>
                    </p>
                }
            >
                <div
                    className="password-reset-invalid"
                    role="alert"
                >
                    <KeyRound
                        aria-hidden="true"
                        size={25}
                    />

                    <div>
                        <h2>The link is incomplete</h2>
                        <p>
                            Return to the password reset page
                            and request a new email.
                        </p>
                    </div>
                </div>
            </AuthLayout>
        )
    }

    if (completed) {
        return (
            <AuthLayout
                title="Password updated"
                description="Your new password is ready to use."
                footer={
                    <p>
                        <Link
                            to="/login"
                            state={{ passwordReset: true }}
                        >
                            Continue to login
                        </Link>
                    </p>
                }
            >
                <div
                    className="password-reset-success"
                    role="status"
                >
                    <span>
                        <CheckCircle2
                            aria-hidden="true"
                            size={25}
                        />
                    </span>

                    <div>
                        <h2>Reset complete</h2>
                        <p>
                            You can now sign in using your new
                            password.
                        </p>
                    </div>
                </div>

                <Link
                    className="auth-submit password-reset-login"
                    to="/login"
                    state={{ passwordReset: true }}
                >
                    Sign in
                </Link>
            </AuthLayout>
        )
    }

    return (
        <AuthLayout
            title="Choose a new password"
            description="Enter a new password for your Launch Window account."
            footer={
                <p>
                    <Link
                        className="password-reset-back-link"
                        to="/login"
                    >
                        <ArrowLeft
                            aria-hidden="true"
                            size={14}
                        />
                        Return to login
                    </Link>
                </p>
            }
        >
            {requestError && (
                <div className="auth-error" role="alert">
                    <strong>Unable to reset password</strong>
                    <span>{requestError}</span>

                    <Link to="/forgot-password">
                        Request a new link
                    </Link>
                </div>
            )}

            <form
                className="auth-form"
                onSubmit={handleSubmit}
                noValidate
            >
                <AuthFormField
                    label="New password"
                    name="newPassword"
                    type="password"
                    autoComplete="new-password"
                    error={fieldErrors.newPassword}
                    minLength={8}
                    maxLength={72}
                />

                <AuthFormField
                    label="Repeat new password"
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
                        ? 'Updating password…'
                        : 'Update password'}
                </button>
            </form>
        </AuthLayout>
    )
}