import {
    Bell,
    UsersRound,
} from 'lucide-react'
import { Link } from 'react-router'
import { NotificationCard } from './NotificationCard'
import { useNotifications } from './useNotifications'
import './messages.css'

export function MessagesPage() {
    const {
        notifications,
        error,
        isError,
        isLoading,
        respondingToId,
        respond,
    } = useNotifications()

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

            {notifications.length > 0 && (
                <section
                    className="messages-list"
                    aria-label="Messages"
                >
                    {notifications.map(
                        (notification) => (
                            <NotificationCard
                                key={notification.id}
                                notification={
                                    notification
                                }
                                isResponding={
                                    respondingToId ===
                                    notification.id
                                }
                                onRespond={respond}
                            />
                        ),
                    )}
                </section>
            )}
        </main>
    )
}