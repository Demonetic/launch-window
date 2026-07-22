import {
    CalendarClock,
    Pencil,
    Rocket,
    Trash2,
} from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import { formatDateTime } from '../launches/launchPresentation'
import { NoteDeleteConfirmation } from './NoteDeleteConfirmation'
import { NoteEditor } from './NoteEditor'
import type { NoteOverview } from './types'

interface NoteCardProps {
    note: NoteOverview
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
                             isDeleting,
                             isUpdating,
                             onDelete,
                             onUpdate,
                         }: NoteCardProps) {
    const [isEditing, setIsEditing] =
        useState(false)
    const [isConfirmingDelete, setIsConfirmingDelete] =
        useState(false)

    async function handleUpdate(content: string) {
        await onUpdate(note.id, content)
        setIsEditing(false)
    }

    return (
        <article className="note-card">
            <header className="note-card-header">
                <Link
                    to={`/launches/${note.launchId}`}
                    state={{
                        returnLabel: 'Notes',
                        returnTo: '/notes',
                    }}
                >
                    <span className="note-launch-icon">
                        <Rocket
                            aria-hidden="true"
                            size={18}
                        />
                    </span>

                    <span>
                        <small>
                            {note.organizationName ??
                                'Unknown organization'}
                        </small>
                        <strong>{note.launchName}</strong>
                    </span>
                </Link>

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
            </header>

            {isEditing ? (
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

            {isConfirmingDelete && (
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