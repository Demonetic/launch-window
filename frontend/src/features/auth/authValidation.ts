export interface RegisterFormValues {
    username: string
    email: string
    password: string
    confirmPassword: string
}

export type RegisterFieldErrors = Partial<
    Record<keyof RegisterFormValues, string>
>

const USERNAME_PATTERN = /^[a-zA-Z0-9._-]+$/
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateRegistration(
    values: RegisterFormValues,
): RegisterFieldErrors {
    const errors: RegisterFieldErrors = {}

    if (values.username.length < 3) {
        errors.username =
            'Username must contain at least 3 characters.'
    } else if (values.username.length > 50) {
        errors.username =
            'Username may contain at most 50 characters.'
    } else if (!USERNAME_PATTERN.test(values.username)) {
        errors.username =
            'Use letters, numbers, dots, underscores or hyphens.'
    }

    if (!EMAIL_PATTERN.test(values.email)) {
        errors.email = 'Enter a valid email address.'
    }

    if (values.password.length < 8) {
        errors.password =
            'Password must contain at least 8 characters.'
    } else if (values.password.length > 72) {
        errors.password =
            'Password may contain at most 72 characters.'
    }

    if (values.password !== values.confirmPassword) {
        errors.confirmPassword = 'Passwords do not match.'
    }

    return errors
}