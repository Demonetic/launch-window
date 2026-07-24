import { useMutation } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import { deleteAccount } from './userApi'

export function useDeleteAccount() {
    const { token, logout } = useAuth()

    return useMutation({
        mutationFn: (password: string) =>
            deleteAccount(token!, password),

        onSuccess: () => {
            logout()
        },
    })
}