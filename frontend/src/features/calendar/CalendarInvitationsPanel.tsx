import {
    CalendarCheck,
    Check,
    X,
} from 'lucide-react'
import { UserAvatar } from '../avatar/UserAvatar'
import { useCalendarInvitations } from './useCalendarInvitations'

function formatInvitationDate(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(new Date(value))
}

export function CalendarInvitationsPanel() {
    const {
        invitations,
        error,
        isError,
        isLoading,
        isResponding,
        respondingToId,
        respond,
    } = useCalendarInvitations()

    if (isLoading) {
        return (
            <section
                className="calendar-invitations calendar-invitations-loading"
                aria-label="Calendar invitations"
            >
                <span className="launch-loader" />
                <span>Checking for invitations...</span>
            </section>
        )
    }

    if (invitations.length === 0 && !isError) {
        return null
    }

    return (
        <section
            className="calendar-invitations"
            aria-labelledby="calendar-invitations-title"
        >
            <header className="calendar-invitations-header">
                <div className="calendar-invitations-icon">
                    <CalendarCheck
                        aria-hidden="true"
                        size={20}
                    />
                </div>

                <div>
                    <p className="page-eyebrow">
                        Shared calendars
                    </p>
                    <h2 id="calendar-invitations-title">
                        Calendar invitations
                    </h2>
                </div>

                {invitations.length > 0 && (
                    <span className="calendar-invitation-count">
                        {invitations.length}
                    </span>
                )}
            </header>

            {isError && (
                <p
                    className="calendar-invitation-error"
                    role="alert"
                >
                    {error instanceof Error
                        ? error.message
                        : 'The invitation could not be updated.'}
                </p>
            )}

            <div className="calendar-invitation-list">
                {invitations.map((invitation) => {
                    const responding =
                        isResponding &&
                        respondingToId === invitation.id

                    return (
                        <article
                            className="calendar-invitation"
                            key={invitation.id}
                        >
                            <UserAvatar
                                avatarKey={
                                    invitation.inviterAvatarKey
                                }
                                avatarColor={
                                    invitation.inviterAvatarColor
                                }
                                size="small"
                            />

                            <div className="calendar-invitation-copy">
                                <p>
                                    <strong>
                                        {invitation.inviterUsername}
                                    </strong>{' '}
                                    invited you to watch
                                </p>

                                <h3>
                                    {invitation.launchName}
                                </h3>

                                <time
                                    dateTime={
                                        invitation.launchTime
                                    }
                                >
                                    {formatInvitationDate(
                                        invitation.launchTime,
                                    )}
                                </time>
                            </div>

                            <div className="calendar-invitation-actions">
                                <button
                                    type="button"
                                    className="calendar-invitation-button calendar-invitation-decline"
                                    disabled={responding}
                                    onClick={() =>
                                        respond({
                                            invitationId:
                                            invitation.id,
                                            decision: 'decline',
                                        })
                                    }
                                >
                                    <X
                                        aria-hidden="true"
                                        size={16}
                                    />
                                    Decline
                                </button>

                                <button
                                    type="button"
                                    className="calendar-invitation-button calendar-invitation-accept"
                                    disabled={responding}
                                    onClick={() =>
                                        respond({
                                            invitationId:
                                            invitation.id,
                                            decision: 'accept',
                                        })
                                    }
                                >
                                    <Check
                                        aria-hidden="true"
                                        size={16}
                                    />
                                    {responding
                                        ? 'Saving...'
                                        : 'Accept'}
                                </button>
                            </div>
                        </article>
                    )
                })}
            </div>
        </section>
    )
}