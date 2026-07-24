import {
    LogIn,
    NotebookPen,
} from 'lucide-react'
import {
    Link,
    useLocation,
    useNavigate,
} from 'react-router'
import { useAuth } from '../auth/useAuth'
import { LaunchNoteComposer } from './LaunchNoteComposer'
import { LaunchNoteItem } from './LaunchNoteItem'
import { useLaunchNotes } from './useLaunchNotes'
import { useNoteActions } from './useNoteActions'
import './launchNotes.css'

interface LaunchNotesPanelProps {
    launchId: number
}

export function LaunchNotesPanel({
                                     launchId,
                                 }: LaunchNotesPanelProps) {
    const location = useLocation()
    const navigate = useNavigate()
    const { user } = useAuth()

    const {
        createNote,
        error,
        isAuthenticated,
        isCreating,
        isError,
        isLoading,
        notes,
    } = useLaunchNotes(launchId)

    const {
        deleteError,
        deletingNoteId,
        updateError,
        updatingNoteId,
        deleteNote,
        updateNote,
    } = useNoteActions()

    const actionError =
        updateError ?? deleteError

    const hasOwnNotes =
        user !== null &&
        notes.some(
            (note) =>
                note.authorId === user.id,
        )

    function openLogin() {
        void navigate('/login', {
            state: {
                from: location.pathname,
            },
        })
    }

    return (
        <section className="detail-panel launch-notes-panel">
            <div className="launch-notes-heading">
                <div>
                    <p className="page-eyebrow">
                        Mission journal
                    </p>

                    <h2>Launch notes</h2>
                </div>

                {isAuthenticated && (
                    <LaunchNoteComposer
                        isCreating={isCreating}
                        onCreate={createNote}
                    />
                )}
            </div>

            {!isAuthenticated && (
                <div className="launch-notes-sign-in">
                    <NotebookPen
                        aria-hidden="true"
                        size={28}
                    />

                    <p>
                        Sign in to write and view notes
                        connected to this launch.
                    </p>

                    <button
                        type="button"
                        onClick={openLogin}
                    >
                        <LogIn
                            aria-hidden="true"
                            size={16}
                        />
                        Sign in
                    </button>
                </div>
            )}

            {isLoading && (
                <div
                    className="launch-notes-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <span>Loading notes...</span>
                </div>
            )}

            {isError && (
                <p
                    className="launch-notes-error"
                    role="alert"
                >
                    {error instanceof Error
                        ? error.message
                        : 'Notes could not be loaded.'}
                </p>
            )}

            {actionError && (
                <p
                    className="launch-notes-error"
                    role="alert"
                >
                    {actionError instanceof Error
                        ? actionError.message
                        : 'The note could not be updated.'}
                </p>
            )}

            {isAuthenticated &&
                !isLoading &&
                !isError &&
                notes.length === 0 && (
                    <p className="launch-notes-empty">
                        No notes have been written for
                        this launch yet.
                    </p>
                )}

            {user && notes.length > 0 && (
                <div className="launch-note-list">
                    {notes.map((note) => (
                        <LaunchNoteItem
                            key={note.id}
                            note={note}
                            currentUserId={user.id}
                            isDeleting={
                                deletingNoteId === note.id
                            }
                            isUpdating={
                                updatingNoteId === note.id
                            }
                            onDelete={deleteNote}
                            onUpdate={updateNote}
                        />
                    ))}
                </div>
            )}

            {isAuthenticated && hasOwnNotes && (
                <Link
                    className="all-notes-link"
                    to="/notes"
                >
                    Manage your notes
                </Link>
            )}
        </section>
    )
}