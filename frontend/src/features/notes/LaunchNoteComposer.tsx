import {
    Send,
    X,
} from 'lucide-react'
import {
    useState,
    type FormEvent,
} from 'react'

interface LaunchNoteComposerProps {
    isCreating: boolean
    onCreate: (content: string) => Promise<unknown>
}

export function LaunchNoteComposer({
                                       isCreating,
                                       onCreate,
                                   }: LaunchNoteComposerProps) {
    const [content, setContent] = useState('')
    const [isOpen, setIsOpen] = useState(false)
    const [validationError, setValidationError] =
        useState<string | null>(null)

    function closeComposer() {
        setContent('')
        setValidationError(null)
        setIsOpen(false)
    }

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

        await onCreate(trimmedContent)
        closeComposer()
    }

    if (!isOpen) {
        return (
            <button
                className="open-note-composer"
                type="button"
                onClick={() => setIsOpen(true)}
            >
                Add a note
            </button>
        )
    }

    return (
        <form
            className="launch-note-composer"
            onSubmit={handleSubmit}
        >
            <textarea
                value={content}
                maxLength={5000}
                rows={5}
                autoFocus
                placeholder="Write a thought or reminder about this launch..."
                aria-label="New note"
                onChange={(event) => {
                    setContent(event.target.value)
                    setValidationError(null)
                }}
            />

            <div className="launch-note-composer-meta">
                <span>{content.length}/5000</span>

                {validationError && (
                    <p role="alert">
                        {validationError}
                    </p>
                )}
            </div>

            <div className="launch-note-composer-actions">
                <button
                    type="button"
                    disabled={isCreating}
                    onClick={closeComposer}
                >
                    <X aria-hidden="true" size={16} />
                    Cancel
                </button>

                <button
                    type="submit"
                    disabled={isCreating}
                >
                    <Send aria-hidden="true" size={16} />
                    {isCreating
                        ? 'Saving...'
                        : 'Save note'}
                </button>
            </div>
        </form>
    )
}