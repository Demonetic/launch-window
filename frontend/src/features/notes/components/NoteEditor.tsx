import {
    Check,
    X,
} from 'lucide-react'
import {
    useState,
    type FormEvent,
} from 'react'

interface NoteEditorProps {
    initialContent: string
    isSaving: boolean
    onCancel: () => void
    onSave: (content: string) => Promise<unknown>
}

export function NoteEditor({
                               initialContent,
                               isSaving,
                               onCancel,
                               onSave,
                           }: NoteEditorProps) {
    const [content, setContent] =
        useState(initialContent)
    const [validationError, setValidationError] =
        useState<string | null>(null)

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const trimmedContent = content.trim()

        if (!trimmedContent) {
            setValidationError(
                'The note cannot be empty.',
            )
            return
        }

        await onSave(trimmedContent)
    }

    return (
        <form
            className="note-edit-form"
            onSubmit={handleSubmit}
        >
            <textarea
                value={content}
                maxLength={5000}
                rows={6}
                aria-label="Note content"
                onChange={(event) => {
                    setContent(event.target.value)
                    setValidationError(null)
                }}
            />

            <div className="note-character-count">
                {content.length}/5000
            </div>

            {validationError && (
                <p role="alert">{validationError}</p>
            )}

            <div className="note-edit-actions">
                <button
                    type="button"
                    disabled={isSaving}
                    onClick={onCancel}
                >
                    <X aria-hidden="true" size={16} />
                    Cancel
                </button>

                <button
                    type="submit"
                    disabled={isSaving}
                >
                    <Check aria-hidden="true" size={16} />
                    {isSaving ? 'Saving...' : 'Save'}
                </button>
            </div>
        </form>
    )
}