export interface ResetPasswordFieldErrors {
    newPassword?: string
    confirmPassword?: string
}

export function validateResetPassword(
    newPassword: string,
    confirmPassword: string,
): ResetPasswordFieldErrors {
    const errors: ResetPasswordFieldErrors = {}

    if (newPassword.length < 8) {
        errors.newPassword =
            'Password must contain at least 8 characters.'
    } else if (newPassword.length > 72) {
        errors.newPassword =
            'Password may contain at most 72 characters.'
    }

    if (!confirmPassword) {
        errors.confirmPassword =
            'Repeat your new password.'
    } else if (newPassword !== confirmPassword) {
        errors.confirmPassword =
            'Passwords do not match.'
    }

    return errors
}