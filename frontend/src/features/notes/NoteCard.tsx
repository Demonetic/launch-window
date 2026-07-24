import {
    CalendarClock,
    Pencil,
    Rocket,
    Trash2,
} from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import { UserAvatar } from '../avatar/UserAvatar'
import { formatDateTime } from '../launches/launchPresentation'
import { NoteDeleteConfirmation } from './NoteDeleteConfirmation'
import { NoteEditor } from './NoteEditor'
import type { NoteOverview } from './types'

interface NoteCardProps {
    note: NoteOverview
    currentUserId: number | undefined
    isDeleting: boolean
    isUpdating: boolean
    onDelete: (noteId: number) => Promise<unknown>
    onUpdate: (
        noteId: number,
        content: string,
    ) => Promise<unknown>
}

export function NoteCard({
                             note,
                             currentUserId,
                             isDeleting,
                             isUpdating,
                             onDelete,
                             onUpdate,
                         }: NoteCardProps) {
    const [isEditing, setIsEditing] = useState(false)
    const [isConfirmingDelete, setIsConfirmingDelete] =
        useState(false)
    const [imageFailed, setImageFailed] = useState(false)

    const isOwnNote = note.authorId === currentUserId

    async function handleUpdate(content: string) {
        await onUpdate(note.id, content)
        setIsEditing(false)
    }

    return (
        <article
            className={
                isOwnNote
                    ? 'note-card'
                    : 'note-card note-card-shared'
            }
        >
            <header className="note-card-header">
                <Link
                    to={`/launches/${note.launchId}`}
                    state={{
                        returnLabel: 'Notes',
                        returnTo: '/notes',
                    }}
                >
                    <span className="note-launch-icon">
                        {note.imageUrl && !imageFailed ? (
                            <img
                                src={note.imageUrl}
                                alt=""
                                referrerPolicy="no-referrer"
                                onError={() =>
                                    setImageFailed(true)
                                }
                            />
                        ) : (
                            <Rocket
                                aria-hidden="true"
                                size={18}
                            />
                        )}
                    </span>

                    <span>
                        <small>
                            {note.organizationName ??
                                'Unknown organization'}
                        </small>
                        <strong>{note.launchName}</strong>
                    </span>
                </Link>

                {isOwnNote && (
                    <div className="note-actions">
                        {!isEditing && (
                            <button
                                type="button"
                                aria-label="Edit note"
                                onClick={() => {
                                    setIsConfirmingDelete(false)
                                    setIsEditing(true)
                                }}
                            >
                                <Pencil
                                    aria-hidden="true"
                                    size={16}
                                />
                            </button>
                        )}

                        {!isEditing &&
                            !isConfirmingDelete && (
                                <button
                                    type="button"
                                    aria-label="Delete note"
                                    onClick={() =>
                                        setIsConfirmingDelete(true)
                                    }
                                >
                                    <Trash2
                                        aria-hidden="true"
                                        size={16}
                                    />
                                </button>
                            )}
                    </div>
                )}
            </header>

            <div className="note-author">
                <UserAvatar
                    avatarKey={note.authorAvatarKey}
                    avatarColor={note.authorAvatarColor}
                    size="small"
                />

                <span className="note-author-details">
                    <strong>{note.authorUsername}</strong>
                    <small>
                        {isOwnNote
                            ? 'Your note'
                            : 'Shared note · Read only'}
                    </small>
                </span>
            </div>

            {isEditing && isOwnNote ? (
                <NoteEditor
                    initialContent={note.content}
                    isSaving={isUpdating}
                    onCancel={() => setIsEditing(false)}
                    onSave={handleUpdate}
                />
            ) : (
                <p className="note-content">
                    {note.content}
                </p>
            )}

            {isConfirmingDelete && isOwnNote && (
                <NoteDeleteConfirmation
                    isDeleting={isDeleting}
                    onCancel={() =>
                        setIsConfirmingDelete(false)
                    }
                    onConfirm={() => onDelete(note.id)}
                />
            )}

            <footer className="note-card-footer">
                <span>
                    <CalendarClock
                        aria-hidden="true"
                        size={14}
                    />
                    Launch {formatDateTime(note.launchTime)}
                </span>

                <span>
                    Updated {formatDateTime(note.updatedAt)}
                </span>
            </footer>
        </article>
    )
}