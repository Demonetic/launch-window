interface NoteDeleteConfirmationProps {
    isDeleting: boolean
    onCancel: () => void
    onConfirm: () => Promise<unknown>
}

export function NoteDeleteConfirmation({
                                           isDeleting,
                                           onCancel,
                                           onConfirm,
                                       }: NoteDeleteConfirmationProps) {
    return (
        <div className="note-delete-confirmation">
            <p>Delete this note permanently?</p>

            <div>
                <button
                    type="button"
                    disabled={isDeleting}
                    onClick={onCancel}
                >
                    Cancel
                </button>

                <button
                    type="button"
                    disabled={isDeleting}
                    onClick={() => void onConfirm()}
                >
                    {isDeleting
                        ? 'Deleting...'
                        : 'Delete'}
                </button>
            </div>
        </div>
    )
}