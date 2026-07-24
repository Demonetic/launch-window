import {
    Check,
    Send,
    UserPlus,
    X,
} from 'lucide-react'
import {
    type FormEvent,
    useState,
} from 'react'
import { useInviteToCalendar } from './useInviteToCalendar'

interface CalendarInviteFormProps {
    launchId: number
}

export function CalendarInviteForm({
                                       launchId,
                                   }: CalendarInviteFormProps) {
    const [isOpen, setIsOpen] = useState(false)
    const [identifier, setIdentifier] = useState('')
    const [sentIdentifier, setSentIdentifier] =
        useState<string | null>(null)

    const invitationMutation =
        useInviteToCalendar(launchId)

    function openForm() {
        invitationMutation.reset()
        setSentIdentifier(null)
        setIsOpen(true)
    }

    function closeForm() {
        invitationMutation.reset()
        setIdentifier('')
        setSentIdentifier(null)
        setIsOpen(false)
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        const normalizedIdentifier = identifier.trim()

        if (!normalizedIdentifier) {
            return
        }

        invitationMutation.mutate(normalizedIdentifier, {
            onSuccess: () => {
                setSentIdentifier(normalizedIdentifier)
                setIdentifier('')
            },
        })
    }

    if (!isOpen) {
        return (
            <button
                type="button"
                className="calendar-invite-toggle"
                onClick={openForm}
            >
                <UserPlus aria-hidden="true" size={16} />
                Invite someone
            </button>
        )
    }

    return (
        <div className="calendar-invite-area">
            <div className="calendar-invite-heading">
                <div>
                    <strong>Invite to this launch</strong>
                    <span>
                        Enter their username or email address.
                    </span>
                </div>

                <button
                    type="button"
                    className="calendar-invite-close"
                    aria-label="Close invitation form"
                    onClick={closeForm}
                >
                    <X aria-hidden="true" size={16} />
                </button>
            </div>

            {sentIdentifier ? (
                <div
                    className="calendar-invite-success"
                    role="status"
                >
                    <Check aria-hidden="true" size={17} />

                    <span>
                        Invitation sent to{' '}
                        <strong>{sentIdentifier}</strong>
                    </span>
                </div>
            ) : (
                <form
                    className="calendar-invite-form"
                    onSubmit={handleSubmit}
                >
                    <label
                        className="sr-only"
                        htmlFor={`calendar-invite-${launchId}`}
                    >
                        Username or email address
                    </label>

                    <input
                        id={`calendar-invite-${launchId}`}
                        type="text"
                        value={identifier}
                        autoComplete="off"
                        placeholder="Username or email"
                        disabled={invitationMutation.isPending}
                        onChange={(event) =>
                            setIdentifier(event.target.value)
                        }
                    />

                    <button
                        type="submit"
                        disabled={
                            invitationMutation.isPending ||
                            !identifier.trim()
                        }
                    >
                        <Send aria-hidden="true" size={15} />
                        {invitationMutation.isPending
                            ? 'Sending...'
                            : 'Send'}
                    </button>
                </form>
            )}

            {invitationMutation.isError && (
                <p
                    className="calendar-invite-error"
                    role="alert"
                >
                    {invitationMutation.error instanceof Error
                        ? invitationMutation.error.message
                        : 'The invitation could not be sent.'}
                </p>
            )}
        </div>
    )
}