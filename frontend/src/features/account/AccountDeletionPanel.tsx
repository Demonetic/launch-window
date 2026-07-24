import {
    AlertTriangle,
    Trash2,
    X,
} from 'lucide-react'
import {
    type FormEvent,
    useId,
    useState,
} from 'react'
import { useDeleteAccount } from './useDeleteAccount'
import './accountDeletion.css'

export function AccountDeletionPanel() {
    const passwordId = useId()
    const confirmationId = useId()

    const [isOpen, setIsOpen] = useState(false)
    const [password, setPassword] = useState('')
    const [hasConfirmed, setHasConfirmed] =
        useState(false)

    const {
        error,
        isPending,
        mutateAsync,
        reset,
    } = useDeleteAccount()

    function openDialog() {
        reset()
        setPassword('')
        setHasConfirmed(false)
        setIsOpen(true)
    }

    function closeDialog() {
        if (isPending) {
            return
        }

        reset()
        setPassword('')
        setHasConfirmed(false)
        setIsOpen(false)
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        if (!password || !hasConfirmed || isPending) {
            return
        }

        try {
            await mutateAsync(password)
        } catch {
            // Mutation state renders the API error below.
        }
    }

    return (
        <>
            <section className="account-danger-zone">
                <div className="account-danger-copy">
                    <span className="account-danger-icon">
                        <AlertTriangle
                            aria-hidden="true"
                            size={20}
                        />
                    </span>

                    <div>
                        <p className="account-danger-eyebrow">
                            Danger zone
                        </p>
                        <h2>Delete your account</h2>
                        <p>
                            Permanently remove your profile,
                            calendar entries, invitations and
                            launch notes.
                        </p>
                    </div>
                </div>

                <button
                    className="account-delete-trigger"
                    type="button"
                    onClick={openDialog}
                >
                    <Trash2 aria-hidden="true" size={17} />
                    Delete account
                </button>
            </section>

            {isOpen && (
                <div
                    className="account-delete-backdrop"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            closeDialog()
                        }
                    }}
                >
                    <section
                        className="account-delete-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="delete-account-title"
                    >
                        <header>
                            <span className="account-delete-warning-icon">
                                <AlertTriangle
                                    aria-hidden="true"
                                    size={22}
                                />
                            </span>

                            <div>
                                <p>Permanent action</p>
                                <h2 id="delete-account-title">
                                    Delete your account?
                                </h2>
                            </div>

                            <button
                                className="account-delete-close"
                                type="button"
                                aria-label="Close"
                                disabled={isPending}
                                onClick={closeDialog}
                            >
                                <X
                                    aria-hidden="true"
                                    size={19}
                                />
                            </button>
                        </header>

                        <div className="account-delete-message">
                            <p>
                                This cannot be undone. Your
                                profile and all data owned by
                                your account will be permanently
                                deleted.
                            </p>

                            <ul>
                                <li>Your saved calendar launches</li>
                                <li>Your calendar invitations</li>
                                <li>Your launch notes</li>
                                <li>Your profile and avatar</li>
                            </ul>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <label
                                className="account-delete-field"
                                htmlFor={passwordId}
                            >
                                <span>Current password</span>
                                <input
                                    id={passwordId}
                                    type="password"
                                    value={password}
                                    autoComplete="current-password"
                                    placeholder="Enter your password"
                                    disabled={isPending}
                                    onChange={(event) => {
                                        setPassword(
                                            event.target.value,
                                        )
                                        reset()
                                    }}
                                />
                            </label>

                            <label
                                className="account-delete-confirmation"
                                htmlFor={confirmationId}
                            >
                                <input
                                    id={confirmationId}
                                    type="checkbox"
                                    checked={hasConfirmed}
                                    disabled={isPending}
                                    onChange={(event) =>
                                        setHasConfirmed(
                                            event.target.checked,
                                        )
                                    }
                                />

                                <span>
                                    I understand that this action
                                    is permanent and cannot be
                                    undone.
                                </span>
                            </label>

                            {error && (
                                <p
                                    className="account-delete-error"
                                    role="alert"
                                >
                                    {error instanceof Error
                                        ? error.message
                                        : 'Your account could not be deleted.'}
                                </p>
                            )}

                            <div className="account-delete-actions">
                                <button
                                    type="button"
                                    disabled={isPending}
                                    onClick={closeDialog}
                                >
                                    Keep my account
                                </button>

                                <button
                                    className="account-delete-final"
                                    type="submit"
                                    disabled={
                                        !password ||
                                        !hasConfirmed ||
                                        isPending
                                    }
                                >
                                    <Trash2
                                        aria-hidden="true"
                                        size={16}
                                    />
                                    {isPending
                                        ? 'Deleting account...'
                                        : 'Permanently delete'}
                                </button>
                            </div>
                        </form>
                    </section>
                </div>
            )}
        </>
    )
}