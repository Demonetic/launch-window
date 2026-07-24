import { UserAvatar } from '../avatar/UserAvatar'
import type { CalendarParticipant } from './types'

interface CalendarParticipantAvatarsProps {
    participants: CalendarParticipant[]
}

const MAX_VISIBLE_PARTICIPANTS = 4

export function CalendarParticipantAvatars({
                                               participants,
                                           }: CalendarParticipantAvatarsProps) {
    const visibleParticipants = participants.slice(
        0,
        MAX_VISIBLE_PARTICIPANTS,
    )

    const remainingCount =
        participants.length - visibleParticipants.length

    if (participants.length === 0) {
        return null
    }

    return (
        <div
            className="calendar-participants"
            aria-label={`Calendar participants: ${participants
                .map((participant) => participant.username)
                .join(', ')}`}
        >
            <div className="calendar-participant-avatars">
                {visibleParticipants.map((participant) => (
                    <span
                        className="calendar-participant-avatar"
                        key={participant.userId}
                        title={participant.username}
                    >
                        <UserAvatar
                            avatarKey={participant.avatarKey}
                            avatarColor={participant.avatarColor}
                            size="small"
                        />
                    </span>
                ))}

                {remainingCount > 0 && (
                    <span className="calendar-participant-overflow">
                        +{remainingCount}
                    </span>
                )}
            </div>

            <span className="calendar-participant-label">
                {participants.length === 1
                    ? participants[0].username
                    : `${participants.length} participants`}
            </span>
        </div>
    )
}