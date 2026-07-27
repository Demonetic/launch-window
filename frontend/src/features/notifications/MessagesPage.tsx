import {
    Bell,
    UsersRound,
} from 'lucide-react'
import { Link } from 'react-router'
import { NotificationCard } from './NotificationCard'
import type { Notification } from './types'
import { useNotifications } from './useNotifications'
import './messages.css'

interface NotificationGroup {
    label: string
    notifications: Notification[]
}

function startOfDay(value: Date) {
    return new Date(
        value.getFullYear(),
        value.getMonth(),
        value.getDate(),
    ).getTime()
}

function groupNotifications(
    notifications: Notification[],
): NotificationGroup[] {
    const today = startOfDay(new Date())
    const yesterday = today - 86_400_000

    const groups: NotificationGroup[] = [
        {
            label: 'Today',
            notifications: [],
        },
        {
            label: 'Yesterday',
            notifications: [],
        },
        {
            label: 'Earlier',
            notifications: [],
        },
    ]

    notifications.forEach((notification) => {
        const notificationDay = startOfDay(
            new Date(notification.createdAt),
        )

        if (notificationDay === today) {
            groups[0].notifications.push(notification)
            return
        }

        if (notificationDay === yesterday) {
            groups[1].notifications.push(notification)
            return
        }

        groups[2].notifications.push(notification)
    })

    return groups.filter(
        (group) => group.notifications.length > 0,
    )
}

export function MessagesPage() {
    const {
        notifications,
        error,
        isError,
        isLoading,
        respondingToId,
        respond,
    } = useNotifications()

    const notificationGroups =
        groupNotifications(notifications)

    return (
        <main className="messages-page">
            <header className="messages-header">
                <div>
                    <p className="page-eyebrow">
                        Communications
                    </p>

                    <h1>Messages</h1>

                    <p>
                        Friend requests, launch invitations
                        and updates from your connections.
                    </p>
                </div>

                <Link
                    className="messages-friends-link"
                    to="/friends"
                >
                    <UsersRound
                        aria-hidden="true"
                        size={18}
                    />
                    Manage friends
                </Link>
            </header>

            {isLoading && (
                <div
                    className="messages-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <p>Loading messages...</p>
                </div>
            )}

            {isError && (
                <div
                    className="messages-state messages-error"
                    role="alert"
                >
                    <h2>
                        Messages could not be loaded
                    </h2>

                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Please try again shortly.'}
                    </p>
                </div>
            )}

            {!isLoading &&
                !isError &&
                notifications.length === 0 && (
                    <div className="messages-state">
                        <span className="messages-empty-icon">
                            <Bell
                                aria-hidden="true"
                                size={30}
                            />
                        </span>

                        <h2>No messages yet</h2>

                        <p>
                            Friend requests and launch
                            invitations will appear here.
                        </p>
                    </div>
                )}

            {notificationGroups.length > 0 && (
                <div className="message-groups">
                    {notificationGroups.map((group) => (
                        <section
                            className="message-group"
                            aria-labelledby={`message-group-${group.label}`}
                            key={group.label}
                        >
                            <div className="message-group-heading">
                                <h2
                                    id={`message-group-${group.label}`}
                                >
                                    {group.label}
                                </h2>

                                <span>
                                    {group.notifications.length}
                                </span>
                            </div>

                            <div className="messages-list">
                                {group.notifications.map(
                                    (notification) => (
                                        <NotificationCard
                                            key={
                                                notification.id
                                            }
                                            notification={
                                                notification
                                            }
                                            isResponding={
                                                respondingToId ===
                                                notification.id
                                            }
                                            onRespond={
                                                respond
                                            }
                                        />
                                    ),
                                )}
                            </div>
                        </section>
                    ))}
                </div>
            )}
        </main>
    )
}