import {
    Eye,
    EyeOff,
} from 'lucide-react'
import {
    type InputHTMLAttributes,
    useState,
} from 'react'
import './passwordInput.css'

type PasswordInputProps = Omit<
    InputHTMLAttributes<HTMLInputElement>,
    'type'
>

export function PasswordInput(
    props: PasswordInputProps,
) {
    const [isVisible, setIsVisible] = useState(false)

    return (
        <div className="password-input">
            <input
                {...props}
                type={isVisible ? 'text' : 'password'}
            />

            <button
                type="button"
                className="password-visibility-button"
                aria-label={
                    isVisible
                        ? 'Hide password'
                        : 'Show password'
                }
                aria-pressed={isVisible}
                disabled={props.disabled}
                onClick={() =>
                    setIsVisible((visible) => !visible)
                }
            >
                {isVisible ? (
                    <EyeOff
                        aria-hidden="true"
                        size={18}
                    />
                ) : (
                    <Eye
                        aria-hidden="true"
                        size={18}
                    />
                )}
            </button>
        </div>
    )
}