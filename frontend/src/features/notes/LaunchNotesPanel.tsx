import {
    LogIn,
    NotebookPen,
} from 'lucide-react'
import {
    Link,
    useLocation,
    useNavigate,
} from 'react-router'
import { formatDateTime } from '../launches/launchPresentation'
import { LaunchNoteComposer } from './LaunchNoteComposer'
import { useLaunchNotes } from './useLaunchNotes'
import './launchNotes.css'

interface LaunchNotesPanelProps {
    launchId: number
}

export function LaunchNotesPanel({
                                     launchId,
                                 }: LaunchNotesPanelProps) {
    const location = useLocation()
    const navigate = useNavigate()

    const {
        createNote,
        error,
        isAuthenticated,
        isCreating,
        isError,
        isLoading,
        notes,
    } = useLaunchNotes(launchId)

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
                    <h2>Your notes</h2>
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
                        Sign in to save private notes about
                        this launch.
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

            {isAuthenticated &&
                !isLoading &&
                !isError &&
                notes.length === 0 && (
                    <p className="launch-notes-empty">
                        You have not written any notes for
                        this launch yet.
                    </p>
                )}

            {notes.length > 0 && (
                <div className="launch-note-list">
                    {notes.map((note) => (
                        <article key={note.id}>
                            <p>{note.content}</p>
                            <time dateTime={note.updatedAt}>
                                Updated{' '}
                                {formatDateTime(
                                    note.updatedAt,
                                )}
                            </time>
                        </article>
                    ))}
                </div>
            )}

            {isAuthenticated && notes.length > 0 && (
                <Link
                    className="all-notes-link"
                    to="/notes"
                >
                    Manage all notes
                </Link>
            )}
        </section>
    )
}