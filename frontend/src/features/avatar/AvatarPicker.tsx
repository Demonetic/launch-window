import {
    type FormEvent,
    useState,
} from 'react'
import { ChevronDown } from 'lucide-react'
import { useUpdateAvatar } from '../account/useUpdateAvatar'
import type { AvatarKey } from '../auth/types'
import {
    avatarColors,
    avatarOptions,
} from './avatarOptions'
import { UserAvatar } from './UserAvatar'

interface AvatarPickerProps {
    currentAvatarKey: AvatarKey
    currentAvatarColor: string
}

export function AvatarPicker({
                                 currentAvatarKey,
                                 currentAvatarColor,
                             }: AvatarPickerProps) {
    const [avatarKey, setAvatarKey] =
        useState<AvatarKey>(currentAvatarKey)
    const [avatarColor, setAvatarColor] =
        useState(currentAvatarColor)

    const updateAvatar = useUpdateAvatar()

    const hasChanges =
        avatarKey !== currentAvatarKey ||
        avatarColor.toUpperCase() !==
        currentAvatarColor.toUpperCase()

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        if (!hasChanges || updateAvatar.isPending) {
            return
        }

        updateAvatar.mutate({
            avatarKey,
            avatarColor,
        })
    }

    return (
        <form
            className="avatar-picker"
            onSubmit={handleSubmit}
        >
            <details className="avatar-picker-disclosure">
                <summary className="avatar-picker-summary">
                    <div>
                        <p className="page-eyebrow">
                            Personalize your profile
                        </p>

                        <h3>Choose your avatar</h3>

                        <p className="avatar-picker-description">
                            Select a mission character and a
                            background color.
                        </p>
                    </div>

                    <div className="avatar-picker-summary-actions">
                        <UserAvatar
                            avatarKey={avatarKey}
                            avatarColor={avatarColor}
                            size="medium"
                        />

                        <ChevronDown
                            aria-hidden="true"
                            className="avatar-picker-chevron"
                            size={21}
                        />
                    </div>
                </summary>

                <div className="avatar-picker-content">
                    <div className="avatar-picker-preview">
                        <UserAvatar
                            avatarKey={avatarKey}
                            avatarColor={avatarColor}
                            size="large"
                        />
                    </div>

                    <fieldset className="avatar-option-group">
                        <legend>Avatar</legend>

                        <div className="avatar-option-grid">
                            {avatarOptions.map((option) => {
                                const selected =
                                    option.key === avatarKey

                                return (
                                    <button
                                        aria-pressed={selected}
                                        className={
                                            `avatar-option` +
                                            (selected
                                                ? ' selected'
                                                : '')
                                        }
                                        key={option.key}
                                        onClick={() => {
                                            setAvatarKey(option.key)
                                            updateAvatar.reset()
                                        }}
                                        type="button"
                                    >
                                        <UserAvatar
                                            avatarKey={option.key}
                                            avatarColor={avatarColor}
                                            size="medium"
                                        />

                                        <span>{option.label}</span>
                                    </button>
                                )
                            })}
                        </div>
                    </fieldset>

                    <fieldset className="avatar-color-group">
                        <legend>Background color</legend>

                        <div className="avatar-color-options">
                            {avatarColors.map((color) => {
                                const selected =
                                    color.toUpperCase() ===
                                    avatarColor.toUpperCase()

                                return (
                                    <button
                                        aria-label={`Use ${color} as avatar background`}
                                        aria-pressed={selected}
                                        className={
                                            `avatar-color-option` +
                                            (selected
                                                ? ' selected'
                                                : '')
                                        }
                                        key={color}
                                        onClick={() => {
                                            setAvatarColor(color)
                                            updateAvatar.reset()
                                        }}
                                        style={{
                                            backgroundColor: color,
                                        }}
                                        type="button"
                                    />
                                )
                            })}

                            <label className="avatar-custom-color">
                                <span>Custom</span>

                                <input
                                    aria-label="Choose custom avatar color"
                                    onChange={(event) => {
                                        setAvatarColor(
                                            event.target.value.toUpperCase(),
                                        )
                                        updateAvatar.reset()
                                    }}
                                    type="color"
                                    value={avatarColor}
                                />
                            </label>
                        </div>
                    </fieldset>

                    <div className="avatar-picker-footer">
                        <div aria-live="polite">
                            {updateAvatar.isSuccess && (
                                <p className="avatar-save-success">
                                    Avatar saved.
                                </p>
                            )}

                            {updateAvatar.isError && (
                                <p className="avatar-save-error">
                                    {updateAvatar.error instanceof Error
                                        ? updateAvatar.error.message
                                        : 'Your avatar could not be saved.'}
                                </p>
                            )}
                        </div>

                        <button
                            className="avatar-save-button"
                            disabled={
                                !hasChanges ||
                                updateAvatar.isPending
                            }
                            type="submit"
                        >
                            {updateAvatar.isPending
                                ? 'Saving...'
                                : 'Save avatar'}
                        </button>
                    </div>
                </div>
            </details>
        </form>
    )
}