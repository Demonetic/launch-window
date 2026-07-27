import {
    type FormEvent,
    useState,
} from 'react'
import {
    Inbox,
    Send,
    UserPlus,
    UsersRound,
} from 'lucide-react'
import { FriendshipCard } from './FriendshipCard'
import { useFriendships } from './useFriendships'
import './friends.css'

export function FriendsPage() {
    const [identifier, setIdentifier] = useState('')
    const [requestSent, setRequestSent] =
        useState(false)

    const {
        friends,
        receivedRequests,
        sentRequests,
        isPending,
        queryError,
        sendError,
        responseError,
        removeError,
        isSending,
        respondingToId,
        removingId,
        sendRequest,
        respond,
        remove,
        resetSendError,
    } = useFriendships()

    const actionError =
        sendError ??
        responseError ??
        removeError

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const normalizedIdentifier =
            identifier.trim()

        if (!normalizedIdentifier || isSending) {
            return
        }

        setRequestSent(false)

        try {
            await sendRequest(normalizedIdentifier)
            setIdentifier('')
            setRequestSent(true)
        } catch {
            // Mutation state displays the API error.
        }
    }

    async function handleResponse(
        friendshipId: number,
        decision: 'accept' | 'decline',
    ) {
        try {
            await respond(friendshipId, decision)
        } catch {
            // Mutation state displays the API error.
        }
    }

    async function handleRemove(
        friendshipId: number,
    ) {
        try {
            await remove(friendshipId)
        } catch {
            // Mutation state displays the API error.
        }
    }

    return (
        <main className="friends-page">
            <header className="friends-header">
                <div>
                    <p className="page-eyebrow">
                        Your crew
                    </p>
                    <h1>Friends</h1>
                    <p>
                        Connect with people before sharing
                        launch calendars and mission plans.
                    </p>
                </div>
            </header>

            {(queryError || actionError) && (
                <div
                    className="friends-error"
                    role="alert"
                >
                    {(
                        actionError ??
                        queryError
                    ) instanceof Error
                        ? (
                            actionError ??
                            queryError
                        )?.message
                        : 'Friends could not be updated.'}
                </div>
            )}

            {isPending && (
                <div
                    className="friends-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <p>Loading your friends...</p>
                </div>
            )}

            {!isPending && (
                <>
                    <div className="friends-tools">
                        <section className="friend-request-panel">
                            <header>
                                <span className="friend-request-icon">
                                    <UserPlus
                                        aria-hidden="true"
                                        size={19}
                                    />
                                </span>

                                <div className="friend-request-copy">
                                    <h2>Add a friend</h2>
                                    <p>
                                        Enter their exact username
                                        or email address.
                                    </p>
                                </div>
                            </header>

                            <form onSubmit={handleSubmit}>
                                <label htmlFor="friend-identifier">
                                    Username or email
                                </label>

                                <input
                                    id="friend-identifier"
                                    value={identifier}
                                    maxLength={255}
                                    autoComplete="off"
                                    placeholder="Username or email"
                                    disabled={isSending}
                                    onChange={(event) => {
                                        setIdentifier(
                                            event.target.value,
                                        )
                                        setRequestSent(false)
                                        resetSendError()
                                    }}
                                />

                                {identifier.trim().length > 0 && (
                                    <button
                                        type="submit"
                                        disabled={isSending}
                                    >
                                        <Send
                                            aria-hidden="true"
                                            size={15}
                                        />
                                        {isSending
                                            ? 'Sending…'
                                            : 'Send request'}
                                    </button>
                                )}
                            </form>

                            {requestSent && (
                                <p
                                    className="friend-request-success"
                                    role="status"
                                >
                                    Friend request sent.
                                </p>
                            )}
                        </section>

                        <FriendshipSection
                            title="Friend requests"
                            description="People waiting for your response."
                            icon={Inbox}
                            emptyMessage="No incoming requests."
                        >
                            {receivedRequests.map(
                                (friendship) => (
                                    <FriendshipCard
                                        key={friendship.id}
                                        friendship={
                                            friendship
                                        }
                                        variant="received"
                                        isPending={
                                            respondingToId ===
                                            friendship.id
                                        }
                                        onAccept={(id) =>
                                            void handleResponse(
                                                id,
                                                'accept',
                                            )
                                        }
                                        onDecline={(id) =>
                                            void handleResponse(
                                                id,
                                                'decline',
                                            )
                                        }
                                    />
                                ),
                            )}
                        </FriendshipSection>

                        <FriendshipSection
                            title="Sent requests"
                            description="Waiting for the other person."
                            icon={Send}
                            emptyMessage="No outgoing requests."
                        >
                            {sentRequests.map(
                                (friendship) => (
                                    <FriendshipCard
                                        key={friendship.id}
                                        friendship={
                                            friendship
                                        }
                                        variant="sent"
                                        isPending={
                                            removingId ===
                                            friendship.id
                                        }
                                        onRemove={(id) =>
                                            void handleRemove(id)
                                        }
                                    />
                                ),
                            )}
                        </FriendshipSection>
                    </div>

                    <div className="friends-directory">
                        <FriendshipSection
                            title="Your friends"
                            description="People you can invite to saved launches."
                            icon={UsersRound}
                            emptyMessage="You have not added any friends yet."
                        >
                            {friends.map((friendship) => (
                                <FriendshipCard
                                    key={friendship.id}
                                    friendship={friendship}
                                    variant="friend"
                                    isPending={
                                        removingId ===
                                        friendship.id
                                    }
                                    onRemove={(id) =>
                                        void handleRemove(id)
                                    }
                                />
                            ))}
                        </FriendshipSection>
                    </div>
                </>
            )}
        </main>
    )
}

interface FriendshipSectionProps {
    title: string
    description: string
    icon: typeof UsersRound
    emptyMessage: string
    children: React.ReactNode
}

function FriendshipSection({
                               title,
                               description,
                               icon: Icon,
                               emptyMessage,
                               children,
                           }: FriendshipSectionProps) {
    const isEmpty =
        Array.isArray(children) &&
        children.length === 0

    return (
        <section className="friendship-section">
            <header>
                <span>
                    <Icon
                        aria-hidden="true"
                        size={18}
                    />
                </span>

                <div>
                    <h2>{title}</h2>
                    <p>{description}</p>
                </div>
            </header>

            <div className="friendship-list">
                {isEmpty ? (
                    <p className="friendship-empty">
                        {emptyMessage}
                    </p>
                ) : (
                    children
                )}
            </div>
        </section>
    )
}