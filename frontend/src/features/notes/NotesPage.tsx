import { NotebookPen } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { useAuth } from '../auth/useAuth'
import { NoteCard } from './NoteCard'
import { useNoteActions } from './useNoteActions'
import { useNotesOverview } from './useNotesOverview'
import './notes.css'

export function NotesPage() {
    const loadMoreRef = useRef<HTMLDivElement>(null)
    const { user } = useAuth()

    const {
        data,
        error,
        fetchNextPage,
        hasNextPage,
        isError,
        isFetchingNextPage,
        isPending,
    } = useNotesOverview()

    const {
        deleteError,
        deletingNoteId,
        updateError,
        updatingNoteId,
        deleteNote,
        updateNote,
    } = useNoteActions()

    const notes =
        data?.pages.flatMap((page) => page.items) ?? []

    useEffect(() => {
        const sentinel = loadMoreRef.current

        if (!sentinel || !hasNextPage) {
            return
        }

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (
                    entry?.isIntersecting &&
                    !isFetchingNextPage
                ) {
                    void fetchNextPage()
                }
            },
            { rootMargin: '300px' },
        )

        observer.observe(sentinel)

        return () => observer.disconnect()
    }, [
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
    ])

    const actionError = updateError ?? deleteError

    return (
        <main className="notes-page">
            <header className="notes-header">
                <div>
                    <p className="page-eyebrow">
                        Mission journal
                    </p>
                    <h1>Mission notes</h1>
                    <p>
                        Your notes and notes shared by people
                        connected to your calendar.
                    </p>
                </div>
            </header>

            {actionError && (
                <div
                    className="notes-action-error"
                    role="alert"
                >
                    {actionError instanceof Error
                        ? actionError.message
                        : 'The note could not be updated.'}
                </div>
            )}

            {isPending && (
                <div className="notes-state" role="status">
                    <span className="launch-loader" />
                    <p>Loading notes...</p>
                </div>
            )}

            {isError && (
                <div
                    className="notes-state notes-error"
                    role="alert"
                >
                    <h2>Notes could not be loaded</h2>
                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Please try again shortly.'}
                    </p>
                </div>
            )}

            {!isPending &&
                !isError &&
                notes.length === 0 && (
                    <div className="notes-state">
                        <NotebookPen
                            aria-hidden="true"
                            size={36}
                        />
                        <h2>No notes yet</h2>
                        <p>
                            Open a launch to create your first
                            mission note.
                        </p>
                    </div>
                )}

            {notes.length > 0 && (
                <section
                    className="notes-grid"
                    aria-label="Launch notes"
                >
                    {notes.map((note) => (
                        <NoteCard
                            key={note.id}
                            note={note}
                            currentUserId={user?.id}
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
                </section>
            )}

            <div
                className="notes-load-more"
                ref={loadMoreRef}
                aria-live="polite"
            >
                {isFetchingNextPage && (
                    <>
                        <span className="launch-loader" />
                        <span>Loading more notes...</span>
                    </>
                )}

                {!hasNextPage && notes.length > 0 && (
                    <span>
                        You have reached your first note.
                    </span>
                )}
            </div>
        </main>
    )
}