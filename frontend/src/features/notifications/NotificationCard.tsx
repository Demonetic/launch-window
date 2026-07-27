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

function notificationCopy(
    notification: Notification,
) {
    switch (notification.type) {
        case 'FRIEND_REQUEST_RECEIVED':
            return {
                title: 'New friend request',
                message: `${notification.actorUsername} wants to be your friend.`,
            }

        case 'FRIEND_REQUEST_ACCEPTED':
            return {
                title: 'Friend request accepted',
                message: `${notification.actorUsername} accepted your friend request.`,
            }

        case 'FRIEND_REQUEST_DECLINED':
            return {
                title: 'Friend request declined',
                message: `${notification.actorUsername} declined your friend request.`,
            }

        case 'CALENDAR_INVITATION_RECEIVED':
            return {
                title: 'Launch invitation',
                message: `${notification.actorUsername} invited you to share a launch.`,
            }

        case 'CALENDAR_INVITATION_ACCEPTED':
            return {
                title: 'Invitation accepted',
                message: `${notification.actorUsername} accepted your launch invitation.`,
            }

        case 'CALENDAR_INVITATION_DECLINED':
            return {
                title: 'Invitation declined',
                message: `${notification.actorUsername} declined your launch invitation.`,
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

    return (
        <article
            className={`notification-card${
                notification.read
                    ? ''
                    : ' notification-card-unread'
            }`}
        >
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

                <time
                    className="notification-timestamp"
                    dateTime={notification.createdAt}
                >
                    {formatTimestamp(
                        notification.createdAt,
                    )}
                </time>

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