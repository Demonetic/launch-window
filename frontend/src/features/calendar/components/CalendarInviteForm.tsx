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
import { Link } from 'react-router'
import { UserAvatar } from '../../avatar/UserAvatar'
import { useAcceptedFriends } from '../../friends/useAcceptedFriends'
import { useInviteToCalendar } from '../hooks/useInviteToCalendar'
import '../styles/calendarInvite.css'

interface CalendarInviteFormProps {
    launchId: number
    excludedUserIds: number[]
}

export function CalendarInviteForm({
                                       launchId,
                                       excludedUserIds,
                                   }: CalendarInviteFormProps) {
    const [isOpen, setIsOpen] = useState(false)

    const [
        selectedFriendId,
        setSelectedFriendId,
    ] = useState<number | null>(null)

    const [
        sentUsername,
        setSentUsername,
    ] = useState<string | null>(null)

    const friendsQuery =
        useAcceptedFriends(isOpen)

    const invitationMutation =
        useInviteToCalendar(launchId)

    const availableFriends =
        friendsQuery.data?.filter(
            (friendship) =>
                !excludedUserIds.includes(
                    friendship.userId,
                ),
        ) ?? []

    const selectedFriend =
        availableFriends.find(
            (friendship) =>
                friendship.userId ===
                selectedFriendId,
        ) ?? null

    function openForm() {
        invitationMutation.reset()
        setSelectedFriendId(null)
        setSentUsername(null)
        setIsOpen(true)
    }

    function closeForm() {
        if (invitationMutation.isPending) {
            return
        }

        invitationMutation.reset()
        setSelectedFriendId(null)
        setSentUsername(null)
        setIsOpen(false)
    }

    function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        if (!selectedFriend) {
            return
        }

        invitationMutation.mutate(
            selectedFriend.username,
            {
                onSuccess: () => {
                    setSentUsername(
                        selectedFriend.username,
                    )
                    setSelectedFriendId(null)
                },
            },
        )
    }

    if (!isOpen) {
        return (
            <button
                type="button"
                className="calendar-invite-toggle"
                onClick={openForm}
            >
                <UserPlus
                    aria-hidden="true"
                    size={16}
                />
                Invite a friend
            </button>
        )
    }

    return (
        <div className="calendar-invite-area">
            <div className="calendar-invite-heading">
                <div>
                    <strong>
                        Invite to this launch
                    </strong>
                    <span>
                        Choose one of your Launch Window
                        friends.
                    </span>
                </div>

                <button
                    type="button"
                    className="calendar-invite-close"
                    aria-label="Close invitation form"
                    disabled={
                        invitationMutation.isPending
                    }
                    onClick={closeForm}
                >
                    <X
                        aria-hidden="true"
                        size={16}
                    />
                </button>
            </div>

            {sentUsername ? (
                <div
                    className="calendar-invite-success"
                    role="status"
                >
                    <Check
                        aria-hidden="true"
                        size={17}
                    />

                    <span>
                        Invitation sent to{' '}
                        <strong>
                            {sentUsername}
                        </strong>
                    </span>
                </div>
            ) : (
                <>
                    {friendsQuery.isPending && (
                        <div
                            className="calendar-friend-state"
                            role="status"
                        >
                            <span className="launch-loader" />
                            <span>
                                Loading friends...
                            </span>
                        </div>
                    )}

                    {friendsQuery.isError && (
                        <p
                            className="calendar-invite-error"
                            role="alert"
                        >
                            {friendsQuery.error instanceof
                            Error
                                ? friendsQuery.error.message
                                : 'Your friends could not be loaded.'}
                        </p>
                    )}

                    {!friendsQuery.isPending &&
                        !friendsQuery.isError &&
                        availableFriends.length ===
                        0 && (
                            <div className="calendar-friend-empty">
                                <UserPlus
                                    aria-hidden="true"
                                    size={20}
                                />

                                <div>
                                    <strong>
                                        No friends available
                                    </strong>

                                    <span>
                                        Add a friend before
                                        inviting someone to
                                        this launch.
                                    </span>
                                </div>

                                <Link to="/friends">
                                    Manage friends
                                </Link>
                            </div>
                        )}

                    {availableFriends.length > 0 && (
                        <form
                            className="calendar-friend-invite-form"
                            onSubmit={handleSubmit}
                        >
                            <div
                                className="calendar-friend-options"
                                role="radiogroup"
                                aria-label="Choose a friend"
                            >
                                {availableFriends.map(
                                    (friendship) => {
                                        const selected =
                                            selectedFriendId ===
                                            friendship.userId

                                        return (
                                            <button
                                                key={
                                                    friendship.id
                                                }
                                                type="button"
                                                role="radio"
                                                aria-checked={
                                                    selected
                                                }
                                                className={`calendar-friend-option${
                                                    selected
                                                        ? ' selected'
                                                        : ''
                                                }`}
                                                disabled={
                                                    invitationMutation.isPending
                                                }
                                                onClick={() => {
                                                    invitationMutation.reset()
                                                    setSelectedFriendId(
                                                        friendship.userId,
                                                    )
                                                }}
                                            >
                                                <UserAvatar
                                                    avatarKey={
                                                        friendship.avatarKey
                                                    }
                                                    avatarColor={
                                                        friendship.avatarColor
                                                    }
                                                    size="small"
                                                />

                                                <span>
                                                    <strong>
                                                        {
                                                            friendship.username
                                                        }
                                                    </strong>
                                                    <small>
                                                        Friend
                                                    </small>
                                                </span>

                                                <span
                                                    className="calendar-friend-selection"
                                                    aria-hidden="true"
                                                >
                                                    {selected && (
                                                        <Check
                                                            size={
                                                                14
                                                            }
                                                        />
                                                    )}
                                                </span>
                                            </button>
                                        )
                                    },
                                )}
                            </div>

                            <button
                                type="submit"
                                className="calendar-friend-send"
                                disabled={
                                    !selectedFriend ||
                                    invitationMutation.isPending
                                }
                            >
                                <Send
                                    aria-hidden="true"
                                    size={15}
                                />

                                {invitationMutation.isPending
                                    ? 'Sending...'
                                    : 'Send invitation'}
                            </button>
                        </form>
                    )}
                </>
            )}

            {invitationMutation.isError && (
                <p
                    className="calendar-invite-error"
                    role="alert"
                >
                    {invitationMutation.error instanceof
                    Error
                        ? invitationMutation.error.message
                        : 'The invitation could not be sent.'}
                </p>
            )}
        </div>
    )
}