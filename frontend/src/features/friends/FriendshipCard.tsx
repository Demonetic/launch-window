import {
    Check,
    Clock3,
    UserMinus,
    X,
} from 'lucide-react'
import { UserAvatar } from '../avatar/UserAvatar'
import type { Friendship } from './types'

type FriendshipCardVariant =
    | 'friend'
    | 'received'
    | 'sent'

interface FriendshipCardProps {
    friendship: Friendship
    variant: FriendshipCardVariant
    isPending: boolean
    onAccept?: (friendshipId: number) => void
    onDecline?: (friendshipId: number) => void
    onRemove?: (friendshipId: number) => void
}

export function FriendshipCard({
                                   friendship,
                                   variant,
                                   isPending,
                                   onAccept,
                                   onDecline,
                                   onRemove,
                               }: FriendshipCardProps) {
    return (
        <article className="friendship-card">
            <UserAvatar
                avatarKey={friendship.avatarKey}
                avatarColor={friendship.avatarColor}
                size="small"
            />

            <div className="friendship-card-copy">
                <strong>{friendship.username}</strong>

                <span>
                    {variant === 'friend' &&
                        'Launch Window friend'}

                    {variant === 'received' &&
                        'Sent you a friend request'}

                    {variant === 'sent' && (
                        <>
                            <Clock3
                                aria-hidden="true"
                                size={12}
                            />
                            Request pending
                        </>
                    )}
                </span>
            </div>

            <div className="friendship-card-actions">
                {variant === 'received' && (
                    <>
                        <button
                            type="button"
                            className="friendship-secondary-action"
                            aria-label={`Decline ${friendship.username}'s friend request`}
                            disabled={isPending}
                            onClick={() =>
                                onDecline?.(
                                    friendship.id,
                                )
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
                            className="friendship-primary-action"
                            disabled={isPending}
                            onClick={() =>
                                onAccept?.(
                                    friendship.id,
                                )
                            }
                        >
                            <Check
                                aria-hidden="true"
                                size={16}
                            />
                            {isPending
                                ? 'Responding…'
                                : 'Accept'}
                        </button>
                    </>
                )}

                {variant === 'sent' && (
                    <button
                        type="button"
                        className="friendship-secondary-action"
                        disabled={isPending}
                        onClick={() =>
                            onRemove?.(friendship.id)
                        }
                    >
                        <X
                            aria-hidden="true"
                            size={16}
                        />
                        {isPending
                            ? 'Cancelling…'
                            : 'Cancel request'}
                    </button>
                )}

                {variant === 'friend' && (
                    <button
                        type="button"
                        className="friendship-remove-action"
                        aria-label={`Remove ${friendship.username} as a friend`}
                        disabled={isPending}
                        onClick={() =>
                            onRemove?.(friendship.id)
                        }
                    >
                        <UserMinus
                            aria-hidden="true"
                            size={16}
                        />
                        {isPending
                            ? 'Removing…'
                            : 'Remove'}
                    </button>
                )}
            </div>
        </article>
    )
}