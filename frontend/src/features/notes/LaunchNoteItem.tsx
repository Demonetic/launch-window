import {
    Pencil,
    Trash2,
} from 'lucide-react'
import { useState } from 'react'
import { UserAvatar } from '../avatar/UserAvatar'
import { formatDateTime } from '../launches/launchPresentation'
import { NoteDeleteConfirmation } from './NoteDeleteConfirmation'
import { NoteEditor } from './NoteEditor'
import type { LaunchNote } from './types'

interface LaunchNoteItemProps {
    note: LaunchNote
    currentUserId: number
    isDeleting: boolean
    isUpdating: boolean
    onDelete: (noteId: number) => Promise<unknown>
    onUpdate: (
        noteId: number,
        content: string,
    ) => Promise<unknown>
}

export function LaunchNoteItem({
                                   note,
                                   currentUserId,
                                   isDeleting,
                                   isUpdating,
                                   onDelete,
                                   onUpdate,
                               }: LaunchNoteItemProps) {
    const [isEditing, setIsEditing] =
        useState(false)

    const [isConfirmingDelete, setIsConfirmingDelete] =
        useState(false)

    const isOwnNote =
        note.authorId === currentUserId

    async function handleUpdate(content: string) {
        await onUpdate(note.id, content)
        setIsEditing(false)
    }

    return (
        <article className="launch-note-item">
            <header className="launch-note-item-header">
                <div className="launch-note-author">
                    <UserAvatar
                        avatarKey={note.authorAvatarKey}
                        avatarColor={
                            note.authorAvatarColor
                        }
                        size="small"
                    />

                    <div>
                        <strong>
                            {note.authorUsername}
                        </strong>

                        <span>
                            {isOwnNote
                                ? 'Your note'
                                : 'Shared note'}
                        </span>
                    </div>
                </div>

                {isOwnNote &&
                    !isEditing &&
                    !isConfirmingDelete && (
                        <div className="launch-note-actions">
                            <button
                                type="button"
                                aria-label="Edit note"
                                onClick={() =>
                                    setIsEditing(true)
                                }
                            >
                                <Pencil
                                    aria-hidden="true"
                                    size={15}
                                />
                            </button>

                            <button
                                type="button"
                                aria-label="Delete note"
                                onClick={() =>
                                    setIsConfirmingDelete(
                                        true,
                                    )
                                }
                            >
                                <Trash2
                                    aria-hidden="true"
                                    size={15}
                                />
                            </button>
                        </div>
                    )}
            </header>

            {isEditing ? (
                <NoteEditor
                    initialContent={note.content}
                    isSaving={isUpdating}
                    onCancel={() =>
                        setIsEditing(false)
                    }
                    onSave={handleUpdate}
                />
            ) : (
                <p className="launch-note-content">
                    {note.content}
                </p>
            )}

            {isConfirmingDelete && (
                <NoteDeleteConfirmation
                    isDeleting={isDeleting}
                    onCancel={() =>
                        setIsConfirmingDelete(false)
                    }
                    onConfirm={() =>
                        onDelete(note.id)
                    }
                />
            )}

            <footer className="launch-note-item-footer">
                <time dateTime={note.updatedAt}>
                    Updated{' '}
                    {formatDateTime(note.updatedAt)}
                </time>

                {!isOwnNote && (
                    <span>Read only</span>
                )}
            </footer>
        </article>
    )
}