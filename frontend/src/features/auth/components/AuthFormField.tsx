import { PasswordInput } from './PasswordInput'

interface AuthFormFieldProps {
    label: string
    name: string
    type?: 'text' | 'email' | 'password'
    autoComplete: string
    error?: string
    minLength?: number
    maxLength?: number
}

export function AuthFormField({
                                  label,
                                  name,
                                  type = 'text',
                                  autoComplete,
                                  error,
                                  minLength,
                                  maxLength,
                              }: AuthFormFieldProps) {
    const errorId = `${name}-error`

    const inputProperties = {
        name,
        autoComplete,
        minLength,
        maxLength,
        'aria-invalid': Boolean(error),
        'aria-describedby': error
            ? errorId
            : undefined,
        required: true,
    }

    return (
        <label className="auth-field">
            <span>{label}</span>

            {type === 'password' ? (
                <PasswordInput {...inputProperties} />
            ) : (
                <input
                    {...inputProperties}
                    type={type}
                />
            )}

            {error && (
                <span
                    className="auth-field-error"
                    id={errorId}
                >
                    {error}
                </span>
            )}
        </label>
    )
}