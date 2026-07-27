import {
    CalendarDays,
    Check,
    UserRound,
    X,
} from 'lucide-react'
import { Link } from 'react-router'
import { UserAvatar } from '../avatar/UserAvatar'
import type {
    Notification,
    NotificationDecision,
} from './types'

interface NotificationCardProps {
    notification: Notification
    isResponding: boolean
    onRespond: (
        notification: Notification,
        decision: NotificationDecision,
    ) => Promise<unknown>
}

interface NotificationCopy {
    title: string
    message: string
    outcome: 'accepted' | 'declined' | null
}

function formatTimestamp(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(new Date(value))
}

function formatLaunchTime(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'long',
        timeStyle: 'short',
    }).format(new Date(value))
}

function receivedFriendRequestCopy(
    notification: Notification,
): NotificationCopy {
    if (notification.friendshipStatus === 'ACCEPTED') {
        return {
            title: 'Request accepted',
            message: `You accepted ${notification.actorUsername}'s friend request.`,
            outcome: 'accepted',
        }
    }

    if (notification.friendshipStatus === 'DECLINED') {
        return {
            title: 'Request declined',
            message: `You declined ${notification.actorUsername}'s friend request.`,
            outcome: 'declined',
        }
    }

    return {
        title: 'New friend request',
        message: `${notification.actorUsername} wants to be your friend.`,
        outcome: null,
    }
}

function receivedCalendarInvitationCopy(
    notification: Notification,
): NotificationCopy {
    if (
        notification.calendarInvitationStatus ===
        'ACCEPTED'
    ) {
        return {
            title: 'Invitation accepted',
            message: `You accepted ${notification.actorUsername}'s launch invitation.`,
            outcome: 'accepted',
        }
    }

    if (
        notification.calendarInvitationStatus ===
        'DECLINED'
    ) {
        return {
            title: 'Invitation declined',
            message: `You declined ${notification.actorUsername}'s launch invitation.`,
            outcome: 'declined',
        }
    }

    return {
        title: 'Launch invitation',
        message: `${notification.actorUsername} invited you to share a launch.`,
        outcome: null,
    }
}

function notificationCopy(
    notification: Notification,
): NotificationCopy {
    switch (notification.type) {
        case 'FRIEND_REQUEST_RECEIVED':
            return receivedFriendRequestCopy(notification)

        case 'FRIEND_REQUEST_ACCEPTED':
            return {
                title: 'Friend request accepted',
                message: `${notification.actorUsername} accepted your friend request.`,
                outcome: 'accepted',
            }

        case 'FRIEND_REQUEST_DECLINED':
            return {
                title: 'Friend request declined',
                message: `${notification.actorUsername} declined your friend request.`,
                outcome: 'declined',
            }

        case 'CALENDAR_INVITATION_RECEIVED':
            return receivedCalendarInvitationCopy(
                notification,
            )

        case 'CALENDAR_INVITATION_ACCEPTED':
            return {
                title: 'Invitation accepted',
                message: `${notification.actorUsername} accepted your launch invitation.`,
                outcome: 'accepted',
            }

        case 'CALENDAR_INVITATION_DECLINED':
            return {
                title: 'Invitation declined',
                message: `${notification.actorUsername} declined your launch invitation.`,
                outcome: 'declined',
            }
    }
}

function isActionable(
    notification: Notification,
) {
    if (
        notification.type ===
        'FRIEND_REQUEST_RECEIVED'
    ) {
        return (
            notification.friendshipStatus === 'PENDING'
        )
    }

    if (
        notification.type ===
        'CALENDAR_INVITATION_RECEIVED'
    ) {
        return (
            notification.calendarInvitationStatus ===
            'PENDING'
        )
    }

    return false
}

export function NotificationCard({
                                     notification,
                                     isResponding,
                                     onRespond,
                                 }: NotificationCardProps) {
    const copy = notificationCopy(notification)
    const actionable = isActionable(notification)

    function respond(
        decision: NotificationDecision,
    ) {
        void onRespond(notification, decision)
    }

    const cardClasses = [
        'notification-card',
        notification.read
            ? ''
            : 'notification-card-unread',
        actionable
            ? 'notification-card-actionable'
            : '',
        copy.outcome
            ? `notification-card-${copy.outcome}`
            : '',
    ]
        .filter(Boolean)
        .join(' ')

    return (
        <article className={cardClasses}>
            <div className="notification-card-icon">
                {notification.type.startsWith(
                    'CALENDAR_',
                ) ? (
                    <CalendarDays
                        aria-hidden="true"
                        size={19}
                    />
                ) : (
                    <UserRound
                        aria-hidden="true"
                        size={19}
                    />
                )}
            </div>

            <UserAvatar
                avatarKey={
                    notification.actorAvatarKey
                }
                avatarColor={
                    notification.actorAvatarColor
                }
                size="small"
            />

            <div className="notification-card-copy">
                <div className="notification-card-heading">
                    <div>
                        <p className="notification-card-type">
                            {copy.title}
                        </p>

                        <h2>{copy.message}</h2>
                    </div>

                    {!notification.read && (
                        <span className="notification-unread-dot">
                            New
                        </span>
                    )}
                </div>

                {notification.launchName &&
                    notification.launchId !== null && (
                        <Link
                            className="notification-launch"
                            to={`/launches/${notification.launchId}`}
                        >
                            <strong>
                                {notification.launchName}
                            </strong>

                            {notification.launchTime && (
                                <span>
                                    {formatLaunchTime(
                                        notification.launchTime,
                                    )}
                                </span>
                            )}
                        </Link>
                    )}

                <div className="notification-card-footer">
                    <time
                        className="notification-timestamp"
                        dateTime={
                            notification.createdAt
                        }
                    >
                        {formatTimestamp(
                            notification.createdAt,
                        )}
                    </time>

                    {copy.outcome && (
                        <span
                            className={`notification-outcome notification-outcome-${copy.outcome}`}
                        >
                            {copy.outcome === 'accepted' ? (
                                <Check
                                    aria-hidden="true"
                                    size={14}
                                />
                            ) : (
                                <X
                                    aria-hidden="true"
                                    size={14}
                                />
                            )}

                            {copy.outcome === 'accepted'
                                ? 'Accepted'
                                : 'Declined'}
                        </span>
                    )}
                </div>

                {actionable && (
                    <div className="notification-actions">
                        <button
                            type="button"
                            className="notification-action notification-decline"
                            disabled={isResponding}
                            onClick={() =>
                                respond('decline')
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
                            className="notification-action notification-accept"
                            disabled={isResponding}
                            onClick={() =>
                                respond('accept')
                            }
                        >
                            <Check
                                aria-hidden="true"
                                size={16}
                            />
                            {isResponding
                                ? 'Saving...'
                                : 'Accept'}
                        </button>
                    </div>
                )}
            </div>
        </article>
    )
}