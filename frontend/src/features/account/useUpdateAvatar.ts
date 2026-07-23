import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../auth/useAuth'
import type { UpdateAvatarRequest } from '../auth/types'
import { updateAvatar } from './userApi'

export function useUpdateAvatar() {
    const queryClient = useQueryClient()
    const { token, updateUser } = useAuth()

    return useMutation({
        mutationFn: (request: UpdateAvatarRequest) =>
            updateAvatar(token!, request),

        onSuccess: (user) => {
            updateUser(user)

            queryClient.setQueryData(
                ['users', 'me', user.id],
                user,
            )
        },
    })
}