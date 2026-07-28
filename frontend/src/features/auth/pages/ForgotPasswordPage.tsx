import {
    ArrowLeft,
    MailCheck,
} from 'lucide-react'
import {
    type FormEvent,
    useState,
} from 'react'
import { Link } from 'react-router'
import { ApiClientError } from '../../../lib/api'
import { AuthFormField } from '../components/AuthFormField'
import { AuthLayout } from '../components/AuthLayout'
import { requestPasswordReset } from '../api/authApi'
import '../styles/passwordReset.css'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function ForgotPasswordPage() {
    const [emailError, setEmailError] =
        useState<string | undefined>()
    const [requestError, setRequestError] =
        useState<string | null>(null)
    const [submittedEmail, setSubmittedEmail] =
        useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const form = new FormData(event.currentTarget)
        const email = String(
            form.get('email') ?? '',
        )
            .trim()
            .toLowerCase()

        setEmailError(undefined)
        setRequestError(null)

        if (!EMAIL_PATTERN.test(email)) {
            setEmailError('Enter a valid email address.')
            return
        }

        setSubmitting(true)

        try {
            await requestPasswordReset(email)
            setSubmittedEmail(email)
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

    if (submittedEmail) {
        return (
            <AuthLayout
                title="Check your inbox"
                description="If an account exists for that email address, a password reset link is on its way."
                footer={
                    <p>
                        Remembered your password?{' '}
                        <Link to="/login">Return to login</Link>
                    </p>
                }
            >
                <div
                    className="password-reset-success"
                    role="status"
                >
                    <span>
                        <MailCheck
                            aria-hidden="true"
                            size={25}
                        />
                    </span>

                    <div>
                        <h2>Reset request received</h2>
                        <p>
                            Check <strong>{submittedEmail}</strong>{' '}
                            and follow the link in the email.
                        </p>
                        <p>
                            The link expires after 30 minutes
                            and can only be used once.
                        </p>
                    </div>
                </div>

                <button
                    className="password-reset-secondary"
                    type="button"
                    onClick={() => {
                        setSubmittedEmail(null)
                        setRequestError(null)
                    }}
                >
                    Send to another email
                </button>
            </AuthLayout>
        )
    }

    return (
        <AuthLayout
            title="Reset your password"
            description="Enter your email address. If an account exists, you will receive a password reset link."
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
                <p className="auth-error" role="alert">
                    {requestError}
                </p>
            )}

            <form
                className="auth-form"
                onSubmit={handleSubmit}
                noValidate
            >
                <AuthFormField
                    label="Email address"
                    name="email"
                    type="email"
                    autoComplete="email"
                    error={emailError}
                    maxLength={255}
                />

                <button
                    className="auth-submit"
                    disabled={submitting}
                    type="submit"
                >
                    {submitting
                        ? 'Sending reset link…'
                        : 'Send reset link'}
                </button>
            </form>
        </AuthLayout>
    )
}