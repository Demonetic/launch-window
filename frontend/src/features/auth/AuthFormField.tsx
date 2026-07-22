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

    return (
        <label className="auth-field">
            <span>{label}</span>

            <input
                name={name}
                type={type}
                autoComplete={autoComplete}
                minLength={minLength}
                maxLength={maxLength}
                aria-invalid={Boolean(error)}
                aria-describedby={error ? errorId : undefined}
                required
            />

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